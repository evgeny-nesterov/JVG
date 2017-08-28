package satis.iface.graph;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.JPanel;
import javax.swing.border.Border;

import satis.iface.graph.grid.Grid;

public class AxisLayout implements LayoutManager {
	private static Dimension nullSize = new Dimension(0, 0);

	private JPanel panel;

	private int axis;

	public AxisLayout(JPanel panel, int axis) {
		this.panel = panel;
		this.axis = axis;
	}

	@Override
	public void layoutContainer(Container parent) {
		synchronized (parent.getTreeLock()) {
			Border border = panel.getBorder();
			for (Component comp : parent.getComponents()) {
				int x = 0, y = 0, w = parent.getWidth(), h = parent.getHeight();
				if (border != null) {
					Insets insets = border.getBorderInsets(panel);
					switch (axis) {
						case Grid.X_AXIS:
							x = insets.left;
							w -= insets.left + insets.right;
							break;

						case Grid.Y_AXIS:
							y = insets.top;
							h -= insets.top + insets.bottom;
							break;
					}
				}

				comp.setBounds(x, y, w, h);
				break;
			}
		}
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		synchronized (parent.getTreeLock()) {
			int ncomponents = parent.getComponentCount();
			if (ncomponents > 0) {
				Component comp = parent.getComponent(0);
				return comp.getMinimumSize();
			} else {
				return nullSize;
			}
		}
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		synchronized (parent.getTreeLock()) {
			int ncomponents = parent.getComponentCount();
			if (ncomponents > 0) {
				Component comp = parent.getComponent(0);
				return comp.getPreferredSize();
			} else {
				return nullSize;
			}
		}
	}

	@Override
	public void removeLayoutComponent(Component comp) {
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {
	}
}
