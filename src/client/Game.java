package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import graphics.Ball;
import graphics.PlayerBox;
import graphics.Position;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import network.Server;

public class Game extends Application {
	
	public static void main(String[] args) {
		if(args.length == 1) {
			if(!args[0].contains(":")) {
				System.err.println("Usage: java -jar client.jar <IP:PORT> or <IP> <PORT>");
				System.exit(1);
			} else {
				IP = args[0].split(":")[0];
				try {
					PORT = Integer.valueOf(args[0].split(":")[1]);
				} catch (NumberFormatException e) {
					System.err.println("Usage: java -jar client.jar <IP:PORT> or <IP> <PORT>");
					System.exit(1);
				}
			}
		} else if(args.length == 2) {
			IP = args[0];
			try {
				PORT = Integer.valueOf(args[1]);
			} catch (NumberFormatException e) {
				System.err.println("Usage: java -jar client.jar <IP:PORT> or <IP> <PORT>");
				System.exit(1);
			}
		} else {
			System.err.println("Usage: java -jar client.jar <IP:PORT> or <IP> <PORT>");
			System.exit(1);
		}
		launch();
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		createClient();
		Group root = new Group();
		cv = new Canvas(WIDTH,HEIGHT);
		gc = cv.getGraphicsContext2D();
		root.getChildren().add(cv);
		
		Scene scene = new Scene(root);
		scene.setOnKeyPressed(new KeyboardEvent());
		scene.setOnKeyReleased(new KeyboardEvent());
		stage.setScene(scene);
		stage.setTitle("Pong Game");
		stage.show();
		
		loop();
	}
	
	private Button start, exit;
	private Label pong;
	public static final int WIDTH = 640, HEIGHT = 480;
	private Canvas cv;
	private GraphicsContext gc;
	private ArrayList<Position> pos;
	private Ball ball;
	private PlayerBox j1, j2;
	private boolean gameRunning = false;
	private Timer t1;
	private boolean keyPressed = false;
	private int id;
	private static String IP;
	private static int PORT;
	
	ObjectOutputStream out;
	ObjectInputStream in;
	Socket socket;
	boolean connected = false;

	static Timer timer;
	
	@SuppressWarnings("unchecked")
	public void createClient() throws IOException {
		try {
			socket = new Socket(IP, PORT);
		} catch (IOException e1) {
			System.err.println("Cannot connect to the server. IP address or Port number may be invalid.");
			System.exit(1);
		}
		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());
		connected = true;
		try {
			pos = (ArrayList<Position>) in.readObject();
			ball = (Ball) pos.get(0);
			if(pos.size() == 2) {
				j1 = (PlayerBox) pos.get(1);
				id = 1;
			} else { //3
				j1 = (PlayerBox) pos.get(2);
				j2 = (PlayerBox) pos.get(1);
				id = 2;
			}
		} catch (ClassNotFoundException e) {
			System.err.println("Erreur chargement des positions");
			System.exit(1);
		}
		// socket.setTcpNoDelay(true);
	}
	
	public void gameContent() throws UnknownHostException, IOException {
	
	}
	
	private void paint() {
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, cv.getWidth(), cv.getHeight());
		
		gc.setFill(Color.WHITE);
		gc.fillRect(j1.getX(), j1.getY(), j1.getWidth(), j1.getHeight());
		
		gc.fillOval(ball.getX(), ball.getY(), ball.getRadius(), ball.getRadius());
		
		if(pos.size() == 3) {
			gc.setFill(Color.WHITE);
			gc.fillRect(j2.getX(), j2.getY(), j2.getWidth(), j2.getHeight());
		}
	}
	
	private void loop() {
		new Timer().schedule(new TimerTask() {

			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				try {
					pos = (ArrayList<Position>) in.readObject();
					ball = (Ball) pos.get(0);
					if(pos.size() == 2) {
						j1 = (PlayerBox) pos.get(1);
					} else {
						if(id == 1) {
							j2 = (PlayerBox) pos.get(2);
						} else {
							j2 = (PlayerBox) pos.get(1);
						}
					}
					paint();
				} catch (ClassNotFoundException | IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
			
		}, 0, 16);
		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {
				try {
					out.writeObject(j1);
					out.reset();
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
			
		}, 0, 16);
	}
	
	private class Menu {
		
		public Scene menuContent() {
			pong = new Label("Pong Game");
			pong.setFont(new Font("Arial bold", 20));
			VBox root = new VBox();
			root.setAlignment(Pos.CENTER);
			
			start = new Button("Start");
			exit = new Button("Exit");
			
			start.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);
			exit.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);
			
			start.addEventHandler(ActionEvent.ACTION, new ButtonClick());
			exit.addEventHandler(ActionEvent.ACTION, new ButtonClick());
			
			root.getChildren().addAll(pong,start,exit);
			
			Scene scene = new Scene(root, 250,100);
			return scene;
		}
	}
	
	private class ButtonClick implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			if(event.getTarget() == exit) {
				System.exit(0);
			} else if(event.getTarget() == start) {
				if(!gameRunning) {
					Game g = new Game();
					try {
						g.gameContent();
					} catch (IOException e) {
						e.printStackTrace();
					}
					gameRunning = true;
				}
			}
		}
	}
	
	private class KeyboardEvent implements EventHandler<KeyEvent> {
		@Override
		public void handle(KeyEvent event) {
			KeyCode k = event.getCode();
			if(k.equals(KeyCode.UP)) {
				if(event.getEventType() == KeyEvent.KEY_PRESSED && !keyPressed) {
					up();
					keyPressed = true;
				} else if(event.getEventType() == KeyEvent.KEY_RELEASED && keyPressed) {
					keyPressed = false;
					t1.cancel();
				}
			} else if(k.equals(KeyCode.DOWN)) {
				if(event.getEventType() == KeyEvent.KEY_PRESSED && !keyPressed) {
					down();
					keyPressed = true;
				} else if(event.getEventType() == KeyEvent.KEY_RELEASED && keyPressed) {
					keyPressed = false;
					t1.cancel();
				}
			}
		}
		
		private void up() {
			t1 = new Timer();
			t1.schedule(new TimerTask() {

				@Override
				public void run() {
					j1.up();
				}
				
			}, 0, 10);
		}
		
		private void down() {
			t1 = new Timer();
			t1.schedule(new TimerTask() {

				@Override
				public void run() {
					j1.down();
				}
				
			}, 0, 10);
		}
	}
}
