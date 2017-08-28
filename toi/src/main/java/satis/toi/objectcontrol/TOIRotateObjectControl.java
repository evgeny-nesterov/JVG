package satis.toi.objectcontrol;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import satis.toi.TOIObject;
import satis.toi.TOIObjectControl;
import satis.toi.TOIPaintContext;
import satis.toi.TOIPane;

public class TOIRotateObjectControl implements TOIObjectControl {
	public static Stroke rotateStroke = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[] { 2f, 2f }, 0f);

	public final static int RADIUS_NO_INCREMENT = 150;

	private TOIObject o;

	public TOIRotateObjectControl(TOIObject o) {
		this.o = o;
	}

	private double mx, my;

	private boolean selection;

	private boolean pressed;

	private double angle;

	private double angleIncrement = Math.PI / 12.0;

	private double curAlfa;

	private Point2D selectioncp;

	@Override
	public void processMouseEvent(MouseEvent e, double x, double y, double adjustX, double adjustY) {
		if (e.getID() == MouseEvent.MOUSE_PRESSED) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				TOIPane pane = (TOIPane) e.getComponent();
				selection = isSelection(pane, x, y);

				Rectangle2D selectionBounds = pane.getSelectionBounds();
				selectioncp = new Point2D.Double(selectionBounds.getCenterX(), selectionBounds.getCenterY());

				mx = x;
				my = y;
				pressed = true;
				angle = 0;
				e.getComponent().repaint();
			}
		} else if (e.getID() == MouseEvent.MOUSE_RELEASED) {
			if (pressed) {
				selectioncp = null;
				pressed = false;
				e.getComponent().repaint();
			}
		} else if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
			if (pressed) {
				TOIPane pane = (TOIPane) e.getComponent();
				Rectangle2D origBounds = !selection ? o.getOriginalBounds() : pane.getSelectionBounds();

				Point2D cp;
				if (!selection) {
					cp = new Point2D.Double(origBounds.getCenterX(), origBounds.getCenterY());
					o.getTransform().transform(cp, cp);
				} else {
					cp = new Point2D.Double(selectioncp.getX(), selectioncp.getY());
				}

				double cx = cp.getX();
				double cy = cp.getY();
				double dx1 = mx - cx;
				double dy1 = my - cy;
				double dx2 = x - cx;
				double dy2 = y - cy;
				double radius = Math.sqrt(dx1 * dx1 + dy1 * dy1);
				double newRadius = Math.sqrt(dx2 * dx2 + dy2 * dy2);
				if (radius != 0 && newRadius != 0) {
					double alfa = Math.acos((dx2 * dx1 + dy2 * dy1) / (radius * newRadius));
					boolean clockwise = dx2 * dy1 < dx1 * dy2;
					if (!clockwise) {
						alfa = -alfa;
					}
					angle += alfa;

					double radiusWithoutIncrement = RADIUS_NO_INCREMENT / pane.getZoom();
					AffineTransform t = new AffineTransform();
					if (angleIncrement > 0 && radius < radiusWithoutIncrement) {
						double increment = (int) ((curAlfa + angle) / angleIncrement) * angleIncrement - curAlfa;
						curAlfa += increment;
						angle -= increment;
						t.rotate(increment, cx, cy);
					} else {
						t.rotate(angle, cx, cy);
						curAlfa += angle;
						angle = 0;
					}

					if (selection) {
						pane.transformSelection(t);
					} else {
						o.transform(t);
						pane.repaint();
					}
				}
				mx = x;
				my = y;
			}
		} else if (e.getID() == MouseEvent.MOUSE_MOVED) {
		}
	}

	public Point2D getPoint(TOIPane pane, double x, double y, boolean selection) {
		Rectangle2D origBounds = !selection ? o.getOriginalBounds() : pane.getSelectionBounds();

		Point2D p1 = new Point2D.Double(origBounds.getX() + origBounds.getWidth() / 2, origBounds.getY());
		if (!selection) {
			o.getTransform().transform(p1, p1);
		}

		double h = 12 / pane.getZoom();

		Point2D p2 = new Point2D.Double(origBounds.getX() + origBounds.getWidth() / 2, origBounds.getY() - h);
		if (!selection) {
			o.getTransform().transform(p2, p2);
		}

		double r = p1.distance(p2);
		if (r != h) {
			h *= h / r;
			p2.setLocation(origBounds.getX() + origBounds.getWidth() / 2, origBounds.getY() - h);
			if (!selection) {
				o.getTransform().transform(p2, p2);
			}
		}
		return p2;
	}

	public boolean isSelection(TOIPane pane, double x, double y) {
		if (pane.getSelectionSize() > 1) {
			Point2D p = getPoint(pane, x, y, true);
			if (x >= p.getX() - 3 && x <= p.getX() + 3 && y >= p.getY() - 3 && y <= p.getY() + 3) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean contains(TOIPane pane, double x, double y) {
		if (o.isSelected() && o.isFocused()) {
			Point2D p = getPoint(pane, x, y, false);
			if (x >= p.getX() - 3 && x <= p.getX() + 3 && y >= p.getY() - 3 && y <= p.getY() + 3) {
				return true;
			}

			if (pane.getSelectionSize() > 1) {
				p = getPoint(pane, x, y, true);
				if (x >= p.getX() - 3 && x <= p.getX() + 3 && y >= p.getY() - 3 && y <= p.getY() + 3) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void paint(Graphics2D g, Graphics2D gt, TOIPaintContext ctx) {
		if (o.isSelected() && o.isFocused()) {
			paint(g, gt, ctx, false);
			if (ctx.getPane().getSelectionSize() > 1) {
				paint(g, gt, ctx, true);
			}
		}
	}

	public void paint(Graphics2D g, Graphics2D gt, TOIPaintContext ctx, boolean selection) {
		Rectangle2D origBounds = !selection ? o.getOriginalBounds() : ctx.getPane().getSelectionBounds();

		Point2D cp;
		if (selection && selectioncp != null) {
			cp = new Point2D.Double(selectioncp.getX(), selectioncp.getY());
		} else {
			cp = new Point2D.Double(origBounds.getCenterX(), origBounds.getCenterY());
		}

		Point2D p1 = new Point2D.Double(cp.getX(), origBounds.getY());
		if (!selection) {
			o.getTransform().transform(p1, p1);
		}
		ctx.getTransform().transform(p1, p1);

		Point2D p2 = new Point2D.Double(cp.getX(), origBounds.getY() - 12 / ctx.getTransform().getScaleY());
		if (!selection) {
			o.getTransform().transform(p2, p2);
		}
		ctx.getTransform().transform(p2, p2);

		if (!selection) {
			double r = p1.distance(p2);
			if (r != 12) {
				double scale = 12 / r;
				p2.setLocation(cp.getX(), origBounds.getY() - scale * 12 / ctx.getTransform().getScaleY());
				o.getTransform().transform(p2, p2);
				ctx.getTransform().transform(p2, p2);
			}
		}

		if (!selection) {
			o.getTransform().transform(cp, cp);
		}
		ctx.getTransform().transform(cp, cp);

		double as = 6;
		Shape a = new Arc2D.Double(p2.getX() - as / 2, p2.getY() - as / 2, as, as, 0, 360, Arc2D.OPEN);
		Shape l = new Line2D.Double(p1, p2);

		Stroke oldStroke = g.getStroke();
		g.setStroke(rotateStroke);
		g.setColor(Color.lightGray);
		g.draw(l);
		g.setStroke(oldStroke);

		g.setColor(Color.yellow);
		g.fill(a);
		g.setColor(Color.blue);
		g.draw(a);

		if (pressed && this.selection == selection) {
			Shape ca = new Arc2D.Double(cp.getX() - 1.5, cp.getY() - 1.5, 3, 3, 0, 360, Arc2D.OPEN);
			g.setColor(Color.white);
			g.fill(ca);
			g.setColor(Color.gray);
			g.draw(ca);
		}
	}

	@Override
	public Cursor getCursor() {
		return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
	}
}
