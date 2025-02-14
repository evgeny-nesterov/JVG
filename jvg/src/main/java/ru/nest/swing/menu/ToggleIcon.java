package ru.nest.swing.menu;

import ru.nest.swing.Constants;

import javax.swing.*;
import java.awt.*;

public class ToggleIcon implements Icon {
	private Icon icon, di;

	public ToggleIcon(Icon icon) {
		this.icon = icon;
		if (icon instanceof ImageIcon) {
			di = new ImageIcon(GrayFilter.createShadowImage(((ImageIcon) icon).getImage()));
		}
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		AbstractButton btn = (AbstractButton) c;
		x = 3;

		// draw border
		if (btn.isSelected()) {
			g.setColor(btn.isEnabled() ? Constants.rolloverOutlineBackground : Color.gray);
			g.drawRect(x - 2, y - 2, 20, 20);
		}

		if (btn.isEnabled() && icon != null) {
			icon.paintIcon(c, g, x, y);
		} else if (!btn.isEnabled() && di != null) {
			di.paintIcon(c, g, x, y);
		}
	}

	@Override
	public int getIconWidth() {
		return icon.getIconWidth();
	}

	@Override
	public int getIconHeight() {
		return icon.getIconHeight();
	}
}
