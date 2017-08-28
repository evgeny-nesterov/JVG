package javax.swing.menu;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

class WrappedIcon implements Icon {
	private Icon i, di;

	private final static Color transparentColor = new Color(0, 0, 0, 0);

	public WrappedIcon(Icon i) {
		this.i = i;
		if (i instanceof ImageIcon) {
			di = new ImageIcon(GrayFilter.createShadowImage(((ImageIcon) i).getImage()));
		}
		// else
		// {
		// BufferedImage bi = new BufferedImage(i.getIconWidth(),
		// i.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		// Graphics g = bi.getGraphics();
		// g.setColor(transparentColor);
		// g.fillRect(0, 0, i.getIconWidth(), i.getIconHeight());
		// i.paintIcon(null, g, 0, 0);
		//
		// di = new ImageIcon(GrayFilter.createShadowImage(bi));
		// }
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		JMenuItem mi = (JMenuItem) c;
		if (c.isEnabled()) {
			if (mi.isArmed()) {
				if (di != null) {
					di.paintIcon(c, g, 5, y + 1);
				}

				i.paintIcon(c, g, 3, y - 1);
			} else {
				i.paintIcon(c, g, 4, y);
			}
		} else {
			if (di != null) {
				di.paintIcon(c, g, 4, y);
			}
		}
	}

	@Override
	public int getIconWidth() {
		return i.getIconWidth();
	}

	@Override
	public int getIconHeight() {
		return i.getIconHeight();
	}
}
