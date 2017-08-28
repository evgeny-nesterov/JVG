package ru.nest.jvg.editor;

import ru.nest.jvg.actionarea.ActionsFilter;
import ru.nest.jvg.actionarea.JVGActionArea;

public class DisableActionsFilter implements ActionsFilter {
	private DisableActionsFilter() {
	}

	private static ActionsFilter filter = new DisableActionsFilter();

	public static ActionsFilter getInstance() {
		return filter;
	}

	@Override
	public boolean pass(JVGActionArea a) {
		return false;
	}
}
