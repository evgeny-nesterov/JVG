package javax.swing;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

import javax.swing.plaf.basic.BasicToolTipUI;

public class AcceleratorToolTipUI extends BasicToolTipUI {

	private static String delimiter = "+";

	public AcceleratorToolTipUI() {
		super();
	}

	@Override
	public void paint(Graphics g, JComponent c) {
		Font font = c.getFont();
		FontMetrics metrics = c.getFontMetrics(font);

		Dimension size = c.getSize();
		if (c.isOpaque()) {
			g.setColor(c.getBackground());
			g.fillRect(0, 0, size.width + 20, size.height);
		}

		g.setColor(c.getForeground());
		g.setFont(font);

		JToolTip tip = (JToolTip) c;
		String keyText = getAccelerator(tip);

		if (keyText != null && keyText.length() > 0) {
			Insets insets = c.getInsets();
			Rectangle paintTextR = new Rectangle(insets.left, insets.top, size.width - (insets.left + insets.right), size.height - (insets.top + insets.bottom));
			g.drawString(keyText, paintTextR.x + 3, paintTextR.y + metrics.getAscent());
		}
	}

	@Override
	public Dimension getPreferredSize(JComponent c) {
		Dimension d = super.getPreferredSize(c);

		JToolTip tip = (JToolTip) c;
		String keyText = getAccelerator(tip);

		if (keyText != null && keyText.length() > 0) {
			Font font = c.getFont();
			FontMetrics fm = c.getFontMetrics(font);
			d.width = fm.stringWidth(keyText) + 8;
		}
		return d;
	}

	private String getAccelerator(JToolTip tip) {
		String text = tip.getTipText();
		if (text == null) {
			text = "";
		}

		Action action = ((AbstractButton) tip.getComponent()).getAction();
		if (action != null) {
			String modText = null;
			KeyStroke keyStroke = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);

			if (keyStroke != null) {
				int mod = keyStroke.getModifiers();
				modText = KeyEvent.getKeyModifiersText(mod);

				if (modText != null && modText.length() > 0) {
					modText += delimiter;
				}

				String keyText = KeyEvent.getKeyText(keyStroke.getKeyCode());
				if (keyText != null && keyText.length() > 0) {
					modText += keyText;
				}

			}

			if (modText != null && modText.length() > 0) {
				text = text + "  (" + modText + ")";
			}
		}
		return text;
	}
}
