package classes;

import java.util.concurrent.TimeUnit;

import enums.CardType;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Pane;

/**
 * Class that can be called by any class to run some of its methods. 
 * It has some that just save lines of code in other classes for
 * 		GUI purposes. 
 * It also has a couple of methods that change the type of a card to
 * 		an integer value, or a string, or the reverse, for convenience's sake. 
 * It also has a method to put the thread on hold, although animations and other things
 * 		will continue thorugh it. 
 * @author Roger
 *
 */
public final class Util {
	public final static void setXYAndSize(Pane p, double x, double y, double width, double height) {
		p.setLayoutX(x);
		p.setLayoutY(y);
		p.setMinSize(width, height);
		p.setMaxSize(width, height);
	}
	public final static void setXYAndSize(Pane p, double x, double y, double width) {
		p.setLayoutX(x);
		p.setLayoutY(y);
		p.setMinWidth(width);
		p.setMaxWidth(width);
	}
	public final static void setXYAndSize(Button p, double x, double y, double width) {
		p.setLayoutX(x);
		p.setLayoutY(y);
		p.setMinWidth(width);
		p.setMaxWidth(width);
	}
	public final static void setXY(Label lb, double x, double y) {
		lb.setLayoutX(x);
		lb.setLayoutY(y);
		lb.setMinHeight(30);
		lb.setMaxHeight(30);
	}
	public final static void setXY(NumberTextField ntf, double x, double y) {
		ntf.setLayoutX(x);
		ntf.setLayoutY(y);
		ntf.setMinSize(150, 30);
		ntf.setMaxSize(150, 30);
		ntf.setAlignment(Pos.CENTER_LEFT);
	}
	public final static void setXY(Button b, double x, double y) {
		b.setLayoutX(x);
		b.setLayoutY(y);
		b.setMinSize(150, 30);
		b.setMaxSize(150, 30);
	}
	public final static void setXY(ToggleButton b, double x, double y) {
		b.setLayoutX(x);
		b.setLayoutY(y);
		b.setMinSize(150, 30);
		b.setMaxSize(150, 30);
	}
	public final static void setOnHover(Pane p, Scene sce) {
		p.setOnMouseEntered(e -> {
			sce.setCursor(Cursor.HAND);
		});
		p.setOnMouseExited(e -> {
			sce.setCursor(Cursor.DEFAULT);
		});
	}
	public final static boolean hasColor(CardType ct) {
		String type = ct.toString();
		return type.indexOf("RED") != -1 || type.indexOf("BLUE") != -1 || type.indexOf("GREEN") != -1 || type.indexOf("YELLOW") != -1;
	}
	public final static String typeToColorString(UnoCard c) {
		if (c == null)
			return "rgb(0,0,0);";
		String type = c.getType().toString();
		return stringToColorString(type);
	}
	public final static String stringToColorString(String str) {
		str = str.toUpperCase();
		if (str.indexOf("RED") != -1)
			return "rgb(238,22,31);";
		else if (str.indexOf("BLUE") != -1)
			return "rgb(0,150,219);";
		else if (str.indexOf("GREEN") != -1)
			return "rgb(0,168,80);";
		else if (str.indexOf("YELLOW") != -1)
			return "rgb(255,223,0);";
		else
			return "rgb(0,0,0);";
	}
	public final static double[] rgbDiff(String old, String neww) {
		double[] oldCols = new double[3];
		double[] newCols = new double[3];
		if (old.indexOf("RED") != -1)
			oldCols = new double[] {238,22,31};
		else if (old.indexOf("BLUE") != -1)
			oldCols = new double[] {0,150,219};
		else if (old.indexOf("GREEN") != -1)
			oldCols = new double[] {0,168,80};
		else if (old.indexOf("YELLOW") != -1)
			oldCols = new double[] {255,223,0};
		if (neww.indexOf("RED") != -1)
			newCols = new double[] {238,22,31};
		else if (neww.indexOf("BLUE") != -1)
			newCols = new double[] {0,150,219};
		else if (neww.indexOf("GREEN") != -1)
			newCols = new double[] {0,168,80};
		else if (neww.indexOf("YELLOW") != -1)
			newCols = new double[] {255,223,0};
		return new double[] {newCols[0] - oldCols[0], newCols[1] - oldCols[1], newCols[2] - oldCols[2]};
	}
	public final static double[] rgbArray(String old) {
		double[] oldCols = new double[3];
		if (old.indexOf("RED") != -1)
			oldCols = new double[] {238,22,31};
		else if (old.indexOf("BLUE") != -1)
			oldCols = new double[] {0,150,219};
		else if (old.indexOf("GREEN") != -1)
			oldCols = new double[] {0,168,80};
		else if (old.indexOf("YELLOW") != -1)
			oldCols = new double[] {255,223,0};
		return oldCols;
	}
	public final static void sleep(int n) {
		try {
			if (n <= 10)
				TimeUnit.SECONDS.sleep(n);
			else
				TimeUnit.MILLISECONDS.sleep(n);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
