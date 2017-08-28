package javax.swing.dock.grid;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.dock.ContentPanelHeader;
import javax.swing.dock.DockContentPanel;
import javax.swing.dock.DockCursors;
import javax.swing.dock.DockFlow;
import javax.swing.dock.DockUtils;
import javax.swing.dock.Dockable;
import javax.swing.dock.ExpandSupport;
import javax.swing.dock.ResizeSupport;
import javax.swing.dock.accordeon.AccordeonDockContainer;
import javax.swing.dock.accordeon.AccordeonLayout;
import javax.swing.dock.grid.GridDockConstraints.GridDockPosition;
import javax.swing.dock.selection.SelectionModel;
import javax.swing.dock.tab.TabDockContainer;

public class GridDockContainer extends JPanel implements Dockable<GridDockConstraints> {
	private static final long serialVersionUID = 1L;

	private GridLayout layout;

	private ResizeSupport resizeSupport;

	private int gap = 2;

	private SelectionModel selectionModel = new SelectionModel(this);

	public GridDockContainer(int gap) {
		layout = new GridLayout();
		resizeSupport = new ResizeSupport(this);

		selectionModel.setEnabled(false);

		setGap(gap);
		setLayout(layout);
		setMinimumSize(new Dimension(30, 30));
	}

	@Override
	public GridDockConstraints getDockConstraints(DockFlow flow, int x, int y) {
		// check on empty container
		if (getComponentCount() == 0) {
			GridDockConstraints p = new GridDockConstraints(0, 0, 1, 1);
			p.rect.setBounds(0, 0, getWidth(), getHeight());
			p.type = GridDockPosition.FIRST_INSERT;
			return p;
		}

		int minx = Integer.MAX_VALUE;
		int miny = Integer.MAX_VALUE;
		int maxx = -Integer.MAX_VALUE;
		int maxy = -Integer.MAX_VALUE;
		for (int i = 0; i < getComponentCount(); i++) {
			Component c = getComponent(i);
			if (c != flow.getUndockComponent()) {
				GridConstraints constraints = layout.getConstraints(c);
				minx = Math.min(minx, constraints.gridx);
				maxx = Math.max(maxx, constraints.gridx + constraints.gridwidth);
				miny = Math.min(miny, constraints.gridy);
				maxy = Math.max(maxy, constraints.gridy + constraints.gridheight);
			}
		}

		Component hor1 = DockUtils.getComponent(this, x - 5, y);
		Component hor2 = DockUtils.getComponent(this, x + 5, y);
		Component ver1 = DockUtils.getComponent(this, x, y - 5);
		Component ver2 = DockUtils.getComponent(this, x, y + 5);
		if (hor1 == hor2 && ver1 == ver2) {
			Component c = DockUtils.getComponent(this, x - 5, y);
			if (c != null) {
				x -= c.getX();
				y -= c.getY();
				GridConstraints constraints = layout.getConstraints(c);
				if (x <= y && x <= c.getHeight() - y && x <= c.getWidth() / 2) {
					// left
					GridDockConstraints p = new GridDockConstraints(constraints.gridx, constraints.gridy, 1, constraints.gridheight);
					p.rect.setBounds(c.getX(), c.getY(), c.getWidth() / 4, c.getHeight());
					p.type = GridDockPosition.HOR_LEFT;
					p.comp = c;
					return p;
				} else if (c.getWidth() - x <= y && c.getWidth() - x <= c.getHeight() - y && x >= c.getWidth() / 2) {
					// right
					GridDockConstraints p = new GridDockConstraints(constraints.gridx + constraints.gridwidth, constraints.gridy, 1, constraints.gridheight);
					p.rect.setBounds(c.getX() + 3 * c.getWidth() / 4, c.getY(), c.getWidth() / 4, c.getHeight());
					p.type = GridDockPosition.HOR_RIGHT;
					p.comp = c;
					return p;
				} else if (y <= c.getHeight() / 2) {
					// top
					GridDockConstraints p = new GridDockConstraints(constraints.gridx, constraints.gridy, constraints.gridwidth, 1);
					p.rect.setBounds(c.getX(), c.getY(), c.getWidth(), c.getHeight() / 4);
					p.type = GridDockPosition.VER_TOP;
					p.comp = c;
					return p;
				} else if (y >= c.getHeight() / 2) {
					// bottom
					GridDockConstraints p = new GridDockConstraints(constraints.gridx, constraints.gridy + constraints.gridheight, constraints.gridwidth, 1);
					p.rect.setBounds(c.getX(), c.getY() + 3 * c.getHeight() / 4, c.getWidth(), c.getHeight() / 4);
					p.type = GridDockPosition.VER_BOTTOM;
					p.comp = c;
					return p;
				}
			} else {
				// assumed there are not empty areas
			}
			return null;
		}

		List<Component> sideHor1 = new ArrayList<Component>();
		List<Component> sideHor2 = new ArrayList<Component>();
		List<Component> sideHorInter = new ArrayList<Component>();
		List<Component> sideVer1 = new ArrayList<Component>();
		List<Component> sideVer2 = new ArrayList<Component>();
		List<Component> sideVerInter = new ArrayList<Component>();

		for (int i = 0; i < getComponentCount(); i++) {
			Component c = getComponent(i);
			if (c != flow.getUndockComponent()) {
				GridConstraints constraints = layout.getConstraints(c);
				if (hor1 != hor2) {
					if (c.getX() >= x - 5 && c.getX() <= x + 5) {
						sideHor2.add(c);
					} else if (c.getX() + c.getWidth() >= x - 5 && c.getX() + c.getWidth() <= x + 5) {
						sideHor1.add(c);
					} else if (c.getX() < x - 5 && c.getX() + c.getWidth() > x + 5) {
						sideHorInter.add(c);
					}
				}

				if (ver1 != ver2) {
					if (c.getY() >= y - 5 && c.getY() <= y + 5) {
						sideVer2.add(c);
					} else if (c.getY() + c.getHeight() >= y - 5 && c.getY() + c.getHeight() <= y + 5) {
						sideVer1.add(c);
					} else if (c.getY() < y - 5 && c.getY() + c.getHeight() > y + 5) {
						sideVerInter.add(c);
					}
				}
			}
		}

		if (x < 10) {
			// left
			GridDockConstraints p = new GridDockConstraints(minx - 1, miny, 1, maxy - miny);
			p.rect.setBounds(0, 0, getWidth() / 4, getHeight());
			p.type = GridDockPosition.BORDER_LEFT;
			return p;
		} else if (x > getWidth() - 10) {
			// right
			GridDockConstraints p = new GridDockConstraints(maxx, miny, 1, maxy - miny);
			p.rect.setBounds(3 * getWidth() / 4, 0, getWidth() / 4, getHeight());
			p.type = GridDockPosition.BORDER_RIGHT;
			return p;
		} else if (y < 10) {
			// top
			GridDockConstraints p = new GridDockConstraints(minx, miny - 1, maxx - minx, 1);
			p.rect.setBounds(0, 0, getWidth(), getHeight() / 4);
			p.type = GridDockPosition.BORDER_TOP;
			return p;
		} else if (y > getHeight() - 10) {
			// bottom
			GridDockConstraints p = new GridDockConstraints(minx, maxy, maxx - minx, 1);
			p.rect.setBounds(0, 3 * getHeight() / 4, getWidth(), getHeight() / 4);
			p.type = GridDockPosition.BORDER_BOTTOM;
			return p;
		} else {
			// middle
			if (hor1 != hor2 && hor1 != null && hor2 != null) {
				GridConstraints constrHor1 = layout.getConstraints(hor1);
				GridConstraints constrHor2 = layout.getConstraints(hor2);

				int x1 = constrHor1.gridx + constrHor1.gridwidth;
				int ymax1 = Math.min(constrHor1.gridy, constrHor2.gridy);
				int ymin2 = Math.max(constrHor1.gridy + constrHor1.gridheight, constrHor2.gridy + constrHor2.gridheight);

				int y1 = miny;
				int y2 = maxy;
				int posy1 = 0;
				int posy2 = getHeight();
				for (Component inter : sideHorInter) {
					GridConstraints constrInter = layout.getConstraints(inter);
					int interY1 = constrInter.gridy;
					int interY2 = constrInter.gridy + constrInter.gridheight;
					if (interY2 <= ymax1) {
						if (y1 <= interY2) {
							y1 = interY2;
							posy1 = inter.getY() + inter.getHeight();
						}
					} else if (interY1 >= ymin2) {
						if (y2 >= interY1) {
							y2 = interY1;
							posy2 = inter.getY();
						}
					}
				}

				int mx = (hor1.getX() + hor1.getWidth() + hor2.getX()) / 2;

				GridDockConstraints p = new GridDockConstraints(x1, y1, 1, y2 - y1);
				p.rect.setBounds(mx - 10, posy1, 20, posy2 - posy1);
				p.type = GridDockPosition.HOR_CENTER;
				return p;
			} else if (ver1 != ver2 && ver1 != null && ver2 != null) {
				GridConstraints constrVer1 = layout.getConstraints(ver1);
				GridConstraints constrVer2 = layout.getConstraints(ver2);

				int y1 = constrVer1.gridy + constrVer1.gridheight;
				int xmax1 = Math.min(constrVer1.gridx, constrVer2.gridx);
				int xmin2 = Math.max(constrVer1.gridx + constrVer1.gridwidth, constrVer2.gridx + constrVer2.gridwidth);

				int x1 = minx;
				int x2 = maxx;
				int posx1 = 0;
				int posx2 = getWidth();
				for (Component inter : sideVerInter) {
					GridConstraints constrInter = layout.getConstraints(inter);
					int interX1 = constrInter.gridx;
					int interX2 = constrInter.gridx + constrInter.gridwidth;
					if (interX2 <= xmax1) {
						if (x1 <= interX2) {
							x1 = interX2;
							posx1 = inter.getX() + inter.getWidth();
						}
					} else if (interX1 >= xmin2) {
						if (x2 >= interX1) {
							x2 = interX1;
							posx2 = inter.getX();
						}
					}
				}

				int my = (ver1.getY() + ver1.getHeight() + ver2.getY()) / 2;

				GridDockConstraints p = new GridDockConstraints(x1, y1, x2 - x1, 1);
				p.rect.setBounds(posx1, my - 10, posx2 - posx1, 20);
				p.type = GridDockPosition.VER_CENTER;
				return p;
			}
		}
		return null;
	}

	@Override
	public void drawDockPlace(Graphics g, DockFlow flow, GridDockConstraints constraints) {
		DockUtils.drawDockRect(g, constraints.rect.x, constraints.rect.y, constraints.rect.width, constraints.rect.height);
	}

	@Override
	public void dock(DockFlow flow, GridDockConstraints constraints) {
		if (flow != null && constraints != null) {
			// build constraints
			GridConstraints flowNewConstraints = null;
			GridConstraints flowOldConstraints = null;

			if (getComponentCount() > 1) {
				// shift to zero point
				GridDockLayoutHelper.shiftToZeroPoint(this);
			}

			if (flow.getUndockComponent().getParent().getLayout() instanceof GridLayout) {
				GridLayout flowParentLayout = (GridLayout) flow.getUndockComponent().getParent().getLayout();
				flowOldConstraints = flowParentLayout.getConstraints(flow.getUndockComponent());
			}

			// remove flow
			flow.removeUndockComponent();

			// if (getComponentCount() > 1) {
			// fill empty areas
			// GridDockLayoutHelper.fillEmptyAreas(this);
			// }

			// update component's constraints
			switch (constraints.type) {
				case BORDER_TOP:
				case BORDER_LEFT:
				case BORDER_BOTTOM:
				case BORDER_RIGHT:
					break;
				case HOR_LEFT:
					GridConstraints compConstr = layout.getConstraints(constraints.comp);
					if (compConstr.gridwidth > 1) {
						compConstr.gridx++;
						compConstr.gridwidth--;
						layout.setConstraints(constraints.comp, compConstr);
					} else {
						for (int i = 0; i < getComponentCount(); i++) {
							Component c = getComponent(i);
							if (c != flow.getUndockComponent()) {
								GridConstraints constr = layout.getConstraints(c);
								if (c != constraints.comp) {
									if (constr.gridx <= constraints.x && constr.gridx + constr.gridwidth > constraints.x) {
										constr.gridwidth++;
										layout.setConstraints(c, constr);
									} else if (constr.gridx > constraints.x) {
										constr.gridx++;
										layout.setConstraints(c, constr);
									}
								} else {
									constr.gridx++;
									layout.setConstraints(c, constr);
								}
							}
						}
					}
					break;
				case HOR_RIGHT:
					compConstr = layout.getConstraints(constraints.comp);
					if (compConstr.gridwidth > 1) {
						compConstr.gridwidth--;
						constraints.x--;
						layout.setConstraints(constraints.comp, compConstr);
					} else {
						for (int i = 0; i < getComponentCount(); i++) {
							Component c = getComponent(i);
							if (c != flow.getUndockComponent()) {
								GridConstraints constr = layout.getConstraints(c);
								if (c != constraints.comp) {
									if (constr.gridx < constraints.x && constr.gridx + constr.gridwidth >= constraints.x) {
										constr.gridwidth++;
										layout.setConstraints(c, constr);
									} else if (constr.gridx >= constraints.x) {
										constr.gridx++;
										layout.setConstraints(c, constr);
									}
								}
							}
						}
					}
					break;
				case VER_TOP:
					compConstr = layout.getConstraints(constraints.comp);
					if (compConstr.gridheight > 1) {
						compConstr.gridy++;
						compConstr.gridheight--;
						layout.setConstraints(constraints.comp, compConstr);
					} else {
						for (int i = 0; i < getComponentCount(); i++) {
							Component c = getComponent(i);
							if (c != flow.getUndockComponent()) {
								GridConstraints constr = layout.getConstraints(c);
								if (c != constraints.comp) {
									if (constr.gridy <= constraints.y && constr.gridy + constr.gridheight > constraints.y) {
										constr.gridheight++;
										layout.setConstraints(c, constr);
									} else if (constr.gridy > constraints.y) {
										constr.gridy++;
										layout.setConstraints(c, constr);
									}
								} else {
									constr.gridy++;
									layout.setConstraints(c, constr);
								}
							}
						}
					}
					break;
				case VER_BOTTOM:
					compConstr = layout.getConstraints(constraints.comp);
					if (compConstr.gridheight > 1) {
						compConstr.gridheight--;
						constraints.y--;
						layout.setConstraints(constraints.comp, compConstr);
					} else {
						for (int i = 0; i < getComponentCount(); i++) {
							Component c = getComponent(i);
							if (c != flow.getUndockComponent()) {
								GridConstraints constr = layout.getConstraints(c);
								if (c != constraints.comp) {
									if (constr.gridy < constraints.y && constr.gridy + constr.gridheight >= constraints.y) {
										constr.gridheight++;
										layout.setConstraints(c, constr);
									} else if (constr.gridy >= constraints.y) {
										constr.gridy++;
										layout.setConstraints(c, constr);
									}
								}
							}
						}
					}
					break;
				case HOR_CENTER:
					for (int i = 0; i < getComponentCount(); i++) {
						Component c = getComponent(i);
						if (c != flow.getUndockComponent()) {
							GridConstraints constr = layout.getConstraints(c);
							if (constr.gridx <= constraints.x && constr.gridx + constr.gridwidth > constraints.x) {
								if (constr.gridy >= constraints.y + constraints.h || constr.gridy + constr.gridheight <= constraints.y) {
									constr.gridwidth++;
									layout.setConstraints(c, constr);
								} else {
									constr.gridx++;
									layout.setConstraints(c, constr);
								}
							} else if (constr.gridx > constraints.x) {
								constr.gridx++;
								layout.setConstraints(c, constr);
							}
						}
					}
					break;
				case VER_CENTER:
					for (int i = 0; i < getComponentCount(); i++) {
						Component c = getComponent(i);
						if (c != flow.getUndockComponent()) {
							GridConstraints constr = layout.getConstraints(c);
							if (constr.gridy <= constraints.y && constr.gridy + constr.gridheight > constraints.y) {
								if (constr.gridx >= constraints.x + constraints.w || constr.gridx + constr.gridwidth <= constraints.x) {
									constr.gridheight++;
									layout.setConstraints(c, constr);
								} else {
									constr.gridy++;
									layout.setConstraints(c, constr);
								}
							} else if (constr.gridy > constraints.y) {
								constr.gridy++;
								layout.setConstraints(c, constr);
							}
						}
					}
					break;
				case FIRST_INSERT:
					// do nothing
					break;
			}

			// create new constraints
			Insets insets = new Insets(gap, gap, gap, gap);
			flowNewConstraints = new GridConstraints(constraints.x, constraints.y, constraints.w, constraints.h, 1, 1, GridConstraints.CENTER, GridConstraints.BOTH, insets, 0, 0);

			// copy old constraints info
			if (flowOldConstraints != null) {
				flowNewConstraints.weightx = flowOldConstraints.weightx;
				flowNewConstraints.weighty = flowOldConstraints.weighty;
				flowNewConstraints.fill = flowOldConstraints.fill;
				flowNewConstraints.anchor = flowOldConstraints.anchor;
				flowNewConstraints.insets = flowOldConstraints.insets;
			}

			// Free place for dock component.
			// Required for docking component rect area (in constraints term) may be busy
			// after previous fillEmptyAreas operation, which were performed while undocking
			// of this component.
			GridDockLayoutHelper.freePlace(this, flowNewConstraints);

			// add
			Component contentPanel = DockContentPanel.create(this, flow.getDropComponent(), flow.getName(), flow.getIcon());
			add(contentPanel, flowNewConstraints);

			if (getComponentCount() > 1) {
				// shift to zero point
				GridDockLayoutHelper.shiftToZeroPoint(this);
				// fill empty areas
				GridDockLayoutHelper.fillEmptyAreas(this);
			}

			revalidate();
		}
	}

	@Override
	public void undock(DockFlow flow) {
		GridDockLayoutHelper.shiftToZeroPoint(this);
		GridDockLayoutHelper.fillEmptyAreas(this);
		revalidate();
	}

	@Override
	public Cursor getDockCursor(DockFlow flow, GridDockConstraints constraints) {
		switch (constraints.type) {
			case BORDER_BOTTOM:
			case VER_BOTTOM:
				return DockCursors.bottomCursor;
			case BORDER_LEFT:
			case HOR_LEFT:
				return DockCursors.leftCursor;
			case BORDER_RIGHT:
			case HOR_RIGHT:
				return DockCursors.rightCursor;
			case BORDER_TOP:
			case VER_TOP:
				return DockCursors.topCursor;
			case HOR_CENTER:
				return DockCursors.horInsertCursor;
			case VER_CENTER:
				return DockCursors.verInsertCursor;
		}
		return Cursor.getDefaultCursor();
	}

	public void addLeft(Component c, boolean fill) {
		addLeft(c, fill, fill);
	}

	public void addRight(Component c, boolean fill) {
		addRight(c, fill, fill);
	}

	public void addTop(Component c, boolean fill) {
		addTop(c, fill, fill);
	}

	public void addBottom(Component c, boolean fill) {
		addBottom(c, fill, fill);
	}

	public void addLeft(Component c, boolean hfill, boolean vfill) {
		double hweight = hfill ? 1 : 0;
		double vweight = vfill ? 1 : 0;
		int[] gridBounds = GridDockLayoutHelper.getGridBounds(this);
		int h = Math.max(1, gridBounds[3] - gridBounds[1]);
		add(c, new GridConstraints(-1, 0, 1, h, hweight, vweight, GridConstraints.CENTER, GridConstraints.BOTH, new Insets(gap, gap, gap, gap), 0, 0));
		GridDockLayoutHelper.shiftToZeroPoint(this);
		revalidate();
	}

	public void addRight(Component c, boolean hfill, boolean vfill) {
		double hweight = hfill ? 1 : 0;
		double vweight = vfill ? 1 : 0;
		int[] gridBounds = GridDockLayoutHelper.getGridBounds(this);
		int h = Math.max(1, gridBounds[3] - gridBounds[1]);
		add(c, new GridConstraints(gridBounds[2], 0, 1, h, hweight, vweight, GridConstraints.CENTER, GridConstraints.BOTH, new Insets(gap, gap, gap, gap), 0, 0));
		GridDockLayoutHelper.shiftToZeroPoint(this);
		revalidate();
	}

	public void addTop(Component c, boolean hfill, boolean vfill) {
		double hweight = hfill ? 1 : 0;
		double vweight = vfill ? 1 : 0;
		int[] gridBounds = GridDockLayoutHelper.getGridBounds(this);
		int w = Math.max(1, gridBounds[2] - gridBounds[0]);
		add(c, new GridConstraints(0, -1, w, 1, hweight, vweight, GridConstraints.CENTER, GridConstraints.BOTH, new Insets(gap, gap, gap, gap), 0, 0));
		GridDockLayoutHelper.shiftToZeroPoint(this);
		revalidate();
	}

	public void addBottom(Component c, boolean hfill, boolean vfill) {
		double hweight = hfill ? 1 : 0;
		double vweight = vfill ? 1 : 0;
		int[] gridBounds = GridDockLayoutHelper.getGridBounds(this);
		int w = Math.max(1, gridBounds[2] - gridBounds[0]);
		add(c, new GridConstraints(0, gridBounds[3], w, 1, hweight, vweight, GridConstraints.CENTER, GridConstraints.BOTH, new Insets(gap, gap, gap, gap), 0, 0));
		GridDockLayoutHelper.shiftToZeroPoint(this);
		revalidate();
	}

	public int getGap() {
		return gap;
	}

	public void setGap(int gap) {
		this.gap = gap;
	}

	@Override
	public ResizeSupport getResizeSupport() {
		return resizeSupport;
	}

	public void setInsets(int size) {
		setInsets(size, size, size, size);
	}

	public void setInsets(int top, int left, int bottom, int right) {
		setBorder(new EmptyBorder(top, left, bottom, right));
	}

	@Override
	public Component createHeader(final DockFlow flow) {
		ContentPanelHeader header = new ContentPanelHeader(this, flow.getName(), flow.getIcon(), flow.getDropComponent(), flow.getUndockComponent());
		header.addMaximizeAction();
		header.addCloseAction();
		return header;
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

		GridDockContainer pnlContent = new GridDockContainer(1);
		pnlContent.putClientProperty(ExpandSupport.COMPONENT_PROPERTY, true);

		GridDockContainer grid = new GridDockContainer(1);
		pnlContent.addRight(grid, true);

		C lbl1 = new C("Центр");
		C lbl2 = new C("Север");
		C lbl3 = new C("Запад");
		C lbl4 = new C("Юг");
		C lbl5 = new C("Восток");

		Component c1 = DockContentPanel.create(grid, lbl1, "Центр", null);
		Component c2 = DockContentPanel.create(grid, lbl2, "Север", null);
		Component c3 = DockContentPanel.create(grid, lbl3, "Запад", null);
		Component c4 = DockContentPanel.create(grid, lbl4, "Юг", null);
		Component c5 = DockContentPanel.create(grid, lbl5, "Восток", null);

		c1.setPreferredSize(new Dimension(100, 100));
		c2.setPreferredSize(new Dimension(100, 100));
		c3.setPreferredSize(new Dimension(100, 100));
		c4.setPreferredSize(new Dimension(100, 100));
		c5.setPreferredSize(new Dimension(100, 100));

		grid.addLeft(c1, true);
		grid.addLeft(c3, true);
		grid.addRight(c5, true);
		grid.addTop(c2, true);
		grid.addBottom(c4, true);

		AccordeonDockContainer accordeon = new AccordeonDockContainer(AccordeonLayout.Y_AXIS, 2);
		pnlContent.addRight(new JScrollPane(accordeon), true);
		for (int i = 0; i < 5; i++) {
			if (i != 3) {
				C lbl = new C("Accordeon " + i);
				Component c = DockContentPanel.create(accordeon, lbl, "Accordeon " + i, null);
				c.setPreferredSize(new Dimension(100, 30));
				accordeon.add(c);
			} else {
				grid = new GridDockContainer(1);

				lbl1 = new C("Ц");
				lbl2 = new C("С");
				lbl3 = new C("З");
				lbl4 = new C("Ю");
				lbl5 = new C("В");

				lbl1.setBackground(Color.yellow);
				lbl2.setBackground(Color.yellow);
				lbl3.setBackground(Color.yellow);
				lbl4.setBackground(Color.yellow);
				lbl5.setBackground(Color.yellow);

				c1 = DockContentPanel.create(grid, lbl1, "Ц", null);
				c2 = DockContentPanel.create(grid, lbl2, "С", null);
				c3 = DockContentPanel.create(grid, lbl3, "З", null);
				c4 = DockContentPanel.create(grid, lbl4, "Ю", null);
				c5 = DockContentPanel.create(grid, lbl5, "В", null);

				grid.addLeft(c1, true);
				grid.addLeft(c3, true);
				grid.addRight(c5, true);
				grid.addTop(c2, true);
				grid.addBottom(c4, true);

				accordeon.add(grid);
			}
		}

		TabDockContainer tab = new TabDockContainer("Tabs", null);
		for (int i = 1; i <= 20; i++) {
			C lbl = new C("Tab " + i);
			tab.addDockTab("Tab " + i, lbl);
		}
		pnlContent.addTop(tab, true);

		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setContentPane(pnlContent);
		f.setBounds(300, 100, 900, 600);
		f.setVisible(true);
	}
}
