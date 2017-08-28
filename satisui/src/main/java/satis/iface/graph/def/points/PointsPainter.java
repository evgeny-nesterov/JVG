package satis.iface.graph.def.points;

import java.awt.Graphics;

import satis.iface.graph.PlotModel;

public interface PointsPainter {
	public void paintPoint(Graphics g, PlotModel model, int index);
}
