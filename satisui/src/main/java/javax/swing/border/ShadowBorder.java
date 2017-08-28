package javax.swing.border;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

public class ShadowBorder implements Border {
	public ShadowBorder() {
	}

	public ShadowBorder(int shadowSize) {
		setShadowSize(shadowSize);
	}

	public ShadowBorder(int shadowSize, Color shadowColor) {
		setShadowSize(shadowSize);
		this.shadowColor = shadowColor;
	}

	private Color shadowColor = Color.darkGray;

	public void setShadowColor(Color shadowColor) {
		this.shadowColor = shadowColor;
	}

	public Color getShadowColor() {
		return shadowColor;
	}

	private int shadowSize = 5;

	public void setShadowSize(int shadowSize) {
		this.shadowSize = shadowSize;
		insets = new Insets(0, 0, shadowSize, shadowSize);
	}

	public int getShadowSize() {
		return shadowSize;
	}

	private Insets insets = new Insets(0, 0, shadowSize, shadowSize);

	@Override
	public Insets getBorderInsets(Component c) {
		return insets;
	}

	@Override
	public boolean isBorderOpaque() {
		return false;
	}

	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		g.setColor(shadowColor);
		g.fillRect(shadowSize, height - shadowSize, width - shadowSize, shadowSize);
		g.fillRect(width - shadowSize, shadowSize, shadowSize, height - 2 * shadowSize);
	}
}
