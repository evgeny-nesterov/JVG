package ru.nest.jvg.event;

import java.awt.GraphicsEnvironment;
import java.awt.event.InputEvent;

import ru.nest.jvg.JVGComponent;

public abstract class JVGInputEvent extends JVGEvent {
	public static final int SHIFT_MASK = InputEvent.SHIFT_MASK;

	public static final int CTRL_MASK = InputEvent.CTRL_MASK;

	public static final int META_MASK = InputEvent.META_MASK;

	public static final int ALT_MASK = InputEvent.ALT_MASK;

	public static final int ALT_GRAPH_MASK = InputEvent.ALT_GRAPH_MASK;

	public static final int BUTTON1_MASK = InputEvent.BUTTON1_MASK;

	public static final int BUTTON2_MASK = InputEvent.BUTTON2_MASK;

	public static final int BUTTON3_MASK = InputEvent.BUTTON3_MASK;

	public static final int SHIFT_DOWN_MASK = InputEvent.SHIFT_DOWN_MASK;

	public static final int CTRL_DOWN_MASK = InputEvent.CTRL_DOWN_MASK;

	public static final int META_DOWN_MASK = InputEvent.META_DOWN_MASK;

	public static final int ALT_DOWN_MASK = InputEvent.ALT_DOWN_MASK;

	public static final int BUTTON1_DOWN_MASK = InputEvent.BUTTON1_DOWN_MASK;

	public static final int BUTTON2_DOWN_MASK = InputEvent.BUTTON2_DOWN_MASK;

	public static final int BUTTON3_DOWN_MASK = InputEvent.BUTTON3_DOWN_MASK;

	public static final int ALT_GRAPH_DOWN_MASK = InputEvent.ALT_GRAPH_DOWN_MASK;

	static final int JDK_1_3_MODIFIERS = SHIFT_DOWN_MASK - 1;

	public JVGInputEvent(InputEvent originEvent, JVGComponent source, int id, long when, int modifiers) {
		super(source, id);
		this.originEvent = originEvent;
		this.when = when;
		this.modifiers = modifiers;
		canAccessSystemClipboard = canAccessSystemClipboard();
	}

	InputEvent originEvent;

	public InputEvent getOriginEvent() {
		return originEvent;
	}

	@Override
	public void consume() {
		consumed = true;
	}

	@Override
	public boolean isConsumed() {
		return consumed;
	}

	long when;

	public long getWhen() {
		return when;
	}

	int modifiers;

	public int getModifiers() {
		return modifiers & JDK_1_3_MODIFIERS;
	}

	public int getModifiersEx() {
		return modifiers & ~JDK_1_3_MODIFIERS;
	}

	public boolean isShiftDown() {
		return (modifiers & SHIFT_MASK) != 0;
	}

	public boolean isControlDown() {
		return (modifiers & CTRL_MASK) != 0;
	}

	public boolean isMetaDown() {
		return (modifiers & META_MASK) != 0;
	}

	public boolean isAltDown() {
		return (modifiers & ALT_MASK) != 0;
	}

	public boolean isAltGraphDown() {
		return (modifiers & ALT_GRAPH_MASK) != 0;
	}

	public static String getModifiersExText(int modifiers) {
		return InputEvent.getModifiersExText(modifiers);
	}

	private transient boolean canAccessSystemClipboard;

	private boolean canAccessSystemClipboard() {
		boolean b = false;

		if (!GraphicsEnvironment.isHeadless()) {
			SecurityManager sm = System.getSecurityManager();
			if (sm != null) {
				try {
					sm.checkSystemClipboardAccess();
					b = true;
				} catch (SecurityException se) {
				}
			} else {
				b = true;
			}
		}
		return b;
	}
}
