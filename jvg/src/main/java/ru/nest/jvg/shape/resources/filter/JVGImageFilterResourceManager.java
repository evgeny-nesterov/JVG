package ru.nest.jvg.shape.resources.filter;

import org.jdom2.Element;

import ru.nest.jvg.shape.resources.JVGResource;
import ru.nest.jvg.shape.resources.JVGResourceManager;

public class JVGImageFilterResourceManager<V extends JVGResource> extends JVGResourceManager<V> {
	@Override
	public Class<V> getClass(Element e) {
		if (e != null) {
			String type = e.getAttributeValue("name");
			if (type != null) {
				type = type.trim().toLowerCase();
				if (GrayFilter.NAME.equals(type)) {
					return (Class<V>) GrayFilter.class;
				}
			}
		}
		return null;
	}

	@Override
	public String getName(V resource) {
		if (resource instanceof GrayFilter) {
			return GrayFilter.NAME;
		}
		return null;
	}
}
