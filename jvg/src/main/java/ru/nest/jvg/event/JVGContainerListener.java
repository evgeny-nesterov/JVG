package ru.nest.jvg.event;

public interface JVGContainerListener extends JVGEventListener {
	public void componentAdded(JVGContainerEvent e);

	public void componentRemoved(JVGContainerEvent e);

	public void componentOrderChanged(JVGContainerEvent e);
}
