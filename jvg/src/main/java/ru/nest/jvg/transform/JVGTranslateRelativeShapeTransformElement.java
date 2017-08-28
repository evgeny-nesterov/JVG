package ru.nest.jvg.transform;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import ru.nest.jvg.shape.JVGShape;

public class JVGTranslateRelativeShapeTransformElement implements JVGTransformElement {
	public double dx;

	public double dy;

	public JVGTranslateRelativeShapeTransformElement(double dx, double dy) {
		this.dx = dx;
		this.dy = dy;
	}

	@Override
	public void transform(JVGShape shape, AffineTransform transform) {
		Rectangle2D r = shape.getRectangleBounds();
		double dx = this.dx * r.getWidth();
		double dy = this.dy * r.getHeight();
		transform.translate(dx, dy);
	}
}
