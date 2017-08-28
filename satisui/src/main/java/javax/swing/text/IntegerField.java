package javax.swing.text;

import java.awt.Color;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class IntegerField extends JTextField {
	private static final long serialVersionUID = 1L;
	public int maxValue, minValue;

	public IntegerField(int columns, int minValue, int maxValue) {
		super(columns);

		this.minValue = minValue;
		this.maxValue = maxValue;

		setCaretColor(Color.black);
		setHorizontalAlignment(SwingConstants.RIGHT);

		addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
			}

			@Override
			public void focusLost(FocusEvent e) {
				apply();
			}
		});

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				int code = e.getKeyCode();
				char ch = e.getKeyChar();

				if (code == KeyEvent.VK_ENTER) {
					KeyboardFocusManager.getCurrentKeyboardFocusManager().upFocusCycle();
				} else if (code != KeyEvent.VK_BACK_SPACE && code != KeyEvent.VK_DELETE) {
					String text = getText().trim();
					if (ch == '-' && getSelectionStart() == 0) {

					} else {
						try {
							Integer.parseInt(text + ch);
						} catch (NumberFormatException ex) {
							e.consume();
						}
					}
				}
			}
		});
	}

	@Override
	public void setText(String s) {
		try {
			Integer.parseInt(s);
		} catch (NumberFormatException ex) {
			return;
		}

		super.setText(s);
		prevValue = s;
	}

	private void apply() {
		int val = minValue;
		try {
			val = Integer.parseInt(getText());
		} catch (Exception exc) {
			setText(prevValue);
			return;
		}

		if (val > maxValue) {
			val = maxValue;
		}

		if (val < minValue) {
			val = minValue;
		}

		setText(Integer.toString(val));
	}

	private String prevValue;
}
