package ru.nest.jvg.event;

public interface JVGContainerListener extends JVGEventListener {
	void componentAdded(JVGContainerEvent e);

	void componentRemoved(JVGContainerEvent e);

	void componentOrderChanged(JVGContainerEvent e);
}
