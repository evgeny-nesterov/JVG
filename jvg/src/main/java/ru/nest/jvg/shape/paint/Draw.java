package ru.nest.jvg.shape.paint;

import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.shape.JVGShape;

public interface Draw<V> extends Cloneable {
	Paint getPaint(JVGShape component, Shape shape, AffineTransform transform);

	void setResource(Resource<V> resource);

	double getOpacity();

	Resource<V> getResource();

	Object clone();
}
