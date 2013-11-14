package shylux.java.windowhockey;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Puck extends JFrame {
	private BufferedImage background;
	
	public Puck() {
		setContentPane(new DrawPane());
		
		setUndecorated(true);
		setBackground(new Color(0f, 0f, 0f, 0f));
		try {
			this.background = ImageIO.read(getClass().getResource("/shylux/java/windowhockey/resources/puck.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public void initialize(WindowHockey game) {
		// cancel if no output is preferred
		if (game.profile.hidden) return;
		setSize(WindowHockeyUtils.calculatePuckSize(game.profile, this));
		setVisible(true);
		requestFocus();
	}
	
	class DrawPane extends JPanel {

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			g2d.drawImage(background, 0, 0, getWidth(), getHeight(), null);
		}
	}
}
