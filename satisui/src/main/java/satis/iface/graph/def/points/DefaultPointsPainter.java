package satis.iface.graph.def.points;

import java.awt.Color;
import java.awt.Graphics;

import satis.iface.graph.PlotModel;

public class DefaultPointsPainter implements PointsPainter {
	public DefaultPointsPainter() {

	}

	private Color color = Color.black;

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		if (color == null) {
			color = Color.black;
		}

		this.color = color;
	}

	@Override
	public void paintPoint(Graphics g, PlotModel model, int index) {
		g.setColor(Color.black);
		g.drawArc(model.getX(index) - 2, model.getY(index) - 2, 5, 5, 0, 360);
	}
}
