package ru.nest.jvg.parser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Constructor;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.parser.svg.SVGBuilder;
import ru.nest.jvg.resource.JVGResources;

public abstract class JVGBuilder implements JVGBuilderInterface {
	public static String className = "ru.nest.jvg.parser.versions.JVGBuilder";

	public static JVGBuilder create(DocumentFormat format) {
		try {
			switch (format) {
				case svg:
					return new SVGBuilder();
				default:
					String version = JVGVersion.VERSION.replace('.', '_');
					Class<?> clazz = Class.forName(className + "_" + version);
					Constructor<?> constructor = clazz.getConstructor();
					return (JVGBuilder) constructor.newInstance();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	protected JVGResources resources;

	protected JVGPane pane;

	public JVGBuilder() {
		resources = new JVGResources();
	}

	@Override
	public void setDocument(JVGPane pane) {
		this.pane = pane;
	}

	@Override
	public void build(JVGPane pane, OutputStream os) throws JVGParseException {
		setDocument(pane);
		build(pane.getRoot().getChildren(), os);
	}

	@Override
	public void build(JVGComponent[] components, OutputStream os) throws JVGParseException {
		try {
			Element element = build(components);
			Document document = new Document(element);

			XMLOutputter out = new XMLOutputter();
			out.getFormat().setIndent("\t");
			out.getFormat().setLineSeparator("\n");
			out.output(document, os);
		} catch (IOException exc) {
			throw new JVGParseException(exc.getMessage(), exc);
		}
	}

	@Override
	public void build(JVGComponent[] components, Writer writer) throws JVGParseException {
		try {
			Element element = build(components);
			Document document = new Document(element);

			XMLOutputter out = new XMLOutputter();
			out.output(document, writer);
		} catch (IOException exc) {
			throw new JVGParseException(exc.getMessage(), exc);
		}
	}

	@Override
	public String build(JVGComponent[] components, String codec) throws JVGParseException {
		try {
			byte[] bytes = buildByteArray(components);
			return new String(bytes, codec);
		} catch (UnsupportedEncodingException exc) {
			throw new JVGParseException(exc.getMessage(), exc);
		}
	}

	private byte[] buildByteArray(JVGComponent[] components) throws JVGParseException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		build(components, os);
		return os.toByteArray();
	}
}
