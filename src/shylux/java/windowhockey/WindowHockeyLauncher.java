package shylux.java.windowhockey;

import java.io.IOException;
import java.net.SocketException;

import shylux.java.network.ConnectionManager;
import shylux.java.network.INetworkListener;
import shylux.java.network.TCPConnection;
import shylux.java.network.UDPMessage;
import shylux.java.windowhockey.network.GameInProgressFrame;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class WindowHockeyLauncher implements INetworkListener {
	public static final String PROGRAM_NAME = "WindowHockey";
	public static final int PORT = 8228;
	
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
			if (this.settings.onlyTCP) {
				cmanager = new ConnectionManager(this.settings.portNumber, true, false);
			} else if (this.settings.onlyUDP) {
				cmanager = new ConnectionManager(this.settings.portNumber, false, true);
			} else {
				cmanager = new ConnectionManager(this.settings.portNumber, true, true);
			}
			
			cmanager.addNetworkListener(this);
			System.out.println("Waiting for connection...");
			
			System.out.println("Sending hi message...");
			try {
				ConnectionManager.sendBroadcastUDPMessage(PROGRAM_NAME);
			} catch (SocketException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void onConnection(TCPConnection pCon) {
		// check if the game is already running
		if (game == null) {
			startGame(pCon);
		} else {
			// send notification that game is in progress
			pCon.sendMessage(new GameInProgressFrame());
			pCon.close();
		}
	}
	
	private void connectToGame() {
		TCPConnection conn;
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
	
	private void startGame(TCPConnection conn) {
		System.out.println("Starting game.");
		game = new WindowHockey(this, conn, this.settings);
	}
	
	public void onGameEnd() {
		game = null;
		
		// check if server should continue
		if (this.settings.isServer() && this.settings.persistentListening) {	
			System.out.println("Waiting for connection...");
		} else {
			if (this.cmanager != null) this.cmanager.stop();
			System.exit(0);
		}
	}

	public void onGameEnd(boolean didIWin) {
		System.out.println((didIWin)?"He won.":"He lost.");
		onGameEnd();
	}

	public void onUDPMessage(UDPMessage msg) {
		System.out.println("Msg:"+msg.getMessage());
		if (msg.getMessage().equals(PROGRAM_NAME)) {
			// change to client
			this.settings.targetHost = null;
			this.settings.targetHost = msg.getInetAddress().getHostAddress();
			connectToGame();
		}
	}
}
