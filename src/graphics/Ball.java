package graphics;

import java.io.Serializable;

import client.Game;
import network.Server;

public class Ball extends Position implements Serializable {
	
	private static final long serialVersionUID = 186179931448993933L;
	private double angle;
	private final double MAX_SPEED = 5;
	private int radius = 15;
	
	public Ball(double x, double y) {
		super(x, y);
		randomAngle();
	}

	@Override
	public String toString() {
		return "Ball [x:" + x + ";" + "y:" + y + "]";
	}
	
	public int getRadius() {
		return radius;
	}

	private void randomAngle() {
		angle = (Math.random() * 90) - 45;
		if(Math.random() > 0.5) {
			angle += 180;
		}
		System.out.println(angle);
	}
	
	private void collision() {
		if(x <= 0) {
			Server.score[1]++;
			x = Game.WIDTH / 2;
			y = Game.HEIGHT / 2;
			randomAngle();
		} else if(x >= (Game.WIDTH - radius)) {
			Server.score[0]++;
			x = Game.WIDTH / 2;
			y = Game.HEIGHT / 2;
			randomAngle();
		}
		else if(y <= 0 || y >= (Game.HEIGHT - radius)) {
			angle = 360 - angle; 
		}
	}
	public void collisionLeft(PlayerBox j1) {
		if(y >= j1.y && y <= (j1.y + j1.getHeight()) && x <= (j1.x + j1.getWidth()) && x >= (j1.x + j1.getWidth() - 10)){
			System.out.println("Bar LEFT");
			angle = (((y - j1.y) * 0.01 * 90) + 315) % 360;
		}
	}
	public void collisionRight(PlayerBox j1) {
		if(y >= j1.y && y <= (j1.y + j1.getHeight()) && x >= j1.x && x <= (j1.x + 10)){
			System.out.println("Bar RIGHT");
			angle = (((y - j1.y) * 0.01 * 90) + 315) / 2;
		}
	}
	
	public void move() {
		collision();
		x += Math.cos((Math.toRadians(angle))) * MAX_SPEED;
		y += Math.sin((Math.toRadians(angle))) * MAX_SPEED;
	}
}
