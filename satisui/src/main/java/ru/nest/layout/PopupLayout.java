package ru.nest.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Toolkit;

public class PopupLayout implements LayoutManager {
	private int maxHeight;

	public PopupLayout(int maxHeight) {
		this.maxHeight = maxHeight;
	}

	public PopupLayout() {
		this.maxHeight = Toolkit.getDefaultToolkit().getScreenSize().height - 100;
	}

	public int getMaxHeight() {
		return maxHeight;
	}

	public void setNaxHeight(int maxHeight) {
		this.maxHeight = maxHeight;
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {
	}

	@Override
	public void removeLayoutComponent(Component comp) {
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		synchronized (parent.getTreeLock()) {
			int ncomponents = parent.getComponentCount();
			if (ncomponents > 0) {
				int w = 0;
				for (int i = 0; i < ncomponents; i++) {
					w = Math.max(w, parent.getComponent(i).getPreferredSize().width);
				}

				int x = 0, y = 0, max = 0;
				for (int i = 0; i < ncomponents; i++) {
					if (y > maxHeight) {
						y = 0;
						x += w;
					}

					y += parent.getComponent(i).getPreferredSize().height + 2;
					max = Math.max(max, y);
				}

				Insets insets = parent.getInsets();
				return new Dimension(x + w + insets.left + insets.right, max + insets.top + insets.bottom);
			} else {
				return new Dimension(0, 0);
			}
		}
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		synchronized (parent.getTreeLock()) {
			int ncomponents = parent.getComponentCount();
			if (ncomponents > 0) {
				int w = 0;
				for (int i = 0; i < ncomponents; i++) {
					w = Math.max(w, parent.getComponent(i).getMinimumSize().width);
				}

				int x = 0, y = 0, max = 0;
				for (int i = 0; i < ncomponents; i++) {
					if (y > maxHeight) {
						y = 0;
						x += w;
					}

					y += parent.getComponent(i).getMinimumSize().height + 2;
					max = Math.max(max, y);
				}

				Insets insets = parent.getInsets();
				return new Dimension(x + w + insets.left + insets.right, max + insets.top + insets.bottom);
			} else {
				return new Dimension(0, 0);
			}
		}
	}

	@Override
	public void layoutContainer(Container parent) {
		synchronized (parent.getTreeLock()) {
			Insets insets = parent.getInsets();
			int ncomponents = parent.getComponentCount();
			if (ncomponents > 0) {
				int w = 0;
				for (int i = 0; i < ncomponents; i++) {
					w = Math.max(w, parent.getComponent(i).getPreferredSize().width);
				}

				int h = parent.getHeight() - (insets.top + insets.bottom);
				int x = insets.left, y = insets.bottom;
				for (int i = 0; i < ncomponents; i++) {
					if (y > h) {
						y = insets.bottom;
						x += w;
					}

					int rowh = parent.getComponent(i).getPreferredSize().height + 2;
					parent.getComponent(i).setBounds(x, y, w, rowh);
					y += rowh;
				}
			}
		}
	}
}
