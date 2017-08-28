package satis.iface.graph.paintobjects;

import java.awt.Color;

public abstract class AbstractPaintObject implements PaintObject {
	protected boolean isPaintOver = false;

	public void setPaintOver(boolean isPaintOver) {
		this.isPaintOver = isPaintOver;
	}

	@Override
	public boolean isPaintOver() {
		return isPaintOver;
	}

	protected Color color = Color.black;

	public void setColor(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	protected boolean isVisible = true;

	@Override
	public boolean isVisible() {
		return isVisible;
	}

	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}
}
