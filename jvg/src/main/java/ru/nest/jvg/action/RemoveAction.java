package ru.nest.jvg.action;

import java.awt.event.ActionEvent;

import javax.swing.event.UndoableEditEvent;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGContainer;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.macros.JVGMacrosCode;
import ru.nest.jvg.undoredo.CompoundUndoRedo;
import ru.nest.jvg.undoredo.RemoveUndoRedo;

public class RemoveAction extends JVGAction {
	public RemoveAction() {
		super("remove");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGPane pane = getPane(e);
		if (pane != null) {
			JVGComponent[] components = getComponents(e);
			if (components != null) {
				remove(pane, components);
				appendMacrosCode(pane, "remove(id);", JVGMacrosCode.ARG_ID);
			}
		}
	}

	public static void remove(JVGPane pane, JVGComponent... components) {
		CompoundUndoRedo edit = new CompoundUndoRedo("remove-selected", pane);

		for (int i = 0; i < components.length; i++) {
			JVGComponent c = components[i];
			JVGContainer p = c.getParent();
			if (p != null) {
				edit.add(new RemoveUndoRedo(pane, c, p, p.getChildIndex(c)));
				p.remove(c);
			}
		}

		pane.fireUndoableEditUpdate(new UndoableEditEvent(pane, edit));
		pane.repaint();
	}
}
