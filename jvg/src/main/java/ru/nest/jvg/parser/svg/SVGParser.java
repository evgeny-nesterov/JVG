package ru.nest.jvg.parser.svg;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.gradient.Gradient;
import javax.swing.gradient.Gradient.GradientUnitsType;
import javax.swing.gradient.LinearGradient;
import javax.swing.gradient.MultipleGradientPaint;
import javax.swing.gradient.RadialGradient;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;

import com.steadystate.css.parser.CSSOMParser;
import com.steadystate.css.parser.SACParserCSS3;

import ru.nest.fonts.Fonts;
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

	private Map<String, Map<String, String>> css = new HashMap<>();

	private Map<String, String> style;

	private LinkedList<Map<String, String>> parents = new LinkedList<>();

	private Rectangle2D lastTextBounds = null;

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
		String heightValue = rootElement.getAttributeValue("height");
		if (widthValue != null && heightValue != null) {
			Float w;
			if (widthValue.endsWith("%")) {
				w = 800.0f;
			} else {
				w = parseLength(widthValue, null);
			}

			Float h;
			if (heightValue.endsWith("%")) {
				h = 800.0f;
			} else {
				h = parseLength(heightValue, null);
			}

			documentSize = new Dimension(w.intValue(), h.intValue());
		}

		if (documentSize == null) {
			String viewBox = rootElement.getAttributeValue("viewBox");
			if (viewBox != null) {
				int[] array = JVGParseUtil.getIntegerArray(viewBox, " ,");
				if (array.length == 4) {
					documentSize = new Dimension(array[2], array[3]);
				}
			}
		}

		parseChildren(rootElement, parent);
	}

	private void parseChildren(Element parentElement, JVGContainer parent) throws JVGParseException {
		for (Element e : (List<Element>) parentElement.getChildren()) {
			Object c = parseComponent(e);
			if (c instanceof JVGShape) {
				parent.add((JVGShape) c);
			}
		}
	}

	private <C> C parseComponent(Element e) throws JVGParseException {
		String type = e.getName();
		style = parseStyle(e);
		Draw defaulFill = this.defaulFill;
		Object c = null;
		if ("path".equals(type)) {
			c = parsePath(e);
		} else if ("g".equals(type)) {
			c = parseGroup(e);
		} else if ("rect".equals(type)) {
			c = parseRect(e);
		} else if ("polygon".equals(type)) {
			c = parsePolygon(e);
		} else if ("text".equals(type)) {
			c = parseText(e);
		} else if ("tspan".equals(type)) {
			c = parseText(e);
		} else if ("polyline".equals(type)) {
			c = parsePolyline(e);
		} else if ("line".equals(type)) {
			c = parseLine(e);
		} else if ("circle".equals(type)) {
			c = parseCircle(e);
		} else if ("ellipse".equals(type)) {
			c = parseEllipse(e);
		} else if ("image".equals(type)) {
			c = parseImage(e);
		} else if ("defs".equals(type)) {
			parseDefs(e);
		} else if ("linearGradient".equals(type)) {
			c = parseLinearGradient(e);
		} else if ("radialGradient".equals(type)) {
			c = parseRadialGradient(e);
		} else if ("clipPath".equals(type)) {
			c = parseClipPath(e);
		} else if ("use".equals(type)) {
			c = getResource(e.getAttributeValue("href", xlink));
			defaulFill = null;
		} else if ("style".equals(type)) {
			parseCss(e.getText());
		} else if ("font".equals(type)) {
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

		if (c != null) {
			String compId = e.getAttributeValue("id");
			if (compId != null) {
				allResources.put(compId, c);

				if (c instanceof Resource) {
					Resource resource = (Resource) c;
					resource.setName(compId);
					resources.addResource(resource);
				}
			}
		}

		if (c instanceof JVGShape) {
			JVGShape shape = (JVGShape) c;
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
			if (clip instanceof Shape) {
				shape.setClip((Shape) clip);
			}

			parseTransform(shape, e);
		}
		return (C) c;
	}

	private void parsePainters(Element e, JVGShape shape, Draw defaulFill) throws JVGParseException {
		List<Painter> painters = new ArrayList<Painter>();
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

	private String s(String name, String defaultValue) {
		String value = style.get(name);
		if (value != null) {
			return value;
		}

		for (Map<String, String> parentStyle : parents) {
			value = parentStyle.get(name);
			if (value != null) {
				return value;
			}
		}
		return defaultValue;
	}

	private JVGShape parseGroup(Element e) throws JVGParseException {
		JVGGroup c = factory.createComponent(JVGGroup.class);
		parents.addFirst(style);
		parseChildren(e, c);
		parents.removeFirst();
		return c;
	}

	private boolean parseTransform(JVGShape c, Element e) throws JVGParseException {
		boolean hasTransform = false;
		AffineTransform transform = parseTransform(e.getAttributeValue("transform"));
		if (transform != null) {
			c.transform(transform);
			hasTransform = true;
		}

		if (c instanceof JVGTextField) {
			if (!hasTransform) {
				Double x = JVGParseUtil.getDouble(e.getAttributeValue("x"), null);
				Double y = JVGParseUtil.getDouble(e.getAttributeValue("y"), null);
				if (x != null || y != null) {
					if (x == null) {
						x = 0.0;
					}
					if (y == null) {
						y = 0.0;
					}
					c.transform(AffineTransform.getTranslateInstance(x, y));
					hasTransform = true;
				}
			}

			if (!hasTransform && lastTextBounds != null) {
				c.transform(AffineTransform.getTranslateInstance(5 + lastTextBounds.getX() + lastTextBounds.getWidth(), 0));
			}
			lastTextBounds = c.getOriginalBounds().getBounds2D();
		}
		return false;
	}

	private Map<String, String> parseStyle(Element e) {
		String style = e.getAttributeValue("style");
		String clazz = e.getAttributeValue("class");

		Map<String, String> map = new HashMap<>();
		if (clazz != null) {
			String[] classes = clazz.split(" ");
			for (String c : classes) {
				Map<String, String> cssStyle = css.get(c);
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
		String fill = e.getAttributeValue("fill");
		if (fill == null) {
			fill = s("fill");
		}

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

	private Float parseLength(String value, Float defaultValue) {
		if (value != null) {
			float koef = 1f;
			if (value.endsWith("px")) {
				value = value.substring(0, value.length() - 2);
			} else if (value.endsWith("pt")) {
				value = value.substring(0, value.length() - 2);
				koef = 1.3281472327365f;
			} else if (value.endsWith("%")) {
				// TODO
			}
			return koef * JVGParseUtil.getFloat(value, defaultValue);
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
			float width = parseLength(e.getAttributeValue("stroke-width"), parseLength(s("stroke-width"), 1f));
			float miterlimit = parseLength(e.getAttributeValue("stroke-miterlimit"), parseLength(s("stroke-miterlimit"), 10f));
			String linecap = e.getAttributeValue("stroke-linecap"); // butt
			if (linecap == null) {
				linecap = s("stroke-linecap");
			}
			String linejoin = e.getAttributeValue("stroke-linejoin");
			if (linejoin == null) {
				linejoin = s("stroke-linejoin");
			}
			float[] dasharray = JVGParseUtil.getFloatArray(e.getAttributeValue("stroke-dasharray"), ",");
			if (dasharray == null || dasharray.length == 0) {
				dasharray = JVGParseUtil.getFloatArray(s("stroke-dasharray"), ",");
			}
			float dashoffset = parseLength(e.getAttributeValue("stroke-dashoffset"), parseLength(s("stroke-dashoffset"), 0f));

			if (draw != null) {
				int cap = BasicStroke.CAP_BUTT;
				if ("round".equals(linecap)) {
					cap = BasicStroke.CAP_ROUND;
				} else if ("square".equals(linecap)) {
					cap = BasicStroke.CAP_SQUARE;
				}

				int join = BasicStroke.JOIN_MITER;
				if ("bevel".equals(linecap)) {
					join = BasicStroke.JOIN_BEVEL;
				} else if ("round".equals(linecap)) {
					join = BasicStroke.JOIN_ROUND;
				}

				if (dasharray != null && dasharray.length == 0) {
					dasharray = null;
				}

				Resource<Stroke> strokeResource = new StrokeResource<Stroke>(width, cap, join, miterlimit, dasharray, dashoffset);
				return new OutlinePainter(strokeResource, draw);
			}
		}
		return null;
	}

	private JVGShape parsePath(Element e) throws JVGParseException {
		String geom = e.getAttributeValue("d");
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

	private JVGShape parseRect(Element e) throws JVGParseException {
		double x = parseLength(e.getAttributeValue("x"), 0f);
		double y = parseLength(e.getAttributeValue("y"), 0f);
		double w = parseLength(e.getAttributeValue("width"), 0f);
		double h = parseLength(e.getAttributeValue("height"), 0f);
		Float rx = parseLength(e.getAttributeValue("rx"), null);
		Float ry = parseLength(e.getAttributeValue("ry"), null);

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

	private JVGShape parseLine(Element e) throws JVGParseException {
		double x1 = parseLength(e.getAttributeValue("x1"), 0f);
		double y1 = parseLength(e.getAttributeValue("y1"), 0f);
		double x2 = parseLength(e.getAttributeValue("x2"), 0f);
		double y2 = parseLength(e.getAttributeValue("y2"), 0f);

		MutableGeneralPath shape = new MutableGeneralPath();
		shape.moveTo(x1, y1);
		shape.lineTo(x2, y2);

		JVGPath c = factory.createComponent(JVGPath.class, shape, false);
		c.setFill(false);
		return c;
	}

	private JVGShape parseCircle(Element e) throws JVGParseException {
		double cx = parseLength(e.getAttributeValue("cx"), 0f);
		double cy = parseLength(e.getAttributeValue("cy"), 0f);
		double r = parseLength(e.getAttributeValue("r"), 1f);

		Ellipse2D shape = new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r);

		JVGPath c = factory.createComponent(JVGPath.class, shape, false);
		c.setFill(true);
		return c;
	}

	private JVGShape parseEllipse(Element e) throws JVGParseException {
		double cx = parseLength(e.getAttributeValue("cx"), 0f);
		double cy = parseLength(e.getAttributeValue("cy"), 0f);
		double rx = parseLength(e.getAttributeValue("rx"), 1f);
		double ry = parseLength(e.getAttributeValue("ry"), 1f);

		Ellipse2D shape = new Ellipse2D.Double(cx - rx, cy - ry, 2 * rx, 2 * ry);

		JVGPath c = factory.createComponent(JVGPath.class, shape, false);
		c.setFill(true);
		return c;
	}

	private Shape parseClipPath(Element e) throws JVGParseException {
		Shape clip = null;
		for (Element c : (List<Element>) e.getChildren()) {
			Object child = parseComponent(c);
			Shape shape = null;
			if (child instanceof Shape) {
				shape = (Shape) child;
			} else if (child instanceof JVGShape) {
				shape = ((JVGShape) child).getShape();
			}
			if (shape != null) {
				if (clip == null) {
					clip = shape;
				} else if (clip instanceof Area) {
					((Area) clip).add(new Area(shape));
				} else {
					Area area = new Area(clip);
					area.add(new Area(shape));
					clip = area;
				}
			}
		}

		if (clip == null) {
			Object ref = getResource(e.getAttributeValue("href", xlink));
			if (ref instanceof Shape) {
				clip = (Shape) ref;
			} else if (ref instanceof JVGShape) {
				clip = ((JVGShape) ref).getShape();
			}
		}
		return clip;
	}

	private JVGShape parseImage(Element e) throws JVGParseException {
		double w = parseLength(e.getAttributeValue("width"), 0f);
		double h = parseLength(e.getAttributeValue("height"), 0f);
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

	private JVGShape parseText(Element e) throws JVGParseException {
		if ("text".equals(e.getName())) {
			List<Element> spans = e.getChildren();
			if (spans.size() > 0) {
				lastTextBounds = null;
				return parseGroup(e);
			}
		}

		String text = e.getText();
		String fontFamily = s("font-family", e, FontResource.DEFAULT_FAMILY);
		int fontSize = parseLength(s("font-size", e, null), 12f).intValue();
		Font font = Fonts.getFont(fontFamily, Font.PLAIN, fontSize);

		JVGTextField c = factory.createComponent(JVGTextField.class, text, font);
		String anchor = s("text-anchor");
		if (anchor != null) {
			c.setAnchor(TextAnchor.valueOf(anchor));
		}
		return c;
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
		double x1 = (Math.cos(theta) * (double) dx2 + Math.sin(theta) * (double) dy2);
		double y1 = (-Math.sin(theta) * (double) dx2 + Math.cos(theta) * (double) dy2);
		// Ensure radii are large enough
		rx = Math.abs(rx);
		ry = Math.abs(ry);
		double Prx = rx * rx;
		double Pry = ry * ry;
		double Px1 = x1 * x1;
		double Py1 = y1 * y1;
		double d = Px1 / Prx + Py1 / Pry;
		if (d > 1) {
			rx = Math.abs((Math.sqrt(d) * (double) rx));
			ry = Math.abs((Math.sqrt(d) * (double) ry));
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
		double cx = sx2 + (Math.cos(theta) * (double) cx1 - Math.sin(theta) * (double) cy1);
		double cy = sy2 + (Math.sin(theta) * (double) cx1 + Math.cos(theta) * (double) cy1);

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

	private void parseDefs(Element defsElement) throws JVGParseException {
		String id = defsElement.getAttributeValue("id");
		for (int order = 0; order < 2; order++) {
			for (Element e : (List<Element>) defsElement.getChildren()) {
				String href = e.getAttributeValue("href", xlink);
				if ((order == 0 && href != null) || (order == 1 && href == null)) {
					continue;
				}
				parseComponent(e);
			}
		}
	}

	private LinearGradientResource parseLinearGradient(Element e) {
		Resource<Gradient> ref = getResource(e.getAttributeValue("href", xlink));
		Gradient refSource = null;
		if (ref != null) {
			refSource = ref.getResource();
		}

		float x1 = parseLength(e.getAttributeValue("x1"), 0f);
		float x2 = parseLength(e.getAttributeValue("x2"), 0f);
		float y1 = parseLength(e.getAttributeValue("y1"), 0f);
		float y2 = parseLength(e.getAttributeValue("y2"), 0f);
		AffineTransform transform = parseTransform(e.getAttributeValue("gradientTransform"));
		String spreadMethod = e.getAttributeValue("spreadMethod"); // pad, repeat, reflect
		String units = e.getAttributeValue("gradientUnits"); // objectBoundingBox, userSpaceOnUse

		List<Resource<Color>> colorsList = new ArrayList<>();
		List<Float> offsetsList = new ArrayList<>();
		for (Element stopElement : (List<Element>) e.getChildren()) {
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

	private RadialGradientResource parseRadialGradient(Element e) {
		Resource<Gradient> ref = getResource(e.getAttributeValue("href", xlink));
		Gradient refSource = null;
		if (ref != null) {
			refSource = ref.getResource();
		}

		float cx = parseLength(e.getAttributeValue("cx"), 0f);
		float cy = parseLength(e.getAttributeValue("cy"), 0f);
		float fx = parseLength(e.getAttributeValue("fx"), 0f);
		float fy = parseLength(e.getAttributeValue("fy"), 0f);
		float r = parseLength(e.getAttributeValue("r"), 0f);
		AffineTransform transform = parseTransform(e.getAttributeValue("gradientTransform"));
		String units = e.getAttributeValue("gradientUnits");
		String spreadMethod = e.getAttributeValue("spreadMethod");

		List<Resource<Color>> colorsList = new ArrayList<>();
		List<Float> offsetsList = new ArrayList<>();
		for (Element stopElement : (List<Element>) e.getChildren()) {
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
						if (selectorName.startsWith(".")) {
							selectorName = selectorName.substring(1);
						}

						Map<String, String> style = new HashMap<>();
						CSSStyleDeclaration styleDeclaration = styleRule.getStyle();
						for (int j = 0; j < styleDeclaration.getLength(); j++) {
							String property = styleDeclaration.item(j);
							String value = styleDeclaration.getPropertyCSSValue(property).getCssText();
							String priority = styleDeclaration.getPropertyPriority(property);
							style.put(property, value);
						}
						css.put(selectorName, style);
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
}
