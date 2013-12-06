package shylux.java.windowhockey;

import java.io.Serializable;
import java.util.UUID;
import java.util.logging.Level;

import com.beust.jcommander.Parameter;

/**
 * All settings for my hockey game. The values are parsed with JCommander.
 * 
 * Long parameter intentionally use only one dash to show that flags can't
 * be written together like "ls -ahl".
 * 
 * @author Shylux
 * @see http://jcommander.org/
 */
public class HockeyProfile implements Serializable {
	/**
	 * Version id. Used to if both parties have the same version of the game
	 */
	private static final long serialVersionUID = 5941466079359778114L;

	enum ExitBinding {LEFT, RIGHT}

	enum GameSpeed {
		STOP(0),
		SLOW(.75),
		NORMAL(1),
		FAST(1.5);

		private final double modifier;

		GameSpeed(double mod) {
			this.modifier = mod;
		}
		
		public double modifier() {
			return this.modifier;
		}
	}

	// Frames per second
	static final int FPS = 60;
	//
	
	// id to identify the game instance
	private UUID id;
	public UUID getId() {
		return id;
	}

	public HockeyProfile() {
		id = UUID.randomUUID();
	}
	
	@Parameter(names = {"-i", "-invert"}, description = "Makes the cursur pull the puck instead of pushing it away.")
	private boolean inverted = false;
	
	@Parameter(names = {"-left"}, description = "Set for left machine.")
	private boolean left = false;
	@Parameter(names = {"-right"}, description = "Set for right machine.")
	private boolean right = false;

	@Parameter(names = {"-p", "-port"}, description = "Specify communication port.")
	transient Integer portNumber = 8228;

	@Parameter(names = "-continue", description = "Starts searching for new game after old one ends.")
	transient boolean persistentListening = false;
	
	@Parameter(names = "-tcp", description = "DEBUG: Only TCP server.")
	transient boolean onlyTCP = false;
	
	@Parameter(names = "-udp", description = "DEBUG: Only UDP server.")
	transient boolean onlyUDP = false;
	
	@Parameter(names = "-log", description = "DEBUG: Define log level.")
	transient String loglevel = Level.INFO.getName();
	
	// Size of puck/goal relative to screen height
	static final double PUCK_DIMENSIONS = 0.1;
	static final double[] GOAL_SIZE = new double[] {.05, .2};
	
	// marks the game instance which connected the the other instance.
	// used for profile exchange protocol
	private boolean initiator = false;
	public boolean isInitiator() {
		return initiator;
	}
	public void setInitiator(boolean newState) {
		initiator = newState;
	}
	
	public ExitBinding getExitBinding() throws RuntimeException {
//		Such beautiful code..
//		for (ExitBinding binding: ExitBinding.values()) {
//			if (binding.name().equalsIgnoreCase(exitBinding)) return binding;
//		}
		if (right)
			return ExitBinding.LEFT;
		else if (left)
			return ExitBinding.RIGHT;
		else
			throw new RuntimeException("Not specified if this is left or right machine.");
	}
	
	public Level getLogLevel() {
		return Level.parse(loglevel);
	}
	
	/* ## GAME SPEED SETTINGS ## */
	@Parameter(names = "-stop", description = "DEBUG: Stops puck.")
	private boolean speed_stop = false;
	@Parameter(names = "-slow", description = "Slow game speed.")
	private boolean speed_slow = false;
	@Parameter(names = "-fast", description = "Fast game speed.")
	private boolean speed_fast = false;
	
	private GameSpeed getGameSpeed() {
		if (speed_stop)
			return GameSpeed.STOP;
		if (speed_slow)
			return GameSpeed.SLOW;
		if (speed_fast)
			return GameSpeed.FAST;
		return GameSpeed.NORMAL;
	}
	
	private double mouseInfluenceRadius = .3;
	public double getMouseInfluenceRadius() {
		return mouseInfluenceRadius;
	}
	
	private double mouseMaxInfluenceRate = .007;
	public double getMaxInfluenceRate() {
		return mouseMaxInfluenceRate * getGameSpeed().modifier();
	}
	
	private double maxPuckSpeed = 0.03;
	public double getMaxPuckSpeed() {
		return maxPuckSpeed * getGameSpeed().modifier();
	}
}
