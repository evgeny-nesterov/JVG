package ru.nest.jvg.event;

import ru.nest.jvg.JVGComponent;

public class JVGComponentEvent extends JVGEvent {
	public static final int COMPONENT_TRANSFORMED = 0;

	public static final int COMPONENT_GEOMETRY_CHANGED = 1;

	public static final int COMPONENT_SHOWN = 2;

	public static final int COMPONENT_HIDDEN = 3;

	public JVGComponentEvent(JVGComponent source, int id) {
		super(source, id);
	}

	@Override
	public String paramString() {
		String typeStr;
		switch (id) {
			case COMPONENT_SHOWN:
				typeStr = "COMPONENT_SHOWN";
				break;

			case COMPONENT_GEOMETRY_CHANGED:
				typeStr = "COMPONENT_GEOMETRY_CHANGED";
				break;

			case COMPONENT_HIDDEN:
				typeStr = "COMPONENT_HIDDEN";
				break;

			case COMPONENT_TRANSFORMED:
				typeStr = "COMPONENT_TRANSFORMED";
				break;

			default:
				typeStr = "unknown type";
		}
		return typeStr;
	}
}
