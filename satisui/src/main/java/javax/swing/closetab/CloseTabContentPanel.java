package javax.swing.closetab;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;

public class CloseTabContentPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	/** the highlight border width */
	private static final int BORDER_WIDTH = 4;

	/** The added tab panel border */
	private static Border border;

	/** the active color for the tab border */
	private static Color activeColor;

	/** the displayed component */
	private Component component;

	/** the associated menu item */
	private TabMenuItem tabMenuItem;

	/** Creates a new instance of CloseTabContentPanel */
	public CloseTabContentPanel(int tabPlacement, Component component) {
		super(new BorderLayout());

		this.component = component;

		if (activeColor == null) {
			activeColor = UIManager.getColor("InternalFrame.activeTitleBackground");
		}

		if (border == null) {
			switch (tabPlacement) {
				case SwingConstants.TOP:
					border = BorderFactory.createMatteBorder(BORDER_WIDTH, 0, 0, 0, activeColor);
					break;
				case SwingConstants.BOTTOM:
					border = BorderFactory.createMatteBorder(0, 0, BORDER_WIDTH, 0, activeColor);
					break;
			}
		}
		add(component, BorderLayout.CENTER);
		setBorder(border);
	}

	public Component getDisplayComponent() {
		return component;
	}

	public TabMenuItem getTabMenuItem() {
		return tabMenuItem;
	}

	public void setTabMenuItem(TabMenuItem tabMenuItem) {
		this.tabMenuItem = tabMenuItem;
	}
}
