package ru.nest.jvg.undoredo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGContainer;
import ru.nest.jvg.JVGPane;

public class RemoveUndoRedo extends JVGUndoRedo {
	private JVGComponent component;

	private JVGContainer parent;

	private int index;

	public RemoveUndoRedo(JVGPane pane, JVGComponent component, JVGContainer parent, int index) {
		super("remove", pane);
		this.component = component;
		this.parent = parent;
		this.index = index;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		parent.add(component, index);
		parent.repaint();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		parent.remove(component);
		parent.repaint();
	}
}
