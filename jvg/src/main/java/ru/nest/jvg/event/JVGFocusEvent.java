package ru.nest.jvg.event;

import ru.nest.jvg.JVGComponent;

public class JVGFocusEvent extends JVGEvent {
	public static final int FOCUS_LOST = 20;

	public static final int FOCUS_GAINED = 21;

	public JVGFocusEvent(JVGComponent source, int id) {
		super(source, id);
	}

	@Override
	public String paramString() {
		String typeStr;
		switch (id) {
			case FOCUS_LOST:
				typeStr = "FOCUS_LOST";
				break;

			case FOCUS_GAINED:
				typeStr = "FOCUS_GAINED";
				break;

			default:
				typeStr = "unknown type";
		}
		return typeStr;
	}
}
