package ru.nest.jvg.editor;

import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.menu.WMenuItem;

import ru.nest.jvg.action.JVGAction;
import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.shape.paint.ColorDraw;
import ru.nest.jvg.shape.paint.Draw;

public class ChooseColorButton extends AbstractChooseColorButton {
	private ActionCreator actionCreator;

	public ChooseColorButton(ActionCreator actionCreator, Icon icon, Draw draw) {
		this.actionCreator = actionCreator;
		init(actionCreator.createAction(draw), icon, draw);
	}

	@Override
	protected AbstractButton createColorButton(Draw draw) {
		return new ColorButton(draw, getAction(draw));
	}

	@Override
	protected AbstractButton createChooseButton() {
		return new WMenuItem();
	}

	@Override
	public void setFiller(Resource color) {
		Action action = getAction(new ColorDraw(color));
		setCurrentAction(action, new ColorDraw(color));
		action.actionPerformed(null);
	}

	private Map<Draw, Action> map = null;

	public Action getAction(Draw draw) {
		if (map == null) {
			map = new HashMap<>();
		}

		if (draw != null) {
			Action action = map.get(draw);
			if (action == null && actionCreator != null) {
				action = actionCreator.createAction(draw);
				map.put(draw, action);
			}
			return action;
		} else {
			return null;
		}
	}

	public static interface ActionCreator {
		public JVGAction createAction(Draw draw);
	}
}
