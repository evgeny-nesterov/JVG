package javax.swing.outlook;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

public class OutlookLayout implements LayoutManager {
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

		Insets i = parent.getInsets();
		if (i != null) {
			w += i.left + i.right;
			h += i.top + i.bottom;
		}

		JOutlookPane p = (JOutlookPane) parent;
		w += gap * (p.tabs.size() - 1);

		for (OutlookTab tab : p.tabs) {
			Dimension s = tab.header.getPreferredSize();
			w = Math.max(s.width, w);
			h += p.getHeaderHeight();
			if (tab.comp.isVisible()) {
				s = tab.comp.getPreferredSize();
				w = Math.max(s.width, w);
				h += s.height;
			}
		}
		return new Dimension(w, h);
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		int w = 1000;
		int h = 0;

		Insets i = parent.getInsets();
		if (i != null) {
			w += i.left + i.right;
			h += i.top + i.bottom;
		}

		JOutlookPane p = (JOutlookPane) parent;
		w += gap * (p.tabs.size() - 1);

		for (OutlookTab tab : p.tabs) {
			Dimension s = tab.header.getMinimumSize();
			w = Math.min(s.width, w);
			h += p.getHeaderHeight();
			if (tab.comp.isVisible()) {
				s = tab.comp.getMinimumSize();
				w = Math.min(s.width, w);
				h += s.height;
			}
		}
		return new Dimension(w, h);
	}

	@Override
	public void layoutContainer(Container parent) {
		JOutlookPane p = (JOutlookPane) parent;
		Insets i = parent.getInsets();

		int totalTitleHeight = p.tabs.size() * p.getHeaderHeight();
		double totalCompHeightPerc = 0;

		for (OutlookTab tab : p.tabs) {
			if (tab.comp.isVisible()) {
				totalCompHeightPerc += tab.getWeight();
			}
		}

		int totalCompHeight = parent.getHeight() - totalTitleHeight;
		if (p.tabs.size() > 0) {
			totalCompHeight -= gap * (p.tabs.size() - 1);
		}
		if (i != null) {
			totalCompHeight -= i.top + i.bottom;
		}

		int y = i != null ? i.top : 0;
		int w = parent.getWidth() - (i != null ? i.left + i.right : 0);
		for (OutlookTab tab : p.tabs) {
			tab.header.setBounds(i != null ? i.left : 0, y, w, p.getHeaderHeight());
			y += p.getHeaderHeight();

			if (tab.comp.isVisible()) {
				int compHeight = (int) (totalCompHeight * tab.getWeight() / totalCompHeightPerc);
				tab.comp.setBounds(i != null ? i.left : 0, y, w, compHeight);
				y += compHeight;
			}
			y += gap;
		}

		if (p.pnlGlass.isShowing()) {
			p.pnlGlass.setBounds(0, 0, p.getWidth(), p.getHeight());
		}
	}

	public int getGap() {
		return gap;
	}

	public void setGap(int gap) {
		this.gap = gap;
	}
}
