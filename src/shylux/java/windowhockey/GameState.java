package shylux.java.windowhockey;

import java.io.Serializable;
import java.util.UUID;

import shylux.java.windowhockey.HockeyProfile.ExitBinding;


public class GameState implements Serializable, Cloneable {

	private static final long serialVersionUID = -8222913770464185282L;

	public GameState(long tick, UUID master, Vector2D position, Vector2D velocity) {
		this.gameTick = tick;
		this.currentMaster = master;
		this.puckPosition = position;
		this.velocity = velocity;
	}
	
	public GameState(WindowHockey game, UUID master) {
		this(0, master, new Vector2D(0.0, 0.0), new Vector2D(0.01, 0.01));
	}
	
	public static GameState update(GameState state, Vector2D newPosition, Vector2D newVelocity) {
		return new GameState(state.getGameTick()+1, state.getCurrentMaster(), newPosition, newVelocity);
	}
	
	public static GameState update(GameState state, UUID newMaster) {
		GameState newState = update(state, state.getPuckPosition(), state.getVelocity());
		newState.currentMaster = newMaster;
		return newState;
	}
	
	public static GameState processEntryPoint(GameState state, ExitBinding direction, WindowHockey parent) {
		Vector2D newPosition = null;
		Vector2D newVelocity = null;
		switch (direction) {
		case WEST:
			newPosition = new Vector2D(0, state.getPuckPosition().y());
			newVelocity = (state.getVelocity().x() > 0) ? state.getVelocity() : new Vector2D(-state.getVelocity().x(), state.getVelocity().y());
			break;
		case EAST:
			newPosition = new Vector2D(WindowHockeyUtils.getRelativeScreenWidth(parent.puck)-parent.profile.puckDimensions, state.getPuckPosition().y());
			newVelocity = (state.getVelocity().x() < 0) ? state.getVelocity() : new Vector2D(-state.getVelocity().x(), state.getVelocity().y());
			break;
		}
		return GameState.update(state, newPosition, newVelocity);
	}
	
	private long gameTick = 0;

	private UUID currentMaster;
	
	private Vector2D puckPosition;
	
	// velocity goes relative to puck size ant thus relative to screen height
	private Vector2D velocity;

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
}
