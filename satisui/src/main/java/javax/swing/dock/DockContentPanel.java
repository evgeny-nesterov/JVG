package javax.swing.dock;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.dock.AbstractContentPanelHeader.HeaderOrientation;
import javax.swing.dock.tab.TabDockContainer;

public class DockContentPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private Component header;

	private Component content;

	private DockContentPanel(Dockable<?> dock, Component dropComponent, String name, Icon icon) {
		DockFlow flow = new DockFlow(null, dropComponent, this, name, icon);
		Component title = dock.createHeader(flow);
		set(flow.getName(), flow.getIcon(), flow.getDropComponent(), title);
	}

	public Component getHeader() {
		return header;
	}

	public Component getContent() {
		return content;
	}

	private void set(String title, Icon icon, final Component content, final Component header) {
		this.header = header;
		this.content = content;

		setLayout(new LayoutManager() {
			@Override
			public void addLayoutComponent(String name, Component comp) {
			}

			@Override
			public void removeLayoutComponent(Component comp) {
			}

			@Override
			public Dimension preferredLayoutSize(Container parent) {
				Dimension hs = header.getPreferredSize();
				if (header instanceof AbstractContentPanelHeader) {
					AbstractContentPanelHeader ah = (AbstractContentPanelHeader) header;
					if (ah.getOrientation() == HeaderOrientation.VERTICAL) {
						int w = hs.width;
						int h = hs.height;
						if (content.isVisible()) {
							Dimension cs = content.getPreferredSize();
							w += cs.width;
							h = Math.max(h, cs.height);
						}
						return new Dimension(w, h);
					}
				}

				int w = hs.width;
				int h = hs.height;
				if (content.isVisible()) {
					Dimension cs = content.getPreferredSize();
					w = Math.max(w, cs.width);
					h += cs.height;
				}
				return new Dimension(w, h);
			}

			@Override
			public Dimension minimumLayoutSize(Container parent) {
				Dimension hs = header.getPreferredSize();
				if (header instanceof AbstractContentPanelHeader) {
					AbstractContentPanelHeader ah = (AbstractContentPanelHeader) header;
					if (ah.getOrientation() == HeaderOrientation.VERTICAL) {
						int w = hs.width;
						int h = hs.height;
						if (content.isVisible()) {
							Dimension cs = content.getMinimumSize();
							w += cs.width;
							h = Math.max(h, cs.height);
							return new Dimension(w, h);
						}
					}
				}

				int w = hs.width;
				int h = hs.height;
				if (content.isVisible()) {
					Dimension cs = content.getMinimumSize();
					w = Math.max(w, cs.width);
					h += cs.height;
				}
				return new Dimension(w, h);
			}

			@Override
			public void layoutContainer(Container parent) {
				if (!content.isVisible()) {
					header.setBounds(0, 0, parent.getWidth(), parent.getHeight());
					return;
				}

				Dimension hs = header.getPreferredSize();
				if (header instanceof AbstractContentPanelHeader) {
					AbstractContentPanelHeader ah = (AbstractContentPanelHeader) header;
					if (ah.getOrientation() == HeaderOrientation.VERTICAL) {
						header.setBounds(0, 0, hs.width, parent.getHeight());
						content.setBounds(hs.width, 0, parent.getWidth() - hs.width, parent.getHeight());
						return;
					}
				}

				header.setBounds(0, 0, parent.getWidth(), hs.height);
				content.setBounds(0, hs.height, parent.getWidth(), parent.getHeight() - hs.height);
			}
		});
		setMinimumSize(new Dimension(10, 25));
		setBorder(null);
		add(header, BorderLayout.NORTH);
		add(content, BorderLayout.CENTER);
	}

	public void setInsets(int size) {
		setInsets(size, size, size, size);
	}

	public void setInsets(int top, int left, int bottom, int right) {
		setBorder(new EmptyBorder(top, left, bottom, right));
	}

	public static Component create(Dockable<?> dock, Component content, String name, Icon icon) {
		if (content instanceof TabDockContainer) {
			return content;
		} else {
			return new DockContentPanel(dock, content, name, icon);
		}
	}
}
