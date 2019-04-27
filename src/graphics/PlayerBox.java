package graphics;

import java.io.Serializable;

import client.Game;

public class PlayerBox extends Position implements Serializable {
	
	private static final long serialVersionUID = -5822306690030507282L;
	private final int WIDTH, HEIGHT;
	
	public PlayerBox(double x, double y, int width, int height) {
		super(x, y);
		this.WIDTH = width;
		this.HEIGHT = height;
	}

	@Override
	public String toString() {
		return "PlayerBox [start x: " + x + " ; " + " start y: " + y + " ; " + "end x: " + (x+WIDTH) + " ; " + "end y: " + (y+HEIGHT) +"]";
	}

	public int getWidth() {
		return WIDTH;
	}

	public int getHeight() {
		return HEIGHT;
	}
	
	public void up() {
		if(y > 0) y -= 3;
	}
	
	public void down() {
		if(y < (Game.HEIGHT - HEIGHT)) y += 3;
	}
}
