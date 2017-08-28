package satis.iface.graph.grid;

import java.awt.Graphics;

import javax.swing.JComponent;

import satis.iface.graph.Group;

public interface GridRenderer {
	public JComponent getGridLabel(Grid grid, Group group, Object formattedValue, boolean isLast);

	public Object formatValue(double val, Object format);

	public void paintGridLine(Grid grid, Graphics g, int x, int y, int w, int h, int index);
}
