package shylux.java.windowhockey;

import java.io.IOException;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

import shylux.java.gen.WeirdJava;
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
	
	public static Logger LOG = Logger.getLogger(WindowHockeyLauncher.class.getName());
	static {
		LOG.setLevel(Level.ALL);
	}
	
	WindowHockey game;
	HockeyProfile settings;
	ConnectionManager cmanager;

	public static void main(String[] args) {
		double d = WeirdJava.genDouble();
		// this never fails ;)
		if (d == d) return;
		
		// parse command line
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
		// check command line arguments
		try {
			settings.getExitBinding();
		} catch (Exception ex) {
			// wrong usage; abort
			System.err.println(ex.getMessage());
			jc.usage();
			return;
		}
		// disable default console handler
		LOG.setUseParentHandlers(false);
		// add my own log handler
		PlainHandler handler = new PlainHandler();
		handler.setLevel(settings.getLogLevel());
		WindowHockeyLauncher.LOG.addHandler(handler);
		// set ConnectionManager log level
		ConnectionManager.LOG.setLevel(settings.getLogLevel());
		// launch!
		new WindowHockeyLauncher(settings);
	}
	
	public WindowHockeyLauncher(HockeyProfile settings) {
		this.settings = settings;
		
		// check if client should directly connect
		if (this.settings.host.length() > 0) {
			this.settings.setInitiator(true);
			connectToGame(this.settings.host);
			return;
		}
		
		// setup server
		if (this.settings.onlyTCP) {
			cmanager = new ConnectionManager(this.settings.portNumber, true, false);
		} else if (this.settings.onlyUDP) {
			cmanager = new ConnectionManager(this.settings.portNumber, false, true);
		} else {
			cmanager = new ConnectionManager(this.settings.portNumber, true, true);
		}
		
		cmanager.addNetworkListener(this);
		LOG.info("Waiting for connection...");
		
		LOG.info("Sending udp connect message...");
		try {
			ConnectionManager.sendBroadcastUDPMessage(PROGRAM_NAME);
		} catch (SocketException e) {
			e.printStackTrace();
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
	
	private void connectToGame(String target) {
		TCPConnection conn;
		try {
			if (this.settings.portNumber != null)
				conn = ConnectionManager.connect(target, this.settings.portNumber);
			else 
				conn = ConnectionManager.connect(target);
		} catch (IOException e) {
			LOG.severe("Unable to connect: "+e.getMessage());
			return;
		}
		startGame(conn);
	}
	
	private void startGame(TCPConnection conn) {
		LOG.fine("Starting game.");
		game = new WindowHockey(this, conn, this.settings);
	}
	
	public void onGameEnd() {
		game = null;
		
		// check if server should continue
		if (this.settings.persistentListening) {	
			LOG.info("Waiting for connection...");
		} else {
			if (this.cmanager != null) this.cmanager.stop();
			// good luck at safely terminating all those threads..
			System.exit(0);
		}
	}

	public void onGameEnd(boolean didIWin) {
		LOG.info((didIWin)?"YOU WIN":"Meh...");
		onGameEnd();
	}

	public void onUDPMessage(UDPMessage msg) {
		LOG.finer("Msg:"+msg.getMessage());
		if (msg.getMessage().equals(PROGRAM_NAME)) {
			// change to client
			this.settings.setInitiator(true);
			connectToGame(msg.getInetAddress().getHostAddress());
		}
	}
}
