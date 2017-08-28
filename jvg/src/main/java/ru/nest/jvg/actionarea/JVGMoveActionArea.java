package ru.nest.jvg.actionarea;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.event.JVGEvent;
import ru.nest.jvg.event.JVGMouseEvent;

public class JVGMoveActionArea extends JVGTransformActionArea {
	public final static int MOVE_CENTER = 0;

	public final static int MOVE_COMPONENT = 1;

	public JVGMoveActionArea(int type) {
		super(type == MOVE_COMPONENT ? VISIBILITY_TYPE_ALWAYS : VISIBILITY_TYPE_SELECTED, true);
		this.type = type;

		setName("move");
		setClipped(true);
	}

	private int type;

	public int getType() {
		return type;
	}

	private AffineTransform t = new AffineTransform();

	@Override
	public AffineTransform getTransform(double x, double y, double dx, double dy) {
		t.setToTranslation(dx, dy);
		return t;
	}

	@Override
	public boolean contains(double x, double y) {
		if (type == MOVE_CENTER) {
			return super.contains(x, y);
		} else {
			return true;
		}
	}

	@Override
	protected Shape computeActionBounds() {
		JVGComponent parent = getParent();
		if (parent != null) {
			Rectangle2D r = parent.getRectangleBounds();
			double x = r.getX() + r.getWidth() / 2;
			double y = r.getY() + r.getHeight() / 2;
			if (type == MOVE_CENTER) {
				return new Rectangle2D.Double(x - 2, y - 2, 4, 4);
			} else {
				return new Rectangle2D.Double(x - 10000, y - 10000, 20000, 20000);
			}
		}
		return null;
	}

	@Override
	public double getPressedX(JVGMouseEvent e) {
		return e.getAdjustedX();
	}

	@Override
	public double getPressedY(JVGMouseEvent e) {
		return e.getAdjustedY();
	}

	@Override
	protected Color getColor() {
		return type == MOVE_CENTER ? Color.green : null;
	}

	@Override
	protected Color getBorderColor() {
		return null;
	}

	@Override
	public Cursor getCursor() {
		if (type == MOVE_CENTER) {
			return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
		} else {
			return Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
		}
	}

	@Override
	protected void dispatchEventImpl(JVGEvent e) {
		super.dispatchEventImpl(e);
		if (parent != null) {
			if (!e.isConsumed()) {
				if (e.getID() == JVGMouseEvent.MOUSE_PRESSED) {
					JVGMouseEvent me = (JVGMouseEvent) e;
					parent.requestFocus();
					parent.setSelected(!parent.isSelected() || !me.isControlDown(), !me.isControlDown());
				}
			}
		}
	}
}
