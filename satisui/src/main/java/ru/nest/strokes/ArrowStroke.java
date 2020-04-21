package ru.nest.strokes;

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

	private boolean fitToPoint = false;

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
		Area area = new Area(body);
		if (type == DIRECTION_NONE) {
			return area;
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
						if (fitToPoint) {
							area.subtract(getFitToPointArrowNegativeShape(x2, y2, angle));
							area.add(getFitToPointArrowShape(x2, y2, angle));
						} else {
							area.add(getArrowShape(x2, y2, angle));
						}
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
				if (fitToPoint) {
					area.subtract(getFitToPointArrowNegativeShape(sx1, sy1, angle));
					area.add(getFitToPointArrowShape(sx1, sy1, angle));
				} else {
					area.add(getArrowShape(sx1, sy1, angle));
				}
				drawBackArrow = 0;
			}
			it.next();
		}

		if (drawDirectArrow) {
			double dx = x2 - x1;
			double dy = y2 - y1;
			double angle = Math.atan2(dx, dy);
			if (fitToPoint) {
				area.subtract(getFitToPointArrowNegativeShape(x2, y2, angle));
				area.add(getFitToPointArrowShape(x2, y2, angle));
			} else {
				area.add(getArrowShape(x2, y2, angle));
			}
			drawDirectArrow = false;
		}
		return area;
	}

	private Area getArrowShape(double x, double y, double angle) {
		AffineTransform transform = AffineTransform.getTranslateInstance(x, y);
		transform.rotate(-angle);

		Path2D arrowShape = new Path2D.Double();
		arrowShape.moveTo(-getArrowWidth(), -1);
		arrowShape.lineTo(0, getArrowLength());
		arrowShape.lineTo(getArrowWidth(), -1);
		return new Area(transform.createTransformedShape(arrowShape));
	}

	private Area getFitToPointArrowShape(double x, double y, double angle) {
		AffineTransform transform = AffineTransform.getTranslateInstance(x, y);
		transform.rotate(-angle);

		Path2D arrowShape = new Path2D.Double();
		arrowShape.moveTo(0, 0);
		arrowShape.lineTo(-getArrowWidth(), -getArrowLength());
		arrowShape.lineTo(getArrowWidth(), -getArrowLength());
		return new Area(transform.createTransformedShape(arrowShape));
	}

	private Area getFitToPointArrowNegativeShape(double x, double y, double angle) {
		AffineTransform transform = AffineTransform.getTranslateInstance(x, y);
		transform.rotate(-angle);

		double cutHeight = getArrowLength() * (1 - (getArrowWidth() - width / 2.0) / width);
		Path2D arrowShape = new Path2D.Double();
		arrowShape.moveTo(-getArrowWidth(), 10);
		arrowShape.lineTo(-getArrowWidth(), -cutHeight);
		arrowShape.lineTo(getArrowWidth(), -cutHeight);
		arrowShape.lineTo(getArrowWidth(), 10);
		return new Area(transform.createTransformedShape(arrowShape));
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

	public boolean isFitToPoint() {
		return fitToPoint;
	}

	public void setFitToPoint(boolean fitToPoint) {
		this.fitToPoint = fitToPoint;
	}
}
