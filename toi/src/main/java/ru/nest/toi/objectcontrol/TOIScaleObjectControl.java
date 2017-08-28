package ru.nest.toi.objectcontrol;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import ru.nest.toi.TOIObject;
import ru.nest.toi.TOIObjectControl;
import ru.nest.toi.TOIPaintContext;
import ru.nest.toi.TOIPane;

public class TOIScaleObjectControl implements TOIObjectControl {
	public final static int N = 0;

	public final static int W = 1;

	public final static int S = 2;

	public final static int E = 3;

	public final static int NW = 4;

	public final static int NE = 5;

	public final static int SW = 6;

	public final static int SE = 7;

	private TOIObject o;

	public TOIScaleObjectControl(TOIObject o) {
		this.o = o;
	}

	private int type;

	private boolean selection;

	private double mx, my;

	private boolean pressed;

	@Override
	public void processMouseEvent(MouseEvent e, double x, double y, double adjustX, double adjustY) {
		if (e.getID() == MouseEvent.MOUSE_PRESSED) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				TOIPane pane = (TOIPane) e.getComponent();
				type = getType(pane, x, y, true);
				if (type != -1) {
					selection = true;
				} else {
					type = getType(pane, x, y, false);
					selection = false;
				}

				if (type != -1) {
					mx = x;
					my = y;
					pressed = true;
					e.getComponent().repaint();
				}
			}
		} else if (e.getID() == MouseEvent.MOUSE_RELEASED) {
			if (pressed) {
				pressed = false;
				e.getComponent().repaint();
			}
		} else if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
			if (pressed) {
				TOIPane pane = (TOIPane) e.getComponent();
				Rectangle2D origBounds = !selection ? o.getOriginalBounds() : pane.getSelectionBounds();

				Point2D p1 = new Point2D.Double(mx, my);
				Point2D p2 = new Point2D.Double(x, y);
				if (!selection) {
					try {
						o.getTransform().inverseTransform(p1, p1);
						o.getTransform().inverseTransform(p2, p2);
					} catch (NoninvertibleTransformException e2) {
						e2.printStackTrace();
					}
				}

				double dx = p2.getX() - p1.getX();
				double dy = p2.getY() - p1.getY();

				double x1 = origBounds.getX();
				double x2 = origBounds.getX() + origBounds.getWidth() / 2;
				double x3 = origBounds.getX() + origBounds.getWidth();
				double y1 = origBounds.getY();
				double y2 = origBounds.getY() + origBounds.getHeight() / 2;
				double y3 = origBounds.getY() + origBounds.getHeight();

				AffineTransform t = new AffineTransform();
				switch (type) {
					case N:
						double sx = 1;
						double sy = (origBounds.getHeight() - dy) / origBounds.getHeight();
						t.translate(x2, y3);
						t.scale(sx, sy);
						t.translate(-x2, -y3);
						break;
					case S:
						sx = 1;
						sy = (origBounds.getHeight() + dy) / origBounds.getHeight();
						t.setToTranslation(x2, y1);
						t.scale(sx, sy);
						t.translate(-x2, -y1);
						break;
					case E:
						sx = (origBounds.getWidth() + dx) / origBounds.getWidth();
						sy = 1;
						t.setToTranslation(x1, y2);
						t.scale(sx, sy);
						t.translate(-x1, -y2);
						break;
					case W:
						sx = (origBounds.getWidth() - dx) / origBounds.getWidth();
						sy = 1;
						t.setToTranslation(x3, y2);
						t.scale(sx, sy);
						t.translate(-x3, -y2);
						break;
					case NW:
						sx = (origBounds.getWidth() - dx) / origBounds.getWidth();
						sy = (origBounds.getHeight() - dy) / origBounds.getHeight();
						t.setToTranslation(x3, y3);
						t.scale(sx, sy);
						t.translate(-x3, -y3);
						break;
					case SE:
						sx = (origBounds.getWidth() + dx) / origBounds.getWidth();
						sy = (origBounds.getHeight() + dy) / origBounds.getHeight();
						t.translate(x1, y1);
						t.scale(sx, sy);
						t.translate(-x1, -y1);
						break;
					case NE:
						sx = (origBounds.getWidth() + dx) / origBounds.getWidth();
						sy = (origBounds.getHeight() - dy) / origBounds.getHeight();
						t.translate(x1, y3);
						t.scale(sx, sy);
						t.translate(-x1, -y3);
						break;
					case SW:
						sx = (origBounds.getWidth() - dx) / origBounds.getWidth();
						sy = (origBounds.getHeight() + dy) / origBounds.getHeight();
						t.translate(x3, y1);
						t.scale(sx, sy);
						t.translate(-x3, -y1);
						break;
					default:
						break;
				}

				try {
					if (selection) {
						pane.transformSelection(t);
					} else {
						t.preConcatenate(o.getTransform());
						t.concatenate(o.getTransform().createInverse());
						o.transform(t);
						pane.repaint();
					}
				} catch (NoninvertibleTransformException e1) {
					e1.printStackTrace();
				}
				mx = x;
				my = y;
			}
		} else if (e.getID() == MouseEvent.MOUSE_MOVED) {
		}
	}

	public int getType(TOIPane pane, double x, double y, boolean selection) {
		Rectangle2D origBounds = !selection ? o.getOriginalBounds() : pane.getSelectionBounds();
		double x1 = origBounds.getX();
		double x2 = origBounds.getX() + origBounds.getWidth() / 2;
		double x3 = origBounds.getX() + origBounds.getWidth();
		double y1 = origBounds.getY();
		double y2 = origBounds.getY() + origBounds.getHeight() / 2;
		double y3 = origBounds.getY() + origBounds.getHeight();

		Point2D[] p = new Point2D[8];
		p[N] = new Point2D.Double(x2, y1);
		p[W] = new Point2D.Double(x1, y2);
		p[S] = new Point2D.Double(x2, y3);
		p[E] = new Point2D.Double(x3, y2);
		p[NW] = new Point2D.Double(x1, y1);
		p[NE] = new Point2D.Double(x3, y1);
		p[SW] = new Point2D.Double(x1, y3);
		p[SE] = new Point2D.Double(x3, y3);

		if (!selection) {
			o.getTransform().transform(p, 0, p, 0, p.length);
		}
		for (int i = 0; i < p.length; i++) {
			if (x >= p[i].getX() - 3 && x <= p[i].getX() + 3 && y >= p[i].getY() - 3 && y <= p[i].getY() + 3) {
				return i;
			}
		}
		return -1;
	}

	public Point2D getPoint(TOIPane pane, int type, boolean selection) {
		Rectangle2D origBounds = !selection ? o.getOriginalBounds() : pane.getSelectionBounds();
		double x = origBounds.getX();
		double y = origBounds.getY();
		switch (type) {
			case N:
				x += origBounds.getWidth() / 2;
				break;
			case W:
				y += origBounds.getHeight() / 2;
				break;
			case S:
				x += origBounds.getWidth() / 2;
				y += origBounds.getHeight();
				break;
			case E:
				x += origBounds.getWidth();
				y += origBounds.getHeight() / 2;
				break;
			case NW:
				break;
			case NE:
				x += origBounds.getWidth();
				break;
			case SE:
				y += origBounds.getHeight();
				break;
			case SW:
				x += origBounds.getWidth();
				y += origBounds.getHeight();
				break;
			default:
				return null;
		}

		Point2D p = new Point2D.Double(x, y);
		if (!selection) {
			o.getTransform().transform(p, p);
		}
		return p;
	}

	@Override
	public boolean contains(TOIPane pane, double x, double y) {
		if (o.isSelected() && o.isFocused()) {
			if (getType(pane, x, y, false) != -1) {
				return true;
			}
			if (pane.getSelectionSize() > 1) {
				if (getType(pane, x, y, true) != -1) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void paint(Graphics2D g, Graphics2D gt, TOIPaintContext ctx) {
		for (int type = 0; type < 8; type++) {
			Point2D p = getPoint(ctx.getPane(), type, false);
			ctx.getTransform().transform(p, p);
			Rectangle2D r = new Rectangle2D.Double(p.getX() - 1.5, p.getY() - 1.5, 3, 3);

			g.setColor(Color.yellow);
			g.fill(r);
			g.setColor(Color.blue);
			g.draw(r);
		}

		if (ctx.getPane().getSelectionSize() > 1) {
			for (int type = 0; type < 8; type++) {
				Point2D p = getPoint(ctx.getPane(), type, true);
				ctx.getTransform().transform(p, p);
				Rectangle2D r = new Rectangle2D.Double(p.getX() - 1.5, p.getY() - 1.5, 3, 3);

				g.setColor(Color.green);
				g.fill(r);
				g.setColor(Color.white);
				g.draw(r);
			}
		}
	}

	@Override
	public Cursor getCursor() {
		return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
	}
}
