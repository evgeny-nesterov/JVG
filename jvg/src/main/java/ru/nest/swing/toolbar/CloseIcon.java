package ru.nest.swing.toolbar;

import javax.swing.*;
import java.awt.*;

public class CloseIcon implements Icon {
	private int size;

	public CloseIcon(int size) {
		this.size = size;
	}

	@Override
	public int getIconHeight() {
		return size;
	}

	@Override
	public int getIconWidth() {
		return size;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		g.setColor(Color.white);
		int w = size - 2;
		g.drawLine(x, y, x + w, y + w);
		g.drawLine(x, y + w, x + w, y);
	}
}
