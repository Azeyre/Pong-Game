package network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import client.Game;
import graphics.Ball;
import graphics.PlayerBox;
import graphics.Position;

public class Server implements Runnable {

	private ArrayList<Position> players = new ArrayList<Position>();
	private static int totalPlayers;

	private ServerSocket server;
	private Socket socket;

	private static Timer timer;
	public static int PORT;
	public static final String IP = getIp();
	private boolean isRunning = false;
	public static int[] score = {0,0};
	private Object[] object = {players,score};
	private int res = 0;
	private Ball ball;
	
	public static int getId() {return totalPlayers;}
	
	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		if(args.length != 1) {
			System.err.println("Usage: java -jar server.jar <PORT>");
			System.exit(1);
		} else {
			try {
				PORT = Integer.valueOf(args[0]);
			} catch(NumberFormatException e) {
				System.err.println("Usage: java -jar server.jar <PORT>");
				System.exit(1);
			}
			if (IP != null) {
				System.out.println("Starting server on: " + IP + ":" + PORT);
				Server multi = new Server();
				new Thread(multi).start();
				Thread.currentThread().stop();
				System.exit(0);
			} else {
				System.err.println("Cannot start the server...");
				System.exit(1);
			}
		}
	}

	@Override
	public void run() {
		try {
			createServer();
			ball = new Ball(Game.WIDTH / 2, Game.HEIGHT / 2);
			players.add(ball);
			isRunning = true;
		} catch (ClassNotFoundException | IOException e) {
			System.err.println("Cannot create the server");
			System.exit(1);
		}

		while (isRunning) {
			try {
				socket = this.server.accept();
			} catch (IOException e) {
				System.err.println("Problem with the client...");
				System.exit(1);
			}
			if(totalPlayers >= 1) {
				System.out.println("Lance la balle");
				new Timer().schedule(new TimerTask() {

					@Override
					public void run() {
						ball.move();
					}
					
				}, 0, 8);
			}
			new Thread(new WorkerRunnable(socket)).start();
			if (totalPlayers == 0) {
				isRunning = false;
				System.out.println("isRunning = false");
			}
		}
		System.out.println("No client online, shutting down the server.");
		try {
			server.close();
			System.exit(1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void createServer() throws IOException, ClassNotFoundException {
		server = new ServerSocket(PORT);
	}

	private static String getIp() {
		InetAddress inetAddress;
		try {
			inetAddress = InetAddress.getLocalHost();
			return inetAddress.getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private class WorkerRunnable implements Runnable {

		private Socket clientSocket = null;
		private ObjectOutputStream out;
		private ObjectInputStream in;
		private PlayerBox j;
		private int id;
		private boolean running;

		public WorkerRunnable(Socket clientSocket) {
			this.clientSocket = clientSocket;
			try {
				out = new ObjectOutputStream(this.clientSocket.getOutputStream());
				in = new ObjectInputStream(this.clientSocket.getInputStream());
			} catch (IOException e) {
				System.err.println("Problem with communication..., shutting down socket");
				try {
					socket.close();
					System.exit(1);
				} catch (IOException e1) {
					System.err.println("Error during socket close, shutting down server");
					try {
						server.close();
						System.exit(1);
					} catch (IOException e2) {
						System.err.println("Error during server close, closing app");
						System.exit(1);
					}
				}
			}
			//System.out.println("Server / Client connection, set up complete");
			id = totalPlayers++;
			if(id == 0) {
				j = new PlayerBox(20,10,20,100);
			} else j = new PlayerBox(Game.WIDTH - 40,10,20,100);
			players.add(j);
			try {
				out.writeObject(object);
				out.reset();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}
			running = true;
		}

		/*
		 * run() done everytime client is sending information
		 */
		public void run() {
			//System.out.println("Enter run(), setting up player information");
			Timer t1 = new Timer();
			Timer t2 = new Timer();
			
			t1.schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						j = (PlayerBox) in.readObject();
						players.get((id+1)).setY(j.getY());
					} catch (ClassNotFoundException | IOException e) {
						//System.err.println("Error during receiving packets");
						running = false;
						t1.cancel();
						t2.cancel();
						try {
							socket.close();
							System.exit(1);
						} catch (IOException e1) {
							t1.cancel();
							System.exit(1);
						}
					}

				}
			}, 0, 8);
			//System.out.println("Sending packets");
			t2.schedule(new TimerTask() {
				@Override
				public void run() {
					if (running) {
						try {
							if(players.size() == 3) {
								if(id == 0) ball.collisionLeft(j);
								else ball.collisionRight(j);
							}
							out.writeObject(object);
							out.reset();
						} catch (IOException e) {
							//System.err.println("Error during sending packets");
							t1.cancel();
							t2.cancel();
							try {
								socket.close();
								System.exit(1);
							} catch (IOException e1) {
								t2.cancel();
								System.exit(1);
							}
						}
					} else
						t2.cancel();
				}
			}, 0, 8);
		}
	}
}
