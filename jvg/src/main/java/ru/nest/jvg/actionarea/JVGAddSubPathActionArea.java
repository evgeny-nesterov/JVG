package ru.nest.jvg.actionarea;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Robot;
import java.awt.Shape;
import java.awt.event.InputEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import javax.swing.event.UndoableEditEvent;

import ru.nest.jvg.event.JVGFocusEvent;
import ru.nest.jvg.event.JVGMouseAdapter;
import ru.nest.jvg.event.JVGMouseEvent;
import ru.nest.jvg.shape.JVGGroupPath;
import ru.nest.jvg.shape.JVGGroupPath.PathPoint;
import ru.nest.jvg.shape.JVGSubPath;
import ru.nest.jvg.undoredo.AddUndoRedo;
import ru.nest.jvg.undoredo.JVGUndoRedo;

public class JVGAddSubPathActionArea extends JVGActionArea {
	private PathPoint point;

	public JVGAddSubPathActionArea() {
		super(VISIBILITY_TYPE_FOCUSED);
		setName("add-sub-path");
		setActive(false);

		addMouseListener(new JVGMouseAdapter() {
			@Override
			public void mousePressed(JVGMouseEvent e) {
				if (e.getID() == JVGMouseEvent.MOUSE_PRESSED && e.getButton() == JVGMouseEvent.BUTTON1 && e.getClickCount() == 1) {
					if (point != null) {
						JVGGroupPath path = (JVGGroupPath) getParent();
						JVGSubPath subPath = path.add(point.percentPos);
						subPath.setSelected(true, true);
						subPath.setFocused(true);
						path.setEditMode(JVGGroupPath.EDIT_MODE__MOVE_SUBPATH);
						subPath.invalidate();
						invalidate();
						path.validate();
						try {
							Robot robot = new Robot();
							robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
							robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
						} catch (AWTException exc) {
							exc.printStackTrace();
						}
						repaint();

						JVGUndoRedo edit = new AddUndoRedo(pane, subPath, null, -1, path, path.getChildIndex(subPath));
						pane.fireUndoableEditUpdate(new UndoableEditEvent(pane, edit));
					}
				}
			}

			@Override
			public void mouseMoved(JVGMouseEvent e) {
				JVGGroupPath path = (JVGGroupPath) getParent();

				Point2D p = e.getPoint();
				path.getInverseTransform().transform(p, p);

				point = path.getClosestPoint(p.getX(), p.getY());
				if (point != null && point.dist > 15) {
					point = null;
				}
				repaint();
			}
		});
	}

	@Override
	public void focusGained(JVGFocusEvent e) {
		point = null;
		super.focusGained(e);
	}

	@Override
	public void focusLost(JVGFocusEvent e) {
		JVGGroupPath path = (JVGGroupPath) getParent();
		if (path.getEditMode() == JVGGroupPath.EDIT_MODE__ADD_SUBPATH) {
			path.setEditMode(JVGGroupPath.EDIT_MODE__NONE);
		}
		super.focusLost(e);
	}

	@Override
	public boolean contains(double x, double y) {
		if (!super.contains(x, y)) {
			return false;
		}
		JVGGroupPath path = (JVGGroupPath) getParent();
		return path.getEditMode() == JVGGroupPath.EDIT_MODE__ADD_SUBPATH && path.getTransformedShape().contains(x, y);
	}

	@Override
	public void paintAction(Graphics2D g, AffineTransform transform) {
		if (point != null) {
			JVGGroupPath path = (JVGGroupPath) getParent();
			Point2D p = new Point2D.Double(point.point.getX(), point.point.getY());
			path.getTransform().transform(p, p);
			transform.transform(p, p);

			int x = (int) p.getX();
			int y = (int) p.getY();
			g.setXORMode(Color.black);
			g.setColor(Color.white);
			g.drawLine(x - 5, y - 5, x + 5, y + 5);
			g.drawLine(x + 5, y - 5, x - 5, y + 5);
			g.setPaintMode();
		}
	}

	@Override
	public Cursor getCursor() {
		return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
	}

	@Override
	protected boolean isDrawAction() {
		if (!super.isDrawAction()) {
			return false;
		}
		JVGGroupPath path = (JVGGroupPath) getParent();
		return path.getEditMode() == JVGGroupPath.EDIT_MODE__ADD_SUBPATH;
	}

	@Override
	protected Shape computeActionBounds() {
		return null;
	}
}
