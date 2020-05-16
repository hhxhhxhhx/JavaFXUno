package classes;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Creates a GUI Server Launcher that allows the Server Launcher to customize
 * 		rules of the game, and whether to enable special cards. 
 * It will create and run the UnoServer object.
 * @author Roger
 *
 */
public class UnoServerLauncher extends Application {

	public static void main(String[] args) {
		launch(args);
	}
	
	Pane pane = new Pane();
	
	Button submit;
	
	Label invalid = new Label("Invalid port and/or invalid players!");
	String[] lblNames = new String[] {"Port: ", "Humans: ", "Bots: ", "Draw only 1: ", "Draw at will: ", 
											"Redistribute: ", "Draw random: ", "Rotate if 0: ", 
											"Swap if 7: ", "Stacking +2/+4: "};
	Label gameVersion = new Label("Uno " + UnoGame.VERSION);
	ToggleButton[] buttons = new ToggleButton[lblNames.length - 3];
	NumberTextField[] fields = new NumberTextField[3];
	Stage stage;

	@Override
	public void start(Stage primaryStage) {
		
		stage = primaryStage;

		gameVersion.setId("topInfo");
		Util.setXY(gameVersion, 25, 25);
		
		pane.getChildren().add(gameVersion);
		
		for (int i=0;i<3;i++) {
			
			Label lb = new Label(lblNames[i]);
			Util.setXY(lb, 25, 80 + 40 * i);
			fields[i] = new NumberTextField((i == 0 ? 6354 : (i == 1 ? 2 : 2)));
			Util.setXY(fields[i], 150, 80 + 40 * i);

			pane.getChildren().addAll(lb, fields[i]);
		}
		
		for (int i=0;i<buttons.length;i++) {
			final int j = i;
			Label lb = new Label(lblNames[i + 3]);
			Util.setXY(lb, 25, 200 + 40 * i);
			buttons[i] = new ToggleButton("Enabled");
			buttons[i].setOnKeyPressed(e -> {
				if (e.getCode() != KeyCode.ENTER && e.getCode() != KeyCode.SPACE)
					return;
				if (buttons[j].getText().equals("Enabled")) {
					buttons[j].setText("Disabled");
					buttons[j].setSelected(false);
				} else {
					buttons[j].setText("Enabled");
					buttons[j].setSelected(true);
				}
			});
			buttons[i].setOnMouseClicked(e -> {
				if (buttons[j].getText().equals("Enabled")) {
					buttons[j].setText("Disabled");
					buttons[j].setSelected(false);
				} else {
					buttons[j].setText("Enabled");
					buttons[j].setSelected(true);
				}
				submit.requestFocus();
			});
			buttons[i].setStyle("-fx-font-size: 16px;");
			buttons[i].setSelected(true);
			Util.setXY(buttons[i], 150, 200 + 40 * i);
			pane.getChildren().addAll(lb, buttons[i]);
		}
		buttons[0].setText("Disabled");
		buttons[0].setSelected(false);
		
		submit = new Button("Launch Server!");
		submit.setOnAction(e -> {
			launchServer();
		});
		Util.setXY(submit, 25, 220 + 40 * buttons.length);
		
		invalid.setVisible(false);
		invalid.setTextFill(Color.RED);
		Util.setXY(invalid, 25, 260 + 40 * buttons.length);
		
		pane.getChildren().addAll(submit, invalid);

		Scene scene = new Scene(pane, 325, 300 + 40 * buttons.length);
		scene.getStylesheets().add("css/style.css");
		scene.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				launchServer();
			}
		});
		stage.setTitle("Uno Server Launcher");
		stage.setScene(scene);
		stage.show();
	}
	
	private void launchServer() {
		try {
			int port = parse(fields[0]);
			int humans = parse(fields[1]);
			int bots = parse(fields[2]);
			if (port < 1000) {
				invalid.setVisible(true);
				fields[0].setText("6354");
			} 
			if (humans < 1) {
				invalid.setVisible(true);
				fields[1].setText("2");
			}
			if (bots + humans > 10 || bots + humans <= 1) {
				invalid.setVisible(true);
				fields[1].setText("2");
				fields[2].setText("2");
			}
			if (port < 1000 || bots + humans > 10 || humans < 1 || bots + humans <= 1)
				return;
			stage.close();
			new UnoServer(humans, bots, port, 
									 buttons[0].getText().equals("Enabled"), 
									 buttons[1].getText().equals("Enabled"),
									 buttons[2].getText().equals("Enabled"), 
									 buttons[3].getText().equals("Enabled"), 
									 buttons[4].getText().equals("Enabled"),
									 buttons[5].getText().equals("Enabled"),
									 buttons[6].getText().equals("Enabled")).start();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private int parse(NumberTextField ntf) {
		return Integer.parseInt(ntf.getText());
	}
}
