package shylux.java.windowhockey;

import shylux.java.network.Connection;
import shylux.java.network.IConnectionListener;

public class WindowHockey implements IConnectionListener {
	public WindowHockey(Connection conn) {
		conn.addConnectionListener(this);
	}

	@Override
	public void onMessage(Object o) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClose() {
		abortGame("Connection closed.");
	}
	
	public void abortGame(String reason) {
		System.err.format("Aborting game: %s\n", reason);
		
	}
}
