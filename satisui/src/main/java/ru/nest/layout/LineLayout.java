package ru.nest.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

public class LineLayout implements LayoutManager {
	@Override
	public void addLayoutComponent(String name, Component comp) {
	}

	@Override
	public void removeLayoutComponent(Component comp) {
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		int w = 0, h = 0;
		for (Component c : parent.getComponents()) {
			Dimension d = c.getPreferredSize();
			w += d.width;
			if (h < d.height) {
				h = d.height;
			}
		}

		Insets insets = parent.getInsets();
		if (insets != null) {
			w += insets.left + insets.right;
			h += insets.top + insets.bottom;
		}

		return new Dimension(w, h);
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		int w = 0, h = 0;
		for (Component c : parent.getComponents()) {
			Dimension d = c.getMinimumSize();
			w += d.width;
			if (h < d.height) {
				h = d.height;
			}
		}

		Insets insets = parent.getInsets();
		if (insets != null) {
			w += insets.left + insets.right;
			h += insets.top + insets.bottom;
		}

		return new Dimension(w, h);
	}

	@Override
	public void layoutContainer(Container parent) {
		int x = 0, y = 0;
		Insets insets = parent.getInsets();
		if (insets != null) {
			x = insets.left;
			y = insets.top;
		}

		int h = 0;
		for (Component c : parent.getComponents()) {
			Dimension d = c.getPreferredSize();
			if (h < d.height) {
				h = d.height;
			}
		}

		for (Component c : parent.getComponents()) {
			Dimension d = c.getPreferredSize();
			c.setSize(d);
			c.setLocation(x, y + (h - d.height) / 2);
			x += d.width;
		}
	}
}
