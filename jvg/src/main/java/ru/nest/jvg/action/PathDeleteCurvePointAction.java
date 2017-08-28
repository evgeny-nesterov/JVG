package ru.nest.jvg.action;

import java.awt.event.ActionEvent;

import javax.swing.event.UndoableEditEvent;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.geom.coord.Coordinable;
import ru.nest.jvg.shape.JVGPath;
import ru.nest.jvg.undoredo.PathDeleteCurvePointUndoRedo;

public class PathDeleteCurvePointAction extends JVGAction {
	public PathDeleteCurvePointAction(JVGComponent o, int coordIndex) {
		super("path-delete-curve-point");
		this.coordIndex = coordIndex;
	}

	private int coordIndex;

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
			pane.fireUndoableEditUpdate(new UndoableEditEvent(path, new PathDeleteCurvePointUndoRedo(pane, path, coordIndex)));
			path.deletePoint(coordIndex);
			path.repaint();

			// TODO: add macrosCode
		}
	}
}
