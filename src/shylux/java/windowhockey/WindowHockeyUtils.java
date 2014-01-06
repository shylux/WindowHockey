package shylux.java.windowhockey;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.JFrame;

import shylux.java.windowhockey.HockeyProfile.ExitBinding;


public abstract class WindowHockeyUtils {
	
	private static Insets screen_bounds;
	public static Insets getAvailableScreenBounds(JFrame frame) throws IllegalStateException {
		// calculate them once! also prevents null pointer when puck is outside of screen.
		if (screen_bounds != null) return screen_bounds; 
		
		// select screen with puck
		GraphicsConfiguration graphicsConfiguration = null;
		for (GraphicsDevice gd : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
		    if (gd.getDefaultConfiguration().getBounds().contains(frame.getLocation())) {
		        graphicsConfiguration = gd.getDefaultConfiguration();
		        break;
		    }
		}
		
		// config is null if frame is not visible (not on a screen)
		if (graphicsConfiguration == null) throw new IllegalStateException();
		
		Rectangle screenBounds = graphicsConfiguration.getBounds();
		Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(graphicsConfiguration);
		screen_bounds = applyInsets(screenBounds, screenInsets);
		return screen_bounds;
	}
	
	public static double getRelativeScreenWidth(JFrame frame) {
		return (double)getAvailableScreenWidth(frame) / getAvailableScreenHeight(frame);
	}
	
	public static int getAvailableScreenHeight(JFrame frame) {
		Insets screen = getAvailableScreenBounds(frame);
		return screen.bottom - screen.top;
	}
	
	public static int getAvailableScreenWidth(JFrame frame) {
		Insets screen = getAvailableScreenBounds(frame);
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
		int puckSize = (int) (HockeyProfile.PUCK_DIMENSIONS * getAvailableScreenHeight(puck));
		return new Dimension(puckSize, puckSize);
	}
	
	public static void applyPuckLocation(GameState state, Puck puck) {
		Insets screen = getAvailableScreenBounds(puck);
		int height = getAvailableScreenHeight(puck);
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
	
	public static Vector2D applyMouseForce(Vector2D newVelocity, HockeyProfile profile, Puck puck, GameState state) {
		Point mousePosition = MouseInfo.getPointerInfo().getLocation();
		Vector2D mouseDiff = puck.getAbsoluteCenterPoint().minus(new Vector2D(mousePosition));
		// convert to relative value
		mouseDiff = mouseDiff.times(1./getAvailableScreenHeight(puck));
		if (mouseDiff.norm() < profile.getMouseInfluenceRadius()) { // Box in influence radius of mouse?
			// black coding magic
			double influenceRate = (profile.getMouseInfluenceRadius() - mouseDiff.norm()) / profile.getMouseInfluenceRadius(); // bigger if closer. max at 1
			influenceRate *= state.getMaxInfluenceRate();
			Vector2D influence = mouseDiff.unit().times(influenceRate);
			if (state.isInverted()) influence = influence.invert();
			newVelocity = newVelocity.plus(influence).cap(state.getMaxPuckSpeed());
		}
		return newVelocity;
	}
	
	/**
	 * Generates the initial movement of the puck. The angle is random and points away of your goal to avoid an immideate defeat.
	 * @param profile to determine right or left machine
	 * @return generated initial movement of puck
	 */
	public static Vector2D generateInitialMovement(HockeyProfile profile) {
		double randAngle = Math.random()*Math.PI; // angle between 0 and 180 degree (PI rad)
		// the initial movement should be away of your goal
		if (profile.getExitBinding() == ExitBinding.RIGHT) {
			randAngle += Math.PI;
		}
		return Vector2D.fromAngle(randAngle, .005);
	}

	public static Vector2D applyFriction(Vector2D newVelocity, GameState state) {
		int fps = HockeyProfile.FPS;
		double friction = state.getMaxPuckSpeed() / (fps*5);
		Vector2D vFriction = newVelocity.unit().times(friction);
		return newVelocity.minus(vFriction);
	}
	
	public static double getCursorSize(JFrame frame, HockeyProfile profile) {
		return getAvailableScreenHeight(frame) * profile.getMouseInfluenceRadius() * 2;
	}
}
