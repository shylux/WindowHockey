package shylux.java.windowhockey;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class TransparentWindow extends JFrame {
	public Image background = null;
	public TransparentWindow() {
		this.addFocusListener(KeyboardState.getInstance());
		
		setUndecorated(true);

		setBackground(new Color(0, 255, 0, 0));

		ShowImage imgpanel = new ShowImage();
		setContentPane(imgpanel);
		
		// blank cursor
		// Transparent 16 x 16 pixel cursor image.
//		BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

		// Create a new blank cursor.
//		Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
//		    cursorImg, new Point(0, 0), "blank cursor");
//		imgpanel.setCursor(blankCursor);
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

			//if (background != null)
				g2d.drawImage(background, 0, 0, getWidth(), getHeight(), null);
		}
	}
}
