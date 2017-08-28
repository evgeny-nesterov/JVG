package ru.nest.jvg.action;

import java.awt.BasicStroke;
import java.awt.Stroke;
import java.awt.event.ActionEvent;

import javax.swing.event.UndoableEditEvent;

import ru.nest.jvg.CommonUtil;
import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.JVGScriptSupport;
import ru.nest.jvg.editor.JVGEditPane;
import ru.nest.jvg.editor.JVGEditor;
import ru.nest.jvg.macros.JVGMacrosCode;
import ru.nest.jvg.resource.ColorResource;
import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.resource.StrokeResource;
import ru.nest.jvg.shape.JVGGroup;
import ru.nest.jvg.shape.JVGGroupPath;
import ru.nest.jvg.shape.JVGShape;
import ru.nest.jvg.shape.paint.ColorDraw;
import ru.nest.jvg.shape.paint.Draw;
import ru.nest.jvg.shape.paint.OutlinePainter;
import ru.nest.jvg.shape.paint.Painter;
import ru.nest.jvg.undoredo.CompoundUndoRedo;
import ru.nest.jvg.undoredo.PropertyUndoRedo;

public class BasicStrokeAction extends JVGAction {
	public final static int TYPE_NONE = 0;

	public final static int TYPE_LINE_WIDTH = 1;

	public final static int TYPE_END_CAP = 2;

	public final static int TYPE_LINE_JOIN = 4;

	public final static int TYPE_MITER_LIMIT = 8;

	public final static int TYPE_DASH_ARRAY = 16;

	public final static int TYPE_DASH_PHASE = 32;

	public final static int TYPE_STROKE = TYPE_LINE_WIDTH | TYPE_END_CAP | TYPE_LINE_JOIN | TYPE_MITER_LIMIT | TYPE_DASH_ARRAY | TYPE_DASH_PHASE;

	public BasicStrokeAction() {
		this("outline-none", (Resource<BasicStroke>) null, TYPE_NONE);
	}

	public BasicStrokeAction(float[] dash_array) {
		this("outline-dash-array", new BasicStroke(1f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, dash_array, 0.0f), TYPE_DASH_ARRAY);
	}

	public BasicStrokeAction(float lineWidth) {
		this("outline-width", new BasicStroke(lineWidth), TYPE_LINE_WIDTH);
	}

	public BasicStrokeAction(BasicStroke stroke) {
		this(new StrokeResource<BasicStroke>(stroke));
	}

	public BasicStrokeAction(Resource<BasicStroke> stroke) {
		this("outline", stroke, TYPE_STROKE);
	}

	public BasicStrokeAction(String name, BasicStroke stroke, int type) {
		super(name);
		this.stroke = new StrokeResource<BasicStroke>(stroke);
		this.type = type;
	}

	public BasicStrokeAction(String name, Resource<BasicStroke> stroke, int type) {
		super(name);
		this.stroke = stroke;
		this.type = type;
	}

	private Resource<BasicStroke> stroke;

	private int type;

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGShape[] shapes = getShapes(e);
		if (shapes != null && shapes.length > 0) {
			JVGPane pane = getPane(e);
			CompoundUndoRedo edit = new CompoundUndoRedo(getName(), pane);

			for (JVGShape shape : shapes) {
				setStroke(pane, shape, stroke, getName(), edit, type);
			}

			if (!edit.isEmpty()) {
				pane.fireUndoableEditUpdate(new UndoableEditEvent(pane, edit));
				pane.repaint();

				if (type == TYPE_NONE) {
					appendMacrosCode(pane, "removeStroke(id);", JVGMacrosCode.ARG_ID);
				} else {
					appendMacrosCode(pane, "setStroke(id, %s, %s, %s, %s, %s, %s, %s);", JVGMacrosCode.ARG_ID, type, stroke.getResource().getLineWidth(), stroke.getResource().getEndCap(), stroke.getResource().getLineJoin(), stroke.getResource().getMiterLimit(), JVGScriptSupport.getArray(stroke.getResource().getDashArray()), stroke.getResource().getDashPhase());
				}
			}
		}
	}

	public static <S extends Stroke> S setStroke(JVGPane pane, JVGShape shape, Resource<S> stroke, String actionName, CompoundUndoRedo edit, int type) {
		if (shape instanceof JVGGroup || shape instanceof JVGGroupPath) {
			for (JVGComponent c : shape.getChildren()) {
				if (c instanceof JVGShape) {
					setStroke(pane, (JVGShape) c, stroke, actionName, edit, type);
				}
			}
			return null;
		}

		Painter painter = null;
		int count = shape.getPaintersCount();
		for (int i = count - 1; i >= 0; i--) {
			Painter p = shape.getPainter(i);
			if (p.getType() == Painter.OUTLINE) {
				painter = p;
				break;
			}
		}

		if (type == TYPE_NONE) {
			if (painter != null) {
				Class<?>[] c = { Painter.class };
				for (int i = count - 1; i >= 0; i--) {
					Painter p = shape.getPainter(i);
					if (p.getType() == Painter.OUTLINE) {
						shape.removePainter(p);
						Object[] o = { p };
						edit.add(new PropertyUndoRedo(actionName, pane, shape, "addPainter", c, o, "removePainter", c, o));
					}
				}
			}
		} else if (painter == null) {
			OutlinePainter defaultPainter = new OutlinePainter();
			defaultPainter.setStroke(stroke);
			defaultPainter.setPaint(getDefaultDraw(pane));

			shape.addPainter(defaultPainter);

			Class<?>[] p = { Painter.class };
			Object[] o = { defaultPainter };
			edit.add(new PropertyUndoRedo(actionName, pane, shape, "removePainter", p, o, "addPainter", p, o));
		} else if (painter instanceof OutlinePainter) {
			S s = setStroke(pane, (OutlinePainter) painter, stroke, actionName, edit, type);
			if (painter.getPaint() == null) {
				painter.setPaint(getDefaultDraw(pane));
				edit.add(new PropertyUndoRedo(actionName, pane, painter, "setPaint", null, painter.getPaint()));
			}
			return s;
		}
		return stroke != null ? stroke.getResource() : null;
	}

	public static Draw<?> getDefaultDraw(JVGPane pane) {
		Draw<?> draw = null;
		if (pane instanceof JVGEditPane) {
			JVGEditPane ep = (JVGEditPane) pane;
			JVGEditor editor = ep.getEditor();
			if (editor != null) {
				Painter painter = editor.getEditorActions().getOutlinePainter();
				if (painter != null) {
					draw = painter.getPaint();
				}
			}
		}
		if (draw == null) {
			draw = new ColorDraw(ColorResource.black);
		}
		return draw;
	}

	public static <S extends Stroke> S setStroke(JVGPane pane, OutlinePainter outliner, Resource<S> newStrokeResource, String actionName, CompoundUndoRedo edit, int type) {
		Stroke stroke = null;

		Stroke newStroke = newStrokeResource != null ? newStrokeResource.getResource() : null;
		Stroke oldStroke = null;
		if (outliner.getStroke() != null) {
			oldStroke = outliner.getStroke().getResource();
		}

		if (newStroke instanceof BasicStroke) {
			BasicStroke newBasicStroke = (BasicStroke) newStroke;
			BasicStroke oldBasicStroke = oldStroke instanceof BasicStroke ? (BasicStroke) oldStroke : null;

			float lineWidth = (oldBasicStroke == null || (type & TYPE_LINE_WIDTH) != 0) ? newBasicStroke.getLineWidth() : oldBasicStroke.getLineWidth();
			int endCap = (oldBasicStroke == null || (type & TYPE_END_CAP) != 0) ? newBasicStroke.getEndCap() : oldBasicStroke.getEndCap();
			int lineJoin = (oldBasicStroke == null || (type & TYPE_LINE_JOIN) != 0) ? newBasicStroke.getLineJoin() : oldBasicStroke.getLineJoin();
			float miterLimit = (oldBasicStroke == null || (type & TYPE_MITER_LIMIT) != 0) ? newBasicStroke.getMiterLimit() : oldBasicStroke.getMiterLimit();
			float[] dashArray = (oldBasicStroke == null || (type & TYPE_DASH_ARRAY) != 0) ? newBasicStroke.getDashArray() : oldBasicStroke.getDashArray();
			float dashPhase = (oldBasicStroke == null || (type & TYPE_DASH_PHASE) != 0) ? newBasicStroke.getDashPhase() : oldBasicStroke.getDashPhase();

			if (dashArray != null) {
				stroke = new BasicStroke(lineWidth, endCap, lineJoin, miterLimit, dashArray, dashPhase);
			} else {
				stroke = new BasicStroke(lineWidth, endCap, lineJoin, miterLimit);
			}

			boolean changed = oldBasicStroke == null || oldBasicStroke.getLineWidth() != lineWidth || oldBasicStroke.getEndCap() != endCap || oldBasicStroke.getLineJoin() != lineJoin || oldBasicStroke.getMiterLimit() != miterLimit || oldBasicStroke.getDashPhase() != dashPhase || !CommonUtil.equals(oldBasicStroke.getDashArray(), dashArray);
			if (changed) {
				if (oldBasicStroke != null && type != TYPE_STROKE) {
					newStrokeResource = new StrokeResource<S>((S) stroke);
				}

				edit.add(new PropertyUndoRedo(actionName, pane, outliner, "setStroke", Resource.class, outliner.getStroke(), newStrokeResource));
				outliner.setStroke(newStrokeResource);
			}
		}
		return (S) stroke;
	}
}
