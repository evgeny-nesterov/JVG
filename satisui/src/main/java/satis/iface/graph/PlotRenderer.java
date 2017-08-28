package satis.iface.graph;

import java.awt.Color;
import java.awt.Graphics;

public interface PlotRenderer {
	public void paintPlot(Graphics g, Group group, Plot plot);

	public Color getPlotColor();
}
