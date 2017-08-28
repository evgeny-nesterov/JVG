package javax.swing.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

public class CopyLayout implements LayoutManager {
	private Component src;

	public CopyLayout(Component src) {
		setSource(src);
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {
	}

	public void setSource(Component src) {
		this.src = src;
	}

	@Override
	public void removeLayoutComponent(Component comp) {
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		return src.getPreferredSize();
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return src.getMinimumSize();
	}

	@Override
	public void layoutContainer(Container parent) {
		if (src != null && parent.getComponentCount() > 0) {
			parent.getComponent(0).setBounds(src.getBounds());
		}
	}
}
