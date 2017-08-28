package ru.nest.jvg.editor.editoraction;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.ImageIcon;
import javax.swing.event.UndoableEditEvent;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.actionarea.ActionsFilter;
import ru.nest.jvg.actionarea.JVGActionArea;
import ru.nest.jvg.actionarea.JVGCoordinateActionArea;
import ru.nest.jvg.actionarea.JVGPathGeomActionArea;
import ru.nest.jvg.editor.DisableActionsFilter;
import ru.nest.jvg.editor.JVGEditPane;
import ru.nest.jvg.editor.ShapeCreator;
import ru.nest.jvg.editor.SingleActionsFilter;
import ru.nest.jvg.shape.JVGComplexShape;
import ru.nest.jvg.shape.JVGImage;
import ru.nest.jvg.shape.JVGPath;
import ru.nest.jvg.shape.JVGShape;
import ru.nest.jvg.shape.JVGStyledText;
import ru.nest.jvg.undoredo.AddUndoRedo;

public class AddShapeEditorAction extends EditorAction {
	private final static Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon(AddShapeEditorAction.class.getResource("/ru/nest/jvg/actionarea/cursors/cursor_insert.png")).getImage(), new Point(15, 15), "pencil");

	public AddShapeEditorAction(String name, ShapeCreator creator) {
		super(name);
		this.creator = creator;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGPane pane = getPane(e);
		if (pane instanceof JVGEditPane) {
			JVGEditPane editorPane = (JVGEditPane) pane;
			setEditorPane(editorPane);

			editorPane.setEditor(this);
			consumeMouseEvents(true);
		}
	}

	private ShapeCreator creator;

	private ActionsFilter complexShapeActionsFilter = new SingleActionsFilter(JVGCoordinateActionArea.class);

	private ActionsFilter pathActionsFilter = new SingleActionsFilter(JVGPathGeomActionArea.class);

	private ActionsFilter disableActionsFilter = DisableActionsFilter.getInstance();

	private boolean started = false;

	private JVGShape shape = null;

	@Override
	public void mousePressed(MouseEvent e, double x, double y, double adjustedX, double adjustedY) {
		JVGEditPane editorPane = getEditorPane();

		started = false;
		if (x != adjustedX || y != adjustedY) {
			x = adjustedX;
			y = adjustedY;

			// TODO doesn't work (infinite cycle)
			//			e.consume();
			//			editorPane.adjustCursor(e);
			//			return;
		}
		started = true;

		consumeMouseEvents(false);
		try {
			shape = creator.create(editorPane);
			if (shape != null) {
				// edit path mode
				if (shape instanceof JVGPath) {
					JVGComponent focusOwner = getComponent(null);
					if (focusOwner instanceof JVGPath) {
						JVGPath path = (JVGPath) focusOwner;

						double[] point = { adjustedX, adjustedY };
						path.getInverseTransform().transform(point, 0, point, 0, 1);

						path.moveTo(point[0], point[1]);

						EditorAction mouseAction = new BuildPathEditorAction(path);
						mouseAction.setEditorPane(editorPane);
						editorPane.setEditor(mouseAction);
						return;
					} else {
						consumeMouseEvents(true);
						editorPane.getRobot().mouseRelease(InputEvent.BUTTON1_MASK);
						editorPane.getRobot().mousePress(InputEvent.BUTTON1_MASK);
					}
				}

				JVGActionArea.setActionsFilter(disableActionsFilter);

				// correct shape geometry
				if (shape instanceof JVGPath) {
					JVGPath path = (JVGPath) shape;
					path.moveTo(adjustedX, adjustedY);

					JVGActionArea.setActionsFilter(pathActionsFilter);
				}

				// correct on add point
				Point2D point = null;
				if (shape instanceof JVGComplexShape) {
					point = ((JVGComplexShape) shape).getContext().getPointOnAdd();
					if (point == null) {
						Rectangle2D bounds = shape.getRectangleBounds();
						point = new Point2D.Double(bounds.getX() + bounds.getWidth() / 2, bounds.getY() + bounds.getHeight() / 2);
					}
					JVGActionArea.setActionsFilter(complexShapeActionsFilter);
				}

				// not for complex shapes
				if (point == null) {
					Rectangle2D bounds = shape.getRectangleBounds();
					point = new Point2D.Double(bounds.getX(), bounds.getY());
				}
				editorPane.adjustPoint(point);

				double deltaX = adjustedX - point.getX();
				double deltaY = adjustedY - point.getY();
				shape.transform(AffineTransform.getTranslateInstance(deltaX, deltaY));

				// add shape to root component
				editorPane.getRoot().add(shape);
				editorPane.fireUndoableEditUpdate(new UndoableEditEvent(this, new AddUndoRedo(editorPane, shape, null, -1, editorPane.getRoot(), -1)));

				// validate before process event
				shape.validate();

				editorPane.processNativeMouseEvent(e, x, y, adjustedX, adjustedY);

				// --- post add actions ---
				JVGActionArea.setActionsFilter(null);

				if (shape instanceof JVGStyledText) {
					JVGStyledText text = (JVGStyledText) shape;
					text.setEditMode(true);
					text.moveEnd(false);
				}

				shape.requestFocus();
				shape.setSelected(true, true);
				editorPane.repaint();

				// if there is dialog during adding
				if (shape instanceof JVGImage) {
					JVGActionArea.reset();
					editorPane.setEditor(null);
					editorPane.getRobot().mouseRelease(InputEvent.BUTTON1_MASK);
				}

				// if all ok - consume mouse event
				e.consume();
			}
			editorPane.requestFocus();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e, double x, double y, double adjustedX, double adjustedY) {
		if (started) {
			JVGEditPane editorPane = getEditorPane();
			if (shape instanceof JVGPath) {
				EditorAction mouseAction = new BuildPathEditorAction((JVGPath) shape);
				mouseAction.setEditorPane(editorPane);
				editorPane.setEditor(mouseAction);
				e.consume();
			} else {
				editorPane.setEditor(null);
			}
		}
	}

	@Override
	public void start() {
		JVGEditPane editorPane = getEditorPane();
		editorPane.setCursor(cursor);
		editorPane.getEditor().getEditorActions().enablePathActions();
	}

	@Override
	public void finish() {
		shape = null;
	}

	@Override
	public boolean isCustomActionsManager() {
		return false;
	}
}
