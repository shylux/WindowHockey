package shylux.java.windowhockey;

import java.awt.Color;
import java.awt.Container;
import java.awt.Image;
import java.awt.Insets;

import javax.swing.JPanel;

public class Goal extends TransparentWindow {
	Image background;
	
	public Goal(HockeyProfile profile) {
		super();
		
		JPanel pane = new JPanel();
		pane.setBackground(Color.BLACK);
		setContentPane(pane);
		
		setVisible(true);
		
		// calc size
		int screen_height = WindowHockeyUtils.getAvailableScreenHeight(this);
		int width = (int)(profile.goalSize[0]*screen_height);
		int height = (int)(profile.goalSize[1]*screen_height);
		setSize(width, height);
		
		// calc position
		Insets bounds = WindowHockeyUtils.getAvailableScreenBounds(this);
		int locX = 0;
		switch(profile.getExitBinding()) {
		case EAST:
			locX = 0;
			break;
		case WEST:
			locX = (int) (bounds.right - this.getSize().getWidth());
			break;
		}
		int locY = (int)(WindowHockeyUtils.getAvailableScreenHeight(this)-this.getSize().getHeight())/2;
		setLocation(locX, locY);
	}
}
