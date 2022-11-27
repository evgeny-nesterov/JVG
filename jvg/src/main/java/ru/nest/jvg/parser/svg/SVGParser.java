package ru.nest.jvg.parser.svg;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.gradient.Gradient;
import javax.swing.gradient.Gradient.GradientUnitsType;
import javax.swing.gradient.LinearGradient;
import javax.swing.gradient.MultipleGradientPaint;
import javax.swing.gradient.RadialGradient;

import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.Text;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;

import com.steadystate.css.parser.CSSOMParser;
import com.steadystate.css.parser.SACParserCSS3;

import ru.nest.fonts.Fonts;
import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGComponentType;
import ru.nest.jvg.JVGContainer;
import ru.nest.jvg.JVGFactory;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.geom.MutableGeneralPath;
import ru.nest.jvg.parser.JVGParseException;
import ru.nest.jvg.parser.JVGParseUtil;
import ru.nest.jvg.parser.JVGParserInterface;
import ru.nest.jvg.resource.ColorResource;
import ru.nest.jvg.resource.FontResource;
import ru.nest.jvg.resource.ImageResource;
import ru.nest.jvg.resource.JVGResources;
import ru.nest.jvg.resource.LinearGradientResource;
import ru.nest.jvg.resource.RadialGradientResource;
import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.resource.ScriptResource;
import ru.nest.jvg.resource.StrokeResource;
import ru.nest.jvg.shape.JVGGroup;
import ru.nest.jvg.shape.JVGImage;
import ru.nest.jvg.shape.JVGPath;
import ru.nest.jvg.shape.JVGShape;
import ru.nest.jvg.shape.JVGTextField;
import ru.nest.jvg.shape.JVGTextField.TextAnchor;
import ru.nest.jvg.shape.paint.ColorDraw;
import ru.nest.jvg.shape.paint.Draw;
import ru.nest.jvg.shape.paint.FillPainter;
import ru.nest.jvg.shape.paint.LinearGradientDraw;
import ru.nest.jvg.shape.paint.OutlinePainter;
import ru.nest.jvg.shape.paint.Painter;
import ru.nest.jvg.shape.paint.RadialGradientDraw;
import sun.misc.BASE64Decoder;

public class SVGParser implements JVGParserInterface {
	private final static Namespace xlink = Namespace.getNamespace("http://www.w3.org/1999/xlink");

	private JVGFactory factory;

	private JVGResources resources;

	private Dimension documentSize;

	private Map<String, Object> allResources = new HashMap<>();

	private Map<String, Map<String, String>> cssClasses = new HashMap<>();

	private Map<String, Map<String, String>> cssTags = new HashMap<>();

	private class StackElement {
		Element e;

		Map<String, String> style;

		StackElement(Element e) {
			this.e = e;
			this.style = parseStyle(e);
		}

		@Override
		public String toString() {
			return e + ", style: " + style;
		}
	}

	private LinkedList<StackElement> stack = new LinkedList<>();

	private Rectangle2D lastTextBounds = null;

	private JVGPane pane;

	public SVGParser(JVGFactory factory, JVGResources resources) {
		this.factory = factory;
		this.resources = resources;
	}

	@Override
	public JVGResources getResources() {
		return null;
	}

	@Override
	public Dimension getDocumentSize() {
		return documentSize;
	}

	@Override
	public Color getDocumentColor() {
		return null;
	}

	@Override
	public Map<String, ScriptResource> getDocumentScripts() {
		return null;
	}

	@Override
	public void parse(Element rootElement, JVGContainer parent) throws JVGParseException {
		String widthValue = rootElement.getAttributeValue("width");
		Float width = null;
		if (widthValue != null) {
			Float w = parseLength(widthValue, null);
			if (w != null) {
				if (widthValue.endsWith("%")) {
					width = pane != null ? (float) (w * pane.getDocumentSize().getWidth()) : 700f;
				} else {
					width = w;
				}
			}
		}

		Float height = null;
		String heightValue = rootElement.getAttributeValue("height");
		if (heightValue != null) {
			Float h = parseLength(heightValue, null);
			if (h != null) {
				if (widthValue.endsWith("%")) {
					height = pane != null ? (float) (h * pane.getDocumentSize().getHeight()) : 500f;
				} else {
					height = h;
				}
			}
		}

		float viewX = 0;
		float viewY = 0;
		Float viewWidth = null;
		Float viewHeight = null;
		String viewBox = rootElement.getAttributeValue("viewBox");
		if (viewBox != null) {
			float[] array = JVGParseUtil.getFloatArray(viewBox, " ,");
			if (array.length == 4) {
				viewX = array[0];
				viewY = array[1];
				viewWidth = array[2];
				viewHeight = array[3];
			}
		}
		width = defaultValue(700, width, viewWidth);
		viewWidth = defaultValue(700, viewWidth, width);
		height = defaultValue(500, height, viewHeight);
		viewHeight = defaultValue(500, viewHeight, height);

		documentSize = new Dimension(viewWidth.intValue(), viewHeight.intValue());

		//		String preserveAspectRatio = rootElement.getAttributeValue("preserveAspectRatio");
		//		// TODO
		//
		//		float scaleX = width / (float) viewWidth;
		//		float scaleY = height / (float) viewHeight;
		//		AffineTransform transform = AffineTransform.getTranslateInstance(viewX, viewY);
		//		transform.scale(scaleX, scaleY);
		//		if (pane != null) {
		//			pane.setTransform(transform);
		//		}

		parseChildren(rootElement, parent);
	}

	private float defaultValue(float defaultValue, Float... values) {
		for (Float value : values) {
			if (value != null) {
				return value;
			}
		}
		return defaultValue;
	}

	private <C> C parseComponent(Element e, JVGContainer parent) throws JVGParseException {
		String type = e.getName();
		stack.addFirst(new StackElement(e));

		Object component = null;
		if ("path".equals(type)) {
			component = parsePath(e);
		} else if ("g".equals(type)) {
			component = parseGroup(e);
		} else if ("rect".equals(type)) {
			component = parseRect(e, parent);
		} else if ("polygon".equals(type)) {
			component = parsePolygon(e);
		} else if ("text".equals(type)) {
			component = parseText(e, parent);
		} else if ("tspan".equals(type)) {
			component = parseText(e, parent);
		} else if ("polyline".equals(type)) {
			component = parsePolyline(e);
		} else if ("line".equals(type)) {
			component = parseLine(e, parent);
		} else if ("circle".equals(type)) {
			component = parseCircle(e, parent);
		} else if ("ellipse".equals(type)) {
			component = parseEllipse(e, parent);
		} else if ("image".equals(type)) {
			component = parseImage(e, parent);
		} else if ("defs".equals(type)) {
			parseDefs(e, parent);
		} else if ("linearGradient".equals(type)) {
			component = parseLinearGradient(e, parent);
		} else if ("radialGradient".equals(type)) {
			component = parseRadialGradient(e, parent);
		} else if ("clipPath".equals(type)) {
			component = parseClipPath(e, parent);
		} else if ("use".equals(type)) {
			return (C) parseUse(e, parent);
		} else if ("style".equals(type)) {
			parseCss(e.getText());
		} else if ("marker".equals(type)) {
			// TODO marker-end for pathes
		} else if ("font".equals(type)) {
			// TODO
		} else if ("filter".equals(type)) {
			// TODO
		} else if ("metadata".equals(type)) {
			// TODO
		} else if ("namedview".equals(type)) {
			// TODO
		} else if ("perspective".equals(type)) {
			// TODO
		} else if ("title".equals(type)) {
			// TODO
		} else {
			System.out.println("svg type not supported: " + type);
		}

		if (component != null) {
			String compId = e.getAttributeValue("id");
			if (compId != null) {
				allResources.put(compId, component);

				if (component instanceof Resource) {
					Resource resource = (Resource) component;
					resource.setName(compId);
					resources.addResource(resource);
				} else if (component instanceof JVGShape) {
					((JVGShape) component).setName(compId);
				}
			}
		}

		if (component instanceof JVGShape) {
			parseShape(e, (JVGShape) component, parent);
		}

		stack.removeFirst();
		return (C) component;
	}

	private void parseShape(Element e, JVGShape shape, JVGContainer parent) throws JVGParseException {
		shape.setClientProperty("svg:element", e);
		if ("none".equals(e.getAttributeValue("display"))) {
			shape.setVisible(false);
		}

		String compId = e.getAttributeValue("id");
		if (compId != null) {
			shape.setName(compId);
		}

		shape.setAntialias(true);
		parsePainters(e, shape, defaulFill);

		double opacity = JVGParseUtil.getDouble(s("opacity", e, null), 1);
		if (opacity != 1) {
			shape.setAlfa((int) (255 * opacity));
		}

		String clipValue = e.getAttributeValue("clip-path");
		Object clip = getResource(clipValue);
		if (clip instanceof List) {
			for (Object o : (List) clip) {
				shape.add((JVGShape) o);
			}
		}

		parseTransform(shape, e, parent);
	}

	private void parsePainters(Element e, JVGShape shape, Draw defaulFill) throws JVGParseException {
		List<Painter> painters = new ArrayList<>();
		FillPainter fill = parseFill(shape, e, painters, defaulFill);
		if (fill != null) {
			shape.setFill(true);
		} else if (shape.isFill()) {
			fill = new FillPainter(ColorResource.black);
		}

		OutlinePainter outline = parseOutline(shape, e, painters);
		if (!shape.isFill() && outline == null) {
			outline = transparentOutline;
		}

		if (fill != null) {
			painters.add(fill);
		}
		if (outline != null) {
			painters.add(outline);
		}
		shape.setPainters(painters);
	}

	private AffineTransform parseTransform(String value) {
		return parseTransform(value, null);
	}

	private AffineTransform parseTransform(String value, AffineTransform defaultValue) {
		return parseTransform(value, 0, defaultValue);
	}

	private AffineTransform parseTransform(String value, int offset, AffineTransform defaultValue) {
		if (value != null) {
			AffineTransform transform = null;
			String[] types = { "matrix", "rotate", "translate", "scale" };
			while (offset < value.length()) {
				int index = -1;
				String type = null;
				for (String t : types) {
					String startPattern = t + "(";
					int i = value.indexOf(startPattern, offset);
					if (i != -1 && (index == -1 || i < index)) {
						index = i + startPattern.length();
						type = t;
					}
				}

				if (index != -1) {
					int index2 = value.indexOf(")", index);
					if (index2 == -1) {
						index2 = value.length();
						offset = index2;
					} else {
						offset = index2 + 1;
					}
					String data = value.substring(index, index2);
					double[] m = JVGParseUtil.getDoubleArray(data, " ,");
					AffineTransform t = getTransform(type, m);
					if (transform == null) {
						transform = t;
					} else {
						transform.concatenate(t);
					}
					continue;
				}
				break;
			}
			if (transform != null) {
				return transform;
			}
		}
		return defaultValue;
	}

	private AffineTransform getTransform(String type, double[] m) {
		if (type.equals("matrix")) {
			return new AffineTransform(m);
		} else if (type.equals("rotate")) {
			if (m.length == 1) {
				return AffineTransform.getRotateInstance(Math.toRadians(m[0]));
			} else if (m.length == 2) {
				return AffineTransform.getRotateInstance(m[0], m[1]);
			} else if (m.length == 3) {
				return AffineTransform.getRotateInstance(Math.toRadians(m[0]), m[1], m[2]);
			} else if (m.length == 4) {
				return AffineTransform.getRotateInstance(m[0], m[1], m[2], m[4]);
			}
		} else if (type.equals("translate")) {
			if (m.length == 2) {
				return AffineTransform.getTranslateInstance(m[0], m[1]);
			} else {
				return AffineTransform.getTranslateInstance(m[0], 0);
			}
		} else if (type.equals("translateX")) {
			return AffineTransform.getTranslateInstance(m[0], 0);
		} else if (type.equals("translateY")) {
			return AffineTransform.getTranslateInstance(0, m[0]);
		} else if (type.equals("scale")) {
			if (m.length == 1) {
				return AffineTransform.getScaleInstance(m[0], m[0]);
			} else {
				return AffineTransform.getScaleInstance(m[0], m[1]);
			}
		} else if (type.equals("scaleX")) {
			return AffineTransform.getScaleInstance(m[0], 1);
		} else if (type.equals("scaleY")) {
			return AffineTransform.getScaleInstance(1, m[0]);
		} else if (type.equals("skew")) { // skewX, skewY
			return null; // AffineTransform.getShearInstance(m[0], m[1]); // angles
		}
		return null;
	}

	private String s(String name) {
		return s(name, null);
	}

	private String s(String name, Element e, String defaultValue) {
		String value = e.getAttributeValue(name);
		if (value != null) {
			return value;
		}
		return s(name, defaultValue);
	}

	Set<String> notInheritedProperties = new HashSet<>();
	{
		notInheritedProperties.add("id");
		notInheritedProperties.add("x");
		notInheritedProperties.add("y");
		notInheritedProperties.add("width");
		notInheritedProperties.add("height");
		notInheritedProperties.add("dx");
		notInheritedProperties.add("dy");
		notInheritedProperties.add("transform");
	}

	private String s(String name, String defaultValue) {
		if (!notInheritedProperties.contains(name)) {
			for (StackElement se : stack) {
				String value = se.e.getAttributeValue(name);
				if (value != null) {
					return value;
				}

				value = se.style.get(name);
				if (value != null) {
					return value;
				}
			}
		} else if (stack.size() > 0) {
			Map<String, String> parentStyle = stack.getFirst().style;
			String value = parentStyle.get(name);
			if (value != null) {
				return value;
			}

			Element parentElement = stack.getFirst().e;
			value = parentElement.getAttributeValue(name);
			if (value != null) {
				return value;
			}
		}
		return defaultValue;
	}

	private JVGShape parseGroup(Element e) throws JVGParseException {
		JVGGroup c = factory.createComponent(JVGGroup.class);
		parseChildren(e, c);
		return c;
	}

	private void parseChildren(Element parentElement, JVGContainer parent) throws JVGParseException {
		for (Element e : parentElement.getChildren()) {
			Object c = parseComponent(e, parent);
			if (c instanceof JVGShape) {
				parent.add((JVGShape) c);
			}
		}
	}

	private boolean parseTransform(JVGShape c, Element e, JVGContainer parent) throws JVGParseException {
		boolean hasTransform = false;
		AffineTransform transform = parseTransform(e.getAttributeValue("transform"));
		if (transform != null) {
			c.transform(transform);
			hasTransform = true;
		}

		//		transform = parseTransform(e.getAttributeValue("xtransform"));
		//		if (transform != null) {
		//			c.transform(transform);
		//			hasTransform = true;
		//		}
		return false;
	}

	private Map<String, String> parseStyle(Element e) {
		String style = e.getAttributeValue("style");
		String clazz = e.getAttributeValue("class");

		Map<String, String> map = new HashMap<>();
		if (clazz != null) {
			Map<String, String> cssStyle = cssTags.get(e.getName());
			if (cssStyle != null) {
				map.putAll(cssStyle);
			}

			String[] classes = clazz.split(" ");
			for (String c : classes) {
				cssStyle = cssClasses.get(c);
				if (cssStyle != null) {
					map.putAll(cssStyle);
				}
			}
		}
		if (style != null) {
			for (String attr : style.split(";")) {
				String[] nameValue = attr.split(":");
				String name = nameValue[0].trim().toLowerCase();
				if (nameValue.length == 2) {
					map.put(name, nameValue[1].trim());
				} else {
					map.put(name, "");
				}
			}

			//			try {
			//				InputSource source = new InputSource(new StringReader(style));
			//				CSSOMParser parser = new CSSOMParser(new SACParserCSS3());
			//				CSSStyleDeclaration decl = parser.parseStyleDeclaration(source);
			//				for (int i = 0; i < decl.getLength(); i++) {
			//					String name = decl.item(i);
			//					map.put(name, decl.getPropertyValue(name));
			//				}
			//			} catch (Exception exc) {
			//				exc.printStackTrace();
			//			}
		}
		return map;
	}

	private <R> R getResource(String href) {
		if (href != null) {
			if (href.startsWith("url(#") && href.endsWith(")")) {
				String resourceId = href.substring(5, href.length() - 1);
				return (R) allResources.get(resourceId);
			} else if (href.startsWith("#")) {
				String resourceId = href.substring(1);
				return (R) allResources.get(resourceId);
			}
		}
		return null;
	}

	private Draw getDrawResource(String href, double opacity) {
		Resource<?> resource = getResource(href);
		if (resource instanceof ColorResource) {
			return new ColorDraw((ColorResource) resource, opacity);
		} else if (resource instanceof LinearGradientResource) {
			return new LinearGradientDraw((LinearGradientResource) resource, opacity);
		} else if (resource instanceof RadialGradientResource) {
			return new RadialGradientDraw((RadialGradientResource) resource, opacity);
		}
		return null;
	}

	private FillPainter transparentFill = new FillPainter(new ColorDraw(ColorResource.transparent));

	private Draw defaulFill = new ColorDraw(ColorResource.black);

	private FillPainter parseFill(JVGShape c, Element e, List<Painter> painters, Draw defaulFill) throws JVGParseException {
		String fill = s("fill", e, null);
		if ("none".equals(fill)) {
			return transparentFill;
		}

		double opacity = JVGParseUtil.getDouble(e.getAttributeValue("fill-opacity"), JVGParseUtil.getDouble(s("fill-opacity"), 1));
		Draw draw = getDrawResource(fill, opacity);
		if (draw == null) {
			Color color = JVGParseUtil.getColor(fill, null);
			if (color != null) {
				draw = new ColorDraw(color, opacity);
			}
		}

		if (draw == null) {
			draw = defaulFill;
		}

		if (draw != null) {
			return new FillPainter(draw);
		}
		return null;
	}

	private Float parseXLength(String value, Float defaultValue, JVGComponent component) {
		if (value != null && value.endsWith("%")) {
			if (component != null) {
				Float percent = JVGParseUtil.getFloat(value, defaultValue);
				if (percent != null) {
					return (float) (percent * component.getRectangleBounds().getWidth());
				}
			}
			return defaultValue;
		}
		return parseLength(value, defaultValue);
	}

	private Float parseYLength(String value, Float defaultValue, JVGComponent component) {
		if (value != null && value.endsWith("%")) {
			if (component != null) {
				Float percent = JVGParseUtil.getFloat(value, defaultValue);
				if (percent != null) {
					return (float) (percent * component.getRectangleBounds().getHeight());
				}
			}
			return defaultValue;
		}
		return parseLength(value, defaultValue);
	}

	private Float parseLength(String value, Float defaultValue) {
		if (value != null) {
			return JVGParseUtil.getFloat(value, defaultValue);
		}
		return defaultValue;
	}

	private OutlinePainter transparentOutline = new OutlinePainter(StrokeResource.DEFAULT, ColorResource.transparent);

	private OutlinePainter parseOutline(JVGShape c, Element e, List<Painter> painters) throws JVGParseException {
		String stroke = e.getAttributeValue("stroke");
		if (stroke == null) {
			stroke = s("stroke");
		}

		if ("none".equals(stroke)) {
			return transparentOutline;
		}

		double opacity = JVGParseUtil.getDouble(s("stroke-opacity", e, null), 1);
		Draw draw = getDrawResource(stroke, opacity);
		if (draw == null) {
			Color color = JVGParseUtil.getColor(stroke, null);
			if (color != null) {
				draw = new ColorDraw(color, opacity);
			}
		}

		if (draw != null) {
			float width = parseXLength(s("stroke-width", e, null), 1f, c);
			float miterlimit = parseXLength(s("stroke-miterlimit", e, null), 10f, c);
			String linecap = s("stroke-linecap", e, null);
			String linejoin = s("stroke-linejoin", e, null);
			float[] dasharray = JVGParseUtil.getFloatArray(s("stroke-dasharray", e, null), ", ");
			float dashoffset = parseXLength(s("stroke-dashoffset", e, null), 0f, c);
			if (draw != null) {
				int cap = BasicStroke.CAP_BUTT;
				if ("round".equals(linecap)) {
					cap = BasicStroke.CAP_ROUND;
				} else if ("square".equals(linecap)) {
					cap = BasicStroke.CAP_SQUARE;
				}

				int join = BasicStroke.JOIN_MITER;
				if ("bevel".equals(linejoin)) {
					join = BasicStroke.JOIN_BEVEL;
				} else if ("round".equals(linejoin)) {
					join = BasicStroke.JOIN_ROUND;
				}

				if (dasharray != null && dasharray.length == 0) {
					dasharray = null;
				}

				Resource<Stroke> strokeResource = new StrokeResource<>(width, cap, join, miterlimit, dasharray, dashoffset);
				return new OutlinePainter(strokeResource, draw);
			}
		}
		return null;
	}

	private JVGShape parsePath(Element e) throws JVGParseException {
		String geom = e.getAttributeValue("d");
		if (geom == null) {
			return null;
		}

		MutableGeneralPath shape = new MutableGeneralPath();
		boolean fill = getPath(geom, shape);

		JVGPath c = factory.createComponent(JVGPath.class, shape, false);
		c.setFill(fill);
		//		if (fill) {
		//			c.setPainter(new FillPainter(ColorResource.black));
		//		} else {
		//			c.setPainter(new OutlinePainter(new StrokeResource<Stroke>(1f), ColorResource.black));
		//		}
		return c;
	}

	private JVGShape parsePolygon(Element e) throws JVGParseException {
		MutableGeneralPath shape = new MutableGeneralPath();

		String geom = e.getAttributeValue("points").trim();
		String[] coords = JVGParseUtil.getStringArray(geom, " ,", false);
		for (int i = 0; i < coords.length; i += 2) {
			double x = Double.parseDouble(coords[i]);
			double y = Double.parseDouble(coords[i + 1]);
			if (shape.numTypes == 0) {
				shape.moveTo(x, y);
			} else {
				shape.lineTo(x, y);
			}
		}
		shape.closePath();

		JVGPath c = factory.createComponent(JVGPath.class, shape, false);
		c.setFill(true);
		return c;
	}

	private JVGShape parsePolyline(Element e) throws JVGParseException {
		MutableGeneralPath shape = new MutableGeneralPath();

		String geom = e.getAttributeValue("points").trim();
		String[] points = geom.split("\\s");
		for (String p : points) {
			p = p.trim();
			if (p.length() > 0) {
				String[] c = p.split(",");
				if (shape.numTypes == 0) {
					shape.moveTo(Double.parseDouble(c[0]), Double.parseDouble(c[1]));
				} else {
					shape.lineTo(Double.parseDouble(c[0]), Double.parseDouble(c[1]));
				}
			}
		}

		JVGPath c = factory.createComponent(JVGPath.class, shape, false);
		c.removeAllPainters();
		c.setFill(false);
		return c;
	}

	private JVGShape parseRect(Element e, JVGContainer parent) throws JVGParseException {
		double x = parseXLength(e.getAttributeValue("x"), 0f, parent);
		double y = parseYLength(e.getAttributeValue("y"), 0f, parent);
		double w = parseXLength(e.getAttributeValue("width"), 0f, parent);
		double h = parseYLength(e.getAttributeValue("height"), 0f, parent);
		Float rx = parseXLength(e.getAttributeValue("rx"), null, parent);
		Float ry = parseYLength(e.getAttributeValue("ry"), null, parent);

		Shape shape;
		if (rx != null || ry != null) {
			if (rx == null) {
				rx = ry;
			} else if (ry == null) {
				ry = rx;
			}
			shape = new RoundRectangle2D.Double(x, y, w, h, 2 * rx, 2 * ry);
		} else {
			shape = new Rectangle2D.Double(x, y, w, h);
		}

		JVGPath c = factory.createComponent(JVGPath.class, shape, false);
		c.setFill(true);
		return c;
	}

	private JVGShape parseLine(Element e, JVGContainer parent) throws JVGParseException {
		double x1 = parseXLength(e.getAttributeValue("x1"), 0f, parent);
		double y1 = parseYLength(e.getAttributeValue("y1"), 0f, parent);
		double x2 = parseXLength(e.getAttributeValue("x2"), 0f, parent);
		double y2 = parseYLength(e.getAttributeValue("y2"), 0f, parent);

		MutableGeneralPath shape = new MutableGeneralPath();
		shape.moveTo(x1, y1);
		shape.lineTo(x2, y2);

		JVGPath c = factory.createComponent(JVGPath.class, shape, false);
		c.setFill(false);
		return c;
	}

	private JVGShape parseCircle(Element e, JVGContainer parent) throws JVGParseException {
		double cx = parseXLength(e.getAttributeValue("cx"), 0f, parent);
		double cy = parseYLength(e.getAttributeValue("cy"), 0f, parent);
		double r = parseXLength(e.getAttributeValue("r"), 1f, parent);

		Ellipse2D shape = new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r);

		JVGPath c = factory.createComponent(JVGPath.class, shape, false);
		c.setFill(true);
		return c;
	}

	private JVGShape parseEllipse(Element e, JVGContainer parent) throws JVGParseException {
		double cx = parseXLength(e.getAttributeValue("cx"), 0f, parent);
		double cy = parseYLength(e.getAttributeValue("cy"), 0f, parent);
		double rx = parseXLength(e.getAttributeValue("rx"), 1f, parent);
		double ry = parseYLength(e.getAttributeValue("ry"), 1f, parent);

		Ellipse2D shape = new Ellipse2D.Double(cx - rx, cy - ry, 2 * rx, 2 * ry);

		JVGPath c = factory.createComponent(JVGPath.class, shape, false);
		c.setFill(true);
		return c;
	}

	private List<JVGShape> parseClipPath(Element e, JVGContainer parent) throws JVGParseException {
		List<JVGShape> clipShapes = new ArrayList<>();
		for (Element c : e.getChildren()) {
			Object child = parseComponent(c, parent);
			if (child instanceof JVGShape) {
				JVGShape shape = (JVGShape) child;
				shape.setComponentType(JVGComponentType.clip);
				shape.setVisible(false);
				clipShapes.add(shape);
			}
		}

		if (clipShapes.size() == 0) {
			Object ref = getResource(e.getAttributeValue("href", xlink));
			if (ref instanceof JVGShape) {
				JVGShape shape = (JVGShape) ref;
				shape.setComponentType(JVGComponentType.clip);
				shape.setVisible(false);
				// TODO clone shape if shape.isClip() == false
				clipShapes.add(shape);
			} else if (ref instanceof List) {
				for (Object o : (List) ref) {
					if (o instanceof JVGShape) {
						JVGShape shape = (JVGShape) o;
						shape.setComponentType(JVGComponentType.clip);
						shape.setVisible(false);
						clipShapes.add(shape);
					}
				}
			}
		}
		return clipShapes;
	}

	private Object parseUse(Element e, JVGContainer parent) throws JVGParseException {
		Object use = getResource(e.getAttributeValue("href", xlink));
		Element useElement = null;
		if (use instanceof JVGComponent) {
			JVGComponent c = (JVGComponent) use;
			useElement = (Element) c.getClientProperty("svg:element");
		} else if (use instanceof Element) {
			useElement = (Element) use;
		}

		if (useElement != null) {
			return parseComponent(useElement, parent);
		} else {
			return null;
		}
	}

	private JVGShape parseImage(Element e, JVGContainer parent) throws JVGParseException {
		double w = parseXLength(e.getAttributeValue("width"), 0f, parent);
		double h = parseYLength(e.getAttributeValue("height"), 0f, parent);
		String href = e.getAttributeValue("href", xlink);
		if (href != null) {
			try {
				int index = href.indexOf("base64,");
				if (index != -1) {
					String base64Data = href.substring(index + 7).replace("\n", "");
					byte[] bytes = new BASE64Decoder().decodeBuffer(base64Data);
					Resource<ImageIcon> image = new ImageResource<>(bytes);
					JVGImage c = factory.createComponent(JVGImage.class, image);
					return c;
				}
			} catch (Exception exc) {
				throw new JVGParseException("Can't read image: " + href);
			}
		}
		return null;
	}

	private JVGShape parseText(Element e, JVGContainer parent) throws JVGParseException {
		boolean isText = "text".equals(e.getName());
		boolean isGroup = false;
		if (isText) {
			lastTextBounds = null;
			List<Element> spans = e.getChildren();
			for (Element span : spans) {
				if ("tspan".equals(span.getName())) {
					isGroup = true;
					break;
				}
			}
		}

		String fontFamily = s("font-family", e, FontResource.DEFAULT_FAMILY);
		int fontSize = parseLength(s("font-size", e, null), 12f).intValue();
		String fontWeight = s("font-weight", e, null);
		String fontStyle = s("font-style", e, null);
		int fw = Font.PLAIN;
		if ("bold".equals(fontWeight)) {
			fw = Font.BOLD;
		} else if ("italic".equals(fontStyle)) {
			fw = Font.ITALIC;
		}
		Font font = Fonts.getFont(fontFamily, fw, fontSize);

		JVGShape c;
		if (isGroup) {
			JVGGroup g = factory.createComponent(JVGGroup.class);
			for (Content content : e.getContent()) {
				Object child = null;
				if (content instanceof Element) {
					child = parseComponent((Element) content, parent);
				} else if (content instanceof Text) {
					String text = ((Text) content).getText();
					if (text.trim().length() == 0) {
						continue;
					}
					JVGTextField t = factory.createComponent(JVGTextField.class, text, font);
					setTextPos(t, null, null, false);
					parseShape(e, t, g);
					child = t;
				}
				if (child instanceof JVGShape) {
					g.add((JVGShape) child);
				}
			}
			c = g;
		} else {
			JVGTextField t = factory.createComponent(JVGTextField.class, e.getText(), font);
			String anchor = s("text-anchor", e, null);
			if (anchor != null) {
				t.setAnchor(TextAnchor.valueOf(anchor));
			}
			c = t;
		}

		Float x = parseXLength(s("x", e, null), null, parent);
		if (x == null) {
			Float dx = parseXLength(s("dx", e, null), null, parent);
			if (dx != null) {
				if (lastTextBounds != null) {
					x = (float) (lastTextBounds.getX() + lastTextBounds.getWidth() + dx);
				} else {
					x = dx;
				}
			}
		}

		Float y = parseYLength((s("y", e, null)), null, parent);
		if (y == null) {
			Float dy = parseYLength(s("dy", e, null), null, parent);
			if (dy != null) {
				if (lastTextBounds != null) {
					y = (float) (lastTextBounds.getY() + lastTextBounds.getHeight() + dy);
				} else {
					y = dy;
				}
			}
		}

		setTextPos(c, x, y, isText);
		return c;
	}

	private void setTextPos(JVGShape c, Float x, Float y, boolean isText) {
		if (x != null || y != null) {
			if (x == null) {
				x = lastTextBounds != null ? (float) (lastTextBounds.getX() + lastTextBounds.getWidth()) : 0f;
			}
			if (y == null) {
				y = lastTextBounds != null ? (float) (lastTextBounds.getY() + lastTextBounds.getHeight()) : 0f;
			}
			c.transform(AffineTransform.getTranslateInstance(x, y));
		}

		if (!isText) {
			if (x == null && y == null && lastTextBounds != null) {
				c.transform(AffineTransform.getTranslateInstance(5 + lastTextBounds.getX() + lastTextBounds.getWidth(), 0));
			}
			lastTextBounds = c.getOriginalBounds().getBounds2D();
		}
	}

	public static double[] getPathCoords(String value, int length) throws JVGParseException {
		String[] array = JVGParseUtil.getStringArray(value.trim().toLowerCase(), " -,.", true);

		List<Double> coords = new ArrayList<>();
		int index = 0;
		while (index < array.length) {
			String v = array[index++];
			boolean minus = false;
			if (v.equals(" ") || v.equals(",")) {
				continue;
			} else if (v.equals("-")) {
				minus = true;
				v = array[index++];
			}
			if (v.equals(".")) {
				v = "." + array[index++];
			} else if (index < array.length && ".".equals(array[index])) {
				index++;
				v += "." + array[index++];
			}
			if (v.endsWith("e")) {
				String m = array[index++];
				if (m.equals("-")) {
					v += "-" + array[index++];
				} else {
					v += m;
				}
			}
			try {
				double d = Double.parseDouble(v);
				if (minus) {
					d = -d;
				}
				coords.add(d);
			} catch (Exception e) {
				throw e;
			}
		}

		if (length > 1 && coords.size() % length != 0) {
			System.err.println("invalid coords: " + value);
		}

		double[] c = new double[coords.size()];
		for (int i = 0; i < c.length; i++) {
			c[i] = coords.get(i);
		}
		return c;
	}

	public static boolean getPath(String value, MutableGeneralPath path) throws JVGParseException {
		boolean fill = false;
		if (value == null) {
			return false;
		}

		value = value.trim();
		String[] array = JVGParseUtil.getStringArray(value, "MmLlCcQqAaHhVvSsTtZz", true);
		if (array == null) {
			return fill;
		}

		int index = 0;
		double x1 = 0, y1 = 0, x2 = 0, y2 = 0, mx = 0, my = 0;
		boolean moved = false;
		while (index < array.length) {
			String t = array[index].trim();
			if (t.length() != 1) {
				throw new JVGParseException("Bad path format: curve type=" + t);
			}

			char type = t.charAt(0);
			index++;

			try {
				switch (type) {
					case 'M': // moveto
						double[] c = getPathCoords(array[index++], 2);
						for (int i = 0; i < c.length; i += 2) {
							x2 = x1;
							y2 = y1;
							x1 = c[i];
							y1 = c[i + 1];
							if (!moved) {
								mx = x1;
								my = y1;
								moved = true;
							}
							if (i == 0) {
								path.moveTo(x1, y1);
							} else {
								path.lineTo(x1, y1);
							}
						}
						break;
					case 'm':
						c = getPathCoords(array[index++], 2);
						for (int i = 0; i < c.length; i += 2) {
							x2 = x1;
							y2 = y1;
							x1 += c[i];
							y1 += c[i + 1];
							if (!moved) {
								mx = x1;
								my = y1;
								moved = true;
							}
							if (i == 0) {
								path.moveTo(x1, y1);
							} else {
								path.lineTo(x1, y1);
							}
						}
						break;

					case 'L': // lineto
						c = getPathCoords(array[index++], 2);
						for (int i = 0; i < c.length; i += 2) {
							x2 = x1;
							y2 = y1;
							x1 = c[i];
							y1 = c[i + 1];
							if (!moved) {
								mx = x1;
								my = y1;
								moved = true;
							}
							path.lineTo(x1, y1);
						}
						break;
					case 'l':
						c = getPathCoords(array[index++], 2);
						for (int i = 0; i < c.length; i += 2) {
							x2 = x1;
							y2 = y1;
							x1 += c[i];
							y1 += c[i + 1];
							if (!moved) {
								mx = x1;
								my = y1;
								moved = true;
							}
							path.lineTo(x1, y1);
						}
						break;

					case 'C': // curveto
						c = getPathCoords(array[index++], 6);
						for (int i = 0; i < c.length; i += 6) {
							double x0 = c[i];
							double y0 = c[i + 1];
							x2 = c[i + 2];
							y2 = c[i + 3];
							x1 = c[i + 4];
							y1 = c[i + 5];
							path.curveTo(x0, y0, x2, y2, x1, y1);
						}
						break;
					case 'c':
						c = getPathCoords(array[index++], 6);
						for (int i = 0; i < c.length; i += 6) {
							double x0 = x1 + c[i];
							double y0 = y1 + c[i + 1];
							x2 = x1 + c[i + 2];
							y2 = y1 + c[i + 3];
							x1 = x1 + c[i + 4];
							y1 = y1 + c[i + 5];
							path.curveTo(x0, y0, x2, y2, x1, y1);
						}
						break;

					case 'S': // smooth curveto
						c = getPathCoords(array[index++], 4);
						for (int i = 0; i < c.length; i += 4) {
							double x0 = 2 * x1 - x2;
							double y0 = 2 * y1 - y2;
							x2 = c[i];
							y2 = c[i + 1];
							x1 = c[i + 2];
							y1 = c[i + 3];
							path.curveTo(x0, y0, x2, y2, x1, y1);
						}
						break;
					case 's':
						c = getPathCoords(array[index++], 4);
						for (int i = 0; i < c.length; i += 4) {
							double x0 = 2 * x1 - x2;
							double y0 = 2 * y1 - y2;
							x2 = x1 + c[i];
							y2 = y1 + c[i + 1];
							x1 = x1 + c[i + 2];
							y1 = y1 + c[i + 3];
							path.curveTo(x0, y0, x2, y2, x1, y1);
						}
						break;

					case 'Q': // quadratic Bezier curve
						c = getPathCoords(array[index++], 4);
						for (int i = 0; i < c.length; i += 4) {
							x2 = c[i];
							y2 = c[i + 1];
							x1 = c[i + 2];
							y1 = c[i + 3];
							path.quadTo(x2, y2, x1, y1);
						}
						break;
					case 'q':
						c = getPathCoords(array[index++], 4);
						for (int i = 0; i < c.length; i += 4) {
							x2 = x1 + c[i];
							y2 = y1 + c[i + 1];
							x1 = x1 + c[i + 2];
							y1 = y1 + c[i + 3];
							path.quadTo(x2, y2, x1, y1);
						}
						break;

					case 'T': // smooth quadratic Bezier quadto
						c = getPathCoords(array[index++], 2);
						for (int i = 0; i < c.length; i += 2) {
							x2 = 2 * x1 - x2;
							y2 = 2 * y1 - y2;
							x1 = c[i];
							y1 = c[i + 1];
							path.quadTo(x2, y2, x1, y1);
						}
						break;
					case 't':
						c = getPathCoords(array[index++], 2);
						for (int i = 0; i < c.length; i += 2) {
							x2 = 2 * x1 - x2;
							y2 = 2 * y1 - y2;
							x1 = x1 + c[i];
							y1 = y1 + c[i + 1];
							path.quadTo(x2, y2, x1, y1);
						}
						break;

					case 'H': // horizontal lineto
						c = getPathCoords(array[index++], 1);
						for (int i = 0; i < c.length; i += 1) {
							x2 = x1;
							y2 = y1;
							x1 = c[i];
							path.lineTo(x1, y1);
						}
						break;
					case 'h':
						c = getPathCoords(array[index++], 1);
						for (int i = 0; i < c.length; i += 1) {
							x2 = x1;
							y2 = y1;
							x1 += c[i];
							path.lineTo(x1, y1);
						}
						break;

					case 'V': // vertical lineto
						c = getPathCoords(array[index++], 1);
						for (int i = 0; i < c.length; i += 1) {
							x2 = x1;
							y2 = y1;
							y1 = c[i];
							path.lineTo(x1, y1);
						}
						break;
					case 'v':
						c = getPathCoords(array[index++], 1);
						for (int i = 0; i < c.length; i += 1) {
							x2 = x1;
							y2 = y1;
							y1 += c[i];
							path.lineTo(x1, y1);
						}
						break;

					case 'A': // elliptical Arc
						c = getPathCoords(array[index++], 7);
						for (int i = 0; i < c.length; i += 7) {
							double rx = c[i];
							double ry = c[i + 1];
							double angle = c[i + 2];
							boolean largeArcFlag = 1 == (int) c[i + 3];
							boolean sweepFlag = 1 == (int) c[i + 4];
							double x = c[i + 5];
							double y = c[i + 6];

							arcTo(path, rx, ry, angle, largeArcFlag, sweepFlag, x, y);

							x2 = x1;
							y2 = y1;
							x1 = x;
							y1 = y;
						}
						break;
					case 'a':
						c = getPathCoords(array[index++], 7);
						for (int i = 0; i < c.length; i += 7) {
							double rx = c[i];
							double ry = c[i + 1];
							double angle = c[i + 2];
							boolean largeArcFlag = 1 == (int) c[i + 3];
							boolean sweepFlag = 1 == (int) c[i + 4];
							double x = x1 + c[i + 5];
							double y = y1 + c[i + 6];

							arcTo(path, rx, ry, angle, largeArcFlag, sweepFlag, x, y);

							x2 = x1;
							y2 = y1;
							x1 = x;
							y1 = y;
						}
						break;

					case 'Z': // closepath
					case 'z':
						x2 = x1;
						y2 = y1;
						x1 = mx;
						y1 = my;
						path.closePath();
						if (index < array.length && array[index].trim().length() == 0) {
							index++;
						}
						fill = true;
						moved = false;
						break;

					default:
						throw new JVGParseException("Bad path format: curve type=" + array[index - 1]);
				}
			} catch (NumberFormatException exc) {
				exc.printStackTrace();
				throw new JVGParseException("Bad path format: " + exc.getMessage() + " (" + value + ")");
			}
		}
		return fill;
	}

	public static final void arcTo(MutableGeneralPath path, double rx, double ry, double theta, boolean largeArcFlag, boolean sweepFlag, double x, double y) {
		// Ensure radii are valid
		if (rx == 0 || ry == 0) {
			path.lineTo(x, y);
			return;
		}
		// Get the current (x, y) coordinates of the path
		Point2D p2d = path.getCurrentPoint();
		double x0 = p2d.getX();
		double y0 = p2d.getY();
		// Compute the half distance between the current and the final point
		double dx2 = (x0 - x) / 2.0f;
		double dy2 = (y0 - y) / 2.0f;
		// Convert theta from degrees to radians
		theta = Math.toRadians(theta % 360f);

		//
		// Step 1 : Compute (x1, y1)
		//
		double x1 = (Math.cos(theta) * dx2 + Math.sin(theta) * dy2);
		double y1 = (-Math.sin(theta) * dx2 + Math.cos(theta) * dy2);
		// Ensure radii are large enough
		rx = Math.abs(rx);
		ry = Math.abs(ry);
		double Prx = rx * rx;
		double Pry = ry * ry;
		double Px1 = x1 * x1;
		double Py1 = y1 * y1;
		double d = Px1 / Prx + Py1 / Pry;
		if (d > 1) {
			rx = Math.abs((Math.sqrt(d) * rx));
			ry = Math.abs((Math.sqrt(d) * ry));
			Prx = rx * rx;
			Pry = ry * ry;
		}

		//
		// Step 2 : Compute (cx1, cy1)
		//
		double sign = (largeArcFlag == sweepFlag) ? -1d : 1d;
		double coefSq = ((Prx * Pry) - (Prx * Py1) - (Pry * Px1)) / ((Prx * Py1) + (Pry * Px1));
		if (coefSq < 0) {
			coefSq = 0;
		}
		double coef = sign * Math.sqrt(coefSq);
		double cx1 = coef * ((rx * y1) / ry);
		double cy1 = coef * -((ry * x1) / rx);

		//
		// Step 3 : Compute (cx, cy) from (cx1, cy1)
		//
		double sx2 = (x0 + x) / 2.0f;
		double sy2 = (y0 + y) / 2.0f;
		double cx = sx2 + (Math.cos(theta) * cx1 - Math.sin(theta) * cy1);
		double cy = sy2 + (Math.sin(theta) * cx1 + Math.cos(theta) * cy1);

		//
		// Step 4 : Compute the angleStart (theta1) and the angleExtent (dtheta)
		//
		double ux = (x1 - cx1) / rx;
		double uy = (y1 - cy1) / ry;
		double vx = (-x1 - cx1) / rx;
		double vy = (-y1 - cy1) / ry;
		double p, n;
		// Compute the angle start
		n = Math.sqrt((ux * ux) + (uy * uy));
		p = ux; // (1 * ux) + (0 * uy)
		sign = (uy < 0) ? -1d : 1d;
		double angleStart = Math.toDegrees(sign * Math.acos(p / n));
		// Compute the angle extent
		n = Math.sqrt((ux * ux + uy * uy) * (vx * vx + vy * vy));
		p = ux * vx + uy * vy;
		sign = (ux * vy - uy * vx < 0) ? -1d : 1d;
		double angleExtent = Math.toDegrees(sign * Math.acos(p / n));
		if (!sweepFlag && angleExtent > 0) {
			angleExtent -= 360f;
		} else if (sweepFlag && angleExtent < 0) {
			angleExtent += 360f;
		}
		angleExtent %= 360f;
		angleStart %= 360f;

		Arc2D arc = new Arc2D.Double(cx - rx, cy - ry, rx * 2.0f, ry * 2.0f, -angleStart, -angleExtent, Arc2D.OPEN);
		path.append(arc, true);
	}

	private void parseDefs(Element defsElement, JVGContainer parent) throws JVGParseException {
		String id = defsElement.getAttributeValue("id");
		for (int order = 0; order < 2; order++) {
			for (Element e : defsElement.getChildren()) {
				String href = e.getAttributeValue("href", xlink);
				if ((order == 0 && href != null) || (order == 1 && href == null)) {
					continue;
				}
				parseComponent(e, parent);
			}
		}
	}

	private LinearGradientResource parseLinearGradient(Element e, JVGComponent component) {
		Resource<Gradient> ref = getResource(e.getAttributeValue("href", xlink));
		Gradient refSource = null;
		if (ref != null) {
			refSource = ref.getResource();
		}

		// in percents [0, 1]
		float x1 = parseLength(s("x1", e, null), 0f);
		float x2 = parseLength(s("x2", e, null), 0f);
		float y1 = parseLength(s("y1", e, null), 0f);
		float y2 = parseLength(s("y2", e, null), 0f);

		AffineTransform transform = parseTransform(s("gradientTransform", e, null));
		String spreadMethod = s("spreadMethod", e, null); // pad, repeat, reflect
		String units = s("gradientUnits", e, null); // objectBoundingBox, userSpaceOnUse

		List<Resource<Color>> colorsList = new ArrayList<>();
		List<Float> offsetsList = new ArrayList<>();
		for (Element stopElement : e.getChildren()) {
			if (stopElement.getName().equals("stop")) {
				String stopId = stopElement.getAttributeValue("id");
				Map<String, String> style = parseStyle(stopElement); // stop-color, stop-opacity
				Color color = JVGParseUtil.getColor(style.get("stop-color"), JVGParseUtil.getColor(stopElement.getAttributeValue("stop-color"), null));
				float opacity = JVGParseUtil.getFloat(style.get("stop-opacity"), JVGParseUtil.getFloat(stopElement.getAttributeValue("stop-opacity"), 1f));
				if (opacity != 1) {
					color = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (opacity * color.getAlpha()));
				}
				colorsList.add(new ColorResource(color));

				float offset = parseLength(stopElement.getAttributeValue("offset"), 0f);
				offsetsList.add(offset);
			}
		}

		float[] fractions = null;
		Resource<Color>[] colors = null;
		if (colorsList.size() > 0) {
			fractions = new float[colorsList.size()];
			colors = new Resource[colorsList.size()];
			for (int i = 0; i < colorsList.size(); i++) {
				colors[i] = colorsList.get(i);
				fractions[i] = offsetsList.get(i);
			}
		} else if (refSource != null) {
			fractions = refSource.getFractions();
			colors = refSource.getColors();
		}

		MultipleGradientPaint.CycleMethodEnum cycleMethod = MultipleGradientPaint.NO_CYCLE;
		if ("repeat".equals(spreadMethod)) {
			cycleMethod = MultipleGradientPaint.REPEAT;
		} else if ("reflect".equals(spreadMethod)) {
			cycleMethod = MultipleGradientPaint.REFLECT;
		}

		LinearGradient gradient = new LinearGradient(fractions, colors, cycleMethod, x1, y1, x2, y2);
		if ("userSpaceOnUse".equals(units)) {
			gradient.setUnitsType(GradientUnitsType.ABSOLUTE);
		}
		gradient.setTransform(transform);

		return new LinearGradientResource(gradient);
	}

	private RadialGradientResource parseRadialGradient(Element e, JVGComponent component) {
		Resource<Gradient> ref = getResource(e.getAttributeValue("href", xlink));
		Gradient refSource = null;
		if (ref != null) {
			refSource = ref.getResource();
		}

		// in percents [0, 1]
		float cx = parseLength(s("cx", e, null), 0f);
		float cy = parseLength(s("cy", e, null), 0f);
		float fx = parseLength(s("fx", e, null), 0f);
		float fy = parseLength(s("fy", e, null), 0f);
		float r = parseLength(s("r", e, null), 0f);

		AffineTransform transform = parseTransform(s("gradientTransform", e, null));
		String spreadMethod = s("spreadMethod", e, null); // pad, repeat, reflect
		String units = s("gradientUnits", e, null); // objectBoundingBox, userSpaceOnUse

		List<Resource<Color>> colorsList = new ArrayList<>();
		List<Float> offsetsList = new ArrayList<>();
		for (Element stopElement : e.getChildren()) {
			if (stopElement.getName().equals("stop")) {
				String stopId = stopElement.getAttributeValue("id");
				Map<String, String> style = parseStyle(stopElement); // stop-color, stop-opacity
				Color color = JVGParseUtil.getColor(style.get("stop-color"), JVGParseUtil.getColor(stopElement.getAttributeValue("stop-color"), null));
				float opacity = JVGParseUtil.getFloat(style.get("stop-opacity"), JVGParseUtil.getFloat(stopElement.getAttributeValue("stop-opacity"), 1f));
				if (opacity != 1) {
					color = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (opacity * color.getAlpha()));
				}
				colorsList.add(new ColorResource(color));

				float offset = parseLength(stopElement.getAttributeValue("offset"), 0f);
				offsetsList.add(offset);
			}
		}

		float[] fractions = null;
		Resource<Color>[] colors = null;
		if (colorsList.size() > 0) {
			fractions = new float[colorsList.size()];
			colors = new Resource[colorsList.size()];
			for (int i = 0; i < colorsList.size(); i++) {
				colors[i] = colorsList.get(i);
				fractions[i] = offsetsList.get(i);
			}
		} else if (refSource != null) {
			fractions = refSource.getFractions();
			colors = refSource.getColors();
		}

		MultipleGradientPaint.CycleMethodEnum cycleMethod = MultipleGradientPaint.NO_CYCLE;
		if ("repeat".equals(spreadMethod)) {
			cycleMethod = MultipleGradientPaint.REPEAT;
		} else if ("reflect".equals(spreadMethod)) {
			cycleMethod = MultipleGradientPaint.REFLECT;
		}

		RadialGradient gradient = new RadialGradient(fractions, colors, cycleMethod, cx, cy, fx, fy, r);
		if ("userSpaceOnUse".equals(units)) {
			gradient.setUnitsType(GradientUnitsType.ABSOLUTE);
		}
		gradient.setTransform(transform);
		return new RadialGradientResource(gradient);
	}

	private void parseCss(String text) {
		if (text != null && text.length() > 0) {
			try {
				InputSource source = new InputSource(new StringReader(text));
				CSSOMParser parser = new CSSOMParser(new SACParserCSS3());
				CSSStyleSheet sheet = parser.parseStyleSheet(source, null, null);
				CSSRuleList rules = sheet.getCssRules();
				for (int i = 0; i < rules.getLength(); i++) {
					CSSRule rule = rules.item(i);
					if (rule instanceof CSSStyleRule) {
						CSSStyleRule styleRule = (CSSStyleRule) rule;
						String selectorName = styleRule.getSelectorText();
						Map<String, String> style = new HashMap<>();
						CSSStyleDeclaration styleDeclaration = styleRule.getStyle();
						for (int j = 0; j < styleDeclaration.getLength(); j++) {
							String property = styleDeclaration.item(j);
							String value = styleDeclaration.getPropertyCSSValue(property).getCssText();
							String priority = styleDeclaration.getPropertyPriority(property);
							style.put(property, value);
						}

						String[] selectors = JVGParseUtil.getStringArray(selectorName, " ,");
						for (String s : selectors) {
							if (s.startsWith(".")) {
								s = s.substring(1);
								cssClasses.put(s, style);
							} else {
								cssTags.put(s, style);
							}
						}
					}
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}

	}

	@Override
	public void init(JVGPane pane) {
	}

	public JVGPane getPane() {
		return pane;
	}

	public void setPane(JVGPane pane) {
		this.pane = pane;
	}
}
