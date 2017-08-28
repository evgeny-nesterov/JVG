package satis.iface.graph.axis;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import satis.iface.graph.DinamicPlot;
import satis.iface.graph.Group;
import satis.iface.graph.grid.Grid;
import satis.iface.graph.grid.GridRenderer;

public class LabelAxisMarkerRenderer implements AxisMarkerRenderer {
	public LabelAxisMarkerRenderer() {
	}

	public LabelAxisMarkerRenderer(Object format) {
		setFormat(format);
	}

	public LabelAxisMarkerRenderer(Color foreground, Color background) {
		setForeground(foreground);
		setBackground(background);
	}

	public LabelAxisMarkerRenderer(Object format, Color foreground, Color background) {
		setFormat(format);
		setForeground(foreground);
		setBackground(background);
	}

	@Override
	public void paintMarkerX(Graphics g, Group group, DinamicPlot plot, Axis axis, int coord, int w, int h) {
		Grid grid = axis.getGrid();
		Object format = (getFormat() != null) ? getFormat() : axis.getFormat();
		GridRenderer renderer = grid.getRenderer();
		Component c = renderer.getGridLabel(grid, group, renderer.formatValue(plot.getRealMarkerX(), format), false);
		if (c != null) {
			if (foreground != null) {
				c.setForeground(foreground);
			}
			c.setSize(c.getWidth() + 6, h);
			coord -= c.getWidth() / 2;

			g.translate(coord, 0);

			g.setColor((background != null) ? background : axis.getBackground());
			g.fillRect(0, 0, c.getWidth() - 1, c.getHeight() - 1);

			g.translate(3, 0);
			c.paint(g);
			g.translate(-3, 0);

			g.setColor(c.getForeground());
			g.drawRect(0, 0, c.getWidth() - 1, c.getHeight() - 1);

			g.translate(-coord, 0);
		}
	}

	@Override
	public void paintMarkerY(Graphics g, Group group, DinamicPlot plot, Axis axis, int coord, int w, int h) {
		Grid grid = axis.getGrid();
		Object format = (getFormat() != null) ? getFormat() : axis.getFormat();
		GridRenderer renderer = grid.getRenderer();
		Component c = renderer.getGridLabel(grid, group, renderer.formatValue(plot.getRealMarkerY(), format), false);
		if (c != null) {
			if (foreground != null) {
				c.setForeground(foreground);
			}
			c.setSize(w, c.getHeight() + 4);
			coord -= c.getHeight() / 2;

			g.translate(0, coord);

			g.setColor((background != null) ? background : axis.getBackground());
			g.fillRect(0, 0, c.getWidth() - 1, c.getHeight() - 1);

			g.translate(2, 0);
			c.paint(g);
			g.translate(-2, 0);

			g.setColor(c.getForeground());
			g.drawRect(0, 0, c.getWidth() - 1, c.getHeight() - 1);

			g.translate(0, -coord);
		}
	}

	private Object format = null;

	public void setFormat(Object format) {
		this.format = format;
	}

	public Object getFormat() {
		return format;
	}

	private Color foreground = null;

	public Color getForeground() {
		return foreground;
	}

	public void setForeground(Color foreground) {
		this.foreground = foreground;
	}

	private Color background = null;

	public Color getBackground() {
		return background;
	}

	public void setBackground(Color background) {
		this.background = background;
	}
}
