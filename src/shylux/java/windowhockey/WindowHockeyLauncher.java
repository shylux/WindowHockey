package shylux.java.windowhockey;

import java.awt.Container;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class WindowHockeyLauncher extends JFrame {
	public static final String TITLE = "WindowHockey";

	public static void main(String[] args) {
		new WindowHockeyLauncher();
	}
	
	public WindowHockeyLauncher() {
		setTitle(TITLE);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setResizable(false);
		
		buildGUI();
		
		setVisible(true);
	}
	
	private void buildGUI() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {e.printStackTrace();}
		
		JComponent container = (JComponent) getContentPane();
		// padding
		container.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		// layout
		GridLayout glayout = new GridLayout(2,2);
		container.setLayout(glayout);
		
		// ip
		JLabel ip_label = new JLabel("Connect to");
		JTextField ip_field = new JTextField();
		container.add(ip_label);
		container.add(ip_field);

		// submit
		JButton submit = new JButton("Connect");
		container.add(submit);
		
		pack();
	}

}
