package ru.nest.jvg.undoredo;

import java.awt.geom.PathIterator;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import ru.nest.jvg.JVGPane;
import ru.nest.jvg.geom.coord.Coordinable;
import ru.nest.jvg.shape.JVGPath;

public class PathAddCurveUndoRedo extends JVGUndoRedo {
	private JVGPath component;

	private int index;

	private int type;

	private Coordinable[] c;

	public PathAddCurveUndoRedo(JVGPane pane, JVGPath component, int type, Coordinable[] c) {
		this(pane, component, type, c, component.getNumTypes() - 1);
	}

	public PathAddCurveUndoRedo(JVGPane pane, JVGPath component, int type, Coordinable[] c, int index) {
		super("path-add-curve", pane);
		this.component = component;
		this.c = c;
		this.index = index;
		this.type = type;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		component.delete(index);
		component.repaint();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
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
