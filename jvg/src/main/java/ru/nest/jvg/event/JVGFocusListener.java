package ru.nest.jvg.event;

public interface JVGFocusListener extends JVGEventListener {
	void focusLost(JVGFocusEvent e);

	void focusGained(JVGFocusEvent e);
}
