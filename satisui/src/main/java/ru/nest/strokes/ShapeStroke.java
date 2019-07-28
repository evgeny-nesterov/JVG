package ru.nest.strokes;

import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;

public class ShapeStroke implements Stroke {
	private Shape shapes[];

	private float advance;

	private boolean stretchToFit = false;

	private boolean repeat = true;

	private AffineTransform t = new AffineTransform();

	private static final float FLATNESS = 1;

	public ShapeStroke(Shape shapes, float advance) {
		this(new Shape[] { shapes }, advance);
	}

	public ShapeStroke(Shape shapes[], float advance) {
		this.advance = advance;
		this.shapes = new Shape[shapes.length];

		for (int i = 0; i < this.shapes.length; i++) {
			Rectangle2D bounds = shapes[i].getBounds2D();
			t.setToTranslation(-bounds.getCenterX(), -bounds.getCenterY());
			this.shapes[i] = t.createTransformedShape(shapes[i]);
		}
	}

	@Override
	public Shape createStrokedShape(Shape shape) {
		GeneralPath result = new GeneralPath();
		PathIterator it = new FlatteningPathIterator(shape.getPathIterator(null), FLATNESS);
		double points[] = new double[6];
		double moveX = 0, moveY = 0;
		double lastX = 0, lastY = 0;
		double thisX = 0, thisY = 0;
		int type = 0;
		boolean first = false;
		double next = 0;
		int currentShape = 0;
		int length = shapes.length;

		double factor = 1;

		while (currentShape < length && !it.isDone()) {
			type = it.currentSegment(points);
			switch (type) {
				case PathIterator.SEG_MOVETO:
					moveX = lastX = points[0];
					moveY = lastY = points[1];
					result.moveTo(moveX, moveY);
					first = true;
					next = 0;
					break;

				case PathIterator.SEG_CLOSE:
					points[0] = moveX;
					points[1] = moveY;
					// Fall into....

				case PathIterator.SEG_LINETO:
					thisX = points[0];
					thisY = points[1];
					double dx = thisX - lastX;
					double dy = thisY - lastY;
					double distance = Math.sqrt(dx * dx + dy * dy);
					if (distance >= next) {
						double r = 1.0f / distance;
						double angle = Math.atan2(dy, dx);
						while (currentShape < length && distance >= next) {
							double x = lastX + next * dx * r;
							double y = lastY + next * dy * r;
							t.setToTranslation(x, y);
							t.rotate(angle);
							result.append(t.createTransformedShape(shapes[currentShape]), false);
							next += advance;
							currentShape++;
							if (repeat)
								currentShape %= length;
						}
					}
					next -= distance;
					first = false;
					lastX = thisX;
					lastY = thisY;
					break;
			}
			it.next();
		}
		return result;
	}
}