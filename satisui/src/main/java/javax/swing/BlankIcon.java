package javax.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

public class BlankIcon implements Icon {
	private Color fillColor;

	private int size;

	public BlankIcon() {
		this(null, 11);
	}

	public BlankIcon(int size) {
		this(null, size);
	}

	public BlankIcon(Color color, int size) {
		fillColor = color;
		this.size = size;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		if (fillColor != null) {
			g.setColor(fillColor);
			g.fillRect(x, y, size - 1, size - 1);
		}
	}

	@Override
	public int getIconWidth() {
		return size;
	}

	@Override
	public int getIconHeight() {
		return size;
	}
}
