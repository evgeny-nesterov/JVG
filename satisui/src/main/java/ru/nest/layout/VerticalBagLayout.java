package ru.nest.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

public class VerticalBagLayout implements LayoutManager {
	int vgap;

	public VerticalBagLayout() {
		this(0);
	}

	public VerticalBagLayout(int vgap) {
		this.vgap = vgap;
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {
	}

	@Override
	public void removeLayoutComponent(Component comp) {
	}

	@Override
	public Dimension minimumLayoutSize(Container target) {
		Dimension dim = new Dimension();
		int nmembers = target.countComponents();

		for (int i = 0; i < nmembers; i++) {
			Component comp = target.getComponent(i);
			if (comp.isVisible()) {
				Dimension d = comp.minimumSize();
				dim.width = Math.max(d.width, dim.width);
				dim.height += d.height + vgap;
			}
		}

		Insets insets = target.insets();
		dim.width += insets.left + insets.right;
		dim.height += insets.top + insets.bottom;
		return dim;
	}

	@Override
	public Dimension preferredLayoutSize(Container target) {
		Dimension dim = new Dimension();
		int nmembers = target.countComponents();

		for (int i = 0; i < nmembers; i++) {
			Component comp = target.getComponent(i);
			if (true || comp.isVisible()) {
				Dimension d = comp.preferredSize();
				dim.width = Math.max(d.width, dim.width);
				dim.height += d.height + vgap;
			}
		}

		Insets insets = target.insets();
		dim.width += insets.left + insets.right;
		dim.height += insets.top + insets.bottom;

		return dim;
	}

	@Override
	public void layoutContainer(Container target) {
		Insets insets = target.insets();
		int top = insets.top;
		int bottom = target.size().height - insets.bottom;
		int left = insets.left;
		int right = target.size().width - insets.right;
		int nmembers = target.countComponents();

		for (int i = 0; i < nmembers; i++) {
			Component comp = target.getComponent(i);
			if (comp.isVisible()) {
				int compHeight = comp.size().height;
				comp.resize(right - left, compHeight);
				Dimension d = comp.preferredSize();
				comp.reshape(left, top, right - left, d.height);
				top += d.height + vgap;
			}
		}
	}
}
