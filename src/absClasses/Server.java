package absClasses;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.Hashtable;

public abstract class Server {
	
	/**
	 * Should not be an Arraylist!
	 * Should be HashTable with some integer as key. The integer should correspond to their
	 * 		turn for Game.
	 */
	protected Hashtable<Integer, Socket> sockets = new Hashtable<Integer, Socket>();
	protected Hashtable<Integer, DataInputStream> inputStreams = new Hashtable<Integer, DataInputStream>();
	protected Hashtable<Integer, DataOutputStream> outputStreams = new Hashtable<Integer, DataOutputStream>();
	protected Hashtable<Integer, String> clientNames = new Hashtable<Integer, String>();
	protected Game game;
	protected int humans;
	protected int port;
	protected int bots;
	
	protected Server(int max_clients, int bots, int port) {
		this.humans = max_clients;
		this.bots = bots;
		this.port = port;
	}
	
	protected abstract String parseInput(String input);
	public abstract void start() throws Exception;
	
	protected final void add(int index, Socket s) {
		try {
			sockets.put(index, s);
			inputStreams.put(index, new DataInputStream(new BufferedInputStream(s.getInputStream())));
			outputStreams.put(index, new DataOutputStream(s.getOutputStream()));
			outputStreams.get(index).writeUTF("Successfully connected");
			try {
				String input = this.inputStreams.get(index).readUTF();
				clientNames.put(index, input);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Failed to get name of client!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error at adding socket.");
		}
	}
	protected final void remove(Socket socket) {
		int key = -1;
		for (Integer k : Collections.list(sockets.keys())) {
			if (sockets.get(k).equals(socket)) {
				key = k;
				break;
			}
		}
		try {
			inputStreams.remove(key).close();
			outputStreams.remove(key).close();;
			sockets.remove(key).close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error at removing socket");
		}
	}
	protected final void closeAll() {
		try {
			for (Socket s: sockets.values()) 
				s.close();
		} catch (Exception e) {
		}
		sockets.clear();
	}

	protected final int numOfConnections() {
		return sockets.size();
	}
}
