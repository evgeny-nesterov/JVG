package ru.nest.jvg.parser.svg;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.gradient.Gradient;
import javax.swing.gradient.LinearGradient;
import javax.swing.gradient.MultipleGradientPaint;
import javax.swing.gradient.RadialGradient;

import org.jdom2.Element;
import org.jdom2.Namespace;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGContainer;
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

	private long id = 0;

	private Element defsElement;

	@Override
	public Element build(JVGComponent[] components) throws JVGParseException {
		Element rootElement = new Element("svg", xmlns);

		if (pane != null) {
			if (pane.isDocumentSizeSet()) {
				rootElement.setAttribute("width", Integer.toString(pane.getDocumentSize().width));
				rootElement.setAttribute("height", Integer.toString(pane.getDocumentSize().height));
				rootElement.setAttribute("viewBox", "0 0 " + pane.getDocumentSize().width + " " + pane.getDocumentSize().height);
			}

			if (pane.isBackgroundSet() && !Color.white.equals(pane.getBackground())) {
				Element backgroundElement = new Element("rect", xmlns);
				backgroundElement.setAttribute("x", "0%");
				backgroundElement.setAttribute("y", "0%");
				backgroundElement.setAttribute("width", "100%");
				backgroundElement.setAttribute("height", "100%");
				backgroundElement.setAttribute("fill", JVGParseUtil.getColor(pane.getBackground()));
				rootElement.addContent(backgroundElement);
			}
		}
		rootElement.setAttribute("version", "1.0");

		defsElement = new Element("defs", xmlns);
		List<Element> childrenElements = buildComponents(components);
		buildDefs(resources);

		if (defsElement != null) {
			rootElement.addContent(defsElement);
		}
		for (Element e : childrenElements) {
			rootElement.addContent(e);
		}
		return rootElement;
	}

	private String nextId() {
		String name = "jvg" + id;
		while (resources.getResource(Resource.class, name) != null) {
			id++;
			name = "jvg" + id;
		}
		return name;
	}

	private String getResourceUrl(Resource<?> resource) {
		if (resource.getName() == null) {
			resource.setName(nextId());
		}
		return "url(#" + resource.getName() + ")";
	}

	private List<Element> buildComponents(JVGComponent[] components) throws JVGParseException {
		List<Element> childrenElements = new ArrayList<>();
		for (int i = 0; i < components.length; i++) {
			if (components[i] instanceof JVGShape) {
				JVGShape shape = (JVGShape) components[i];
				if (!shape.isClip()) {
					Element componentElement = buildComponent(shape);
					if (componentElement != null) {
						childrenElements.add(componentElement);
					}
				}
			}
		}
		return childrenElements;
	}

	private void buildClipPath(JVGShape shape, Element shapeElement) throws JVGParseException {
		Element clipPathElement = new Element("clipPath", xmlns);
		for (int i = 0; i < shape.getChildCount(); i++) {
			JVGComponent child = shape.getChild(i);
			if (child instanceof JVGShape) {
				JVGShape childShape = (JVGShape) child;
				if (childShape.isClip()) {
					Element childElement = buildComponent(childShape);
					if (childElement != null) {
						clipPathElement.addContent(childElement);
					}
				}
			}
		}
		if (clipPathElement.getChildren().size() > 0) {
			String id = nextId();
			clipPathElement.setAttribute("id", id);
			shapeElement.setAttribute("clip-path", "url(#" + id + ")");
			defsElement.addContent(clipPathElement);
		}
	}

	private Element buildComponent(JVGShape shape) throws JVGParseException {
		Element componentElement = null;
		if (shape instanceof JVGGroup) {
			if (shape.isClip()) {
				return null;
			}
			componentElement = new Element("g", xmlns);
			JVGGroup group = (JVGGroup) shape;
			for (int i = 0; i < group.getChildCount(); i++) {
				JVGComponent child = group.getChild(i);
				if (child instanceof JVGShape) {
					JVGShape childShape = (JVGShape) child;
					Element childElement = buildComponent(childShape);
					if (childElement != null) {
						componentElement.addContent(childElement);
					}
				}
			}
		} else if (shape instanceof JVGImage) {
			if (shape.isClip()) {
				return null;
			}
			componentElement = new Element("image", xmlns);
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
			componentElement = new Element("text", xmlns);
			JVGTextField c = (JVGTextField) shape;
			Font font = c.getFont() != null ? c.getFont().getResource() : null;
			if (font != null) {
				componentElement.setAttribute("font", font.getFontName());
				componentElement.setAttribute("font-size", Integer.toString(font.getSize()));
			}
			componentElement.setText(c.getText());
		} else {
			componentElement = new Element("path", xmlns);
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

		String name = shape.getName();
		if (name == null || name.length() == 0) {
			name = shape.getId().toString();
		}
		componentElement.setAttribute("id", name);

		if (!shape.isClip()) {
			setPainters(shape, componentElement);

			int opacity = shape.getAlfa();
			if (opacity != 255) {
				componentElement.setAttribute("opacity", Integer.toString(opacity));
			}

			if (!shape.isVisible()) {
				componentElement.setAttribute("display", "none");
			}

			//			if (component.getName() != null) {
			//				componentElement.setAttribute("name", component.getName());
			//			}

			//			if (component.isClipped()) {
			//				componentElement.setAttribute("clip", "yes");
			//			}

			setProperties(shape, componentElement);
			buildClipPath(shape, componentElement);
		}
		return componentElement;
	}

	private void setProperties(JVGComponent component, Element componentElement) {
		Map<String, String> hash = (Map<String, String>) component.getClientProperty("component-properties");
		if (hash != null && hash.size() > 0) {
			for (Map.Entry<String, String> entry : hash.entrySet()) {
				// TODO
			}
		}
	}

	private void setTransform(AffineTransform transform, Element element) {
		if (transform != null) {
			int type = transform.getType();
			if (!transform.isIdentity()) {
				double[] matrix = new double[6];
				transform.getMatrix(matrix);
				if (type == AffineTransform.TYPE_TRANSLATION) {
					element.setAttribute("transform", "translate(" + JVGParseUtil.getValue(transform.getTranslateX()) + "," + JVGParseUtil.getValue(transform.getTranslateY()) + ")");
				} else if (type == AffineTransform.TYPE_GENERAL_SCALE || type == AffineTransform.TYPE_UNIFORM_SCALE || type == AffineTransform.TYPE_FLIP || type == (AffineTransform.TYPE_GENERAL_SCALE | AffineTransform.TYPE_FLIP) || type == (AffineTransform.TYPE_UNIFORM_SCALE | AffineTransform.TYPE_FLIP)) {
					element.setAttribute("transform", "scale(" + JVGParseUtil.getValue(transform.getScaleX()) + "," + JVGParseUtil.getValue(transform.getScaleY()) + ")");
				} else if (type == AffineTransform.TYPE_GENERAL_ROTATION || type == AffineTransform.TYPE_QUADRANT_ROTATION) {
					double cos = matrix[0];
					double sin = matrix[1];
					element.setAttribute("transform", "rotate(" + JVGParseUtil.getValue(Math.toDegrees((sin < 0 ? -1 : 1) * Math.acos(cos))) + ")");
				} else {
					element.setAttribute("transform", "matrix(" + JVGParseUtil.getValue(matrix[0]) + "," + JVGParseUtil.getValue(matrix[1]) + "," + JVGParseUtil.getValue(matrix[2]) + "," + JVGParseUtil.getValue(matrix[3]) + "," + JVGParseUtil.getValue(matrix[4]) + "," + JVGParseUtil.getValue(matrix[5]) + ")");
				}
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
		} else if (outlinPainter != null) {
			componentElement.setAttribute("fill-opacity", "0");
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
			componentElement.setAttribute("stroke", getResourceUrl(drawResource));
		}

		Stroke stroke = (Stroke) strokeResource.getResource();
		if (stroke instanceof BasicStroke) {
			double opacity = draw.getOpacity();
			if (opacity != 1) {
				componentElement.setAttribute("stroke-opacity", Double.toString(opacity));
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
			componentElement.setAttribute("fill", getResourceUrl(drawResource));
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

	public <G extends Gradient> void getGradient(Resource<G> resource, Element element) {
		G g = resource.getResource();
		if (g.getCycleMethod() == MultipleGradientPaint.REFLECT) {
			element.setAttribute("spreadMethod", "reflect");
		} else if (g.getCycleMethod() == MultipleGradientPaint.REPEAT) {
			element.setAttribute("spreadMethod", "repeat");
		}

		setTransform(g.getTransform(), element);

		for (int i = 0; i < g.getColors().length; i++) {
			Element stop = new Element("stop", xmlns);
			stop.setAttribute("offset", Float.toString(g.getFractions()[i]));
			Color color = g.getColors()[i].getResource();
			if (color != null) {
				float opacity = color.getAlpha() / 255f;
				if (opacity != 1f) {
					stop.setAttribute("stop-opacity", Float.toString(opacity));
					color = new Color(color.getRed(), color.getGreen(), color.getBlue());
				}
				stop.setAttribute("stop-color", JVGParseUtil.getColor(color));
			}
			element.addContent(stop);
		}
	}

	public void getLinearGradient(Resource<LinearGradient> resource, Element element) {
		LinearGradient g = resource.getResource();
		element.setAttribute("x1", Float.toString(g.getX1()));
		element.setAttribute("y1", Float.toString(g.getY1()));
		element.setAttribute("x2", Float.toString(g.getX2()));
		element.setAttribute("y2", Float.toString(g.getY2()));
		getGradient(resource, element);
	}

	public void getRadialGradient(Resource<RadialGradient> resource, Element element) {
		RadialGradient g = resource.getResource();
		element.setAttribute("cx", Float.toString(g.getCX()));
		element.setAttribute("cy", Float.toString(g.getCY()));
		element.setAttribute("fx", Float.toString(g.getFX()));
		element.setAttribute("fy", Float.toString(g.getFY()));
		element.setAttribute("r", Float.toString(g.getR()));
		getGradient(resource, element);
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
				if (index > 0) {
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
	public void buildDefs(JVGResources resources) {
		for (Class resourceClass : resources.getClasses()) {
			int count = resources.getResourcesCount(resourceClass);
			for (int i = 0; i < count; i++) {
				Resource resource = resources.getResource(resourceClass, i);
				Element resourceElement = null;
				if (resource instanceof LinearGradientResource) {
					resourceElement = new Element("linearGradient", xmlns);
					getLinearGradient(resource, resourceElement);
				} else if (resource instanceof RadialGradientResource) {
					resourceElement = new Element("radialGradient", xmlns);
					getRadialGradient(resource, resourceElement);
				}

				if (resourceElement != null && resource.getName() != null) {
					resourceElement.setAttribute("id", resource.getName());
					defsElement.addContent(resourceElement);
				}
			}
		}
	}
}
