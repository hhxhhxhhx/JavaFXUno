package classes;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import absClasses.Client;
import absClasses.GameGUI;
import enums.CardType;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * GUI object created by Client. Not a graphical version of the UnoGame class. 
 * 		This is created and updated by messages sent from the server. 
 * Contains everything needed to create the full GUI for each client. 
 * @author Roger
 *
 */
public class UnoGameGUI extends GameGUI {
	
	private boolean drawIffNeeded = true;
	
	private StackPane lastCardPane = new StackPane();
	private Pane handPane = new Pane();
	
	private ArrayList<Pane> eachPlayerNumCards = new ArrayList<Pane>();
	private ArrayList<UnoCard> cards = new ArrayList<UnoCard>();
	private int displaySet = 0;
	private Button nextPage = new Button("Next Page");
	private Button prevPage = new Button("Previous Page");
	
	private HBox selfNameAndCards = new HBox();
	private StackPane drawCard = new StackPane();	
	private ImageView drawCardIV;
	
	private ArrayList<Line> regDirectionArrows = new ArrayList<Line>();
	private ArrayList<Line> reverseDirectionArrows = new ArrayList<Line>();
	private ArrayList<Line> permanentLines = new ArrayList<Line>();

	private VBox chooseACard = new VBox();

	private ImageCursor notYourTurn = new ImageCursor(new Image("sprites/nono.png"));
	private boolean disabledCursor = false;
	
	private UnoCard lastCard = null;
	
	private Circle movingCardBack = new Circle();

	private final double criticalAngleTopRight = Math.atan(325.0/450) * 180 / Math.PI;
	private final double criticalAngleTopLeft = 180 - criticalAngleTopRight;
	private final double criticalAngleBottomLeft = 180 + criticalAngleTopRight;
	
	public UnoGameGUI(Client c, int width, int height) {
		super(c, width, height);
		
		Util.setXYAndSize(lastCardPane, 375, 225, 150, 150);
		lastCardPane.setAlignment(Pos.CENTER);
		
		Util.setXYAndSize(handPane, 150, 450, 600, 134);
		
		Util.setXYAndSize(prevPage, 50, 600, 150);
		
		Util.setXYAndSize(nextPage, 700, 600, 150);
		
		Util.setXYAndSize(selfNameAndCards, 300, 600, 300);
		
		Util.setXYAndSize(drawCard, 175, 225, 150, 150);
		
		nextPage.setOnAction(e -> {
			displaySet += 1;
			updateHand();
		});
		prevPage.setOnAction(e -> {
			displaySet -= 1;
			updateHand();
		});
		
		BufferedImage cardImages;
		try {
			cardImages = ImageIO.read(new File("sprites/draw.png"));
		} catch (Exception e) {
			cardImages = null;
		}
		drawCardIV = new ImageView(SwingFXUtils.toFXImage(cardImages, null));
		drawCardIV.setOnMouseClicked(e -> {
			this.client.sendMessageToServer("DRAW");
		});
		drawCard.getChildren().add(drawCardIV);
		
		BufferedImage cardBack;
		try {
			cardBack = ImageIO.read(new File("sprites/cardback.png"));
		} catch (Exception e) {
			cardBack = null;
		}
		movingCardBack.setFill(new ImagePattern(SwingFXUtils.toFXImage(cardBack, null)));
		movingCardBack.setCenterX(-100);
		movingCardBack.setCenterY(-100);
		movingCardBack.setRadius(Math.sqrt(6338));
		
		permanentLines.add(new Line(25, 450, 25, 550));
		permanentLines.add(new Line(25, 550, 125, 550));
		permanentLines.add(new Line(775, 550, 875, 550));
		permanentLines.add(new Line(875, 550, 875, 450));
		for (Line l : permanentLines) {
			l.setStrokeWidth(3);
		}
		
		regDirectionArrows.add(new Line(120, 545, 125, 550));
		regDirectionArrows.add(new Line(120, 555, 125, 550));
		regDirectionArrows.add(new Line(870, 455, 875, 450));
		regDirectionArrows.add(new Line(880, 455, 875, 450));
		for (Line l : regDirectionArrows) {
			l.setStrokeWidth(5);
		}
		
		reverseDirectionArrows.add(new Line(20, 455, 25, 450));
		reverseDirectionArrows.add(new Line(30, 455, 25, 450));
		reverseDirectionArrows.add(new Line(780, 545, 775, 550));
		reverseDirectionArrows.add(new Line(780, 555, 775, 550));
		for (Line l : reverseDirectionArrows) {
			l.setStrokeWidth(5);
			l.setVisible(false);
		}
		
		this.pane.getChildren().addAll(lastCardPane, handPane, nextPage, prevPage, selfNameAndCards);
		this.pane.getChildren().addAll(drawCard, movingCardBack);
		this.pane.getChildren().addAll(permanentLines);
		this.pane.getChildren().addAll(regDirectionArrows);
		this.pane.getChildren().addAll(reverseDirectionArrows);
	}
	
	@Override
	public void setTitleOfStage() {
		this.stage.setTitle("Uno " + UnoGame.VERSION + ". Player " + client.getID() + ", " + client.getName());
	}

	@Override
	public void updateCursor(String command) {
		if (command.equals("DISABLE")) {
			disabledCursor = true;
			scene.setCursor(notYourTurn);
		} else {
			disabledCursor = false;
			scene.setCursor(Cursor.DEFAULT);
		}
		drawCard.getChildren().clear();
		drawCard.getChildren().add(drawCardIV);
		updateHand("from cursor");
	}
	
	public void setDrawIffNeeded(String str) {
		drawIffNeeded = Boolean.parseBoolean(str);
	}
	
	public void drew_this_card(String repr) {
		Stage popup = new Stage();
		VBox vb = new VBox();
		vb.setAlignment(Pos.CENTER);
		vb.setStyle("-fx-spacing: 40px;");
		Label lb1 = new Label("You drew this card");
		lb1.setStyle("-fx-font-size: 16px;");
		UnoCard c = UnoCard.eval(repr).get(0);
		Pane cardpane = c.getCopyOfPane();
		Label lb2 = new Label("Would you like to play it?");
		lb2.setStyle("-fx-font-size: 16px;");
		
		Pane yesNoPane = new Pane();
		yesNoPane.setMinWidth(400);
		Button yes = new Button("Yes");
		yes.setOnAction(e -> {
			if (c.getType().toString().indexOf("WILD") != -1) {
				drew_this_card_and_play_wild(repr, popup);
			} else {
				this.client.sendMessageToServer(c.toString());
				popup.close();
			}
		});
		yes.setLayoutX(75);
		yes.setMinWidth(100);
		yes.setStyle("-fx-font-size: 16px;");
		Button no = new Button("No");
		no.setOnAction(e -> {
			drawCardIV.setOnMouseClicked(ef -> {});
			this.client.sendMessageToServer("NO");
			popup.close();
		});
		no.setLayoutX(225);
		no.setMinWidth(100);
		no.setStyle("-fx-font-size: 16px;");
		popup.setOnCloseRequest(e -> {
			drawCardIV.setOnMouseClicked(ef -> {});
			this.client.sendMessageToServer("NO");
		});
		yesNoPane.getChildren().addAll(yes, no);
		vb.getChildren().addAll(lb1, cardpane, lb2, yesNoPane);
		Scene sce = new Scene(vb, 400, 400);
		popup.setScene(sce);
		popup.initModality(Modality.APPLICATION_MODAL);
		popup.setAlwaysOnTop(true);
		popup.show();
	}
	
	private void drew_this_card_and_play_wild(String repr, Stage other) {
		
		other.close();
		
		Stage popup = new Stage();
		
		Pane colorSelect = new Pane();
		Scene sce = new Scene(colorSelect, 200, 200);
		colorSelect.setMinSize(200, 200);
		colorSelect.setMaxSize(200, 200);
		
		Pane red = new Pane();
		red.setStyle("-fx-background-color: rgb(238,22,31);");
		Util.setXYAndSize(red, 25, 25, 75, 75);
		Util.setOnHover(red, sce);
		red.setOnMouseClicked(f -> {
			popup.close();
			other.close();
			this.client.sendMessageToServer(repr+",RED");
		});
		
		Pane blue = new Pane();
		blue.setStyle("-fx-background-color: rgb(0,150,219);");
		Util.setXYAndSize(blue, 100, 25, 75, 75);
		Util.setOnHover(blue, sce);
		blue.setOnMouseClicked(f -> {
			popup.close();
			other.close();
			this.client.sendMessageToServer(repr+",BLUE");
		});
		
		Pane green = new Pane();
		green.setStyle("-fx-background-color: rgb(0,168,80);");
		Util.setXYAndSize(green, 25, 100, 75, 75);
		Util.setOnHover(green, sce);
		green.setOnMouseClicked(f -> {
			popup.close();
			other.close();
			this.client.sendMessageToServer(repr+",GREEN");
		});
		
		Pane yellow = new Pane();
		yellow.setStyle("-fx-background-color: rgb(255,223,0);");
		Util.setXYAndSize(yellow, 100, 100, 75, 75);
		Util.setOnHover(yellow, sce);
		yellow.setOnMouseClicked(f -> {
			popup.close();
			other.close();
			this.client.sendMessageToServer(repr+",YELLOW");
		});
		
		colorSelect.getChildren().addAll(blue, red, green, yellow);
		
		Label direction;
		if (repr.indexOf("DRAW4") == -1)
			direction = new Label("Wild Card Color Selection");
		else
			direction = new Label("Wild +4 Color Selection");
		direction.setLayoutX(0);
		direction.setLayoutY(8);
		direction.setMinSize(200, 0);
		direction.setAlignment(Pos.CENTER);
		colorSelect.getChildren().add(direction);
		
		popup.setScene(sce);
		popup.initModality(Modality.APPLICATION_MODAL);
		popup.setAlwaysOnTop(true);
		popup.setOnCloseRequest(e -> {
			other.show();
		});
		popup.show();
	}
	
	public void stackableCard(String repr) {
		Stage popup = new Stage();
		ArrayList<UnoCard> cards = UnoCard.eval(repr);
		
		Pane newPane = new Pane();
		
		Label lb = new Label("Choose a card to stack");
		lb.setMinWidth(Math.max(100 + 60 + cards.size() * 25, 400));
		lb.setAlignment(Pos.CENTER);
		lb.setLayoutX(0);
		lb.setLayoutY(50);
		lb.setStyle("-fx-font-size: 16px;");
		newPane.getChildren().add(lb);
		
		Button no = new Button("No thanks");
		no.setOnAction(e -> {
			drawCardIV.setOnMouseClicked(ef -> {});
			this.client.sendMessageToServer("NO STACK");
			popup.close();
		});
		popup.setOnCloseRequest(e -> {
			drawCardIV.setOnMouseClicked(ef -> {});
			this.client.sendMessageToServer("NO STACK");
		});
		
		no.setLayoutX(lb.getMinWidth()/2 - 100);
		no.setLayoutY(300);
		no.setMinWidth(200);
		no.setMinHeight(50);
		no.setStyle("-fx-font-size: 16px;");
		newPane.getChildren().add(no);

		Scene sce = new Scene(newPane, Math.max(100 + 60 + cards.size() * 25, 400), 400);
		
		int maxCardsInHand = (int)Math.floor((lb.getMinWidth() - 60) / 25);
		final double dist_from_left = (lb.getMinWidth() - (60 + 25 * (Math.min(maxCardsInHand, cards.size())))) / 2.0;
		for (int i=0;i<cards.size();i++) {
			final int k = i;
			Pane p = cards.get(i).getCopyOfPane();
			p.setLayoutX(dist_from_left + 25 * i);
			p.setLayoutY(100);
			p.setOnMouseEntered(e -> {
				sce.setCursor(Cursor.HAND);
			});
			p.setOnMouseExited(e -> {
				sce.setCursor(Cursor.DEFAULT);
			});
			p.setOnMouseClicked(e -> {
				if (cards.get(k).getType().toString().indexOf("WILD") == -1)
					this.client.sendMessageToServer(cards.get(k).toString());
				else {
					stackableCardWild(cards.get(k).toString(), popup);
				}
				popup.close();
			});
			newPane.getChildren().add(p);
		}
		popup.setScene(sce);
		popup.initModality(Modality.APPLICATION_MODAL);
		popup.setAlwaysOnTop(true);
		popup.show();
	}
	
	
	private void stackableCardWild(String repr, Stage other) {
		
		other.close();
		
		Stage popup = new Stage();
		
		Pane colorSelect = new Pane();
		Scene sce = new Scene(colorSelect, 200, 200);
		colorSelect.setMinSize(200, 200);
		colorSelect.setMaxSize(200, 200);
		
		Pane red = new Pane();
		red.setStyle("-fx-background-color: rgb(238,22,31);");
		Util.setXYAndSize(red, 25, 25, 75, 75);
		Util.setOnHover(red, sce);
		red.setOnMouseClicked(f -> {
			popup.close();
			other.close();
			this.client.sendMessageToServer(repr+",RED");
		});
		
		Pane blue = new Pane();
		blue.setStyle("-fx-background-color: rgb(0,150,219);");
		Util.setXYAndSize(blue, 100, 25, 75, 75);
		Util.setOnHover(blue, sce);
		blue.setOnMouseClicked(f -> {
			popup.close();
			other.close();
			this.client.sendMessageToServer(repr+",BLUE");
		});
		
		Pane green = new Pane();
		green.setStyle("-fx-background-color: rgb(0,168,80);");
		Util.setXYAndSize(green, 25, 100, 75, 75);
		Util.setOnHover(green, sce);
		green.setOnMouseClicked(f -> {
			popup.close();
			other.close();
			this.client.sendMessageToServer(repr+",GREEN");
		});
		
		Pane yellow = new Pane();
		yellow.setStyle("-fx-background-color: rgb(255,223,0);");
		Util.setXYAndSize(yellow, 100, 100, 75, 75);
		Util.setOnHover(yellow, sce);
		yellow.setOnMouseClicked(f -> {
			popup.close();
			other.close();
			this.client.sendMessageToServer(repr+",YELLOW");
		});
		
		colorSelect.getChildren().addAll(blue, red, green, yellow);
		
		Label direction;
		if (repr.indexOf("DRAW4") == -1)
			direction = new Label("Wild Card Color Selection");
		else
			direction = new Label("Wild +4 Color Selection");
		direction.setLayoutX(0);
		direction.setLayoutY(8);
		direction.setMinSize(200, 0);
		direction.setAlignment(Pos.CENTER);
		colorSelect.getChildren().add(direction);
		
		popup.setScene(sce);
		popup.initModality(Modality.APPLICATION_MODAL);
		popup.setAlwaysOnTop(true);
		popup.setOnCloseRequest(e -> {
			other.show();
		});
		popup.show();
	}
	
	public void selectSomeoneToSwap(String command) {
		
		Stage popup = new Stage();
		String[] parsed = command.split("\n");
		Pane newPane = new Pane();
		
		newPane.setMinSize(scene.getWidth(), scene.getHeight());
		newPane.setMaxSize(scene.getWidth(), scene.getHeight());
		
		Scene sce = new Scene(newPane, scene.getWidth(), scene.getHeight());
		
		for (int i=0; i<totalPlayers; i++) {
			if (i == this.client.getID())
				continue;
			final int j = i;
			double angle = getAngle(i);
			double endxbar = -1, endybar = -1;
			if (angle < criticalAngleTopRight) {
				endxbar = this.scene.getWidth();
				endybar = (this.scene.getHeight() / 2) - (this.scene.getWidth() / 2) * Math.tan(angle * Math.PI / 180);
			} else if (angle < criticalAngleTopLeft) {
				endybar = 0;
				if (angle == 90) endxbar = this.scene.getWidth() / 2;
				else endxbar = (this.scene.getHeight() / 2) / Math.tan(angle * Math.PI / 180) + (this.scene.getWidth() / 2);
			} else if (angle < criticalAngleBottomLeft) {
				endxbar = 0;
				endybar = (this.scene.getHeight() / 2) - (this.scene.getWidth() / 2) * Math.tan((180 - angle) * Math.PI / 180);
			} 
			Pane newPanee = new TotalCardPane(parsed[i].substring(0, parsed[i].indexOf(",")), 
													  parsed[i].substring(parsed[i].indexOf(",") + 1), 
													  endxbar, endybar, false).getPane();
			newPanee.setOnMouseEntered(e -> {
				sce.setCursor(Cursor.HAND);
				newPanee.setStyle(newPanee.getId() + "-fx-background-color: lightskyblue;");
			});
			newPanee.setOnMouseExited(e -> {
				sce.setCursor(Cursor.DEFAULT);
				newPanee.setStyle(newPanee.getId());
			});
			newPanee.setOnMouseClicked(e -> {
				this.client.sendMessageToServer("" + j);
				popup.close();
			});
			newPane.getChildren().add(newPanee);
		}
		
		HBox selfName = new HBox();
		selfName.setLayoutX(selfNameAndCards.getLayoutX());
		selfName.setLayoutY(selfNameAndCards.getLayoutY());
		Label selfname = new Label(this.client.getName());
		selfname.setStyle("-fx-font-size: 20px;");
		Label selfnum = new Label("" + cards.size());
		selfnum.setStyle("-fx-font-size: 20px;");
		selfName.getChildren().addAll(selfname, selfnum);
		selfName.setStyle("-fx-spacing: 25px;-fx-border-width: 2px;-fx-border-color: black;");
		selfName.setAlignment(Pos.CENTER);
		selfName.setMinSize(selfNameAndCards.getMinWidth(), selfNameAndCards.getMinHeight());
		newPane.getChildren().add(selfName);
		
		Label lb = new Label("Choose a player to\nswap hands with.");
		lb.setMinSize(200, 100);
		lb.setAlignment(Pos.CENTER);
		lb.setLayoutX(scene.getWidth() / 2 - 100);
		lb.setLayoutY(scene.getHeight() / 2 - 50);
		lb.setStyle("-fx-font-size: 20px;");
		newPane.getChildren().add(lb);
		
		popup.setScene(sce);
		popup.initModality(Modality.APPLICATION_MODAL);
		popup.setAlwaysOnTop(true);
		
		popup.setOnCloseRequest(e -> {
			e.consume();
		});
		popup.show();
	}
	
	public void animateCardDraw(String command) {
		/**
		 * Bad idea since people can technically see what others drew, but i can't care enough atm
		 * ID of player
		 * Card
		 */
		String[] parsed = command.split("\n");
		int id = Integer.parseInt(parsed[0]);
		double angle = getAngle(id);
		
		if (id == this.client.getID()) {
			animateCardDrawShowCard(parsed[1]);
		} else {
			animateCardDrawShowBack(angle);
		}
	}
	
	private void animateCardDrawShowCard(String card) {
		UnoCard c = UnoCard.eval(card).get(0);
		Pane p = c.getCopyOfPane();
		final double startxbar = drawCard.getLayoutX() + drawCard.getMinWidth() / 2 - p.getMinWidth()/2;
		final double startybar = drawCard.getLayoutY() + drawCard.getMinHeight() / 2 - p.getMinHeight()/2;
		final double endxbar = (this.scene.getWidth() / 2) - p.getMinWidth()/2;
		final double endybar = (handPane.getLayoutY() + handPane.getMinHeight() / 2) - p.getMinHeight()/2;
		p.setLayoutX(startxbar);
		p.setLayoutY(startybar);
		p.setOpacity(handPane.getOpacity());
		this.pane.getChildren().add(p);
		Timeline tl = new Timeline(new KeyFrame(Duration.millis(25), ae -> {
			p.setLayoutX(p.getLayoutX() + (endxbar - startxbar)/30);
			p.setLayoutY(p.getLayoutY() + (endybar - startybar)/30);
			p.setOpacity(handPane.getOpacity());
		}));
		tl.setCycleCount(30);
		tl.setOnFinished(ef -> {
			p.setLayoutX(-500);
			p.setLayoutY(-500);
			this.pane.getChildren().remove(p);
		});
		tl.play();
	}
	
	private void animateCardDrawShowBack(double angle) {
		double startxbar = drawCard.getLayoutX() + drawCard.getMinWidth() / 2;
		double startybar = drawCard.getLayoutY() + drawCard.getMinHeight() / 2;
		double endxbar = -1, endybar = -1;
		if (angle < criticalAngleTopRight) {
			endxbar = this.scene.getWidth();
			endybar = (this.scene.getHeight() / 2) - (this.scene.getWidth() / 2) * Math.tan(angle * Math.PI / 180);
		} else if (angle < criticalAngleTopLeft) {
			endybar = 0;
			if (angle == 90) endxbar = this.scene.getWidth() / 2;
			else endxbar = (this.scene.getHeight() / 2) / Math.tan(angle * Math.PI / 180) + (this.scene.getWidth() / 2);
		} else if (angle < criticalAngleBottomLeft) {
			endxbar = 0;
			endybar = (this.scene.getHeight() / 2) - (this.scene.getWidth() / 2) * Math.tan((180 - angle) * Math.PI / 180);
		}
		
		movingCardBack.setCenterX(startxbar);
		movingCardBack.setCenterY(startybar);
		movingCardBack.setRotate(90 - angle);
		
		if (angle > 90 && angle < criticalAngleTopLeft) {
			movingCardBack.setRotate(180 + angle);
		}
		
		final double xposstart = startxbar;
		final double yposstart = startybar;
		final double xposend = endxbar;
		final double yposend = endybar;
		Timeline tl = new Timeline(new KeyFrame(Duration.millis(25), ae -> {
			movingCardBack.setCenterX(movingCardBack.getCenterX() + (xposend - xposstart)/20);
			movingCardBack.setCenterY(movingCardBack.getCenterY() + (yposend - yposstart)/20);
		}));
		tl.setCycleCount(20);
		tl.setOnFinished(ef -> {
			movingCardBack.setCenterX(-100);
			movingCardBack.setCenterY(-100);
		});
		tl.play();
	}
	
	public void animateCardPlay(String command) {
		/**
		 * ID of player
		 * Card
		 */
		String[] parsed = command.split("\n");
		int id = Integer.parseInt(parsed[0]);
		double angle = getAngle(id);
		UnoCard card = UnoCard.eval(parsed[1]).get(0);
		Pane p = card.getCopyOfPane();
		
		double startxbar = 0, startybar = 0, endxbar = 0, endybar = 0;
		
		if (angle < criticalAngleTopRight) {
			startxbar = this.scene.getWidth() - p.getMinWidth();
			startybar = (this.scene.getHeight() / 2) - (this.scene.getWidth() / 2) * Math.tan(angle * Math.PI / 180) - p.getMinHeight() / 2;
		} else if (angle < criticalAngleTopLeft) {
			startybar = 0;
			if (angle == 90) 
				startxbar = this.scene.getWidth() / 2 - p.getMinWidth() / 2;
			else 
				startxbar = (this.scene.getHeight() / 2) / Math.tan(angle * Math.PI / 180) + (this.scene.getWidth() / 2) - p.getMinWidth() / 2;
		} else if (angle < criticalAngleBottomLeft) {
			startxbar = 0;
			startybar = (this.scene.getHeight() / 2) - (this.scene.getWidth() / 2) * Math.tan((180 - angle) * Math.PI / 180) - p.getMinHeight() / 2;
		} else if (angle == 270) {
			startxbar = (this.scene.getWidth() / 2) - p.getMinWidth() / 2;
			startybar = (handPane.getLayoutY() + handPane.getMinHeight() / 2) - p.getMinHeight() / 2;
		}
		
		endxbar = (this.scene.getWidth() / 2) - p.getMinWidth()/2;
		endybar = this.lastCardPane.getLayoutY() + 0.5*this.lastCardPane.getMinHeight() - p.getMinHeight() / 2;
		p.setLayoutX(startxbar);
		p.setLayoutY(startybar);
		this.pane.getChildren().add(p);
		final double xposstart = startxbar;
		final double yposstart = startybar;
		final double xposend = endxbar;
		final double yposend = endybar;
		Timeline tl = new Timeline(new KeyFrame(Duration.millis(25), ae -> {
			p.setLayoutX(p.getLayoutX() + (xposend - xposstart)/20);
			p.setLayoutY(p.getLayoutY() + (yposend - yposstart)/20);
		}));
		tl.setCycleCount(20);
		tl.setOnFinished(ef -> {
			p.setLayoutX(-500);
			p.setLayoutY(-500);
			this.pane.getChildren().remove(p);
		});
		tl.play();
	}
	
	public void updateHand(String...command) {
		/**
		 * All of your cards
		 */
		if (command.length != 0 && !command[0].equals("from cursor")) {
			String car = command[0];
			ArrayList<UnoCard> cs = UnoCard.eval(car);
			this.cards = cs;
			displaySet = 0;
		}
		if (this.cards == null)
			this.cards = new ArrayList<UnoCard>();
		
		UnoCard.sort(this.cards);
		
		handPane.getChildren().clear();
		if (displaySet == 0) {
			prevPage.setVisible(false);
		} else {
			prevPage.setVisible(true);
		}
		if (disabledCursor || (!allCardsUnplayable() && drawIffNeeded)) {
			drawCardIV.setOnMouseClicked(e -> {});
		} else {
			drawCardIV.setOnMouseClicked(e -> {
				if (!disabledCursor) {
					this.client.sendMessageToServer("DRAW");
					drawCardIV.setOnMouseClicked(ef -> {});
				}
			});
		}
		if (!disabledCursor) {
			drawCardIV.setOnMouseEntered(e -> {
				if (!disabledCursor)
					scene.setCursor(Cursor.HAND);
			});
			drawCardIV.setOnMouseExited(e -> {
				if (!disabledCursor)
					scene.setCursor(Cursor.DEFAULT);
			});
		}
		int maxCardsInHand = (int)Math.floor((this.handPane.getMaxWidth() - 60) / 25);
		for (int i=maxCardsInHand*displaySet;i<Math.min(maxCardsInHand + maxCardsInHand * displaySet, cards.size());i++) {
			final int k = i;
			final double dist_from_left = (this.handPane.getMinWidth() - (60 + 25 * (Math.min(maxCardsInHand + maxCardsInHand * displaySet, cards.size()) - maxCardsInHand * displaySet))) / 2.0;
			Pane p = cards.get(i).getPane();
			p.setLayoutX(dist_from_left + 25 * (i % maxCardsInHand));
			if (!disabledCursor) {
				p.setOnMouseEntered(e -> {
					if (disabledCursor)
						return;
					drawCard.getChildren().clear();
					Pane newPane = cards.get(k).getCopyOfPane();
					drawCard.getChildren().add(newPane);
					scene.setCursor(Cursor.HAND);
				});
				p.setOnMouseExited(e -> {
					if (disabledCursor)
						return;
					drawCard.getChildren().clear();
					drawCard.getChildren().add(drawCardIV);
					scene.setCursor(Cursor.DEFAULT);
				});
				p.setOnMouseClicked(e -> {
					if (disabledCursor)
						return;
					if (cards.get(k).getType().toString().indexOf("WILD") == -1)
						this.client.sendMessageToServer(cards.get(k).toString());
					else {
						Stage popup = new Stage();
						
						Pane colorSelect = new Pane();
						Scene sce = new Scene(colorSelect, 200, 200);
						colorSelect.setMinSize(200, 200);
						colorSelect.setMaxSize(200, 200);
						
						Pane red = new Pane();
						red.setStyle("-fx-background-color: rgb(238,22,31);");
						Util.setXYAndSize(red, 25, 25, 75, 75);
						Util.setOnHover(red, sce);
						red.setOnMouseClicked(f -> {
							popup.close();
							wildCardSelection(cards.get(k).toString() + ",RED");
						});
						
						Pane blue = new Pane();
						blue.setStyle("-fx-background-color: rgb(0,150,219);");
						Util.setXYAndSize(blue, 100, 25, 75, 75);
						Util.setOnHover(blue, sce);
						blue.setOnMouseClicked(f -> {
							popup.close();
							wildCardSelection(cards.get(k).toString() + ",BLUE");
						});
						
						Pane green = new Pane();
						green.setStyle("-fx-background-color: rgb(0,168,80);");
						Util.setXYAndSize(green, 25, 100, 75, 75);
						Util.setOnHover(green, sce);
						green.setOnMouseClicked(f -> {
						popup.close();
							wildCardSelection(cards.get(k).toString() + ",GREEN");
						});
						
						Pane yellow = new Pane();
						yellow.setStyle("-fx-background-color: rgb(255,223,0);");
						Util.setXYAndSize(yellow, 100, 100, 75, 75);
						Util.setOnHover(yellow, sce);
						yellow.setOnMouseClicked(f -> {
							popup.close();
							wildCardSelection(cards.get(k).toString() + ",YELLOW");
						});
						
						colorSelect.getChildren().addAll(blue, red, green, yellow);
						
						Label direction;
						if (cards.get(k).getType() == CardType.WILD)
							direction = new Label("Wild Card Color Selection");
						else
							direction = new Label("Wild +4 Color Selection");
						direction.setLayoutX(0);
						direction.setLayoutY(8);
						direction.setMinSize(200, 0);
						direction.setAlignment(Pos.CENTER);
						colorSelect.getChildren().add(direction);
						
						popup.setScene(sce);
						popup.initModality(Modality.APPLICATION_MODAL);
						popup.setAlwaysOnTop(true);
						popup.show();
					}
				});
			}
			handPane.getChildren().add(p);
		}
		if (cards.size() <= maxCardsInHand + maxCardsInHand * displaySet) {
			nextPage.setVisible(false);
		} else {
			nextPage.setVisible(true);
		}
	}

	public void updateCardPlayed(String repr) {
		UnoCard c = UnoCard.eval(repr).get(0);
		if (c == null) {
			chooseACard.getChildren().clear();
			Label lastPlayerLabel = new Label("Choose any card");
			lastPlayerLabel.setStyle("-fx-font-size: 20px;");
			Label lastPlayerLabel2 = new Label("to play.");
			lastPlayerLabel2.setStyle("-fx-font-size: 20px;");
			chooseACard.getChildren().addAll(lastPlayerLabel, lastPlayerLabel2);
			lastCardPane.getChildren().addAll(chooseACard);
		} else if (!UnoCard.typeWillNotAffectNext(c)) {
			lastCard = c;
			lastCardPane.getChildren().removeAll(chooseACard);
			StackPane p = (StackPane)(c.getPane());
			lastCardPane.getChildren().remove(p);
			lastCardPane.getChildren().add(p);
		}
		updateDirectionColors(c);
	}
	
	private void updateDirectionColors(UnoCard c) {
		//Called by updateCardPlayed
		if (c != null && Util.hasColor(c.getType())) {
			for (Line l:permanentLines) 
				l.setStroke(Color.web(Util.typeToColorString(c)));
			for (Line l : regDirectionArrows) 
				l.setStroke(Color.web(Util.typeToColorString(c)));
			for (Line l:reverseDirectionArrows)
				l.setStroke(Color.web(Util.typeToColorString(c)));
		}
	}
	
	public void updateGameDirection(String pos) {
		boolean positive = Boolean.parseBoolean(pos);
		if (positive) {//regular direction
			if (!regDirectionArrows.get(0).isVisible())
				for (Line l:regDirectionArrows)
					l.setVisible(true);
			if (reverseDirectionArrows.get(0).isVisible())
				for (Line l:reverseDirectionArrows)
					l.setVisible(false);
		} else {//opposite direction
			if (regDirectionArrows.get(0).isVisible())
				for (Line l:regDirectionArrows)
					l.setVisible(false);
			if (!reverseDirectionArrows.get(0).isVisible())
				for (Line l : reverseDirectionArrows)
					l.setVisible(true);
		}
	}
	
	@Override
	public void updateEnd(String command) {
		
		this.stage.setOnCloseRequest(null);
		
		drawCardIV.setVisible(false);
		
		Stage newStage = new Stage();
		StackPane sp = new StackPane();
		sp.setMinSize(300, 200);
		Label lb = new Label(command);
		lb.setStyle("-fx-font-size: 20px;");
		sp.getChildren().add(lb);
		newStage.setTitle("Game Over!");
		Scene sc = new Scene(sp, 500, 200);
		newStage.setScene(sc);
		newStage.setOnCloseRequest(e -> {
			this.client.sendMessageToServer("DISCONNECT");
			this.close();
		});
		newStage.initModality(Modality.APPLICATION_MODAL);
		newStage.setAlwaysOnTop(true);
		newStage.show();
	}

	public void updateNumCards(String command) {
		/**
		 * Player 0's name "," Player 0's number of cards
		 * player 1's name "," Player 1's number of cards
		 */
		String[] parsed = command.split("\n");
		
		int turn = Integer.parseInt(parsed[0]);
		this.updateSelfNameAndCards(turn);
		
		this.pane.getChildren().removeAll(eachPlayerNumCards);
		eachPlayerNumCards.clear();
		
		for (int i=0; i<totalPlayers; i++) {
			if (i == this.client.getID())
				continue;
			double angle = getAngle(i);
			double endxbar = -1, endybar = -1;
			if (angle < criticalAngleTopRight) {
				endxbar = this.scene.getWidth();
				endybar = (this.scene.getHeight() / 2) - (this.scene.getWidth() / 2) * Math.tan(angle * Math.PI / 180);
			} else if (angle < criticalAngleTopLeft) {
				endybar = 0;
				if (angle == 90) endxbar = this.scene.getWidth() / 2;
				else endxbar = (this.scene.getHeight() / 2) / Math.tan(angle * Math.PI / 180) + (this.scene.getWidth() / 2);
			} else if (angle < criticalAngleBottomLeft) {
				endxbar = 0;
				endybar = (this.scene.getHeight() / 2) - (this.scene.getWidth() / 2) * Math.tan((180 - angle) * Math.PI / 180);
			} 
			Pane newPanee = new TotalCardPane(parsed[i+1].substring(0, parsed[i+1].indexOf(",")), 
													  parsed[i+1].substring(parsed[i+1].indexOf(",") + 1), 
													  endxbar, endybar, (i == turn)).getPane();
			newPanee.setOpacity(handPane.getOpacity());
			eachPlayerNumCards.add(newPanee);
		}
		this.pane.getChildren().addAll(eachPlayerNumCards);
	}
	
	public void updateNumCardsFinal(String command) {
		/**
		 * Player 0's name "," Player 0's number of cards
		 * player 1's name "," Player 1's number of cards
		 */
		
		String[] parsed = command.split("\n");
		
		int turn = Integer.parseInt(parsed[0]);
		this.updateSelfNameAndCards(turn);
		
		this.pane.getChildren().removeAll(eachPlayerNumCards);
		eachPlayerNumCards.clear();
		
		for (int i=0; i<totalPlayers; i++) {
			if (i == this.client.getID())
				continue;
			double angle = getAngle(i);
			double endxbar = -1, endybar = -1;
			if (angle < criticalAngleTopRight) {
				endxbar = this.scene.getWidth();
				endybar = (this.scene.getHeight() / 2) - (this.scene.getWidth() / 2) * Math.tan(angle * Math.PI / 180);
			} else if (angle < criticalAngleTopLeft) {
				endybar = 0;
				if (angle == 90) endxbar = this.scene.getWidth() / 2;
				else endxbar = (this.scene.getHeight() / 2) / Math.tan(angle * Math.PI / 180) + (this.scene.getWidth() / 2);
			} else if (angle < criticalAngleBottomLeft) {
				endxbar = 0;
				endybar = (this.scene.getHeight() / 2) - (this.scene.getWidth() / 2) * Math.tan((180 - angle) * Math.PI / 180);
			} 
			eachPlayerNumCards.add((new TotalCardPane(parsed[i+1].substring(0, parsed[i+1].indexOf(",")), 
													  parsed[i+1].substring(parsed[i+1].indexOf(",") + 1), 
													  endxbar, endybar, false)).getPane());
		}
		this.pane.getChildren().addAll(eachPlayerNumCards);
	}
	
	private void updateSelfNameAndCards(int turn) {
		selfNameAndCards.getChildren().clear();
		Label lb = new Label(this.client.getName());
		if (turn == this.client.getID())
			lb.setTextFill(Color.RED);
		lb.setStyle("-fx-font-size: 20px;");
		Label lb2 = new Label("" + cards.size());
		if (turn == this.client.getID())
			lb2.setTextFill(Color.RED);
		lb2.setStyle("-fx-font-size: 20px;");
		selfNameAndCards.getChildren().addAll(lb, lb2);
		selfNameAndCards.setStyle("-fx-spacing: 25px;-fx-border-width: 2px;-fx-border-color: " + (turn == this.client.getID() ? "red;" : "black;"));
		selfNameAndCards.setAlignment(Pos.CENTER);
	}
	
	private double getAngle(int playerID) {
		if (playerID == this.client.getID())
			return 270;
		else {
			ArrayList<Double> angles = new ArrayList<Double>();//0 <= all angles <= 270
			angles.add(270.0);
			if (totalPlayers == 2)
				angles.add(90.0);
			for (int i=0;i<=totalPlayers - 2;i++)
				angles.add(i * (180.0 / (totalPlayers - 2)));
			return angles.get(((playerID + totalPlayers) - this.client.getID()) % totalPlayers);
		}
	}
	
	private void wildCardSelection(String selection) {
		this.client.sendMessageToServer(selection);
		drawCard.getChildren().clear();
		drawCard.getChildren().add(drawCardIV);
	}
	
	private boolean allCardsUnplayable() {
		for (UnoCard c : cards)
			if (c.isCompatibleWith(lastCard))
				return false;
		return true;
	}
	
	public void showSkipOverlay(String ct) {
		
		String color = Util.stringToColorString(ct.substring(0, ct.indexOf("\n")));
		String direction = ct.substring(ct.indexOf("\n") + 1);
		
		Pane newPane = new Pane();
		newPane.setMinSize(this.scene.getWidth(), this.scene.getHeight());
		
		double radius = 200;
		Circle circle = new Circle(radius);
		circle.setCenterX(this.scene.getWidth() / 2);
		circle.setCenterY(this.scene.getHeight() / 2);
		circle.setStrokeWidth(10);
		circle.setStroke(Color.web(color));
		circle.setFill(Color.TRANSPARENT);
		
		Line l = new Line(circle.getCenterX() - radius, circle.getCenterY(),
				          circle.getCenterX() + radius, circle.getCenterY());
		l.setStrokeWidth(10);
		l.setStroke(Color.web(color));
		
		newPane.getChildren().addAll(circle, l);
		this.pane.getChildren().add(newPane);
		
		setOpacityOfPaneElements(0.2);
		
		newPane.setRotate(Math.random() * 360);
		
		Timeline tl = new Timeline(new KeyFrame(Duration.millis(25), ae -> {
			newPane.setRotate(newPane.getRotate() - 4.5 * (direction.equals("true") ? 1 : -1));
		}));
		tl.setCycleCount(80);
		tl.setOnFinished(e -> {
			setOpacityOfPaneElements(1);
			newPane.getChildren().remove(circle);
			newPane.getChildren().remove(l);
			this.pane.getChildren().remove(newPane);
		});
		tl.play();
	}

	public void showReverseOverlay(String direction) {
		Pane newPane = new Pane();
		newPane.setMinSize(this.scene.getWidth(), this.scene.getHeight());
		
		Circle circle = new Circle(this.scene.getHeight() / 2 - 100);
		circle.setCenterX(this.scene.getWidth() / 2);
		circle.setCenterY(this.scene.getHeight() / 2);
		circle.setStrokeWidth(10);
		circle.setStroke(Color.SILVER);
		circle.setFill(Color.TRANSPARENT);
		
		double[] topPoint = new double[] {circle.getCenterX(), circle.getCenterY() - circle.getRadius()};
		double[] rightPoint = new double[] {circle.getCenterX() + circle.getRadius(), circle.getCenterY()};
		double[] botPoint = new double[] {circle.getCenterX(), circle.getCenterY() + circle.getRadius()};
		double[] leftPoint = new double[] {circle.getCenterX() - circle.getRadius(), circle.getCenterY()};
		
		int factor = (direction.equals("true") ? 1 : -1);
		ArrayList<Line> lines = new ArrayList<Line>();
		lines.add(new Line(topPoint[0] + factor * 40, topPoint[1] - 40, topPoint[0], topPoint[1]));
		lines.add(new Line(topPoint[0] + factor * 40, topPoint[1] + 40, topPoint[0], topPoint[1]));
		lines.add(new Line(rightPoint[0] - 40, rightPoint[1] + factor * 40, rightPoint[0], rightPoint[1]));
		lines.add(new Line(rightPoint[0] + 40, rightPoint[1] + factor * 40, rightPoint[0], rightPoint[1]));
		lines.add(new Line(botPoint[0] - factor * 40, botPoint[1] - 40, botPoint[0], botPoint[1]));
		lines.add(new Line(botPoint[0] - factor * 40, botPoint[1] + 40, botPoint[0], botPoint[1]));
		lines.add(new Line(leftPoint[0] - 40, leftPoint[1] - factor * 40, leftPoint[0], leftPoint[1]));
		lines.add(new Line(leftPoint[0] + 40, leftPoint[1] - factor * 40, leftPoint[0], leftPoint[1]));

		for (Line l : lines) {
			l.setStrokeWidth(20);
		}
		for (int i=0;i<2;i++) {
			lines.get(i).setStroke(Color.web(Util.stringToColorString("RED")));
			lines.get(i + 2).setStroke(Color.web(Util.stringToColorString("BLUE")));
			lines.get(i + 4).setStroke(Color.web(Util.stringToColorString("YELLOW")));
			lines.get(i + 6).setStroke(Color.web(Util.stringToColorString("GREEN")));
		}
		
		newPane.getChildren().add(circle);
		newPane.getChildren().addAll(lines);
		this.pane.getChildren().add(newPane);
		
		circle.setId("");

		setOpacityOfPaneElements(0.2);
		
		Timeline tl = new Timeline(new KeyFrame(Duration.millis(25), ae -> {
			circle.setId(circle.getId() + " ");
			newPane.setRotate(0.15 * factor * circle.getId().length() * circle.getId().length() - 18 * factor * circle.getId().length());
			
			if (circle.getId().length() == 60) {
				newPane.getChildren().removeAll(lines);
				lines.clear();
				lines.add(new Line(topPoint[0] - factor * 40, topPoint[1] - 40, topPoint[0], topPoint[1]));
				lines.add(new Line(topPoint[0] - factor * 40, topPoint[1] + 40, topPoint[0], topPoint[1]));
				lines.add(new Line(rightPoint[0] - 40, rightPoint[1] - factor * 40, rightPoint[0], rightPoint[1]));
				lines.add(new Line(rightPoint[0] + 40, rightPoint[1] - factor * 40, rightPoint[0], rightPoint[1]));
				lines.add(new Line(botPoint[0] + factor * 40, botPoint[1] - 40, botPoint[0], botPoint[1]));
				lines.add(new Line(botPoint[0] + factor * 40, botPoint[1] + 40, botPoint[0], botPoint[1]));
				lines.add(new Line(leftPoint[0] - 40, leftPoint[1] + factor * 40, leftPoint[0], leftPoint[1]));
				lines.add(new Line(leftPoint[0] + 40, leftPoint[1] + factor * 40, leftPoint[0], leftPoint[1]));
				for (Line l : lines)
					l.setStrokeWidth(20);
				for (int i=0;i<2;i++) {
					lines.get(i).setStroke(Color.web(Util.stringToColorString("RED")));
					lines.get(i + 2).setStroke(Color.web(Util.stringToColorString("BLUE")));
					lines.get(i + 4).setStroke(Color.web(Util.stringToColorString("YELLOW")));
					lines.get(i + 6).setStroke(Color.web(Util.stringToColorString("GREEN")));
				}
				newPane.getChildren().addAll(lines);
			}
			
		}));
		tl.setCycleCount(120);
		tl.setOnFinished(e -> {
			setOpacityOfPaneElements(1);
			newPane.getChildren().remove(circle);
			newPane.getChildren().removeAll(lines);
			this.pane.getChildren().remove(newPane);
		});
		tl.play();
	}
	
	public void showPlus2Overlay(String ct) {
		
		String color = Util.stringToColorString(ct.substring(0, ct.indexOf("\n")));
		String direction = ct.substring(ct.indexOf("\n") + 1, ct.lastIndexOf("\n"));
		String cardsToDraw = ct.substring(ct.lastIndexOf("\n") + 1);
		
		Pane newPane = new Pane();
		newPane.setMinSize(this.scene.getWidth(), this.scene.getHeight());
		
		double radius = 200;
		Circle circle = new Circle(radius);
		circle.setCenterX(this.scene.getWidth() / 2);
		circle.setCenterY(this.scene.getHeight() / 2);
		circle.setStrokeWidth(10);
		circle.setStroke(Color.web(color));
		circle.setFill(Color.TRANSPARENT);
		
		Line l = new Line(circle.getCenterX() - radius, circle.getCenterY(),
				          circle.getCenterX() + radius, circle.getCenterY());
		l.setStrokeWidth(10);
		l.setStroke(Color.web(color));
		
		Label plus = new Label("PLUS 2");
		plus.setAlignment(Pos.BOTTOM_CENTER);
		plus.setTextFill(Color.web(color));
		plus.setStyle("-fx-font-size: 80;");
		Util.setXY(plus, circle.getCenterX() - radius, circle.getCenterY() - radius);
		plus.setMinSize(400, 190);
		plus.setMaxSize(400, 190);
		
		Label two = new Label("+" + cardsToDraw);
		two.setAlignment(Pos.TOP_CENTER);
		two.setTextFill(Color.web(color));
		two.setStyle("-fx-font-size: 80;");
		Util.setXY(two, circle.getCenterX() - radius, circle.getCenterY() + 10);
		two.setMinSize(400, 190);
		two.setMaxSize(400, 190);
		
		newPane.getChildren().addAll(circle, l, plus, two);
		this.pane.getChildren().add(newPane);
		
		setOpacityOfPaneElements(0.2);
		
		Timeline tl = new Timeline(new KeyFrame(Duration.millis(25), ae -> {
			newPane.setRotate(newPane.getRotate() - 4.5 * (direction.equals("true") ? 1 : -1));
		}));
		tl.setCycleCount(80);
		tl.setOnFinished(e -> {
			setOpacityOfPaneElements(1);
			newPane.getChildren().removeAll(circle, l, plus, two);
			this.pane.getChildren().remove(newPane);
		});
		tl.play();
	}
	
	public void showPlus4Overlay(String input) {
		String[] parsed = input.split("\n");
		String oldType = parsed[0];
		String newType = parsed[1];
		String direction = parsed[2];
		String cardsToDraw = parsed[3];
		
		double[] rgbStart = Util.rgbArray(oldType);
		double[] rgbDiff = Util.rgbDiff(oldType, newType);
		
		Pane newPane = new Pane();
		newPane.setMinSize(this.scene.getWidth(), this.scene.getHeight());
		
		double radius = 200;
		Circle circle = new Circle(radius);
		circle.setCenterX(this.scene.getWidth() / 2);
		circle.setCenterY(this.scene.getHeight() / 2);
		circle.setStrokeWidth(10);
		circle.setStroke(Color.web(Util.stringToColorString(oldType)));
		circle.setFill(Color.TRANSPARENT);
		
		Line l = new Line(circle.getCenterX() - radius, circle.getCenterY(),
		          circle.getCenterX() + radius, circle.getCenterY());
		l.setStrokeWidth(10);
		l.setStroke(Color.web(Util.stringToColorString(oldType)));
		
		Label plus = new Label("DRAW 4");
		plus.setAlignment(Pos.BOTTOM_CENTER);
		plus.setTextFill(Color.web(Util.stringToColorString(oldType)));
		plus.setStyle("-fx-font-size: 80;");
		Util.setXY(plus, circle.getCenterX() - radius, circle.getCenterY() - radius);
		plus.setMinSize(400, 200);
		plus.setMaxSize(400, 200);
		
		Label four = new Label("+" + cardsToDraw);
		four.setAlignment(Pos.TOP_CENTER);
		four.setTextFill(Color.web(Util.stringToColorString(oldType)));
		four.setStyle("-fx-font-size: 80;");
		Util.setXY(four, circle.getCenterX() - radius, circle.getCenterY());
		four.setMinSize(400, 200);
		four.setMaxSize(400, 200);
		
		newPane.getChildren().addAll(circle, l, plus, four);
		this.pane.getChildren().add(newPane);
		
		circle.setId("");
		
		setOpacityOfPaneElements(0.2);
		
		Timeline tl = new Timeline(new KeyFrame(Duration.millis(25), ae -> {
			circle.setId(circle.getId() + " ");
			String newRgb = "rgb(" + (int)(rgbStart[0] + Math.min(80, circle.getId().length() + 20)*rgbDiff[0] / 80.0) + 
					"," + (int)(rgbStart[1] + Math.min(80, circle.getId().length() + 20)*rgbDiff[1] / 80.0) + 
					"," + (int)(rgbStart[2] + Math.min(80, circle.getId().length() + 20)*rgbDiff[2] / 80.0) + ");";
			circle.setStroke(Color.web(newRgb));
			l.setStroke(Color.web(newRgb));
			plus.setTextFill(Color.web(newRgb));
			four.setTextFill(Color.web(newRgb));
			newPane.setRotate(newPane.getRotate() - 4.5 * (direction.equals("true") ? 1 : -1));
		}));
		tl.setCycleCount(80);
		tl.setOnFinished(e -> {
			setOpacityOfPaneElements(1);
			newPane.getChildren().removeAll(circle, l, plus, four);
			this.pane.getChildren().remove(newPane);
		});
		tl.play();
	}
	
	public void showWildOverlay(String input) {
		String[] parsed = input.split("\n");
		String oldType = parsed[0];
		String newType = parsed[1];
		String direction = parsed[2];
		double[] rgbDiff = Util.rgbDiff(oldType, newType);
		double[] rgbStart = Util.rgbArray(oldType);
		
		Pane newPane = new Pane();
		newPane.setMinSize(this.scene.getWidth(), this.scene.getHeight());
		
		double radius = 200;
		Circle circle = new Circle(radius);
		circle.setCenterX(this.scene.getWidth() / 2);
		circle.setCenterY(this.scene.getHeight() / 2);
		circle.setStrokeWidth(50);
		circle.setStroke(Color.web(Util.stringToColorString(oldType)));
		circle.setFill(Color.TRANSPARENT);
		
		Label lbl = new Label("WILD");
		lbl.setAlignment(Pos.CENTER);
		lbl.setTextFill(Color.web(Util.stringToColorString(oldType)));
		lbl.setStyle("-fx-font-size: 100;");
		Util.setXY(lbl, circle.getCenterX() - radius, circle.getCenterY() - radius);
		lbl.setMinSize(400, 400);
		lbl.setMaxSize(400, 400);
		
		newPane.getChildren().addAll(circle, lbl);
		this.pane.getChildren().add(newPane);
		
		circle.setId("");
		
		setOpacityOfPaneElements(0.2);
		
		Timeline tl = new Timeline(new KeyFrame(Duration.millis(25), ae -> {
			circle.setId(circle.getId() + " ");
			String newRgb = "rgb(" + (int)(rgbStart[0] + Math.min(80, circle.getId().length() + 20)*rgbDiff[0] / 80.0) + 
					"," + (int)(rgbStart[1] + Math.min(80, circle.getId().length() + 20)*rgbDiff[1] / 80.0) + 
					"," + (int)(rgbStart[2] + Math.min(80, circle.getId().length() + 20)*rgbDiff[2] / 80.0) + ");";
			circle.setRotate(circle.getRotate() - 4.5 * (direction.equals("true") ? 1 : -1));
			circle.setStroke(Color.web(newRgb));
			lbl.setTextFill(Color.web(newRgb));
		}));
		tl.setCycleCount(80);
		tl.setOnFinished(e -> {
			setOpacityOfPaneElements(1);
			newPane.getChildren().remove(circle);
			newPane.getChildren().remove(lbl);
			this.pane.getChildren().remove(newPane);
		});
		tl.play();
	}
	
	public void showRotateCardOverlay(String direction) {
		Pane newPane = new Pane();
		newPane.setMinSize(this.scene.getWidth(), this.scene.getHeight());
		
		Circle circle = new Circle(this.scene.getHeight() / 2 - 100);
		circle.setCenterX(this.scene.getWidth() / 2);
		circle.setCenterY(this.scene.getHeight() / 2);
		circle.setStrokeWidth(10);
		circle.setStroke(Color.SILVER);
		circle.setFill(Color.TRANSPARENT);
		
		double[] topPoint = new double[] {circle.getCenterX(), circle.getCenterY() - circle.getRadius()};
		double[] rightPoint = new double[] {circle.getCenterX() + circle.getRadius(), circle.getCenterY()};
		double[] botPoint = new double[] {circle.getCenterX(), circle.getCenterY() + circle.getRadius()};
		double[] leftPoint = new double[] {circle.getCenterX() - circle.getRadius(), circle.getCenterY()};
		
		int factor = (direction.equals("true") ? 1 : -1);
		ArrayList<Line> lines = new ArrayList<Line>();
		lines.add(new Line(topPoint[0] + factor * 40, topPoint[1] - 40, topPoint[0], topPoint[1]));
		lines.add(new Line(topPoint[0] + factor * 40, topPoint[1] + 40, topPoint[0], topPoint[1]));
		lines.add(new Line(rightPoint[0] - 40, rightPoint[1] + factor * 40, rightPoint[0], rightPoint[1]));
		lines.add(new Line(rightPoint[0] + 40, rightPoint[1] + factor * 40, rightPoint[0], rightPoint[1]));
		lines.add(new Line(botPoint[0] - factor * 40, botPoint[1] - 40, botPoint[0], botPoint[1]));
		lines.add(new Line(botPoint[0] - factor * 40, botPoint[1] + 40, botPoint[0], botPoint[1]));
		lines.add(new Line(leftPoint[0] - 40, leftPoint[1] - factor * 40, leftPoint[0], leftPoint[1]));
		lines.add(new Line(leftPoint[0] + 40, leftPoint[1] - factor * 40, leftPoint[0], leftPoint[1]));

		for (Line l : lines) {
			l.setStrokeWidth(20);
		}
		for (int i=0;i<2;i++) {
			lines.get(i).setStroke(Color.web(Util.stringToColorString("RED")));
			lines.get(i + 2).setStroke(Color.web(Util.stringToColorString("BLUE")));
			lines.get(i + 4).setStroke(Color.web(Util.stringToColorString("YELLOW")));
			lines.get(i + 6).setStroke(Color.web(Util.stringToColorString("GREEN")));
		}
		
		newPane.getChildren().add(circle);
		newPane.getChildren().addAll(lines);
		this.pane.getChildren().add(newPane);
				
		setOpacityOfPaneElements(0.2);
		
		Timeline tl = new Timeline(new KeyFrame(Duration.millis(25), ae -> {
			newPane.setRotate(newPane.getRotate() + 9 * factor * -1);
		}));
		tl.setCycleCount(80);
		tl.setOnFinished(e -> {
			setOpacityOfPaneElements(1);
			newPane.getChildren().remove(circle);
			newPane.getChildren().removeAll(lines);
			this.pane.getChildren().remove(newPane);
		});
		tl.play();
	}
	
	public void showSwapCardOverlay(String command) {
		double angle1 = getAngle(Integer.parseInt(command.split("\n")[0]));
		double angle2 = getAngle(Integer.parseInt(command.split("\n")[1]));
		double startxbar = 0, startybar = 0, endxbar = 0, endybar = 0;
		if (angle1 < criticalAngleTopRight) {
			startxbar = this.scene.getWidth();
			startybar = (this.scene.getHeight() / 2) - (this.scene.getWidth() / 2) * Math.tan(angle1 * Math.PI / 180);
		} else if (angle1 < criticalAngleTopLeft) {
			startybar = 0;
			if (angle1 == 90) startxbar = this.scene.getWidth() / 2;
			else startxbar = (this.scene.getHeight() / 2) / Math.tan(angle1 * Math.PI / 180) + (this.scene.getWidth() / 2);
		} else if (angle1 < criticalAngleBottomLeft) {
			startxbar = 0;
			startybar = (this.scene.getHeight() / 2) - (this.scene.getWidth() / 2) * Math.tan((180 - angle1) * Math.PI / 180);
		} else if (angle1 == 270) {
			startxbar = this.scene.getWidth() / 2;
			startybar = this.handPane.getLayoutY() + this.handPane.getMinHeight() / 2;
		}
		if (angle2 < criticalAngleTopRight) {
			endxbar = this.scene.getWidth();
			endybar = (this.scene.getHeight() / 2) - (this.scene.getWidth() / 2) * Math.tan(angle2 * Math.PI / 180);
		} else if (angle2 < criticalAngleTopLeft) {
			endybar = 0;
			if (angle2 == 90) endxbar = this.scene.getWidth() / 2;
			else endxbar = (this.scene.getHeight() / 2) / Math.tan(angle2 * Math.PI / 180) + (this.scene.getWidth() / 2);
		} else if (angle2 < criticalAngleBottomLeft) {
			endxbar = 0;
			endybar = (this.scene.getHeight() / 2) - (this.scene.getWidth() / 2) * Math.tan((180 - angle2) * Math.PI / 180);
		} else if (angle2 == 270) {
			endxbar = this.scene.getWidth() / 2;
			endybar = this.handPane.getLayoutY() + this.handPane.getMinHeight() / 2;
		}
		ArrayList<Line> lines = new ArrayList<Line>();
		double diff = 20;//customizable
		if (endxbar == startxbar) {
			lines.add(new Line(startxbar - diff - diff /2, startybar, endxbar - diff - diff /2, endybar));
			lines.add(new Line(startxbar - diff / 2, startybar, endxbar - diff / 2, endybar));
			lines.add(new Line(startxbar + diff / 2, startybar, endxbar + diff / 2, endybar));
			lines.add(new Line(startxbar + diff + diff / 2, startybar, endxbar + diff + diff / 2, endybar));
		} else /*if (angle1 == 270 && (angle2 < criticalAngleTopRight || angle2 > criticalAngleTopLeft) ||
				angle2 == 270 && (angle1 < criticalAngleTopRight || angle1 > criticalAngleTopLeft)) */{
			double phi = Math.atan((endybar - startybar) / (endxbar - startxbar));
			lines.add(new Line(startxbar + (diff + diff / 2) * Math.sin(phi), startybar - (diff + diff / 2) / Math.cos(phi) * (1 - Math.tan(phi) * Math.sin(phi)), 
								endxbar + (diff + diff / 2) * Math.sin(phi), endybar - (diff + diff / 2) / Math.cos(phi) * (1 - Math.tan(phi) * Math.sin(phi))));
			lines.add(new Line(startxbar + (diff / 2) * Math.sin(phi), startybar - (diff / 2) / Math.cos(phi) * (1 - Math.tan(phi) * Math.sin(phi)), 
					endxbar + (diff / 2) * Math.sin(phi), endybar - (diff / 2) / Math.cos(phi) * (1 - Math.tan(phi) * Math.sin(phi))));
			lines.add(new Line(startxbar - (diff / 2) * Math.sin(phi), startybar + (diff / 2) / Math.cos(phi) * (1 - Math.tan(phi) * Math.sin(phi)), 
					endxbar - (diff / 2) * Math.sin(phi), endybar + (diff / 2) / Math.cos(phi) * (1 - Math.tan(phi) * Math.sin(phi))));
			lines.add(new Line(startxbar - (diff + diff / 2) * Math.sin(phi), startybar + (diff + diff / 2) / Math.cos(phi) * (1 - Math.tan(phi) * Math.sin(phi)), 
					endxbar - (diff + diff / 2) * Math.sin(phi), endybar + (diff + diff / 2) / Math.cos(phi) * (1 - Math.tan(phi) * Math.sin(phi))));
		} /*else {
			double phi = Math.atan((endybar - startybar) / (endxbar - startxbar));
			lines.add(new Line(startxbar - (diff + diff / 2) / Math.cos(phi) * (1 - Math.tan(phi) * Math.sin(phi)), startybar + (diff + diff / 2) * Math.sin(phi), 
								endxbar - (diff + diff / 2) / Math.cos(phi) * (1 - Math.tan(phi) * Math.sin(phi)), endybar + (diff + diff / 2) * Math.sin(phi)));
			lines.add(new Line(startxbar - (diff / 2) / Math.cos(phi) * (1 - Math.tan(phi) * Math.sin(phi)), startybar + (diff / 2) * Math.sin(phi), 
					endxbar - (diff / 2) / Math.cos(phi) * (1 - Math.tan(phi) * Math.sin(phi)), endybar + (diff / 2) * Math.sin(phi)));
			lines.add(new Line(startxbar + (diff / 2) / Math.cos(phi) * (1 - Math.tan(phi) * Math.sin(phi)), startybar - (diff / 2) * Math.sin(phi), 
					endxbar + (diff / 2) / Math.cos(phi) * (1 - Math.tan(phi) * Math.sin(phi)), endybar - (diff / 2) * Math.sin(phi)));
			lines.add(new Line(startxbar + (diff + diff / 2) / Math.cos(phi) * (1 - Math.tan(phi) * Math.sin(phi)), startybar - (diff + diff / 2) * Math.sin(phi), 
					endxbar + (diff + diff / 2) / Math.cos(phi) * (1 - Math.tan(phi) * Math.sin(phi)), endybar - (diff + diff / 2) * Math.sin(phi)));
		}*/
		
		lines.get(0).setStroke(Color.web(Util.stringToColorString("RED")));
		lines.get(1).setStroke(Color.web(Util.stringToColorString("BLUE")));
		lines.get(2).setStroke(Color.web(Util.stringToColorString("YELLOW")));
		lines.get(3).setStroke(Color.web(Util.stringToColorString("GREEN")));
		
		for (Line l : lines) {
			l.setStrokeWidth(10);
		}
		this.pane.getChildren().addAll(lines);
		
		setOpacityOfPaneElements(0.2);
		
		Timeline tl = new Timeline(new KeyFrame(Duration.millis(25), ae-> {

		}));
		tl.setCycleCount(80);
		tl.setOnFinished(e -> {
			setOpacityOfPaneElements(1);
			this.pane.getChildren().removeAll(lines);
		});
		tl.play();
	}
	
	private void setOpacityOfPaneElements(double val) {
		lastCardPane.setOpacity(val);
		movingCardBack.setOpacity(val);
		handPane.setOpacity(val);
		for (Pane pp: eachPlayerNumCards)
			pp.setOpacity(val);
		selfNameAndCards.setOpacity(val);
		drawCard.setOpacity(val);
	}

}
