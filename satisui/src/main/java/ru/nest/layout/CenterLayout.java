package ru.nest.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

public class CenterLayout implements LayoutManager {
	@Override
	public void addLayoutComponent(String name, Component comp) {
	}

	@Override
	public void removeLayoutComponent(Component comp) {
	}

	@Override
	public Dimension preferredLayoutSize(Container container) {
		Component c = container.getComponent(0);
		if (c != null) {
			Dimension size = c.getPreferredSize();
			Insets insets = container.getInsets();
			size.width += insets.left + insets.right;
			size.height += insets.top + insets.bottom;
			return size;
		} else {
			return new Dimension(0, 0);
		}
	}

	@Override
	public Dimension minimumLayoutSize(Container cont) {
		return preferredLayoutSize(cont);
	}

	@Override
	public void layoutContainer(Container container) {
		try {
			Component c = container.getComponent(0);

			c.setSize(c.getPreferredSize());
			Dimension size = c.getSize();
			Dimension containerSize = container.getSize();
			Insets containerInsets = container.getInsets();
			containerSize.width -= containerInsets.left + containerInsets.right;
			containerSize.height -= containerInsets.top + containerInsets.bottom;
			int componentLeft = (containerSize.width / 2) - (size.width / 2);
			int componentTop = (containerSize.height / 2) - (size.height / 2);
			componentLeft += containerInsets.left;
			componentTop += containerInsets.top;

			c.setBounds(componentLeft, componentTop, size.width, size.height);
		} catch (Exception e) {
		}
	}
}
