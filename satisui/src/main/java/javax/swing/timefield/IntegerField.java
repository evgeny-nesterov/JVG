package javax.swing.timefield;

import java.awt.Color;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

public class IntegerField extends JTextField {
	private static final long serialVersionUID = 1L;

	public int maxSize, maxValue, minValue;

	public boolean isDrawNulls = false;

	private Border emptyBorder = BorderFactory.createEmptyBorder();

	public IntegerField(int maxSize, int minValue, int maxValue, boolean isDrawNulls) {
		this.maxSize = maxSize;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.isDrawNulls = isDrawNulls;

		setCaretColor(Color.black);
		setBorder(emptyBorder);
		setHorizontalAlignment(SwingConstants.RIGHT);
		setOpaque(false);

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

		if (isDrawNulls) {
			while (s.length() < maxSize) {
				s = "0" + s;
			}
		}

		super.setText(s);
		prevValue = s;
	}

	protected void apply() {
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
		setBorder(null);
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		super.setEnabled(isEnabled);
		if (!isEnabled) {
			setBorder(null);
		}
	}

	private String prevValue;
}
