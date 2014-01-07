package shylux.java.windowhockey;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.KeyEvent;
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
	CursorOverlay coverlay;
	
	public WindowHockey(WindowHockeyLauncher launcher, TCPConnection conn, HockeyProfile profile) {
		this.launcher = launcher;
		this.profile = profile;
		this.conn = conn;
		conn.addConnectionListener(this);
		
		// server initiates hand shake
		if (this.profile.isInitiator()) {
			conn.sendMessage(this.profile);
			WindowHockeyLauncher.LOG.fine("Sent server profile: "+this.profile.getId());
		}
	}

	public void onMessage(Object o) {
		// Initial hand shake
		if (o instanceof HockeyProfile) {
			// hand shake
			this.opponent = (HockeyProfile) o;
			WindowHockeyLauncher.LOG.fine("Got profile: "+this.opponent.getId());
			// if client answer with own profile
			if (!this.profile.isInitiator()) {
				this.conn.sendMessage(this.profile);
				WindowHockeyLauncher.LOG.fine("Sent client profile: "+this.profile.getId());
			}
			else
				// handshake done. lets start the game
				initiateGame(true);
		}
		if (o instanceof PingFrame) {
			PingFrame ping = (PingFrame) o;
			if (!ping.getSource().equals(this.profile.getId())) {
				// send back to owner
				this.conn.sendMessage(ping);
			} else {
				this.currentPing = PingFrame.calculatePing(ping);
				WindowHockeyLauncher.LOG.info("Ping: "+this.currentPing+"ms");
			}
		}
		if (o instanceof GameState) {
			// update game state
			if (this.state == null) initiateGame(false);
			if (isMaster()) return; // no one can command me!
			GameState gamestate = (GameState) o;
			state = gamestate;
			render();
		}
		if (o instanceof TransferFrame) {
			TransferFrame tframe = (TransferFrame) o;
			state = tframe.getState();
			state = GameState.processEntryPoint(state, profile.getExitBinding(), this);
			processTick();
			render();
		}
		if (o instanceof GameEndFrame) {
			GameEndFrame eframe = (GameEndFrame) o;
			endGame(this.profile.getId().equals(eframe.getWinner()));
		}
	}
	
	private void initiateGame(boolean isMaster) {
		WindowHockeyLauncher.LOG.fine("Initiate Game");
		this.puck = new Puck();
		this.state = GameState.setup(
				(isMaster)?profile.getId():opponent.getId(),
				new Vector2D(.5,.5),
				profile.getMaxPuckSpeed(),
				profile.getMaxInfluenceRate(),
				WindowHockeyUtils.generateInitialMovement(profile),
				profile.isInverted());
		this.puck.initialize(this);
		
		this.coverlay = new CursorOverlay((int) WindowHockeyUtils.getCursorSize(this.puck, this.profile));
		
		// goal
		this.goal = new Goal(profile);
		
		// start ping schedule
		exec = new WestCoastScheduledExecutor(2);
		exec.scheduleAtFixedRate(new Runnable() {
			public void run() {
				if (conn.isClosed()) return;
				conn.sendMessage(new PingFrame(profile.getId()));
			}
		}, 50, 500, TimeUnit.MILLISECONDS);

		// send game states
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
			}}, 50, 1000 / HockeyProfile.FPS, TimeUnit.MILLISECONDS);
		
		// start cursor movement
		exec.scheduleAtFixedRate(new Runnable() {
			double currentMouseAngle = 0;
			public void run() {
				if (!isActiveScreen()) return;
				if (KeyboardState.getInstance().isKeyPressed(KeyEvent.VK_CONTROL)) return;
				double mouseSpeed = 10;
				double rotationSpeed = 6;
				currentMouseAngle += rotationSpeed;
				Vector2D mouseDiff = Vector2D.fromAngle(Math.toRadians(currentMouseAngle), mouseSpeed);
				Point cursorPosition = MouseInfo.getPointerInfo().getLocation();
				Vector2D cursorMove = new Vector2D(cursorPosition).minus(mouseDiff);
				try {
					Robot r = new Robot();
					r.mouseMove((int)Math.round(cursorMove.x()), (int)Math.round(cursorMove.y()));
				} catch (AWTException e) {
					e.printStackTrace();
				}
			}
		}, 10, 17, TimeUnit.MILLISECONDS);
	}
	
	private void processTick() {
		WindowHockeyLauncher.LOG.finer(String.format("%s tick: %d\n", (isMaster())?"Calculate":"Simulate", state.getGameTick()));
		
		Vector2D nextPosition = state.getPuckPosition().plus(state.getVelocity());

		Vector2D newVelocity = WindowHockeyUtils.applyMouseForce(state.getVelocity(), profile, puck, state);
		
		if (puck.intersects(goal)) {
			// i lost.
			conn.sendMessage(new GameEndFrame(this.opponent.getId()));
			endGame(false);
		}
		
		
		// vertical collision
		if ( (nextPosition.y() < 0 && state.getVelocity().y() < 0) ||
			 (nextPosition.y()+HockeyProfile.PUCK_DIMENSIONS > 1 && state.getVelocity().y() > 0))
			 newVelocity = WindowHockeyUtils.mirrorVector(state.getVelocity(), false);
		
		
		// collision left wall
		if (nextPosition.x() < 0) {
			switch (profile.getExitBinding()) {
			case RIGHT:
				// collision with goal wall -> bounce
				if (state.getVelocity().x() < 0) // make sure puck doesn't get stuck in wall
					newVelocity = WindowHockeyUtils.mirrorVector(state.getVelocity(), true);
				break;
			case LEFT:
				// collision with exit wall -> transfer game master rights
				transferMaster();
				return;
			}
		}
		
		// collision right wall
		if (nextPosition.x() > WindowHockeyUtils.getRelativeScreenWidth(puck)) {
			switch (profile.getExitBinding()) {
			case LEFT:
				// collision with goal wall -> bounce
				if (state.getVelocity().x() > 0) // make sure puck doesn't get stuck in wall
					newVelocity = WindowHockeyUtils.mirrorVector(state.getVelocity(), true);
				break;
			case RIGHT:
				// collision with exit wall -> transfer game master rights
				transferMaster();
				return;
			}
		}
		
		state = GameState.updateBall(state, nextPosition, newVelocity);
	}
	
	private void render() {
		// check if overlay should be hidden in debug mode
		this.coverlay.setVisible(isActiveScreen());
		
		// puck update
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
		cleanUp();
		this.launcher.onGameEnd(didIWin);
	}
	
	private void abortGame(String reason) {
		WindowHockeyLauncher.LOG.info("Aborting game: "+reason);
		cleanUp();
		this.launcher.onGameEnd();
	}
	
	private void cleanUp() {
		if (this.coverlay != null) this.coverlay.close();
		if (this.puck != null) this.puck.close();
		if (this.goal != null) this.goal.close();
		if (!this.conn.isClosed()) this.conn.close();
		if (this.exec != null) exec.shutdownNow();
	}
	
	private boolean isMaster() {
		return this.state.getCurrentMaster().equals(this.profile.getId());
	}
	
	public void transferMaster() {
		if (!isMaster()) return;
		this.puck.setVisible(false);
		conn.sendMessage(new TransferFrame(state, this.opponent.getId()));
		state = GameState.updateMaster(state, this.opponent.getId());
	}
	
	/**
	 * Screen in normally active and only disabled if debug mode is active and
	 * the ball is currently on the other screen.
	 * @return
	 */
	public boolean isActiveScreen() {
		return !((profile.onlyTCP || profile.onlyUDP) && !isMaster());
	}
}
