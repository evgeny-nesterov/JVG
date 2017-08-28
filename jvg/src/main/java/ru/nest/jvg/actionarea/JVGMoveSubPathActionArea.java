package ru.nest.jvg.actionarea;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.event.UndoableEditEvent;

import ru.nest.jvg.JVGPane;
import ru.nest.jvg.event.JVGMouseAdapter;
import ru.nest.jvg.event.JVGMouseEvent;
import ru.nest.jvg.geom.MutableGeneralPath;
import ru.nest.jvg.shape.JVGGroupPath;
import ru.nest.jvg.shape.JVGGroupPath.PathPoint;
import ru.nest.jvg.shape.JVGSubPath;
import ru.nest.jvg.undoredo.JVGUndoRedo;
import ru.nest.jvg.undoredo.PropertyUndoRedo;

public class JVGMoveSubPathActionArea extends JVGActionArea {
	private boolean pressed;

	private PathPoint point;

	private double oldPosition;

	private double newPosition;

	public JVGMoveSubPathActionArea() {
		super(VISIBILITY_TYPE_FOCUSED);
		setName("move-sub-path");
		setActive(false);

		addMouseListener(new JVGMouseAdapter() {
			@Override
			public void mousePressed(JVGMouseEvent e) {
				if (e.getButton() == JVGMouseEvent.BUTTON1 && e.getClickCount() == 1) {
					pressed = true;
					JVGSubPath subpath = (JVGSubPath) getParent();
					oldPosition = newPosition = subpath.getPosition();
				}
			}

			@Override
			public void mouseReleased(JVGMouseEvent e) {
				if (pressed) {
					if (oldPosition != newPosition && point != null && point.dist < 50) {
						JVGPane pane = getPane();
						JVGSubPath subpath = (JVGSubPath) getParent();
						JVGUndoRedo edit = new PropertyUndoRedo(getName(), pane, subpath, "setPosition", double.class, oldPosition, newPosition);
						pane.fireUndoableEditUpdate(new UndoableEditEvent(pane, edit));
					}
					pressed = false;
					point = null;
				}
			}

			@Override
			public void mouseDragged(JVGMouseEvent e) {
				if (pressed) {
					JVGSubPath subpath = (JVGSubPath) getParent();
					JVGGroupPath path = (JVGGroupPath) subpath.getParent();

					Point2D p = e.getPoint();
					path.getInverseTransform().transform(p, p);

					point = path.getClosestPoint(p.getX(), p.getY());

					if (point != null && point.dist < 50) {
						newPosition = point.percentPos;
						subpath.setPosition(newPosition);
						//invalidate();
						repaint();
					}
				}
			}
		});
	}

	@Override
	public boolean contains(double x, double y) {
		if (!super.contains(x, y)) {
			return false;
		}
		JVGSubPath subpath = (JVGSubPath) getParent();
		JVGGroupPath path = (JVGGroupPath) subpath.getParent();
		return path.getEditMode() == JVGGroupPath.EDIT_MODE__MOVE_SUBPATH;
	}

	@Override
	protected Shape computeActionBounds() {
		JVGSubPath parent = (JVGSubPath) getParent();
		if (parent != null) {
			MutableGeneralPath path = parent.getPath();
			if (path.numCoords >= 2) {
				double x = path.pointCoords[path.numCoords - 2];
				double y = path.pointCoords[path.numCoords - 1];

				double[] point = new double[] { x, y };
				AffineTransform transform = parent.getTransform();
				transform.transform(point, 0, point, 0, 1);
				x = point[0];
				y = point[1];
				return new Rectangle2D.Double(x - 2.5, y - 2.5, 5, 5);
			}
		}
		return null;
	}

	@Override
	protected Color getColor() {
		return Color.orange;
	}

	@Override
	protected Color getBorderColor() {
		return Color.red;
	}

	@Override
	public Cursor getCursor() {
		return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
	}

	@Override
	protected boolean isDrawAction() {
		if (!(isActive() && (getActionsFilter() == null || getActionsFilter().pass(this)))) {
			return false;
		}
		JVGSubPath subpath = (JVGSubPath) getParent();
		JVGGroupPath path = (JVGGroupPath) subpath.getParent();
		return path.getEditMode() == JVGGroupPath.EDIT_MODE__MOVE_SUBPATH;
	}
}
