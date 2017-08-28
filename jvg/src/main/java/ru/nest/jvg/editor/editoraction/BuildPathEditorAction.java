package ru.nest.jvg.editor.editoraction;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.action.PathAddCurveAction;
import ru.nest.jvg.editor.JVGEditPane;
import ru.nest.jvg.editor.JVGEditor;
import ru.nest.jvg.event.JVGMouseEvent;
import ru.nest.jvg.geom.coord.Coordinable;
import ru.nest.jvg.geom.coord.Coordinate;
import ru.nest.jvg.shape.JVGPath;

public class BuildPathEditorAction extends EditorAction {
	private final static Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon(BuildPathEditorAction.class.getResource("/ru/nest/jvg/actionarea/cursors/cursor_edit_path.png")).getImage(), new Point(12, 15), "pencil");

	public BuildPathEditorAction() {
		this(null);
	}

	public BuildPathEditorAction(JVGPath path) {
		super("build-path");
		this.path = path;
		consumeMouseEvents(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);

		JVGComponent component = getComponent(e);
		if (component instanceof JVGPath) {
			path = (JVGPath) component;
		}
	}

	private JVGPath path;

	private double x, y;

	private List<Point2D> middlePoints = new ArrayList<Point2D>();

	private boolean isPressed = false;

	@Override
	public void mousePressed(MouseEvent e, double x, double y, double adjustedX, double adjustedY) {
		if (e.isControlDown()) {
			this.x = x;
			this.y = y;
		} else {
			this.x = adjustedX;
			this.y = adjustedY;
		}

		if (e.getButton() == MouseEvent.BUTTON1) {
			isPressed = true;
			path.setFocused(false);
			path.setSelected(true, false);
			insert(this.x, this.y);
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			JVGEditPane editorPane = getEditorPane();
			JVGEditor editor = editorPane.getEditor();
			int appendType = editor.getPathAppendType();

			Point2D point = new Point2D.Double(this.x, this.y);
			//path.getInverseTransform().transform(point, point);
			middlePoints.add(point);

			if (appendType == PathIterator.SEG_QUADTO) {
				if (middlePoints.size() > 1) {
					middlePoints.remove(0);
				}
			} else if (appendType == PathIterator.SEG_CUBICTO) {
				if (middlePoints.size() > 2) {
					middlePoints.remove(0);
				}
			} else {
				middlePoints.clear();
			}
			editorPane.repaint();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e, double x, double y, double adjustedX, double adjustedY) {
		isPressed = false;
		action = null;
	}

	private PathAddCurveAction action;

	private void insert(double adjustedX, double adjustedY) {
		JVGEditPane editorPane = getEditorPane();

		double[] point = { adjustedX, adjustedY };
		path.getInverseTransform().transform(point, 0, point, 0, 1);

		double lastX = path.getCoord(path.getNumCoords() - 2);
		double lastY = path.getCoord(path.getNumCoords() - 1);
		if (lastX == point[0] && lastY == point[1]) {
			return;
		}

		Coordinate[] coords = new Coordinate[] { new Coordinate(point[0]), new Coordinate(point[1]) };

		JVGEditor editor = editorPane.getEditor();
		editorPane.getEditor().getEditorActions().enablePathActions();
		int appendType = editor.getPathAppendType();
		if (appendType == PathIterator.SEG_QUADTO) {
			if (middlePoints.size() == 1) {
				Point2D p = middlePoints.get(0);
				coords = new Coordinate[] { new Coordinate(p.getX()), new Coordinate(p.getY()), new Coordinate(point[0]), new Coordinate(point[1]) };
			}
		} else if (appendType == PathIterator.SEG_CUBICTO) {
			if (middlePoints.size() == 2) {
				Point2D p1 = middlePoints.get(0);
				Point2D p2 = middlePoints.get(1);
				coords = new Coordinate[] { new Coordinate(p1.getX()), new Coordinate(p1.getY()), new Coordinate(p2.getX()), new Coordinate(p2.getY()), new Coordinate(point[0]), new Coordinate(point[1]) };
			}
		} else {
			coords = new Coordinate[] { new Coordinate(point[0]), new Coordinate(point[1]) };
		}
		middlePoints.clear();

		action = new PathAddCurveAction(path, appendType, coords);
		action.doAction();
	}

	@Override
	public void processKeyEvent(KeyEvent e) {
		super.processKeyEvent(e);

		if (e.getID() == KeyEvent.KEY_PRESSED) {
			JVGEditPane editorPane = getEditorPane();
			double incrementx = !e.isControlDown() ? editorPane.getIncrement() * editorPane.getScaleX() : 1;
			double incrementy = !e.isControlDown() ? editorPane.getIncrement() * editorPane.getScaleY() : 1;

			double[] point = new double[] { this.x, this.y };
			editorPane.getTransform().transform(point, 0, point, 0, 1);
			double x = point[0];
			double y = point[1];

			switch (e.getKeyCode()) {
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

				// lineto
				case KeyEvent.VK_SPACE:
				case KeyEvent.VK_L:
					if (!isPressed) {
						editorPane.getEditor().setPathAppendType(PathIterator.SEG_LINETO);
						editorPane.getRobot().mousePress(InputEvent.BUTTON1_MASK);
						editorPane.getRobot().mouseRelease(InputEvent.BUTTON1_MASK);
					}
					break;

				case KeyEvent.VK_Q:
					if (!isPressed) {
						editorPane.getEditor().setPathAppendType(PathIterator.SEG_QUADTO);
						editorPane.getRobot().mousePress(InputEvent.BUTTON1_MASK);
						editorPane.getRobot().mouseRelease(InputEvent.BUTTON1_MASK);
					}
					break;

				case KeyEvent.VK_C:
					if (!isPressed) {
						editorPane.getEditor().setPathAppendType(PathIterator.SEG_CUBICTO);
						editorPane.getRobot().mousePress(InputEvent.BUTTON1_MASK);
						editorPane.getRobot().mouseRelease(InputEvent.BUTTON1_MASK);
					}
					break;

				// moveto
				case KeyEvent.VK_M:
					if (!isPressed) {
						editorPane.getEditor().setPathAppendType(PathIterator.SEG_MOVETO);
						editorPane.getRobot().mousePress(InputEvent.BUTTON1_MASK);
						editorPane.getRobot().mouseRelease(InputEvent.BUTTON1_MASK);
					}
					break;

				case KeyEvent.VK_P:
					if (!isPressed) {
						editorPane.getRobot().mousePress(InputEvent.BUTTON3_MASK);
						editorPane.getRobot().mouseRelease(InputEvent.BUTTON3_MASK);
					}
					break;

				// close path
				case KeyEvent.VK_X:
					new PathAddCurveAction(path, PathIterator.SEG_CLOSE, null, null).doAction();
					break;

				// exit from edit mode
				case KeyEvent.VK_ESCAPE:
					editorPane.setEditor(null);
					break;

				case KeyEvent.VK_Z:
					if (e.isControlDown() && path.getPath().numTypes > 1) {
						return;
					}
					break;

				case KeyEvent.VK_V:
					if (e.isControlDown()) {
						return;
					}
					break;
			}
		}
		e.consume();
	}

	private boolean newPointInitiated = false;

	@Override
	public void mouseMoved(MouseEvent e, double x, double y, double adjustedX, double adjustedY) {
		if (e.isControlDown()) {
			this.x = x;
			this.y = y;
		} else {
			this.x = adjustedX;
			this.y = adjustedY;
		}
		newPointInitiated = true;
		path.repaint();
	}

	@Override
	public void mouseDragged(MouseEvent e, double x, double y, double adjustedX, double adjustedY) {
		if (isPressed) {
			if (e.isControlDown()) {
				this.x = x;
				this.y = y;
			} else {
				this.x = adjustedX;
				this.y = adjustedY;
			}
			newPointInitiated = false;

			// Transform point to original coordinates.
			double[] point = { adjustedX, adjustedY };
			path.getInverseTransform().transform(point, 0, point, 0, 1);

			// Update coordinates of last point for undo / redo action.
			// Coords may be equals to null if move action is performed one after
			// another.
			if (action != null) {
				Coordinable[] coords = action.getCoord();
				if (coords != null) {
					coords[coords.length - 2].setCoord(point[0]);
					coords[coords.length - 1].setCoord(point[1]);
				}
			}

			// Update coordinates of last path point.
			int numCoords = path.getNumCoords();
			path.setPoint(numCoords - 2, point[0], point[1]);
			path.repaint();
		}
	}

	private Point2D lastPoint = new Point2D.Double();

	private Stroke stroke = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f, new float[] { 1f, 1f }, 0);

	@Override
	public void paint(Graphics2D g) {
		if (newPointInitiated) {
			AffineTransform transform = getEditorPane().getTransform();
			try {
				g.transform(transform.createInverse());
			} catch (NoninvertibleTransformException e) {
				e.printStackTrace();
			}
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

			path.getLastPoint(lastPoint);
			path.getTransform().transform(lastPoint, lastPoint);

			Stroke oldStroke = g.getStroke();
			g.setColor(Color.lightGray);

			g.draw(transform.createTransformedShape(new Line2D.Double(-10000, y, 10000, y)));
			g.draw(transform.createTransformedShape(new Line2D.Double(x, -10000, x, 10000)));
			g.draw(transform.createTransformedShape(new Line2D.Double(x - 10000, y - 10000, x + 10000, y + 10000)));
			g.draw(transform.createTransformedShape(new Line2D.Double(x - 10000, y + 10000, x + 10000, y - 10000)));

			// draw line to new point
			Shape l = transform.createTransformedShape(new Line2D.Double(lastPoint.getX(), lastPoint.getY(), x, y));
			g.setColor(Color.white);
			g.draw(l);
			g.setStroke(stroke);
			g.setColor(Color.darkGray);
			g.draw(l);

			g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			g.setPaintMode();
			g.setStroke(oldStroke);

			g.transform(transform);

			g.setColor(Color.black);
			for (Point2D p : middlePoints) {
				g.fillRect((int) p.getX() - 1, (int) p.getY() - 1, 3, 3);
			}
		}
	}

	@Override
	public void start() {
		JVGEditPane editorPane = getEditorPane();
		editorPane.setCursor(cursor);

		JVGEditor editor = editorPane.getEditor();
		editorPane.getEditor().getEditorActions().enablePathActions();
		int appendType = editor.getPathAppendType();
		if (appendType == PathIterator.SEG_MOVETO) {
			editor.setPathAppendType(PathIterator.SEG_LINETO);
		}
	}

	@Override
	public void finish() {
		JVGEditPane editorPane = getEditorPane();
		editorPane.repaint();

		path.getRoot().dispatchEvent(new JVGMouseEvent(null, path.getRoot(), JVGMouseEvent.MOUSE_RELEASED, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 0, 0));
		//path.getRoot().dispatchEvent(new JVGMouseEvent(null, path.getRoot(), JVGMouseEvent.MOUSE_MOVED, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 0, 0));
	}

	@Override
	public boolean isCustomActionsManager() {
		return false;
	}
}
