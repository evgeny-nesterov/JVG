package ru.nest.jvg.actionarea;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Shape;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.event.JVGMouseAdapter;
import ru.nest.jvg.event.JVGMouseEvent;
import ru.nest.jvg.geom.MutableGeneralPath;
import ru.nest.jvg.shape.JVGStyledText;

public class JVGParagraphIndentActionArea extends JVGActionArea {
	public final static int LEFT = 0;

	public final static int RIGHT = 1;

	private int type;

	public JVGParagraphIndentActionArea(int type) {
		this.type = type;

		addMouseListener(new JVGMouseAdapter() {
			@Override
			public void mousePressed(JVGMouseEvent e) {
			}

			@Override
			public void mouseReleased(JVGMouseEvent e) {
			}

			@Override
			public void mouseDragged(JVGMouseEvent e) {
				JVGComponent p = getParent();
				if (p instanceof JVGStyledText) {
					JVGStyledText parent = (JVGStyledText) p;
					if (parent.isWrap()) {
						double[] point = { e.getX(), e.getY() };
						parent.getInverseTransform().transform(point, 0, point, 0, 1);
						double x = point[0];
						double y = point[1];

						parent.setWrapSize(x);
						parent.invalidate();
						parent.repaint();
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
			// JVGText parent = (JVGText) p;
			// Shape pb = parent.getInitialBounds();
			// 
			// Rectangle2D b = pb instanceof Rectangle2D ? (Rectangle2D) pb :
			// pb.getBounds2D();
			//
			// double[] points = {b.getX(), b.getY(), b.getX() + b.getWidth(),
			// b.getY(), b.getX(), b.getY() - 1};
			// parent.getTransform().transform(points, 0, points, 0, 3);

			MutableGeneralPath path = new MutableGeneralPath();
			// path.moveTo();
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
