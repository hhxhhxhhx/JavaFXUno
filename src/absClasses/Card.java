package absClasses;

import java.util.ArrayList;

import enums.CardType;
import javafx.scene.layout.Pane;

public abstract class Card {
	protected boolean special;
	protected int value;
	protected CardType type;
	protected Pane pane;
	
	public abstract String toString();
	
	protected abstract int getSortValue();
	
	public abstract boolean isCompatibleWith(Card other);
	
	public abstract Pane getCopyOfPane(int...size);
	
	public static String handToString(ArrayList<Card> cards) {
		String msg = "";
		for (Card c : cards)
			msg += c.toString() + "\n";
		return msg;
	}
	
	@Override
	public final boolean equals(Object o) {
		Card other = (Card)(o);
		return this.special == other.special && this.value == other.value &&
			   this.type == other.type;
	}
	
	public final int getValue() {
		return value;
	}
	
	public final CardType getType() {
		return type;
	}
	
	public final void setType(CardType ct) {
		this.type = ct;
	}
	
	public final Pane getPane() {
		return pane;
	}
	
	public final boolean isSpecial() {
		return special;
	}
	
	public final int getNextPlayerDraws() {
		switch (type) {
			case WILD_DRAW4:
				return 4;
			case RED_PLUS2:
			case YELLOW_PLUS2:
			case BLUE_PLUS2:
			case GREEN_PLUS2:
				return 2;
			default:
				return 0;
		}
	}
	
	public final boolean skipNextPlayer() {
		if (getNextPlayerDraws() != 0)
			return true;
		switch (type) {
			case RED_SKIP:
			case YELLOW_SKIP:
			case BLUE_SKIP:
			case GREEN_SKIP:
				return true;
			default:
				return false;
		}
	}
	
	public final boolean isReverse() {
		return type.toString().indexOf("REVERSE") != -1;
	}
	
	public final boolean isSkip() {
		return type.toString().indexOf("SKIP") != -1;
	}
}
