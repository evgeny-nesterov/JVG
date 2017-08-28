package satis.iface.graph.axis;

import java.awt.Color;
import java.awt.Graphics;

import satis.iface.graph.DinamicPlot;
import satis.iface.graph.Group;
import satis.iface.graph.MarkerListener;
import satis.iface.graph.Plot;

public class DinamicAxis extends Axis implements MarkerListener {
	private static final long serialVersionUID = 1L;

	public final static int SHOW_VALUE = 1;

	public final static int SHOW_MARKER = 2;

	private boolean paint = false;

	public DinamicAxis(Group group, int orientation) {
		super(group, orientation);
	}

	public DinamicAxis(Group group, int orientation, AxisMarkerRenderer renderer) {
		super(group, orientation);
		setMarkerRenderer(renderer);
	}

	@Override
	public void paintAxisX(Graphics g, Group group) {
		super.paintAxisX(g, group);

		if (paint && group.getPlotsCount() > 0) {
			int len = group.getPlotsCount();
			for (int i = 0; i < len; i++) {
				Plot p = group.getPlot(i);
				if (p instanceof DinamicPlot) {
					DinamicPlot plot = (DinamicPlot) p;
					if (plot.getRealMarkerX() != Double.MAX_VALUE && plot.getRealMarkerX() != Double.NaN) {
						int coord = (int) ((getWidth() - 1) * (plot.getRealMarkerX() - group.getInnerBounds().x) / group.getInnerBounds().w);
						getMarkerRenderer().paintMarkerX(g, group, plot, this, coord, getWidth(), getHeight());
					}
					return;
				}
			}
		}
	}

	@Override
	public void paintAxisY(Graphics g, Group group) {
		super.paintAxisY(g, group);

		if (paint && group.getPlotsCount() > 0) {
			g.setColor(Color.black);

			int len = group.getPlotsCount();
			for (int i = 0; i < len; i++) {
				Plot p = group.getPlot(i);
				if (p instanceof DinamicPlot) {
					DinamicPlot plot = (DinamicPlot) p;
					if (plot.getRealMarkerY() != Double.MAX_VALUE && plot.getRealMarkerY() != Double.NaN) {
						int coord = (int) ((getHeight() - 1) * (1 - (plot.getRealMarkerY() - group.getInnerBounds().y) / group.getInnerBounds().h));
						if (coord != Integer.MAX_VALUE && coord != Integer.MIN_VALUE) {
							getMarkerRenderer().paintMarkerY(g, group, plot, this, coord, getWidth(), getHeight());
						}
					}
				}
			}
		}
	}

	@Override
	public void markersMoved() {
		paint = true;
		repaint();
	}

	@Override
	public void markersHided() {
		paint = false;
		repaint();
	}

	private AxisMarkerRenderer renderer = null;

	public AxisMarkerRenderer getMarkerRenderer() {
		if (renderer == null) {
			renderer = new DefaultAxisMarkerRenderer();
		}

		return renderer;
	}

	public void setMarkerRenderer(AxisMarkerRenderer renderer) {
		this.renderer = renderer;
	}
}
