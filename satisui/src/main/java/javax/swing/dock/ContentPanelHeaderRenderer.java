package javax.swing.dock;

import java.awt.Component;

import javax.swing.Icon;

public interface ContentPanelHeaderRenderer {
	public Component getContentPanelHeaderRendererComponent(AbstractContentPanelHeader header, String title, Icon icon);
}
