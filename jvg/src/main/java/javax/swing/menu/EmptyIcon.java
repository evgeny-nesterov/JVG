package javax.swing.menu;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

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
