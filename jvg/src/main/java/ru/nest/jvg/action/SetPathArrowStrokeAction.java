package ru.nest.jvg.action;

import ru.nest.jvg.JVGPane;
import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.resource.StrokeResource;
import ru.nest.jvg.undoredo.CompoundUndoRedo;
import ru.nest.strokes.ArrowStroke;

public class SetPathArrowStrokeAction extends SetPathStrokeAction<ArrowStroke> {
	public final static int TYPE_DIRECTION = 1;

	public final static int TYPE_WIDTH = 2;

	public final static int TYPE_END_CAP_WIDTH = 4;

	public final static int TYPE_END_CAP_LENGTH = 8;

	public final static int TYPE_ARROW = TYPE_WIDTH | TYPE_END_CAP_WIDTH | TYPE_END_CAP_LENGTH;

	public final static int TYPE_STROKE = TYPE_DIRECTION | TYPE_WIDTH | TYPE_END_CAP_WIDTH | TYPE_END_CAP_LENGTH;

	public static String getName(int type) {
		String name = "pathstroke-arrow";
		switch (type) {
			case TYPE_DIRECTION:
				name += "-direction";
				break;
			case TYPE_WIDTH:
				name += "-width";
				break;
			case TYPE_END_CAP_WIDTH:
				name += "-end-cap-width";
				break;
			case TYPE_END_CAP_LENGTH:
				name += "-end-cap-length";
				break;
			case TYPE_WIDTH | TYPE_END_CAP_WIDTH | TYPE_END_CAP_LENGTH:
				break;
		}
		return name;
	}

	public SetPathArrowStrokeAction() {
	}

	public SetPathArrowStrokeAction(int direction) {
		super(getName(TYPE_DIRECTION), new ArrowStroke(14, 12, 6, direction), TYPE_DIRECTION);
	}

	public SetPathArrowStrokeAction(double width, double endCapWidth, double endCapLength) {
		super(getName(TYPE_ARROW), new ArrowStroke(width, endCapWidth, endCapLength, ArrowStroke.DIRECTION_DIRECT), TYPE_ARROW);
	}

	public SetPathArrowStrokeAction(double value, int type) {
		super(getName(type));
		ArrowStroke stroke = null;
		switch (type) {
			case TYPE_WIDTH:
				stroke = new ArrowStroke(value, 12, 6, ArrowStroke.DIRECTION_DIRECT);
				break;
			case TYPE_END_CAP_WIDTH:
				stroke = new ArrowStroke(14, value, 6, ArrowStroke.DIRECTION_DIRECT);
				break;
			case TYPE_END_CAP_LENGTH:
				stroke = new ArrowStroke(14, 12, value, ArrowStroke.DIRECTION_DIRECT);
				break;
		}
		set(stroke, type);
	}

	@Override
	public Resource<ArrowStroke> getStroke(JVGPane pane, Resource<ArrowStroke> oldStrokeResource, Resource<ArrowStroke> newStrokeResource, String actionName, CompoundUndoRedo edit, int type) {
		ArrowStroke oldStroke = oldStrokeResource.getResource();
		ArrowStroke newStroke = newStrokeResource.getResource();

		int direction = (oldStroke == null || (type & TYPE_DIRECTION) != 0) ? newStroke.getType() : oldStroke.getType();
		double width = (oldStroke == null || (type & TYPE_WIDTH) != 0) ? newStroke.getWidth() : oldStroke.getWidth();
		double endCapWidth = (oldStroke == null || (type & TYPE_END_CAP_WIDTH) != 0) ? newStroke.getArrowWidth() : oldStroke.getArrowWidth();
		double endCapLength = (oldStroke == null || (type & TYPE_END_CAP_LENGTH) != 0) ? newStroke.getArrowLength() : oldStroke.getArrowLength();

		ArrowStroke stroke = new ArrowStroke(width, endCapWidth, endCapLength, direction);
		boolean changed = oldStroke == null;
		changed |= oldStroke != null && oldStroke.getType() != direction;
		changed |= oldStroke != null && oldStroke.getWidth() != width;
		changed |= oldStroke != null && oldStroke.getArrowWidth() != endCapWidth;
		changed |= oldStroke != null && oldStroke.getArrowLength() != endCapLength;

		if (changed) {
			return new StrokeResource<ArrowStroke>(stroke);
		} else {
			return null;
		}
	}
}
