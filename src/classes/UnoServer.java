package classes;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;

import absClasses.Card;
import absClasses.Server;

/**
 * Server Object created and executed by UnoServerLauncher after launching the server. 
 * It can send messages to any specific client or all clients, and receives commands
 * 		from the Game object to send to each Client. Clients then will update their 
 * 		GUI by themselves. 
 * @author Roger
 *
 */
public class UnoServer extends Server {
	
	private boolean DRAW_AT_WILL;
	private boolean ENABLE_SEVEN_SWAP;
	ArrayList<Integer> keys;

	public UnoServer(int players, int bots, int port, boolean draw_only_one, boolean draw_at_will, boolean enable_redistribute, boolean enable_random_draws,
			boolean enable_zero_rotate, boolean enable_seven_swap, boolean enable_stacking) {
		super(players, bots, port);
		System.out.println();
		System.out.println();
		System.out.println("Uno " + UnoGame.VERSION);
		System.out.println();
		System.out.println("Human players: " + players);
		System.out.println("Bots: " + bots);
		System.out.println("Total players: " + (players + bots));
		System.out.println();
		System.out.println((draw_only_one ? "Enabled: " : "Disabled: ") + "Draw only 1 card");
		System.out.println((draw_at_will ? "Enabled: " : "Disabled: ") + "Draw at will");
		System.out.println((enable_redistribute ? "Enabled: " : "Disabled: ") + "Redistribute card");
		System.out.println((enable_random_draws ? "Enabled: " : "Disabled: ") + "Randomly draw cards");
		System.out.println((enable_zero_rotate ? "Enabled: " : "Disabled: ") + "Rotate hands when a zero is played");
		System.out.println((enable_seven_swap ? "Enabled: " : "Disabled: ") + "Swap hands with a random person when a seven is played");
		System.out.println((enable_stacking ? "Enabled: " : "Disabled: ") + "Stacking +2 and +4");
		System.out.println();
		DRAW_AT_WILL = draw_at_will;
		ENABLE_SEVEN_SWAP = enable_seven_swap;
		this.game = new UnoGame(players, bots, this, !draw_only_one, enable_redistribute, enable_random_draws, enable_zero_rotate, enable_seven_swap, 
								enable_stacking);
		keys = game.getHumanPlayerIndexes();
	}

	@Override
	protected String parseInput(String input) {
		return input;
	}

	@Override
	public void start() throws Exception {
		ServerSocket serverSocket = null;
		
		try {
			serverSocket = new ServerSocket(this.port);
		} catch (IOException e) {
			System.out.println("Could not reach port "+this.port);
		}

		System.out.println("Server established at port " + this.port);
		System.out.println("Waiting for clients . . . (max of " + this.humans + ")");
		

		/**
		 * Get ID list from Game for human players
		 */
		int index = 0;
		while (this.numOfConnections() < this.humans) {
			Socket socket = null;
			try {
				socket = serverSocket.accept();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Can't accept new clients!");
			}
			add(keys.get(index), socket);
			//Server.java also prints "Successfully Connected" to Client
			System.out.println("Accepted new client " + keys.get(index) + "!");
			index += 1;
		}
		
		Collections.sort(keys);
		
		try {
			serverSocket.close();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.out.println("Error at closing server socket");
		}
		
		System.out.println("All clients logged in");
		
		for (Integer n : keys) {
			sendMessageToClient(n, "Loading...");
			sendMessageToClient(n, "ID" + "\n" + n);
		}
		
		for (Integer c : keys) {
			sendMessageToClient(c, "START");
			sendMessageToClient(c, "DRAW IFF NEEDED\n" + !DRAW_AT_WILL);
			sendMessageToClient(c, "ENABLE SEVEN SWAP\n" + ENABLE_SEVEN_SWAP);
			sendMessageToClient(c, "MAX_NUM" + "\n" + (this.humans + this.bots));
		}
		
		sendMsgUpdateCursors();
		for (Integer i : keys)
			sendMsgUpdateHand(i);
		
		sendMsgUpdateNumCards();
		sendMsgUpdateGameDirection(game.isForward());
		sendMsgUpdateCardPlayed(null);
		
		game.runGame();
		
		java.util.Scanner s = new java.util.Scanner(System.in);
		System.out.print("Press [Enter] to exit.");
		s.nextLine();
		s.close();
	}
	
	public void askForMove(int key) {
		sendMsgUpdateCursors();
		try {
			sendMessageToClient(key, "PING COMMAND");

			boolean gotPing = false;
			String input = "";
			
			while (true) {
				input = this.inputStreams.get(key).readUTF();
				if (input.equals("FUCK THIS SHIT IM OUT"))
					break;
				if (gotPing)
					break;
				if (input.equals("imagine needing to send a useless message or this won't work"))
					gotPing = true;
			}
			
			System.out.println("\t\tClient " + key + " trying to play " + input);
			
			input = parseInput(input);
			
			while (!game.isValidMove(input) && !input.equals("FUCK THIS SHIT IM OUT")) {
				System.out.println("\tInvalid move!");
				input = this.inputStreams.get(key).readUTF();
				input = parseInput(input);
			}
			
			if (input.equals("FUCK THIS SHIT IM OUT")) {
				keys.remove(Integer.valueOf(key));
				this.clientNames.remove(key);
				this.inputStreams.remove(key);
				this.outputStreams.remove(key);
			}
			
			game.takeTurnHuman(input);
			
		} catch (Exception e) {
			//Error cuz guy dc'd
			keys.remove(Integer.valueOf(key));
			this.clientNames.remove(key);
			this.inputStreams.remove(key);
			this.outputStreams.remove(key);
			game.takeTurnHuman("FUCK THIS SHIT IM OUT");
		}
	}
	
	public void sendMsgEndGame(int winner_key) {
		sendMsgUpdateNumCardsFinal();
		sendMsgUpdateEnd(winner_key);
	}
	
	public void sendMsgEndGame(String winner_name) {
		sendMsgUpdateNumCardsFinal();
		sendMsgUpdateEnd(winner_name);
	}
	
	public void sendMsgUpdateCursors() {
		for (Integer i : keys) {
			if (i != game.getTurn() || game.isOver()) {
				sendMessageToClient(i, "CURSOR\nDISABLE");
			} else {
				sendMessageToClient(i, "CURSOR\nENABLE");
			}
		}
	}
	
	public void disableAllCursors() {
		for (Integer i : keys) 
			sendMessageToClient(i, "CURSOR\nDISABLE");
		
	}
	
	public void sendMsgRotateOverlay(boolean b) {
		for (Integer i : keys) 
			sendMessageToClient(i, "ROTATE HANDS\n" + b);
		
	}

	public void sendMsgReverseOverlay(boolean b) {
		for (Integer i : keys) 
			sendMessageToClient(i, "REVERSE OVERLAY\n" + b);
	}
	
	public void sendMsgSkipOverlay(String b) {
		for (Integer i : keys) 
			sendMessageToClient(i, "SKIP OVERLAY\n" + b);
	}
	
	public void sendMsgPlus2Overlay(String b) {
		for (Integer i : keys) 
			sendMessageToClient(i, "PLUS2 OVERLAY\n" + b);
	}
	
	public void sendMsgPlus4Overlay(String b) {
		for (Integer i : keys) 
			sendMessageToClient(i, "PLUS4 OVERLAY\n" + b);
	}
	
	public void sendMsgWildOverlay(String b) {
		for (Integer i : keys) 
			sendMessageToClient(i, "WILD OVERLAY\n" + b);
	}
	
	public void sendMsgSwapCards(int turn, int randomIndex) {
		for (Integer i : keys) 
			sendMessageToClient(i, "SWAP HANDS\n" + turn + "\n" + randomIndex);
	}
	
	public void sendMsgAnimateCardDraw(int id_who_drew, UnoCard card) {
		for (Integer i : keys) 
			sendMessageToClient(i, "ANIMATE CARD DRAW\n" + id_who_drew + "\n" + card.toString());
	}
	
	public void sendMsgAnimateCardPlay(int id_who_played, UnoCard card) {
		for (Integer i : keys) 
			sendMessageToClient(i, "ANIMATE CARD PLAY\n" + id_who_played + "\n" + card.toString());
	}
	
	public void sendMsgUpdateHand(int id_to_update) {
		if (!keys.contains(id_to_update))
			return;
		String msg = "UPDATE HAND\n";
		for (Card c : this.game.getHand(id_to_update)) 
			msg += c.toString() + "\n";
		sendMessageToClient(id_to_update, msg);
	}
	
	public void sendMsgUpdateNumCards() {
		String msg = "UPDATE NUMCARDS\n" + this.game.getTurn() + "\n";
		for (int i=0;i<this.bots+this.humans;i++) {
			if (keys.contains(i))
				msg += this.clientNames.get(i) + "," + this.game.getHand(i).size() + "\n";
			else
				msg += ((UnoGame)(this.game)).getBotName(i) + "," + this.game.getHand(i).size() + "\n";
		}
		for (Integer i : keys) {
			sendMessageToClient(i, msg);
		}
	}
	
	public void sendMsgUpdateNumCardsFinal() {
		String msg = "UPDATE NUMCARDS FINAL\n" + this.game.getTurn() + "\n";
		for (int i=0;i<this.bots+this.humans;i++) {
			if (keys.contains(i))
				msg += this.clientNames.get(i) + "," + this.game.getHand(i).size() + "\n";
			else
				msg += ((UnoGame)(this.game)).getBotName(i) + "," + this.game.getHand(i).size() + "\n";
		}
		for (Integer i : keys)
			sendMessageToClient(i, msg);
	}
	
	public void sendMsgUpdateCardPlayed(UnoCard c) {
		String msg = "";
		if (c != null) 
			msg = "UPDATE CARD PLAYED\n" + c.toString();
		else
			msg = "UPDATE CARD PLAYED\nnull";
		for (Integer i : keys) {
			sendMessageToClient(i, msg);
		}
	}
	
	public void sendMsgUpdateGameDirection(boolean is_normal) {
		String msg = "UPDATE GAME DIRECTION\n" + is_normal;
		for (Integer i : keys)
			sendMessageToClient(i, msg);
	}
	
	public void sendMsgUpdateEnd(int winner_key) {
		String msg = "UPDATE END\n " + this.clientNames.get(winner_key) + " has won the game!";
		for (Integer i : keys)
			sendMessageToClient(i, msg);
	}
	
	public void sendMsgUpdateEnd(String winner_name) {
		String msg = "UPDATE END\n " + winner_name + " has won the game!";
		for (Integer i : keys)
			sendMessageToClient(i, msg);
	}
	
	public void sendMessageToClient(int id, String msg) {
		try {
			this.outputStreams.get(id).writeUTF(msg);
		} catch (Exception e) {
			//Do nothing
		}
	}

	public void takeResponseFromClient_ToPlayOrNotToPlayDrawn(int turn) {
		String input = "";
		
		try {
			input = this.inputStreams.get(turn).readUTF();
			if (input.equals("NO")) {
				((UnoGame)(this.game)).takeTurnDraw_DoNotPlayTheCard();
			} else {
				((UnoGame)(this.game)).takeTurnDraw_PlayTheCard(input);
			}
		} catch (Exception e) {
			//e.printStackTrace();
			//System.out.println("Error at takeResponseFromClient.......");
		}
	}
	
	public void askIfWantToStack(int turn, ArrayList<Card> stackableCards) {
		String msg = "WANT TO STACK\n" + UnoCard.handToString(stackableCards);
		sendMessageToClient(turn, msg);
	}
	
	public void takeResponseFromClient_ToStackOrNotToStack(int turn) {
		String input = "";
		
		try {
			input = this.inputStreams.get(turn).readUTF();
			System.out.println("\t\t\tClient " + turn + " trying to stack " + input);
			if (input.equals("NO STACK")) {
				((UnoGame)(this.game)).takeTurnStack_DoNotStack();
			} else {
				((UnoGame)(this.game)).takeTurnStack_DoStack(input);
			}
		} catch (Exception e) {
			//e.printStackTrace();
			//System.out.println("Error at takeResponseFromClient.......");
		}
	}

	public void askWhoToSwapWith(int turn) {
		String msg = "CHOOSE WHO TO SWAP\n";
		for (int i=0;i<this.bots+this.humans;i++) {
			if (keys.contains(i))
				msg += this.clientNames.get(i) + "," + this.game.getHand(i).size() + "\n";
			else
				msg += ((UnoGame)(this.game)).getBotName(i) + "," + this.game.getHand(i).size() + "\n";
		}
		sendMessageToClient(turn, msg);
	}
	
	public void takeResponseFromClient_WhoToSwapWith(int turn) {
		String input = "";
		try {
			input = this.inputStreams.get(turn).readUTF();
			System.out.println("\t\t\tClient " + turn + " wants to swap with " + input);
			((UnoGame)(this.game)).takeTurnSevenSwap(Integer.parseInt(input));
		} catch (Exception e) {
			//e.printStackTrace();
			//System.out.println("Error at takeResponseFromClient.......");
		}
	}

	public void sendMsgRedistributeOverlay() {
		// TODO Auto-generated method stub
		
	}

	public void sendMsgDrawRandomAmountOverlay() {
		// TODO Auto-generated method stub
		
	}
}
