package ru.nest.toi.objects;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

import ru.nest.toi.Arrow;
import ru.nest.toi.MutablePath;
import ru.nest.toi.PathElementPoint;
import ru.nest.toi.TOIPaintContext;
import ru.nest.toi.TOIUtil;

public class TOIArrowPathElement extends TOIPath implements Arrow, Comparable<TOIArrowPathElement> {
	public final static int DIRECTION_NONE = 0;

	public final static int DIRECTION_DIRECT = 1;

	public final static int DIRECTION_BACK = 2;

	public final static int DIRECTION_BOTH = 3;

	private Stroke stroke;

	private Stroke outlineStroke;

	private double width = 14;

	private double arrowWidth = 12;

	private double arrowLength = 6;

	private int direction = DIRECTION_DIRECT;

	private double percentPos;

	private double pos;

	Point2D endPoint;

	private TOIMultiArrowPath parent;

	private Color outlineColor = Color.black;

	private Shape outlineBodyShape;

	private Shape outlineArrowShape1;

	private Shape outlineArrowShape2;

	private double outlineWidth = 2;

	public TOIArrowPathElement(TOIMultiArrowPath parent, double percentPos) {
		this.parent = parent;
		this.percentPos = percentPos;
		setColor(Color.lightGray);
	}

	public TOIArrowPathElement() {
		this(null, 1);
	}

	@Override
	public void paintPath(Graphics2D g, Graphics2D gt, TOIPaintContext ctx) {
		paintOutline(g, gt, ctx);
		paintArrow(g, gt, ctx);
	}

	public void paintArrow(Graphics2D g, Graphics2D gt, TOIPaintContext ctx) {
		if (getShape() != null) {
			Color color = ctx.getColorRenderer().getColor(this, Color.lightGray);
			if (outlineArrowShape1 != null) {
				if (color.getRed() + color.getGreen() + color.getBlue() > 50) {
					gt.setColor(outlineColor);
				} else {
					gt.setColor(Color.white);
				}
				gt.fill(outlineArrowShape1);
			}
			if (outlineArrowShape2 != null) {
				if (color.getRed() + color.getGreen() + color.getBlue() > 50) {
					gt.setColor(outlineColor);
				} else {
					gt.setColor(Color.white);
				}
				gt.fill(outlineArrowShape2);
			}

			gt.setColor(color);
			gt.fill(getShape());
		}
	}

	public void paintOutline(Graphics2D g, Graphics2D gt, TOIPaintContext ctx) {
		if (getShape() != null) {
			Color color = ctx.getColorRenderer().getColor(this, Color.lightGray);
			if (outlineBodyShape != null) {
				if (color.getRed() + color.getGreen() + color.getBlue() > 50) {
					gt.setColor(outlineColor);
				} else {
					gt.setColor(Color.white);
				}
				gt.fill(outlineBodyShape);
			}
		}
	}

	public boolean isLast() {
		return parent != null && parent.getElements().indexOf(this) == parent.getElements().size() - 1;
	}

	@Override
	public void validate() {
		if (!isValid()) {
			super.validate();

			if (parent != null) {
				parent.validate();
			}

			if (path != null && path.numCoords >= 4) {
				Stroke outlineStroke = getOutlineStroke();
				double outlineWidth = getOutlineWidth();

				outlineArrowShape1 = null;
				Area a1 = null;
				Area a1_ = null;
				{
					double lastx = path.doubleCoords[path.numCoords - 2];
					double lasty = path.doubleCoords[path.numCoords - 1];
					double dx = lastx - path.doubleCoords[path.numCoords - 4];
					double dy = lasty - path.doubleCoords[path.numCoords - 3];
					double angle = Math.atan2(dx, dy);

					AffineTransform transform = AffineTransform.getTranslateInstance(lastx, lasty);
					transform.rotate(-angle);

					if (direction == DIRECTION_DIRECT || direction == DIRECTION_BOTH) {
						MutablePath.Double arrowShape = new MutablePath.Double();
						arrowShape.moveTo(-getArrowWidth(), 0);
						arrowShape.lineTo(0, getArrowLength());
						arrowShape.lineTo(getArrowWidth(), 0);
						a1 = new Area(transform.createTransformedShape(arrowShape));
					}

					if (outlineStroke != null) {
						MutablePath.Double arrowShape_ = new MutablePath.Double();
						if (direction == DIRECTION_DIRECT || direction == DIRECTION_BOTH) {
							double tanFi = getArrowLength() / getArrowWidth();
							double fi = Math.atan(tanFi);
							double y0 = getArrowLength() + Math.abs(outlineWidth / Math.cos(fi));
							double x0 = (y0 + outlineWidth) / tanFi;
							arrowShape_.moveTo(-x0, -outlineWidth);
							arrowShape_.lineTo(0, y0);
							arrowShape_.lineTo(x0, -outlineWidth);
							outlineArrowShape1 = transform.createTransformedShape(arrowShape_);
						} else {
							double x = getWidth() / 2 + outlineWidth;
							arrowShape_.moveTo(-x, 0);
							arrowShape_.lineTo(-x, outlineWidth);
							arrowShape_.lineTo(x, outlineWidth);
							arrowShape_.lineTo(x, 0);
							a1_ = new Area(transform.createTransformedShape(arrowShape_));
						}
					}
				}

				outlineArrowShape2 = null;
				Area a2 = null;
				Area a2_ = null;
				{
					double firstx = path.doubleCoords[0];
					double firsty = path.doubleCoords[1];
					double dx = firstx - path.doubleCoords[2];
					double dy = firsty - path.doubleCoords[3];
					double angle = Math.atan2(dx, dy);

					AffineTransform transform = AffineTransform.getTranslateInstance(firstx, firsty);
					transform.rotate(-angle);

					if (direction == DIRECTION_BACK || direction == DIRECTION_BOTH) {
						MutablePath.Double arrowShape = new MutablePath.Double();
						arrowShape.moveTo(-getWidth() / 2.0, 0);
						arrowShape.lineTo(-getArrowWidth(), 0);
						arrowShape.lineTo(0, getArrowLength());
						arrowShape.lineTo(getArrowWidth(), 0);
						arrowShape.lineTo(getWidth() / 2.0, 0);
						a2 = new Area(transform.createTransformedShape(arrowShape));
					}

					if (outlineStroke != null) {
						MutablePath.Double arrowShape_ = new MutablePath.Double();
						if (direction == DIRECTION_BACK || direction == DIRECTION_BOTH) {
							double tanFi = getArrowLength() / getArrowWidth();
							double fi = Math.atan(tanFi);
							double y0 = getArrowLength() + Math.abs(outlineWidth / Math.cos(fi));
							double x0 = (y0 + outlineWidth) / tanFi;
							arrowShape_.moveTo(-x0, -outlineWidth);
							arrowShape_.lineTo(0, y0);
							arrowShape_.lineTo(x0, -outlineWidth);
							outlineArrowShape2 = transform.createTransformedShape(arrowShape_);
						} else {
							double x = getWidth() / 2 + outlineWidth;
							arrowShape_.moveTo(-x, 0);
							arrowShape_.lineTo(-x, outlineWidth);
							arrowShape_.lineTo(x, outlineWidth);
							arrowShape_.lineTo(x, 0);
							a2_ = new Area(transform.createTransformedShape(arrowShape_));
						}
					}
				}

				Shape curve = getStroke().createStrokedShape(path);
				if (a1 != null || a2 != null) {
					Area a0 = new Area(curve);
					if (a1 != null) {
						a0.add(a1);
					}
					if (a2 != null) {
						a0.add(a2);
					}
					shape = a0;
				} else {
					shape = curve;
				}

				if (outlineStroke != null) {
					Shape curve_ = getOutlineStroke().createStrokedShape(path);
					Area a0_ = new Area(curve_);
					if (a1_ != null) {
						a0_.add(a1_);
					}
					if (a2_ != null) {
						a0_.add(a2_);
					}
					outlineBodyShape = a0_;
				} else {
					outlineBodyShape = null;
				}

				bounds = shape.getBounds2D();
				try {
					originalBounds = getTransform().createInverse().createTransformedShape(shape).getBounds2D();
				} catch (NoninvertibleTransformException e) {
					e.printStackTrace();
				}
			}

			if (parent != null) {
				pos = parent.length * percentPos;
			}
		}
	}

	@Override
	public boolean contains(double x, double y) {
		return getShape() != null ? getShape().contains(x, y) : false;
	}

	//	@Override
	//	public void transform(AffineTransform transform) {
	//		// translate parent instead
	//	}

	@Override
	public int compareTo(TOIArrowPathElement o) {
		if (percentPos == o.percentPos) {
			return 0;
		}
		return percentPos < o.percentPos ? -1 : 1;
	}

	public PathElementPoint getClosestPoint(double x, double y) {
		if (path == null) {
			return null;
		}

		Point2D closestPoint = null;
		double minDistance = Double.MAX_VALUE;
		double minPos = 0;

		PathIterator it = new FlatteningPathIterator(path.getPathIterator(null), 1);
		float points[] = new float[6];
		float moveX = 0, moveY = 0;
		float x1 = 0, y1 = 0;
		float x2 = 0, y2 = 0;
		float totalLength = 0;
		while (!it.isDone()) {
			int type = it.currentSegment(points);
			switch (type) {
				case PathIterator.SEG_MOVETO:
					moveX = x1 = points[0];
					moveY = y1 = points[1];
					break;

				case PathIterator.SEG_CLOSE:
					points[0] = moveX;
					points[1] = moveY;
					// Fall into....

				case PathIterator.SEG_LINETO:
					x2 = points[0];
					y2 = points[1];

					double dx21 = x2 - x1;
					double dy21 = y2 - y1;
					double dx1 = x1 - x;
					double dy1 = y1 - y;
					double r_sq = dx21 * dx21 + dy21 * dy21;
					double alfa = -(dx21 * dx1 + dy21 * dy1) / r_sq;
					double r = Math.sqrt(r_sq);

					double x_;
					double y_;
					double dist, pos;
					if (alfa <= 0) {
						x_ = x1;
						y_ = y1;
						dist = Math.sqrt(dx1 * dx1 + dy1 * dy1);
						pos = totalLength;
					} else if (alfa >= 1) {
						x_ = x2;
						y_ = y2;
						double dx2 = x2 - x;
						double dy2 = y2 - y;
						dist = Math.sqrt(dx2 * dx2 + dy2 * dy2);
						pos = totalLength + r;
					} else {
						x_ = x1 + alfa * dx21;
						y_ = y1 + alfa * dy21;
						double dx_ = x_ - x;
						double dy_ = y_ - y;
						dist = Math.sqrt(dx_ * dx_ + dy_ * dy_);
						pos = totalLength + alfa * r;
					}

					if (dist < minDistance) {
						minDistance = dist;
						if (closestPoint == null) {
							closestPoint = new Point2D.Double();
						}
						closestPoint.setLocation(x_, y_);
						minPos = pos;
					}

					totalLength += (float) r;
					x1 = x2;
					y1 = y2;
					break;
			}
			it.next();
		}

		if (closestPoint != null) {
			PathElementPoint p = new PathElementPoint();
			p.point = closestPoint;
			p.dist = minDistance;
			p.element = this;
			p.elementPercentPos = minPos / length;
			p.elementPos = minPos;
			p.pos = pos - (1 - p.elementPercentPos) * length;
			p.percentPos = parent != null ? p.pos / parent.length : 1;
			return p;
		}
		return null;
	}

	public double getPercentPos() {
		return percentPos;
	}

	public void setPercentPos(double percentPos) {
		this.percentPos = percentPos;
		parent.invalidate();
	}

	public double getPos() {
		return pos;
	}

	@Override
	public MutablePath.Double getPath() {
		return path;
	}

	@Override
	public void setPath(MutablePath.Double path) {
		this.path = path;

		length = TOIUtil.measurePathLength(path);

		double lastx = path.doubleCoords[path.numCoords - 2];
		double lasty = path.doubleCoords[path.numCoords - 1];
		if (endPoint == null) {
			endPoint = new Point2D.Double(lastx, lasty);
		} else {
			endPoint.setLocation(lastx, lasty);
		}

		invalidate();
	}

	public TOIMultiArrowPath getParent() {
		return parent;
	}

	public Stroke getStroke() {
		if (parent != null) {
			return parent.getStroke();
		}
		if (stroke == null) {
			stroke = new BasicStroke((float) getWidth(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
		}
		return stroke;
	}

	public Stroke getOutlineStroke() {
		if (getOutlineWidth() <= 0) {
			outlineStroke = null;
			return null;
		}
		if (outlineStroke == null) {
			outlineStroke = new BasicStroke((float) (getWidth() + 2 * getOutlineWidth()), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
		}
		return outlineStroke;
	}

	@Override
	public double getWidth() {
		if (parent != null) {
			return parent.getWidth();
		}
		return width;
	}

	@Override
	public void setWidth(double width) {
		this.width = width;
		stroke = null;
		outlineStroke = null;
		invalidate();
	}

	@Override
	public double getArrowWidth() {
		if (parent != null) {
			return parent.getArrowWidth();
		}
		return arrowWidth;
	}

	@Override
	public void setArrowWidth(double arrowWidth) {
		this.arrowWidth = arrowWidth;
		invalidate();
	}

	@Override
	public double getArrowLength() {
		if (parent != null) {
			return parent.getArrowLength();
		}
		return arrowLength;
	}

	@Override
	public void setArrowLength(double arrowLength) {
		this.arrowLength = arrowLength;
		invalidate();
	}

	@Override
	public void setScale(double scaleWidth, double scaleArrowWidth, double scaleArrowLength) {
		width = 14 * scaleWidth;
		arrowWidth = 12 * scaleArrowWidth;
		arrowLength = 6 * scaleArrowLength;
		stroke = null;
		outlineStroke = null;
		invalidate();
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
		invalidate();
	}

	public double getOutlineWidth() {
		if (parent != null) {
			return parent.getOutlineWidth();
		}
		return outlineWidth;
	}

	public void setOutlineWidth(double outlineWidth) {
		this.outlineWidth = outlineWidth;
		invalidate();
	}
}
