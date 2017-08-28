package satis.iface.graph.paintobjects;

import java.awt.Font;
import java.awt.Graphics;

import javax.swing.SwingUtilities;

import satis.iface.graph.Group;

public class Text extends AbstractPaintObject {
	public final static int LEFT = 0;

	public final static int CENTER = 1;

	public final static int RIGHT = 2;

	String text;

	double X, Y;

	int x, y;

	public Text(String text, double X, double Y) {
		this.text = text;
		this.X = X;
		this.Y = Y;
	}

	@Override
	public void compile(Group group) {
		x = (int) group.modelToViewX(X);
		y = (int) group.modelToViewY(Y);
	}

	Font font = null;

	public void setFont(Font font) {
		this.font = font;
	}

	public Font getFont() {
		return font;
	}

	int horizontalAlignment = CENTER;

	public void setHorizontalAlignment(int horizontalAlignment) {
		this.horizontalAlignment = horizontalAlignment;
	}

	public int setHorizontalAlignment() {
		return horizontalAlignment;
	}

	@Override
	public void paint(Group group, Graphics g) {
		if (text != null) {
			if (font != null) {
				g.setFont(font);
			}

			if (color != null) {
				g.setColor(color);
			}

			int x0 = x;
			if (horizontalAlignment == CENTER) {
				x0 -= SwingUtilities.computeStringWidth(g.getFontMetrics(), text) / 2;
			} else if (horizontalAlignment == RIGHT) {
				x0 -= SwingUtilities.computeStringWidth(g.getFontMetrics(), text);
			}

			g.drawString(text, x0, y);
		}
	}
}
