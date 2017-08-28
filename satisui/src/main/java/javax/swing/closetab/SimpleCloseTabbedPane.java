package javax.swing.closetab;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JTabbedPane;
import javax.swing.plaf.TabbedPaneUI;

public class SimpleCloseTabbedPane extends JTabbedPane {
	private static final long serialVersionUID = 1L;
	private List<TabRollOverListener> rollListeners;

	public SimpleCloseTabbedPane() {
		this(TOP, SCROLL_TAB_LAYOUT);
	}

	public SimpleCloseTabbedPane(int tabPlacement) {
		this(tabPlacement, SCROLL_TAB_LAYOUT);
	}

	public SimpleCloseTabbedPane(int tabPlacement, int tabLayoutPolicy) {
		super(tabPlacement, tabLayoutPolicy);
	}

	public void fireTabRollOver(TabRolloverEvent e) {
		if (rollListeners == null || rollListeners.isEmpty()) {
			return;
		}

		for (int i = 0, k = rollListeners.size(); i < k; i++) {
			rollListeners.get(i).tabRollOver(e);
		}
	}

	public void fireTabRollOverFinished(TabRolloverEvent e) {
		if (rollListeners == null || rollListeners.isEmpty()) {
			return;
		}

		for (int i = 0, k = rollListeners.size(); i < k; i++) {
			rollListeners.get(i).tabRollOverFinished(e);
		}
	}

	public void addTabRollOverListener(TabRollOverListener listener) {
		if (rollListeners == null) {
			rollListeners = new ArrayList<TabRollOverListener>();
		}
		rollListeners.add(listener);

	}

	public void removeTabRollOverListener(TabRollOverListener listener) {
		if (rollListeners == null) {
			return;
		}
		rollListeners.remove(listener);
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
	public void addTab(String title, Icon icon, Component component, String tip) {
		// make sure the pane is visible - may have been empty
		if (!isVisible()) {
			setVisible(true);
		}
		super.addTab(title, icon, component, tip);
	}

	@Override
	public void insertTab(String title, Icon icon, Component component, String tip, int index) {
		// make sure the pane is visible - may have been empty
		if (!isVisible()) {
			setVisible(true);
		}
		super.insertTab(title, icon, component, tip, index);
	}

	@Override
	public void removeAll() {
		super.removeAll();
		setVisible(false);
	}

	@Override
	public void remove(int index) {
		super.remove(index);
		if (getTabCount() == 0) {
			setVisible(false);
		}
	}

	@Override
	public void remove(Component c) {
		super.remove(c);
		if (getTabCount() == 0) {
			setVisible(false);
		}
	}

	protected CloseTabbedPaneUI tabUI;

	@Override
	public TabbedPaneUI getUI() {
		return tabUI;
	}

	@Override
	public void updateUI() {
		tabUI = new CloseTabbedPaneUI();
		setUI(tabUI);
	}
}
