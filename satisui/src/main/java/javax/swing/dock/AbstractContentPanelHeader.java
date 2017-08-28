package javax.swing.dock;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BlankIcon;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.border.Border;
import javax.swing.dock.ExpandSupport.ExpandListener;

public abstract class AbstractContentPanelHeader extends JLabel {
	private static final long serialVersionUID = 1L;

	public enum HeaderOrientation {
		HORIZONTAL, VERTICAL
	}

	private List<Action> actions = new ArrayList<Action>();

	private boolean over = false;

	private Action overAction = null;

	private Action pressedAction = null;

	private int gap = 2;

	private ExpandSupport expandSupport;

	private DockFlowSupport flowSupport;

	private ContentPanelHeaderRenderer renderer;

	private String title;

	private Icon icon;

	private HeaderOrientation orientation = HeaderOrientation.HORIZONTAL;

	public AbstractContentPanelHeader(final String title, Icon icon) {
		this.title = title;
		this.icon = icon != null ? icon : new BlankIcon(5);

		setText(title);
		setIcon(this.icon);

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				if (e.getModifiers() == MouseEvent.NOBUTTON) {
					over = true;
					repaint();
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				if (over) {
					over = false;
					pressedAction = null;
					overAction = null;
					repaint();
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {
				pressedAction = getActionAt(e.getX(), e.getY());
				repaint();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (pressedAction != null) {
					Action releasedAction = getActionAt(e.getX(), e.getY());
					if (releasedAction == pressedAction) {
						pressedAction.actionPerformed(null);
					}
					pressedAction = null;
					repaint();
				}
			}
		});
		addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				Action a = getActionAt(e.getX(), e.getY());
				if (a != overAction) {
					overAction = a;
					repaint();
				}
			}
		});

		setBorder(new Border() {
			@Override
			public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
			}

			@Override
			public boolean isBorderOpaque() {
				return false;
			}

			@Override
			public Insets getBorderInsets(Component c) {
				int w = 0;
				if (actions.size() > 0) {
					w += 10 + (actions.size() - 1) * gap;
					for (Action a : actions) {
						Icon icon = (Icon) a.getValue(Action.SMALL_ICON);
						if (icon != null) {
							w += icon.getIconWidth();
						}
					}
				}
				return new Insets(0, 0, 0, w);
			}
		});
	}

	public void addCloseAction() {
		addAction(new AbstractAction("close", DockIcons.closeIcon) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});
	}

	public void removeCloseAction() {
		for (Action a : actions) {
			if ("close".equals(a.getValue(Action.NAME))) {
				actions.remove(a);
				return;
			}
		}
	}

	public void removeAllActions() {
		actions.clear();
	}

	public void addMaximizeAction() {
		final Action action = new AbstractAction("maximize", DockIcons.maximizeIcon) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				getExpandSupport().setExpanded(!getExpandSupport().isExpanded());
			}
		};
		getExpandSupport().addListener(new ExpandListener() {
			@Override
			public void expandStateChanged(boolean isExpanded, boolean isMinimized) {
				if (!isExpanded) {
					action.putValue(Action.SMALL_ICON, DockIcons.maximizeIcon);
				} else {
					action.putValue(Action.SMALL_ICON, DockIcons.normalizeIcon);
				}
			}
		});
		addAction(action);
	}

	public void addMinimizeAction() {
		final Action action = new AbstractAction("minimize", DockIcons.minimizeIcon) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				getExpandSupport().setMinimized(!getExpandSupport().isMinimized());
			}
		};
		getExpandSupport().addListener(new ExpandListener() {
			@Override
			public void expandStateChanged(boolean isExpanded, boolean isMinimized) {
				if (!isMinimized) {
					action.putValue(Action.SMALL_ICON, DockIcons.minimizeIcon);
				} else {
					action.putValue(Action.SMALL_ICON, DockIcons.normalizeIcon);
				}
			}
		});
		addAction(action);
	}

	public void removeMinimizeAction() {
		for (Action a : actions) {
			if ("minimize".equals(a.getValue(Action.NAME))) {
				actions.remove(a);
				return;
			}
		}
	}

	public void addAction(Action action) {
		actions.add(action);
		repaint();
	}

	private int height = 20;

	public void setHeight(int height) {
		this.height = height;
		if (orientation == HeaderOrientation.VERTICAL) {
			setPreferredSize(new Dimension(height, 30));
		} else {
			setPreferredSize(new Dimension(30, height));
		}
	}

	private int getIconsOffset(int w, int h) {
		int x = w - (actions.size() - 1) * gap - 5;
		for (Action a : actions) {
			Icon icon = (Icon) a.getValue(Action.SMALL_ICON);
			if (icon != null) {
				x -= icon.getIconWidth();
			}
		}
		if (x < 30) {
			x = 30;
		}
		if (w - x < 16) {
			x = Math.max(1, getWidth() - 16);
		}
		return x;
	}

	private Action getActionAt(int mx, int my) {
		int w;
		int h;
		if (orientation == HeaderOrientation.VERTICAL) {
			w = getHeight();
			h = getWidth();

			int my_ = mx;
			mx = getHeight() - my;
			my = my_;
		} else {
			w = getWidth();
			h = getHeight();
		}

		int x = getIconsOffset(w, h);
		for (Action a : actions) {
			Icon icon = (Icon) a.getValue(Action.SMALL_ICON);
			if (icon != null) {
				int iy = (h - icon.getIconHeight()) / 2;
				if (mx >= x && mx <= x + icon.getIconWidth() && my >= iy && my <= iy + icon.getIconHeight()) {
					return a;
				}
				x += icon.getIconWidth() + gap;
			}
		}
		return null;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (renderer != null) {
			Component c = renderer.getContentPanelHeaderRendererComponent(this, title, icon);
			if (c != null) {
				if (orientation == HeaderOrientation.VERTICAL) {
					c.setSize(new Dimension(getHeight(), getWidth()));
				} else {
					c.setSize(getSize());
				}
				c.paint(g);
			}
		}
	}

	protected boolean isDrawIcons() {
		return over || expandSupport.isExpanded();
	}

	@Override
	public void paint(Graphics g) {
		if (orientation == HeaderOrientation.VERTICAL) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.rotate(-Math.PI / 2, getHeight() / 2, getHeight() / 2);
		}

		super.paint(g);

		if (orientation == HeaderOrientation.VERTICAL) {
			drawIcons(g, getHeight(), getWidth());
		} else {
			drawIcons(g, getWidth(), getHeight());
		}
	}

	public void drawIcons(Graphics g, int w, int h) {
		if (isDrawIcons()) {
			int x = getIconsOffset(w, h);
			for (Action a : actions) {
				Icon icon = (Icon) a.getValue(Action.SMALL_ICON);
				if (icon != null) {
					int iy = (h - icon.getIconHeight()) / 2;

					if (a == pressedAction) {
						g.setColor(new Color(200, 200, 200));
						g.fillRect(x, iy, icon.getIconWidth() - 1, icon.getIconHeight() - 1);
					}

					icon.paintIcon(this, g, x, iy);

					if (a == overAction) {
						g.setColor(Color.gray);
						g.drawRect(x, iy, icon.getIconWidth() - 1, icon.getIconHeight() - 1);
					}
					x += icon.getIconWidth() + gap;
				}
			}
		}
	}

	public void close() {
	}

	public void maximize() {
		getExpandSupport().expand();
	}

	public void normalize() {
		getExpandSupport().expandRestore();
	}

	public ExpandSupport getExpandSupport() {
		return expandSupport;
	}

	public void setExpandSupport(ExpandSupport expandSupport) {
		this.expandSupport = expandSupport;

		// Default action on double click is expand.
		// Call expandSupport.setMouseListener(<your mouse listener>) to set custom mouse action for header.
		// Call expandSupport.setMouseListener(null) to remove mouse action.
		expandSupport.expandOnDoubleClick(this);
	}

	public DockFlowSupport getFlowSupport() {
		return flowSupport;
	}

	public void setFlowSupport(DockFlowSupport flowSupport) {
		this.flowSupport = flowSupport;
	}

	public ContentPanelHeaderRenderer getRenderer() {
		return renderer;
	}

	public void setRenderer(ContentPanelHeaderRenderer renderer) {
		this.renderer = renderer;
		if (renderer != null) {
			setText("");
			setIcon(null);
		} else {
			setText(title);
			setIcon(icon);
		}
	}

	public HeaderOrientation getOrientation() {
		return orientation;
	}

	public void setOrientation(HeaderOrientation orientation) {
		this.orientation = orientation;
		setHeight(height);
	}
}
