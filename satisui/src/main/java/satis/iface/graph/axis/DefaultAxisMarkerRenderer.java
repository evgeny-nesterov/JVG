package satis.iface.graph.axis;

import java.awt.Color;
import java.awt.Graphics;

import satis.iface.graph.DinamicPlot;
import satis.iface.graph.Group;

public class DefaultAxisMarkerRenderer implements AxisMarkerRenderer {
	public DefaultAxisMarkerRenderer() {

	}

	public DefaultAxisMarkerRenderer(Color color) {
		setColor(color);
	}

	private int[] x = new int[4];

	private int[] y = new int[4];

	@Override
	public void paintMarkerX(Graphics g, Group group, DinamicPlot plot, Axis axis, int coord, int w, int h) {
		g.setColor((color != null) ? color : Color.black);
		x[0] = coord - 2;
		x[1] = coord + 2;
		x[2] = coord;
		x[3] = x[0];
		y[0] = h - 1;
		y[1] = h - 1;
		y[2] = h - 3;
		y[3] = y[0];
		g.drawPolyline(x, y, 4);
	}

	@Override
	public void paintMarkerY(Graphics g, Group group, DinamicPlot plot, Axis axis, int coord, int w, int h) {
		g.setColor((color != null) ? color : Color.black);
		x[0] = 0;
		x[1] = 0;
		x[2] = 2;
		x[3] = x[0];
		y[0] = coord - 2;
		y[1] = coord + 2;
		y[2] = coord;
		y[3] = y[0];
		g.drawPolyline(x, y, 4);
	}

	private Color color = null;

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}
}
