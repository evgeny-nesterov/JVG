package ru.nest.jvg.editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JLabel;

import ru.nest.fonts.Fonts;

public class JVGRowHeader extends JLabel {
	private JVGEditPane pane;

	private NumberFormat f = new DecimalFormat("0.#");

	private Integer markerPosition;

	public JVGRowHeader(JVGEditPane pane) {
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
		return new Dimension(18, pane.getHeight());
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		Graphics2D g2d = (Graphics2D) g;
		int increment = (int) pane.getIncrement();
		if (increment == -1) {
			increment = 10;
		}
		int delta = increment;
		while (delta < 40) {
			delta *= 2;
		}

		int w = getWidth() - 1, h = getHeight();
		if (markerPosition != null) {
			g.setColor(Color.blue);
			g.drawLine(0, markerPosition, w, markerPosition);
		}

		g.setColor(Color.black);
		g.drawLine(w, 0, w, h);

		double y = 0, v = -pane.getDocumentInsets().bottom;
		y += y % delta;
		v += v % delta;

		double scale = pane.getScaleY();
		y *= scale;
		double ydelta = scale * delta;
		while (ydelta > 80) {
			ydelta /= 2;
			delta /= 2;
		}

		for (; y < h; y += ydelta, v += delta) {
			g.drawLine(w - 1, (int) y, w, (int) y);

			double d = ydelta / 4;
			g.drawLine(w - 1, (int) (y + d), w, (int) (y + d));
			d = ydelta / 2;
			g.drawLine(w - 3, (int) (y + d), w, (int) (y + d));
			d = 3 * ydelta / 4;
			g.drawLine(w - 1, (int) (y + d), w, (int) (y + d));
		}

		g2d.rotate(-Math.PI / 2, getHeight() / 2.0, getHeight() / 2.0);
		double x = 0;
		y = g.getFontMetrics().getHeight() - 3;
		v = -pane.getDocumentInsets().bottom;
		for (; x < h; x += ydelta, v += delta) {
			String s = f.format(v);
			int textWidth = g.getFontMetrics().stringWidth(s);
			g.drawString(s, (int) (x - textWidth / 2.0), (int) y);
		}

		Rectangle projectionBounds = pane.getProjectionBounds();
		if (projectionBounds != null) {
			g.setColor(Color.blue);

			int px = getHeight() - projectionBounds.y;
			g.drawLine(px, 0, px, getWidth());

			px = getHeight() - (projectionBounds.y + projectionBounds.height / 2);
			g.drawLine(px, 0, px, getWidth());

			px = getHeight() - (projectionBounds.y + projectionBounds.height);
			g.drawLine(px, 0, px, getWidth());
		}
	}

	public Integer getMarkerPosition() {
		return markerPosition;
	}

	public void setMarkerPosition(Integer markerPosition) {
		this.markerPosition = markerPosition;
	}
}
