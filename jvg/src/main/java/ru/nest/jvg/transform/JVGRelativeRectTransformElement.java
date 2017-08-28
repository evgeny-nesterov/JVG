package ru.nest.jvg.transform;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import ru.nest.jvg.shape.JVGShape;

public abstract class JVGRelativeRectTransformElement implements JVGTransformElement {
	public final static int TYPE_SHAPE = 0;

	public final static int TYPE_ORIGINAL_SHAPE = 1;

	public final static int TYPE_SELECTION = 2;

	public final static int TYPE_PANE = 3;

	public int rectType;

	public JVGRelativeRectTransformElement(int rectType) {
		this.rectType = rectType;
	}

	public abstract void transform(JVGShape shape, AffineTransform transform, Rectangle2D r);

	@Override
	public void transform(JVGShape shape, AffineTransform transform) {
		Rectangle2D r = computeRect(shape);
		transform(shape, transform, r);
	}

	public Rectangle2D computeRect(JVGShape shape) {
		switch (rectType) {
			case TYPE_SHAPE:
				Shape ob = shape.getOriginalBounds();
				return shape.getRectangleBounds();
			case TYPE_ORIGINAL_SHAPE:
				ob = shape.getOriginalBounds();
				return ob instanceof Rectangle2D ? (Rectangle2D) ob : ob.getBounds2D();
			case TYPE_SELECTION:
				return shape.getPane().getSelectionManager().getSelectionBounds();
			case TYPE_PANE:
				return shape.getPane().getBounds();
		}
		return null;
	}
}
