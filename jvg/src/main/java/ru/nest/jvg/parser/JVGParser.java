package ru.nest.jvg.parser;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import ru.nest.jvg.JVGContainer;
import ru.nest.jvg.JVGFactory;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.JVGRoot;
import ru.nest.jvg.parser.svg.SVGParser;
import ru.nest.jvg.resource.JVGResources;
import ru.nest.jvg.resource.ScriptResource;

public class JVGParser implements JVGParserInterface {
	public static String className = "ru.nest.jvg.parser.versions.JVGParser";

	private JVGFactory factory;

	private JVGResources resources;

	private JVGParserInterface parser;

	public JVGParser() {
		this(null);
	}

	public JVGParser(JVGFactory factory) {
		this(factory, null);
	}

	public JVGParser(JVGFactory factory, JVGResources resources) {
		if (resources == null) {
			resources = new JVGResources();
		}
		if (factory == null) {
			factory = JVGFactory.createDefault();
		}
		this.factory = factory;
		this.resources = resources;
	}

	@Override
	public JVGResources getResources() {
		return parser.getResources();
	}

	@Override
	public Map<String, ScriptResource> getDocumentScripts() {
		return parser.getDocumentScripts();
	}

	public void init(JVGPane pane) {
		Dimension documentSize = parser.getDocumentSize();
		if (documentSize == null) {
			Rectangle2D bounds = pane.getRoot().getRectangleBounds();
			documentSize = new Dimension((int) (bounds.getX() + bounds.getWidth()), (int) (bounds.getY() + bounds.getHeight()));
		}
		pane.setDocumentSize(documentSize);

		if (parser.getDocumentColor() != null) {
			pane.setBackground(parser.getDocumentColor());
		}

		Map<String, ScriptResource> scripts = getDocumentScripts();
		if (scripts != null) {
			for (String scriptName : scripts.keySet()) {
				pane.putClientProperty(scriptName, scripts.get(scriptName));
			}
		}

		parser.init(pane);
	}

	public JVGRoot parse(String content) throws JVGParseException {
		return parse(new StringReader(content));
	}

	public JVGRoot parse(File file) throws JVGParseException {
		try {
			return parse(new FileInputStream(file));
		} catch (FileNotFoundException exc) {
			throw new JVGParseException(exc.getMessage(), exc);
		}
	}

	public JVGRoot parse(InputStream is) throws JVGParseException {
		JVGRoot root = factory.createComponent(JVGRoot.class, new Object[] { null });
		parse(is, root);
		return root;
	}

	public JVGRoot parse(Reader is) throws JVGParseException {
		JVGRoot root = factory.createComponent(JVGRoot.class, new Object[] { null });
		parse(is, root);
		return root;
	}

	public JVGRoot parse(Element rootElement) throws JVGParseException {
		JVGRoot root = factory.createComponent(JVGRoot.class, new Object[] { null });
		parse(rootElement, root);
		return root;
	}

	public void parse(String s, JVGContainer parent) throws JVGParseException {
		parse(new StringReader(s), parent);
	}

	//	public void parse(InputStream is, JVGContainer parent) throws JVGParseException {
	//		String[] encodings = { "UTF-8", "UTF-16", "ISO-8859-1" };
	//		UnsupportedEncodingException e = null;
	//		for (String encoding : encodings) {
	//			try {
	//				InputStreamReader reader = new InputStreamReader(is, encoding);
	//				parse(reader, parent);
	//				break;
	//			} catch (UnsupportedEncodingException exc) {
	//				e = exc;
	//			}
	//		}
	//
	//		if (e != null) {
	//			throw new JVGParseException(e.getMessage(), e);
	//		}
	//	}

	public void parse(File file, JVGContainer parent) throws JVGParseException {
		try {
			parse(new FileInputStream(file), parent);
		} catch (FileNotFoundException exc) {
			throw new JVGParseException(exc.getMessage(), exc);
		}
	}

	@Override
	public Dimension getDocumentSize() {
		return parser.getDocumentSize();
	}

	@Override
	public Color getDocumentColor() {
		return parser.getDocumentColor();
	}

	public void parse(Reader is, JVGContainer parent) throws JVGParseException {
		try {
			SAXBuilder builder = new SAXBuilder();
			builder.setValidation(false);
			builder.setFeature("http://xml.org/sax/features/validation", false);
			builder.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
			builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

			Document xml = builder.build(is);
			Element rootElement = xml.getRootElement();
			parse(rootElement, parent);
		} catch (IOException exc) {
			throw new JVGParseException(exc.getMessage(), exc);
		} catch (JDOMException exc) {
			throw new JVGParseException(exc.getMessage(), exc);
		}
	}

	public void parse(InputStream is, JVGContainer parent) throws JVGParseException {
		try {
			SAXBuilder builder = new SAXBuilder();
			builder.setValidation(false);
			builder.setFeature("http://xml.org/sax/features/validation", false);
			builder.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
			builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

			Document xml = builder.build(is);
			Element rootElement = xml.getRootElement();
			parse(rootElement, parent);
		} catch (IOException exc) {
			throw new JVGParseException(exc.getMessage(), exc);
		} catch (JDOMException exc) {
			throw new JVGParseException(exc.getMessage(), exc);
		}
	}

	@Override
	public void parse(Element rootElement, JVGContainer parent) throws JVGParseException {
		String format = rootElement.getName().toLowerCase();
		if ("jvg".equals(format)) {
			String version = rootElement.getAttributeValue("version", JVGVersion.VERSION);
			version = version.replace('.', '_');
			try {
				Class<?> clazz = Class.forName(className + "_" + version);
				Constructor<?> constructor = clazz.getConstructor(JVGFactory.class, JVGResources.class);
				parser = (JVGParserInterface) constructor.newInstance(factory, resources);
				parser.parse(rootElement, parent);
			} catch (Exception e) {
				parser = null;
				e.printStackTrace();
				throw new JVGParseException("Format of version " + version + " is not supported.", e);
			}
		} else if ("svg".equals(format)) {
			parser = new SVGParser(factory, resources);
			parser.parse(rootElement, parent);
		} else if ("toi".equals(format)) {
			parser = new TOIParser(factory, resources);
			parser.parse(rootElement, parent);
		} else {
			throw new JVGParseException("Unsupported format " + format);
		}
	}

	public static void main(String[] args) {
		try {
			for (File f : new File("C:/Users/john/Dropbox/Satis Soft/Рабочие материалы/Common/ЦОДД/Контроллеры/Подложки svg").listFiles()) {
				System.out.println(f.getName());
				if (f.getName().endsWith(".svg")) {
					JVGParser p = new JVGParser(null);
					p.parse(f);
				}
			}
		} catch (JVGParseException e) {
			e.printStackTrace();
		}
	}
}
