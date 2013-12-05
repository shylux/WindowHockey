package shylux.java.windowhockey;

import java.io.Serializable;
import java.util.UUID;

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

	// Frames per second
	static final int FPS = 60;
	//
	
	// id to identify the game instance
	UUID id;

	public HockeyProfile() {
		id = UUID.randomUUID();
	}
	
	@Parameter(names = {"-i", "-invert"}, description = "Makes the cursur pull the puck instead of pushing it away.")
	private boolean inverted = false;
	
	@Parameter(names = {"-left"}, description = "Left Machine.")
	private boolean left = false;
	@Parameter(names = {"-right"}, description = "Right Machine.")
	private boolean right = false;

	@Parameter(names = {"-p", "-port"}, description = "Port to listen or if host defined port on target host.")
	Integer portNumber = 8228;

	@Parameter(names = "-persistent", description = "Continue listening after game end.")
	transient boolean persistentListening = false;
	
	@Parameter(names = "-tcp", description = "Only TCP server.")
	transient boolean onlyTCP = false;
	
	@Parameter(names = "-udp", description = "Only UDP server.")
	transient boolean onlyUDP = false;
	
	// Size of puck relative to screen height
	static final double PUCK_DIMENSIONS = 0.1;
	double mouseInfluenceRadius = .3;
	double mouseMaxInfluenceRate = .007;
	double maxPuckSpeed = 0.03;
	// x y
	double[] goalSize = new double[] {.05, .2};
	
	boolean initiator = false;
	public boolean isInitiator() {
		return initiator;
	}
	
	// the hidden in the annotation does hide it from help text. nothing to do with actual value.
	@Parameter(names = "-hidden", hidden = true)
	transient boolean hidden = false;
	
	public ExitBinding getExitBinding() {
//		Such beautiful code..
//		for (ExitBinding binding: ExitBinding.values()) {
//			if (binding.name().equalsIgnoreCase(exitBinding)) return binding;
//		}
		if (right)
			return ExitBinding.LEFT;
		else if (left)
			return ExitBinding.RIGHT;
		else
			return null;
	}
}
