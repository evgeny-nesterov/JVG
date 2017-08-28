package ru.nest.jvg.actionarea;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.ImageIcon;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.event.JVGFocusEvent;
import ru.nest.jvg.event.JVGMouseAdapter;
import ru.nest.jvg.event.JVGMouseEvent;
import ru.nest.jvg.shape.JVGShape;

public class JVGRotateActionArea extends JVGTransformActionArea {
	public final static int RADIUS_NO_INCREMENT = 200;

	private static Cursor centerCursor;

	private static Cursor rotateCursor;
	static {
		ImageIcon img = new ImageIcon(JVGRotateActionArea.class.getResource("cursors/cursor_rotate_center.png"));
		centerCursor = Toolkit.getDefaultToolkit().createCustomCursor(img.getImage(), new Point(16, 16), "rotate-center");

		img = new ImageIcon(JVGRotateActionArea.class.getResource("cursors/cursor_rotate.png"));
		rotateCursor = Toolkit.getDefaultToolkit().createCustomCursor(img.getImage(), new Point(16, 16), "rotate");
	}

	private double weightX = 0.5, weightY = 0.5;

	private boolean firstAdd = false;

	private double angleIncrement;

	private AffineTransform t = new AffineTransform();

	private double angle;

	public JVGRotateActionArea() {
		this(0);
	}

	public JVGRotateActionArea(double angleIncrement) {
		super(true);

		setName("rotate");
		this.angleIncrement = angleIncrement;

		addMouseListener(new JVGMouseAdapter() {
			@Override
			public void mouseEntered(JVGMouseEvent e) {
				canTranslateCenter = true;
				repaint();
			}

			@Override
			public void mouseMoved(JVGMouseEvent e) {
				if (isCenter(e.getX(), e.getY())) {
					setCursor(centerCursor);
				} else {
					setCursor(rotateCursor);
				}
			}
		});
		setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
	}

	@Override
	public void addNotify() {
		super.addNotify();

		if (firstAdd && !listenParentFocus) {
			firstAdd = false;
			listenParentFocus = true;
			parent.addFocusListener(this);
		}
	}

	@Override
	public void removeNotify() {
		firstAdd = true;
		super.removeNotify();
	}

	@Override
	public void focusLost(JVGFocusEvent event) {
		super.focusLost(event);
		canTranslateCenter = false;
	}

	@Override
	public AffineTransform getTransform(double x, double y, double dx, double dy) {
		if (!isTranslateCenter) {
			t.setToIdentity();

			updateCenterPoint();

			double x0 = rotatePoints[0];
			double y0 = rotatePoints[1];
			double x2 = x + dx;
			double y2 = y + dy;

			double dx1 = x - x0, dy1 = y - y0;
			double dx2 = x2 - x0, dy2 = y2 - y0;
			double radius = Math.sqrt(dx1 * dx1 + dy1 * dy1);
			double newRadius = Math.sqrt(dx2 * dx2 + dy2 * dy2);
			if (radius != 0 && newRadius != 0) {
				double alfa = Math.acos((dx2 * dx1 + dy2 * dy1) / (radius * newRadius));
				boolean clockwise = dx2 * dy1 < dx1 * dy2;
				if (!clockwise) {
					alfa = -alfa;
				}
				angle += alfa;

				double radiusWithoutIncrement = RADIUS_NO_INCREMENT / getPaneScale();
				if (angleIncrement > 0 && radius < radiusWithoutIncrement) {
					double increment = (int) ((curAlfa + angle) / angleIncrement) * angleIncrement - curAlfa;
					curAlfa += increment;
					angle -= increment;
					t.rotate(increment, x0, y0);
				} else {
					t.rotate(angle, x0, y0);
					curAlfa += angle;
					angle = 0;
				}
				return t;
			}
			return t;
		} else {
			setCenterPoint(x + dx, y + dy);
			return null;
		}
	}

	@Override
	public double getDraggedX(JVGMouseEvent e) {
		if (isTranslateCenter) {
			return super.getDraggedX(e);
		} else {
			return e.getX();
		}
	}

	@Override
	public double getDraggedY(JVGMouseEvent e) {
		if (isTranslateCenter) {
			return super.getDraggedY(e);
		} else {
			return e.getY();
		}
	}

	@Override
	public double getPressedX(JVGMouseEvent e) {
		if (isTranslateCenter) {
			return super.getPressedX(e);
		} else {
			return e.getX();
		}
	}

	@Override
	public double getPressedY(JVGMouseEvent e) {
		if (isTranslateCenter) {
			return super.getPressedY(e);
		} else {
			return e.getY();
		}
	}

	private boolean canTranslateCenter = false;

	private boolean isTranslateCenter = false;

	private double curAlfa = 0;

	private boolean isActive = false;

	@Override
	public void start(JVGMouseEvent e) {
		isTranslateCenter = canTranslateCenter && isCenter(e.getX(), e.getY());
		isActive = true;
	}

	@Override
	public void finish(JVGMouseEvent e) {
		isActive = false;
		isTranslateCenter = false;
		invalidate();
	}

	private boolean isCenter(double x, double y) {
		double scale = getPaneScale();
		double delta = 2 / scale;
		return rotatePoints != null && x >= rotatePoints[0] - delta && x <= rotatePoints[0] + delta && y >= rotatePoints[1] - delta && y <= rotatePoints[1] + delta;
	}

	@Override
	public boolean contains(double x, double y) {
		return isActive() && (getActionsFilter() == null || getActionsFilter().pass(this)) && ((getScaledBounds() != null && getScaledBounds().contains(x, y)) || (canTranslateCenter && isCenter(x, y)));
	}

	// center point, bounds point, rotate point, minus infinity point, plus, infinity point
	private double[] rotatePoints = new double[10];

	@Override
	protected Shape computeActionBounds() {
		updateCenterPoint();

		JVGComponent parent = getParent();
		if (parent instanceof JVGShape) {
			JVGShape shape = (JVGShape) parent;
			Shape b = shape.getInitialBounds();
			if (b == null) {
				return null;
			}

			Rectangle2D parentBounds = b instanceof Rectangle2D ? (Rectangle2D) b : b.getBounds2D();

			double cx = parentBounds.getX() + weightX * parentBounds.getWidth();
			double cy = parentBounds.getY() + weightY * parentBounds.getHeight();
			double h = 20 / getPaneScale();
			rotatePoints[0] = cx;
			rotatePoints[1] = cy;
			rotatePoints[2] = cx;
			rotatePoints[3] = parentBounds.getY();
			rotatePoints[4] = cx;
			rotatePoints[5] = rotatePoints[3] - h;
			rotatePoints[6] = cx;
			rotatePoints[7] = rotatePoints[3] - 4000;
			rotatePoints[8] = cx;
			rotatePoints[9] = rotatePoints[3] + 4000;

			AffineTransform transform = shape.getTransform();
			transform.transform(rotatePoints, 0, rotatePoints, 0, 5);

			// adjust rotate point: its height over bounds of the shape has to
			// be equal to 20 pixels
			double deltaX = rotatePoints[4] - rotatePoints[2];
			double deltaY = rotatePoints[5] - rotatePoints[3];
			double radius = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
			rotatePoints[4] = rotatePoints[2] + deltaX * h / radius;
			rotatePoints[5] = rotatePoints[3] + deltaY * h / radius;

			return new Rectangle2D.Double(rotatePoints[4] - 3, rotatePoints[5] - 3, 6, 6);
		} else {
			return null;
		}
	}

	// private Rectangle2D parentBounds;
	public void updateCenterPoint() {
		JVGComponent parent = getParent();
		if (parent instanceof JVGShape) {
			JVGShape shape = (JVGShape) parent;
			if (!shape.isValid()) {
				return;
			}
			Shape b = shape.getInitialBounds();
			if (b == null) {
				return;
			}

			Rectangle2D parentBounds = b instanceof Rectangle2D ? (Rectangle2D) b : b.getBounds2D();

			double cx = parentBounds.getX() + weightX * parentBounds.getWidth();
			double cy = parentBounds.getY() + weightY * parentBounds.getHeight();
			rotatePoints[0] = cx;
			rotatePoints[1] = cy;

			shape.getTransform().transform(rotatePoints, 0, rotatePoints, 0, 1);
		}
	}

	public void setCenterPoint(double x, double y) {
		JVGComponent parent = getParent();
		if (parent instanceof JVGShape) {
			JVGShape shape = (JVGShape) parent;
			Rectangle2D bounds = shape.getInitialBounds().getBounds2D();

			Point2D p = new Point2D.Double();
			p.setLocation(x, y);
			try {
				shape.getTransform().inverseTransform(p, p);
			} catch (NoninvertibleTransformException exc) {
			}
			x = p.getX();
			y = p.getY();

			weightX = (x - bounds.getX()) / bounds.getWidth();
			weightY = (y - bounds.getY()) / bounds.getHeight();
		} else {
			weightX = 0.5;
			weightY = 0.5;
		}

		updateCenterPoint();
	}

	private final static Stroke stroke = new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 1f, new float[] { 3f, 3f }, 0f);

	private NumberFormat angleFormat = new DecimalFormat("##.##");

	@Override
	public void paintAction(Graphics2D g, AffineTransform transform) {
		if (rotatePoints != null) {
			double cx = rotatePoints[0];
			double cy = rotatePoints[1];
			double bx = rotatePoints[2];
			double by = rotatePoints[3];
			double rx = rotatePoints[4];
			double ry = rotatePoints[5];

			double sx = transform.getScaleX();
			double sy = transform.getScaleY();

			double dx1 = 3.5 / sx;
			double dy1 = 3.5 / sy;
			Shape circle1 = new Arc2D.Double(cx - dx1 / 2, cy - dy1 / 2, dx1, dy1, 0, 360, Arc2D.OPEN);
			circle1 = transform.createTransformedShape(circle1);

			if (isActive && !isTranslateCenter) {
				// axis line
				Stroke oldStroke = g.getStroke();
				g.setStroke(stroke);
				g.setColor(Color.gray);
				g.draw(transform.createTransformedShape(new Line2D.Double(rotatePoints[6], rotatePoints[7], rotatePoints[8], rotatePoints[9])));
				g.setStroke(oldStroke);

				// center small circle
				g.setColor(Color.black);
				g.fill(circle1);
				g.setColor(Color.white);
				g.draw(circle1);

				Rectangle b = circle1.getBounds();
				String angleText = angleFormat.format(((Math.toDegrees(curAlfa) % 360) + 360) % 360) + "°";
				g.setColor(new Color(220, 220, 220, 200));
				g.drawString(angleText, (int) b.getCenterX() + 10 - 1, (int) b.getCenterY() + 10 - 1);
				g.drawString(angleText, (int) b.getCenterX() + 10 - 1, (int) b.getCenterY() + 10 + 1);
				g.drawString(angleText, (int) b.getCenterX() + 10 + 1, (int) b.getCenterY() + 10 - 1);
				g.drawString(angleText, (int) b.getCenterX() + 10 + 1, (int) b.getCenterY() + 10 + 1);
				g.setColor(Color.black);
				g.drawString(angleText, (int) b.getCenterX() + 10, (int) b.getCenterY() + 10);
			} else if (canTranslateCenter) {
				// line which connecting rotate point and center
				Stroke oldStroke = g.getStroke();
				g.setStroke(stroke);
				g.setColor(Color.gray);
				g.draw(transform.createTransformedShape(new Line2D.Double(cx, cy, rx, ry)));
				g.setStroke(oldStroke);

				// center small circle
				g.setColor(Color.black);
				g.fill(circle1);
				g.setColor(Color.white);
				g.draw(circle1);
			} else if (!canTranslateCenter) {
				// line which connecting rotate point and bounds of shape
				Stroke oldStroke = g.getStroke();
				g.setStroke(stroke);
				g.setColor(Color.gray);
				g.draw(transform.createTransformedShape(new Line2D.Double(bx, by, rx, ry)));
				g.setStroke(oldStroke);
			}

			double dx2 = 7 / sx;
			double dy2 = 7 / sy;
			Shape circle2 = new Arc2D.Double(rx - dx2 / 2, ry - dy2 / 2, dx2, dy2, 0, 360, Arc2D.OPEN);
			circle2 = transform.createTransformedShape(circle2);

			// draw rotate point
			g.setColor(Color.yellow);
			g.fill(circle2);
			g.setColor(Color.black);
			g.draw(circle2);
		}
	}
}
