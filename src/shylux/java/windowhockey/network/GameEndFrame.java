package shylux.java.windowhockey.network;

import java.io.Serializable;
import java.util.UUID;

@SuppressWarnings("serial")
public class GameEndFrame implements Serializable {
	UUID winner;
	
	public GameEndFrame(UUID winner) {
		this.winner = winner;
	}
	
	public UUID getWinner() {
		return winner;
	}
}
