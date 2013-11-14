package shylux.java.windowhockey;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import shylux.java.network.Connection;
import shylux.java.network.IConnectionListener;
import shylux.java.windowhockey.network.PingFrame;
import srasul.WestCoastScheduledExecutor;

public class WindowHockey implements IConnectionListener {
	Connection conn;
	HockeyProfile profile;
	HockeyProfile opponent;
	ScheduledExecutorService exec;
	
	long currentPing;
	
	WindowHockeyLauncher launcher;
	GameState state;

	Puck puck;
	
	public WindowHockey(WindowHockeyLauncher launcher, Connection conn, HockeyProfile profile) {
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

	@Override
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
			GameState gamestate = (GameState) o;
			//System.out.println("Got state: "+gamestate.getGameTick()+gamestate.getPuckPosition());
			state = gamestate;
			render();
		}
	}
	
	private void initiateGame(boolean isMaster) {
		System.out.println("Initiate Game");
		this.puck = new Puck();
		this.state = new GameState(this, (isMaster)?profile.id:opponent.id);
		this.puck.initialize(this);
		
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
				boolean isMaster = state.getCurrentMaster().equals(profile.id);
				processTick();
				if (isMaster) {
					conn.sendMessage(state);
				}
				render();
			}}, 50, 1000 / profile.fps, TimeUnit.MILLISECONDS);

		// simulate running game
		exec.schedule(new Runnable() {
			public void run() {
				if (conn.isClosed()) return;
				abortGame("debug");
			}}, 5, TimeUnit.SECONDS);
		
	}
	
	private void processTick() {
		System.out.println("Calculate tick: "+state.getGameTick());
		
		Vector2D nextPosition = state.getPuckPosition().plus(state.getVelocity());
		
		Vector2D newVelocity = state.getVelocity();
		// vertical collision
		if (nextPosition.y() < 0 || nextPosition.y()+profile.puckDimensions > 1) newVelocity = WindowHockeyUtils.mirrorVector(state.getVelocity(), false);
		
		
		boolean doTransfer = false;
		// collision east wall
		if (nextPosition.x() > WindowHockeyUtils.getRelativeScreenWidth(puck)) {
			switch (profile.getExitBinding()) {
			case WEST:
				// collision with goal wall -> bounce
				newVelocity = WindowHockeyUtils.mirrorVector(state.getVelocity(), true);
				break;
			case EAST:
				// collision with exit wall -> transfer game master rights
				System.out.println("Collision each, transfer!");
				doTransfer = true;
			}
		}
		
		// collision west wall
		if (nextPosition.x()+profile.puckDimensions < 0) {
			switch (profile.getExitBinding()) {
			case EAST:
				// collision with goal wall -> bounce
				newVelocity = WindowHockeyUtils.mirrorVector(state.getVelocity(), true);
			case WEST:
				// collision with exit wall -> transfer game master rights
				System.out.println("Collision west, transfer!");
				doTransfer = true;
			}
		}
		
		if (doTransfer) {
			state = GameState.update(state, this.opponent.id);
		} else {
			state = GameState.update(state, nextPosition, newVelocity);
		}
	}
	
	private void render() {
		WindowHockeyUtils.applyPuckLocation(state, puck);
	}

	@Override
	public void onClose() {
		abortGame("Connection closed.");
	}
	
	public void abortGame(String reason) {
		System.err.format("Aborting game: %s\n", reason);
		if (!this.conn.isClosed()) this.conn.close();
		exec.shutdownNow();
		this.puck.dispose();
		this.launcher.onGameEnd();
	}
}
