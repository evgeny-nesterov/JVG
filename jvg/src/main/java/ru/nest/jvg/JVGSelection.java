package ru.nest.jvg;

import java.awt.Shape;

import ru.nest.jvg.event.JVGMouseEvent;

public interface JVGSelection<V extends Shape> extends Repainter {
	public V getShape();

	public void mousePressed(JVGMouseEvent e);

	public void mouseReleases(JVGMouseEvent e);

	public void mouseDragged(JVGMouseEvent e);
}
