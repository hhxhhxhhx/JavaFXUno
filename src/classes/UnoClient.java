package classes;

import java.net.Socket;

import absClasses.Client;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.Stage;

/**
 * Client Object, this is the class that creates a GUI object and 
 * 		receives messages from the Server. It will determine what 
 * 		method to call in the GUI based on the message received.
 * @author Roger
 *
 */
public class UnoClient extends Client {
	
	private Thread readMessage;
	private boolean exit = false;

	public UnoClient(Socket socket, String name) {
		super(socket, name);
	}

	@Override
	public void startGUI() {
		new JFXPanel();
		this.gui = new UnoGameGUI(this, 900, 670);
		Platform.runLater(() -> {
			gui.start(new Stage());
		});
	}

	@Override
	public void run() {
		readMessage = new Thread(()	-> {
			while (!exit) { 
				try {
					String msg = dis.readUTF(); 
					if (msg.equals("PING COMMAND")) {
						sendMessageToServer("Client received ping command");
					} else if (msg.equals("Successfully connected")) {
						this.sendMessageToServer(this.name);
						System.out.println("\n" + msg + "!\n");
					} else if (msg.equals("START")) {
						System.out.println("Game loaded!");
						startGUI();
					} else if (msg.indexOf("MAX_NUM") != -1) {
						this.gui.setTotalPlayers(Integer.parseInt(msg.substring(msg.indexOf("\n") + 1)));
					} else if (msg.indexOf("CHOOSE WHO TO SWAP") != -1) {
						Platform.runLater(() -> {
							((UnoGameGUI)(this.gui)).selectSomeoneToSwap(msg.substring(msg.indexOf("\n") + 1));
						});
					} else if (msg.equals("Loading...")) {
						System.out.println("Game loading . . .\n");
					} else if (msg.indexOf("ANIMATE CARD DRAW") != -1) {
						Platform.runLater(() -> {
							((UnoGameGUI)(this.gui)).animateCardDraw(msg.substring(msg.indexOf("\n") + 1));
						});
					} else if (msg.indexOf("ANIMATE CARD PLAY") != -1) {
						Platform.runLater(() -> {
							((UnoGameGUI)(this.gui)).animateCardPlay(msg.substring(msg.indexOf("\n") + 1));
						});
					} else if (msg.indexOf("ROTATE HANDS") != -1) {
						Platform.runLater(() -> {
							((UnoGameGUI)(this.gui)).showRotateCardOverlay(msg.substring(msg.indexOf("\n") + 1));
						});
					} else if (msg.indexOf("REVERSE OVERLAY") != -1) {
						Platform.runLater(() -> {
							((UnoGameGUI)(this.gui)).showReverseOverlay(msg.substring(msg.indexOf("\n") + 1));
						});
					} else if (msg.indexOf("SKIP OVERLAY") != -1) {
						Platform.runLater(() -> {
							((UnoGameGUI)(this.gui)).showSkipOverlay(msg.substring(msg.indexOf("\n") + 1));
						});
					} else if (msg.indexOf("PLUS2 OVERLAY") != -1) {
						Platform.runLater(() -> {
							((UnoGameGUI)(this.gui)).showPlus2Overlay(msg.substring(msg.indexOf("\n") + 1));
						});
					} else if (msg.indexOf("PLUS4 OVERLAY") != -1) {
						Platform.runLater(() -> {
							((UnoGameGUI)(this.gui)).showPlus4Overlay(msg.substring(msg.indexOf("\n") + 1));
						});
					} else if (msg.indexOf("WILD OVERLAY") != -1) {
						Platform.runLater(() -> {
							((UnoGameGUI)(this.gui)).showWildOverlay(msg.substring(msg.indexOf("\n") + 1));
						});
					} else if (msg.indexOf("SWAP HANDS") != -1) {
						Platform.runLater(() -> {
							((UnoGameGUI)(this.gui)).showSwapCardOverlay(msg.substring(msg.indexOf("\n") + 1));
						});
					} else if (msg.indexOf("WANT TO STACK") != -1) {
						Platform.runLater(() -> {
							((UnoGameGUI)(this.gui)).stackableCard(msg.substring(msg.indexOf("\n") + 1));
						});
					} else if (msg.indexOf("UPDATE HAND") != -1) {
						Platform.runLater(() -> {
							((UnoGameGUI)(this.gui)).updateHand(msg.substring(msg.indexOf("\n") + 1));
						});
					} else if (msg.indexOf("UPDATE NUMCARDS") != -1) {
						Platform.runLater(() -> {
							((UnoGameGUI)(this.gui)).updateNumCards(msg.substring(msg.indexOf("\n") + 1));
						});
					} else if (msg.indexOf("UPDATE NUMCARDS FINAL") != -1) {
						Platform.runLater(() -> {
							((UnoGameGUI)(this.gui)).updateNumCardsFinal(msg.substring(msg.indexOf("\n") + 1));
						});
					} else if (msg.indexOf("UPDATE CARD PLAYED") != -1) {
						Platform.runLater(() -> {
							((UnoGameGUI)(this.gui)).updateCardPlayed(msg.substring(msg.indexOf("\n") + 1));
						});
					} else if (msg.indexOf("UPDATE GAME DIRECTION") != -1) {
						Platform.runLater(() -> {
							((UnoGameGUI)(this.gui)).updateGameDirection(msg.substring(msg.indexOf("\n") + 1));
						});
					} else if (msg.indexOf("UPDATE END") != -1) {
						Platform.runLater(() -> {
							this.gui.updateEnd(msg.substring(msg.indexOf("\n") + 1));
						});
					} else if (msg.indexOf("ID") != -1) {
						setID(Integer.parseInt(msg.substring(msg.indexOf("\n") + 1)));
					} else if (msg.indexOf("CURSOR") != -1) {
						Platform.runLater(() -> {
							this.gui.updateCursor(msg.substring(msg.indexOf("\n") + 1));
						});
					} else if (msg.indexOf("PLAY THIS ONE?") != -1) {
						Platform.runLater(() -> {
							((UnoGameGUI)(this.gui)).drew_this_card(msg.substring(msg.indexOf("\n") + 1));
						});
					} else if (msg.indexOf("DRAW IFF NEEDED") != -1) {
						Platform.runLater(() -> {
							((UnoGameGUI)(this.gui)).setDrawIffNeeded(msg.substring(msg.indexOf("\n") + 1));
						});
					}
				} catch (Exception e) { 
					System.out.println("Lost connection to server");
					e.printStackTrace();
					break;
				} 
			}
		}); 
		readMessage.start();
	}
	public final void closeSelf() {
		readMessage.interrupt();
		exit = true;
	}
}
