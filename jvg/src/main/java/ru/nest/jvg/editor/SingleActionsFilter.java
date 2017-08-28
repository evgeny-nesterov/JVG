package ru.nest.jvg.editor;

import ru.nest.jvg.actionarea.ActionsFilter;
import ru.nest.jvg.actionarea.JVGActionArea;

public class SingleActionsFilter implements ActionsFilter {
	public SingleActionsFilter(Class<?> clazz) {
		this.clazz = clazz;
	}

	private Class<?> clazz;

	@Override
	public boolean pass(JVGActionArea a) {
		return clazz.isAssignableFrom(a.getClass());
	}
}
