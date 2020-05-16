package classes;

import java.util.ArrayList;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Used by GameGUI to put create a Pane that contains a name and number of cards
 * 		that player has. It also sets the layoutx and layouty for it. 
 * @author Roger
 *
 */
public class TotalCardPane {
	
	private VBox pane = new VBox();

	public TotalCardPane(String name, String cards, double centerx, double centery, boolean b) {
		pane.setMinSize(150, 150);
		pane.setMaxSize(150, 150);
		if (centerx <= 75)
			pane.setLayoutX(centerx);
		else if (centerx >= 750)
			pane.setLayoutX(centerx - 150);
		else
			pane.setLayoutX(centerx - pane.getMinWidth() / 2);
		if (centery <= 75)
			pane.setLayoutY(centery);
		else
			pane.setLayoutY(centery - pane.getMinHeight() / 2);
		ArrayList<Label> lbs = new ArrayList<Label>();
		while (name.length() > 0) {
			Label lb = new Label(name.substring(0, Math.min(name.length(), 11)));
			if (b)
				lb.setTextFill(Color.RED);
			lb.setStyle("-fx-font-size: 20px;");
			lbs.add(lb);
			name = name.substring(Math.min(name.length(), 11));
		}
		pane.getChildren().addAll(lbs);

		Label lb2 = new Label(cards);
		if (b)
			lb2.setTextFill(Color.RED);
		lb2.setStyle("-fx-font-size: 20px;");
		pane.getChildren().addAll(lb2);
		pane.setStyle("-fx-spacing: 15px;-fx-border-width: 2px;-fx-border-color: " + (b ? "red;" : "black;"));
		pane.setId(pane.getStyle());
		pane.setAlignment(Pos.CENTER);
	}
	
	public Pane getPane() {
		return pane;
	}
}
