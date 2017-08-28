package ru.nest.jvg.undoredo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGContainer;
import ru.nest.jvg.JVGPane;

public class OrderUndoRedo extends JVGUndoRedo {
	private JVGComponent component;

	private int oldIndex;

	private int newIndex;

	public OrderUndoRedo(JVGPane pane, JVGComponent component, int oldIndex, int newIndex) {
		super("order", pane);
		this.component = component;
		this.oldIndex = oldIndex;
		this.newIndex = newIndex;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		JVGContainer parent = component.getParent();
		parent.setComponentIndex(component, oldIndex);
		component.repaint();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		JVGContainer parent = component.getParent();
		parent.setComponentIndex(component, newIndex);
		component.repaint();
	}
}
