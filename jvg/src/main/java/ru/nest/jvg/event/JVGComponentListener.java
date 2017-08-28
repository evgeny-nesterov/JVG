package ru.nest.jvg.event;

public interface JVGComponentListener extends JVGEventListener {
	public void componentTransformed(JVGComponentEvent e);

	public void componentGeometryChanged(JVGComponentEvent e);

	public void componentShown(JVGComponentEvent e);

	public void componentHidden(JVGComponentEvent e);
}
