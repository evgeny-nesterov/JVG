package ru.nest.jvg.actionarea;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.shape.JVGShape;

public class JVGScaleActionArea extends JVGTransformActionArea implements Constants {
	public JVGScaleActionArea(int type) {
		super(false);
		this.type = type;
		setName("scale-" + getDescr());
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
			JVGShape parentShape = (JVGShape) p;
			boolean isOriginalSelection = parentShape.isOriginalBounds();

			Rectangle2D bounds;
			if (isOriginalSelection) {
				bounds = getParentInitialBounds();

				double[] point = new double[] { x, y, x + dx, y + dy };
				parentShape.getInverseTransform().transform(point, 0, point, 0, 2);
				x = point[0];
				y = point[1];
				dx = point[2] - x;
				dy = point[3] - y;
			} else {
				bounds = parentShape.getRectangleBounds();
			}

			double translateX = 0;
			double translateY = 0;
			double scaleX = 1;
			double scaleY = 1;

			if (type == TOP || type == TOP_RIGHT || type == TOP_LEFT) {
				if (isOriginalSelection) {
					double delta = bounds.getHeight() + (dy > 0 ? -dy : dy);
					if (delta < 1 && delta > -1) {
						if (dy > 0) {
							dy++;
						} else if (dy < 0) {
							dy--;
						}
					}
				} else {
					if (dy > 0) {
						double delta = bounds.getHeight() - dy;
						if (delta < 1) {
							dy = bounds.getHeight() - 1;
						}
					}
				}

				translateY = bounds.getY() + bounds.getHeight();
				scaleY = (bounds.getHeight() - dy) / bounds.getHeight();
			} else if (type == BOTTOM || type == BOTTOM_RIGHT || type == BOTTOM_LEFT) {
				if (isOriginalSelection) {
					double delta = bounds.getHeight() + (dy > 0 ? -dy : dy);
					if (delta < 1 && delta > -1) {
						if (dy > 0) {
							dy++;
						} else if (dy < 0) {
							dy--;
						}
					}
				} else {
					if (dy < 0) {
						double delta = bounds.getHeight() + dy;
						if (delta < 1) {
							dy = 1 - bounds.getHeight();
						}
					}
				}

				translateY = bounds.getY();
				if (bounds.getHeight() != 0) {
					scaleY = (bounds.getHeight() + dy) / bounds.getHeight();
				}
			}

			if (type == LEFT || type == TOP_LEFT || type == BOTTOM_LEFT) {
				if (isOriginalSelection) {
					double delta = bounds.getWidth() + (dx > 0 ? -dx : dx);
					if (delta < 1 && delta > -1) {
						if (dx > 0) {
							dx++;
						} else if (dx < 0) {
							dx--;
						}
					}
				} else {
					if (dx > 0) {
						double delta = bounds.getWidth() - dx;
						if (delta < 1) {
							dx = bounds.getWidth() - 1;
						}
					}
				}

				translateX = bounds.getX() + bounds.getWidth();
				scaleX = (bounds.getWidth() - dx) / bounds.getWidth();
			} else if (type == RIGHT || type == TOP_RIGHT || type == BOTTOM_RIGHT) {
				if (isOriginalSelection) {
					double delta = bounds.getWidth() + (dx > 0 ? -dx : dx);
					if (delta < 1 && delta > -1) {
						if (dx > 0) {
							dx++;
						} else if (dx < 0) {
							dx--;
						}
					}
				} else {
					if (dx < 0) {
						double delta = bounds.getWidth() + dx;
						if (delta < 1) {
							dx = 1 - bounds.getWidth();
						}
					}
				}

				translateX = bounds.getX();
				if (bounds.getWidth() != 0) {
					scaleX = (bounds.getWidth() + dx) / bounds.getWidth();
				}
			}

			t.setToTranslation(translateX, translateY);
			t.scale(scaleX, scaleY);
			t.translate(-translateX, -translateY);

			if (isOriginalSelection) {
				t.preConcatenate(parentShape.getTransform());
				t.concatenate(parentShape.getInverseTransform());

				double[] point = new double[] { x, y, x + dx, y + dy };
				parentShape.getTransform().transform(point, 0, point, 0, 2);
				x = point[0];
				y = point[1];
				dx = point[2] - x;
				dy = point[3] - y;
			}

			this.dx = dx;
			this.dy = dy;
			return t;
		}

		t.setToIdentity();
		return t;
	}

	private double dx;

	@Override
	public double getDeltaX() {
		return dx;
	}

	private double dy;

	@Override
	public double getDeltaY() {
		return dy;
	}

	@Override
	protected Shape computeActionBounds() {
		JVGComponent p = getParent();
		if (p instanceof JVGShape) {
			JVGShape parentShape = (JVGShape) p;
			boolean isOriginalSelection = parentShape.isOriginalBounds();

			Rectangle2D bounds;
			if (isOriginalSelection) {
				bounds = getParentInitialBounds();
			} else {
				bounds = parentShape.getRectangleBounds();
			}
			if (bounds == null) {
				return null;
			}

			double x;
			double y;
			switch (type) {
				case TOP:
					x = bounds.getX() + bounds.getWidth() / 2; // - width / 2.0;
					y = bounds.getY(); // - height;
					break;

				case LEFT:
					x = bounds.getX(); // - width;
					y = bounds.getY() + bounds.getHeight() / 2; // - height / 2.0;
					break;

				case BOTTOM:
					x = bounds.getX() + bounds.getWidth() / 2; // - width / 2.0;
					y = bounds.getY() + bounds.getHeight();
					break;

				case RIGHT:
					x = bounds.getX() + bounds.getWidth();
					y = bounds.getY() + bounds.getHeight() / 2; // - height / 2.0;
					break;

				case TOP_LEFT:
					x = bounds.getX(); // - width;
					y = bounds.getY(); // - height;
					break;

				case TOP_RIGHT:
					x = bounds.getX() + bounds.getWidth();
					y = bounds.getY(); // - height;
					break;

				case BOTTOM_LEFT:
					x = bounds.getX(); // - width;
					y = bounds.getY() + bounds.getHeight();
					break;

				case BOTTOM_RIGHT:
					x = bounds.getX() + bounds.getWidth();
					y = bounds.getY() + bounds.getHeight();
					break;

				default:
					x = bounds.getX() + bounds.getWidth() / 2; // - width / 2.0;
					y = bounds.getY() + bounds.getHeight() / 2; // - height / 2.0;
					break;
			}

			if (isOriginalSelection) {
				double[] point = new double[] { x, y };
				AffineTransform transform = parentShape.getTransform();
				transform.transform(point, 0, point, 0, 1);
				x = point[0];
				y = point[1];
			}
			return new Rectangle2D.Double(x - 2.5, y - 2.5, 5, 5);
		} else {
			return null;
		}
	}

	@Override
	protected Color getColor() {
		return Color.white;
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
			case BOTTOM:
			case TOP:
				return Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);

			case LEFT:
			case RIGHT:
				return Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);

			case TOP_LEFT:
			case BOTTOM_RIGHT:
				return Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);

			case TOP_RIGHT:
			case BOTTOM_LEFT:
				return Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
		}

		return Cursor.getDefaultCursor();
	}

	public String getDescr() {
		return ActionUtil.getDescr(type);
	}
}
