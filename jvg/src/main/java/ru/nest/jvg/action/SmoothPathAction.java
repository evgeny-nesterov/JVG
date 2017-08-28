package ru.nest.jvg.action;

import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.geom.PathIterator;

import javax.swing.event.UndoableEditEvent;

import ru.nest.jvg.JVGPane;
import ru.nest.jvg.geom.CoordinablePathIterator;
import ru.nest.jvg.geom.MutableGeneralPath;
import ru.nest.jvg.macros.JVGMacrosCode;
import ru.nest.jvg.shape.JVGPath;
import ru.nest.jvg.undoredo.CompoundUndoRedo;
import ru.nest.jvg.undoredo.ShapeChangedUndoRedo;

public class SmoothPathAction extends JVGAction {
	public SmoothPathAction(double weight) {
		super("smooth-path");
		this.weight = weight;
	}

	private double weight = 0.1;

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGPath[] pathes = getPathes(e);
		if (pathes != null && pathes.length > 0) {
			JVGPane pane = getPane(e);
			smooth(pane, pathes, weight);
			appendMacrosCode(pane, "smooth(id, %s);", JVGMacrosCode.ARG_ID, weight);
		}
	}

	public static void smooth(JVGPane pane, JVGPath[] pathes, double weight) {
		if (pathes != null && pathes.length > 0) {
			CompoundUndoRedo edit = new CompoundUndoRedo("smooth-path", pane);

			for (JVGPath path : pathes) {
				Shape oldShape = path.getShape();
				Shape newShape = smooth(oldShape, weight);
				path.setShape(newShape, false);
				edit.add(new ShapeChangedUndoRedo("smooth-path", pane, path, oldShape, newShape));
			}

			pane.fireUndoableEditUpdate(new UndoableEditEvent(pane, edit));
			pane.repaint();
		}
	}

	public static Shape smooth(Shape shape, double weight) {
		MutableGeneralPath path = shape instanceof MutableGeneralPath ? (MutableGeneralPath) shape : new MutableGeneralPath(shape);
		MutableGeneralPath newPath = new MutableGeneralPath(shape);
		int moveIndex = 0, coord = 0, lastType = 0;
		double sum = 1 + 2 * weight;
		for (int t = 0; t < path.numTypes; t++) {
			int type = path.pointTypes[t];
			if (type == PathIterator.SEG_CLOSE) {
				double prevX = path.pointCoords[coord - 2];
				double prevY = path.pointCoords[coord - 1];
				double x = path.pointCoords[moveIndex];
				double y = path.pointCoords[moveIndex + 1];
				double nextX = path.pointCoords[moveIndex + 2];
				double nextY = path.pointCoords[moveIndex + 3];

				newPath.pointCoords[moveIndex] = (x + weight * (prevX + nextX)) / sum;
				newPath.pointCoords[moveIndex + 1] = (y + weight * (prevY + nextY)) / sum;
			} else if (type == PathIterator.SEG_MOVETO) {
				moveIndex = coord;
				coord += 2;
			} else {
				int coordinates = CoordinablePathIterator.curvesize[type];
				double prevX = 0, prevY = 0, x = 0, y = 0, nextX = 0, nextY = 0;
				for (int i = 0; i < coordinates; i += 2) {
					if (i == 0 && lastType == PathIterator.SEG_CLOSE) {
						prevX = path.pointCoords[moveIndex];
						prevY = path.pointCoords[moveIndex + 1];
					} else {
						prevX = path.pointCoords[coord - 2];
						prevY = path.pointCoords[coord - 1];
					}

					x = path.pointCoords[coord];
					y = path.pointCoords[coord + 1];

					if (i == coordinates - 2 && t < path.numTypes - 1) {
						int nextType = path.pointTypes[t + 1];
						if (nextType == PathIterator.SEG_CLOSE) {
							nextX = path.pointCoords[moveIndex];
							nextY = path.pointCoords[moveIndex + 1];
						} else if (nextType == PathIterator.SEG_MOVETO) {
							coord += 2;
							continue;
						} else {
							nextX = path.pointCoords[coord + 2];
							nextY = path.pointCoords[coord + 3];
						}
					} else if (coord < path.numCoords - 2) {
						nextX = path.pointCoords[coord + 2];
						nextY = path.pointCoords[coord + 3];
					} else {
						// End of path
						break;
					}

					newPath.pointCoords[coord] = (x + weight * (prevX + nextX)) / sum;
					newPath.pointCoords[coord + 1] = (y + weight * (prevY + nextY)) / sum;

					coord += 2;
				}
			}
			lastType = type;
		}
		return newPath;
	}
}
