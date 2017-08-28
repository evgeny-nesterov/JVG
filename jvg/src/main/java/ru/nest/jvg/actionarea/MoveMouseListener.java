package ru.nest.jvg.actionarea;

import java.awt.AWTException;
import java.awt.Paint;
import java.awt.Robot;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

import javax.swing.event.UndoableEditEvent;

import ru.nest.jvg.ComponentOrderComparator;
import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGContainer;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.JVGRoot;
import ru.nest.jvg.JVGSelectionModel;
import ru.nest.jvg.JVGUtil;
import ru.nest.jvg.event.JVGMouseAdapter;
import ru.nest.jvg.event.JVGMouseEvent;
import ru.nest.jvg.macros.JVGMacros;
import ru.nest.jvg.parser.JVGBuilder;
import ru.nest.jvg.parser.JVGParseException;
import ru.nest.jvg.parser.JVGParser;
import ru.nest.jvg.shape.JVGShape;
import ru.nest.jvg.undoredo.AddUndoRedo;
import ru.nest.jvg.undoredo.CompoundUndoRedo;
import ru.nest.jvg.undoredo.TransformUndoRedo;

public class MoveMouseListener extends JVGMouseAdapter {
	private final static Object SHAPE_PROPERTY = new Object();

	private static Robot robot;
	static {
		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	private JVGContainer shape;

	private JVGComponent[] shapes;

	private Paint[] oldComposePaints;

	private MoveMouseListener(JVGContainer shape) {
		this.shape = shape;
	}

	public static MoveMouseListener getListener(JVGContainer shape) {
		return (MoveMouseListener) shape.getClientProperty(SHAPE_PROPERTY);
	}

	public static void uninstall(JVGContainer shape) {
		MoveMouseListener oldListener = getListener(shape);
		if (oldListener != null) {
			shape.removeMouseListener(oldListener);
			shape.setClientProperty(SHAPE_PROPERTY, null);
		}
	}

	public static void install(JVGContainer shape) {
		uninstall(shape);

		MoveMouseListener listener = new MoveMouseListener(shape);
		shape.setClientProperty(SHAPE_PROPERTY, listener);
		shape.addMouseListener(listener);
	}

	private boolean pressed = false;

	@Override
	public void mousePressed(JVGMouseEvent e) {
		if (e.getButton() == JVGMouseEvent.BUTTON1) {
			x = getPressedX(e);
			y = getPressedY(e);
			if (!e.isAltDown()) {
				mousePressed(x, y);
			}
		}
	}

	private void mousePressed(double x, double y) {
		if (!pressed) {
			JVGPane pane = shape.getPane();
			if (pane != null) {
				if (pane.getSelectionManager().getSelectionCount() < 2) {
					shapes = new JVGComponent[] { shape };
				} else {
					shapes = pane.getSelectionManager().getSelection();
				}

				if (shapes != null) {
					oldComposePaints = new Paint[shapes.length];
					for (int i = 0; i < shapes.length; i++) {
						if (shapes[i] instanceof JVGShape) {
							JVGShape s = (JVGShape) shapes[i];
							oldComposePaints[i] = s.getComposePaint();
							s.setAlfa(200);
						}
					}
				}
			}

			pressed = true;
			dragged = false;
			editTransform = new AffineTransform();
			this.x = x;
			this.y = y;
			shape.repaint();
		}
	}

	@Override
	public void mouseReleased(JVGMouseEvent e) {
		mouseReleased();
	}

	private void mouseReleased() {
		if (pressed) {
			if (shapes != null) {
				for (int i = 0; i < shapes.length; i++) {
					if (shapes[i] instanceof JVGShape) {
						JVGShape s = (JVGShape) shapes[i];
						s.setComposePaint(oldComposePaints[i]);
					}
				}
			}

			if (dragged) {
				JVGActionArea.reset();
			}

			if (editTransform != null && !editTransform.isIdentity()) {
				JVGPane pane = shape.getPane();
				JVGSelectionModel selectionModel = pane.getSelectionManager();
				if (selectionModel != null) {
					shape.fireUndoableEditUpdate(new UndoableEditEvent(shape.getParent(), new TransformUndoRedo("move", shape.getPane(), selectionModel.getSelection(), editTransform)));
					JVGMacros.appendTransform(pane, editTransform);
				}
			}

			shapes = null;
			oldComposePaints = null;
			editTransform = null;
			pressed = false;
			dragged = false;
			cloned = false;

			shape.repaint();
		}
	}

	private boolean dragged = false;

	private boolean cloned = false;

	@Override
	public void mouseDragged(JVGMouseEvent e) {
		double newX = getDraggedX(e);
		double newY = getDraggedY(e);

		// перетаскивание объекта инструментом "Выделение" с нажатой клавишей Alt
		if (e.isAltDown() && !cloned) {
			double dx = newX - x;
			double dy = newY - y;
			double delta = Math.sqrt(dx * dx + dy * dy);
			if (delta >= 5) {
				JVGPane pane = shape.getPane();
				JVGSelectionModel selectionModel = pane.getSelectionManager();
				if (selectionModel != null) {
					JVGComponent[] components = selectionModel.getSelection();
					if (components != null && components.length > 0) {
						Arrays.sort(components, new ComponentOrderComparator());
						components = JVGUtil.getRootsSaveOrder(components);
						try {
							JVGBuilder build = JVGBuilder.create();
							String xml = build.build(components, "UTF8");

							JVGParser parser = new JVGParser(pane.getEditorKit().getFactory());
							JVGRoot root = parser.parse(xml);
							components = root.getChildren();
							pane.getRoot().add(components);

							JVGComponent focusOwner = pane.getFocusOwner();
							if (focusOwner != null) {
								focusOwner.setFocused(false);
							}
							selectionModel.clearSelection();

							JVGShape[] shapes = JVGUtil.getComponents(JVGShape.class, components);
							for (JVGShape c : shapes) {
								if (c.contains(x, y)) {
									c.requestFocus();
									break;
								}
							}
							selectionModel.addSelection(shapes);

							CompoundUndoRedo edit = new CompoundUndoRedo("copy", pane);
							for (int i = 0; i < components.length; i++) {
								edit.add(new AddUndoRedo(pane, components[i], null, -1, pane.getRoot(), -1));
							}
							pane.fireUndoableEditUpdate(new UndoableEditEvent(pane, edit));

							mousePressed(x, y);
						} catch (JVGParseException exc) {
						}
					}
				}
				cloned = true;
			}
			if (!cloned)
				return;
		}

		if (pressed) {
			dragged = true;
			JVGActionArea.setActionActive(true);

			dx = newX - x;
			dy = newY - y;

			if (dx != 0 || dy != 0) {
				AffineTransform transform = getTransform(x, y, dx, dy);
				if (transform != null && !transform.isIdentity()) {
					transform(transform);
				}
				x = newX + getDeltaX() - dx;
				y = newY + getDeltaY() - dy;

				shape.getParent().invalidate();
				shape.repaint();
			}
		}
	}

	private AffineTransform t = new AffineTransform();

	public AffineTransform getTransform(double x, double y, double dx, double dy) {
		t.setToTranslation(dx, dy);
		return t;
	}

	private AffineTransform editTransform;

	private void transform(AffineTransform transform) {
		JVGPane pane = shape.getPane();
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

		if (editTransform != null) {
			editTransform.preConcatenate(transform);
		}
	}

	public double getDraggedX(JVGMouseEvent e) {
		return e.getAdjustedX();
	}

	public double getDraggedY(JVGMouseEvent e) {
		return e.getAdjustedY();
	}

	public double getPressedX(JVGMouseEvent e) {
		return e.getAdjustedX();
	}

	public double getPressedY(JVGMouseEvent e) {
		return e.getAdjustedY();
	}

	private double x, y, dx, dy;

	public double getDeltaX() {
		return dx;
	}

	public double getDeltaY() {
		return dy;
	}

	public Rectangle2D getParentInitialBounds() {
		JVGShape parent = (JVGShape) shape.getParent();
		return parent.getInitialBounds().getBounds2D();
	}
}
