package ru.nest.jvg.parser;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Map;

import org.jdom2.Element;

import ru.nest.jvg.JVGContainer;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.resource.JVGResources;
import ru.nest.jvg.resource.ScriptResource;

public interface JVGParserInterface {
	JVGResources getResources();

	Dimension getDocumentSize();

	Color getDocumentColor();

	Map<String, ScriptResource> getDocumentScripts();

	void init(JVGPane pane);

	void parse(Element rootElement, JVGContainer parent) throws JVGParseException;
}
