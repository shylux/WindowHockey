package shylux.java.windowhockey;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;


public abstract class WindowHockeyUtils {
	public static Insets getAvailableScreenBounds(Puck puck) throws IllegalStateException {
		// select screen with puck
		GraphicsConfiguration graphicsConfiguration = null;
		for (GraphicsDevice gd : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
		    if (gd.getDefaultConfiguration().getBounds().contains(puck.getLocation())) {
		        graphicsConfiguration = gd.getDefaultConfiguration();
		        break;
		    }
		}
		
		// config is null if frame is not visible (not on a screen)
		if (graphicsConfiguration == null) throw new IllegalStateException();
		
		Rectangle screenBounds = graphicsConfiguration.getBounds();
		Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(graphicsConfiguration);
		return applyInsets(screenBounds, screenInsets);
	}
	
	public static double getRelativeScreenWidth(Puck puck) {
		return (double)getScreenWidth(puck) / getScreenHeight(puck);
	}
	
	public static int getScreenHeight(Puck puck) {
		Insets screen = getAvailableScreenBounds(puck);
		return screen.bottom - screen.top;
	}
	
	public static int getScreenWidth(Puck puck) {
		Insets screen = getAvailableScreenBounds(puck);
		return screen.right - screen.left;
	}

	public static Insets applyInsets(Rectangle rect, Insets ins) {
		return new Insets(rect.y+ins.top, rect.x+ins.left, rect.y+rect.height-ins.bottom, rect.x+rect.width-ins.right);
	}
	
	public static Point calculateScreenCenter(Puck puck) {
		Insets screen = getAvailableScreenBounds(puck);
		return new Point((screen.right - screen.left) / 2 + screen.left, (screen.bottom - screen.top) / 2 + screen.top);
	}
	
	public static Dimension calculatePuckSize(HockeyProfile profile, Puck puck) {
		int puckSize = (int) (profile.puckDimensions * getScreenHeight(puck));
		return new Dimension(puckSize, puckSize);
	}
	
	public static void applyPuckLocation(GameState state, Puck puck) {
		Insets screen = getAvailableScreenBounds(puck);
		int height = getScreenHeight(puck);
		puck.setLocation(new Point( (int) (state.getPuckPosition().x() * height) + screen.left, (int) (state.getPuckPosition().y() * height) + screen.top));
	}
	
	/**
	 * Mirrors vector vertical or horizontal.
	 * @param v Vector to modify
	 * @param side true means horizontal, false vertical
	 * @return
	 */
	public static Vector2D mirrorVector(Vector2D v, boolean side) {
		if (side)
			// horizontal
			return new Vector2D(-v.x(), v.y());
		else
			return new Vector2D(v.x(), -v.y());
	}
}
