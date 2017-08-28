package ru.nest.jvg.event;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import ru.nest.jvg.JVGComponent;

public class JVGKeyEvent extends JVGInputEvent {
	public static final int KEY_TYPED = KeyEvent.KEY_TYPED;

	public static final int KEY_PRESSED = KeyEvent.KEY_PRESSED;

	public static final int KEY_RELEASED = KeyEvent.KEY_RELEASED;

	public JVGKeyEvent(InputEvent originEvent, JVGComponent source, int id, long when, int modifiers, int keyCode, char keyChar) {
		super(originEvent, source, id, when, modifiers);
		this.keyCode = keyCode;
		this.keyChar = keyChar;
	}

	public void setModifiers(int modifiers) {
		this.modifiers = modifiers;
	}

	int keyCode;

	public int getKeyCode() {
		return keyCode;
	}

	char keyChar;

	public char getKeyChar() {
		return keyChar;
	}

	public static String getKeyText(int keyCode) {
		return KeyEvent.getKeyText(keyCode);
	}

	public static String getKeyModifiersText(int modifiers) {
		return KeyEvent.getKeyModifiersText(modifiers);
	}

	@Override
	public String paramString() {
		StringBuffer str = new StringBuffer(100);

		switch (id) {
			case KEY_PRESSED:
				str.append("KEY_PRESSED");
				break;
			case KEY_RELEASED:
				str.append("KEY_RELEASED");
				break;
			case KEY_TYPED:
				str.append("KEY_TYPED");
				break;
			default:
				str.append("unknown type");
				break;
		}

		str.append(",keyCode=").append(keyCode);
		str.append(",keyText=").append(getKeyText(keyCode));

		return str.toString();
	}
}
