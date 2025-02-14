package ru.nest.swing;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class IntegerTextField extends JTextField {
	protected char[] allowed1 = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

	protected char[] allowed2 = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-' };

	protected boolean mNeg = false; // allow negative numbers

	protected boolean mAllowLeadingZero = true;

	public IntegerTextField() {
		super();
		init();
	}

	public IntegerTextField(String s, int d) {
		super(s, d);
		init();
	}

	public IntegerTextField(int value, boolean neg) {
		super(Integer.toString(value));
		mNeg = neg;
		init();
	}

	public IntegerTextField(int value, int d, boolean neg) {
		super(Integer.toString(value), d);
		mNeg = neg;
		init();
	}

	public IntegerTextField(String s, int d, boolean neg) {
		super(s, d);
		mNeg = neg;
		init();
	}

	private void init() {
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				char c = e.getKeyChar();
				if (!mAllowLeadingZero && c == KeyEvent.VK_0 && getSelectionStart() == 0) {
					e.consume();
				} else if ((c != KeyEvent.VK_BACK_SPACE) && (c != KeyEvent.VK_DELETE)) {
					if (!match(c)) {
						e.consume();
					}
				}
			}
		});
	}

	protected boolean match(char c) {
		char allowed[] = mNeg && getSelectionStart() == 0 ? allowed2 : allowed1;
		for (int i = 0; i < allowed.length; i++) {
			if (c == allowed[i]) {
				return true;
			}
		}
		return false;
	}

	public void setAllowNegative(boolean b) {
		mNeg = b;
	}

	public void setAllowLeadingZero(boolean b) {
		mAllowLeadingZero = b;
	}
}
