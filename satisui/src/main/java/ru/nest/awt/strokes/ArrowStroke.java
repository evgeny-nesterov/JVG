package ru.nest.awt.strokes;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;

public class ArrowStroke implements Stroke {
	public static final int curvesize[] = { 2, 2, 4, 6, 0 };

	public final static int DIRECTION_NONE = 0;

	public final static int DIRECTION_DIRECT = 1;

	public final static int DIRECTION_BACK = 2;

	public final static int DIRECTION_BOTH = 3;

	private double width = 14;

	private double arrowWidth = 12;

	private double arrowLength = 6;

	private int type;

	private Stroke stroke;

	public ArrowStroke(double width, double arrowWidth, double arrowLength, int type) {
		this.width = width;
		this.arrowWidth = arrowWidth;
		this.arrowLength = arrowLength;
		this.type = type;
		stroke = new BasicStroke((float) width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
	}

	@Override
	public Shape createStrokedShape(Shape shape) {
		Shape body = stroke.createStrokedShape(shape);
		Area a = new Area(body);
		if (type == DIRECTION_NONE) {
			return a;
		}

		double points[] = new double[6];
		PathIterator it = shape.getPathIterator(null);
		double x1 = 0, y1 = 0, x2 = 0, y2 = 0, sx1 = 0, sy1 = 0, sx2 = 0, sy2 = 0;
		boolean drawDirectArrow = false;
		int drawBackArrow = 0;
		while (!it.isDone()) {
			int curvetype = it.currentSegment(points);
			switch (curvetype) {
				case PathIterator.SEG_MOVETO:
					if (drawDirectArrow) {
						double dx = x2 - x1;
						double dy = y2 - y1;
						double angle = Math.atan2(dx, dy);

						AffineTransform transform = AffineTransform.getTranslateInstance(x2, y2);
						transform.rotate(-angle);

						Path2D arrowShape = new Path2D.Double();
						arrowShape.moveTo(-getArrowWidth(), -1);
						arrowShape.lineTo(0, getArrowLength());
						arrowShape.lineTo(getArrowWidth(), -1);
						a.add(new Area(transform.createTransformedShape(arrowShape)));
						drawDirectArrow = false;
					}

					sx1 = x2 = points[0];
					sy1 = y2 = points[1];
					if (type == DIRECTION_BACK || type == DIRECTION_BOTH) {
						drawBackArrow = 1;
					}
					break;
				case PathIterator.SEG_LINETO:
					x1 = x2;
					y1 = y2;
					x2 = points[0];
					y2 = points[1];
					if (type == DIRECTION_DIRECT || type == DIRECTION_BOTH) {
						drawDirectArrow = true;
					}
					if (drawBackArrow == 1) {
						sx2 = points[0];
						sy2 = points[1];
						drawBackArrow = 2;
					}
					break;
				case PathIterator.SEG_QUADTO:
					x1 = points[0];
					y1 = points[1];
					x2 = points[2];
					y2 = points[3];
					if (type == DIRECTION_DIRECT || type == DIRECTION_BOTH) {
						drawDirectArrow = true;
					}
					if (drawBackArrow == 1) {
						sx2 = points[0];
						sy2 = points[1];
						drawBackArrow = 2;
					}
					break;
				case PathIterator.SEG_CUBICTO:
					x1 = points[2];
					y1 = points[3];
					x2 = points[4];
					y2 = points[5];
					if (type == DIRECTION_DIRECT || type == DIRECTION_BOTH) {
						drawDirectArrow = true;
					}
					if (drawBackArrow == 1) {
						sx2 = points[0];
						sy2 = points[1];
						drawBackArrow = 2;
					}
					break;
			}

			if (drawBackArrow == 2) {
				double dx = sx1 - sx2;
				double dy = sy1 - sy2;
				double angle = Math.atan2(dx, dy);

				AffineTransform transform = AffineTransform.getTranslateInstance(sx1, sy1);
				transform.rotate(-angle);

				Path2D arrowShape = new Path2D.Double();
				arrowShape.moveTo(-getArrowWidth(), -1);
				arrowShape.lineTo(0, getArrowLength());
				arrowShape.lineTo(getArrowWidth(), -1);
				a.add(new Area(transform.createTransformedShape(arrowShape)));

				drawBackArrow = 0;
			}
			it.next();
		}

		if (drawDirectArrow) {
			double dx = x2 - x1;
			double dy = y2 - y1;
			double angle = Math.atan2(dx, dy);

			AffineTransform transform = AffineTransform.getTranslateInstance(x2, y2);
			transform.rotate(-angle);

			Path2D arrowShape = new Path2D.Double();
			arrowShape.moveTo(-getArrowWidth(), -1);
			arrowShape.lineTo(0, getArrowLength());
			arrowShape.lineTo(getArrowWidth(), -1);
			a.add(new Area(transform.createTransformedShape(arrowShape)));
			drawDirectArrow = false;
		}
		return a;
	}

	public double getWidth() {
		return width;
	}

	public double getArrowWidth() {
		return arrowWidth;
	}

	public double getArrowLength() {
		return arrowLength;
	}

	public int getType() {
		return type;
	}
}
