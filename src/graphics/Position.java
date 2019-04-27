package graphics;

import java.io.Serializable;

public abstract class Position implements Serializable {

	private static final long serialVersionUID = 5128714323451148345L;
	protected double x,y;
	
	public Position(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}
	
	public abstract String toString();
	
}
