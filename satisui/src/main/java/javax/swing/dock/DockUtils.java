package javax.swing.dock;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.Point;

import javax.swing.GUIUtils;
import javax.swing.dock.grid.GridLayout;

public class DockUtils {
	public static void drawDockRect(Graphics g, int x, int y, int w, int h) {
		Color color = Color.darkGray;
		GUIUtils.fillDottedRect(g, color, x - 1, y - 1, w + 2, 3);
		GUIUtils.fillDottedRect(g, color, x - 1, y + h - 1, w + 2, 3);
		GUIUtils.fillDottedRect(g, color, x - 1, y + 1, 3, h - 2);
		GUIUtils.fillDottedRect(g, color, x + w - 1, y + 1, 3, h - 2);
	}

	public static Object getConstraints(Container container, Component children) {
		LayoutManager layout = container.getLayout();
		if (layout instanceof GridLayout) {
			return ((GridLayout) layout).getConstraints(children);
		}
		if (layout instanceof BorderLayout) {
			return ((BorderLayout) layout).getConstraints(children);
		}
		return null;
	}

	public static Component getComponent(Container container, int x, int y) {
		for (int i = 0; i < container.getComponentCount(); i++) {
			Component c = container.getComponent(i);
			if (x >= c.getX() && x <= c.getX() + c.getWidth() && y >= c.getY() && y <= c.getY() + c.getHeight()) {
				return c;
			}
		}
		return null;
	}

	public static Component getDeepestComponentAt(Component parent, int x, int y) {
		if (!parent.contains(x, y)) {
			return null;
		}
		if (parent instanceof Container) {
			Component components[] = ((Container) parent).getComponents();
			for (int i = 0; i < components.length; i++) {
				Component comp = components[i];
				if (comp != null && comp.isVisible() && !(comp instanceof DockGlassPanel)) {
					Point loc = comp.getLocation();
					if (comp instanceof Container) {
						comp = getDeepestComponentAt(comp, x - loc.x, y - loc.y);
					} else {
						comp = comp.getComponentAt(x - loc.x, y - loc.y);
					}
					if (comp != null && comp.isVisible()) {
						return comp;
					}
				}
			}
		}
		return parent;
	}

	public static boolean isAncestorOf(Component cp, Component c) {
		Container p;
		if (c == null || ((p = c.getParent()) == null)) {
			return false;
		}
		while (p != null) {
			if (p == cp) {
				return true;
			}
			p = p.getParent();
		}
		return false;
	}
}
