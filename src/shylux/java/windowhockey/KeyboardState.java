package shylux.java.windowhockey;

import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Keeps track of which keys are pressed on the keyboard.
 * Make sure to delegate all key/focus events to this class.
 * Otherwise bugs like stuck keys might appear. Use reset() in this case.
 * @author lukas
 *
 */
public class KeyboardState implements KeyListener, FocusListener {
	private static KeyboardState _instance = null;
	private KeyboardState() {}
	public static KeyboardState getInstance() {
		//if (_instance == null) throw new RuntimeException("KeyboardState not initialized.");
		if (_instance == null) _instance = new KeyboardState();
		return _instance;
	}

	List<Integer> keysPressed = new ArrayList<Integer>();

	/**
	 * Mark key as pressed.
	 */
	public void keyPressed(KeyEvent e) {
		System.out.format("Keyup: %d\n", e.getKeyCode());
		if (!keysPressed.contains(new Integer(e.getKeyCode())))
			keysPressed.add(e.getKeyCode());
	}
	
	/**
	 * Mark key as not pressed.
	 */
	public void keyReleased(KeyEvent e) {
		System.out.format("Keyup: %d\n", e.getKeyCode());
		keysPressed.remove(new Integer(e.getKeyCode()));
	}
	
	/**
	 * Check if key is pressed.
	 * @param keyCode KeyEvent code of key
	 * @return true if key is pressed
	 */
	public boolean isKeyPressed(int keyCode) {
		return keysPressed.contains(new Integer(keyCode));
	}

	public void keyTyped(KeyEvent e) {}

	/**
	 * Release all keys when focus lost.
	 * We do this because we don't know which keys get pressed/released
	 * during the time we don't have the focus.
	 */
	public void focusLost(FocusEvent e) {
		reset();
	}

	/**
	 * Mark all keys as released.
	 */
	public void reset() {
		keysPressed.clear();
	}
	public void focusGained(FocusEvent arg0) {}
}
