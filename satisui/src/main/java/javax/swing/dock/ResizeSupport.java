package javax.swing.dock;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.dock.Resizable.ResizeDirection;

public class ResizeSupport {
	private Container container;

	private boolean active = true;

	private boolean pressed = false;

	private boolean horResize;

	private List<Component> p1 = new ArrayList<Component>();

	private List<Component> p2 = new ArrayList<Component>();

	private int mx;

	private int my;

	private boolean changeNextSize = true;

	private boolean nextRequired = true;

	public ResizeSupport(final Container container) {
		this.container = container;

		container.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent e) {
				if (active) {
					if (!pressed) {
						container.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					}
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
					if (active) {
						if (p1.size() > 0 && (p2.size() > 0 || !nextRequired)) {
							mx = e.getX();
							my = e.getY();
							pressed = true;
						}
					}
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (active) {
					checkResize(e.getX(), e.getY());
					pressed = false;
				}
			}
		});
		container.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				if (active) {
					if (!pressed) {
						checkResize(e.getX(), e.getY());
					}
				}
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				if (pressed) {
					int x = e.getX();
					int y = e.getY();
					int dx = 0;
					int dy = 0;

					if (horResize) {
						int minw1 = 0;
						int maxw1 = Integer.MAX_VALUE;
						for (Component c : p1) {
							minw1 = Math.max(minw1, c.isMinimumSizeSet() ? c.getMinimumSize().width : 0);
							maxw1 = Math.min(maxw1, c.isMaximumSizeSet() ? c.getMaximumSize().width : Integer.MAX_VALUE);
							if (maxw1 < 5) {
								maxw1 = 5;
							}
						}

						int minw2 = 0;
						int maxw2 = Integer.MAX_VALUE;
						for (Component c : p2) {
							minw2 = Math.max(minw2, c.isMinimumSizeSet() ? c.getMinimumSize().width : 0);
							maxw2 = Math.min(maxw2, c.isMaximumSizeSet() ? c.getMaximumSize().width : Integer.MAX_VALUE);
							if (maxw1 < 5) {
								maxw1 = 5;
							}
						}

						dx = x - mx;
						for (Component c : p1) {
							if (dx > 0) {
								if (c.getWidth() + dx > maxw1) {
									dx = Math.max(Math.min(dx, maxw1 - c.getWidth()), 0);
								}
							} else {
								if (c.getWidth() + dx < minw1) {
									dx = Math.min(Math.max(dx, minw1 - c.getWidth()), 0);
								}
							}
						}
						for (Component c : p2) {
							if (dx < 0) {
								if (c.getWidth() - dx > maxw2) {
									dx = Math.max(Math.min(dx, maxw2 - c.getWidth()), 0);
								}
							} else {
								if (c.getWidth() - dx < minw2) {
									dx = Math.min(Math.max(dx, minw2 - c.getWidth()), 0);
								}
							}
						}
						x = mx + dx;
					} else {
						int minh1 = 0;
						int maxh1 = Integer.MAX_VALUE;
						for (Component c : p1) {
							minh1 = Math.max(minh1, c.isMinimumSizeSet() ? c.getMinimumSize().height : 0);
							maxh1 = Math.min(maxh1, c.isMaximumSizeSet() ? c.getMaximumSize().height : Integer.MAX_VALUE);
						}

						int minh2 = 0;
						int maxh2 = Integer.MAX_VALUE;
						for (Component c : p2) {
							minh2 = Math.max(minh2, c.isMinimumSizeSet() ? c.getMinimumSize().height : 0);
							maxh2 = Math.min(maxh2, c.isMaximumSizeSet() ? c.getMaximumSize().height : Integer.MAX_VALUE);
						}

						dy = y - my;
						for (Component c : p1) {
							if (dy > 0) {
								if (c.getHeight() + dy > maxh1) {
									dy = Math.max(Math.min(dy, maxh1 - c.getHeight()), 0);
								}
							} else {
								if (c.getHeight() + dy < minh1) {
									dy = Math.min(Math.max(dy, minh1 - c.getHeight()), 0);
								}
							}
						}
						for (Component c : p2) {
							if (dx < 0) {
								if (c.getHeight() - dy > maxh2) {
									dy = Math.max(Math.min(dy, maxh2 - c.getHeight()), 0);
								}
							} else {
								if (c.getHeight() - dy < minh2) {
									dy = Math.min(Math.max(dy, minh2 - c.getHeight()), 0);
								}
							}
						}
						y = my + dy;
					}

					// set preferred sizes to actual sizes to keep aspect ratio
					for (int i = 0; i < container.getComponentCount(); i++) {
						Component c = container.getComponent(i);
						c.setPreferredSize(c.getSize());
					}

					// calc sizes
					Map<Component, Dimension> sizes = new HashMap<Component, Dimension>();
					for (Component c : p1) {
						Dimension s = c.getSize();
						s.width += dx;
						s.height += dy;
						if (c.isMinimumSizeSet()) {
							s.width = Math.max(s.width, c.getMinimumSize().width);
							s.height = Math.max(s.height, c.getMinimumSize().height);
						}
						if (c.isMaximumSizeSet()) {
							s.width = Math.min(s.width, c.getMaximumSize().width);
							s.height = Math.min(s.height, c.getMaximumSize().height);
						}
						sizes.put(c, s);
					}
					for (Component c : p2) {
						Dimension s = c.getSize();
						s.width -= dx;
						s.height -= dy;
						if (c.isMinimumSizeSet()) {
							s.width = Math.max(s.width, c.getMinimumSize().width);
							s.height = Math.max(s.height, c.getMinimumSize().height);
						}
						if (c.isMaximumSizeSet()) {
							s.width = Math.min(s.width, c.getMaximumSize().width);
							s.height = Math.min(s.height, c.getMaximumSize().height);
						}
						sizes.put(c, s);
					}

					// update sizes and get all parents
					Set<Container> parents = new HashSet<Container>();
					for (Component c : sizes.keySet()) {
						if (changeNextSize || !p2.contains(c)) {
							Dimension size = sizes.get(c);
							boolean resized = false;
							if (c.getParent() instanceof Resizable) {
								Resizable resizable = (Resizable) c.getParent();
								resized = resizable.doResize(c, size.width, size.height, dx, dy);
							}

							if (!resized) {
								c.setPreferredSize(size);
								c.setSize(sizes.get(c));
							}
							parents.add(c.getParent());
						}
					}
					for (Container p : parents) {
						if (p.getParent() != null) {
							parents.add(p.getParent());
						}
					}

					// revalidate
					for (Component p : parents) {
						p.invalidate();
						p.validate();
						p.repaint();
					}

					// repaint
					// System.out.println("\n=============================");
					// System.out.println("DELTA> " + dx + ", " + dy);
					// System.out.println("---");
					// for (Component c : p1) {
					// System.out.println("P1> " + c.getPreferredSize().width +
					// " x " + c.getPreferredSize().height);
					// }
					// System.out.println("---");
					// for (Component c : p2) {
					// System.out.println("P2> " + c.getPreferredSize().width +
					// " x " + c.getPreferredSize().height);
					// }

					mx = x;
					my = y;
				}
			}
		});
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isActive() {
		return active;
	}

	private void checkResize(List<Component> p, Component c, ResizeDirection direction) {
		if (c.getParent() instanceof Resizable) {
			Resizable resizable = (Resizable) c.getParent();
			c = resizable.getComponentToResize(c, direction);
			if (c != null) {
				p.add(c);
			}
		} else {
			p.add(c);
		}
	}

	private void checkResize(int x, int y) {
		p1.clear();
		p2.clear();

		Component ver1 = getComponent(x, y - 5);
		Component ver2 = getComponent(x, y + 5);
		if (ver1 != null && (ver2 != null || !nextRequired) && ver1 != ver2) {
			for (int i = 0; i < container.getComponentCount(); i++) {
				Component c = container.getComponent(i);
				if (c.getY() + c.getHeight() >= y - 5 && c.getY() + c.getHeight() <= y + 5) {
					checkResize(p1, c, ResizeDirection.TOP);
				} else if (c.getY() >= y - 5 && c.getY() <= y + 5) {
					checkResize(p2, c, ResizeDirection.BOTTOM);
				}
			}
			horResize = false;
		} else {
			Component hor1 = getComponent(x - 5, y);
			Component hor2 = getComponent(x + 5, y);
			if (hor1 != null && (hor2 != null || !nextRequired) && hor1 != hor2) {
				for (int i = 0; i < container.getComponentCount(); i++) {
					Component c = container.getComponent(i);
					if (c.getX() + c.getWidth() >= x - 5 && c.getX() + c.getWidth() <= x + 5) {
						checkResize(p1, c, ResizeDirection.LEFT);
					} else if (c.getX() >= x - 5 && c.getX() <= x + 5) {
						checkResize(p2, c, ResizeDirection.RIGHT);
					}
				}
				horResize = true;
			}
		}

		if (p1.size() > 0 && (p2.size() > 0 || !nextRequired)) {
			container.setCursor(Cursor.getPredefinedCursor(horResize ? Cursor.W_RESIZE_CURSOR : Cursor.N_RESIZE_CURSOR));
		} else {
			container.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}

	public Component getComponent(int x, int y) {
		for (int i = 0; i < container.getComponentCount(); i++) {
			Component c = container.getComponent(i);
			if (x >= c.getX() && x <= c.getX() + c.getWidth() && y >= c.getY() && y <= c.getY() + c.getHeight()) {
				return c;
			}
		}
		return null;
	}

	public boolean isChangeNextSize() {
		return changeNextSize;
	}

	public void setChangeNextSize(boolean changeNextSize) {
		this.changeNextSize = changeNextSize;
	}

	public boolean isNextRequired() {
		return nextRequired;
	}

	public void setNextRequired(boolean nextRequired) {
		this.nextRequired = nextRequired;
	}
}
