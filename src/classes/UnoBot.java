package classes;

import java.util.ArrayList;

import absClasses.Bot;
import absClasses.Card;
import absClasses.Game;
import enums.CardType;

/**
 * Bot for playing Uno. Current logic is as follows:
 * 
 * If you have no playable cards, obviously draw a card.
 * If next player has equal or more cards than self, try to play a regular card.
 * Otherwise if next player has less cards than self but not the least cards in game,
 * 		it will play a +2/Reverse/Skip, if it doesn't have one, it'll play
 * 					   Nonspecial card
 * 					   +4/Redistribute/Draw random amount
 * If next player has the least amount of cards, it'll play:
 * 					+4
 * 					+2
 * 					+4/Redistribute/Draw random
 * 					+2/Reverse/Skip
 * If nothing has been played still, try to play cards in this order:
 * 					Non-special cards
 * 					+2/Reverse/Skip
 * 					Non +4 Wild
 * 					+4/Redistribute/Draw random
 * 
 * When playing wild cards, it'll check to see how many colored cards it has.
 * 		If for example, it has more red cards than any other card, it'll play the
 * 		red version of the wild card.
 * 		If there is a tie, it'll randomly choose one color.
 * 
 * @author Roger
 *
 */
public class UnoBot extends Bot {
	
	private int ID;

	public UnoBot(int id) {
		super();
		this.ID = id;
	}
	
	public void askIfWantToStack(Game g, ArrayList<Card> playableCards) throws Exception {
		UnoGame game = (UnoGame)g;
		//Always stack!
		Util.sleep(2);
		ArrayList<Card> nonSpecial = new ArrayList<Card>();
		ArrayList<Card> skipReversePlus2 = new ArrayList<Card>();
		ArrayList<Card> plus4RedDraw = new ArrayList<Card>();
		ArrayList<Card> myPlus2 = new ArrayList<Card>();
		ArrayList<Card> myPlus4 = new ArrayList<Card>();
		ArrayList<Card> myWildCards = new ArrayList<Card>();
		for (Card c : playableCards) {
			if (!c.isSpecial()) {
				//In future, account for 0 and 7. Don't play them if you have least cards
				nonSpecial.add(c);
			} else if (c.getType().toString().indexOf("WILD") == -1 &&
					   !UnoCard.typeWillNotAffectNext((UnoCard)c)) {
				//Skip, Reverse, +2
				//If next player has less cards than self, play these
				skipReversePlus2.add(c);
			} else if (c.getType() != CardType.WILD) {
				//If next player has the least cards, play these (+4, redistribute, draw rand)
				plus4RedDraw.add(c);
			}
			if (c.getType().toString().indexOf("PLUS2") != -1) {
				//+ 2
				myPlus2.add(c);
			}
			if (c.getType().toString().indexOf("DRAW4") != -1) {
				//+ 4
				myPlus4.add(c);
			} else if (c.getType().toString().indexOf("WILD") != -1) {
				//My wild cards
				myWildCards.add(c);
			}
		}
		
		int nextPlayerHandSize = game.getHand(game.getNextPlayerIndex()).size();
		
		int minCards = Integer.MAX_VALUE;
		for (int i=0;i<game.numPlayers();i++)
			minCards = Math.min(minCards, game.getHand(i).size());
		
		if (nextPlayerHandSize >= game.getHand(ID).size()) {
			/**
			 * Play normal cards if you have
			 */
			if (nonSpecial.size() != 0) {
				Card c = nonSpecial.get((int)(Math.random() * nonSpecial.size()));
				System.out.println("\t\tTrying to play " + c.toString());
				playThisCard(game, c);
				return;
			}
		}
		if (nextPlayerHandSize != minCards) {
			/**
			 * +2/Reverse/Skip prefered
			 * Normal cards
			 * +4/Redistribute/Random draw
			 */
			if (skipReversePlus2.size() != 0) {
				Card c = skipReversePlus2.get((int)(Math.random() * skipReversePlus2.size()));
				System.out.println("Trying to play " + c.toString());
				playThisCard(game, c);
				return;
			}
			if (nonSpecial.size() != 0) {
				Card c = nonSpecial.get((int)(Math.random() * nonSpecial.size()));
				System.out.println("Trying to play " + c.toString());
				playThisCard(game, c);
				return;
			}
			if (plus4RedDraw.size() != 0) {
				Card c = plus4RedDraw.get((int)(Math.random() * plus4RedDraw.size()));
				System.out.println("Trying to play " + c.toString());
				playThisCard(game, c);
				return;
			}
		}
		if (nextPlayerHandSize == minCards) {
			/**
			 * +4
			 * +2
			 * +4/Redistribute/Draw random
			 * +2/Reverse/Skip
			 */
			if (myPlus4.size() != 0) {
				Card c = myPlus4.get((int)(Math.random() * myPlus4.size()));
				System.out.println("Trying to play " + c.toString());
				playThisCard(game, c);
				return;
			}
			if (myPlus2.size() != 0) {
				Card c = myPlus2.get((int)(Math.random() * myPlus2.size()));
				System.out.println("Trying to play " + c.toString());
				playThisCard(game, c);
				return;
			}
			if (plus4RedDraw.size() != 0) {
				Card c = plus4RedDraw.get((int)(Math.random() * plus4RedDraw.size()));
				System.out.println("Trying to play " + c.toString());
				playThisCard(game, c);
				return;
			}
			if (skipReversePlus2.size() != 0) {
				Card c = skipReversePlus2.get((int)(Math.random() * skipReversePlus2.size()));
				System.out.println("Trying to play " + c.toString());
				playThisCard(game, c);
				return;
			}
		}
		
		if (nonSpecial.size() != 0) {
			Card c = nonSpecial.get((int)(Math.random() * nonSpecial.size()));
			System.out.println("Trying to play " + c.toString());
			playThisCard(game, c);
			return;
		}
		if (skipReversePlus2.size() != 0) {
			Card c = skipReversePlus2.get((int)(Math.random() * skipReversePlus2.size()));
			System.out.println("Trying to play " + c.toString());
			playThisCard(game, c);
			return;
		}
		
		if (myWildCards.size() != 0) {
			Card c = myWildCards.get((int)(Math.random() * myWildCards.size()));
			System.out.println("Trying to play " + c.toString());
			playThisCard(game, c);
			return;
		}
		
		if (plus4RedDraw.size() != 0) {
			Card c = plus4RedDraw.get((int)(Math.random() * plus4RedDraw.size()));
			System.out.println("Trying to play " + c.toString());
			playThisCard(game, c);
			return;
		}
		
		throw new NullPointerException("apparently didnt play a card wtf");
	}
	
	public int chooseSomeoneToSwapWith(ArrayList<Integer> nums) {
		ArrayList<Integer> bestIndices = new ArrayList<Integer>();
		int leastCards = Integer.MAX_VALUE;
		for (int i=0;i<nums.size();i++) {
			if (i == this.ID)
				continue;
			if (nums.get(i) < leastCards) {
				leastCards = nums.get(i);
				bestIndices.clear();
				bestIndices.add(i);
			} else if (nums.get(i) == leastCards) {
				bestIndices.add(i);
			}
		}
		return bestIndices.get((int)(Math.random() * bestIndices.size()));
	}
	
	public void takeTurn(Game g) throws Exception {
		UnoGame game = (UnoGame)(g);
		if (game.getTrueLastCard() == null)
			Util.sleep(2);
		
		int minCards = Integer.MAX_VALUE;
		for (int i=0;i<game.numPlayers();i++)
			minCards = Math.min(minCards, game.getHand(i).size());
		

		ArrayList<Card> hand = game.getHand(ID);
		
		if (minCards <= 3 && !(hand.size() <= 3)) {
			for (Card c : hand) {
				if (c.getType() == CardType.SOCIALISM) {
					playThisCard(game, c);
					return;
				}
			}	
		}
		
		ArrayList<Card> nonSpecial = new ArrayList<Card>();
		ArrayList<Card> skipReversePlus2 = new ArrayList<Card>();
		ArrayList<Card> plus4RedDraw = new ArrayList<Card>();
		ArrayList<Card> myPlus2 = new ArrayList<Card>();
		ArrayList<Card> myPlus4 = new ArrayList<Card>();
		ArrayList<Card> myWildCards = new ArrayList<Card>();
		for (Card c : hand) {
			if (game.isValidMove(c.toString())) {
				if (!c.isSpecial()) {
					//In future, account for 0 and 7. Don't play them if you have least cards
					nonSpecial.add(c);
				} else if (c.getType().toString().indexOf("WILD") == -1 &&
						   !UnoCard.typeWillNotAffectNext((UnoCard)c)) {
					//Skip, Reverse, +2
					//If next player has less cards than self, play these
					skipReversePlus2.add(c);
				} else if (c.getType() != CardType.WILD) {
					//If next player has the least cards, play these (+4, redistribute, draw rand)
					plus4RedDraw.add(c);
				}
				if (c.getType().toString().indexOf("PLUS2") != -1) {
					//+ 2
					myPlus2.add(c);
				}
				if (c.getType().toString().indexOf("DRAW4") != -1) {
					//+ 4
					myPlus4.add(c);
				} else if (c.getType().toString().indexOf("WILD") != -1) {
					//My wild cards
					myWildCards.add(c);
				}
			}
		}
		
		if (nonSpecial.size() + skipReversePlus2.size() + plus4RedDraw.size() + myPlus2.size() + 
			myWildCards.size() == 0) {
			drawACard(game);
			return;
		}
		
		int nextPlayerHandSize = game.getHand(game.getNextPlayerIndex()).size();
		
		if (nextPlayerHandSize >= hand.size()) {
			/**
			 * Play normal cards if you have
			 */
			if (nonSpecial.size() != 0) {
				Card c = nonSpecial.get((int)(Math.random() * nonSpecial.size()));
				System.out.println("Trying to play " + c.toString());
				playThisCard(game, c);
				return;
			}
		}
		if (nextPlayerHandSize != minCards) {
			/**
			 * +2/Reverse/Skip prefered
			 * Normal cards
			 * +4/Redistribute/Random draw
			 */
			if (skipReversePlus2.size() != 0) {
				Card c = skipReversePlus2.get((int)(Math.random() * skipReversePlus2.size()));
				System.out.println("Trying to play " + c.toString());
				playThisCard(game, c);
				return;
			}
			if (nonSpecial.size() != 0) {
				Card c = nonSpecial.get((int)(Math.random() * nonSpecial.size()));
				System.out.println("Trying to play " + c.toString());
				playThisCard(game, c);
				return;
			}
			if (plus4RedDraw.size() != 0) {
				Card c = plus4RedDraw.get((int)(Math.random() * plus4RedDraw.size()));
				System.out.println("Trying to play " + c.toString());
				playThisCard(game, c);
				return;
			}
		}
		if (nextPlayerHandSize == minCards) {
			/**
			 * +4
			 * +2
			 * +4/Redistribute/Draw random
			 * +2/Reverse/Skip
			 */
			if (myPlus4.size() != 0) {
				Card c = myPlus4.get((int)(Math.random() * myPlus4.size()));
				System.out.println("Trying to play " + c.toString());
				playThisCard(game, c);
				return;
			}
			if (myPlus2.size() != 0) {
				Card c = myPlus2.get((int)(Math.random() * myPlus2.size()));
				System.out.println("Trying to play " + c.toString());
				playThisCard(game, c);
				return;
			}
			if (plus4RedDraw.size() != 0) {
				Card c = plus4RedDraw.get((int)(Math.random() * plus4RedDraw.size()));
				System.out.println("Trying to play " + c.toString());
				playThisCard(game, c);
				return;
			}
			if (skipReversePlus2.size() != 0) {
				Card c = skipReversePlus2.get((int)(Math.random() * skipReversePlus2.size()));
				System.out.println("Trying to play " + c.toString());
				playThisCard(game, c);
				return;
			}
		}
		
		if (nonSpecial.size() != 0) {
			Card c = nonSpecial.get((int)(Math.random() * nonSpecial.size()));
			System.out.println("Trying to play " + c.toString());
			playThisCard(game, c);
			return;
		}
		if (skipReversePlus2.size() != 0) {
			Card c = skipReversePlus2.get((int)(Math.random() * skipReversePlus2.size()));
			System.out.println("Trying to play " + c.toString());
			playThisCard(game, c);
			return;
		}
		
		if (myWildCards.size() != 0) {
			Card c = myWildCards.get((int)(Math.random() * myWildCards.size()));
			System.out.println("Trying to play " + c.toString());
			playThisCard(game, c);
			return;
		}
		
		if (plus4RedDraw.size() != 0) {
			Card c = plus4RedDraw.get((int)(Math.random() * plus4RedDraw.size()));
			System.out.println("Trying to play " + c.toString());
			playThisCard(game, c);
			return;
		}
		
		throw new NullPointerException("apparently didnt play a card wtf");
	}
	
	private void drawACard(UnoGame game) throws Exception {
		UnoCard card = (UnoCard)(game.takeTurnBot(""));
		if (card == null)
			return;
		else {
			//determine if it should play this card, right now it'll just play it
			playThisCard(game, card);
		}
	}
	
	private void playThisCard(UnoGame game, Card c) throws Exception {
		if (c.getType() != CardType.WILD && c.getType() != CardType.WILD_DRAW4) {
			//Regular card
			game.takeTurnBot(c);
		} else {
			String[] colors = new String[] {"RED_","BLUE_","YELLOW_","GREEN_"};
			int numReds = 0;
			int numBlues = 0;
			int numYellows = 0;
			int numGreens = 0;
			
			ArrayList<Card> myCards = game.getHand(ID);
			for (Card cd : myCards) {
				if (cd.getType().toString().indexOf("RED") != -1)
					numReds++;
				else if (cd.getType().toString().indexOf("BLUE")!= -1)
					numBlues++;
				else if (cd.getType().toString().indexOf("YELLOW") != -1)
					numYellows++;
				else if (cd.getType().toString().indexOf("GREEN") != -1)
					numGreens++;
			}
			int max = Math.max(numReds, Math.max(numBlues, Math.max(numYellows, numGreens)));
			ArrayList<Integer> maxIndexes = new ArrayList<Integer>();
			if (numReds == max)
				maxIndexes.add(0);
			if (numBlues == max)
				maxIndexes.add(1);
			if (numYellows == max)
				maxIndexes.add(2);
			if (numGreens == max)
				maxIndexes.add(3);
			
			c.setType(CardType.valueOf(colors[ maxIndexes.get((int)(Math.random() * maxIndexes.size())) ] + c.getType().toString()));
			game.takeTurnBot(c);
		}
	}
}
