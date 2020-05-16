package absClasses;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import javafx.application.Platform;

public abstract class Client {
	protected int id;
	protected Socket socket;
	protected DataInputStream dis;
	protected DataOutputStream dos;
	protected GameGUI gui;
	protected String name;
	
	protected Client(Socket socket, String name) {
		this.socket = socket;
		try {
			this.dis = new DataInputStream(this.socket.getInputStream());
			this.dos = new DataOutputStream(this.socket.getOutputStream());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error at trying to initialize input and output streams!");
		}
		this.name = name;
	}

	public abstract void startGUI();
	public abstract void run();//First thing to be called

	public final void gameEndGUI(String command) {
		Platform.runLater(() -> {
			gui.updateEnd(command);
		});
	}
	public final void closeGame() {
		Platform.runLater(() -> {
			gui.close();
		});
	}
	public final int getID() {
		return id;
	}
	public final void setID(int n) {
		System.out.println("Set ID = " + n);
		this.id = n;
	}
	public final String getName() {
		return name;
	}
	public final void sendMessageToServer(String msg) {
		try {
			dos.writeUTF(msg);
		} catch (Exception e) {
			//e.printStackTrace();
			System.out.println("Reached error at sending message to server");
		}
	}

	public abstract void closeSelf();
}
