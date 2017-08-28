package ru.nest.jvg.action;

import java.awt.BasicStroke;

import ru.nest.jvg.JVGPane;
import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.resource.StrokeResource;
import ru.nest.jvg.undoredo.CompoundUndoRedo;
import ru.nest.util.CommonUtil;

public class SetPathBasicStrokeAction extends SetPathStrokeAction<BasicStroke> {
	public final static int TYPE_LINE_WIDTH = 1;

	public final static int TYPE_END_CAP = 2;

	public final static int TYPE_LINE_JOIN = 4;

	public final static int TYPE_MITER_LIMIT = 8;

	public final static int TYPE_DASH_ARRAY = 16;

	public final static int TYPE_DASH_PHASE = 32;

	public final static int TYPE_STROKE = TYPE_LINE_WIDTH | TYPE_END_CAP | TYPE_LINE_JOIN | TYPE_MITER_LIMIT | TYPE_DASH_ARRAY | TYPE_DASH_PHASE;

	public SetPathBasicStrokeAction() {
	}

	public SetPathBasicStrokeAction(float[] dash_array) {
		super("pathstroke-basic-dash-array", new BasicStroke(1f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, dash_array, 0.0f), TYPE_DASH_ARRAY);
	}

	public SetPathBasicStrokeAction(float lineWidth) {
		super("pathstroke-basic-width", new BasicStroke(lineWidth), TYPE_LINE_WIDTH);
	}

	@Override
	public Resource<BasicStroke> getStroke(JVGPane pane, Resource<BasicStroke> oldStrokeResource, Resource<BasicStroke> newStrokeResource, String actionName, CompoundUndoRedo edit, int type) {
		BasicStroke oldStroke = oldStrokeResource.getResource();
		BasicStroke newStroke = newStrokeResource.getResource();

		float lineWidth = (oldStroke == null || (type & TYPE_LINE_WIDTH) != 0) ? newStroke.getLineWidth() : oldStroke.getLineWidth();
		int endCap = (oldStroke == null || (type & TYPE_END_CAP) != 0) ? newStroke.getEndCap() : oldStroke.getEndCap();
		int lineJoin = (oldStroke == null || (type & TYPE_LINE_JOIN) != 0) ? newStroke.getLineJoin() : oldStroke.getLineJoin();
		float miterLimit = (oldStroke == null || (type & TYPE_MITER_LIMIT) != 0) ? newStroke.getMiterLimit() : oldStroke.getMiterLimit();
		float[] dashArray = (oldStroke == null || (type & TYPE_DASH_ARRAY) != 0) ? newStroke.getDashArray() : oldStroke.getDashArray();
		float dashPhase = (oldStroke == null || (type & TYPE_DASH_PHASE) != 0) ? newStroke.getDashPhase() : oldStroke.getDashPhase();

		BasicStroke stroke = new BasicStroke(lineWidth, endCap, lineJoin, miterLimit, dashArray, dashPhase);
		boolean changed = oldStroke == null;
		changed |= oldStroke != null && oldStroke.getLineWidth() != lineWidth;
		changed |= oldStroke != null && oldStroke.getEndCap() != endCap;
		changed |= oldStroke != null && oldStroke.getMiterLimit() != miterLimit;
		changed |= oldStroke != null && !CommonUtil.equals(oldStroke.getDashArray(), dashArray);
		changed |= oldStroke != null && oldStroke.getDashPhase() != dashPhase;

		if (changed) {
			return new StrokeResource<BasicStroke>(stroke);
		} else {
			return null;
		}
	}
}
