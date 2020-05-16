package absClasses;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public abstract class GameGUI extends Application {
	
	protected Client client;
	protected Pane pane = new Pane();
	protected Scene scene;
	protected Stage stage;
	protected int width;
	protected int height;
	protected int totalPlayers;
	
	protected GameGUI(Client c, int width, int height) {
		this.client = c;
		this.width = width;
		this.height = height;
	}
	
	public final void start(Stage stage) {
		scene = new Scene(pane, width, height);
		this.stage = stage;
		this.stage.setScene(scene);
		this.stage.setOnCloseRequest(e -> {
			System.out.println("lol come on whyd u quit!");
			this.client.sendMessageToServer("FUCK THIS SHIT IM OUT");
			this.client.closeSelf();
		});
		setTitleOfStage();
		this.stage.show();
	}
	public final void close() {
		stage.close();
	}
	public final void setTotalPlayers(int n) {
		totalPlayers = n;
	}
	
	protected abstract void setTitleOfStage();
	public abstract void updateCursor(String command);
	public abstract void updateEnd(String command);
}
