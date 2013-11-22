package shylux.java.windowhockey.network;

import java.io.Serializable;
import java.util.UUID;

public class PingFrame implements Serializable {
	private static final long serialVersionUID = 5676250299362165186L;

	UUID source;
	long start;
	
	public PingFrame(UUID source) {
		this.source = source;
		start = System.currentTimeMillis();
	}
	
	public UUID getSource() {
		return this.source;
	}
	
	public long getStartTime() {
		return start;
	}
	
	public static long calculatePing(PingFrame receivedFrame) {
		return System.currentTimeMillis() - receivedFrame.start;
	}
}
