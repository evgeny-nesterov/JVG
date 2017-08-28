package ru.nest.jvg.actionarea;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.shape.JVGShape;

public class JVGShearActionArea extends JVGTransformActionArea implements Constants {
	public JVGShearActionArea(int type) {
		super(false);
		this.type = type;
		setName("shear-" + getDescr());
	}

	private int type;

	public int getType() {
		return type;
	}

	private AffineTransform t = new AffineTransform();

	@Override
	public AffineTransform getTransform(double x, double y, double dx, double dy) {
		JVGComponent p = getParent();
		if (p instanceof JVGShape) {
			JVGShape parent = (JVGShape) p;
			Rectangle2D bounds;
			if (parent.isOriginalBounds()) {
				bounds = getParentInitialBounds();

				double[] point = new double[] { x, y, x + dx, y + dy };
				parent.getInverseTransform().transform(point, 0, point, 0, 2);
				x = point[0];
				y = point[1];
				dx = point[2] - x;
				dy = point[3] - y;
			} else {
				bounds = parent.getRectangleBounds();
			}

			double translateX = 0;
			double translateY = 0;
			double shearX = 0;
			double shearY = 0;
			switch (type) {
				case TOP:
					shearX = -dx / bounds.getHeight();
					translateY = bounds.getY() + bounds.getHeight();
					break;

				case BOTTOM:
					shearX = dx / bounds.getHeight();
					translateY = bounds.getY();
					break;

				case LEFT:
					shearY = -dy / bounds.getWidth();
					translateX = bounds.getX() + bounds.getWidth();
					break;

				case RIGHT:
					shearY = dy / bounds.getWidth();
					translateX = bounds.getX();
					break;
			}

			t.setToTranslation(translateX, translateY);
			t.shear(shearX, shearY);
			t.translate(-translateX, -translateY);

			if (parent.isOriginalBounds()) {
				t.preConcatenate(parent.getTransform());
				t.concatenate(parent.getInverseTransform());
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
			Rectangle2D bounds;
			if (parent.isOriginalBounds()) {
				bounds = getParentInitialBounds();
			} else {
				bounds = parent.getRectangleBounds();
			}
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

			if (parent.isOriginalBounds()) {
				double[] point = new double[] { x, y };
				AffineTransform transform = parent.getTransform();
				transform.transform(point, 0, point, 0, 1);
				x = (int) point[0];
				y = (int) point[1];
			}
			return new Rectangle2D.Double(x - 2.5, y - 2.5, 5, 5);
		} else {
			return null;
		}
	}

	@Override
	protected Color getColor() {
		return Color.cyan;
	}

	@Override
	protected Color getBorderColor() {
		return Color.black;
	}

	@Override
	public Cursor getCursor() {
		JVGComponent p = getParent();
		if (p instanceof JVGShape) {
			JVGShape parent = (JVGShape) p;
			if (parent.isOriginalBounds()) {
				return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
			}
		}

		switch (type) {
			case TOP:
			case BOTTOM:
				return Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);

			case RIGHT:
			case LEFT:
				return Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
		}

		return Cursor.getDefaultCursor();
	}

	public String getDescr() {
		return ActionUtil.getDescr(type);
	}
}
