package shylux.java.windowhockey;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class TransparentWindow extends JFrame {
	public Image background;
	public TransparentWindow() {
		this.addFocusListener(KeyboardState.getInstance());
		
		setUndecorated(true);

		setBackground(new Color(0, 255, 0, 0));

		setContentPane(new ShowImage());
	}
	
	public boolean intersects(JFrame frame) {
		Rectangle bounds = getBounds();
		Rectangle bound2 = frame.getBounds();
		return bounds.intersects(bound2);
	}
	
	public void close() {
		WindowEvent wev = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
	}
	
	public class ShowImage extends JPanel {
		public void paintComponent(Graphics g) {
			this.setOpaque(true);
			Graphics2D g2d = (Graphics2D) g;

			g2d.drawImage(background, 0, 0, getWidth(), getHeight(), null);
		}
	}
}
