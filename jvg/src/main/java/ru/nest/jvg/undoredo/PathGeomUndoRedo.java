package ru.nest.jvg.undoredo;

import java.awt.Shape;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import ru.nest.jvg.JVGPane;
import ru.nest.jvg.geom.CoordinablePath;
import ru.nest.jvg.geom.MutableGeneralPath;
import ru.nest.jvg.geom.coord.Coordinable;
import ru.nest.jvg.shape.JVGShape;

public class PathGeomUndoRedo extends JVGUndoRedo {
	private JVGShape component;

	private int index;

	private double oldX;

	private double oldY;

	private double newX;

	private double newY;

	private Coordinable x;

	private Coordinable y;

	public PathGeomUndoRedo(JVGPane pane, JVGShape component, int index, double oldX, double oldY, double newX, double newY) {
		super("path-geom", pane);
		this.component = component;
		this.index = index;
		this.oldX = oldX;
		this.oldY = oldY;
		this.newX = newX;
		this.newY = newY;

		Shape shape = component.getShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			x = path.pointCoords[2 * index];
			y = path.pointCoords[2 * index + 1];
		} else if (shape instanceof MutableGeneralPath) {
			MutableGeneralPath path = (MutableGeneralPath) shape;
		}
	}

	public PathGeomUndoRedo(JVGPane pane, JVGShape component, Coordinable x, Coordinable y, double oldX, double oldY, double newX, double newY) {
		super("path-geom", pane);
		this.component = component;
		this.x = x;
		this.y = y;
		this.oldX = oldX;
		this.oldY = oldY;
		this.newX = newX;
		this.newY = newY;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		if (x != null && y != null) {
			x.setCoord(oldX);
			y.setCoord(oldY);
			component.invalidate();
			component.repaint();
		} else {
			Shape shape = component.getShape();
			if (shape instanceof MutableGeneralPath) {
				MutableGeneralPath path = (MutableGeneralPath) shape;
				path.pointCoords[2 * index] = oldX;
				path.pointCoords[2 * index + 1] = oldY;
				component.invalidate();
				component.repaint();
			}
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		if (x != null && y != null) {
			x.setCoord(newX);
			y.setCoord(newY);
			component.invalidate();
			component.repaint();
		} else {
			Shape shape = component.getShape();
			if (shape instanceof MutableGeneralPath) {
				MutableGeneralPath path = (MutableGeneralPath) shape;
				path.pointCoords[2 * index] = newX;
				path.pointCoords[2 * index + 1] = newY;
				component.invalidate();
				component.repaint();
			}
		}
	}
}
