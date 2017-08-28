package ru.nest.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JTextArea;

public class CoordinateLayout implements LayoutManager2 {
	private Rectangle parentBounds = new Rectangle();

	private Bounds componentBounds = new Bounds();

	@Override
	public void layoutContainer(Container parent) {
		updateParentBounds(parent);
		for (Component c : parent.getComponents()) {
			Bounds bounds = getBounds(c);
			c.setBounds((int) bounds.x, (int) bounds.y, (int) (bounds.x + bounds.w) - (int) bounds.x, (int) (bounds.y + bounds.h) - (int) bounds.y);
		}
	}

	private void updateParentBounds(Container parent) {
		Insets insets = parent.getInsets();
		parentBounds.x = insets.left;
		parentBounds.y = insets.top;
		parentBounds.width = parent.getWidth() - insets.left - insets.right;
		parentBounds.height = parent.getHeight() - insets.top - insets.bottom;
	}

	public Bounds getBounds(Component c) {
		CoordinateConstraints cs = map.get(c);
		if (cs != null) {
			return cs.getBounds(c, parentBounds);
		} else {
			componentBounds.x = c.getX();
			componentBounds.y = c.getY();
			componentBounds.w = c.getWidth();
			componentBounds.h = c.getHeight();
			return componentBounds;
		}
	}

	public void setBounds(Component c, Bounds bounds) {
		CoordinateConstraints cs = map.get(c);
		if (cs != null) {
			cs.setBounds(c, parentBounds, bounds);
		} else {
			c.setBounds((int) bounds.x, (int) bounds.y, (int) (bounds.x + bounds.w) - (int) bounds.x, (int) (bounds.y + bounds.h) - (int) bounds.y);
		}
	}

	public void setTypes(Component c, int xType, int yType, int wType, int hType) {
		CoordinateConstraints cs = map.get(c);
		if (cs != null) {
			cs.setTypes(c, parentBounds, xType, yType, wType, hType);
		}
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return new Dimension(20, 20);
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		int w = 0, h = 0;
		updateParentBounds(parent);
		for (Component c : parent.getComponents()) {
			Bounds bounds = getBounds(c);
			w = Math.max(w, (int) (bounds.x + bounds.w));
			h = Math.max(h, (int) (bounds.y + bounds.h));
		}
		return new Dimension(w, h);
	}

	// -------------
	private HashMap<Component, CoordinateConstraints> map = new HashMap<Component, CoordinateConstraints>();

	@Override
	public void addLayoutComponent(Component comp, Object constraints) {
		if (comp != null && constraints != null && constraints instanceof CoordinateConstraints) {
			map.put(comp, (CoordinateConstraints) constraints);
		}
	}

	@Override
	public void removeLayoutComponent(Component comp) {
		if (comp != null) {
			map.remove(comp);
		}
	}

	public CoordinateConstraints getConstraints(Component comp) {
		if (comp != null) {
			return map.get(comp);
		} else {
			return null;
		}
	}

	@Override
	public Dimension maximumLayoutSize(Container parent) {
		return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {
		// empty
	}

	@Override
	public void invalidateLayout(Container target) {
		// empty
	}

	@Override
	public float getLayoutAlignmentX(Container parent) {
		return 0.5f;
	}

	@Override
	public float getLayoutAlignmentY(Container parent) {
		return 0.5f;
	}

	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setBounds(100, 100, 1000, 800);

		CoordinateLayout l = new CoordinateLayout();
		f.getContentPane().setLayout(l);

		f.getContentPane().add(new JTextArea("text"), new CoordinateConstraints(0.1, 0.1, 0.8, 0.8, CoordinateConstraints.PERCENT, CoordinateConstraints.PERCENT, CoordinateConstraints.PREFERRED, CoordinateConstraints.PREFERRED, CoordinateConstraints.LEFT, CoordinateConstraints.TOP));
		f.setVisible(true);
	}
}
