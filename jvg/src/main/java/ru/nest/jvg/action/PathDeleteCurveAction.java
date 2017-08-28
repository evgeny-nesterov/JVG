package ru.nest.jvg.action;

import java.awt.event.ActionEvent;

import javax.swing.event.UndoableEditEvent;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.geom.coord.Coordinable;
import ru.nest.jvg.shape.JVGPath;
import ru.nest.jvg.undoredo.PathDeleteCurveUndoRedo;

public class PathDeleteCurveAction extends JVGAction {
	public PathDeleteCurveAction(JVGComponent o, int index) {
		super("path-delete-curve");
		this.index = index;
	}

	private int index;

	private JVGComponent o;

	private Coordinable[] c = null;

	public Coordinable[] getCoord() {
		return c;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGComponent o = this.o == null ? getComponent(e) : this.o;
		if (o != null && o instanceof JVGPath) {
			JVGPane pane = o.getPane();

			JVGPath path = (JVGPath) o;
			pane.fireUndoableEditUpdate(new UndoableEditEvent(path, new PathDeleteCurveUndoRedo(pane, path, index)));
			path.delete(index);
			path.repaint();

			// TODO: add macrosCode
		}
	}
}
