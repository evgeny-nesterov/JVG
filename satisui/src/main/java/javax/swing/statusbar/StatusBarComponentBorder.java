package javax.swing.statusbar;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.GUIUtils;
import javax.swing.border.Border;

public class StatusBarComponentBorder implements Border {
	/** the border colour */
	private Color borderColour;

	/** the border insets */
	private static final Insets insets = new Insets(1, 1, 1, 0);

	@Override
	public Insets getBorderInsets(Component c) {
		return insets;
	}

	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		if (borderColour == null) {
			borderColour = GUIUtils.getDefaultBorderColour();
		}
		g.setColor(borderColour);
		// top edge
		g.drawLine(x, y, width, y);
		// bottom edge
		g.drawLine(x, height - 1, width, height - 1);
		// left edge
		g.drawLine(x, 0, x, height - 1);
	}

	@Override
	public boolean isBorderOpaque() {
		return false;
	}
}
