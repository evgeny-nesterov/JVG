package javax.swing;

import java.awt.Toolkit;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

public class NumberField extends JTextField {
	private static final long serialVersionUID = 1L;
	private NumberFormat format;

	public NumberField() {
		this(NumberFormat.getIntegerInstance());
	}

	public NumberField(NumberFormat format) {
		this.format = format;
	}

	public NumberField(int cols) {
		super(cols);
	}

	public Number getNumber() {
		final String text = getText();
		if (text == null || text.length() == 0) {
			return null;
		}

		try {
			return format.parse(text);
		} catch (ParseException exc) {
			exc.printStackTrace();
		}
		return null;
	}

	public void setNumber(Number value) {
		setText(format.format(value));
	}

	@Override
	protected Document createDefaultModel() {
		return new NumberDocument();
	}

	class NumberDocument extends PlainDocument {
		private static final long serialVersionUID = 1L;

		@Override
		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
			if (str != null) {
				try {
					format.parse(str);
					super.insertString(offs, str, a);
				} catch (ParseException ex) {
					Toolkit.getDefaultToolkit().beep();
				}
			}
		}
	}
}
