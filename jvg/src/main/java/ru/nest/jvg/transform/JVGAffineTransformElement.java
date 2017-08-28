package ru.nest.jvg.transform;

import java.awt.geom.AffineTransform;

import ru.nest.jvg.shape.JVGShape;

public class JVGAffineTransformElement implements JVGTransformElement {
	public AffineTransform transform;

	public JVGAffineTransformElement(AffineTransform transform) {
		this.transform = transform;
	}

	@Override
	public void transform(JVGShape shape, AffineTransform transform) {
		transform.preConcatenate(this.transform);
	}

	public void transform(AffineTransform transform) {
		this.transform.preConcatenate(transform);
	}
}
