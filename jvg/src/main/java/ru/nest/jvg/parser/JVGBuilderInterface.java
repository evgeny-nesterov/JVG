package ru.nest.jvg.parser;

import java.io.OutputStream;
import java.io.Writer;

import org.jdom2.Element;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGPane;

public interface JVGBuilderInterface {
	void setDocument(JVGPane pane);

	void build(JVGPane pane, OutputStream os) throws JVGParseException;

	void build(JVGComponent[] components, OutputStream os) throws JVGParseException;

	void build(JVGComponent[] components, Writer writer) throws JVGParseException;

	String build(JVGComponent[] components, String codec) throws JVGParseException;

	Element build(JVGComponent[] components) throws JVGParseException;
}
