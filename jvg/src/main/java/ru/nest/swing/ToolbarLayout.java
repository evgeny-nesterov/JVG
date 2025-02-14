package ru.nest.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

public class ToolbarLayout implements LayoutManager {
	@Override
	public void addLayoutComponent(String name, Component comp) {
	}

	@Override
	public void layoutContainer(Container parent) {
		int W = parent.getWidth();
		int x = 0, y = 0, h = 0, lastIndex = 0;
		int count = parent.getComponentCount();
		for (int i = 0; i < count; i++) {
			Component c = parent.getComponent(i);
			Dimension s = null;

			boolean newLine = false;
			if (c.isVisible()) {
				s = c.getPreferredSize();
				if (x + s.width > W) {
					newLine = true;
				} else {
					x += s.width;
					h = Math.max(h, s.height);
				}
			}

			boolean end = i == count - 1;
			if (newLine || end) {
				x = 0;
				for (int j = lastIndex; j < (!newLine ? i + 1 : i); j++) {
					Component cc = parent.getComponent(j);
					if (cc.isVisible()) {
						Dimension ss = cc.getPreferredSize();
						cc.setBounds(x, y, ss.width, h);
						x += ss.width;
					}
				}

				y += h;

				if (newLine && end) {
					c.setBounds(0, y, s.width, s.height);
				}

				x = s != null ? s.width : 0;
				h = s != null ? s.height : 0;
				lastIndex = i;
			}
		}
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		int W = Integer.MAX_VALUE;
		Container p = parent.getParent();
		if (p != null) {
			W = p.getWidth();
		}

		int x = 0, y = 0, h = 0, maxw = 0;
		int count = parent.getComponentCount();
		for (int i = 0; i < count; i++) {
			Component c = parent.getComponent(i);
			Dimension s = null;

			boolean newLine = false;
			if (c.isVisible()) {
				s = c.getMinimumSize();
				if (x + s.width > W) {
					newLine = true;
				} else {
					x += s.width;
					h = Math.max(h, s.height);
				}
			}

			boolean end = i == count - 1;
			if (newLine || end) {
				y += h;
				maxw = Math.max(maxw, x);

				if (newLine && end) {
					y += s.height;
					maxw = Math.max(maxw, s.width);
				}

				x = s != null ? s.width : 0;
				h = s != null ? s.height : 0;
			}
		}
		return new Dimension(maxw, y);
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		int W = Integer.MAX_VALUE;
		Container p = parent.getParent();
		if (p != null) {
			W = p.getWidth();
		}

		int x = 0, y = 0, h = 0, maxw = 0;
		int count = parent.getComponentCount();
		for (int i = 0; i < count; i++) {
			Component c = parent.getComponent(i);
			Dimension s = null;

			boolean newLine = false;
			if (c.isVisible()) {
				s = c.getPreferredSize();
				if (x + s.width > W) {
					newLine = true;
				} else {
					x += s.width;
					h = Math.max(h, s.height);
				}
			}

			boolean end = i == count - 1;
			if (newLine || end) {
				y += h;
				maxw = Math.max(maxw, x);

				if (newLine && end) {
					y += s.height;
					maxw = Math.max(maxw, s.width);
				}

				x = s != null ? s.width : 0;
				h = s != null ? s.height : 0;
			}
		}
		return new Dimension(maxw, y);
	}

	@Override
	public void removeLayoutComponent(Component comp) {
	}
}
