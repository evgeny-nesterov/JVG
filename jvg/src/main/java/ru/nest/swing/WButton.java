package ru.nest.swing;

import javax.swing.*;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class WButton extends JButton {
	private boolean isSelected = false;

	private boolean isOver = false;

	public WButton() {
		this("");
	}

	public WButton(String text) {
		super(text);

		setOpaque(false);
		setContentAreaFilled(false);
		setBorderPainted(false);
		setBorder(null);
		setMargin(new Insets(1, 1, 1, 1));

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

	@Override
	public void paint(Graphics g) {
		if (isEnabled() && (isSelected || isOver)) {
			int W = getWidth();
			int H = getHeight();

			// draw background
			g.setColor(isSelected ? Constants.rolloverDarkBackground : Constants.rolloverBackground);
			g.fillRect(0, 0, W, H);

			// draw border
			g.setColor(Constants.rolloverOutlineBackground);
			g.drawRect(0, 0, W - 1, H - 1);
		}

		super.paint(g);
	}
}
