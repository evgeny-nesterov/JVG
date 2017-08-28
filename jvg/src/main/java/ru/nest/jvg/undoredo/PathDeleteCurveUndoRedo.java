package ru.nest.jvg.undoredo;

import java.awt.geom.PathIterator;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import ru.nest.jvg.JVGPane;
import ru.nest.jvg.geom.coord.Coordinable;
import ru.nest.jvg.shape.JVGPath;

public class PathDeleteCurveUndoRedo extends JVGUndoRedo {
	private JVGPath component;

	private int index;

	private int type;

	private Coordinable[] c;

	public PathDeleteCurveUndoRedo(JVGPane pane, JVGPath component) {
		this(pane, component, component.getNumTypes());
	}

	public PathDeleteCurveUndoRedo(JVGPane pane, JVGPath component, int index) {
		super("path-delete-curve", pane);
		this.component = component;
		this.index = index;

		// Init this undo before deleting path element
		this.type = component.getType(index);
		this.c = component.getCurvePoints(index);
	}

	@Override
	public void redo() throws CannotUndoException {
		super.redo();
		component.delete(index);
		component.repaint();
	}

	@Override
	public void undo() throws CannotRedoException {
		super.undo();
		switch (type) {
			case PathIterator.SEG_MOVETO:
				if (c != null && c.length == 2) {
					component.insertMoveTo(index, c[0], c[1]);
				}
				break;

			case PathIterator.SEG_LINETO:
				if (c != null && c.length == 2) {
					component.insertLineTo(index, c[0], c[1]);
				}
				break;

			case PathIterator.SEG_QUADTO:
				if (c != null && c.length == 4) {
					component.insertQuadTo(index, c[0], c[1], c[2], c[3]);
				}
				break;

			case PathIterator.SEG_CUBICTO:
				if (c != null && c.length == 6) {
					component.insertCurveTo(index, c[0], c[1], c[2], c[3], c[4], c[5]);
				}
				break;

			case PathIterator.SEG_CLOSE:
				component.closePath();
				break;
		}
		component.repaint();
	}
}
