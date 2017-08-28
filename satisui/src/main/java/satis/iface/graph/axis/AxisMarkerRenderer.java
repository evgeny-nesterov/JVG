package satis.iface.graph.axis;

import java.awt.Graphics;

import satis.iface.graph.DinamicPlot;
import satis.iface.graph.Group;

public interface AxisMarkerRenderer {
	public void paintMarkerX(Graphics g, Group group, DinamicPlot plot, Axis axis, int coord, int w, int h);

	public void paintMarkerY(Graphics g, Group group, DinamicPlot plot, Axis axis, int coord, int w, int h);
}
