package shylux.java.windowhockey;

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
public class HockeySettings {
	
	@Parameter(names = {"-u", "-username"}, description = "Username", required = true)
	String username;
	
	@Parameter(names = {"-h", "-host"}, description = "Target host to connect.")
	String targetHost;
	@Parameter(names = {"-p", "-port"}, description = "Port to listen or if host defined port on target host.")
	Integer portNumber;
	
	@Parameter(names = "-persistent", description = "Waits automatically for new connection once the game has finished.")
	boolean persistentListening = false;
	
	public boolean isServer() {
		return targetHost == null;
	}
}
