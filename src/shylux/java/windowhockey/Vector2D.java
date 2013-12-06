package shylux.java.windowhockey;

import java.awt.Point;
import java.io.Serializable;

public class Vector2D implements Serializable, Cloneable {
	private static final long serialVersionUID = -3297550874996365039L;

	private double x, y;
	
	/**
	 * Zero Vector
	 */
	public Vector2D() {}
	
	public Vector2D(double x, double y) {
		this.x = x;
		this.y = y;
	}
	 
	public Vector2D(Point point) {
		this(point.x, point.y);
	}
	
	/**
	 * Creates vector from angle and power.
	 * @param angle angle in radians starting at 3 o'clock.
	 * @param power length of the vector.
	 */
	public static Vector2D fromAngle(double angle, double power) {
		Vector2D v = new Vector2D();
		v.x = Math.cos(angle);
		v.y = Math.sin(angle);
		return v.times(power);
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
	
	public Vector2D minus(Vector2D v) {
		return new Vector2D(this.x() - v.x(), this.y() - v.y());
	}
	
	public Vector2D times(double multiplier) {
		return new Vector2D(this.x()*multiplier, this.y()*multiplier);
	}
	
	public Vector2D toPower(double power) {
		Vector2D unit = unit();
		return new Vector2D(unit.x() * power, unit.y() * power);
	}
	
	public double norm() {
		return Math.sqrt(this.x()*this.x()+this.y()*this.y());
	}
	
	// vector with length 1
	public Vector2D unit() {
		return new Vector2D(x() / norm(), y() / norm());
	}
	
	public Vector2D cap(double max) {
		if (this.norm() > max) {
			double multiplier = max / this.norm();
			return this.times(multiplier);
		}
		return this;
	}
	
	public Vector2D invert() {
		return this.times(-1);
	}
	
	public String toString() {
		return String.format("x=%f y=%f", this.x(), this.y());
	}
}
