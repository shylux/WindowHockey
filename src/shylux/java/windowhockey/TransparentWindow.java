package shylux.java.windowhockey;

import java.awt.Color;
import java.awt.Rectangle;

import javax.swing.JFrame;

public class TransparentWindow extends JFrame {
	public TransparentWindow() {
		setUndecorated(true);
		setBackground(new Color(0f, 0f, 0f, 0f));
	}
	
	public boolean intersects(JFrame frame) {
		Rectangle bounds = getBounds();
		Rectangle bound2 = frame.getBounds();
		return bounds.intersects(bound2);
	}
}
