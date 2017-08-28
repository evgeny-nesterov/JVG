package javax.swing;

import java.awt.Toolkit;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

public class CharLimitedTextField extends JTextField {
	private static final long serialVersionUID = 1L;

	private int maxLength;

	private CharLimitedDocument charLimitedDocument;

	public CharLimitedTextField(int maxLength) {
		this.maxLength = maxLength;
		if (charLimitedDocument == null) {
			charLimitedDocument = new CharLimitedDocument();
		}
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}

	public int getMaxLength() {
		return maxLength;
	}

	@Override
	protected Document createDefaultModel() {
		if (charLimitedDocument == null) {
			charLimitedDocument = new CharLimitedDocument();
		}
		return charLimitedDocument;
	}

	class CharLimitedDocument extends PlainDocument {
		private static final long serialVersionUID = 1L;
		private Toolkit toolkit;

		public CharLimitedDocument() {
			toolkit = Toolkit.getDefaultToolkit();
		}

		@Override
		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
			if (getLength() >= maxLength) {
				toolkit.beep();
				return;
			}
			super.insertString(offs, str, a);
		}
	}
}
