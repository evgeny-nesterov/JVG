package javax.swing.dock.accordeon;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.dock.AbstractContentPanelHeader.HeaderOrientation;
import javax.swing.dock.ContentPanelHeader;
import javax.swing.dock.DockContentPanel;
import javax.swing.dock.DockCursors;
import javax.swing.dock.DockFlow;
import javax.swing.dock.DockUtils;
import javax.swing.dock.Dockable;
import javax.swing.dock.ExpandSupport;
import javax.swing.dock.Resizable;
import javax.swing.dock.ResizeSupport;
import javax.swing.dock.accordeon.AccordeonConstraints.AccordeonDirection;
import javax.swing.dock.grid.GridDockContainer;
import javax.swing.dock.selection.SelectionChangeListener;
import javax.swing.dock.selection.SelectionModel;

public class AccordeonDockContainer extends JPanel implements Dockable<AccordeonConstraints>, Resizable {
	private static final long serialVersionUID = 1L;

	public static final int X_AXIS = AccordeonLayout.X_AXIS;

	public static final int Y_AXIS = AccordeonLayout.Y_AXIS;

	protected AccordeonLayout layout;

	private ResizeSupport resizeSupport;

	private SelectionModel selectionModel = new SelectionModel(this);

	public AccordeonDockContainer() {
		this(X_AXIS, 2);
	}

	public AccordeonDockContainer(int axis, int gap) {
		layout = new AccordeonLayout(axis, gap);
		resizeSupport = new ResizeSupport(this);

		setLayout(layout);
		setMinimumSize(new Dimension(30, 30));
	}

	public void setGap(int gap) {
		layout.setGap(gap);
	}

	@Override
	public AccordeonConstraints getDockConstraints(DockFlow flow, int x, int y) {
		AccordeonConstraints constraints = null;

		if (getComponentCount() == 0) {
			// container is empty
			return new AccordeonConstraints(null, AccordeonDirection.NONE);
		} else {
			// check first
			Component c = getComponent(0);
			if (layout.getAxis() == X_AXIS) {
				if (c.getX() + c.getWidth() / 2 >= x) {
					constraints = new AccordeonConstraints(c, AccordeonDirection.BACKWARD);
				}
			} else {
				if (c.getY() + c.getHeight() / 2 >= y) {
					constraints = new AccordeonConstraints(c, AccordeonDirection.BACKWARD);
				}
			}

			// check fast
			if (constraints == null) {
				c = getComponent(getComponentCount() - 1);
				if (layout.getAxis() == X_AXIS) {
					if (c.getX() + c.getWidth() / 2 <= x) {
						constraints = new AccordeonConstraints(c, AccordeonDirection.FORWARD);
					}
				} else {
					if (c.getY() + c.getHeight() / 2 <= y) {
						constraints = new AccordeonConstraints(c, AccordeonDirection.FORWARD);
					}
				}
			}

			// check pos is middle of components
			if (constraints == null) {
				for (int i = 0; i < getComponentCount() - 1; i++) {
					Component c1 = getComponent(i);
					Component c2 = getComponent(i + 1);
					if (layout.getAxis() == X_AXIS) {
						if (c1.getX() + c1.getWidth() / 2 < x && c2.getX() + c2.getWidth() / 2 > x) {
							int middle = (c2.getX() + c1.getX() + c1.getWidth()) / 2;
							if (x < middle) {
								constraints = new AccordeonConstraints(c1, AccordeonDirection.FORWARD);
							} else {
								constraints = new AccordeonConstraints(c2, AccordeonDirection.BACKWARD);
							}
							break;
						}
					} else {
						if (c1.getY() + c1.getHeight() / 2 < y && c2.getY() + c2.getHeight() / 2 > y) {
							int middle = (c2.getY() + c1.getY() + c1.getHeight()) / 2;
							if (y < middle) {
								constraints = new AccordeonConstraints(c1, AccordeonDirection.FORWARD);
							} else {
								constraints = new AccordeonConstraints(c2, AccordeonDirection.BACKWARD);
							}
							break;
						}
					}
				}
			}

			if (constraints != null) {
				// check on flow component is neibour
				int index = getComponentZOrder(constraints.c);
				int neibourIndex;
				if (constraints.direction == AccordeonDirection.BACKWARD) {
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
	public void drawDockPlace(Graphics g, DockFlow flow, AccordeonConstraints constraints) {
		int x = 0, y = 0, w = 10, h = 10;
		if (constraints.c != null) {
			if (layout.getAxis() == X_AXIS) {
				x = constraints.c.getX() - 5;
				h = getHeight();
				if (constraints.direction == AccordeonDirection.FORWARD) {
					x += constraints.c.getWidth();
				}
			} else {
				y = constraints.c.getY() - 5;
				w = getWidth();
				if (constraints.direction == AccordeonDirection.FORWARD) {
					y += constraints.c.getHeight();
				}
			}

			int index = getComponentZOrder(constraints.c);
			if (index == 0 && constraints.direction == AccordeonDirection.BACKWARD) {
				if (layout.getAxis() == X_AXIS) {
					x += 5;
				} else {
					y += 5;
				}
			} else if (index == getComponentCount() - 1 && constraints.direction == AccordeonDirection.FORWARD) {
				if (layout.getAxis() == X_AXIS) {
					x -= 5;
				} else {
					y -= 5;
				}
			}
		} else {
			w = getWidth();
			h = getHeight();
		}
		DockUtils.drawDockRect(g, x, y, w, h);
	}

	@Override
	public void dock(DockFlow flow, AccordeonConstraints constraints) {
		flow.removeUndockComponent();

		if (getComponentCount() > 0) {
			int index = 0;
			if (constraints.c != null) {
				index = getComponentZOrder(constraints.c);
				if (constraints.direction == AccordeonDirection.FORWARD) {
					index++;
				}
			}

			// fetch default size
			Dimension size = flow.getDropComponent().getPreferredSize();
			if (layout.getAxis() == X_AXIS) {
				size.width /= getComponentCount();
				if (size.width < 25) {
					size.width = 25;
				}
			} else {
				size.height /= getComponentCount();
				if (size.height < 25) {
					size.height = 25;
				}
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
	public Cursor getDockCursor(DockFlow flow, AccordeonConstraints constraints) {
		if (constraints.direction != AccordeonDirection.NONE) {
			if (layout.getAxis() == X_AXIS) {
				if (constraints.direction == AccordeonDirection.BACKWARD) {
					return DockCursors.leftCursor;
				} else {
					return DockCursors.rightCursor;
				}
			} else {
				if (constraints.direction == AccordeonDirection.BACKWARD) {
					return DockCursors.topCursor;
				} else {
					return DockCursors.bottomCursor;
				}
			}
		} else {
			// TODO
		}
		return Cursor.getDefaultCursor();
	}

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

	public void setInsets(int size) {
		setInsets(size, size, size, size);
	}

	public void setInsets(int top, int left, int bottom, int right) {
		setBorder(new EmptyBorder(top, left, bottom, right));
	}

	public Component createHeader(String title, Icon icon, final Component dropComponent, final Component undockComponent) {
		final ContentPanelHeader header = new ContentPanelHeader(this, title, icon, dropComponent, undockComponent);
		if (layout.getAxis() == X_AXIS) {
			header.setOrientationMinimized(HeaderOrientation.VERTICAL);
		}
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

		class C extends JLabel {
			private static final long serialVersionUID = 1L;

			public C(String s) {
				super(s);
				setBorder(new LineBorder(Color.lightGray, 1));
				setOpaque(true);
				setBackground(Color.white);
			}
		}

		AccordeonDockContainer accordeon = new AccordeonDockContainer(AccordeonLayout.X_AXIS, 2);
		accordeon.putClientProperty(ExpandSupport.COMPONENT_PROPERTY, true);
		for (int i = 0; i < 7; i++) {
			if (i != 2) {
				C lbl = new C("Accordeon " + i);
				Component c = DockContentPanel.create(accordeon, lbl, "Accordeon " + i, null);
				c.setPreferredSize(new Dimension(100, 30));
				accordeon.add(c);
			} else {
				GridDockContainer grid = new GridDockContainer(1);

				C lbl1 = new C("Ö");
				C lbl2 = new C("Ñ");
				C lbl3 = new C("Ç");
				C lbl4 = new C("Þ");
				C lbl5 = new C("Â");

				lbl1.setBackground(Color.yellow);
				lbl2.setBackground(Color.yellow);
				lbl3.setBackground(Color.yellow);
				lbl4.setBackground(Color.yellow);
				lbl5.setBackground(Color.yellow);

				Component c1 = DockContentPanel.create(grid, lbl1, "Ö", null);
				Component c2 = DockContentPanel.create(grid, lbl2, "Ñ", null);
				Component c3 = DockContentPanel.create(grid, lbl3, "Ç", null);
				Component c4 = DockContentPanel.create(grid, lbl4, "Þ", null);
				Component c5 = DockContentPanel.create(grid, lbl5, "Â", null);

				grid.addLeft(c1, true);
				grid.addLeft(c3, true);
				grid.addRight(c5, true);
				grid.addTop(c2, true);
				grid.addBottom(c4, true);

				accordeon.add(grid);
			}
		}

		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setContentPane(new JScrollPane(accordeon));
		f.setBounds(200, 50, 800, 800);
		f.setVisible(true);
	}
}
