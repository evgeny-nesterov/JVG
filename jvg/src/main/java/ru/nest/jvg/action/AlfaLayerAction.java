package ru.nest.jvg.action;

import java.awt.Paint;
import java.awt.event.ActionEvent;

import javax.swing.event.UndoableEditEvent;

import ru.nest.jvg.JVGPane;
import ru.nest.jvg.macros.JVGMacrosCode;
import ru.nest.jvg.shape.JVGShape;
import ru.nest.jvg.undoredo.CompoundUndoRedo;
import ru.nest.jvg.undoredo.PropertyUndoRedo;

public class AlfaLayerAction extends JVGAction {
	public AlfaLayerAction(int alfa) {
		super("alfa-layer");
		this.alfa = alfa;
	}

	private int alfa;

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGShape[] shapes = getShapes(e);
		if (shapes != null && shapes.length > 0) {
			JVGPane pane = getPane(e);
			CompoundUndoRedo edit = new CompoundUndoRedo(getName(), pane);

			for (JVGShape shape : shapes) {
				Paint oldComposePaint = shape.getComposePaint();
				shape.setAlfa(alfa);

				edit.add(new PropertyUndoRedo(getName(), pane, shape, "setComposePaint", new Class[] { Paint.class }, new Object[] { oldComposePaint }, "setAlfa", new Class[] { int.class }, new Object[] { alfa }));
			}

			if (!edit.isEmpty()) {
				pane.fireUndoableEditUpdate(new UndoableEditEvent(pane, edit));
				pane.repaint();
			}

			appendMacrosCode(pane, "setAlfa(id, %s);", JVGMacrosCode.ARG_ID, alfa);
		}
	}
}
