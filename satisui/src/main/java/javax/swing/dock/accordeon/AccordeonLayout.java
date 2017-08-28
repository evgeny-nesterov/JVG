package javax.swing.dock.accordeon;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.dock.DockContentPanel;

public class AccordeonLayout implements LayoutManager {
	public static final int X_AXIS = 0;

	public static final int Y_AXIS = 1;

	private int gap = 2;

	private int axis;

	public AccordeonLayout(int axis, int gap) {
		this.axis = axis;
		this.gap = gap;
	}

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
				if (axis == Y_AXIS) {
					w = Math.max(s.width, w);
					h += s.height;
				} else {
					h = Math.max(s.height, h);
					w += s.width;
				}
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
				if (axis == Y_AXIS) {
					w = Math.max(s.width, w);
					h += s.height;
				} else {
					h = Math.max(s.height, h);
					w += s.width;
				}
			}
		}
		return new Dimension(w, h);
	}

	@Override
	public void layoutContainer(Container parent) {
		if (axis == Y_AXIS) {
			layoutContainerY(parent);
		} else {
			layoutContainerX(parent);
		}
	}

	public void layoutContainerX(Container parent) {
		Insets insets = parent.getInsets();
		int freeWidth = parent.getWidth() - (insets != null ? (insets.left + insets.right) : 0);
		freeWidth -= (parent.getComponentCount() - 1) * gap;

		// precalc widthes and total width
		int[] widthes = new int[parent.getComponentCount()];
		int totalVisibleWidth = 0;
		for (int i = 0; i < parent.getComponentCount(); i++) {
			Component c = parent.getComponent(i);
			if (c.isVisible()) {
				if (c instanceof DockContentPanel) {
					DockContentPanel d = (DockContentPanel) c;
					if (d.getContent().isVisible()) {
						widthes[i] = c.getPreferredSize().width;
						totalVisibleWidth += widthes[i];
					} else {
						// get size of the header as it will be rotated
						widthes[i] = d.getHeader().getPreferredSize().width;
						freeWidth -= widthes[i];
					}
				} else {
					widthes[i] = c.getPreferredSize().width;
					totalVisibleWidth += widthes[i];
				}
			}
		}

		// calc widthes
		for (int i = 0; i < parent.getComponentCount(); i++) {
			Component c = parent.getComponent(i);
			if (c.isVisible()) {
				if (c instanceof DockContentPanel) {
					DockContentPanel d = (DockContentPanel) c;
					if (d.getContent().isVisible()) {
						if (totalVisibleWidth > 0) {
							widthes[i] = freeWidth * widthes[i] / totalVisibleWidth;
						} else {
							widthes[i] = c.isMinimumSizeSet() ? c.getMinimumSize().width : 0;
						}
					}
				} else {
					if (totalVisibleWidth > 0) {
						widthes[i] = freeWidth * widthes[i] / totalVisibleWidth;
					} else {
						widthes[i] = c.isMinimumSizeSet() ? c.getMinimumSize().width : 0;
					}
				}
			}
		}

		// set sizes
		int x = insets != null ? insets.left : 0;
		int y = insets != null ? insets.top : 0;
		int h = parent.getHeight() - (insets != null ? insets.top + insets.bottom : 0);
		for (int i = 0; i < parent.getComponentCount(); i++) {
			Component c = parent.getComponent(i);
			if (c.isVisible()) {
				c.setBounds(x, y, widthes[i], h);
				x += widthes[i] + gap;
			}
		}
	}

	public void layoutContainerY(Container parent) {
		Insets insets = parent.getInsets();
		int freeHeight = parent.getHeight() - (insets != null ? (insets.top + insets.bottom) : 0);
		freeHeight -= (parent.getComponentCount() - 1) * gap;

		// precalc heights and total height
		int[] heights = new int[parent.getComponentCount()];
		int totalVisibleHeight = 0;
		for (int i = 0; i < parent.getComponentCount(); i++) {
			Component c = parent.getComponent(i);
			if (c.isVisible()) {
				if (c instanceof DockContentPanel) {
					DockContentPanel d = (DockContentPanel) c;
					if (d.getContent().isVisible()) {
						heights[i] = c.getPreferredSize().height;
						totalVisibleHeight += heights[i];
					} else {
						heights[i] = d.getHeader().getPreferredSize().height;
						freeHeight -= heights[i];
					}
				} else {
					heights[i] = c.getPreferredSize().height;
					totalVisibleHeight += heights[i];
				}
			}
		}

		// calc heights
		for (int i = 0; i < parent.getComponentCount(); i++) {
			Component c = parent.getComponent(i);
			if (c.isVisible()) {
				if (c instanceof DockContentPanel) {
					DockContentPanel d = (DockContentPanel) c;
					if (d.getContent().isVisible()) {
						if (totalVisibleHeight != 0) {
							heights[i] = freeHeight * heights[i] / totalVisibleHeight;
						} else {
							heights[i] = c.isMinimumSizeSet() ? c.getMinimumSize().height : 0;
						}

						if (heights[i] < d.getHeader().getPreferredSize().height) {
							heights[i] = d.getHeader().getPreferredSize().height;
						}
					}
				} else {
					if (totalVisibleHeight != 0) {
						heights[i] = freeHeight * heights[i] / totalVisibleHeight;
					} else {
						heights[i] = c.isMinimumSizeSet() ? c.getMinimumSize().height : 0;
					}
				}
			}
		}

		// set sizes
		int x = insets != null ? insets.left : 0;
		int y = insets != null ? insets.top : 0;
		int w = parent.getWidth() - (insets != null ? insets.left + insets.right : 0);
		for (int i = 0; i < parent.getComponentCount(); i++) {
			Component c = parent.getComponent(i);
			if (c.isVisible()) {
				c.setBounds(x, y, w, heights[i]);
				y += heights[i] + gap;
			}
		}
	}

	public int getGap() {
		return gap;
	}

	public void setGap(int gap) {
		this.gap = gap;
	}

	public int getAxis() {
		return axis;
	}

	public void setAxis(int axis) {
		this.axis = axis;
	}
}
