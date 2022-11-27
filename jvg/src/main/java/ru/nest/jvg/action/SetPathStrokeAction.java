package ru.nest.jvg.action;

import java.awt.Stroke;
import java.awt.event.ActionEvent;

import javax.swing.event.UndoableEditEvent;

import ru.nest.jvg.JVGPane;
import ru.nest.jvg.macros.JVGMacrosCode;
import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.resource.StrokeResource;
import ru.nest.jvg.shape.JVGPath;
import ru.nest.jvg.undoredo.CompoundUndoRedo;
import ru.nest.jvg.undoredo.PropertyUndoRedo;

public abstract class SetPathStrokeAction<S extends Stroke> extends JVGAction {
	public final static int TYPE_NONE = 0;

	protected int type;

	protected Resource<S> stroke;

	private boolean undoRedoActive = true;

	public SetPathStrokeAction(String name) {
		super(name);
	}

	public SetPathStrokeAction() {
		super("pathstroke-none");
		this.type = TYPE_NONE;
	}

	public SetPathStrokeAction(String name, S stroke, int type) {
		super(name);
		set(stroke, type);
	}

	public SetPathStrokeAction(String name, Resource<S> stroke, int type) {
		super(name);
		set(stroke, type);
	}

	protected void set(S stroke, int type) {
		set(new StrokeResource<>(stroke), type);
	}

	protected void set(Resource<S> stroke, int type) {
		this.type = type;
		if (stroke != null) {
			this.stroke = new StrokeResource(stroke.getResource());
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGPath[] pathes = getPathes(e);
		if (pathes != null && pathes.length > 0) {
			JVGPane pane = getPane(e);
			CompoundUndoRedo edit = new CompoundUndoRedo(getName(), pane);

			for (JVGPath path : pathes) {
				setStroke(pane, path, stroke, getName(), edit, type);
			}

			if (!edit.isEmpty()) {
				if (undoRedoActive) {
					pane.fireUndoableEditUpdate(new UndoableEditEvent(pane, edit));
					appendMacrosCode(pane);
				}
				pane.repaint();
			}
		}
	}

	public void appendMacrosCode(JVGPane pane) {
		if (type == TYPE_NONE) {
			appendMacrosCode(pane, "removeStroke(id);", JVGMacrosCode.ARG_ID);
		}
	}

	public Resource<S> setStroke(JVGPane pane, JVGPath path, Resource<S> stroke, String actionName, CompoundUndoRedo edit, int type) {
		Resource<? extends Stroke> oldStroke = path.getPathStroke();
		boolean setFullStroke = false;
		if (type == TYPE_NONE || stroke == null) {
			setFullStroke = true;
		} else if (oldStroke == null || oldStroke.getResource().getClass() != stroke.getResource().getClass()) {
			setFullStroke = true;
		}

		if (!setFullStroke) {
			stroke = getStroke(pane, (Resource<S>) oldStroke, stroke, actionName, edit, type);
			if (stroke == null) {
				return null;
			}
		}

		path.setPathStroke(stroke);
		Class<?>[] p = { Resource.class };
		edit.add(new PropertyUndoRedo(actionName, pane, path, "setPathStroke", p, new Object[] { oldStroke }, "setPathStroke", p, new Object[] { stroke }));
		return stroke;
	}

	public abstract Resource<S> getStroke(JVGPane pane, Resource<S> oldStrokeResource, Resource<S> newStrokeResource, String actionName, CompoundUndoRedo edit, int type);

	public boolean isUndoRedoActive() {
		return undoRedoActive;
	}

	public void setUndoRedoActive(boolean undoRedoActive) {
		this.undoRedoActive = undoRedoActive;
	}
}
