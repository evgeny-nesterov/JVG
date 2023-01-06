package ru.nest.jvg.shape.resources;

import org.jdom2.Element;

public interface JVGResource {
	void load(Element e);

	void save(Element e);
}
