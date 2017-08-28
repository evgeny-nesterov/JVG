package ru.nest.jvg.event;

import ru.nest.jvg.JVGComponent;

public class JVGEvent {
	public JVGEvent(JVGComponent source, int id) {
		this.source = source;
		this.id = id;
	}

	protected int id;

	public int getID() {
		return id;
	}

	protected JVGComponent source;

	public JVGComponent getSource() {
		return source;
	}

	public String paramString() {
		return getClass().toString();
	}

	public void consume() {
		switch (id) {
			case JVGKeyEvent.KEY_PRESSED:
			case JVGKeyEvent.KEY_RELEASED:

			case JVGMouseEvent.MOUSE_PRESSED:
			case JVGMouseEvent.MOUSE_RELEASED:
			case JVGMouseEvent.MOUSE_MOVED:
			case JVGMouseEvent.MOUSE_DRAGGED:
			case JVGMouseEvent.MOUSE_ENTERED:
			case JVGMouseEvent.MOUSE_EXITED:
			case JVGMouseEvent.MOUSE_WHEEL:
				consumed = true;
				break;

			default:
		}
	}

	protected boolean consumed = false;

	public boolean isConsumed() {
		return consumed;
	}
}
