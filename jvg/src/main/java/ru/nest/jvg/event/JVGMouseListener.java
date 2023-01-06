package ru.nest.jvg.event;

public interface JVGMouseListener extends JVGEventListener {
	void mouseClicked(JVGMouseEvent e);

	void mousePressed(JVGMouseEvent e);

	void mouseReleased(JVGMouseEvent e);

	void mouseEntered(JVGMouseEvent e);

	void mouseExited(JVGMouseEvent e);

	void mouseDragged(JVGMouseEvent e);

	void mouseMoved(JVGMouseEvent e);
}
