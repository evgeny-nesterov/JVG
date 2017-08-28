package ru.nest.jvg.shape.resources;

import java.util.HashMap;

import org.jdom2.Element;

import ru.nest.jvg.shape.resources.filter.JVGImageFilterResourceManager;

public abstract class JVGResourceManager<V extends JVGResource> {
	public final static int IMAGE_FILTER = 0;

	private static HashMap<Integer, JVGResourceManager<?>> map = new HashMap<Integer, JVGResourceManager<?>>();
	{
		map.put(IMAGE_FILTER, new JVGImageFilterResourceManager<V>());
	}

	public static JVGResourceManager<?> getManager(int type) {
		return map.get(type);
	}

	public abstract Class<V> getClass(Element e);

	public abstract String getName(V resource);

	public V loadResource(Element e) {
		Class<V> clazz = getClass(e);
		V resource = null;
		if (resource != null) {
			try {
				resource = clazz.newInstance();
				resource.load(e);
			} catch (IllegalAccessException exc) {
				exc.printStackTrace();
			} catch (InstantiationException exc) {
				exc.printStackTrace();
			}
		}
		return resource;
	}

	public void saveResource(V resource, Element e) {
		if (resource != null && e != null) {
			String name = getName(resource);
			if (name != null) {
				e.setAttribute("name", name);
				resource.save(e);
			}
		}
	}
}
