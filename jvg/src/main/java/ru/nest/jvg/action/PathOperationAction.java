package ru.nest.jvg.action;

import java.awt.Shape;
import java.awt.event.ActionEvent;

import javax.swing.event.UndoableEditEvent;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGContainer;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.macros.JVGMacrosCode;
import ru.nest.jvg.shape.JVGPath;
import ru.nest.jvg.shape.JVGShape;
import ru.nest.jvg.undoredo.AddUndoRedo;
import ru.nest.jvg.undoredo.CompoundUndoRedo;
import ru.nest.jvg.undoredo.RemoveUndoRedo;
import ru.nest.jvg.undoredo.ShapeChangedUndoRedo;

public class PathOperationAction extends JVGAction {
	public final static int UNION = 0;

	public final static int SUBTRACTION = 1;

	public final static int INTERSECTION = 2;

	public final static int XOR = 3;

	public final static String[] names = { "path-union", "path-subtraction", "path-intersection", "path-xor" };

	public PathOperationAction(int type) {
		super("path-operation");
		this.type = type;
	}

	private int type;

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGPath[] pathes = getPathes(e);
		JVGPane pane = getPane(e);
		JVGComponent focus = getComponent(e);

		doOperation(pane, focus, type, pathes);

		appendMacrosCode(pane, "setPathOperation(ids, getFocus(), %s);", JVGMacrosCode.ARG_IDS, type);
	}

	public static void doOperation(JVGPane pane, JVGComponent focus, int type, JVGShape... pathes) {
		if (pathes != null && pathes.length > 0) {
			CompoundUndoRedo edit = new CompoundUndoRedo(names[type], pane);

			JVGShape focusPath;
			if (focus instanceof JVGShape) {
				focusPath = (JVGShape) focus;
			} else {
				focusPath = pathes[0];
			}

			switch (type) {
				case UNION:
					JVGPath unionPath = pane.getEditorKit().getFactory().createComponent(JVGPath.class, pathes[0].getTransformedShape(), false);
					for (int i = 1; i < pathes.length; i++) {
						unionPath.add(pathes[i].getTransformedShape());
					}

					for (JVGShape path : pathes) {
						JVGContainer parent = path.getParent();
						edit.add(new RemoveUndoRedo(pane, path, parent, parent.getChildIndex(path)));

						parent.remove(path);
					}
					pane.getRoot().add(unionPath);

					for (int i = 0; i < focusPath.getPaintersCount(); i++) {
						unionPath.addPainter(focusPath.getPainter(i));
					}
					unionPath.setFill(focusPath.isFill());
					unionPath.setSelected(true, false);

					edit.add(new AddUndoRedo(pane, unionPath, null, -1, pane.getRoot(), -1));
					break;

				case SUBTRACTION:
					for (JVGShape shape : pathes) {
						if (shape != focusPath && shape instanceof JVGPath) {
							JVGPath path = (JVGPath) shape;

							Shape oldShape = path.getShape();
							path.subtract(focusPath.getTransformedShape());
							Shape newShape = path.getShape();

							edit.add(new ShapeChangedUndoRedo("path-operation", pane, path, oldShape, newShape));
						}
					}
					break;

				case INTERSECTION:
					JVGPath intersectionPath = pane.getEditorKit().getFactory().createComponent(JVGPath.class, new Object[] { pathes[0].getTransformedShape(), false });

					for (int i = 1; i < pathes.length; i++) {
						intersectionPath.intersect(pathes[i].getTransformedShape());
					}

					for (JVGShape path : pathes) {
						JVGContainer parent = path.getParent();
						edit.add(new RemoveUndoRedo(pane, path, parent, parent.getChildIndex(path)));

						parent.remove(path);
					}
					pane.getRoot().add(intersectionPath);

					for (int i = 0; i < focusPath.getPaintersCount(); i++) {
						intersectionPath.addPainter(focusPath.getPainter(i));
					}
					intersectionPath.setFill(focusPath.isFill());
					intersectionPath.setSelected(true, false);

					edit.add(new AddUndoRedo(pane, intersectionPath, null, -1, pane.getRoot(), -1));
					break;

				case XOR:
					JVGPath xorPath = pane.getEditorKit().getFactory().createComponent(JVGPath.class, pathes[0].getTransformedShape(), false);

					for (int i = 1; i < pathes.length; i++) {
						xorPath.exclusiveOr(pathes[i].getTransformedShape());
					}

					for (JVGShape path : pathes) {
						JVGContainer parent = path.getParent();
						edit.add(new RemoveUndoRedo(pane, path, parent, parent.getChildIndex(path)));

						parent.remove(path);
					}
					pane.getRoot().add(xorPath);

					for (int i = 0; i < focusPath.getPaintersCount(); i++) {
						xorPath.addPainter(focusPath.getPainter(i));
					}
					xorPath.setFill(focusPath.isFill());
					xorPath.setSelected(true, false);

					edit.add(new AddUndoRedo(pane, xorPath, null, -1, pane.getRoot(), -1));
					break;
			}

			pane.fireUndoableEditUpdate(new UndoableEditEvent(pane, edit));
			pane.repaint();

			// TODO: add macrosCode
		}
	}
}
