package ru.nest.jvg.action;

import java.awt.event.ActionEvent;

import javax.swing.event.UndoableEditEvent;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGContainer;
import ru.nest.jvg.JVGEditorKit;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.macros.JVGMacrosCode;
import ru.nest.jvg.undoredo.CompoundUndoRedo;
import ru.nest.jvg.undoredo.OrderUndoRedo;

public class OrderAction extends JVGAction {
	public final static int TO_FRONT = 0;

	public final static int TO_UP = 1;

	public final static int TO_DOWN = 2;

	public final static int TO_BACK = 3;

	public OrderAction(int type) {
		super(type >= 0 && type < JVGEditorKit.orderActions.length ? JVGEditorKit.orderActions[type] : "");
		this.type = type;
	}

	private int type;

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGComponent[] components = getComponents(e);
		if (components != null) {
			JVGPane pane = getPane(e);
			setOrder(pane, components, type);

			appendMacrosCode(pane, "setOrder(id, %s);", JVGMacrosCode.ARG_ID, type);
		}
	}

	public static void setOrder(JVGPane pane, JVGComponent[] components, int type) {
		CompoundUndoRedo edit = new CompoundUndoRedo(type >= 0 && type < JVGEditorKit.orderActions.length ? JVGEditorKit.orderActions[type] : "", pane);

		for (JVGComponent component : components) {
			JVGContainer parent = component.getParent();
			if (parent != null) {
				int oldIndex = parent.getChildIndex(component);
				int newIndex = 0;

				switch (type) {
					case TO_FRONT:
						component.toFront();
						newIndex = -1;
						break;

					case TO_BACK:
						component.toBack();
						newIndex = 0;
						break;

					case TO_UP:
						newIndex = component.toUp();
						break;

					case TO_DOWN:
						newIndex = component.toDown();
						break;
				}

				if (oldIndex != newIndex) {
					edit.add(new OrderUndoRedo(pane, component, oldIndex, newIndex));
				}
			}
		}

		if (!edit.isEmpty()) {
			pane.fireUndoableEditUpdate(new UndoableEditEvent(pane, edit));
			pane.repaint();
		}
	}
}
