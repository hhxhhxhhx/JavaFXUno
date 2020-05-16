package classes;

import javafx.scene.control.TextField;

/**
 * TextField but you can only input numbers and decimals
 * @author Roger
 *
 */
public class NumberTextField extends TextField {
	
	private boolean allowDecimals = false;
	
	public NumberTextField(boolean b) {
		allowDecimals = b;
	}
	
	public NumberTextField(int n) {
		this.setText(n + "");
		this.setStyle("-fx-font-size: 16px;");
	}
	
	@Override
	public void replaceText(int start, int end, String text) {
		if (validate(text)) {
			super.replaceText(start, end, text);
		}
	}

	@Override
	public void replaceSelection(String text) {
		if (validate(text)) {
			super.replaceSelection(text);
		}
	}

	private boolean validate(String text) {
		if (allowDecimals)
			return text.replaceAll(".", "").matches("[0-9]*");
		return text.matches("[0-9]*");
	}
}