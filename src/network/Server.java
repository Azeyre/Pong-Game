package network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
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
	public static final int PORT = 4444;
	public static final String IP = getIp();
	private boolean isRunning = false;
	
	private Ball ball;
	
	public static int getId() {return totalPlayers;}

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

	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		if (IP != null) {
			System.out.println("Starting server on: " + IP + ":" + PORT);
			Server multi = new Server();
			new Thread(multi).start();
			Thread.currentThread().stop();
			System.exit(1);
		} else {
			System.err.println("Cannot start the server...");
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
			System.out.println("Server / Client connection, set up complete");
			id = totalPlayers++;
			if(id == 0) {
				j = new PlayerBox(20,10,20,100);
			} else j = new PlayerBox(Game.WIDTH - 40,10,20,100);
			players.add(j);
			try {
				out.writeObject(players);
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
			System.out.println("Enter run(), setting up player information");
			Timer t1 = new Timer();
			Timer t2 = new Timer();
			
			t1.schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						j = (PlayerBox) in.readObject();
						players.get((id+1)).setY(j.getY());
					} catch (ClassNotFoundException | IOException e) {
						System.err.println("Error during receiving packets");
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
			}, 0, 16);
			System.out.println("Sending packets");
			t2.schedule(new TimerTask() {
				@Override
				public void run() {
					if (running) {
						try {
							if(players.size() == 3) {
								if(id == 0) ball.collisionLeft(j);
								else ball.collisionRight(j);
								
								ball.move(j);
							}
							out.writeObject(players);
							out.reset();
						} catch (IOException e) {
							System.err.println("Error during sending packets");
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
			}, 0, 16);
		}
	}
}