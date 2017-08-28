package javax.swing.dock.tab;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.dock.AbstractContentPanelHeader;
import javax.swing.dock.DockFlowSupport;

public class TabDockHeader extends AbstractContentPanelHeader {
	private static final long serialVersionUID = 1L;
	private TabDockContainer tab;

	public TabDockHeader(final TabDockContainer tab, String title, Icon icon, final Component flow) {
		super(title, icon);

		this.tab = tab;

		setFlowSupport(new DockFlowSupport(this, flow, flow, title, icon));
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				int index = tab.indexOfTabComponent(TabDockHeader.this);
				if (index != -1) {
					tab.setSelectedIndex(index);
				}
			}
		});
		addCloseAction();
	}

	@Override
	public void close() {
		int index = tab.indexOfTabComponent(TabDockHeader.this);
		if (index != -1) {
			tab.remove(index);
		}
	}

	@Override
	protected boolean isDrawIcons() {
		if (super.isDrawIcons()) {
			return true;
		} else {
			return tab.getSelectedIndex() != -1 && tab.getTabComponentAt(tab.getSelectedIndex()) == this;
		}
	}
}
