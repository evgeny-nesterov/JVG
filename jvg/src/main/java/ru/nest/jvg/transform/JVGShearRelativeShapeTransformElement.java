package ru.nest.jvg.transform;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import ru.nest.jvg.shape.JVGShape;

public class JVGShearRelativeShapeTransformElement extends JVGRelativeRectTransformElement {
	public double x;

	public double y;

	public double sx;

	public double sy;

	public boolean sxpercent;

	public boolean sypercent;

	public JVGShearRelativeShapeTransformElement(int rectType, double sx, double sy, boolean sxpercent, boolean sypercent, double x, double y) {
		super(rectType);
		this.x = x;
		this.y = y;
		this.sx = sx;
		this.sy = sy;
		this.sxpercent = sxpercent;
		this.sypercent = sypercent;
	}

	@Override
	public void transform(JVGShape shape, AffineTransform transform, Rectangle2D r) {
		double anchorx = r.getX() + x * r.getWidth();
		double anchory = r.getY() + y * r.getHeight();

		double sx = this.sx;
		double sy = this.sy;
		if (sxpercent) {
			sx *= r.getWidth();
		}
		if (sypercent) {
			sy *= r.getHeight();
		}

		transform.translate(anchorx, anchory);
		transform.shear(sx, sy);
		transform.translate(-anchorx, -anchory);
	}
}
