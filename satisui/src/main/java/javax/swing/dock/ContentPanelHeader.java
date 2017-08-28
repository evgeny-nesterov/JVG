package javax.swing.dock;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JLabel3D;
import javax.swing.dock.ExpandSupport.ExpandListener;

public class ContentPanelHeader extends AbstractContentPanelHeader {
	private static final long serialVersionUID = 1L;

	private Component dropComponent;

	private Component undockComponent;

	private HeaderOrientation orientationMinimized = null;

	public ContentPanelHeader(final Dockable<?> dockable, String title, Icon icon, final Component dropComponent, final Component undockComponent) {
		super(title, icon);

		this.dropComponent = dropComponent;
		this.undockComponent = undockComponent;

		setExpandSupport(new ExpandSupport(undockComponent, dropComponent));
		setFlowSupport(new DockFlowSupport(this, dropComponent, undockComponent, title, icon));
		setHeight(20);
		setRenderer(new ContentPanelHeaderRenderer() {
			private JLabel3D renderLabel = new JLabel3D(new Color(220, 220, 220), 1.1);

			@Override
			public Component getContentPanelHeaderRendererComponent(AbstractContentPanelHeader header, String title, Icon icon) {
				renderLabel.setText(title);
				renderLabel.setIcon(icon);
				return renderLabel;
			}
		});

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					if (e.getClickCount() == 1) {
						if (dockable.getSelectionModel() != null) {
							dockable.getSelectionModel().setSelected(undockComponent);
						}
					}
				}
			}
		});

		getExpandSupport().addListener(new ExpandListener() {
			HeaderOrientation o;

			@Override
			public void expandStateChanged(boolean isExpanded, boolean isMinimized) {
				if (orientationMinimized != null) {
					if (isMinimized) {
						if (orientationMinimized != getOrientation()) {
							o = getOrientation();
							setOrientation(orientationMinimized);
							getParent().invalidate();
							getParent().validate();
							getParent().repaint();
						}
					} else {
						if (o != null && o != getOrientation()) {
							setOrientation(o);
							o = null;
							getParent().invalidate();
							getParent().validate();
							getParent().repaint();
						}
					}
				}
			}
		});
	}

	@Override
	public void close() {
		if (getExpandSupport().isExpanded()) {
			getExpandSupport().expandRestore();
		}

		Container parent = undockComponent.getParent();
		parent.remove(undockComponent);
		parent.invalidate();
		parent.validate();
		parent.repaint();
	}

	public HeaderOrientation getOrientationMinimized() {
		return orientationMinimized;
	}

	public void setOrientationMinimized(HeaderOrientation orientationMinimized) {
		this.orientationMinimized = orientationMinimized;
	}
}
