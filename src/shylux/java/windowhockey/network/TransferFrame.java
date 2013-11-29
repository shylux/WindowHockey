package shylux.java.windowhockey.network;

import java.io.Serializable;
import java.util.UUID;

import shylux.java.windowhockey.GameState;

public class TransferFrame implements Serializable {
	private static final long serialVersionUID = -389805120454556898L;

	GameState state;

	UUID newMaster;

	public TransferFrame(GameState state, UUID newMaster) {
		System.out.println("##############################Tranfer!");
		this.state = GameState.updateMaster(state, newMaster);
		this.newMaster = newMaster;
	}
	
	public GameState getState() {
		return state;
	}

	public UUID getNewMaster() {
		return newMaster;
	}
}
