package shylux.java.windowhockey;

import java.io.Serializable;
import java.util.UUID;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

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

	enum ExitBinding {EAST, WEST}
	
	// SETTINGS NOT MEANT TO CHANGE
	public final double puckDimensions = 0.1;
	
	@Parameter(names = "-fps", description = "Frames per second cap.")
	int fps = 60;
	
	public HockeyProfile() {
		id = UUID.randomUUID();
	}
	
	UUID id;
	
	@Parameter(names = {"-e", "-exitside"}, description = "Define on which side the puck will exit your screen. (EAST, WEST)", required = true)
	String exitBinding;
	
	
	@Parameter(names = {"-u", "-username"}, description = "Username", required = true)
	String username;
	
	@Parameter(names = {"-h", "-host"}, description = "Target host to connect.")
	String targetHost;
	@Parameter(names = {"-p", "-port"}, description = "Port to listen or if host defined port on target host.")
	Integer portNumber = 8228;
	
	@Parameter(names = "-persistent", description = "Waits automatically for new connection once the game has finished.")
	transient boolean persistentListening = false;
	
	double mouseInfluenceRadius = .2;
	double mouseMaxInfluenceRate = .005;
	double maxPuckSpeed = 0.02;
	// x y
	double[] goalSize = new double[] {.05, .2};
	
	
	// the hidden in the annotation does hide it from help text. nothing to do with actual value.
	@Parameter(names = "-hidden", hidden = true)
	transient boolean hidden = false;
	
	public boolean isServer() {
		return targetHost == null;
	}
	
	public ExitBinding getExitBinding() {
		for (ExitBinding binding: ExitBinding.values()) {
			if (binding.name().equalsIgnoreCase(exitBinding)) return binding;
		}
		// meh
		return ExitBinding.EAST;
	}
	
	// TODO not working atm. i think i got an older version of jcommander
	public class ExitBindingConverter implements IStringConverter<ExitBinding> {
		public ExitBinding convert(String input) {
			for (ExitBinding binding: ExitBinding.values()) {
				if (binding.name().equalsIgnoreCase(input)) return binding;
			}
			throw new ParameterException(String.format("%s can't be converted to ExitBinding.", input));
		}	
	}
}
