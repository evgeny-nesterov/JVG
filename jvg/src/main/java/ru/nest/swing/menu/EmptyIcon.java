package ru.nest.swing.menu;

import javax.swing.*;
import java.awt.*;

public class EmptyIcon implements Icon {
	private int w = 16;

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		// paint nothing
	}

	@Override
	public int getIconWidth() {
		return w;
	}

	@Override
	public int getIconHeight() {
		return w;
	}
}
