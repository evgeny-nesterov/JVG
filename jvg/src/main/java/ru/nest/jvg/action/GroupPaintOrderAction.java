package ru.nest.jvg.action;

import java.awt.event.ActionEvent;

import javax.swing.event.UndoableEditEvent;

import ru.nest.jvg.JVGPane;
import ru.nest.jvg.shape.JVGGroup;
import ru.nest.jvg.shape.JVGShape;
import ru.nest.jvg.undoredo.CompoundUndoRedo;
import ru.nest.jvg.undoredo.PropertyUndoRedo;

public class GroupPaintOrderAction extends JVGAction {
	private int type;

	public GroupPaintOrderAction(int type) {
		super("paint-order");
		this.type = type;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGShape[] shapes = getShapes(e);
		if (shapes != null && shapes.length > 0) {
			JVGPane pane = getPane(e);
			setPaintOrder(pane, shapes, type);
		}
	}

	public static void setPaintOrder(JVGPane pane, JVGShape[] shapes, int type) {
		CompoundUndoRedo edit = new CompoundUndoRedo("antialias", pane);

		for (JVGShape shape : shapes) {
			if (shape instanceof JVGGroup) {
				JVGGroup group = (JVGGroup) shape;
				if (group.getPaintOrderType() != type) {
					edit.add(new PropertyUndoRedo("paint-order", pane, group, "setPaintOrderType", group.getPaintOrderType(), type));
					group.setPaintOrderType(type);
				}
			}
		}

		if (!edit.isEmpty()) {
			pane.fireUndoableEditUpdate(new UndoableEditEvent(pane, edit));
			pane.repaint();
		}
	}
}
