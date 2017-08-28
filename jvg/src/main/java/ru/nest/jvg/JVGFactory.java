package ru.nest.jvg;

import java.lang.reflect.Constructor;

public abstract class JVGFactory {
	public static String defaultClassName = "ru.nest.jvg.JVGDefaultFactory";

	public static String editorClassName = "ru.nest.jvg.editor.JVGEditorFactory";

	public abstract <V extends JVGComponent> V createComponent(Class<V> clazz, Object... params);

	public static JVGFactory createDefault() {
		return create(defaultClassName);
	}

	public static JVGFactory createEditor() {
		return create(editorClassName);
	}

	public static JVGFactory create(String className) {
		try {
			Class<?> clazz = Class.forName(className);
			Constructor<?> constructor = clazz.getDeclaredConstructor();
			return (JVGFactory) constructor.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Can't create jvg factory.", e);
		}
	}
}
