package javax.swing.dock;

import java.awt.Component;
import java.awt.Container;

import javax.swing.Icon;

public class DockFlow {
	private Component draggedComponent;

	private Component dropComponent;

	private Component undockComponent;

	private String name;

	private Icon icon;

	public DockFlow(Component draggedComponent, Component dropComponent, Component undockComponent, String name) {
		this(draggedComponent, dropComponent, undockComponent, name, null);
	}

	public DockFlow(Component draggedComponent, Component dropComponent, Component undockComponent, String name, Icon icon) {
		this.draggedComponent = draggedComponent;
		this.dropComponent = dropComponent;
		this.undockComponent = undockComponent;
		this.name = name;
		this.icon = icon;
	}

	public void removeUndockComponent() {
		Container parent = undockComponent.getParent();
		parent.remove(undockComponent);
		parent.invalidate();
		parent.validate();
	}

	public void setDraggedComponent(Component draggedComponent) {
		this.draggedComponent = draggedComponent;
	}

	public Component getDraggedComponent() {
		return draggedComponent;
	}

	public void setDropComponent(Component dropComponent) {
		this.dropComponent = dropComponent;
	}

	public Component getDropComponent() {
		return dropComponent;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setIcon(Icon icon) {
		this.icon = icon;
	}

	public Icon getIcon() {
		return icon;
	}

	public Component getUndockComponent() {
		return undockComponent;
	}

	public void setUndockComponent(Component undockComponent) {
		this.undockComponent = undockComponent;
	}
}
