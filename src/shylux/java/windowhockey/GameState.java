package shylux.java.windowhockey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import shylux.java.windowhockey.HockeyProfile.ExitBinding;
import shylux.java.windowhockey.WindowHockey.PowerUp;


public class GameState implements Serializable, Cloneable {

	private static final long serialVersionUID = -8222913770464185282L;
	
	private long gameTick = 0;

	private UUID currentMaster;
	
	private Vector2D puckPosition;
	
	// velocity goes relative to puck size ant thus relative to screen height
	private Vector2D velocity;
	
	List<PowerUp> powerups;

	private GameState(long tick, UUID master, Vector2D position, Vector2D velocity, List<PowerUp> powerups) {
		this.gameTick = tick;
		this.currentMaster = master;
		this.puckPosition = position;
		this.velocity = velocity;
		this.powerups = powerups;
	}
	
	// copy constructor
	private GameState(GameState state) {
		this(state.getGameTick()+1, state.getCurrentMaster(), state.getPuckPosition(), state.getVelocity(), state.getPowerUps());
	}
	
	public static GameState setup(UUID master, Vector2D position, Vector2D velocity) {
		return new GameState(0, master, position, velocity, new ArrayList<PowerUp>());
	}
	
	/* UPDATES */
	public static GameState updateBall(GameState state, Vector2D newPosition, Vector2D newVelocity) {
		GameState newState = new GameState(state);
		newState.puckPosition = newPosition;
		newState.velocity = newVelocity;
		return newState;
	}
	
	public static GameState updateMaster(GameState state, UUID newMaster) {
		GameState newState = new GameState(state);
		newState.currentMaster = newMaster;
		return newState;
	}
	
	public static GameState addPowerUp(GameState state, PowerUp powerup) {
		GameState newState = new GameState(state);
		newState.powerups.add(powerup);
		return newState;
	}
	public static GameState removePowerUp(GameState state, PowerUp powerup) {
		GameState newState = new GameState(state);
		newState.powerups.remove(powerup);
		return newState;
	}
	
	public static GameState processEntryPoint(GameState state, ExitBinding direction, WindowHockey parent) {
		Vector2D newPosition = null;
		Vector2D newVelocity = null;
		switch (direction) {
		case LEFT:
			newPosition = new Vector2D(0, state.getPuckPosition().y());
			newVelocity = (state.getVelocity().x() > 0) ? state.getVelocity() : new Vector2D(-state.getVelocity().x(), state.getVelocity().y());
			break;
		case RIGHT:
			newPosition = new Vector2D(WindowHockeyUtils.getRelativeScreenWidth(parent.puck)-parent.profile.puckDimensions, state.getPuckPosition().y());
			newVelocity = (state.getVelocity().x() < 0) ? state.getVelocity() : new Vector2D(-state.getVelocity().x(), state.getVelocity().y());
			break;
		}
		return GameState.updateBall(state, newPosition, newVelocity);
	}

	public long getGameTick() {
		return gameTick;
	}
	
	public UUID getCurrentMaster() {
		return currentMaster;
	}

	public Vector2D getPuckPosition() {
		return puckPosition;
	}

	public Vector2D getVelocity() {
		return velocity;
	}
	
	public List<PowerUp> getPowerUps() {
		return powerups;
	}
}
