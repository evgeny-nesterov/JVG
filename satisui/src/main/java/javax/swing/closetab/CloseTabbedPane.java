package javax.swing.closetab;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;

public class CloseTabbedPane extends JTabbedPane {
	private static final long serialVersionUID = 1L;

	/** whether the default popup is enabled */
	private boolean tabPopupEnabled;

	/** the popup menu */
	private TabPopupMenu popup;

	public CloseTabbedPane() {
		this(TOP, SCROLL_TAB_LAYOUT);
	}

	public CloseTabbedPane(int tabPlacement) {
		this(tabPlacement, SCROLL_TAB_LAYOUT);
	}

	public CloseTabbedPane(int tabPlacement, int tabLayoutPolicy) {
		super(tabPlacement, tabLayoutPolicy);
	}

	@Override
	public void addTab(String title, Component component) {
		addTab(title, null, component, null);
	}

	@Override
	public void addTab(String title, Icon icon, Component component) {
		addTab(title, icon, component, null);
	}

	@Override
	public void insertTab(String title, Icon icon, Component component, String tip, int index) {
		// make sure the pane is visible - may have been empty
		if (!isVisible()) {
			setVisible(true);
		}

		CloseTabContentPanel _component = new CloseTabContentPanel(tabPlacement, component);
		super.insertTab(title, icon, _component, tip, index);

		if (tabPopupEnabled) {
			TabMenuItem menuItem = addAssociatedMenu(title, icon, _component);
			_component.setTabMenuItem(menuItem);
		}
	}

	@Override
	public void addTab(String title, Icon icon, Component component, String tip) {
		// make sure the pane is visible - may have been empty
		if (!isVisible()) {
			setVisible(true);
		}

		CloseTabContentPanel _component = new CloseTabContentPanel(tabPlacement, component);
		super.addTab(title, icon, _component, tip);

		if (tabPopupEnabled) {
			TabMenuItem menuItem = addAssociatedMenu(title, icon, _component);
			_component.setTabMenuItem(menuItem);
		}
	}

	protected TabMenuItem addAssociatedMenu(String title, Icon icon, Component component) {
		TabMenuItem menuItem = new TabMenuItem(title, icon, component);
		popup.addTabSelectionMenuItem(menuItem);
		return menuItem;
	}

	@Override
	public void removeAll() {
		popup.removeAllTabSelectionMenuItems();
		super.removeAll();
		setVisible(false);
	}

	@Override
	public void remove(int index) {
		CloseTabContentPanel component = (CloseTabContentPanel) getComponentAt(index);
		if (tabPopupEnabled) {
			popup.removeTabSelectionMenuItem(component.getTabMenuItem());
		}
		super.remove(index);
		if (getTabCount() == 0) {
			setVisible(false);
		}
	}

	// public void updateUI() {
	// setUI(new CloseTabbedPaneUI());
	// }

	public boolean isTabPopupEnabled() {
		return tabPopupEnabled;
	}

	public void setTabPopupEnabled(boolean tabPopupEnabled) {
		this.tabPopupEnabled = tabPopupEnabled;
		if (tabPopupEnabled && popup == null) {
			popup = new TabPopupMenu(this);
		}
	}

	public void showPopup(int index, int x, int y) {
		popup.setHoverTabIndex(index);
		popup.show(this, x, y);
	}

	private class TabPopupMenu extends JPopupMenu implements ActionListener {
		private static final long serialVersionUID = 1L;

		private JMenu openTabs;

		private JMenuItem close;

		private JMenuItem closeAll;

		private JMenuItem closeOther;

		private JTabbedPane tabPane;

		private int hoverTabIndex;

		public TabPopupMenu(JTabbedPane tabPane) {
			this.tabPane = tabPane;

			close = new JMenuItem("Close");
			closeAll = new JMenuItem("Close All");
			closeOther = new JMenuItem("Close Others");

			close.addActionListener(this);
			closeAll.addActionListener(this);
			closeOther.addActionListener(this);

			add(close);
			add(closeAll);
			add(closeOther);

			hoverTabIndex = -1;
		}

		public void addTabSelectionMenuItem(TabMenuItem menuItem) {
			if (openTabs == null) {
				addSeparator();
				openTabs = new JMenu("Select");
				add(openTabs);
			}
			menuItem.addActionListener(this);
			openTabs.add(menuItem);
		}

		public void removeAllTabSelectionMenuItems() {
			if (openTabs == null) {
				return;
			}
			openTabs.removeAll();
		}

		public void removeTabSelectionMenuItem(TabMenuItem menuItem) {
			if (openTabs == null) {
				return;
			}
			openTabs.remove(menuItem);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (hoverTabIndex == -1) {
				return;
			}

			Object source = e.getSource();
			if (source == close) {
				tabPane.remove(hoverTabIndex);
			} else if (source == closeAll) {
				tabPane.removeAll();
			} else if (source == closeOther) {
				int count = 0;
				int tabCount = tabPane.getTabCount();
				Component[] tabs = new Component[tabCount - 1];
				for (int i = 0; i < tabCount; i++) {
					if (i != hoverTabIndex) {
						tabs[count++] = tabPane.getComponentAt(i);
					}
				}
				for (int i = 0; i < tabs.length; i++) {
					tabPane.remove(tabs[i]);
				}
			} else if (source instanceof TabMenuItem) {
				TabMenuItem item = (TabMenuItem) source;
				tabPane.setSelectedComponent(item.getTabComponent());
			}
		}

		public int getHoverTabIndex() {
			return hoverTabIndex;
		}

		public void setHoverTabIndex(int hoverTabIndex) {
			this.hoverTabIndex = hoverTabIndex;
		}
	}
}
