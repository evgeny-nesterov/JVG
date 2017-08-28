package ru.nest.jvg.event;

public interface JVGMouseListener extends JVGEventListener {
	public void mouseClicked(JVGMouseEvent e);

	public void mousePressed(JVGMouseEvent e);

	public void mouseReleased(JVGMouseEvent e);

	public void mouseEntered(JVGMouseEvent e);

	public void mouseExited(JVGMouseEvent e);

	public void mouseDragged(JVGMouseEvent e);

	public void mouseMoved(JVGMouseEvent e);
}
