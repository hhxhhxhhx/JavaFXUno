package classes;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import javax.imageio.ImageIO;

import absClasses.Card;
import enums.CardType;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

/**
 * UnoCard is a Class that contains the Card object. 
 * It will create a GUI representation of the card as well. 
 * 
 * @author Roger
 *
 */
@SuppressWarnings("rawtypes")
public class UnoCard extends Card implements Comparable {
	
	private BufferedImage cardImages;
	
	private String message;
	
	public UnoCard(boolean b, int val, CardType ct) {
		try {
			cardImages = ImageIO.read(new File("sprites/cards.png"));
		} catch (Exception e) {
			cardImages = null;
		}
		this.special = b;
		this.value = val;
		this.type = ct;
		
		initPaneStuff();
	}
	
	public UnoCard(String[] args) {
		try {
			cardImages = ImageIO.read(new File("sprites/cards.png"));
		} catch (Exception e) {
			cardImages = null;
		}
		boolean b = Boolean.parseBoolean(args[0]);
		int val = Integer.parseInt(args[1]);
		CardType ct = CardType.valueOf(args[2]);
		this.special = b;
		this.value = val;
		this.type = ct;
		
		initPaneStuff();
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String str) {
		message = str;
	}
	
	private void initPaneStuff() {
		
		this.pane = new StackPane();
		
		CardType ct = this.type;
		BufferedImage image = null;
		if (!this.special) {
			int newPosX = this.value - 1;
			if (newPosX == -1)
				newPosX = 9;
			image = cardImages.getSubimage((int)(85.5*newPosX), (int)Math.round(133.666*typeToInt(ct)), 85, 134);
		} else if (ct == CardType.WILD || ct == CardType.WILD_DRAW4) {
			int additional = (ct.toString().indexOf("DRAW4") == -1) ? 0 : 171;
			image = cardImages.getSubimage(171 + additional, 668, 85, 134);
		} else if (ct.toString().indexOf("WILD") != -1) {
			int is_plus_4 = (ct.toString().indexOf("DRAW4") == -1) ? 0 : 171;
			int color_bonus = 0;
			if (ct.toString().indexOf("YELLOW") != -1 || ct.toString().indexOf("GREEN") != -1)
				color_bonus = 85;
			try {
				if (ct.toString().indexOf("RED") != -1 || ct.toString().indexOf("YELLOW") != -1)
					cardImages = ImageIO.read(new File("sprites/redyellow.png"));
				else
					cardImages = ImageIO.read(new File("sprites/bluegreen.png"));
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error at reading redyellow / bluegreen file");
			}
			image = cardImages.getSubimage(is_plus_4 + color_bonus, 0, 85, 134);
		} else if (ct.toString().indexOf("SKIP") != -1) { 
			image = cardImages.getSubimage((int)(typeToInt(ct) * (171 + 85.5)), 535, 85, 134);
		} else if (ct.toString().indexOf("REVERSE") != -1 && ct.toString().indexOf("BLUE") == -1) { 
			image = cardImages.getSubimage((int)(85.5 + typeToInt(ct) * (171 + 85.5)), 535, 85, 134);
		} else if (ct.toString().indexOf("PLUS2") != -1 && ct.toString().indexOf("BLUE") == -1) { 
			image = cardImages.getSubimage((int)(171 + typeToInt(ct) * (171 + 85.5)), 535, 85, 134);
		} else if (ct.toString().indexOf("REVERSE") != -1 || ct.toString().indexOf("PLUS2") != -1) {
			int additional = 0;
			if (ct == CardType.BLUE_PLUS2)
				additional = 1;
			image = cardImages.getSubimage((int)(additional * 85.5), 668, 85, 134);
		} else if (ct == CardType.SOCIALISM) {
			try {
				cardImages = ImageIO.read(new File("sprites/redistribute.png"));
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error at reading redistribute.png");
			}
			image = cardImages.getSubimage(0, 0, 85, 134);
		} else if (ct == CardType.ZERO_ROTATE) {
			try {
				cardImages = ImageIO.read(new File("sprites/rotate.png"));
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error at reading rotate.png");
			}
			image = cardImages.getSubimage(0, 0, 85, 134);
		} else if (ct == CardType.DRAW_RANDOM_AMOUNT) {
			try {
				cardImages = ImageIO.read(new File("sprites/drawrandom.png"));
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error at reading drawrandom.png");
			}
			image = cardImages.getSubimage(0, 0, 85, 134);
		}
		ImageView imv = new ImageView(SwingFXUtils.toFXImage(image, null));
		this.pane.getChildren().add(imv);
		this.pane.setMinSize(85, 134);
		this.pane.setMaxSize(86, 134);
	}
	
	public final Pane getCopyOfPane(int...size) {
		StackPane newcardPane = (StackPane)((new UnoCard(this.special, this.value, this.type)).pane);
		if (size.length == 0)
			return newcardPane;
		
		newcardPane.setMinSize(size[0] * 85, size[1] * 134);
		newcardPane.setMaxSize(size[0] * 85, size[1] * 134);
		((ImageView)(newcardPane.getChildren().get(0))).setFitWidth(newcardPane.getMinWidth());
		((ImageView)(newcardPane.getChildren().get(0))).setFitHeight(newcardPane.getMinHeight());
		return newcardPane;
	}
	
	private final int typeToInt(CardType ct) {
		if (ct.toString().indexOf("RED") != -1)
			return 0;
		else if (ct.toString().indexOf("YELLOW") != -1)
			return 1;
		else if (ct.toString().indexOf("GREEN") != -1)
			return 2;
		else
			return 3;
	}
	
	public final static String handToString(ArrayList<Card> cards) {
		String msg = "";
		ArrayList<UnoCard> uc = new ArrayList<UnoCard>();
		for (Card c : cards)
			uc.add((UnoCard)(c));
		for (UnoCard cd : uc)
			msg += cd.toString() + "\n";
		return msg;
	}
	
	public final static boolean typeWillNotAffectNext(UnoCard c) {
		if (c == null)
			return false;
		return c.type == CardType.DRAW_RANDOM_AMOUNT || c.type == CardType.SOCIALISM;
	}
	
	@Override
	public boolean isCompatibleWith(Card other) {
		
		if (this.type == CardType.WILD || this.type == CardType.WILD_DRAW4 || this.type == CardType.SOCIALISM || this.type == CardType.DRAW_RANDOM_AMOUNT)
			return true;
		if (other == null)
			return true;
		
		UnoCard c = (UnoCard)(other);
		//This following line should allow red cards to be played after a wild red card. 
		if (c.type.toString().indexOf(this.type.toString()) != -1 || this.type.toString().indexOf(c.type.toString()) != -1) 
			return true;
		if (!c.special && !this.special)
			return c.value == this.value;
		
		if (bothCardTypesHaveThis(c, "RED"))
			return true;
		if (bothCardTypesHaveThis(c, "BLUE"))
			return true;
		if (bothCardTypesHaveThis(c, "YELLOW"))
			return true;
		if (bothCardTypesHaveThis(c, "GREEN"))
			return true;
		
		if (bothCardTypesHaveThis(c, "PLUS2"))
			return true;
		if (bothCardTypesHaveThis(c, "SKIP"))
			return true;
		if (bothCardTypesHaveThis(c, "REVERSE"))
			return true;

		return false;
	}
	
	private boolean bothCardTypesHaveThis(UnoCard c, String str) {
		return c.type.toString().indexOf(str) != -1 && this.type.toString().indexOf(str) != -1;
	}

	public static ArrayList<UnoCard> eval(String message) {
		
		if (message == null) 
			return null;
		
		if (message.equals("All Cards"))
			return getAllCards();
		
		if (message.contentEquals("Regular Cards"))
			return getRegularCards();
		
		ArrayList<UnoCard> cards = new ArrayList<UnoCard>();
		String[] parsed = message.split("\n");
		for (String s : parsed) {
			try {
				if (s.equals("null"))
					cards.add(null);
				else if (s.equals("STOP EVAL"))
					return cards;
				else
					cards.add(new UnoCard(s.split(",")));
			} catch (Exception ef) {
				return null;
			}
		}
		return cards;
	}
	
	@SuppressWarnings("unchecked")
	public static void sort(ArrayList<UnoCard> cards) {
		Collections.sort(cards);
	}
	
	public static void sortOther(ArrayList<Card> cards) {
		ArrayList<UnoCard> cds = new ArrayList<UnoCard>();
		for (Card c : cards)
			cds.add((UnoCard)c);
		sort(cds);
		cards.clear();
		for (UnoCard c : cds)
			cards.add(c);
		
	}
	
	private static ArrayList<UnoCard> getAllCards() {
		ArrayList<UnoCard> cards = getRegularCards();
		
		//Add some custom cards here if u want
		
		for (int i=0;i<2;i++) {
			
			cards.add(new UnoCard(true, -1, CardType.WILD));
			cards.add(new UnoCard(true, -1, CardType.WILD));
			cards.add(new UnoCard(true, -1, CardType.WILD_DRAW4));
			cards.add(new UnoCard(true, -1, CardType.WILD_DRAW4));
			
			cards.add(new UnoCard(true, -1, CardType.RED_PLUS2));
			cards.add(new UnoCard(true, -1, CardType.YELLOW_PLUS2));
			cards.add(new UnoCard(true, -1, CardType.BLUE_PLUS2));
			cards.add(new UnoCard(true, -1, CardType.GREEN_PLUS2));
			
			cards.add(new UnoCard(true, -1, CardType.RED_SKIP));
			cards.add(new UnoCard(true, -1, CardType.YELLOW_SKIP));
			cards.add(new UnoCard(true, -1, CardType.BLUE_SKIP));
			cards.add(new UnoCard(true, -1, CardType.GREEN_SKIP));
			
			cards.add(new UnoCard(true, -1, CardType.RED_REVERSE));
			cards.add(new UnoCard(true, -1, CardType.YELLOW_REVERSE));
			cards.add(new UnoCard(true, -1, CardType.BLUE_REVERSE));
			cards.add(new UnoCard(true, -1, CardType.GREEN_REVERSE));
			
		}
		Collections.shuffle(cards);
		
		return cards;
	}
	
	private static ArrayList<UnoCard> getRegularCards() {
		ArrayList<UnoCard> cards = new ArrayList<UnoCard>();
		
		cards.add(new UnoCard(false, 0, CardType.BLUE));
		cards.add(new UnoCard(false, 0, CardType.RED));
		cards.add(new UnoCard(false, 0, CardType.YELLOW));
		cards.add(new UnoCard(false, 0, CardType.GREEN));
		for (int i=0;i<2;i++) {
			for (int j=1;j<10;j++) {
				cards.add(new UnoCard(false, j, CardType.BLUE));
				cards.add(new UnoCard(false, j, CardType.RED));
				cards.add(new UnoCard(false, j, CardType.YELLOW));
				cards.add(new UnoCard(false, j, CardType.GREEN));
			}
		}
		
		Collections.shuffle(cards);
		return cards;
	}
	
	/**
	 * Red = 1000 + x
	 * Yellow = 2000 + x
	 * Blue = 3000 + x
	 * Green = 4000 + x
	 * Wild = 5000
	 * Wild+4 = 6000
	 * Redistribute = 100000
	 * 
	 * Skip = 11
	 * Reverse = 12
	 * +2 = 13
	 */
	@Override
	protected int getSortValue() {
		String tp = this.type.toString();
		
		if (tp.equals("WILD")) 
			return 5000;
		else if (tp.equals("WILD_DRAW4")) 
			return 6000;
		else if (tp.equals("SOCIALISM"))
			return 100000;
		else if (tp.equals("DRAW_RANDOM_AMOUNT"))
			return 10000;
		int val = 0;
		
		if (tp.indexOf("RED") != -1) 
			val += 1000;
		else if (tp.indexOf("YELLOW") != -1) 
			val += 2000;
		else if (tp.indexOf("BLUE") != -1) 
			val += 3000;
		else if (tp.indexOf("GREEN") != -1) 
			val += 4000;
		
		if (tp.indexOf("SKIP") != -1) 
			return val + 11;
		else if (tp.indexOf("REVERSE") != -1) 
			return val + 12;
		else if (tp.indexOf("PLUS2") != -1) 
			return val + 13;
		else 
			return val + this.value;
	}

	@Override
	public String toString() {
		return (this.special + "," + this.value + "," + this.type); 
	}

	@Override
	public int compareTo(Object o) {
		UnoCard other = (UnoCard)(o);
		return this.getSortValue() - other.getSortValue();
	}

	public static Pane getRotateCardOverlay() {
		StackPane newcardPane = (StackPane)((new UnoCard(true, -1, CardType.ZERO_ROTATE)).pane);
		newcardPane.setMinSize(170, 268);
		newcardPane.setMaxSize(170, 268);
		((ImageView)(newcardPane.getChildren().get(0))).setFitWidth(newcardPane.getMinWidth());
		((ImageView)(newcardPane.getChildren().get(0))).setFitHeight(newcardPane.getMinHeight());
		return newcardPane;
	}
	

	
	public static final UnoCard SOCIALISM = new UnoCard(true, -1, CardType.SOCIALISM);
	public static final UnoCard DRAWRANDOM = new UnoCard(true, -1, CardType.DRAW_RANDOM_AMOUNT);
}
