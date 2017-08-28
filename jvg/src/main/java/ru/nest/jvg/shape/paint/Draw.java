package ru.nest.jvg.shape.paint;

import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.shape.JVGShape;

public interface Draw<V> extends Cloneable {
	public Paint getPaint(JVGShape component, Shape shape, AffineTransform transform);

	public void setResource(Resource<V> resource);

	public double getOpacity();

	public Resource<V> getResource();

	public Object clone();
}
