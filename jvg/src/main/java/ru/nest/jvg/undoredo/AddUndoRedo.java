package ru.nest.jvg.undoredo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGContainer;
import ru.nest.jvg.JVGPane;

public class AddUndoRedo extends JVGUndoRedo {
	private JVGComponent component;

	private JVGContainer oldParent;

	private JVGContainer newParent;

	private int oldIndex;

	private int newIndex;

	public AddUndoRedo(JVGPane pane, JVGComponent component, JVGContainer oldParent, int oldIndex, JVGContainer newParent, int newIndex) {
		super("add", pane);
		this.component = component;
		this.oldParent = oldParent;
		this.newParent = newParent;
		this.oldIndex = oldIndex;
		this.newIndex = newIndex;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		if (oldParent != null) {
			oldParent.add(component, oldIndex);
		} else if (newParent != null) {
			newParent.remove(component);
		}
		getPane().repaint();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		if (newParent != null) {
			newParent.add(component, newIndex);
		} else if (oldParent != null) {
			oldParent.remove(component);
		}
		getPane().repaint();
	}
}
