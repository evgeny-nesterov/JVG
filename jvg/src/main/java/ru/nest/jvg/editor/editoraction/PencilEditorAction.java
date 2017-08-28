package ru.nest.jvg.editor.editoraction;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

import javax.swing.ImageIcon;
import javax.swing.event.UndoableEditEvent;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.editor.JVGEditPane;
import ru.nest.jvg.event.JVGInputEvent;
import ru.nest.jvg.event.JVGKeyEvent;
import ru.nest.jvg.geom.CoordinablePath;
import ru.nest.jvg.geom.MutableGeneralPath;
import ru.nest.jvg.shape.JVGPath;
import ru.nest.jvg.shape.paint.Draw;
import ru.nest.jvg.shape.paint.Painter;
import ru.nest.jvg.undoredo.AddUndoRedo;
import ru.nest.jvg.undoredo.ShapeChangedUndoRedo;

public class PencilEditorAction extends EditorAction {
	// public final static int APPEND_AS_IS = 0;
	// public final static int APPEND_AVERAGE_OUT = 1;
	// public final static int APPEND_SPLINE = 2;

	private final static Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon(PencilEditorAction.class.getResource("/ru/nest/jvg/actionarea/cursors/cursor_pencil.png")).getImage(), new Point(15, 15), "pencil");

	public PencilEditorAction(boolean adjusted) {
		super("pencil");
		this.adjusted = adjusted;
		consumeMouseEvents(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);

		JVGComponent component = getComponent(e);
		if (component instanceof JVGPath) {
			shape = (JVGPath) component;
		}
	}

	private boolean adjusted;

	private JVGPath shape = null;

	private MutableGeneralPath path;

	private double x, y;

	@Override
	public void mousePressed(MouseEvent e, double x, double y, double adjustedX, double adjustedY) {
		JVGEditPane editorPane = getEditorPane();
		if (adjusted) {
			this.x = adjustedX;
			this.y = adjustedY;
		} else {
			this.x = x;
			this.y = y;
		}

		if (path.numCoords == 0 || this.x != path.pointCoords[path.numCoords - 2] || this.y != path.pointCoords[path.numCoords - 1]) {
			path.moveTo(this.x, this.y);
			System.out.println("moveTo: " + this.x + ", " + this.y);
			editorPane.repaint();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e, double x, double y, double adjustedX, double adjustedY) {
		if (adjusted) {
			this.x = adjustedX;
			this.y = adjustedY;
		} else {
			this.x = x;
			this.y = y;
		}
	}

	@Override
	public void mouseDragged(MouseEvent e, double x, double y, double adjustedX, double adjustedY) {
		JVGEditPane editorPane = getEditorPane();
		if (adjusted) {
			this.x = adjustedX;
			this.y = adjustedY;
			if (path.numCoords == 0 || this.x != path.pointCoords[path.numCoords - 2] || this.y != path.pointCoords[path.numCoords - 1]) {
				path.lineTo(this.x, this.y);
				// System.out.println("lineTo: " + this.x + ", " + this.y + ", " + path.numTypes);
			}
		} else {
			this.x = x;
			this.y = y;

			// average out last point
			if (path.numTypes > 1) {
				int lastType = path.pointTypes[path.numTypes - 1];
				if (lastType != PathIterator.SEG_MOVETO) {
					path.pointCoords[path.numCoords - 2] = (path.pointCoords[path.numCoords - 4] + 2 * path.pointCoords[path.numCoords - 2] + x) / 4.0f;
					path.pointCoords[path.numCoords - 1] = (path.pointCoords[path.numCoords - 3] + 2 * path.pointCoords[path.numCoords - 1] + y) / 4.0f;
				}
			}
			if (path.numCoords == 0 || this.x != path.pointCoords[path.numCoords - 2] || this.y != path.pointCoords[path.numCoords - 1]) {
				path.lineTo(this.x, this.y);
			}

			// // append new point
			// if (path.numTypes > 0)
			// {
			// int lastType = path.pointTypes[path.numTypes - 1];
			// if (lastType == PathIterator.SEG_MOVETO)
			// {
			// path.lineTo(x, y);
			// }
			// else if (path.numTypes > 1)
			// {
			// int lastType2 = path.pointTypes[path.numTypes - 2];
			// if (lastType2 == PathIterator.SEG_MOVETO && lastType ==
			// PathIterator.SEG_LINETO)
			// {
			// double lastX = path.pointCoords[path.numCoords - 2];
			// double lastY = path.pointCoords[path.numCoords - 1];
			//
			// path.deleteLast();
			// path.quadTo(lastX, lastY,  x,  y);
			// }
			// else
			// {
			// // r1, r2, r3
			// // r = r2 - alfa * (r1 - r2)
			// // |r - r2| = |r - r3|
			// // (r2 - r3)^2 - 2 * alfa * (r2 - r3) * (r1 - r2) = 0
			// // alfa = (r2 - r3)^2 / (2 * (r1 - r2) * (r2 - r3))
			// double x_12 = path.pointCoords[path.numCoords - 4] -
			// path.pointCoords[path.numCoords - 2];
			// double y_12 = path.pointCoords[path.numCoords - 3] -
			// path.pointCoords[path.numCoords - 1];
			// double x_23 = path.pointCoords[path.numCoords - 2] - x;
			// double y_23 = path.pointCoords[path.numCoords - 1] - y;
			// double alfa = (x_23 * x_23 + y_23 * y_23) / (2 * (x_12 * x_23 +
			// y_12 * y_23));
			// double cx =  (path.pointCoords[path.numCoords - 2] - alfa *
			// x_12);
			// double cy =  (path.pointCoords[path.numCoords - 1] - alfa *
			// y_12);
			// path.quadTo(cx, cy,  x,  y);
			//
			// System.out.println(path.pointCoords[path.numCoords - 4] + "," +
			// path.pointCoords[path.numCoords - 3] + "; " +
			// path.pointCoords[path.numCoords - 2] + "," +
			// path.pointCoords[path.numCoords - 1] + ";    " +
			// cx + "," + cy + "; " + x + "," + y);
			// }
			// }
			// }
		}
		editorPane.repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e, double x, double y, double adjustedX, double adjustedY) {
		if (adjusted) {
			this.x = adjustedX;
			this.y = adjustedY;
		} else {
			this.x = x;
			this.y = y;
		}
	}

	private boolean pressed = false;

	@Override
	public void processKeyEvent(KeyEvent e) {
		super.processKeyEvent(e);
		if (e.getID() == KeyEvent.KEY_PRESSED) {
			JVGEditPane editorPane = getEditorPane();
			double incrementx = adjusted || e.isShiftDown() ? editorPane.getIncrement() * editorPane.getScaleX() : 1;
			double incrementy = adjusted || e.isShiftDown() ? editorPane.getIncrement() * editorPane.getScaleY() : 1;

			Point2D point = new Point2D.Double(this.x, this.y);
			editorPane.getTransform().transform(point, point);
			double x = point.getX();
			double y = point.getY();

			switch (e.getKeyCode()) {
				case KeyEvent.VK_CONTROL:
					if (!pressed) {
						editorPane.mousePress(JVGInputEvent.BUTTON1_MASK);
						pressed = true;
					}
					break;

				// move point
				case KeyEvent.VK_UP:
					editorPane.moveMouse((int) x, (int) (y - incrementy));
					break;

				case KeyEvent.VK_LEFT:
					editorPane.moveMouse((int) (x - incrementx), (int) y);
					break;

				case KeyEvent.VK_DOWN:
					editorPane.moveMouse((int) x, (int) (y + incrementy));
					break;

				case KeyEvent.VK_RIGHT:
					editorPane.moveMouse((int) (x + incrementx), (int) y);
					break;

				case KeyEvent.VK_Z:
					if (e.isControlDown()) {
						path.deleteLast();
						if (path.numTypes > 0 && path.pointTypes[path.numTypes - 1] == PathIterator.SEG_MOVETO) {
							path.deleteLast();
						}
						if (path.numCoords > 0) {
							this.x = path.pointCoords[path.numCoords - 2];
							this.y = path.pointCoords[path.numCoords - 1];
						}
						editorPane.repaint();
					}
					break;

				// exit from edit mode
				case KeyEvent.VK_ESCAPE:
					editorPane.setEditor(null);
					break;
			}
		} else if (e.getID() == JVGKeyEvent.KEY_RELEASED) {
			JVGEditPane editorPane = getEditorPane();
			if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
				if (pressed) {
					editorPane.mouseRelease(JVGInputEvent.BUTTON1_MASK);
					pressed = false;
				}
			}
		}
		e.consume();
	}

	@Override
	public void paint(Graphics2D g) {
		if (path != null) {
			Stroke oldStroke = g.getStroke();
			Stroke stroke = getEditorPane().getEditor().getEditorActions().getStroke();
			Draw<?> draw = getEditorPane().getEditor().getCurrentOutlineDraw();

			g.setStroke(stroke);
			g.setPaint(draw.getPaint(null, path, null));
			g.draw(path);
			g.setStroke(oldStroke);
		}
	}

	@Override
	public void start() {
		JVGEditPane editorPane = getEditorPane();
		editorPane.setCursor(cursor);
		path = new MutableGeneralPath();
		editorPane.getEditor().getEditorActions().enablePathActions();
	}

	@Override
	public void finish() {
		JVGEditPane editorPane = getEditorPane();
		if (shape == null) {
			JVGPath component = editorPane.getEditorKit().getFactory().createComponent(JVGPath.class, new Object[] { path, false });
			Painter painter = getEditorPane().getEditor().getEditorActions().getOutlinePainter();
			component.setPainter(painter);

			// add shape to root component
			editorPane.getRoot().add(component);
			editorPane.fireUndoableEditUpdate(new UndoableEditEvent(this, new AddUndoRedo(editorPane, component, null, -1, editorPane.getRoot(), -1)));

			component.setSelected(true, true);
		} else {
			path.transform(shape.getInverseTransform());

			Shape oldShape = shape.getShape();
			MutableGeneralPath newPath = new MutableGeneralPath(oldShape);
			newPath.append(path, false);

			shape.setShape(newPath, oldShape instanceof CoordinablePath);
			editorPane.fireUndoableEditUpdate(new UndoableEditEvent(shape, new ShapeChangedUndoRedo(getName(), editorPane, shape, oldShape, newPath)));

			shape.setSelected(true, true);
		}

		editorPane.repaint();
		path = null;
		shape = null;
	}

	@Override
	public boolean isCustomActionsManager() {
		return true;
	}
}
