package ru.nest.jvg.action;

import java.awt.event.ActionEvent;
import java.awt.geom.PathIterator;

import javax.swing.event.UndoableEditEvent;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGEditorKit;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.geom.coord.Coordinable;
import ru.nest.jvg.geom.coord.Coordinate;
import ru.nest.jvg.shape.JVGPath;
import ru.nest.jvg.undoredo.PathAddCurveUndoRedo;

public class PathAddCurveAction extends JVGAction {
	public PathAddCurveAction(JVGComponent o, int type, Coordinable x, Coordinable y) {
		super(type >= 0 && type < JVGEditorKit.appendToPathActions.length ? JVGEditorKit.appendToPathActions[type] : "");
		this.o = o;
		this.type = type;
		coords = new Coordinable[] { x, y };
	}

	public PathAddCurveAction(JVGComponent o, int type, Coordinable[] coords) {
		super(type >= 0 && type < JVGEditorKit.appendToPathActions.length ? JVGEditorKit.appendToPathActions[type] : "");
		this.o = o;
		this.type = type;
		this.coords = coords;
	}

	public PathAddCurveAction(int type, Coordinable x, Coordinable y) {
		super(type >= 0 && type < JVGEditorKit.appendToPathActions.length ? JVGEditorKit.appendToPathActions[type] : "");
		this.o = null;
		this.type = type;
		coords = new Coordinable[] { x, y };
	}

	private int type;

	private Coordinable[] coords;

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
			if (pane == null) {
				pane = getFocusedPane();
			}

			JVGPath shape = (JVGPath) o;
			int numTypes = shape.getNumTypes();

			c = coords;
			switch (type) {
				case PathIterator.SEG_CLOSE:
					if (!shape.closePath()) {
						return;
					}
					break;

				case PathIterator.SEG_MOVETO:
					if (!shape.moveTo(coords[0], coords[1])) {
						return;
					}
					c = new Coordinable[] { coords[0], coords[1] };
					break;

				case PathIterator.SEG_LINETO:
					if (numTypes == 0) {
						return;
					}

					shape.lineTo(coords[0], coords[1]);
					c = new Coordinable[] { coords[0], coords[1] };
					break;

				case PathIterator.SEG_QUADTO:
					if (numTypes == 0) {
						return;
					}

					if (coords.length != 4) {
						int numCoords = shape.getNumCoords();
						double startX = shape.getCoord(numCoords - 2);
						double startY = shape.getCoord(numCoords - 1);
						double endX = coords[0].getCoord();
						double endY = coords[1].getCoord();
						Coordinable middleX = new Coordinate((endX + startX) / 2f);
						Coordinable middleY = new Coordinate((endY + startY) / 2f);
						c = new Coordinable[] { middleX, middleY, coords[0], coords[1] };
					}
					shape.quadTo(c[0], c[1], c[2], c[3]);
					break;

				case PathIterator.SEG_CUBICTO:
					if (numTypes == 0) {
						return;
					}

					if (coords.length != 6) {
						int numCoords = shape.getNumCoords();
						double startX = shape.getCoord(numCoords - 2);
						double startY = shape.getCoord(numCoords - 1);
						double endX = coords[0].getCoord();
						double endY = coords[1].getCoord();
						Coordinable middleX1 = new Coordinate(startX + (endX - startX) / 3f);
						Coordinable middleY1 = new Coordinate(startY + (endY - startY) / 3f);
						Coordinable middleX2 = new Coordinate(startX + (endX - startX) * 2f / 3f);
						Coordinable middleY2 = new Coordinate(startY + (endY - startY) * 2f / 3f);
						c = new Coordinable[] { middleX1, middleY1, middleX2, middleY2, coords[0], coords[1] };
					}
					shape.curveTo(c[0], c[1], c[2], c[3], c[4], c[5]);
					break;
			}

			pane.fireUndoableEditUpdate(new UndoableEditEvent(shape, new PathAddCurveUndoRedo(pane, shape, type, c)));
			shape.repaint();

			// TODO: add macrosCode
		}
	}
}
