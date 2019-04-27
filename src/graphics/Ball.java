package graphics;

import java.io.Serializable;

import client.Game;

public class Ball extends Position implements Serializable {
	
	private static final long serialVersionUID = 186179931448993933L;
	private double angle;
	private final double MAX_SPEED = 2.5;
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
		angle = (Math.random() * 360);
		System.out.println(angle);
	}
	
	private void collision(PlayerBox j1) {
		if(x <= 0 || x >= (Game.WIDTH - radius)) {
			System.out.println("SCORED");
			x = Game.WIDTH / 2;
			y = Game.HEIGHT / 2;
			randomAngle();
		} else if(y <= 0 || y >= (Game.HEIGHT - radius)) {
			System.out.println("Y colide");
			angle = 360 - angle; 
		}
	}
	public void collisionLeft(PlayerBox j1) {
		if(y >= j1.y && y <= (j1.y + j1.getHeight()) && x <= (j1.x + j1.getWidth()) && x >= (j1.x + j1.getWidth() - 5)){
			System.out.println("Bar LEFT");
			angle = 180 - angle;
		}
	}
	public void collisionRight(PlayerBox j1) {
		if(y >= j1.y && y <= (j1.y + j1.getHeight()) && x >= j1.x && x <= (j1.x + 5)){
			System.out.println("Bar RIGHT");
			angle = 180 - angle;
		}
	}
	
	public void move(PlayerBox j1) {
		collision(j1);
		x += Math.cos((Math.toRadians(angle))) * MAX_SPEED;
		y += Math.sin((Math.toRadians(angle))) * MAX_SPEED;
	}
}
