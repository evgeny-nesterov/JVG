package ru.nest.jvg.transform;

import java.awt.geom.AffineTransform;

import ru.nest.jvg.shape.JVGShape;

public interface JVGTransformElement {
	public void transform(JVGShape shape, AffineTransform transform);
}
