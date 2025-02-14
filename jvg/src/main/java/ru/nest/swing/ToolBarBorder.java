package ru.nest.swing;

import javax.swing.border.Border;
import java.awt.*;

public class ToolBarBorder implements Border {
	private Insets insets = new Insets(0, 6, 0, 0);

	@Override
	public Insets getBorderInsets(Component c) {
		return insets;
	}

	@Override
	public boolean isBorderOpaque() {
		return false;
	}

	private final static Color color = new Color(180, 180, 180);

	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		g.setColor(color);
		for (int i = 2; i < height - 2; i += 2) {
			g.drawLine(4, i, 6, i);
		}
	}
}
