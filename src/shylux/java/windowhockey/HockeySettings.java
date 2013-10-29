package shylux.java.windowhockey;

import com.beust.jcommander.Parameter;

public class HockeySettings {
	
	@Parameter(names = {"-u", "-username"}, description = "Username", required = true)
	String username;
	
	@Parameter(names = {"-c", "-connect"}, description = "Target host to connect.")
	String targetHost;
}
