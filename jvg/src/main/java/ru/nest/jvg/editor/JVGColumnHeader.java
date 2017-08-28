package ru.nest.jvg.editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JLabel;

import ru.nest.fonts.Fonts;

public class JVGColumnHeader extends JLabel {
	private NumberFormat f = new DecimalFormat("0.#");

	private JVGEditPane pane;

	public JVGColumnHeader(JVGEditPane pane) {
		this.pane = pane;
		setOpaque(true);
		setBackground(new Color(255, 255, 255));
		setFont(Fonts.getFont("Monospaced", Font.PLAIN, 11));

		pane.addPropertyChangeListener("projection-bounds", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				repaint();
			}
		});
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(pane.getWidth(), 18);
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		double increment = pane.getIncrement();
		if (increment == -1) {
			increment = 10;
		}
		double delta = increment;
		while (delta < 40) {
			delta *= 2;
		}

		int h = getHeight() - 1, w = getWidth();
		g.setColor(Color.black);
		g.drawLine(0, h, w, h);

		double x = 0, v = -pane.getDocumentInsets().left, y = g.getFontMetrics().getHeight() - 3;
		x += x % delta;
		v += v % delta;

		double scale = pane.getScaleX();
		x *= scale;
		double xdelta = scale * delta;
		while (xdelta > 80) {
			xdelta /= 2;
			delta /= 2;
		}

		for (; x < w; x += xdelta, v += delta) {
			String s = f.format(v);
			int textWidth = g.getFontMetrics().stringWidth(s);
			g.drawString(s, (int) (x - textWidth / 2), (int) y);

			g.drawLine((int) x, h, (int) x, h - 3);
			g.drawLine((int) (x + xdelta / 2), h, (int) (x + xdelta / 2), h - 1);
		}

		Rectangle projectionBounds = pane.getProjectionBounds();
		if (projectionBounds != null) {
			g.setColor(Color.blue);

			int px = projectionBounds.x;
			g.drawLine(px, 0, px, getHeight());

			px = projectionBounds.x + projectionBounds.width / 2;
			g.drawLine(px, 0, px, getHeight());

			px = projectionBounds.x + projectionBounds.width;
			g.drawLine(px, 0, px, getHeight());
		}
	}
}
