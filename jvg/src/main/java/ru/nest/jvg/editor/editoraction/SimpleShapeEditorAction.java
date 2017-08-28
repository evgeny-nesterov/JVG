package ru.nest.jvg.editor.editoraction;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;

import javax.swing.ImageIcon;
import javax.swing.event.UndoableEditEvent;

import ru.nest.jvg.action.ExclusiveActionAreaAction;
import ru.nest.jvg.editor.JVGEditPane;
import ru.nest.jvg.editor.JVGEditor;
import ru.nest.jvg.geom.MutableGeneralPath;
import ru.nest.jvg.shape.JVGPath;
import ru.nest.jvg.undoredo.AddUndoRedo;

public class SimpleShapeEditorAction extends EditorAction {
	private final static Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon(SimpleShapeEditorAction.class.getResource("/ru/nest/jvg/actionarea/cursors/cursor_small_cross.png")).getImage(), new Point(15, 15), "pencil");

	public SimpleShapeEditorAction(String name, Shape shape, boolean isFill) {
		this(name, shape, isFill, -1);
	}

	public SimpleShapeEditorAction(String name, Shape shape, boolean isFill, int transformType) {
		super(name);
		this.shape = shape;
		this.isFill = isFill;
		this.transformType = transformType;
		consumeMouseEvents(true);
	}

	private Shape shape;

	private boolean isFill;

	private int transformType = -1;

	private MutableGeneralPath path = new MutableGeneralPath();

	private double x, y, ex, ey;

	private boolean started = false;

	@Override
	public void mousePressed(MouseEvent e, double x, double y, double adjustedX, double adjustedY) {
		started = false;
		JVGEditPane editorPane = getEditorPane();
		if (x != adjustedX || y != adjustedY) {
			x = adjustedX;
			y = adjustedY;
		}
		started = true;

		this.x = ex = adjustedX;
		this.y = ey = adjustedY;

		Rectangle2D bounds = shape.getBounds2D();
		path.transform(AffineTransform.getTranslateInstance(adjustedX - bounds.getX(), adjustedY - bounds.getY()));
		path.transform(AffineTransform.getScaleInstance(0, 0));

		editorPane.repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e, double x, double y, double adjustedX, double adjustedY) {
		if (started) {
			JVGEditPane editorPane = getEditorPane();
			JVGEditor editor = editorPane.getEditor();

			// check on not empty shape
			if (this.x != ex || this.y != ey) {
				JVGPath component = editorPane.getEditorKit().getFactory().createComponent(JVGPath.class, new Object[] { new MutableGeneralPath(path), false });
				component.setFill(isFill);
				if (isFill && editor.getCurrentFillDraw() != null) {
					component.setPainter(editor.getEditorActions().getFillPainter());
				}

				if (editor.getCurrentOutlineDraw() != null) {
					if (!isFill) {
						component.setPainter(editor.getEditorActions().getOutlinePainter());
					} else {
						component.addPainter(editor.getEditorActions().getOutlinePainter());
					}
				}

				// add shape to root component
				editorPane.getRoot().add(component);
				editorPane.fireUndoableEditUpdate(new UndoableEditEvent(this, new AddUndoRedo(editorPane, component, null, -1, editorPane.getRoot(), -1)));

				component.setSelected(true, true);

				if (transformType != -1) {
					new ExclusiveActionAreaAction(transformType, component).doAction();
				}
			}

			started = false;
			path.reset();

			editorPane.repaint();
		}
	}

	private double scaleX = 1;

	private double scaleY = 1;

	@Override
	public void mouseDragged(MouseEvent e, double x, double y, double adjustedX, double adjustedY) {
		ex = adjustedX;
		ey = adjustedY;

		path.reset();
		path.append(shape, false);

		Rectangle2D bounds = shape.getBounds2D();
		if (shape instanceof Line2D) {
			scaleX = (adjustedX - this.x) / bounds.getWidth();
			scaleY = (adjustedY - this.y) / bounds.getHeight();
			if (e.isShiftDown()) {
				if (Math.abs(scaleX) > Math.abs(scaleY)) {
					path.pointCoords[3] = path.pointCoords[1];
					scaleY = 1;
				} else {
					path.pointCoords[2] = path.pointCoords[0];
					scaleX = 1;
				}
			}
		} else {
			if (!e.isControlDown()) {
				scaleX = (adjustedX - this.x) / bounds.getWidth();
			}
			if (!e.isShiftDown()) {
				scaleY = (adjustedY - this.y) / bounds.getHeight();
			}
		}

		path.transform(AffineTransform.getTranslateInstance(-bounds.getX(), -bounds.getY()));
		path.transform(AffineTransform.getScaleInstance(scaleX, scaleY));
		path.transform(AffineTransform.getTranslateInstance(this.x, this.y));

		JVGEditPane editorPane = getEditorPane();
		editorPane.repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e, double x, double y, double adjustedX, double adjustedY) {
	}

	private Stroke stroke = new BasicStroke(1f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1f, new float[] { 3f, 5f }, 0f);

	@Override
	public void paint(Graphics2D g) {
		if (started) {
			AffineTransform transform = getEditorPane().getTransform();
			try {
				g.transform(transform.createInverse());
			} catch (NoninvertibleTransformException e) {
				e.printStackTrace();
			}
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			Shape transformedPath = transform.createTransformedShape(path);
			g.setColor(Color.white);
			g.draw(transformedPath);

			Stroke oldStroke = g.getStroke();
			g.setStroke(stroke);
			g.setColor(Color.black);
			g.draw(transformedPath);
			g.setStroke(oldStroke);

			Rectangle bounds = path.getBounds();
			if (bounds.width != 0 && bounds.width == bounds.height) {
				double x2 = x + ((ex > x) ? 10000 : -10000);
				double y2 = y + ((ey > y) ? 10000 : -10000);
				g.setStroke(stroke);
				g.setColor(Color.black);
				g.draw(transform.createTransformedShape(new Line2D.Double(x, y, x2, y2)));
				g.setStroke(oldStroke);
			}

			double w = 4 / transform.getScaleX();
			double h = 4 / transform.getScaleX();
			g.setColor(Color.black);
			g.fill(transform.createTransformedShape(new Rectangle2D.Double(x - w / 2, y - h / 2, w, h)));
			g.fill(transform.createTransformedShape(new Rectangle2D.Double(ex - w / 2, ey - h / 2, w, h)));
			g.setColor(Color.white);
			g.draw(transform.createTransformedShape(new Rectangle2D.Double(x - w / 2, y - h / 2, w, h)));
			g.draw(transform.createTransformedShape(new Rectangle2D.Double(ex - w / 2, ey - h / 2, w, h)));

			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			g.transform(transform);
		}
	}

	@Override
	public void start() {
		path.reset();
		path.append(shape, false);

		JVGEditPane editorPane = getEditorPane();
		editorPane.setCursor(cursor);
		editorPane.getEditor().getEditorActions().enablePathActions();
	}

	@Override
	public void finish() {
	}

	@Override
	public boolean isCustomActionsManager() {
		return true;
	}
}
