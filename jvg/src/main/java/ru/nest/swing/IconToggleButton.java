package ru.nest.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class IconToggleButton extends JToggleButton {
	private static Color disableColor = new Color(200, 200, 200, 128);

	private boolean isOver = false;

	private boolean isPressed = false;

	public IconToggleButton(Icon icon) {
		this.icon = icon;
		setOpaque(false);
		setIcon(icon);
		setContentAreaFilled(false);
		setBorderPainted(false);

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (isEnabled()) {
					isPressed = true;
					repaint();
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (isEnabled()) {
					isPressed = false;
					repaint();
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				if (isEnabled()) {
					isOver = true;
					repaint();
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				if (isEnabled()) {
					isOver = false;
					repaint();
				}
			}
		});
	}

	private Icon icon;

	private Image disabledIcon;

	@Override
	public void setIcon(Icon icon) {
		super.setIcon(icon);
		this.icon = icon;
		if (icon != null) {
			disabledIcon = Util.createDisabledImage(icon);
		}
	}

	@Override
	public void paint(Graphics g) {
		int w = 0;
		int h = 0;
		if (icon != null) {
			w = icon.getIconWidth();
			h = icon.getIconHeight();
		}
		int W = getWidth();
		int H = getHeight();
		int x = (W - w) / 2;
		int y = (H - h) / 2;

		// draw background
		if (isEnabled() && (isPressed || isOver)) {
			g.setColor((isPressed || isSelected()) ? Constants.rolloverDarkBackground : Constants.rolloverBackground);
			g.fillRect(0, 0, W, H);
		}

		// draw border
		if (isSelected() || (isEnabled() && isOver)) {
			g.setColor(isEnabled() ? Constants.rolloverOutlineBackground : Color.gray);
			g.drawRect(0, 0, W - 1, H - 1);
		}

		if (isEnabled() && icon != null) {
			icon.paintIcon(this, g, x, y);
		} else if (!isEnabled() && disabledIcon != null) {
			g.drawImage(disabledIcon, x, y, null);
		}
	}
}
