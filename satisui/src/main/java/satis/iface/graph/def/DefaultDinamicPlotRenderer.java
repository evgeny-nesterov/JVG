package satis.iface.graph.def;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Stroke;

import satis.iface.graph.DinamicPlot;
import satis.iface.graph.DinamicPlotRenderer;
import satis.iface.graph.Group;
import satis.iface.graph.def.outliners.PlotOutliner;

public class DefaultDinamicPlotRenderer extends DefaultPlotRenderer implements DinamicPlotRenderer {
	public DefaultDinamicPlotRenderer() {
		this(Color.black, null, null, FILL_NONE);
	}

	public DefaultDinamicPlotRenderer(Color color) {
		this(color, null, null, FILL_NONE);
	}

	public DefaultDinamicPlotRenderer(Color color, Stroke stroke) {
		this(color, stroke, null, FILL_NONE);
	}

	public DefaultDinamicPlotRenderer(Color color, PlotOutliner plotOutliner, int fillType) {
		this(color, null, plotOutliner, fillType);
	}

	public DefaultDinamicPlotRenderer(Color color, Stroke stroke, PlotOutliner plotOutliner, int fillType) {
		super(color, stroke, plotOutliner, fillType);
	}

	private Color markerColor = Color.black;

	public Color getMarkerColor() {
		return markerColor;
	}

	public void setMarkerColor(Color markerColor) {
		this.markerColor = markerColor;
	}

	@Override
	public void paintMarker(Graphics g, Group group, DinamicPlot plot) {
		g.setColor(markerColor);
		g.fillArc((int) plot.getMarkerX() - 2, (int) plot.getMarkerY() - 2, 5, 5, 0, 360);

		// --- Draw side points ---
		if (plot.getMarkerX() != plot.getModel().getX(plot.getMarkerIndex())) {
			int x1 = plot.getModel().getX(plot.getMarkerIndex() - 1);
			int x2 = plot.getModel().getX(plot.getMarkerIndex());
			if (x2 - x1 > 5) {
				int y1 = plot.getModel().getY(plot.getMarkerIndex() - 1);
				int y2 = plot.getModel().getY(plot.getMarkerIndex());

				g.fillRect(x1 - 1, y1 - 1, 3, 3);
				g.fillRect(x2 - 1, y2 - 1, 3, 3);
			}
		}
	}
}
