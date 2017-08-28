package ru.nest.jvg.undoredo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.editor.resources.JVGLocaleManager;

public class JVGUndoRedo extends AbstractUndoableEdit {
	private JVGLocaleManager lm = JVGLocaleManager.getInstance();

	public JVGUndoRedo(String name, JVGPane pane) {
		this.name = name;
		this.pane = pane;
		selection = pane.getSelectionManager().getSelection();
	}

	private JVGComponent[] selection;

	private String name;

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	private JVGPane pane;

	public JVGPane getPane() {
		return pane;
	}

	@Override
	public void undo() throws CannotRedoException {
		super.undo();
		pane.getSelectionManager().setSelection(selection);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		pane.getSelectionManager().setSelection(selection);
	}

	@Override
	public String getPresentationName() {
		return lm.getValue("undo." + name, name);
	}
}
