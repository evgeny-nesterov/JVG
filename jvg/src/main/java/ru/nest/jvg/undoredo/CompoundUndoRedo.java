package ru.nest.jvg.undoredo;

import java.util.ArrayList;
import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import ru.nest.jvg.JVGPane;

public class CompoundUndoRedo extends JVGUndoRedo {
	public CompoundUndoRedo(String name, JVGPane pane) {
		super(name, pane);
	}

	private List<UndoableEdit> edits = null;

	public synchronized void add(UndoableEdit edit) {
		if (edits == null) {
			edits = new ArrayList<>();
		}
		edits.add(edit);
	}

	public synchronized boolean isEmpty() {
		return edits == null;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		if (edits != null) {
			for (int i = edits.size() - 1; i >= 0; i--) {
				edits.get(i).undo();
			}
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		if (edits != null) {
			for (int i = 0; i < edits.size(); i++) {
				edits.get(i).redo();
			}
		}
	}
}
