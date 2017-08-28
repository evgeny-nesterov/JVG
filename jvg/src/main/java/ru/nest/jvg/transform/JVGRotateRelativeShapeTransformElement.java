package ru.nest.jvg.transform;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import ru.nest.jvg.shape.JVGShape;

public class JVGRotateRelativeShapeTransformElement extends JVGRelativeRectTransformElement {
	public double x;

	public double y;

	public double angle;

	public JVGRotateRelativeShapeTransformElement(int rectType, double angle, double x, double y) {
		super(rectType);
		this.x = x;
		this.y = y;
		this.angle = angle;
	}

	@Override
	public void transform(JVGShape shape, AffineTransform transform, Rectangle2D r) {
		double anchorx = r.getX() + x * r.getWidth();
		double anchory = r.getY() + y * r.getHeight();
		transform.rotate(angle, anchorx, anchory);
	}
}
