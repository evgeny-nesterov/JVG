package ru.nest.jvg.shape.resources;

import org.jdom2.Element;

public interface JVGResource {
	public void load(Element e);

	public void save(Element e);
}
