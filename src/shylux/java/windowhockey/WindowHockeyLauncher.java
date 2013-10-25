package shylux.java.windowhockey;

import java.awt.Container;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class WindowHockeyLauncher extends JFrame {
	public static final String TITLE = "WindowHockey";

	public static void main(String[] args) {
		new WindowHockeyLauncher();
	}
	
	public WindowHockeyLauncher() {
		setTitle(TITLE);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		
		buildGUI();
		
		setVisible(true);
	}
	
	private void buildGUI() {
		Container container = getContentPane();
		
		// settings panel
		JPanel settings = new JPanel();
		settings.setBorder(BorderFactory.createTitledBorder("Settings"));
		
		// add panels
		container.add(settings);
		
		pack();
	}

}
