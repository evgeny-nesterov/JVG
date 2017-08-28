package satis.iface.graph.paintobjects;

import java.awt.Graphics;

import satis.iface.graph.Group;

public class Polyline extends Poly {
	public Polyline(double[] X, double[] Y) {
		super(X, Y);
	}

	@Override
	public void paint(Group group, Graphics g) {
		g.setColor(color);
		g.drawPolyline(x, y, count);
	}
}
