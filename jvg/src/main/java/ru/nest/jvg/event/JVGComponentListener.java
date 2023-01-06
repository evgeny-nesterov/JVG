package ru.nest.jvg.event;

public interface JVGComponentListener extends JVGEventListener {
	void componentTransformed(JVGComponentEvent e);

	void componentGeometryChanged(JVGComponentEvent e);

	void componentShown(JVGComponentEvent e);

	void componentHidden(JVGComponentEvent e);
}
