package ru.nest.jvg;

import java.util.HashSet;
import java.util.Set;

public class DefaultFilter implements Filter {
	public DefaultFilter() {
	}

	public DefaultFilter(boolean ignoreVisibility) {
		setIgnoreVisibility(ignoreVisibility);
	}

	public DefaultFilter(boolean ignoreVisibility, JVGComponent excludeComponent) {
		this(ignoreVisibility);
		exclude(excludeComponent);
	}

	public DefaultFilter(boolean ignoreVisibility, JVGComponent excludeComponent, Class<?> clazz) {
		this(ignoreVisibility, excludeComponent);
		addClass(clazz);
	}

	private boolean ignoreVisibility = false;

	public void setIgnoreVisibility(boolean ignoreVisibility) {
		this.ignoreVisibility = ignoreVisibility;
	}

	private Set<JVGComponent> exclude = null;

	public void exclude(JVGComponent c) {
		if (c != null) {
			if (exclude == null) {
				exclude = new HashSet<>();
			}
			exclude.add(c);
		}
	}

	private Set<Class<?>> classes = null;

	public void addClass(Class<?> c) {
		if (c != null) {
			if (classes == null) {
				classes = new HashSet<>();
			}
			classes.add(c);
		}
	}

	@Override
	public boolean pass(JVGComponent component) {
		if (!ignoreVisibility && !component.isVisible()) {
			return false;
		}
		if (exclude != null && exclude.contains(component)) {
			return false;
		}
		if (classes != null && !classes.contains(component.getClass())) {
			return false;
		}
		return true;
	}
}
