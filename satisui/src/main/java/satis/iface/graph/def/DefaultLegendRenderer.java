package satis.iface.graph.def;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.Iterator;

import javax.swing.JLabel;

import satis.iface.graph.Group;
import satis.iface.graph.Plot;
import satis.iface.graph.PlotRenderer;
import satis.iface.graph.StaticGraphArea;
import satis.iface.graph.legend.Legend;
import satis.iface.graph.legend.LegendRenderer;

public class DefaultLegendRenderer extends JLabel implements LegendRenderer {
	private static final long serialVersionUID = 1L;

	private StaticGraphArea graph;

	private static int lineWidth = 20;

	public DefaultLegendRenderer(StaticGraphArea graph) {
		this.graph = graph;
		setOpaque(false);
	}

	@Override
	public int getRows() {
		int rows = 0;

		Iterator<Group> iter = graph.getGroups();
		while (iter.hasNext()) {
			Group group = iter.next();
			rows += group.getPlotsCount();
		}

		return rows;
	}

	private Plot plot;

	public Plot getPlot(int row) {
		int rows = 0;
		Iterator<Group> iter = graph.getGroups();
		while (iter.hasNext()) {
			Group group = iter.next();
			int r = rows + group.getPlotsCount();
			if (row >= rows && row < rows + r) {
				return group.getPlot(row - rows);
			}
			rows = r;
		}

		return null;
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension size = super.getPreferredSize();
		size.width += lineWidth;
		return size;
	}

	@Override
	public Component getLegendComponent(Legend legend, int row) {
		plot = getPlot(row);
		if (plot != null) {
			setText("  (" + row + ") " + plot.getTitle() + "  ");
		}

		return this;
	}

	private Stroke stroke = new BasicStroke(3);

	@Override
	public void paint(Graphics g) {
		if (plot != null) {
			Graphics2D G = (Graphics2D) g;
			Stroke oldStroke = G.getStroke();
			G.setStroke(stroke);
			PlotRenderer pb = plot.getPlotRenderer();
			G.setColor(pb.getPlotColor());
			int h = getHeight() / 2;
			G.drawLine(5, h, lineWidth, h);
			G.translate(lineWidth, 0);
			G.setStroke(oldStroke);
		}

		super.paint(g);

		if (plot != null) {
			g.translate(-lineWidth, 0);
		}
	}
}
