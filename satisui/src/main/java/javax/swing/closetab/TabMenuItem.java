package javax.swing.closetab;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JMenuItem;

public class TabMenuItem extends JMenuItem {
	private static final long serialVersionUID = 1L;
	/** the tab component for this menu item */
	private Component tabComponent;

	/** Creates a new instance of TabMenuItem */
	public TabMenuItem(String title, Component component) {
		this(title, null, component);
	}

	/** Creates a new instance of TabMenuItem */
	public TabMenuItem(String title, Icon icon, Component component) {
		super(title, icon);
		this.tabComponent = component;
	}

	public Component getTabComponent() {
		return tabComponent;
	}

	public void setTabComponent(Component component) {
		this.tabComponent = component;
	}
}
