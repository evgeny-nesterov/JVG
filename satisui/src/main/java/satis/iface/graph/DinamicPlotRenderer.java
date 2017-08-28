package satis.iface.graph;

import java.awt.Graphics;

public interface DinamicPlotRenderer extends PlotRenderer {
	public void paintMarker(Graphics g, Group group, DinamicPlot plot);
}
