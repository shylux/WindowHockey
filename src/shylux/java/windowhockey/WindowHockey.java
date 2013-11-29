package shylux.java.windowhockey;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import shylux.java.network.IConnectionListener;
import shylux.java.network.TCPConnection;
import shylux.java.windowhockey.network.GameEndFrame;
import shylux.java.windowhockey.network.PingFrame;
import shylux.java.windowhockey.network.TransferFrame;
import srasul.WestCoastScheduledExecutor;

public class WindowHockey implements IConnectionListener {
	enum PowerUp {Unstoppable}
	
	TCPConnection conn;
	HockeyProfile profile;
	HockeyProfile opponent;
	ScheduledExecutorService exec;
	
	long currentPing;
	
	WindowHockeyLauncher launcher;
	GameState state;

	Puck puck;
	Goal goal;
	
	public WindowHockey(WindowHockeyLauncher launcher, TCPConnection conn, HockeyProfile profile) {
		this.launcher = launcher;
		this.profile = profile;
		this.conn = conn;
		conn.addConnectionListener(this);
		
		// server initiates hand shake
		if (this.profile.isServer()) {
			conn.sendMessage(this.profile);
			System.out.println("Sent server profile: "+this.profile.username);
		}
		System.out.println("I am a server? "+profile.isServer());
	}

	public void onMessage(Object o) {
		//System.out.println("got object "+o.getClass().toString());
		// Initial hand shake
		if (o instanceof HockeyProfile) {
			// hand shake
			this.opponent = (HockeyProfile) o;
			System.out.println("Got profile: "+this.opponent.username);
			// if client answer with own profile
			if (!this.profile.isServer()) {
				this.conn.sendMessage(this.profile);
				System.out.println("Sent client profile: "+this.profile.username);
			}
			else
				// handshake done. lets start the game
				initiateGame(true);
		}
		if (o instanceof PingFrame) {
			PingFrame ping = (PingFrame) o;
			if (!ping.getSource().equals(this.profile.id)) {
				// send back to owner
				this.conn.sendMessage(ping);
			} else {
				this.currentPing = PingFrame.calculatePing(ping);
				System.out.format("Ping: %dms\n", this.currentPing);
			}
		}
		if (o instanceof GameState) {
			// update game state
			if (this.state == null) initiateGame(false);
			if (isMaster()) return; // no one can command me!
			GameState gamestate = (GameState) o;
			//System.out.println("Got state: "+gamestate.getGameTick()+gamestate.getPuckPosition());
			state = gamestate;
			render();
		}
		if (o instanceof TransferFrame) {
			TransferFrame tframe = (TransferFrame) o;
			state = tframe.getState();
			state = GameState.processEntryPoint(state, profile.getExitBinding(), this);
			processTick();
			puck.setVisible(true);
			render();
		}
		if (o instanceof GameEndFrame) {
			GameEndFrame eframe = (GameEndFrame) o;
			endGame(this.profile.id.equals(eframe.getWinner()));
		}
	}
	
	private void initiateGame(boolean isMaster) {
		System.out.println("Initiate Game");
		this.puck = new Puck();
		this.state = GameState.setup((isMaster)?profile.id:opponent.id, new Vector2D(.5,.5), Vector2D.fromAngle(Math.random()*Math.PI*2,.005));
		this.puck.initialize(this);
		
		// goal
		this.goal = new Goal(profile);
		
		// start ping schedule
		exec = new WestCoastScheduledExecutor(2);
		exec.scheduleAtFixedRate(new Runnable() {
			public void run() {
				if (conn.isClosed()) return;
				conn.sendMessage(new PingFrame(profile.id));
			}
		}, 50, 500, TimeUnit.MILLISECONDS);

		exec.scheduleWithFixedDelay(new Runnable() {
			public void run() {
				if (conn.isClosed()) return;
				// One game tick
				// current master may change on tick.. not very elegant :P
				boolean isMaster = isMaster();
				processTick();
				if (isMaster) {
					conn.sendMessage(state);
				}
				render();
			}}, 50, 1000 / profile.fps, TimeUnit.MILLISECONDS);
	}
	
	private void processTick() {
		//System.out.format("%s tick: %d\n", (isMaster())?"Calculate":"Simulate", state.getGameTick());
		
		Vector2D nextPosition = state.getPuckPosition().plus(state.getVelocity());

		Vector2D newVelocity = WindowHockeyUtils.applyMouseForce(profile, puck, state);
		
		if (puck.intersects(goal)) {
			// i lost.
			conn.sendMessage(new GameEndFrame(this.opponent.id));
			endGame(false);
		}
		
		// vertical collision
		if (nextPosition.y() < 0 || nextPosition.y()+profile.puckDimensions > 1) newVelocity = WindowHockeyUtils.mirrorVector(state.getVelocity(), false);
		
		
		// collision west wall
		if (nextPosition.x() < 0) {
			switch (profile.getExitBinding()) {
			case RIGHT:
				// collision with goal wall -> bounce
				newVelocity = WindowHockeyUtils.mirrorVector(state.getVelocity(), true);
				break;
			case LEFT:
				// collision with exit wall -> transfer game master rights
				System.out.println("Collision west, transfer!");
				transferMaster();
				return;
			}
		}
		
		// collision east wall
		if (nextPosition.x() > WindowHockeyUtils.getRelativeScreenWidth(puck)) {
			switch (profile.getExitBinding()) {
			case LEFT:
				// collision with goal wall -> bounce
				newVelocity = WindowHockeyUtils.mirrorVector(state.getVelocity(), true);
				break;
			case RIGHT:
				// collision with exit wall -> transfer game master rights
				System.out.println("Collision each, transfer!");
				transferMaster();
				return;
			}
		}
		
		state = GameState.updateBall(state, nextPosition, newVelocity);
	}
	
	private void render() {
		if (isMaster()) {
			puck.setVisible(true);
			WindowHockeyUtils.applyPuckLocation(state, puck);
		} else
			puck.setVisible(false);
	}

	public void onClose() {
		abortGame("Connection closed.");
	}
	
	private void endGame(boolean didIWin) {
		System.out.println((didIWin)?"Horrray im a winner":"Meh...");
		cleanUp();
		this.launcher.onGameEnd(didIWin);
	}
	
	private void abortGame(String reason) {
		System.err.format("Aborting game: %s\n", reason);
		cleanUp();
		this.launcher.onGameEnd();
	}
	
	private void cleanUp() {
		this.puck.close();
		this.goal.close();
		if (!this.conn.isClosed()) this.conn.close();
		exec.shutdownNow();
	}
	
	private boolean isMaster() {
		return this.state.getCurrentMaster().equals(this.profile.id);
	}
	
	public void transferMaster() {
		if (!isMaster()) return;
		conn.sendMessage(new TransferFrame(state, this.opponent.id));
		state = GameState.updateMaster(state, this.opponent.id);
	}
}
