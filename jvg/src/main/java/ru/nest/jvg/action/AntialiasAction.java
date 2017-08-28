package ru.nest.jvg.action;

import java.awt.event.ActionEvent;

import javax.swing.event.UndoableEditEvent;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.macros.JVGMacrosCode;
import ru.nest.jvg.shape.JVGShape;
import ru.nest.jvg.undoredo.CompoundUndoRedo;
import ru.nest.jvg.undoredo.PropertyUndoRedo;

public class AntialiasAction extends JVGAction {
	public AntialiasAction() {
		super("antialias");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGShape[] shapes = getShapes(e);
		if (shapes != null && shapes.length > 0) {
			JVGComponent focus = getComponent(e);
			JVGShape focusedShape;
			if (focus instanceof JVGShape) {
				focusedShape = (JVGShape) focus;
			} else {
				focusedShape = shapes[0];
			}

			boolean antialias = !focusedShape.isAntialias();

			JVGPane pane = getPane(e);
			setAntialias(pane, shapes, !focusedShape.isAntialias());

			appendMacrosCode(pane, "setAntialias(id, %s);", JVGMacrosCode.ARG_ID, antialias);
		}
	}

	public static void setAntialias(JVGPane pane, JVGShape[] shapes, boolean isAntialias) {
		CompoundUndoRedo edit = new CompoundUndoRedo("antialias", pane);

		for (JVGShape shape : shapes) {
			if (shape.isAntialias() != isAntialias) {
				edit.add(new PropertyUndoRedo("antialias", pane, shape, "setAntialias", shape.isAntialias(), isAntialias));
				shape.setAntialias(isAntialias);
			}
		}

		if (!edit.isEmpty()) {
			pane.fireUndoableEditUpdate(new UndoableEditEvent(pane, edit));
			pane.repaint();
		}
	}
}
