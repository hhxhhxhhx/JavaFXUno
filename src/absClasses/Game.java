package absClasses;

import java.util.ArrayList;

public abstract class Game {
	
	protected int turn = 0;
	protected int direction = 1;
	protected int totalHumans;
	protected int totalBots;
	
	protected ArrayList<Integer> humanPlayerIndexes;
	
	protected Game(int numPlayers, int numBots) {
		totalHumans = numPlayers;
		totalBots = numBots;
	}
	
	public abstract Card getLastCard();
	public abstract ArrayList<Card> getHand(int index);
	public abstract boolean isValidMove(String command);
	public abstract Object takeTurnBot(String command);
	public abstract void takeTurnBot(Card c);
	public abstract void takeTurnHuman(String command);
	public abstract boolean isOver();
	public abstract boolean lastPlayIsDraw();
	public abstract void runGame();
	
	public final ArrayList<Integer> getHumanPlayerIndexes() {
		return humanPlayerIndexes;
	}
	public final int getTurn() {
		return turn;
	}
	public final boolean isForward() {
		return direction == 1;
	}
	public final void moveToNextPlayer() {
		turn = getNextPlayerIndex();
	}
	public final int getNextPlayerIndex() {
		return (turn + direction + totalHumans + totalBots) % (totalHumans + totalBots);
	}
}
