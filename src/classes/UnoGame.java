package classes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

import absClasses.Card;
import absClasses.Game;
import enums.CardType;

/**
 * Game object. Contains rules for the game, as well as status of the game. 
 * Only thing it doesn't have control over is the GameGUI, which doesn't have
 * 		any relationship to this. 
 * @author Roger
 *
 */
public class UnoGame extends Game {
	
	public static final String VERSION = "V 6.2.0";
	
	private int stackedCards = 0;
	
	private boolean DRAW_UNTIL_PLAYABLE = true;

	private boolean ZERO_ROTATE;
	
	private boolean SEVEN_SWAP;
	
	private boolean STACKING;
	
	private UnoServer server;
	private ArrayList<ArrayList<UnoCard>> hands = new ArrayList<ArrayList<UnoCard>>();
	private UnoDeck deck;
	private UnoCard lastCard = null;
	private UnoCard lastCard_redisOrDrawRandom = null;
	
	private boolean lastPlayDraw = false;
	
	private Hashtable<Integer, UnoBot> bots = new Hashtable<Integer, UnoBot>();

	public UnoGame(int numHumans, int numBots, UnoServer server, boolean draw_till_playable, boolean enable_redistribute, boolean enable_random_draws, 
			boolean enable_zero_rotate, boolean enable_seven_swap, boolean enable_stacking) {
		super(numHumans, numBots);
		this.server = server;
		DRAW_UNTIL_PLAYABLE = draw_till_playable;
		ZERO_ROTATE = enable_zero_rotate;
		SEVEN_SWAP = enable_seven_swap;
		STACKING = enable_stacking;
		deck = new UnoDeck(enable_redistribute, enable_random_draws);
		
		humanPlayerIndexes = new ArrayList<Integer>();
		for (int i=0;i<numHumans + numBots;i++) {
			ArrayList<UnoCard> hand = new ArrayList<UnoCard>();
			hand.addAll(deck.draw(7));
			hands.add(hand);
			humanPlayerIndexes.add(i);
		}
		Collections.shuffle(humanPlayerIndexes);
		for (int i=0;i<numBots;i++)
			humanPlayerIndexes.remove(0);
		
		for (int i=0;i<numHumans + numBots; i++) {
			if (!humanPlayerIndexes.contains(i)) {
				bots.put(i, new UnoBot(i));
			}
		}
	}
	
	public String getBotName(int key) {
		return this.bots.get(key).getName();
	}
	
	public void runGame() {
		int minCards = Integer.MAX_VALUE;
		
		while (minCards > 0) {
			
			if (this.bots.size() == this.hands.size()) {
				//Only bots left
				System.out.println("\n\nAll players quit.\n");
				return;
			}
			
			try {
				System.out.println("It is "+this.turn+"'s turn");
				if (this.humanPlayerIndexes.contains(this.turn)) {
					//This is a human
					System.out.println("\tThis is a human");
					this.server.askForMove(this.turn);
				} else {
					System.out.println("\tThis is a robot");
					this.bots.get(this.turn).takeTurn(this);
				}
			} catch (Exception e) {
				//Errored cuz human didn't exist lmao
			}
			try {
				this.server.sendMsgUpdateNumCards();
				this.server.sendMsgUpdateGameDirection(this.isForward());
				this.server.sendMsgUpdateCursors();
				
				minCards = Integer.MAX_VALUE;
				for (ArrayList<UnoCard> hand : hands)
					minCards = Math.min(minCards, hand.size());
			} catch (Exception e) {
				e.printStackTrace();
				//return;
			}
		}
		
		try {
			if (this.humanPlayerIndexes.contains(this.turn)) // human won game
				this.server.sendMsgEndGame(this.turn);
			else
				this.server.sendMsgEndGame(bots.get(this.turn).getName()); // bot won game
		} catch (Exception e) {
			//Do nothing
		}
			
	}
	
	public int numPlayers() {
		return hands.size();
	}
	
	@Override
	public Object takeTurnBot(String command) {
		//Should be ONLY drawing a card, no need to check command, because what else can it be?
		lastPlayDraw = true;
		UnoCard uc = this.deck.draw(1).get(0);
		
		this.server.sendMsgAnimateCardDraw(this.turn, uc);
		hands.get(turn).add(uc);
		Util.sleep(900);
		this.server.sendMsgUpdateHand(this.turn);
		Util.sleep(400);
		this.server.sendMsgUpdateNumCards();
		
		if (uc.isCompatibleWith(lastCard) || (lastCard.getType() == CardType.SOCIALISM && uc.isCompatibleWith(lastCard_redisOrDrawRandom))) {
			return uc;
		} else {
			if (DRAW_UNTIL_PLAYABLE) {
				while (true) {
					UnoCard new_card = this.deck.draw(1).get(0);
					this.server.sendMsgAnimateCardDraw(this.turn, new_card);
					hands.get(turn).add(new_card);
					Util.sleep(900);
					this.server.sendMsgUpdateHand(this.turn);
					Util.sleep(400);
					this.server.sendMsgUpdateNumCards();
					if (new_card.isCompatibleWith(lastCard) || 
							(UnoCard.typeWillNotAffectNext(lastCard) && new_card.isCompatibleWith(lastCard_redisOrDrawRandom))) {
						return new_card;
					}
				}
			} else {
				this.moveToNextPlayer();
				return null;
			}
		}
	}
	
	@Override
	public void takeTurnBot(Card c) {
		takeTurnHuman(c.toString());
	}
	
	private void replacePlayerWithBot(int turn) {
		this.humanPlayerIndexes.remove(Integer.valueOf(turn));
		this.bots.put(turn, new UnoBot(turn));
	}

	@Override
	public void takeTurnHuman(String command) {
		
		if (command.equals("FUCK THIS SHIT IM OUT")) {
			replacePlayerWithBot(this.turn);
			return;
		}
		
		assert isValidMove(command) : "Not a valid move!";
		this.server.disableAllCursors();
		if (command.equals("DRAW")) {
			takeTurnDraw(command);
			return;
		}
		//They played a card
		
		//Check if card is a wild card, make sure it's valid
		UnoCard card = UnoCard.eval(command).get(0);
		
		if (card.getType() == CardType.WILD || card.getType() == CardType.WILD_DRAW4) {
			String[] parsed = command.split(",");
			assert parsed.length == 4 : "Wild Cards must provide a new color!";
			hands.get(this.turn).remove(card);
			card.setType(CardType.valueOf(parsed[3] + "_" + card.getType()));
		} else {
			hands.get(this.turn).remove(card);
		}
		this.server.sendMsgUpdateHand(this.turn);
		this.server.sendMsgAnimateCardPlay(this.turn, card);
		this.server.sendMsgUpdateNumCards();
		
		Util.sleep(700);
		
		if (UnoCard.typeWillNotAffectNext(card))
			lastCard_redisOrDrawRandom = lastCard;
		
		this.server.sendMsgUpdateCardPlayed(card);
		
		if (card.getType() == CardType.SOCIALISM) {
			this.server.sendMsgRedistributeOverlay();
			//TODO Util.sleep(time)
			ArrayList<UnoCard> cards = new ArrayList<UnoCard>();
			for (ArrayList<UnoCard> ucs : hands)
				for (UnoCard uc : ucs)
					cards.add(uc);
			Collections.shuffle(cards);
			int numOfCardsEach = cards.size() / hands.size();
			for (int i=this.turn;i<hands.size() + this.turn;i++) {
				final int j = i % hands.size();
				hands.get(j).clear();
				//Fuck the line below this, just the for loop line, nothing else
				for (int k=0;k<numOfCardsEach;k++)
					hands.get(j).add(cards.remove(0));
				if (i != hands.size() + this.turn - 1)
					numOfCardsEach = cards.size() / (hands.size() + this.turn - i - 1);
				this.server.sendMsgUpdateHand(j);
			}
			this.server.sendMsgUpdateNumCards();
		} else if (card.getType() == CardType.DRAW_RANDOM_AMOUNT) {
			this.server.sendMsgDrawRandomAmountOverlay();
			//TODO Util.sleep(time);
			final int AVERAGE_CARDS_PER_PERSON = 3;
			int totalAmount = hands.size() * AVERAGE_CARDS_PER_PERSON;
			for (int i=0;i<totalAmount;i++) {
				int indexDrawing = (int)(Math.random() * hands.size());

				UnoCard drawnCard = deck.draw(1).get(0);
				this.server.sendMsgAnimateCardDraw(indexDrawing, drawnCard);
				hands.get(indexDrawing).add(drawnCard);
				Util.sleep(900);
				this.server.sendMsgUpdateHand(indexDrawing);
				Util.sleep(400);
				this.server.sendMsgUpdateNumCards();
			}
		}
		
		if (this.hands.get(this.turn).size() == 0)
			return;
		
		if (ZERO_ROTATE && card.getValue() == 0) {
			if (this.direction == 1) { //0 --> 1
				this.hands.add(0, this.hands.remove(this.hands.size() - 1));
			} else if (this.direction == -1) { // 1 --> 0
				this.hands.add(this.hands.remove(0));
			}
			this.server.sendMsgRotateOverlay(this.direction == 1);
			Util.sleep(2);
			for (int i=0;i<hands.size();i++)
				this.server.sendMsgUpdateHand(i);
			this.server.sendMsgUpdateNumCards();
		} else if (SEVEN_SWAP & card.getValue() == 7) {
			lastCard = card;
			if (this.humanPlayerIndexes.contains(this.turn)) {
				this.server.askWhoToSwapWith(this.turn);
				this.server.takeResponseFromClient_WhoToSwapWith(this.turn);
			} else {
				ArrayList<Integer> numCards = new ArrayList<Integer>();
				for (ArrayList<UnoCard> uc : hands)
					numCards.add(uc.size());
				takeTurnSevenSwap(this.bots.get(this.turn).chooseSomeoneToSwapWith(numCards));
			}
			return;
		}
		
		String cType = card.getType().toString();
		
		if (cType.indexOf("REVERSE") != -1) {
			this.server.sendMsgReverseOverlay(this.direction == 1);
			Util.sleep(3);
		} else if (cType.indexOf("SKIP") != -1) {
			this.server.sendMsgSkipOverlay(card.getType().toString() + "\n" + (this.direction == 1));
			Util.sleep(2);
		} else if (cType.indexOf("PLUS2") != -1) {
			stackedCards += 2;
			this.server.sendMsgPlus2Overlay(card.getType().toString() + "\n" + (this.direction == 1) + "\n" + stackedCards);
			Util.sleep(2);
		} else if (cType.indexOf("DRAW4") != -1) {
			stackedCards += 4;
			if (UnoCard.typeWillNotAffectNext(lastCard))
				this.server.sendMsgPlus4Overlay(lastCard_redisOrDrawRandom.getType().toString() + "\n" + card.getType().toString() + "\n" + (this.direction == 1) + "\n" + stackedCards);
			else if (lastCard != null)
				this.server.sendMsgPlus4Overlay(lastCard.getType().toString() + "\n" + card.getType().toString() + "\n" + (this.direction == 1) + "\n" + stackedCards);
			else
				this.server.sendMsgPlus4Overlay(card.getType().toString() + "\n" + card.getType().toString() + "\n" + (this.direction == 1) + "\n" + stackedCards);
			Util.sleep(2);
		} else if (cType.indexOf("WILD") != -1) {
			if (UnoCard.typeWillNotAffectNext(lastCard))
				this.server.sendMsgWildOverlay(lastCard_redisOrDrawRandom.getType().toString() + "\n" + card.getType().toString() + "\n" + (this.direction == 1));
			else if (lastCard != null)
				this.server.sendMsgWildOverlay(lastCard.getType().toString() + "\n" + card.getType().toString() + "\n" + (this.direction == 1));
			else
				this.server.sendMsgWildOverlay(card.getType().toString() + "\n" + card.getType().toString() + "\n" + (this.direction == 1));
			Util.sleep(2);
		}
		
		lastCard = card;
		
		//This block here changes direction / skips a player
		if (cType.indexOf("REVERSE") != -1) {
			this.direction *= -1;
		} else if (cType.indexOf("SKIP") != -1 || cType.indexOf("DRAW4") != -1 ||
				   cType.indexOf("PLUS2") != -1) {
			this.moveToNextPlayer();
		}
		if (STACKING) {
			if (cType.indexOf("PLUS2") != -1) {
				ArrayList<Card> stackableCards = new ArrayList<Card>();
				for (Card c : hands.get(turn)) 
					if (c.getType().toString().indexOf("PLUS2") != -1 || c.getType().toString().indexOf("DRAW4") != -1) 
						stackableCards.add(c);
				UnoCard.sortOther(stackableCards);
				if (stackableCards.size() != 0) {
					this.server.sendMsgUpdateNumCards();
					//They have a stackable
					System.out.println("It is "+this.turn+"'s turn to stack");
					if (this.humanPlayerIndexes.contains(this.turn)) {
						System.out.println("\tThis is a human");
						this.server.askIfWantToStack(this.turn, stackableCards);
						this.server.takeResponseFromClient_ToStackOrNotToStack(turn);
					} else {
						System.out.println("\tThis is a robot");
						try {
							this.bots.get(this.turn).askIfWantToStack(this, stackableCards);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					return;
				}
			} else if (cType.indexOf("DRAW4") != -1) {
				ArrayList<Card> stackableCards = new ArrayList<Card>();
				for (Card c : hands.get(turn)) 
					if (c.getType().toString().indexOf("DRAW4") != -1) 
						stackableCards.add(c);
				UnoCard.sortOther(stackableCards);
				if (stackableCards.size() != 0) {
					this.server.sendMsgUpdateNumCards();
					//They have a stackable
					System.out.println("It is "+this.turn+"'s turn to stack");
					if (this.humanPlayerIndexes.contains(this.turn)) {
						System.out.println("\tThis is a human");
						this.server.askIfWantToStack(this.turn, stackableCards);
						this.server.takeResponseFromClient_ToStackOrNotToStack(turn);
					} else {
						System.out.println("\tThis is a robot");
						try {
							this.bots.get(this.turn).askIfWantToStack(this, stackableCards);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					return;
				}
			}
		}
		//Reached if no stacking available
		
		for (int i=0;i<stackedCards;i++) {
			UnoCard drawnCard = deck.draw(1).get(0);
			this.server.sendMsgAnimateCardDraw(this.turn, drawnCard);
			hands.get(turn).add(drawnCard);
			Util.sleep(900);
			this.server.sendMsgUpdateHand(this.turn);
			Util.sleep(400);
			this.server.sendMsgUpdateNumCards();
		}
		stackedCards = 0;
		
		if (!UnoCard.typeWillNotAffectNext(card)) {
			lastCard_redisOrDrawRandom = null;
		}
		//End code that adds cards to another player's hand
		this.moveToNextPlayer();
		lastPlayDraw = false;
	}

	private void takeTurnDraw(String command) {
		
		lastPlayDraw = true;
		UnoCard uc = this.deck.draw(1).get(0);
		
		this.server.sendMsgAnimateCardDraw(this.turn, uc);
		hands.get(turn).add(uc);
		Util.sleep(900);
		this.server.sendMsgUpdateHand(this.turn);
		Util.sleep(400);
		this.server.sendMsgUpdateNumCards();
		
		if (uc.isCompatibleWith(lastCard) || (lastCard.getType() == CardType.SOCIALISM && uc.isCompatibleWith(lastCard_redisOrDrawRandom))) {
			this.server.sendMessageToClient(turn, "PLAY THIS ONE?" + "\n" + uc.toString());
			this.server.takeResponseFromClient_ToPlayOrNotToPlayDrawn(turn);
			return;
		} else {
			if (DRAW_UNTIL_PLAYABLE) {
				while (true) {
					UnoCard new_card = this.deck.draw(1).get(0);
					this.server.sendMsgAnimateCardDraw(this.turn, new_card);
					hands.get(turn).add(new_card);
					Util.sleep(900);
					this.server.sendMsgUpdateHand(this.turn);
					Util.sleep(400);
					this.server.sendMsgUpdateNumCards();
					if (new_card.isCompatibleWith(lastCard) || 
							(UnoCard.typeWillNotAffectNext(lastCard) && new_card.isCompatibleWith(lastCard_redisOrDrawRandom))) {
						this.server.sendMessageToClient(turn, "PLAY THIS ONE?" + "\n" + new_card.toString());
						this.server.takeResponseFromClient_ToPlayOrNotToPlayDrawn(turn);
						return;
					}
				}
			} else {
				this.moveToNextPlayer();
			}
		}
	}
	
	public void takeTurnDraw_PlayTheCard(String input) {
		takeTurnHuman(input);
	}
	
	public void takeTurnDraw_DoNotPlayTheCard() {
		this.moveToNextPlayer();
	}
	
	public void takeTurnStack_DoStack(String input) {
		takeTurnHuman(input);
	}
	
	public void takeTurnStack_DoNotStack() {
		for (int i=0;i<stackedCards;i++) {
			UnoCard drawnCard = deck.draw(1).get(0);
			this.server.sendMsgAnimateCardDraw(this.turn, drawnCard);
			hands.get(turn).add(drawnCard);
			Util.sleep(900);
			this.server.sendMsgUpdateHand(this.turn);
			Util.sleep(400);
			this.server.sendMsgUpdateNumCards();
		}
		stackedCards = 0;
		
		if (!UnoCard.typeWillNotAffectNext(lastCard)) {
			lastCard_redisOrDrawRandom = null;
		}
		this.moveToNextPlayer();
		lastPlayDraw = false;
	}
	
	public void takeTurnSevenSwap(int playerID) {
		
		ArrayList<UnoCard> hand = hands.get(this.turn);
		
		hands.set(this.turn, hands.get(playerID));
		hands.set(playerID, hand);
		this.server.sendMsgSwapCards(this.turn, playerID);
		Util.sleep(2);
		for (int i=0;i<hands.size();i++)
			this.server.sendMsgUpdateHand(i);
		this.server.sendMsgUpdateNumCards();
		
		stackedCards = 0;
		
		if (!UnoCard.typeWillNotAffectNext(lastCard)) {
			lastCard_redisOrDrawRandom = null;
		}
		//End code that adds cards to another player's hand
		this.moveToNextPlayer();
		lastPlayDraw = false;
	}

	@Override
	public boolean isOver() {
		for (ArrayList<UnoCard> hand : hands)
			if (hand.size() == 0) 
				return true;
		return false;
	}

	@Override
	public boolean lastPlayIsDraw() {
		return lastPlayDraw;
	}
	
	
	@Override
	public ArrayList<Card> getHand(int index) {
		ArrayList<Card> newHand = new ArrayList<Card>();
		for (UnoCard c:hands.get(index))
			newHand.add(c);
		return newHand;
	}
	
	@Override
	public Card getLastCard() {
		if (lastCard == null)
			return null;
		if (UnoCard.typeWillNotAffectNext(lastCard))
			return lastCard_redisOrDrawRandom;
		return lastCard;
	}
	
	public UnoCard getTrueLastCard() {
		return lastCard;
	}

	@Override
	public boolean isValidMove(String command) {
		if (command.equals("DRAW"))
			return true;
		if (UnoCard.eval(command) == null)
			return false;
		UnoCard c = UnoCard.eval(command).get(0);
		if (lastCard == null)
			return true;
		if (!UnoCard.typeWillNotAffectNext(lastCard))
			return c.isCompatibleWith(lastCard);
		else
			return c.isCompatibleWith(lastCard_redisOrDrawRandom);
	}
	
	public boolean zeroRotateEnabled() {
		return this.ZERO_ROTATE;
	}
	public boolean sevenSwapEnabled() {
		return this.SEVEN_SWAP;
	}
}
