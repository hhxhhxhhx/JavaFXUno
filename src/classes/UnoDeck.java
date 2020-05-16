package classes;

import java.util.ArrayList;

import absClasses.Deck;

/**
 * Deck object for Uno. An instance of this is created by the Game object. 
 * @author Roger
 *
 */
public class UnoDeck extends Deck {

	private ArrayList<UnoCard> cards = new ArrayList<UnoCard>();
	private boolean isFirstDeck = true;
	
	private boolean enableRedistribute;
	private boolean enableDrawRandomAmount;
	
	public UnoDeck(boolean enableRedistribute, boolean enableRandomDraws) {
		super();
		this.enableRedistribute = enableRedistribute;
		this.enableDrawRandomAmount = enableRandomDraws;
		initDeck();
	}
	
	public final ArrayList<UnoCard> draw(int n) {
		ArrayList<UnoCard> c = new ArrayList<UnoCard>();
		for (int i=0;i<n;i++) {
			if (cards.size() == 0)
				initDeck();
			c.add(cards.remove(0));
		}
		return c;
	}
	
	@Override
	protected void initDeck() {
		if (isFirstDeck) {
			this.cards = UnoCard.eval("All Cards");
		} else {
			this.cards = UnoCard.eval("Regular Cards");
		}
		if (isFirstDeck) {
			if (enableRedistribute) 
				this.cards.add((int)(Math.random() * this.cards.size()), UnoCard.SOCIALISM);
			if (enableDrawRandomAmount)
				this.cards.add((int)(Math.random() * this.cards.size()), UnoCard.DRAWRANDOM);
		}
		if (isFirstDeck) {
			//isFirstDeck = false;
		}
	}

}
