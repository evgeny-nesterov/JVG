package ru.nest.jvg.actionarea;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.shape.JVGShape;

public class JVGVectorActionArea extends JVGTransformActionArea implements Constants {
	public JVGVectorActionArea(int type) {
		super(false);
		this.type = type;
		setName("vector-" + getDescr());
	}

	private int type;

	public int getType() {
		return type;
	}

	private final static double MIN_RADIUS = 1E-15;

	private AffineTransform t = new AffineTransform();

	@Override
	public AffineTransform getTransform(double x, double y, double dx, double dy) {
		JVGComponent p = getParent();
		if (p instanceof JVGShape) {
			JVGShape parent = (JVGShape) p;
			Rectangle2D bounds = getParentInitialBounds();
			double newX = x + dx;
			double newY = y + dy;

			double icx0 = x, icy0 = y;
			if (type == TOP) {
				icx0 = bounds.getX() + bounds.getWidth() / 2.0;
				icy0 = bounds.getY() + bounds.getHeight();
			} else if (type == BOTTOM) {
				icx0 = bounds.getX() + bounds.getWidth() / 2.0;
				icy0 = bounds.getY();
			} else if (type == LEFT) {
				icx0 = bounds.getX() + bounds.getWidth();
				icy0 = bounds.getY() + bounds.getHeight() / 2.0;
			} else if (type == RIGHT) {
				icx0 = bounds.getX();
				icy0 = bounds.getY() + bounds.getHeight() / 2.0;
			}

			double[] point = new double[] { icx0, icy0 };
			parent.getTransform().transform(point, 0, point, 0, 1);
			double cx0 = point[0];
			double cy0 = point[1];

			t.setToIdentity();

			double dx0 = x - cx0;
			double dy0 = y - cy0;
			double dx1 = newX - cx0;
			double dy1 = newY - cy0;
			double radius = Math.sqrt(dx0 * dx0 + dy0 * dy0);
			double newRadius = Math.sqrt(dx1 * dx1 + dy1 * dy1);
			if (radius > MIN_RADIUS && newRadius > MIN_RADIUS) {
				t.translate(cx0, cy0);

				double cos = (dx1 * dx0 + dy1 * dy0) / (radius * newRadius);
				double alfa = Math.acos(cos);
				boolean clockwise = dx1 * dy0 < dx0 * dy1;
				if (!clockwise) {
					alfa *= -1.0;
				}

				double dx2 = -dy0 / radius;
				double dy2 = dx0 / radius;
				double dx3 = -dy1 / newRadius;
				double dy3 = dx1 / newRadius;

				double D = dx0 * dy2 - dx2 * dy0;
				if (D != 0) {
					double m00 = (dx1 * dy2 - dx3 * dy0) / D;
					double m01 = (dx0 * dx3 - dx2 * dx1) / D;
					double m10 = (dy1 * dy2 - dy3 * dy0) / D;
					double m11 = (dx0 * dy3 - dx2 * dy1) / D;
					double[] matrix = { m00, m10, m01, m11, 0, 0 };
					AffineTransform transform = new AffineTransform(matrix);
					t.concatenate(transform);
				}

				t.translate(-cx0, -cy0);
			}

			return t;
		}

		t.setToIdentity();
		return t;
	}

	@Override
	protected Shape computeActionBounds() {
		JVGComponent p = getParent();
		if (p instanceof JVGShape) {
			JVGShape parent = (JVGShape) p;
			Rectangle2D bounds = getParentInitialBounds();
			if (bounds == null) {
				return null;
			}

			double x;
			double y;
			switch (type) {
				case TOP:
					x = bounds.getX() + bounds.getWidth() / 2.0;
					y = bounds.getY();
					break;

				case LEFT:
					x = bounds.getX();
					y = bounds.getY() + bounds.getHeight() / 2.0;
					break;

				case BOTTOM:
					x = bounds.getX() + bounds.getWidth() / 2.0;
					y = bounds.getY() + bounds.getHeight();
					break;

				case RIGHT:
					x = bounds.getX() + bounds.getWidth();
					y = bounds.getY() + bounds.getHeight() / 2.0;
					break;

				default:
					x = bounds.getX() + bounds.getWidth() / 2.0;
					y = bounds.getY() + bounds.getHeight() / 2.0;
					break;
			}

			double[] point = new double[] { x, y };
			AffineTransform transform = parent.getTransform();
			transform.transform(point, 0, point, 0, 1);
			x = (int) point[0];
			y = (int) point[1];
			return new Rectangle2D.Double(x - 2.5, y - 2.5, 5, 5);
		} else {
			return null;
		}
	}

	@Override
	protected Color getColor() {
		return Color.yellow;
	}

	@Override
	protected Color getBorderColor() {
		return Color.black;
	}

	@Override
	public Cursor getCursor() {
		return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
	}

	public String getDescr() {
		return ActionUtil.getDescr(type);
	}
}
