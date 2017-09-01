package ru.nest.jvg.parser;

import java.io.OutputStream;
import java.io.Writer;

import org.jdom2.Element;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGPane;

public interface JVGBuilderInterface {
	public void setDocument(JVGPane pane);

	public void build(JVGPane pane, OutputStream os) throws JVGParseException;

	public void build(JVGComponent[] components, OutputStream os) throws JVGParseException;

	public void build(JVGComponent[] components, Writer writer) throws JVGParseException;

	public String build(JVGComponent[] components, String codec) throws JVGParseException;

	public Element build(JVGComponent[] components) throws JVGParseException;
}
