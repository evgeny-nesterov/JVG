package javax.swing.dock.dashboard;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.dock.DockContentPanel;

public class DashboardLayout implements LayoutManager {
	private int gap = 2;

	@Override
	public void addLayoutComponent(String name, Component comp) {
	}

	@Override
	public void removeLayoutComponent(Component comp) {
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		int w = 0;
		int h = 0;

		Insets insets = parent.getInsets();
		if (insets != null) {
			w += insets.left + insets.right;
			h += insets.top + insets.bottom;
		}

		w += gap * (parent.getComponentCount() - 1);

		for (int i = 0; i < parent.getComponentCount(); i++) {
			Component c = parent.getComponent(i);
			if (c.isVisible()) {
				Dimension s = c.getPreferredSize();
				w = Math.max(s.width, w);

				int compHeight = s.height;
				if (c instanceof DockContentPanel) {
					DockContentPanel cp = (DockContentPanel) c;
					if (!cp.getContent().isVisible()) {
						compHeight = cp.getHeader().getPreferredSize().height;
					}
				}

				h += compHeight;
			}
		}
		return new Dimension(w, h);
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		int w = 0;
		int h = 0;

		Insets insets = parent.getInsets();
		if (insets != null) {
			w += insets.left + insets.right;
			h += insets.top + insets.bottom;
		}

		w += gap * (parent.getComponentCount() - 1);

		for (int i = 0; i < parent.getComponentCount(); i++) {
			Component c = parent.getComponent(i);
			if (c.isVisible()) {
				Dimension s = c.getMinimumSize();
				w = Math.max(s.width, w);

				int compHeight = s.height;
				if (c instanceof DockContentPanel) {
					DockContentPanel cp = (DockContentPanel) c;
					if (!cp.getContent().isVisible()) {
						compHeight = cp.getHeader().getPreferredSize().height;
					}
				}

				h += compHeight;
			}
		}
		return new Dimension(w, h);
	}

	@Override
	public void layoutContainer(Container parent) {
		Insets insets = parent.getInsets();
		int y = insets != null ? insets.top : 0;
		int w = parent.getWidth() - (insets != null ? insets.left + insets.right : 0);
		for (int i = 0; i < parent.getComponentCount(); i++) {
			Component c = parent.getComponent(i);
			if (c.isVisible()) {
				int compHeight = c.getPreferredSize().height;
				if (c instanceof DockContentPanel) {
					DockContentPanel cp = (DockContentPanel) c;
					if (!cp.getContent().isVisible()) {
						compHeight = cp.getHeader().getPreferredSize().height;
					}
				}

				c.setBounds(insets != null ? insets.left : 0, y, w, compHeight);
				y += compHeight + gap;
			}
		}
	}

	public int getGap() {
		return gap;
	}

	public void setGap(int gap) {
		this.gap = gap;
	}
}
