package ru.nest.jvg.event;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGContainer;

public class JVGContainerEvent extends JVGComponentEvent {
	public static final int COMPONENT_ADDED = 10;

	public static final int COMPONENT_REMOVED = 11;

	public static final int COMPONENT_ORDER_CHANGED = 12;

	public JVGContainerEvent(JVGComponent source, int id, JVGComponent child) {
		super(source, id);
		this.child = child;
	}

	public JVGContainer getContainer() {
		return (source instanceof JVGContainer) ? (JVGContainer) source : null;
	}

	JVGComponent child;

	public JVGComponent getChild() {
		return child;
	}

	@Override
	public String paramString() {
		String typeStr;
		switch (id) {
			case COMPONENT_ADDED:
				typeStr = "COMPONENT_ADDED";
				break;

			case COMPONENT_REMOVED:
				typeStr = "COMPONENT_REMOVED";
				break;

			case COMPONENT_ORDER_CHANGED:
				typeStr = "COMPONENT_ORDER_CHANGED";
				break;

			default:
				typeStr = "unknown type";
		}
		return typeStr + ",child=" + child.getName();
	}
}
