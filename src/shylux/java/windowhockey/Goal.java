package shylux.java.windowhockey;

import java.awt.Color;
import java.awt.Image;
import java.awt.Insets;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class Goal extends TransparentWindow {
	Image background;
	
	Color backgroundColor = Color.BLACK;
	
	public Goal(HockeyProfile profile) {
		super();
		
		// apply debug colors
		if (profile.onlyUDP) {
			backgroundColor = Color.GREEN;
		} else if (profile.onlyTCP) {
			backgroundColor = Color.CYAN;
		}
		
		JPanel pane = new JPanel();
		pane.setBackground(backgroundColor);
		setContentPane(pane);
		
		setVisible(true);
		
		// calc size
		int screen_height = WindowHockeyUtils.getAvailableScreenHeight(this);
		int width = (int)(HockeyProfile.GOAL_SIZE[0]*screen_height);
		int height = (int)(HockeyProfile.GOAL_SIZE[1]*screen_height);
		setSize(width, height);
		
		// calc position
		Insets bounds = WindowHockeyUtils.getAvailableScreenBounds(this);
		int locX = 0;
		switch(profile.getExitBinding()) {
		case RIGHT:
			locX = 0;
			break;
		case LEFT:
			locX = (int) (bounds.right - this.getSize().getWidth());
			break;
		}
		int locY = (int)(WindowHockeyUtils.getAvailableScreenHeight(this)-this.getSize().getHeight())/2;
		setLocation(locX, locY);
	}
}
