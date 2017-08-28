package javax.swing.dock.tab;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.dock.DockCursors;
import javax.swing.dock.DockFlow;
import javax.swing.dock.DockFlowSupport;
import javax.swing.dock.DockUtils;
import javax.swing.dock.Dockable;
import javax.swing.dock.ExpandSupport;
import javax.swing.dock.ResizeSupport;
import javax.swing.dock.selection.SelectionModel;

public class TabDockContainer extends JTabbedPane implements Dockable<TabDockConstraints> {
	private static final long serialVersionUID = 1L;
	private ExpandSupport expandSupport;

	public TabDockContainer(String title, Icon icon) {
		setMinimumSize(new Dimension(30, 30));
		setTabLayoutPolicy(SCROLL_TAB_LAYOUT);

		expandSupport = new ExpandSupport(this, this);
		expandSupport.expandOnDoubleClick(this);

		new DockFlowSupport(this, this, this, title, icon);
	}

	public void addDockTab(String title, Icon icon, Component component) {
		insertDockTab(title, icon, component, null, getTabCount());
	}

	public void addDockTab(String title, Component component) {
		insertDockTab(title, null, component, null, getTabCount());
	}

	public void addDockTab(String title, Icon icon, Component component, String tip) {
		insertDockTab(title, icon, component, tip, getTabCount());
	}

	public void insertDockTab(String title, Icon icon, Component component, String tip, int index) {
		super.insertTab(" ", icon, component, tip, index);
		TabDockHeader header = new TabDockHeader(this, title, icon, component);
		header.setExpandSupport(expandSupport);
		setTabComponentAt(indexOfComponent(component), header);
	}

	@Override
	public TabDockConstraints getDockConstraints(DockFlow flow, int x, int y) {
		// check on empty tab
		if (getTabCount() == 0) {
			return new TabDockConstraints(TabDockConstraints.CENTER);
		}

		// find tab header
		Point p1 = getLocationOnScreen();
		Component c = getSelectedComponent();
		Point p3 = c.getLocationOnScreen();
		for (int i = 0; i < getTabCount(); i++) {
			Component header = getTabComponentAt(i);
			Point p2 = header.getLocationOnScreen();

			int x1 = p2.x - p1.x - 5;
			int y1 = 0;
			int x2 = x1 + header.getWidth() + 10;
			int y2 = y1 + p3.y - p2.y;
			if (x >= x1 && x <= x2 && y >= y1 && y <= y2) {
				return new TabDockConstraints(i);
			}
		}

		// check left area of headers
		Component lastHeader = getTabComponentAt(getTabCount() - 1);
		Point p2 = lastHeader.getLocationOnScreen();
		int hx = p2.x - p1.x;
		int hy = p2.y - p1.y;
		if (y >= hy && y <= hy + lastHeader.getHeight() && x > hx + lastHeader.getWidth()) {
			if (indexOfComponent(flow.getUndockComponent()) == -1) {
				return new TabDockConstraints(TabDockConstraints.END);
			} else {
				return null;
			}
		}

		// check on add to the end
		if (y > hy + lastHeader.getHeight()) {
			return new TabDockConstraints(TabDockConstraints.CENTER);
		}
		return null;
	}

	@Override
	public void drawDockPlace(Graphics g, DockFlow flow, TabDockConstraints constraints) {
		int x = 0;
		int y = 0;
		int w = 0;
		int h = 0;
		if (constraints.getIndex() >= 0) {
			Component header = getTabComponentAt(constraints.getIndex());
			Component c = getSelectedComponent();

			Point p1 = getLocationOnScreen();
			Point p2 = header.getLocationOnScreen();
			Point p3 = c.getLocationOnScreen();

			x = p2.x - p1.x - 5;
			y = 0;
			w = header.getWidth() + 10;
			h = p3.y - p2.y;
		} else if (constraints.getIndex() == TabDockConstraints.CENTER) {
			x = 0;
			y = 0;
			w = getWidth();
			h = getHeight();
		} else if (constraints.getIndex() == TabDockConstraints.END) {
			Component header = getTabComponentAt(getTabCount() - 1);
			Component c = getSelectedComponent();

			Point p1 = getLocationOnScreen();
			Point p2 = header.getLocationOnScreen();
			Point p3 = c.getLocationOnScreen();

			x = p2.x - p1.x + header.getWidth() + 5;
			y = 0;
			w = header.getWidth() + 10;
			h = p3.y - p2.y;
		}

		DockUtils.drawDockRect(g, x, y, w, h);
	}

	@Override
	public void dock(DockFlow flow, TabDockConstraints constraints) {
		int dockIndex = constraints.getIndex();
		if (dockIndex >= 0) {
			int index = indexOfComponent(flow.getUndockComponent());
			if (index != -1) {
				if (index == dockIndex) {
					return;
				}
			}
		} else if (dockIndex == TabDockConstraints.CENTER || dockIndex == TabDockConstraints.END) {
			int index = indexOfComponent(flow.getUndockComponent());
			if (dockIndex == TabDockConstraints.CENTER && index != -1) {
				return;
			}
			dockIndex = getTabCount();
		}

		flow.removeUndockComponent();

		insertDockTab(flow.getName(), flow.getIcon(), flow.getDropComponent(), null, dockIndex);
		setSelectedComponent(flow.getDropComponent());
	}

	@Override
	public Cursor getDockCursor(DockFlow flow, TabDockConstraints constraints) {
		return DockCursors.tabCursor;
	}

	@Override
	public void undock(DockFlow flow) {
		int index = indexOfComponent(flow.getDropComponent());
		if (index != -1) {
			removeTabAt(index);
		}
	}

	@Override
	public Component createHeader(DockFlow flow) {
		TabDockHeader header = new TabDockHeader(this, flow.getName(), flow.getIcon(), flow.getDropComponent());
		header.setExpandSupport(expandSupport);
		return header;
	}

	public void setInsets(int size) {
		setInsets(size, size, size, size);
	}

	public void setInsets(int top, int left, int bottom, int right) {
		setBorder(new EmptyBorder(top, left, bottom, right));
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
	public void paint(Graphics g) {
		super.paint(g);
		g.setColor(Color.lightGray);
		g.drawLine(1, 1, getWidth() - 3, 1);
		g.drawLine(1, 1, 1, 22);
		g.drawLine(getWidth() - 3, 1, getWidth() - 3, 22);
	}

	@Override
	public ResizeSupport getResizeSupport() {
		return null;
	}

	@Override
	public SelectionModel getSelectionModel() {
		return null;
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		JPanel content = new JPanel();
		content.putClientProperty(ExpandSupport.COMPONENT_PROPERTY, true);
		content.setLayout(new GridLayout(2, 2));

		for (int n = 0; n < 4; n++) {
			TabDockContainer tab = new TabDockContainer("Tabs", null);
			for (int i = 1; i <= 5; i++) {
				tab.addDockTab("Tab " + i, null, new JLabel("Component " + i), null);
			}
			content.add(tab);
		}

		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setContentPane(content);
		f.setBounds(300, 100, 900, 600);
		f.setVisible(true);
	}
}
