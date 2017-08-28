package ru.nest.jvg.actionarea;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import javax.swing.event.UndoableEditEvent;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.event.JVGMouseAdapter;
import ru.nest.jvg.event.JVGMouseEvent;
import ru.nest.jvg.geom.MutableGeneralPath;
import ru.nest.jvg.shape.JVGStyledText;
import ru.nest.jvg.undoredo.PropertyUndoRedo;

public class JVGTextWrapActionArea extends JVGActionArea {
	private double s1;

	private double s2;

	public JVGTextWrapActionArea() {
		addMouseListener(new JVGMouseAdapter() {
			@Override
			public void mousePressed(JVGMouseEvent e) {
				JVGComponent p = getParent();
				if (p instanceof JVGStyledText) {
					JVGStyledText text = (JVGStyledText) p;
					if (text.isWrap()) {
						s1 = s2 = text.getWrapSize();
					}
				}
			}

			@Override
			public void mouseReleased(JVGMouseEvent e) {
				JVGComponent p = getParent();
				if (p instanceof JVGStyledText) {
					JVGStyledText text = (JVGStyledText) p;
					if (text.isWrap()) {
						if (s1 != s2) {
							PropertyUndoRedo edit = new PropertyUndoRedo("text-wrap-size", pane, text, "setWrapSize", s1, s2);
							pane.fireUndoableEditUpdate(new UndoableEditEvent(pane, edit));
						}
					}
				}
			}

			@Override
			public void mouseDragged(JVGMouseEvent e) {
				JVGComponent p = getParent();
				if (p instanceof JVGStyledText) {
					JVGStyledText text = (JVGStyledText) p;
					if (text.isWrap()) {
						double[] point = { e.getX(), e.getY() };
						text.getInverseTransform().transform(point, 0, point, 0, 1);
						double x = point[0];
						double y = point[1];

						s2 = x;
						text.setWrapSize(x);
						text.invalidate();
						text.repaint();
					}
				}
			}
		});
	}

	@Override
	public boolean contains(double x, double y) {
		double scale = getPaneScale();
		double delta = 5 / scale;
		return isActive() && getScaledBounds().intersects(x - delta / 2, y - delta / 2, delta, delta);
	}

	@Override
	public Shape computeActionBounds() {
		JVGComponent p = getParent();
		if (p instanceof JVGStyledText) {
			JVGStyledText parent = (JVGStyledText) p;
			Shape b = parent.getInitialBounds();

			Rectangle2D parentInitialBounds = b instanceof Rectangle2D ? (Rectangle2D) b : b.getBounds2D();
			Rectangle2D initialBounds = new Rectangle2D.Double(parent.getWrapSize(), 0, 0, parentInitialBounds.getHeight());

			MutableGeneralPath path = new MutableGeneralPath(initialBounds);
			path.transform(parent.getTransform());
			return path;
		} else {
			return null;
		}
	}

	@Override
	protected Color getColor() {
		return null;
	}

	@Override
	protected Color getBorderColor() {
		return Color.black;
	}

	@Override
	public boolean isActive() {
		return super.isActive() && ((JVGStyledText) getParent()).isWrap();
	}

	@Override
	public Cursor getCursor() {
		return Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
	}
}
