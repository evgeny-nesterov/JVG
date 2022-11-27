package ru.nest.jvg.action;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;

import javax.swing.event.UndoableEditEvent;

import ru.nest.jvg.JVGPane;
import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.resource.StrokeResource;
import ru.nest.jvg.shape.JVGPath;
import ru.nest.jvg.shape.paint.OutlinePainter;
import ru.nest.jvg.shape.paint.Painter;
import ru.nest.jvg.undoredo.CompoundUndoRedo;
import ru.nest.jvg.undoredo.PropertyUndoRedo;
import ru.nest.jvg.undoredo.ShapeChangedUndoRedo;

public class StrokePathAction extends JVGAction {
	public final static BasicStroke DEFAULT_STROKE = new BasicStroke(1f);

	public StrokePathAction(Stroke stroke) {
		super("stroke-shape");
		this.stroke = stroke;
	}

	public StrokePathAction() {
		this(null);
	}

	private Stroke stroke;

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGPath[] pathes = getPathes(e);
		if (pathes != null && pathes.length > 0) {
			JVGPane pane = getPane(e);
			CompoundUndoRedo edit = new CompoundUndoRedo(getName(), pane);

			for (JVGPath path : pathes) {
				OutlinePainter outliner = null;
				int count = path.getPaintersCount();
				for (int i = count - 1; i >= 0; i--) {
					Painter p = path.getPainter(i);
					if (p instanceof OutlinePainter) {
						outliner = (OutlinePainter) p;
						break;
					}
				}

				Stroke stroke = this.stroke;
				if (stroke == null) {
					if (outliner != null && outliner.getStroke() != null && outliner.getStroke().getResource() != null) {
						stroke = outliner.getStroke().getResource();
					} else {
						stroke = DEFAULT_STROKE;
					}
				}

				Shape oldShape = path.getPathShape();
				Shape newShape = stroke.createStrokedShape(path.getShape());

				if (path.getPathStroke() != null) {
					Resource<? extends Stroke> oldPathStroke = path.getPathStroke();
					edit.add(new PropertyUndoRedo(getName(), pane, path, "setPathStroke", Resource.class, oldPathStroke, null));
					path.setPathStroke(null);
				}

				path.setShape(newShape);
				path.invalidate();
				edit.add(new ShapeChangedUndoRedo(getName(), pane, path, oldShape, newShape));

				if (outliner != null) {
					BasicStrokeAction.setStroke(pane, outliner, new StrokeResource<>(DEFAULT_STROKE), getName(), edit, BasicStrokeAction.TYPE_STROKE);
				}
			}

			if (!edit.isEmpty()) {
				pane.fireUndoableEditUpdate(new UndoableEditEvent(pane, edit));
				pane.repaint();

				// TODO: add macrosCode
			}
		}
	}
}
