package shylux.java.windowhockey;

import java.io.IOException;

import shylux.java.network.Connection;
import shylux.java.network.ConnectionManager;
import shylux.java.network.INetworkListener;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class WindowHockeyLauncher implements INetworkListener {
	public static final String PROGRAM_NAME = "WindowHockey";
	
	WindowHockey game;
	HockeyProfile settings;
	ConnectionManager cmanager;

	public static void main(String[] args) {
		HockeyProfile settings = new HockeyProfile();
		JCommander jc = new JCommander(settings);
		jc.setProgramName(PROGRAM_NAME);
		try {
			jc.parse(args);
		} catch (Exception e) {
			e.printStackTrace();
			if (e instanceof ParameterException) System.err.println(e.getMessage());
			jc.usage();
			return;
		}
		new WindowHockeyLauncher(settings);
	}
	
	public WindowHockeyLauncher(HockeyProfile settings) {
		this.settings = settings;
		System.out.format("Welcome %s\n", this.settings.username);
		
		if (!this.settings.isServer()) {
			connectToGame();
		} else {
			// setup server
			if (this.settings.portNumber != null)
				cmanager = new ConnectionManager(this.settings.portNumber);
			else
				cmanager = new ConnectionManager();
			
			cmanager.addNetworkListener(this);
			System.out.println("Waiting for connection...");
		}
	}
	
	public void onConnection(Connection pCon) {
		// check if the game is already running
		if (game == null) {
			startGame(pCon);
		} else
			pCon.close();
	}
	
	private void connectToGame() {
		Connection conn;
		try {
			if (this.settings.portNumber != null)
				conn = ConnectionManager.connect(this.settings.targetHost, this.settings.portNumber);
			else 
				conn = ConnectionManager.connect(this.settings.targetHost);
		} catch (IOException e) {
			System.err.format("Unable to connect: %s\n", e.getMessage());
			return;
		}
		startGame(conn);
	}
	
	private void startGame(Connection conn) {
		System.out.println("Starting game.");
		game = new WindowHockey(this, conn, this.settings);
	}
	
	public void onGameEnd() {
		game = null;
		
		// check if server should continue
		if (this.settings.isServer()) {
			if (this.settings.persistentListening) {
				System.out.println("Waiting for connection...");
			}
		} else {
			if (this.cmanager != null) this.cmanager.stop();
			System.exit(0);
		}
	}

	public void onGameEnd(boolean didIWin) {
		System.out.println((didIWin)?"He won.":"He lost.");
		onGameEnd();
	}
}
