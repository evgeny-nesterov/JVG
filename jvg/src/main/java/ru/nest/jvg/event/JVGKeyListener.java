package ru.nest.jvg.event;

public interface JVGKeyListener extends JVGEventListener {
	public void keyTyped(JVGKeyEvent e);

	public void keyPressed(JVGKeyEvent e);

	public void keyReleased(JVGKeyEvent e);
}
