package shylux.java.windowhockey;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class WindowHockeyLauncher {
	public static final String PROGRAM_NAME = "WindowHockey";
	
	HockeySettings settings;

	public static void main(String[] args) {
		HockeySettings settings = new HockeySettings();
		JCommander jc = new JCommander(settings);
		jc.setProgramName(PROGRAM_NAME);
		try {
			jc.parse(args);
		} catch (Exception e) {
			if (e instanceof ParameterException) System.err.println(e.getMessage());
			jc.usage();
			return;
		}
		new WindowHockeyLauncher(settings);
	}
	
	public WindowHockeyLauncher(HockeySettings settings) {
		this.settings = settings;
		System.out.println(this.settings.username);
	}
}
