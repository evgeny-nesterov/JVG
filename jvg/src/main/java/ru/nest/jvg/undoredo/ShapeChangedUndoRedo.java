package ru.nest.jvg.undoredo;

import java.awt.Shape;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import ru.nest.jvg.JVGPane;
import ru.nest.jvg.shape.JVGShape;

public class ShapeChangedUndoRedo extends JVGUndoRedo {
	private JVGShape component;

	private Shape oldShape;

	private Shape newShape;

	public ShapeChangedUndoRedo(String name, JVGPane pane, JVGShape component, Shape oldShape, Shape newShape) {
		super(name, pane);
		this.component = component;
		this.oldShape = oldShape;
		this.newShape = newShape;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		component.setShape(oldShape, false);
		component.invalidate();
		component.repaint();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		component.setShape(newShape, false);
		component.invalidate();
		component.repaint();
	}
}
