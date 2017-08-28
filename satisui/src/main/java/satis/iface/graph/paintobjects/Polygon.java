package satis.iface.graph.paintobjects;

import java.awt.Graphics;

import satis.iface.graph.Group;

public class Polygon extends Poly {
	public Polygon(double[] X, double[] Y) {
		super(X, Y);
	}

	boolean isFill = false;

	public void setFill(boolean isFill) {
		this.isFill = isFill;
	}

	public boolean isFill() {
		return isFill;
	}

	@Override
	public void paint(Group group, Graphics g) {
		g.setColor(color);
		if (isFill) {
			g.fillPolygon(x, y, count);
		} else {
			g.drawPolygon(x, y, count);
		}
	}
}
