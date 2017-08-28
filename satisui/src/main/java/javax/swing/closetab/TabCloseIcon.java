package javax.swing.closetab;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.UIManager;

public class TabCloseIcon implements Icon {
	/** the icon width */
	protected static final int ICON_WIDTH = 6;

	/** the icon height */
	protected static final int ICON_HEIGHT = 6;

	/** Creates a new instance of TabCloseButtonIcon */
	public TabCloseIcon() {
	}

	/**
	 * Returns the icon's height.
	 * 
	 * @return the height of the icon
	 */
	@Override
	public int getIconHeight() {
		return ICON_HEIGHT;
	}

	/**
	 * Returns the icon's width.
	 * 
	 * @return the width of the icon
	 */
	@Override
	public int getIconWidth() {
		return ICON_WIDTH;
	}

	/**
	 * Draw the icon at the specified location.
	 * 
	 * @param the
	 *            component
	 * @param the
	 *            graphics context
	 * @param x
	 *            coordinate
	 * @param y
	 *            coordinate
	 */
	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		g.setColor(UIManager.getColor("controlShadow").darker().darker());
		g.drawLine(x, y, x + ICON_WIDTH - 1, y + ICON_HEIGHT - 1);
		g.drawLine(x + ICON_WIDTH - 1, y, x, y + ICON_HEIGHT - 1);
	}
}
