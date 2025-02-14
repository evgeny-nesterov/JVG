package ru.nest.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class IconButton extends JButton {
	private boolean isSelected = false;

	private boolean isOver = false;

	public IconButton() {
		this(null);
	}

	public IconButton(Icon icon) {
		this(icon, 1);
	}

	public IconButton(Icon icon, int margin) {
		this(icon, new Insets(margin, margin, margin, margin));
	}

	public IconButton(Icon icon, Insets margin) {
		setOpaque(false);
		setIcon(icon);
		setContentAreaFilled(false);
		setBorderPainted(false);
		setBorder(null);
		setMargin(margin);
		if (icon != null) {
			setPreferredSize(new Dimension(icon.getIconWidth() + margin.left + margin.right, icon.getIconHeight() + margin.top + margin.bottom));
		}

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (isEnabled()) {
					isSelected = true;
					repaint();
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				isSelected = false;
				if (isEnabled()) {
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
				isOver = false;
				if (isEnabled()) {
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
		repaint();
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

		if (isSelected) {
			x++;
			y++;
		}

		if (isEnabled() && (isSelected || isOver)) {
			// draw background
			g.setColor(isSelected ? Constants.rolloverDarkBackground : Constants.rolloverBackground);
			g.fillRect(0, 0, W, H);

			// draw border
			g.setColor(Constants.rolloverOutlineBackground);
			g.drawRect(0, 0, W - 1, H - 1);
		}

		// draw icon
		if (isEnabled() && icon != null) {
			icon.paintIcon(this, g, x, y);
		} else if (!isEnabled() && disabledIcon != null) {
			g.drawImage(disabledIcon, x, y, null);
		}
	}
}
