package satis.iface.graph.def;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.text.DateFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.util.Date;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import satis.iface.graph.Group;
import satis.iface.graph.grid.Grid;
import satis.iface.graph.grid.GridRenderer;

public class DefaultGridRenderer extends JLabel implements GridRenderer {
	private static final long serialVersionUID = 1L;

	private Date date = new Date();

	public DefaultGridRenderer() {
		setOpaque(false);
		setLabelColor(Color.black);
		setHorizontalTextPosition(SwingConstants.RIGHT);
		setLocation(0, 0);
	}

	private boolean isPaintGridLine = true;

	public boolean isPaintGridLine() {
		return isPaintGridLine;
	}

	public void setPaintGridLine(boolean isPaintGridLine) {
		this.isPaintGridLine = isPaintGridLine;
	}

	private boolean isLast;

	private FontRenderContext frc = new FontRenderContext(null, false, false);

	@Override
	public JComponent getGridLabel(Grid grid, Group group, Object formattedValue, boolean isLast) {
		this.isLast = isLast;
		String value = formattedValue.toString();

		Font font = getFont();
		if (font == null) {
			font = new Font("SanSerif", 0, 11);
			setFont(font);
		}
		Rectangle2D bounds = font.getStringBounds(value, frc);

		setText(value);
		setOpaque(false);
		setPreferredSize(new Dimension((int) bounds.getWidth(), (int) bounds.getHeight()));
		setSize(getPreferredSize());
		setLabelColor(labelColor);
		return this;
	}

	@Override
	public Object formatValue(double val, Object format) {
		if (format instanceof satis.iface.Format) {
			return ((satis.iface.Format) format).form(val);
		} else if (format instanceof Format) {
			date.setTime((long) val);
			return ((DateFormat) format).format(date);
		} else if (format instanceof NumberFormat) {
			return ((NumberFormat) format).format(val);
		}

		// On default
		return Double.toString(val);
	}

	@Override
	public void paintGridLine(Grid gr, Graphics G, int x, int y, int w, int h, int index) {
		if (!isPaintGridLine && !(gr instanceof DefaultGrid)) {
			return;
		}

		DefaultGrid grid = (DefaultGrid) gr;
		Graphics2D g = (Graphics2D) G;
		Stroke oldStroke = g.getStroke();

		if (grid.isMainGridLine(index)) {
			g.setStroke(strokeMain);
		} else {
			g.setStroke(stroke);
		}
		g.setColor(gridColor != null ? gridColor : Color.black);
		g.drawLine(x, y, x + w, y + h);

		g.setStroke(oldStroke);
	}

	private Stroke stroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f, new float[] { 3f, 5f }, 0f);

	public void setStroke(Stroke stroke) {
		this.stroke = stroke;
	}

	private static Stroke strokeMain = new BasicStroke(1);

	public void setMainStroke(Stroke strokeMain) {
		DefaultGridRenderer.strokeMain = strokeMain;
	}

	private Color gridColor = Color.black;

	public Color getGridColor() {
		return gridColor;
	}

	public void setGridColor(Color gridColor) {
		if (labelColor == null) {
			setForeground(gridColor != null ? gridColor : Color.black);
		}
		this.gridColor = gridColor;
	}

	private Color labelColor;

	public Color getLabelColor() {
		return labelColor;
	}

	public void setLabelColor(Color labelColor) {
		setForeground(labelColor != null ? labelColor : (gridColor != null ? gridColor : Color.black));
		this.labelColor = labelColor;
	}
}
