package shylux.java.windowhockey;

import java.io.Serializable;

public class Vector2D implements Serializable, Cloneable {
	private static final long serialVersionUID = -3297550874996365039L;

	private double x, y;
	
	public Vector2D(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public double x() {
		return this.x;
	}
	
	public double y() {
		return this.y;
	}
	
	public Vector2D plus(Vector2D v) {
		return new Vector2D(this.x() + v.x(), this.y() + v.y());
	}
	
	public String toString() {
		return String.format("x=%f y=%f", this.x(), this.y());
	}
}
