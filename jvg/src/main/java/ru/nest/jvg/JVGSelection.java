package ru.nest.jvg;

import java.awt.Shape;

import ru.nest.jvg.event.JVGMouseEvent;

public interface JVGSelection<V extends Shape> extends Repainter {
	V getShape();

	void mousePressed(JVGMouseEvent e);

	void mouseReleases(JVGMouseEvent e);

	void mouseDragged(JVGMouseEvent e);
}
