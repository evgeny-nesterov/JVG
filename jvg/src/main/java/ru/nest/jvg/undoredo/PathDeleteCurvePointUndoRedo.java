package ru.nest.jvg.undoredo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import ru.nest.jvg.JVGPane;
import ru.nest.jvg.geom.coord.Coordinable;
import ru.nest.jvg.shape.JVGPath;

public class PathDeleteCurvePointUndoRedo extends JVGUndoRedo {
	private JVGPath component;

	private int coordIndex;

	private Coordinable[] point;

	public PathDeleteCurvePointUndoRedo(JVGPane pane, JVGPath component) {
		this(pane, component, component.getNumTypes());
	}

	public PathDeleteCurvePointUndoRedo(JVGPane pane, JVGPath component, int coordIndex) {
		super("path-delete-curve-point", pane);
		this.component = component;
		this.coordIndex = coordIndex;

		// Init this undo before deleting path point
		this.point = component.getPoint(coordIndex);
	}

	@Override
	public void redo() throws CannotUndoException {
		super.redo();
		component.deletePoint(coordIndex);
		component.repaint();
	}

	@Override
	public void undo() throws CannotRedoException {
		super.undo();
		component.insertPoint(coordIndex, point[0], point[1]);
		component.repaint();
	}
}
