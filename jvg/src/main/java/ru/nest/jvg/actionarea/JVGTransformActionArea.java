package ru.nest.jvg.actionarea;

import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.swing.event.UndoableEditEvent;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.JVGSelectionModel;
import ru.nest.jvg.JVGUtil;
import ru.nest.jvg.event.JVGMouseAdapter;
import ru.nest.jvg.event.JVGMouseEvent;
import ru.nest.jvg.macros.JVGMacros;
import ru.nest.jvg.shape.JVGShape;
import ru.nest.jvg.undoredo.TransformUndoRedo;

public abstract class JVGTransformActionArea extends JVGActionArea {
	private AffineTransform editTransform;

	public JVGTransformActionArea(boolean applyToSelection) {
		this(VISIBILITY_TYPE_FOCUSED, applyToSelection);
	}

	public JVGTransformActionArea(int visibilityType, final boolean applyToSelection) {
		super(visibilityType);

		this.applyToSelection = applyToSelection;
		addMouseListener(new JVGMouseAdapter() {
			private Paint oldComposePaint;

			@Override
			public void mousePressed(JVGMouseEvent e) {
				if (parent instanceof JVGShape) {
					JVGShape shape = (JVGShape) parent;
					oldComposePaint = shape.getComposePaint();
					shape.setAlfa(200);
				}

				editTransform = new AffineTransform();
				start(e);
				x = getPressedX(e);
				y = getPressedY(e);
				repaint();
			}

			@Override
			public void mouseReleased(JVGMouseEvent e) {
				if (parent instanceof JVGShape) {
					JVGShape shape = (JVGShape) parent;
					shape.setComposePaint(oldComposePaint);
				}

				if (!editTransform.isIdentity()) {
					if (applyToSelection) {
						JVGPane pane = getPane();
						JVGSelectionModel selectionModel = pane.getSelectionManager();
						if (selectionModel != null) {
							fireUndoableEditUpdate(new UndoableEditEvent(getParent(), new TransformUndoRedo(getName(), getPane(), selectionModel.getSelection(), editTransform)));
						}
					} else {
						fireUndoableEditUpdate(new UndoableEditEvent(getParent(), new TransformUndoRedo(getName(), getPane(), getParent(), editTransform)));
					}

					JVGMacros.appendTransform(pane, editTransform);
				}

				finish(e);
				repaint();
			}

			@Override
			public void mouseDragged(JVGMouseEvent e) {
				double newX = getDraggedX(e);
				double newY = getDraggedY(e);

				dx = newX - x;
				dy = newY - y;

				if (dx != 0 || dy != 0) {
					AffineTransform transform = getTransform(x, y, dx, dy);
					if (transform != null && !transform.isIdentity()) {
						transform(transform);
					}
					x = newX + getDeltaX() - dx;
					y = newY + getDeltaY() - dy;

					invalidate();
					repaint();
				}
			}
		});

		setName("transform");
	}

	protected JVGShape getSelectedShape() {
		JVGPane pane = getPane();
		if (pane != null) {
			if (pane.getSelectionManager().getSelectionCount() == 1) {
				JVGComponent c = pane.getSelectionManager().getSelection()[0];
				if (c instanceof JVGShape) {
					return (JVGShape) c;
				}
			}
		}
		return null;
	}

	private void transform(AffineTransform transform) {
		if (applyToSelection) {
			JVGPane pane = getPane();
			if (pane != null) {
				if (pane.getSelectionManager().getSelectionCount() == 1) {
					JVGComponent c = pane.getSelectionManager().getSelection()[0];
					if (c instanceof JVGShape) {
						JVGShape shape = (JVGShape) c;
						shape.transform(transform);
					}
				} else {
					JVGUtil.transformSelection(pane, transform);
				}
			}
		} else {
			if (parent instanceof JVGShape) {
				JVGShape shape = (JVGShape) parent;
				shape.transform(transform);
			}
		}

		editTransform.preConcatenate(transform);
	}

	public double getDraggedX(JVGMouseEvent e) {
		return e.getAdjustedX();
	}

	public double getDraggedY(JVGMouseEvent e) {
		return e.getAdjustedY();
	}

	public double getPressedX(JVGMouseEvent e) {
		Rectangle2D bounds = getRectangleBounds();
		return bounds.getX() + bounds.getWidth() / 2.0;
	}

	public double getPressedY(JVGMouseEvent e) {
		Rectangle2D bounds = getRectangleBounds();
		return bounds.getY() + bounds.getHeight() / 2.0;
	}

	private boolean applyToSelection;

	public boolean isApplyToSelection() {
		return applyToSelection;
	}

	public void start(JVGMouseEvent e) {
	}

	public void finish(JVGMouseEvent e) {
	}

	public AffineTransform getTransform(double oldX, double oldY, double dx, double dy) {
		return new AffineTransform();
	}

	private double x, y, dx, dy;

	public double getDeltaX() {
		return dx;
	}

	public double getDeltaY() {
		return dy;
	}

	public Rectangle2D getParentInitialBounds() {
		JVGShape parent = (JVGShape) getParent();
		Shape b = parent.getInitialBounds();
		return b != null ? b.getBounds2D() : null;
	}
}
