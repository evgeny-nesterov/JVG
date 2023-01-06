package ru.nest.jvg.event;

public interface JVGKeyListener extends JVGEventListener {
	void keyTyped(JVGKeyEvent e);

	void keyPressed(JVGKeyEvent e);

	void keyReleased(JVGKeyEvent e);
}
