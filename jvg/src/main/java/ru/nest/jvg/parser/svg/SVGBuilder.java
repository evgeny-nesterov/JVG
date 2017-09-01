package ru.nest.jvg.parser.svg;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.gradient.LinearGradient;
import javax.swing.gradient.MultipleGradientPaint;
import javax.swing.gradient.RadialGradient;

import org.jdom2.Element;
import org.jdom2.Namespace;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGContainer;
import ru.nest.jvg.actionarea.JVGActionArea;
import ru.nest.jvg.actionarea.JVGCustomActionArea;
import ru.nest.jvg.geom.CoordinablePathIterator;
import ru.nest.jvg.parser.JVGBuilder;
import ru.nest.jvg.parser.JVGBuilderInterface;
import ru.nest.jvg.parser.JVGParseException;
import ru.nest.jvg.parser.JVGParseUtil;
import ru.nest.jvg.resource.ImageResource;
import ru.nest.jvg.resource.JVGResources;
import ru.nest.jvg.resource.LinearGradientResource;
import ru.nest.jvg.resource.RadialGradientResource;
import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.shape.JVGGroup;
import ru.nest.jvg.shape.JVGImage;
import ru.nest.jvg.shape.JVGShape;
import ru.nest.jvg.shape.JVGTextField;
import ru.nest.jvg.shape.paint.Draw;
import ru.nest.jvg.shape.paint.FillPainter;
import ru.nest.jvg.shape.paint.OutlinePainter;
import ru.nest.jvg.shape.paint.Painter;
import sun.misc.BASE64Encoder;

public class SVGBuilder extends JVGBuilder implements JVGBuilderInterface {
	private final static Namespace xlink = Namespace.getNamespace("http://www.w3.org/1999/xlink");

	private final static Namespace xmlns = Namespace.getNamespace("http://www.w3.org/2000/svg");

	@Override
	public Element build(JVGComponent[] components) throws JVGParseException {
		Element rootElement = new Element("svg", xmlns);
		// rootElement.addNamespaceDeclaration(xlink);

		Element defsElement = buildDefs(resources);
		if (defsElement != null) {
			rootElement.addContent(defsElement);
		}

		if (pane != null) {
			if (pane.isDocumentSizeSet()) {
				rootElement.setAttribute("width", Integer.toString(pane.getDocumentSize().width));
				rootElement.setAttribute("height", Integer.toString(pane.getDocumentSize().height));
				rootElement.setAttribute("viewBox", "0 0 " + pane.getDocumentSize().width + " " + pane.getDocumentSize().height);
			}

			if (pane.isBackgroundSet() && !Color.white.equals(pane.getBackground())) {
				Element backgroundElement = new Element("rect");
				backgroundElement.setAttribute("x", "0%");
				backgroundElement.setAttribute("y", "0%");
				backgroundElement.setAttribute("w", "100%");
				backgroundElement.setAttribute("h", "100%");
				backgroundElement.setAttribute("fill", JVGParseUtil.getColor(pane.getBackground()));
				rootElement.addContent(backgroundElement);
			}
		}
		rootElement.setAttribute("version", "1.0");

		build(components, rootElement);
		return rootElement;
	}

	private void build(JVGComponent[] components, Element componentsElement) throws JVGParseException {
		for (int i = 0; i < components.length; i++) {
			if (!(components[i] instanceof JVGActionArea) || (components[i] instanceof JVGCustomActionArea)) {
				Element componentElement = buildComponent(components[i]);
				if (componentElement != null) {
					componentsElement.addContent(componentElement);
				}
			}
		}
	}

	private Element buildComponent(JVGComponent component) throws JVGParseException {
		if (!(component instanceof JVGShape)) {
			return null;
		}

		Element componentElement = null;
		JVGShape shape = (JVGShape) component;
		if (shape instanceof JVGGroup) {
			componentElement = new Element("g");
			JVGGroup group = (JVGGroup) shape;
			for (int i = 0; i < group.getChildCount(); i++) {
				JVGComponent child = group.getChild(i);
				if (!(child instanceof JVGActionArea) || (child instanceof JVGCustomActionArea)) {
					Element childElement = buildComponent(child);
					if (childElement != null) {
						componentElement.addContent(childElement);
					}
				}
			}
		} else if (shape instanceof JVGImage) {
			componentElement = new Element("image");
			JVGImage image = (JVGImage) shape;
			String source = getImage(image.getImage());
			if (source != null) {
				componentElement.setAttribute("href", source);
			} else {
				ImageResource r = (ImageResource) image.getImage();
				byte[] data = r.getData();
				String base64data = new BASE64Encoder().encode(data);
				componentElement.setAttribute("href", "base64," + base64data);
			}
		} else if (shape instanceof JVGTextField) {
			componentElement = new Element("text");
			JVGTextField c = (JVGTextField) shape;
			Font font = c.getFont() != null ? c.getFont().getResource() : null;
			if (font != null) {
				componentElement.setAttribute("font", font.getFontName());
				componentElement.setAttribute("font-size", Integer.toString(font.getSize()));
			}
			componentElement.setText(c.getText());
		} else {
			componentElement = new Element("path");
			setPath(shape.getPath().getPathIterator(null), componentElement);

			//			if (path.getPathStroke() != null) {
			//				String pathStroke = getStroke(path.getPathStroke());
			//				if (pathStroke != null) {
			//					componentElement.setAttribute("path-stroke", pathStroke);
			//				}
			//			}
		}

		//			if (shape.isAntialias()) {
		//				componentElement.setAttribute("antialias", "yes");
		//			}

		// painters
		setPainters(shape, componentElement);

		// transform
		AffineTransform transform;
		JVGContainer parent = shape.getParent();
		if (parent instanceof JVGShape) {
			JVGShape parentShape = (JVGShape) parent;
			transform = (AffineTransform) shape.getTransform().clone();
			transform.preConcatenate(parentShape.getInverseTransform());
		} else {
			transform = shape.getTransform();
		}
		setTransform(transform, componentElement);

		// opacity
		int opacity = shape.getAlfa();
		if (opacity != 255) {
			componentElement.setAttribute("opacity", Integer.toString(opacity));
		}

		componentElement.setAttribute("id", shape.getId().toString());

		//			if (component.getName() != null) {
		//				componentElement.setAttribute("name", component.getName());
		//			}

		if (!shape.isVisible()) {
			componentElement.setAttribute("display", "none");
		}

		//			if (component.isClipped()) {
		//				componentElement.setAttribute("clip", "yes");
		//			}

		// properties
		setProperties(shape, componentElement);
		return componentElement;
	}

	private void setProperties(JVGComponent component, Element componentElement) {
		Map<String, String> hash = (HashMap<String, String>) component.getClientProperty("component-properties");
		if (hash != null && hash.size() > 0) {
			for (Map.Entry<String, String> entry : hash.entrySet()) {
				// TODO
			}
		}
	}

	private void setTransform(AffineTransform transform, Element componentElement) {
		int type = transform.getType();
		if (!transform.isIdentity()) {
			double[] matrix = new double[6];
			transform.getMatrix(matrix);
			if (type == AffineTransform.TYPE_TRANSLATION) {
				componentElement.setAttribute("transform", "translate(" + JVGParseUtil.getValue(transform.getTranslateX()) + "," + JVGParseUtil.getValue(transform.getTranslateY()) + ")");
			} else if (type == AffineTransform.TYPE_GENERAL_SCALE || type == AffineTransform.TYPE_UNIFORM_SCALE || type == AffineTransform.TYPE_FLIP || type == (AffineTransform.TYPE_GENERAL_SCALE | AffineTransform.TYPE_FLIP) || type == (AffineTransform.TYPE_UNIFORM_SCALE | AffineTransform.TYPE_FLIP)) {
				componentElement.setAttribute("type", "scale(" + JVGParseUtil.getValue(transform.getScaleX()) + "," + JVGParseUtil.getValue(transform.getScaleY()) + ")");
			} else if (type == AffineTransform.TYPE_GENERAL_ROTATION || type == AffineTransform.TYPE_QUADRANT_ROTATION) {
				double cos = matrix[0];
				double sin = matrix[1];
				componentElement.setAttribute("type", "rotate(" + JVGParseUtil.getValue(Math.toDegrees((sin < 0 ? -1 : 1) * Math.acos(cos))) + ")");
			} else {
				componentElement.setAttribute("type", "matrix(" + JVGParseUtil.getValue(matrix[0]) + "," + JVGParseUtil.getValue(matrix[1]) + "," + JVGParseUtil.getValue(matrix[2]) + "," + JVGParseUtil.getValue(matrix[3]) + "," + JVGParseUtil.getValue(matrix[4]) + "," + JVGParseUtil.getValue(matrix[5]) + ")");
			}
		}
	}

	private String getImage(Resource<? extends Icon> image) throws JVGParseException {
		if (image != null) {
			ImageResource<? extends Icon> imageResource = (ImageResource<? extends Icon>) image;
			return "url(" + imageResource.getSource() + ")";
		} else {
			return null;
		}
	}

	private void setPainters(JVGShape shape, Element componentElement) throws JVGParseException {
		OutlinePainter outlinPainter = shape.getPainter(OutlinePainter.class);
		if (outlinPainter != null) {
			getStroke(outlinPainter, componentElement);
		}

		FillPainter fillPaint = shape.getPainter(FillPainter.class);
		if (fillPaint != null) {
			getFill(fillPaint, componentElement);
		}

		// ShadowPainter, EndingsPainter
	}

	private void getStroke(OutlinePainter painter, Element componentElement) {
		Resource<?> strokeResource = painter.getStroke();
		if (strokeResource == null) {
			return;
		}

		Draw draw = painter.getPaint();
		if (draw == null) {
			return;
		}

		Resource drawResource = draw.getResource();
		if (drawResource == null) {
			return;
		}

		Object drawObject = drawResource.getResource();
		if (drawObject instanceof Color) {
			String color = getColor(drawResource);
			if (color != null) {
				componentElement.setAttribute("stroke", color);
			}
		} else if (drawObject instanceof LinearGradient || drawObject instanceof RadialGradient) {
			resources.addResource(drawResource);
			componentElement.setAttribute("stroke", "url(#" + drawResource.getName() + ")");
		}

		Stroke stroke = (Stroke) strokeResource.getResource();
		if (stroke instanceof BasicStroke) {
			double opacity = draw.getOpacity();
			if (opacity != 1) {
				componentElement.setAttribute("fill-opacity", Double.toString(opacity));
			}

			BasicStroke s = (BasicStroke) stroke;
			if (s.getLineWidth() != 1.0f) {
				componentElement.setAttribute("stroke-width", Float.toString(s.getLineWidth()));
			}

			if (s.getEndCap() == BasicStroke.CAP_ROUND) {
				componentElement.setAttribute("stroke-linecap", "round");
			} else if (s.getEndCap() == BasicStroke.CAP_SQUARE) {
				componentElement.setAttribute("stroke-linecap", "square");
			}

			if (s.getMiterLimit() != 4) {
				componentElement.setAttribute("stroke-miterlimit", Float.toString(s.getMiterLimit()));
			}

			if (s.getLineJoin() == BasicStroke.JOIN_BEVEL) {
				componentElement.setAttribute("stroke-linejoin", "bevel");
			} else if (s.getLineJoin() == BasicStroke.JOIN_ROUND) {
				componentElement.setAttribute("stroke-linejoin", "round");
			}

			if (s.getDashPhase() != 0f) {
				componentElement.setAttribute("stroke-dashoffset", Float.toString(s.getDashPhase()));
			}

			if (s.getDashArray() != null && s.getDashArray().length > 0) {
				componentElement.setAttribute("stroke-dasharray", JVGParseUtil.getValue(s.getDashArray(), " "));
			}
		}
	}

	private void getFill(Painter painter, Element componentElement) throws JVGParseException {
		Draw draw = painter.getPaint();
		if (draw == null) {
			return;
		}

		Resource drawResource = draw.getResource();
		if (drawResource == null) {
			return;
		}

		Object drawObject = drawResource.getResource();
		if (drawObject instanceof Color) {
			String color = getColor(drawResource);
			if (color != null) {
				componentElement.setAttribute("fill", color);
			}
			//		} else if (drawObject instanceof Texture) {
			//			String texture = getTexture(drawResource);
			//			drawElement.setAttribute("type", "texture");
			//			drawElement.setAttribute("source", texture);
			//			return true;
		} else if (drawObject instanceof LinearGradient || drawObject instanceof RadialGradient) {
			resources.addResource(drawResource);
			componentElement.setAttribute("fill", "url(#" + drawResource.getName() + ")");
		}

		double opacity = draw.getOpacity();
		if (opacity != 1) {
			componentElement.setAttribute("fill-opacity", Double.toString(opacity));
		}
	}

	private String getColor(Resource<Color> resource) {
		return resource != null ? JVGParseUtil.getColor(resource.getResource()) : null;
	}

	//	private String getTexture(Resource<Texture> resource) throws JVGParseException {
	//		if (resource != null) {
	//			Texture texture = resource.getResource();
	//			String url = getImage(texture.getImage());
	//			if (url != null) {
	//				String value = url;
	//				if (texture.hasAnchor()) {
	//					value += "; " + texture.getAnchorX() + "," + texture.getAnchorY() + "," + texture.getAnchorWidth() + "," + texture.getAnchorHeight();
	//				}
	//				return value;
	//			}
	//		}
	//		return null;
	//	}

	public String getLinearGradient(Resource<LinearGradient> resource, boolean checkResource) {
		String id = resource.getName();
		if (checkResource && id != null) {
			resources.addResource(resource);
			return "url(#" + id + ")";
		} else {
			LinearGradient g = resource.getResource();
			String value = g.getX1() + "," + g.getY1() + "," + g.getX2() + "," + g.getY2() + ";";
			if (g.getCycleMethod() == MultipleGradientPaint.NO_CYCLE) {
				value += "no;"; // default
			} else if (g.getCycleMethod() == MultipleGradientPaint.REFLECT) {
				value += "reflect;";
			} else if (g.getCycleMethod() == MultipleGradientPaint.REPEAT) {
				value += "repeat;";
			}
			for (int i = 0; i < g.getColors().length; i++) {
				if (i > 0) {
					value += " ";
				}
				value += getColor(g.getColors()[i]) + " " + g.getFractions()[i];
			}
			return value;
		}
	}

	public String getRadialGradient(Resource<RadialGradient> resource, boolean checkResource) {
		String id = resource.getName();
		if (checkResource && id != null) {
			resources.addResource(resource);
			return JVGParseUtil.ID_PREFIX + id;
		} else {
			RadialGradient g = resource.getResource();
			String value = g.getCX() + "," + g.getCY() + "," + g.getFX() + "," + g.getFY() + "," + g.getR() + ";";
			if (g.getCycleMethod() == MultipleGradientPaint.NO_CYCLE) {
				value += "no;"; // default
			} else if (g.getCycleMethod() == MultipleGradientPaint.REFLECT) {
				value += "reflect;";
			} else if (g.getCycleMethod() == MultipleGradientPaint.REPEAT) {
				value += "repeat;";
			}
			for (int i = 0; i < g.getColors().length; i++) {
				if (i > 0) {
					value += " ";
				}
				value += getColor(g.getColors()[i]) + " " + g.getFractions()[i];
			}
			return value;
		}
	}

	private String getFont(Resource<Font> font) {
		if (font != null) {
			String id = font.getName();
			if (id != null) {
				resources.addResource(font);
				return JVGParseUtil.ID_PREFIX + id;
			} else {
				return JVGParseUtil.getFont(font.getResource());
			}
		} else {
			return null;
		}
	}

	private static char[] curveChars = { 'M', 'L', 'Q', 'C', 'Z' };

	private void setPath(PathIterator iter, Element componentElement) {
		double[] coords = new double[6];
		StringBuilder value = new StringBuilder();
		while (!iter.isDone()) {
			int type = iter.currentSegment(coords);
			if (value.length() > 0) {
				value.append(" ");
			}
			value.append(curveChars[type]);

			int index = 0;
			int coordCount = CoordinablePathIterator.curvesize[type];
			for (int i = 0; i < coordCount; i++) {
				if (index > 0 && coords[i] > 0) {
					value.append(" ");
				}
				value.append(JVGParseUtil.getValue(coords[i]));
				index++;
			}

			iter.next();
		}

		if (value.length() > 0) {
			componentElement.setAttribute("d", value.toString());
		}
	}

	// Supported resources:
	// color, stroke, font, image, script, linear gradient, radial gradient, transform 
	public Element buildDefs(JVGResources resources) {
		Element resourcesElement = null;
		for (Class resourceClass : resources.getClasses()) {
			int count = resources.getResourcesCount(resourceClass);
			for (int i = 0; i < count; i++) {
				Resource resource = resources.getResource(resourceClass, i);
				Element resourceElement = new Element("resource");
				resourceElement.setAttribute("name", resource.getName());

				if (resourcesElement == null) {
					resourcesElement = new Element("resources");
				}

				if (resource instanceof LinearGradientResource) {
					resourceElement.setAttribute("type", "linear-gradient");
					resourceElement.setAttribute("value", getLinearGradient(resource, false));
				} else if (resource instanceof RadialGradientResource) {
					resourceElement.setAttribute("type", "radial-gradient");
					resourceElement.setAttribute("value", getRadialGradient(resource, false));
				}

				if (resourceElement.getAttribute("type") != null) {
					resourcesElement.addContent(resourceElement);
				}
			}
		}
		return resourcesElement;
	}
}
