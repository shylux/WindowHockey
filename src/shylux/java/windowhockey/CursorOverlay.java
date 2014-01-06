package shylux.java.windowhockey;

import java.awt.MouseInfo;
import java.awt.Point;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

public class CursorOverlay extends TransparentWindow {
	ScheduledExecutorService exec;
	int size;
	
	public CursorOverlay(int size) {
		this.size = size;
		
		setVisible(true);
		

		try {
			this.background = ImageIO.read(getClass().getResource("/shylux/java/windowhockey/resources/grad.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		setSize(size, size);

		exec = Executors.newScheduledThreadPool(1);
		exec.scheduleAtFixedRate(new Runnable() {
			public void run() {
				updatePosition();
			}
		}, 0, 16, TimeUnit.MILLISECONDS);
		
		updatePosition();
	}
	
	private void updatePosition() {
		Point mousePosition = MouseInfo.getPointerInfo().getLocation();
		mousePosition.translate(-(size/2), -(size/2));
		setLocation(mousePosition);
	}
	
	public void close() {
		super.close();
		exec.shutdown();
	}
}
