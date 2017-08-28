package javax.swing.dock.dashboard;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.dock.ContentPanelHeader;
import javax.swing.dock.DockContentPanel;
import javax.swing.dock.DockCursors;
import javax.swing.dock.DockFlow;
import javax.swing.dock.DockUtils;
import javax.swing.dock.Dockable;
import javax.swing.dock.ExpandSupport;
import javax.swing.dock.Resizable;
import javax.swing.dock.ResizeSupport;
import javax.swing.dock.dashboard.DashboardConstraints.DashboardDirection;
import javax.swing.dock.selection.SelectionChangeListener;
import javax.swing.dock.selection.SelectionModel;

public class DashboardDockContainer extends JPanel implements Dockable<DashboardConstraints>, Resizable {
	private static final long serialVersionUID = 1L;

	private ResizeSupport resizeSupport;

	private SelectionModel selectionModel = new SelectionModel(this);

	public DashboardDockContainer() {
		resizeSupport = new ResizeSupport(this);
		resizeSupport.setChangeNextSize(false);
		resizeSupport.setNextRequired(false);

		setMinimumSize(new Dimension(30, 30));
		setLayout(new DashboardLayout());
		setBorder(new EmptyBorder(2, 2, 2, 2));

		addContainerListener(new ContainerListener() {
			@Override
			public void componentRemoved(ContainerEvent e) {
				e.getComponent().removeComponentListener(componentListener);
			}

			@Override
			public void componentAdded(ContainerEvent e) {
				e.getComponent().addComponentListener(componentListener);
			}
		});
	}

	private ComponentListener componentListener = new ComponentListener() {
		@Override
		public void componentResized(ComponentEvent e) {
			repaint();
		}

		@Override
		public void componentMoved(ComponentEvent e) {
			repaint();
		}

		@Override
		public void componentShown(ComponentEvent e) {
			repaint();
		}

		@Override
		public void componentHidden(ComponentEvent e) {
			repaint();
		}
	};

	@Override
	public ResizeSupport getResizeSupport() {
		return resizeSupport;
	}

	public void addSelectionChangeListener(SelectionChangeListener l) {
		listenerList.add(SelectionChangeListener.class, l);
	}

	public void removeSelectionChangeListener(SelectionChangeListener l) {
		listenerList.remove(SelectionChangeListener.class, l);
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		selectionModel.drawSelection(g);
	}

	@Override
	public DashboardConstraints getDockConstraints(DockFlow flow, int x, int y) {
		DashboardConstraints constraints = null;

		if (getComponentCount() == 0) {
			// container is empty
			return new DashboardConstraints(null, DashboardDirection.NONE);
		} else {
			// check first
			Component c = getComponent(0);
			if (c.getY() + c.getHeight() / 2 >= y) {
				constraints = new DashboardConstraints(c, DashboardDirection.BACKWARD);
			}

			// check fast
			if (constraints == null) {
				c = getComponent(getComponentCount() - 1);
				if (c.getY() + c.getHeight() / 2 <= y) {
					constraints = new DashboardConstraints(c, DashboardDirection.FORWARD);
				}
			}

			// check pos is middle of components
			if (constraints == null) {
				for (int i = 0; i < getComponentCount() - 1; i++) {
					Component c1 = getComponent(i);
					Component c2 = getComponent(i + 1);
					if (c1.getY() + c1.getHeight() / 2 < y && c2.getY() + c2.getHeight() / 2 > y) {
						int middle = (c2.getY() + c1.getY() + c1.getHeight()) / 2;
						if (y < middle) {
							constraints = new DashboardConstraints(c1, DashboardDirection.FORWARD);
						} else {
							constraints = new DashboardConstraints(c2, DashboardDirection.BACKWARD);
						}
						break;
					}
				}
			}

			if (constraints != null) {
				// check on flow component is neibour
				int index = getComponentZOrder(constraints.c);
				int neibourIndex;
				if (constraints.direction == DashboardDirection.BACKWARD) {
					neibourIndex = index - 1;
				} else {
					neibourIndex = index + 1;
				}

				if (neibourIndex >= 0 && neibourIndex < getComponentCount() && getComponent(neibourIndex) == flow.getUndockComponent()) {
					return null;
				}
			}
		}
		return constraints;
	}

	@Override
	public void drawDockPlace(Graphics g, DockFlow flow, DashboardConstraints constraints) {
		int x = 0, y = 0, w = 10, h = 10;
		if (constraints.c != null) {
			y = constraints.c.getY() - 5;
			w = getWidth();
			if (constraints.direction == DashboardDirection.FORWARD) {
				y += constraints.c.getHeight();
			}

			int index = getComponentZOrder(constraints.c);
			if (index == 0 && constraints.direction == DashboardDirection.BACKWARD) {
				y += 5;
			} else if (index == getComponentCount() - 1 && constraints.direction == DashboardDirection.FORWARD) {
				y -= 5;
			}
		} else {
			w = getWidth();
			h = getHeight();
		}
		DockUtils.drawDockRect(g, x, y, w, h);
	}

	@Override
	public void dock(DockFlow flow, DashboardConstraints constraints) {
		Dimension undockPrefSize = flow.getUndockComponent().isPreferredSizeSet() ? flow.getUndockComponent().getPreferredSize() : null;

		flow.removeUndockComponent();

		if (getComponentCount() > 0) {
			int index = 0;
			if (constraints.c != null) {
				index = getComponentZOrder(constraints.c);
				if (constraints.direction == DashboardDirection.FORWARD) {
					index++;
				}
			}

			// fetch default size
			Dimension size = undockPrefSize;
			if (size == null) {
				size = flow.getDropComponent().getPreferredSize();
			} else {
				size.width = flow.getDropComponent().getPreferredSize().width;
			}
			if (size.height < 25) {
				size.height = 25;
			}
			flow.getDropComponent().setPreferredSize(size);

			Component contentPanel = DockContentPanel.create(this, flow.getDropComponent(), flow.getName(), flow.getIcon());
			add(contentPanel, index);
		} else {
			// first insert
			Component contentPanel = DockContentPanel.create(this, flow.getDropComponent(), flow.getName(), flow.getIcon());
			add(contentPanel);
		}
		revalidate();
	}

	@Override
	public void undock(DockFlow flow) {
		// do nothing
	}

	@Override
	public Cursor getDockCursor(DockFlow flow, DashboardConstraints constraints) {
		if (constraints.direction != DashboardDirection.NONE) {
			if (constraints.direction == DashboardDirection.BACKWARD) {
				return DockCursors.topCursor;
			} else {
				return DockCursors.bottomCursor;
			}
		} else {
			// TODO
		}
		return Cursor.getDefaultCursor();
	}

	public void setInsets(int size) {
		setInsets(size, size, size, size);
	}

	public void setInsets(int top, int left, int bottom, int right) {
		setBorder(new EmptyBorder(top, left, bottom, right));
	}

	public Component createHeader(String title, Icon icon, final Component dropComponent, final Component undockComponent) {
		final ContentPanelHeader header = new ContentPanelHeader(this, title, icon, dropComponent, undockComponent);
		header.addMinimizeAction();
		header.addMaximizeAction();
		header.addCloseAction();
		header.getExpandSupport().minimizeOnDblCLick(header);
		return header;
	}

	@Override
	public Component createHeader(final DockFlow flow) {
		return createHeader(flow.getName(), flow.getIcon(), flow.getDropComponent(), flow.getUndockComponent());
	}

	@Override
	public boolean acceptDock(DockFlow flow) {
		return true;
	}

	@Override
	public boolean acceptUndock(DockFlow flow, Dockable<?> dst) {
		return true;
	}

	@Override
	public SelectionModel getSelectionModel() {
		return selectionModel;
	}

	@Override
	public boolean doResize(Component c, int width, int height, int dw, int dh) {
		return false;
	}

	@Override
	public Component getComponentToResize(Component c, ResizeDirection direction) {
		if (!isResizable(c)) {
			int index = -1;
			for (int i = 0; i < getComponentCount(); i++) {
				if (c == getComponent(i)) {
					index = i;
					break;
				}
			}

			switch (direction) {
				case TOP:
				case LEFT:
					while (index > 0) {
						index--;
						c = getComponent(index);
						if (isResizable(c)) {
							return c;
						}
					}
					break;
				case BOTTOM:
				case RIGHT:
					while (index < getComponentCount() - 1) {
						index++;
						c = getComponent(index);
						if (isResizable(c)) {
							return c;
						}
					}
					break;
			}
			return null;
		}
		return c;
	}

	private boolean isResizable(Component c) {
		if (c instanceof DockContentPanel) {
			DockContentPanel cp = (DockContentPanel) c;
			return cp.getContent().isVisible();
		} else {
			return true;
		}
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		DashboardDockContainer dashboard = new DashboardDockContainer();
		dashboard.putClientProperty(ExpandSupport.COMPONENT_PROPERTY, true);

		JTextArea a1 = new JTextArea("1");
		JTextArea a2 = new JTextArea("2");
		JTextArea a3 = new JTextArea("3");
		JTextArea a4 = new JTextArea("4");
		JTextArea a5 = new JTextArea("5");

		Component c1 = DockContentPanel.create(dashboard, a1, "111", null);
		Component c2 = DockContentPanel.create(dashboard, a2, "222", null);
		Component c3 = DockContentPanel.create(dashboard, a3, "333", null);
		Component c4 = DockContentPanel.create(dashboard, a4, "444", null);
		Component c5 = DockContentPanel.create(dashboard, a5, "555", null);

		c1.setPreferredSize(new Dimension(100, 100));
		c2.setPreferredSize(new Dimension(100, 100));
		c3.setPreferredSize(new Dimension(100, 100));
		c4.setPreferredSize(new Dimension(100, 100));
		c5.setPreferredSize(new Dimension(100, 100));

		dashboard.add(c1);
		dashboard.add(c2);
		dashboard.add(c3);
		dashboard.add(c4);
		dashboard.add(c5);

		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setContentPane(new JScrollPane(dashboard));
		f.setBounds(300, 100, 500, 800);
		f.setVisible(true);
	}
}
