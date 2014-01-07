package shylux.java.windowhockey;

import java.awt.Point;
import java.io.IOException;

import javax.imageio.ImageIO;

@SuppressWarnings("serial")
public class Puck extends TransparentWindow {
	private boolean unstoppable = false;

	public Puck() {
		try {
			this.background = ImageIO.read(getClass().getResource("/shylux/java/windowhockey/resources/puck_surface.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void initialize(WindowHockey game) {
		setSize(WindowHockeyUtils.calculatePuckSize(game.profile, this));
		requestFocus();
	}

	public Vector2D getAbsoluteCenterPoint() {
		Point root = this.getLocation();
		return new Vector2D(root.x + getWidth()/2, root.y + getHeight()/2);
	}

	public boolean isUnstoppable() {
		return unstoppable;
	}

	public void setUnstoppable(boolean unstoppable) {
		this.unstoppable = unstoppable;
	}
}
