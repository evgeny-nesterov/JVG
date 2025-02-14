package ru.nest.jvg.parser.versions;

import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.gradient.Gradient.GradientUnitsType;
import javax.swing.gradient.LinearGradient;
import javax.swing.gradient.MultipleGradientPaint;
import javax.swing.gradient.RadialGradient;
import javax.swing.text.StyleConstants;

import org.jdom2.CDATA;
import org.jdom2.Element;

import ru.nest.expression.NumberValue;
import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGContainer;
import ru.nest.jvg.JVGFactory;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.JVGRoot;
import ru.nest.jvg.actionarea.JVGActionArea;
import ru.nest.jvg.actionarea.JVGCustomActionArea;
import ru.nest.jvg.geom.CoordinablePathIterator;
import ru.nest.jvg.parser.JVGBuilder;
import ru.nest.jvg.parser.JVGBuilderInterface;
import ru.nest.jvg.parser.JVGParseException;
import ru.nest.jvg.parser.JVGParseUtil;
import ru.nest.jvg.parser.JVGParser;
import ru.nest.jvg.parser.JVGVersion;
import ru.nest.jvg.resource.ColorResource;
import ru.nest.jvg.resource.FontResource;
import ru.nest.jvg.resource.ImageResource;
import ru.nest.jvg.resource.JVGResources;
import ru.nest.jvg.resource.LinearGradientResource;
import ru.nest.jvg.resource.RadialGradientResource;
import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.resource.Script;
import ru.nest.jvg.resource.ScriptResource;
import ru.nest.jvg.resource.StrokeResource;
import ru.nest.jvg.resource.Texture;
import ru.nest.jvg.resource.TransformResource;
import ru.nest.jvg.shape.JVGComplexShape;
import ru.nest.jvg.shape.JVGGroup;
import ru.nest.jvg.shape.JVGGroupPath;
import ru.nest.jvg.shape.JVGImage;
import ru.nest.jvg.shape.JVGPath;
import ru.nest.jvg.shape.JVGShape;
import ru.nest.jvg.shape.JVGStyledText;
import ru.nest.jvg.shape.JVGSubPath;
import ru.nest.jvg.shape.JVGTextField;
import ru.nest.jvg.shape.paint.Draw;
import ru.nest.jvg.shape.paint.Endings;
import ru.nest.jvg.shape.paint.EndingsPainter;
import ru.nest.jvg.shape.paint.FillPainter;
import ru.nest.jvg.shape.paint.OutlinePainter;
import ru.nest.jvg.shape.paint.Painter;
import ru.nest.jvg.shape.paint.ShadowPainter;
import ru.nest.jvg.shape.text.JVGStyleConstants;

public class JVGBuilder_1_0 extends JVGBuilder implements JVGBuilderInterface {
	private final static Map<Integer, String> shadows = new HashMap<>();

	private final static Map<Integer, String> endings_types = new HashMap<>();

	private final static Map<Integer, String> endings_figures = new HashMap<>();

	static {
		shadows.put(ShadowPainter.SLOPE_LEFT_BACK, "slope-left-back");
		shadows.put(ShadowPainter.SLOPE_LEFT_FORWARD, "slope-left-forward");
		shadows.put(ShadowPainter.SLOPE_RIGHT_BACK, "slope-right-back");
		shadows.put(ShadowPainter.SLOPE_RIGHT_FORWARD, "slope-right-forward");
		shadows.put(ShadowPainter.STRAIGHT__LEFT_BOTTOM, "stright-left-bottom");
		shadows.put(ShadowPainter.STRAIGHT__LEFT_TOP, "stright-left-top");
		shadows.put(ShadowPainter.STRAIGHT__RIGHT_BOTTOM, "stright-right-bottom");
		shadows.put(ShadowPainter.STRAIGHT__RIGHT_TOP, "stright-right-top");

		endings_types.put(Endings.TYPE_ALL_ENDINGS, "all");
		endings_types.put(Endings.TYPE_BOTH_ENDING, "both");
		endings_types.put(Endings.TYPE_FIRST_ENDING, "first");
		endings_types.put(Endings.TYPE_LAST_ENDING, "last");

		endings_figures.put(Endings.FIGURE_ARROW, "arrow");
		endings_figures.put(Endings.FIGURE_CIRCLE, "circle");
		endings_figures.put(Endings.FIGURE_SQUARE, "square");
		endings_figures.put(Endings.FIGURE_ROMB, "romb");
	}

	@Override
	public Element build(JVGComponent[] components) throws JVGParseException {
		Element rootElement = new Element("jvg");
		rootElement.setAttribute("version", JVGVersion.VERSION);

		if (pane != null) {
			Element documentElement = new Element("document");

			boolean add = false;
			if (pane.isDocumentSizeSet()) {
				documentElement.setAttribute("width", Integer.toString(pane.getDocumentSize().width));
				documentElement.setAttribute("height", Integer.toString(pane.getDocumentSize().height));
				add = true;
			}

			if (pane.isBackgroundSet()) {
				documentElement.setAttribute("color", JVGParseUtil.getColor(pane.getBackground()));
			}

			if (add) {
				rootElement.addContent(documentElement);
			}
		}

		Element componentsElement = new Element("components");
		rootElement.addContent(componentsElement);
		build(components, componentsElement);
		setScripts(rootElement);

		Element resourcesElement = buildResources(resources);
		if (resourcesElement != null) {
			rootElement.addContent(resourcesElement);
		}
		return rootElement;
	}

	private void build(JVGComponent[] components, Element componentsElement) throws JVGParseException {
		for (int i = 0; i < components.length; i++) {
			if (!(components[i] instanceof JVGActionArea) || (components[i] instanceof JVGCustomActionArea)) {
				Element componentElement = new Element("component");
				componentsElement.addContent(componentElement);
				buildComponent(components[i], componentElement);
			}
		}
	}

	private void buildComponent(JVGComponent component, Element componentElement) throws JVGParseException {
		String type = getType(component);
		if (type != null) {
			componentElement.setAttribute("type", type);
		}

		componentElement.setAttribute("id", component.getId().toString());

		if (component.getName() != null) {
			componentElement.setAttribute("name", component.getName());
		}

		if (!component.isVisible()) {
			componentElement.setAttribute("visible", "no");
		}

		if (component.isClipped()) {
			componentElement.setAttribute("clip", "yes");
		}

		if (component instanceof JVGShape) {
			JVGShape shape = (JVGShape) component;

			if (shape.isClip()) {
				componentElement.setAttribute("isclip", "yes");
				componentElement.setAttribute("visible", "no");
			}

			if (shape.isAntialias()) {
				componentElement.setAttribute("antialias", "yes");
			}

			// painters
			setPainters(shape, componentElement);

			// form
			Element formElement = new Element("form");
			if (setForm(shape, formElement)) {
				componentElement.addContent(formElement);
			}

			// transform
			AffineTransform transform;
			JVGContainer parent = component.getParent();
			if (parent instanceof JVGShape) {
				JVGShape parentShape = (JVGShape) parent;
				transform = (AffineTransform) shape.getTransform().clone();
				transform.preConcatenate(parentShape.getInverseTransform());
			} else {
				transform = shape.getTransform();
			}

			setTransform(transform, componentElement);

			// alfa
			int alfa = shape.getAlfa();
			if (alfa != 255) {
				componentElement.setAttribute("alfa", Integer.toString(alfa));
			}
		} else if (component instanceof JVGCustomActionArea) {
			JVGCustomActionArea control = (JVGCustomActionArea) component;
			setControl(control, componentElement);
		}

		// scripts
		setScripts(component, componentElement);

		// properties
		setProperties(component, componentElement);

		// container
		if (component instanceof JVGContainer) {
			JVGContainer container = (JVGContainer) component;
			Element childsElement = null;
			for (int i = 0; i < container.getChildCount(); i++) {
				JVGComponent child = container.getChild(i);
				if (!(child instanceof JVGActionArea) || (child instanceof JVGCustomActionArea)) {
					if (childsElement == null) {
						childsElement = new Element("components");
						componentElement.addContent(childsElement);
					}
					Element childElement = new Element("component");
					childsElement.addContent(childElement);
					buildComponent(child, childElement);
				}
			}
		}
	}

	private void setScripts(JVGComponent component, Element componentElement) {
		Element scriptsElement = new Element("scripts");
		boolean thereAreScripts = false;

		for (Script.Type type : Script.types) {
			ScriptResource script = (ScriptResource) component.getClientProperty(type.getActionName());
			if (script != null && script.getResource().getData() != null && script.getResource().getData().length() > 0) {
				thereAreScripts = true;

				Element scriptElement = new Element("script");
				scriptElement.setAttribute("type", type.getName());

				if (script.getName() != null && script.getName().length() > 0) {
					scriptElement.setAttribute("ref", script.getName());
					resources.addResource(script);
				} else {
					scriptElement.addContent(new CDATA(script.getResource().getData()));
				}
				if (script.getPostScript() != null) {
					String postScriptName = script.getPostScript().getName();
					if (postScriptName == null || postScriptName.length() == 0) {
						postScriptName = "script" + script.getPostScript().getResource().getData().hashCode();
					}
					resources.addResource(script.getPostScript());
					scriptElement.setAttribute("post", postScriptName);
				}
				scriptsElement.addContent(scriptElement);
			}
		}

		if (thereAreScripts) {
			componentElement.addContent(scriptsElement);
		}
	}

	private void setScripts(Element jvgElement) {
		if (pane != null) {
			Element scriptsElement = new Element("scripts");
			boolean thereAreScripts = false;

			for (Script.Type type : Script.types) {
				ScriptResource script = (ScriptResource) pane.getClientProperty(type.getActionName());
				if (script != null && script.getResource().getData() != null && script.getResource().getData().length() > 0) {
					thereAreScripts = true;

					Element scriptElement = new Element("script");
					scriptElement.setAttribute("type", type.getName());

					if (script.getName() != null && script.getName().length() > 0) {
						scriptElement.setAttribute("ref", script.getName());
						resources.addResource(script);
					} else {
						scriptElement.addContent(new CDATA(script.getResource().getData()));
					}
					scriptsElement.addContent(scriptElement);
				}
			}

			if (thereAreScripts) {
				jvgElement.addContent(scriptsElement);
			}
		}
	}

	private void setProperties(JVGComponent component, Element componentElement) {
		Map<String, String> hash = (Map<String, String>) component.getClientProperty("component-properties");
		if (hash != null && hash.size() > 0) {
			Element propertiesElement = new Element("properties");
			componentElement.addContent(propertiesElement);

			for (Map.Entry<String, String> entry : hash.entrySet()) {
				Element propertyElement = new Element("property");
				propertiesElement.addContent(propertyElement);

				propertyElement.setAttribute("name", entry.getKey());
				propertyElement.setAttribute("value", entry.getValue());
			}
		}
	}

	private void setTransform(AffineTransform transform, Element componentElement) {
		int type = transform.getType();
		if (!transform.isIdentity()) {
			Element transformElement = new Element("transform");
			componentElement.addContent(transformElement);

			double[] matrix = new double[6];
			transform.getMatrix(matrix);

			if (type == AffineTransform.TYPE_TRANSLATION) {
				transformElement.setAttribute("type", "translate");
				transformElement.setAttribute("value", JVGParseUtil.getValue(transform.getTranslateX()) + ";" + JVGParseUtil.getValue(transform.getTranslateY()));
			} else if (type == AffineTransform.TYPE_GENERAL_SCALE || type == AffineTransform.TYPE_UNIFORM_SCALE || type == AffineTransform.TYPE_FLIP || type == (AffineTransform.TYPE_GENERAL_SCALE | AffineTransform.TYPE_FLIP) || type == (AffineTransform.TYPE_UNIFORM_SCALE | AffineTransform.TYPE_FLIP)) {
				transformElement.setAttribute("type", "scale");
				transformElement.setAttribute("value", JVGParseUtil.getValue(transform.getScaleX()) + ";" + JVGParseUtil.getValue(transform.getScaleY()));
			} else if (type == AffineTransform.TYPE_GENERAL_ROTATION || type == AffineTransform.TYPE_QUADRANT_ROTATION) {
				double cos = matrix[0];
				double sin = matrix[1];
				transformElement.setAttribute("type", "rotate");
				transformElement.setAttribute("value", JVGParseUtil.getValue((sin < 0 ? -1 : 1) * Math.acos(cos)));
			} else {
				transformElement.setAttribute("type", "matrix");
				transformElement.setAttribute("value", JVGParseUtil.getValue(matrix[0]) + ";" + JVGParseUtil.getValue(matrix[1]) + ";" + JVGParseUtil.getValue(matrix[2]) + ";" + JVGParseUtil.getValue(matrix[3]) + ";" + JVGParseUtil.getValue(matrix[4]) + ";" + JVGParseUtil.getValue(matrix[5]));
			}
		}
	}

	private String getImage(Resource<? extends Icon> image) {
		if (image != null) {
			String id = image.getName();
			if (id != null) {
				resources.addResource(image);
				return JVGParseUtil.ID_PREFIX + id;
			} else {
				ImageResource<? extends Icon> imageResource = (ImageResource<? extends Icon>) image;
				return imageResource.getSource();
			}
		} else {
			return null;
		}
	}

	private boolean setForm(JVGShape component, Element formElement) throws JVGParseException {
		if (component instanceof JVGImage) {
			JVGImage image = (JVGImage) component;
			String source = getImage(image.getImage());
			if (source != null) {
				formElement.setAttribute("source", source);
				return true;
			} else {
				ImageResource r = (ImageResource) image.getImage();
				byte[] data = r.getData();
				String base64data = new String(Base64.getEncoder().encode(data), StandardCharsets.UTF_8);
				formElement.setAttribute("datatype", "base64");
				formElement.setAttribute("source", base64data);
				return true;
			}
		} else if (component instanceof JVGStyledText) {
			boolean write = false;
			JVGStyledText c = (JVGStyledText) component;

			String font = getFont(c.getFont());
			if (font != null) {
				formElement.setAttribute("font", font);
				write = true;
			}

			boolean wrap = c.isWrap();
			if (wrap) {
				formElement.setAttribute("wrap", Double.toString(c.getWrapSize()));
				write = true;
			}

			write |= setTextDocument(c.getDocument(), formElement);
			return write;
		} else if (component instanceof JVGTextField) {
			JVGTextField c = (JVGTextField) component;

			String font = getFont(c.getFont());
			if (font != null) {
				formElement.setAttribute("font", font);
			}

			formElement.setText(c.getText());
			return true;
		} else if (component instanceof JVGGroup) {
			JVGGroup g = (JVGGroup) component;
			if (g.getPaintOrderType() != JVGGroup.PAINT_ORDER_COMPONENT) {
				switch (g.getPaintOrderType()) {
					case JVGGroup.PAINT_ORDER_FILL_FIRST:
						formElement.setAttribute("draw-order", "fill-first");
						break;
					case JVGGroup.PAINT_ORDER_OUTLINE_FIRST:
						formElement.setAttribute("draw-order", "outline-first");
						break;
				}
				return true;
			}
			return false;
		} else if (component instanceof JVGSubPath) {
			JVGSubPath shape = (JVGSubPath) component;
			if (shape.isLead()) {
				formElement.setAttribute("lead", "yes");
			} else {
				formElement.setAttribute("position", Double.toString(shape.getPosition()));
			}

			if (shape.getPathStroke() != null) {
				String pathStroke = getStroke(shape.getPathStroke());
				if (pathStroke != null) {
					formElement.setAttribute("path-stroke", pathStroke);
				}
			}
			return true;
		} else if (component instanceof JVGPath || component instanceof JVGGroupPath) {
			JVGPath shape = (JVGPath) component;
			setPath(shape.getPathShape().getPathIterator(null), formElement);
			if (shape.getPathStroke() != null) {
				String pathStroke = getStroke(shape.getPathStroke());
				if (pathStroke != null) {
					formElement.setAttribute("path-stroke", pathStroke);
				}
			}
			return true;
		} else if (component instanceof JVGComplexShape) {
			JVGComplexShape shape = (JVGComplexShape) component;
			formElement.setAttribute("source", shape.getURL().toExternalForm());

			Map<String, NumberValue> arguments = shape.getContext().getArguments();
			Iterator<String> iter = arguments.keySet().iterator();
			while (iter.hasNext()) {
				String id = iter.next();
				NumberValue value = arguments.get(id);

				Element paramElement = new Element("arg");
				formElement.addContent(paramElement);
				paramElement.setAttribute("id", id);
				paramElement.setAttribute("value", JVGParseUtil.getValue(value.getValue()));
			}
			return true;
		}

		throw new JVGParseException("Unsupported component type: " + component.getClass());
	}

	private boolean setTextDocument(javax.swing.text.StyledDocument doc, Element e) throws JVGParseException {
		try {
			int start = 0;
			int end = doc.getLength();
			if (end == 0) {
				return false;
			}

			if (doc.getCharacterElement(start) == doc.getCharacterElement(end)) {
				javax.swing.text.Element paragraphElement = doc.getParagraphElement(start);
				setParagraphAttributes(paragraphElement.getAttributes(), e);

				javax.swing.text.Element characterElement = doc.getCharacterElement(start);
				setCharacterAttributes(characterElement.getAttributes(), e);

				String text = doc.getText(start, end);
				e.addContent(new CDATA(text));
			} else {
				while (start < end) {
					Element paragraph = new Element("paragraph");
					e.addContent(paragraph);

					javax.swing.text.Element paragraphElement = doc.getParagraphElement(start);
					setParagraphAttributes(paragraphElement.getAttributes(), paragraph);
					int next = Math.min(end, paragraphElement.getEndOffset());

					int startText = start, endText = next;
					while (startText < endText) {
						Element child = new Element("elem");
						paragraph.addContent(child);

						javax.swing.text.Element characterElement = doc.getCharacterElement(startText);
						setCharacterAttributes(characterElement.getAttributes(), child);
						int nextText = Math.min(endText, characterElement.getEndOffset());

						int len = nextText - startText;
						String text = doc.getText(startText, len);
						if (text.length() > 0) {
							child.addContent(new CDATA(text));
						}

						startText = nextText;
					}

					start = next;
				}
			}
			return true;
		} catch (javax.swing.text.BadLocationException exc) {
			exc.printStackTrace();
		}

		return false;
	}

	private final static String[] stylesList = { "underline", "strike", "sub", "sup" };

	private String getFillValue(Resource resource) throws JVGParseException {
		Object o = resource.getResource();
		if (o instanceof Color) {
			return getColor(resource);
		} else if (o instanceof LinearGradient) {
			return "lg(" + getLinearGradient(null, resource, true) + ")";
		} else if (o instanceof RadialGradient) {
			return "rg(" + getRadialGradient(null, resource, true) + ")";
		} else if (o instanceof Texture) {
			return "img(" + getTexture(resource) + ")";
		}
		throw new JVGParseException("unknown fill resource: " + resource);
	}

	private void setCharacterAttributes(javax.swing.text.AttributeSet attr, Element characterElement) throws JVGParseException {
		String fontFamily = StyleConstants.getFontFamily(attr);
		boolean isBold = StyleConstants.isBold(attr);
		boolean isItalic = StyleConstants.isItalic(attr);
		int size = StyleConstants.getFontSize(attr);
		String font = JVGParseUtil.getFont(fontFamily, isBold, isItalic, size);
		if (font != null) {
			characterElement.setAttribute("font", font);
		}

		Resource background = (Resource) attr.getAttribute(JVGStyleConstants.Background);
		if (background != null) {
			if (!ColorResource.white.equals(background.getResource())) {
				characterElement.setAttribute("background", getFillValue(background));
			}
		}

		Resource foreground = (Resource) attr.getAttribute(JVGStyleConstants.Foreground);
		if (foreground != null) {
			if (!ColorResource.white.equals(foreground.getResource())) {
				characterElement.setAttribute("foreground", getFillValue(foreground));
			}
		}

		// --- Set properties ---
		StringBuffer styles = appendStyle("strike", StyleConstants.isStrikeThrough(attr), null);
		styles = appendStyle("sub", StyleConstants.isSubscript(attr), styles);
		styles = appendStyle("sup", StyleConstants.isSuperscript(attr), styles);

		switch (JVGStyleConstants.getUnderline(attr)) {
			case JVGStyleConstants.UNDERLINE_LINE:
				appendStyle("underline", StyleConstants.isUnderline(attr), styles);
				break;

			case JVGStyleConstants.UNDERLINE_DOUBLE_LINE:
				appendStyle("underline-double", StyleConstants.isUnderline(attr), styles);
				break;

			case JVGStyleConstants.UNDERLINE_TRIPLE_LINE:
				appendStyle("underline-triple", StyleConstants.isUnderline(attr), styles);
				break;

			case JVGStyleConstants.UNDERLINE_ZIGZAG:
				appendStyle("underline-zigzag", StyleConstants.isUnderline(attr), styles);
				break;
		}

		if (styles != null) {
			characterElement.setAttribute("style", styles.toString());
		}
	}

	private static StringBuffer appendStyle(String name, boolean append, StringBuffer buf) {
		if (append) {
			if (buf == null) {
				buf = new StringBuffer();
			} else {
				buf.append(",");
			}

			buf.append(name);
		}
		return buf;
	}

	private void setParagraphAttributes(javax.swing.text.AttributeSet attr, Element paragraph) {
		int alignment = StyleConstants.getAlignment(attr);
		switch (alignment) // "left" on default
		{
			case StyleConstants.ALIGN_CENTER:
				paragraph.setAttribute("alignment", "center");
				break;

			case StyleConstants.ALIGN_RIGHT:
				paragraph.setAttribute("alignment", "right");
				break;

			case StyleConstants.ALIGN_JUSTIFIED:
				paragraph.setAttribute("alignment", "just");
				break;
		}

		// bullets
		int bulletsType = JVGStyleConstants.getParagraphBullets(attr);
		switch (bulletsType) {
			case JVGStyleConstants.BULLETS_CIRCLE:
				paragraph.setAttribute("bullets", "circle");
				break;

			case JVGStyleConstants.BULLETS_MINUS:
				paragraph.setAttribute("bullets", "minus");
				break;

			case JVGStyleConstants.BULLETS_SQURE:
				paragraph.setAttribute("bullets", "square");
				break;

			case JVGStyleConstants.BULLETS_NUMBER:
				paragraph.setAttribute("bullets", "numeric");
				break;

			case JVGStyleConstants.BULLETS_LETTER:
				paragraph.setAttribute("bullets", "letter");
				break;
		}

		if (bulletsType != JVGStyleConstants.BULLETS_NONE) {
			int bulletsIndentSize = JVGStyleConstants.getParagraphBulletsIndentSize(attr);
			if (bulletsIndentSize != JVGStyleConstants.defaultBulletsIndentSize) {
				paragraph.setAttribute("bullets-indent-size", Integer.toString(bulletsIndentSize));
			}

			int bulletsIndentCount = JVGStyleConstants.getParagraphBulletsIndentCount(attr);
			if (bulletsIndentCount != 0) {
				paragraph.setAttribute("bullets-indent-count", Integer.toString(bulletsIndentCount));
			}

			Resource<Color> bulletsColor = JVGStyleConstants.getParagraphBulletsColor(attr);
			if (bulletsColor != null && !Color.black.equals(bulletsColor)) {
				paragraph.setAttribute("bullets-color", getColor(bulletsColor));
			}

			Resource<Font> bulletsFont = JVGStyleConstants.getParagraphBulletsFont(attr);
			if (bulletsFont != null && !JVGStyleConstants.defaultBulletsFont.equals(bulletsFont)) {
				paragraph.setAttribute("bullets-font", getFont(bulletsFont));
			}
		}

		// border
		Border border = JVGStyleConstants.getParagraphBorder(attr);
		if (border != null) {
			JVGParseUtil.setBorder(paragraph, border);
		}

		// insets
		float i1 = StyleConstants.getSpaceAbove(attr);
		float i2 = StyleConstants.getLeftIndent(attr);
		float i3 = StyleConstants.getSpaceBelow(attr);
		float i4 = StyleConstants.getRightIndent(attr);
		if (i1 != 0 || i2 != 0 || i3 != 0 || i4 != 0) {
			paragraph.setAttribute("insets", i1 + "," + i2 + "," + i3 + "," + i4);
		}

		// lines
		float lineSpacing = StyleConstants.getLineSpacing(attr);
		if (lineSpacing != 0) {
			paragraph.setAttribute("line-spacing", Float.toString(lineSpacing));
		}
	}

	private void setPainters(JVGShape shape, Element componentElement) throws JVGParseException {
		int count = shape.getPaintersCount();
		if (count == 1) {
			Element paintElement = new Element("paint");
			if (setPaint(shape.getPainter(0), paintElement)) {
				componentElement.addContent(paintElement);
			}
		} else if (count > 1) {
			Element paintsElement = new Element("paints");
			boolean add = false;
			for (int i = 0; i < count; i++) {
				Element paintElement = new Element("paint");
				paintsElement.addContent(paintElement);
				add |= setPaint(shape.getPainter(i), paintElement);
			}
			if (add) {
				componentElement.addContent(paintsElement);
			}
		}
	}

	private boolean setPaint(Painter painter, Element paintElement) throws JVGParseException {
		if (!setDraw(painter, paintElement)) {
			return false;
		}

		if (painter instanceof FillPainter) {
			paintElement.setAttribute("type", "fill");
			return true;
		} else if (painter instanceof OutlinePainter) {
			paintElement.setAttribute("type", "outline");

			OutlinePainter outlinPainter = (OutlinePainter) painter;
			String stroke = getStroke(outlinPainter.getStroke());
			if (stroke != null) {
				paintElement.setAttribute("stroke", stroke);
			}
			return true;
		} else if (painter instanceof ShadowPainter) {
			ShadowPainter shadowPainter = (ShadowPainter) painter;
			paintElement.setAttribute("type", "shadow");

			if (shadowPainter.getShadowType() != ShadowPainter.STRAIGHT__RIGHT_TOP) {
				paintElement.setAttribute("shadow-type", shadows.get(shadowPainter.getShadowType()));
			}
			return true;
		} else if (painter instanceof EndingsPainter) {
			EndingsPainter epainter = (EndingsPainter) painter;
			paintElement.setAttribute("type", "endings");

			if (epainter.getEndings().getEndingType() != Endings.TYPE_LAST_ENDING) {
				paintElement.setAttribute("endings-type", endings_types.get(epainter.getEndings().getEndingType()));
			}

			if (!epainter.getEndings().isDirect()) {
				paintElement.setAttribute("direct", "no");
			}

			if (epainter.getEndings().getFigure() != Endings.FIGURE_ARROW) {
				paintElement.setAttribute("figure", endings_figures.get(epainter.getEndings().getFigure()));
			}

			if (!epainter.getEndings().isFill()) {
				paintElement.setAttribute("fill", "no");
			}
			return true;
		}
		return false;
	}

	private boolean setDraw(Painter painter, Element paintElement) throws JVGParseException {
		if (painter != null) {
			Draw draw = painter.getPaint();
			if (draw != null) {
				Resource drawResource = draw.getResource();
				if (drawResource != null) {
					Element drawElement = new Element("draw");
					if (setDraw(drawResource, drawElement)) {
						double opacity = draw.getOpacity();
						if (opacity != 1) {
							drawElement.setAttribute("opacity", Double.toString(opacity));
						}
						paintElement.addContent(drawElement);
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean setDraw(Resource drawResource, Element drawElement) throws JVGParseException {
		Object drawObject = drawResource.getResource();
		if (drawObject instanceof Color) {
			String color = getColor(drawResource);
			if (color != null) {
				drawElement.setAttribute("type", "color");
				drawElement.setAttribute("color", color);
				return true;
			}
		} else if (drawObject instanceof Texture) {
			String texture = getTexture(drawResource);
			drawElement.setAttribute("type", "texture");
			drawElement.setAttribute("source", texture);
			return true;
		} else if (drawObject instanceof LinearGradient) {
			String gradient = getLinearGradient(drawElement, drawResource, true);
			drawElement.setAttribute("type", "linear-gradient");
			drawElement.setAttribute("value", gradient);
			return true;
		} else if (drawObject instanceof RadialGradient) {
			drawElement.setAttribute("type", "radial-gradient");
			String gradient = getRadialGradient(drawElement, drawResource, true);
			drawElement.setAttribute("value", gradient);
			return true;
		}
		return false;
	}

	private String getColor(Resource<Color> resource) {
		if (resource != null) {
			String id = resource.getName();
			if (id != null) {
				resources.addResource(resource);
				return JVGParseUtil.ID_PREFIX + id;
			} else {
				return JVGParseUtil.getColor(resource.getResource());
			}
		}
		return null;
	}

	private String getTexture(Resource<Texture> resource) throws JVGParseException {
		if (resource != null) {
			String id = resource.getName();
			if (id != null) {
				resources.addResource(resource);
				return JVGParseUtil.ID_PREFIX + id;
			} else {
				Texture texture = resource.getResource();
				String url = getImage(texture.getImage());
				if (url != null) {
					String value = url;
					if (texture.hasAnchor()) {
						value += "; " + texture.getAnchorX() + "," + texture.getAnchorY() + "," + texture.getAnchorWidth() + "," + texture.getAnchorHeight();
					}
					return value;
				}
			}
		}
		return null;
	}

	public String getLinearGradient(Element element, Resource<LinearGradient> resource, boolean checkResource) {
		String id = resource.getName();
		if (checkResource && id != null) {
			resources.addResource(resource);
			return JVGParseUtil.ID_PREFIX + id;
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

			if (element != null && resource.getResource().getUnitsType() == GradientUnitsType.ABSOLUTE) {
				element.setAttribute("unitstype", "absolute");
			}
			return value;
		}
	}

	public String getRadialGradient(Element element, Resource<RadialGradient> resource, boolean checkResource) {
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

			if (element != null && resource.getResource().getUnitsType() == GradientUnitsType.ABSOLUTE) {
				element.setAttribute("unitstype", "absolute");
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

	private <S extends Stroke> String getStroke(Resource<S> stroke) {
		if (stroke != null) {
			String id = stroke.getName();
			if (id != null) {
				resources.addResource(stroke);
				return JVGParseUtil.ID_PREFIX + id;
			} else {
				return JVGParseUtil.getStroke(stroke.getResource());
			}
		} else {
			return null;
		}
	}

	private static Map<Class<?>, String> types = new HashMap<>();
	static {
		types.put(JVGGroup.class, "container");
		types.put(JVGPath.class, "path");
		types.put(JVGGroupPath.class, "pathgroup");
		types.put(JVGSubPath.class, "subpath");
		types.put(JVGStyledText.class, "text");
		types.put(JVGTextField.class, "textfield");
		types.put(JVGImage.class, "image");
		types.put(JVGComplexShape.class, "shape");
		types.put(JVGCustomActionArea.class, "control");
	}

	private String getType(JVGComponent component) {
		return types.get(component.getClass());
	}

	private static char[] curveChars = { 'M', 'L', 'Q', 'C', 'X' };

	private void setPath(PathIterator iter, Element e) {
		double[] coords = new double[6];
		StringBuilder value = new StringBuilder();
		while (!iter.isDone()) {
			int type = iter.currentSegment(coords);
			value.append(curveChars[type]);

			int coordCount = CoordinablePathIterator.curvesize[type];
			if (coordCount > 0) {
				value.append(" ");
			}

			for (int i = 0; i < coordCount; i++) {
				value.append(JVGParseUtil.getValue(coords[i]));
				if (i != coordCount - 1) {
					value.append(" ");
				}
			}

			iter.next();
			if (!iter.isDone()) {
				value.append(" ");
			}
		}

		if (value.length() > 0) {
			e.setAttribute("path", value.toString());
		}
	}

	private void setControl(JVGCustomActionArea control, Element componentElement) {
	}

	// Supported resources:
	// color, stroke, font, image, script, linear gradient, radial gradient, transform 
	public Element buildResources(JVGResources resources) {
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

				if (resource instanceof ColorResource) {
					ColorResource r = (ColorResource) resource;
					resourceElement.setAttribute("type", "color");
					resourceElement.setAttribute("value", JVGParseUtil.getColor(r.getResource()));
				} else if (resource instanceof StrokeResource) {
					StrokeResource r = (StrokeResource) resource;
					resourceElement.setAttribute("type", "stroke");
					resourceElement.setAttribute("value", JVGParseUtil.getStroke(r.getResource()));
				} else if (resource instanceof FontResource) {
					FontResource r = (FontResource) resource;
					resourceElement.setAttribute("type", "font");
					resourceElement.setAttribute("value", JVGParseUtil.getFont(r.getResource()));
				} else if (resource instanceof ImageResource) {
					// datatype=<base64, url(on default)>
					ImageResource r = (ImageResource) resource;
					String url = r.getSource();
					if (url != null) {
						resourceElement.setAttribute("type", "image");
						resourceElement.setAttribute("value", url);
					} else {
						byte[] data = r.getData();
						String base64data = new String(Base64.getEncoder().encode(data), StandardCharsets.UTF_8);
						resourceElement.setAttribute("type", "image");
						resourceElement.setAttribute("datatype", "base64");
						resourceElement.setAttribute("value", base64data);
					}
				} else if (resource instanceof ScriptResource) {
					ScriptResource script = (ScriptResource) resource;
					resourceElement.setAttribute("type", "script");
					String data = script.getResource().getData();
					if (data.indexOf('\n') == -1 && data.indexOf('"') == -1) {
						resourceElement.setAttribute("value", data);
					} else {
						resourceElement.addContent(new CDATA(data));
					}
				} else if (resource instanceof LinearGradientResource) {
					resourceElement.setAttribute("type", "linear-gradient");
					resourceElement.setAttribute("value", getLinearGradient(resourceElement, resource, false));
				} else if (resource instanceof RadialGradientResource) {
					resourceElement.setAttribute("type", "radial-gradient");
					resourceElement.setAttribute("value", getRadialGradient(resourceElement, resource, false));
				} else if (resource instanceof TransformResource) {
					resourceElement.setAttribute("type", "transform");
					// TODO
					resourceElement.setAttribute("value", null);
				}

				if (resourceElement.getAttribute("type") != null) {
					resourcesElement.addContent(resourceElement);
				}
			}
		}
		return resourcesElement;
	}

	public static void main(String[] args) {
		try {
			JVGParser parser = new JVGParser(JVGFactory.createDefault());
			parser.getResources().addResource(new ImageResource<ImageIcon>("image", "c:/Fon.tif"));

			JVGPane p = new JVGPane();
			JVGRoot root = parser.parse(JVGParser.class.getResourceAsStream("test.xml"));
			p.setRoot(root);

			JFrame f = new JFrame();
			f.setContentPane(new JScrollPane(p));
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			f.setLocation(400, 400);
			f.pack();
			f.setVisible(true);

			JVGBuilder_1_0 builder = new JVGBuilder_1_0();
			String s = builder.build(root.getChildren(), "UTF-8");
			System.out.println(s);

			p = new JVGPane();
			root = new JVGParser(JVGFactory.createDefault(), parser.getResources()).parse(s);
			p.setRoot(root);

			f = new JFrame();
			f.setContentPane(new JScrollPane(p));
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			f.setLocation(400, 400);
			f.pack();
			f.setVisible(true);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
}
