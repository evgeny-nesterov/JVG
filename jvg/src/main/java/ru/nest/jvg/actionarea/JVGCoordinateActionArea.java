package ru.nest.jvg.actionarea;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import javax.swing.event.UndoableEditEvent;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.event.JVGComponentEvent;
import ru.nest.jvg.event.JVGMouseAdapter;
import ru.nest.jvg.event.JVGMouseEvent;
import ru.nest.jvg.geom.coord.Coordinable;
import ru.nest.jvg.shape.JVGShape;
import ru.nest.jvg.undoredo.PathGeomUndoRedo;

public class JVGCoordinateActionArea extends JVGActionArea {
	private Coordinable x;

	private Coordinable y;

	public JVGCoordinateActionArea(Coordinable x, Coordinable y) {
		super(VISIBILITY_TYPE_FOCUSED);

		this.x = x;
		this.y = y;

		addMouseListener(new JVGMouseAdapter() {
			@Override
			public void mousePressed(JVGMouseEvent e) {
				newX = oldX = JVGCoordinateActionArea.this.x.getCoord();
				newY = oldY = JVGCoordinateActionArea.this.y.getCoord();
			}

			@Override
			public void mouseReleased(JVGMouseEvent e) {
				if (oldX != newX || oldY != newY) {
					JVGComponent p = getParent();
					if (p instanceof JVGShape) {
						JVGShape parent = (JVGShape) p;
						fireUndoableEditUpdate(new UndoableEditEvent(parent, new PathGeomUndoRedo(getPane(), parent, JVGCoordinateActionArea.this.x, JVGCoordinateActionArea.this.y, oldX, oldY, newX, newY)));
					}
				}
			}

			@Override
			public void mouseDragged(JVGMouseEvent e) {
				setCoord(e.getAdjustedX(), e.getAdjustedY());
				invalidate();
				repaint();
			}
		});
	}

	private double oldX, oldY, newX, newY;

	public void setCoord(double x, double y) {
		JVGComponent p = getParent();
		if (p instanceof JVGShape) {
			JVGShape parent = (JVGShape) p;

			double[] point = { x, y };
			parent.getInverseTransform().transform(point, 0, point, 0, 1);
			newX = point[0];
			newY = point[1];

			this.x.setCoord(newX);
			this.y.setCoord(newY);

			JVGComponentEvent event = new JVGComponentEvent(parent, JVGComponentEvent.COMPONENT_GEOMETRY_CHANGED);
			parent.dispatchEvent(event);
		}
	}

	@Override
	public Shape computeActionBounds() {
		JVGComponent p = getParent();
		if (p instanceof JVGShape) {
			JVGShape parent = (JVGShape) p;
			double[] point = { x.getCoord(), y.getCoord() };
			parent.getTransform().transform(point, 0, point, 0, 1);
			return new Rectangle2D.Double(point[0] - 2, point[1] - 2, 4, 4);
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
		return Color.red;
	}

	@Override
	public Cursor getCursor() {
		return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
	}
}
