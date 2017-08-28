package ru.nest.jvg.event;

public interface JVGFocusListener extends JVGEventListener {
	public void focusLost(JVGFocusEvent e);

	public void focusGained(JVGFocusEvent e);
}
