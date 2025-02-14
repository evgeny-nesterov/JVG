package ru.nest.jvg.parser.versions;

import org.jdom2.Element;
import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGComponentType;
import ru.nest.jvg.JVGContainer;
import ru.nest.jvg.JVGFactory;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.JVGRoot;
import ru.nest.jvg.JVGUtil;
import ru.nest.jvg.actionarea.JVGCustomActionArea;
import ru.nest.jvg.geom.MutableGeneralPath;
import ru.nest.jvg.parser.JVGParseException;
import ru.nest.jvg.parser.JVGParseUtil;
import ru.nest.jvg.parser.JVGParser;
import ru.nest.jvg.parser.JVGParserInterface;
import ru.nest.jvg.resource.ColorResource;
import ru.nest.jvg.resource.FontResource;
import ru.nest.jvg.resource.ImageResource;
import ru.nest.jvg.resource.JVGResources;
import ru.nest.jvg.resource.LinearGradientResource;
import ru.nest.jvg.resource.RadialGradientResource;
import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.resource.Script;
import ru.nest.jvg.resource.Script.Type;
import ru.nest.jvg.resource.ScriptResource;
import ru.nest.jvg.resource.StrokeResource;
import ru.nest.jvg.resource.Texture;
import ru.nest.jvg.resource.TextureResource;
import ru.nest.jvg.shape.JVGComplexShape;
import ru.nest.jvg.shape.JVGGroup;
import ru.nest.jvg.shape.JVGGroupPath;
import ru.nest.jvg.shape.JVGImage;
import ru.nest.jvg.shape.JVGPath;
import ru.nest.jvg.shape.JVGShape;
import ru.nest.jvg.shape.JVGStyledText;
import ru.nest.jvg.shape.JVGSubPath;
import ru.nest.jvg.shape.JVGTextField;
import ru.nest.jvg.shape.paint.ColorDraw;
import ru.nest.jvg.shape.paint.Draw;
import ru.nest.jvg.shape.paint.Endings;
import ru.nest.jvg.shape.paint.EndingsPainter;
import ru.nest.jvg.shape.paint.FillPainter;
import ru.nest.jvg.shape.paint.LinearGradientDraw;
import ru.nest.jvg.shape.paint.OutlinePainter;
import ru.nest.jvg.shape.paint.Painter;
import ru.nest.jvg.shape.paint.RadialGradientDraw;
import ru.nest.jvg.shape.paint.ShadowPainter;
import ru.nest.jvg.shape.paint.TextureDraw;
import ru.nest.jvg.shape.text.JVGStyleConstants;
import ru.nest.swing.gradient.Gradient.GradientUnitsType;
import ru.nest.swing.gradient.LinearGradient;
import ru.nest.swing.gradient.MultipleGradientPaint;
import ru.nest.swing.gradient.RadialGradient;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JVGParser_1_0 implements JVGParserInterface {
	protected final static Map<String, Integer> shadows = new HashMap<>();

	protected final static Map<String, Integer> endings_types = new HashMap<>();

	protected final static Map<String, Integer> endings_figures = new HashMap<>();

	static {
		shadows.put("slope-left-back", ShadowPainter.SLOPE_LEFT_BACK);
		shadows.put("slope-left-forward", ShadowPainter.SLOPE_LEFT_FORWARD);
		shadows.put("slope-right-back", ShadowPainter.SLOPE_RIGHT_BACK);
		shadows.put("slope-right-forward", ShadowPainter.SLOPE_RIGHT_FORWARD);
		shadows.put("stright-left-bottom", ShadowPainter.STRAIGHT__LEFT_BOTTOM);
		shadows.put("stright-left-top", ShadowPainter.STRAIGHT__LEFT_TOP);
		shadows.put("stright-right-bottom", ShadowPainter.STRAIGHT__RIGHT_BOTTOM);
		shadows.put("stright-right-top", ShadowPainter.STRAIGHT__RIGHT_TOP);

		endings_types.put("all", Endings.TYPE_ALL_ENDINGS);
		endings_types.put("both", Endings.TYPE_BOTH_ENDING);
		endings_types.put("first", Endings.TYPE_FIRST_ENDING);
		endings_types.put("last", Endings.TYPE_LAST_ENDING);

		endings_figures.put("arrow", Endings.FIGURE_ARROW);
		endings_figures.put("circle", Endings.FIGURE_CIRCLE);
		endings_figures.put("square", Endings.FIGURE_SQUARE);
		endings_figures.put("romb", Endings.FIGURE_ROMB);
	}

	protected JVGFactory factory;

	protected JVGResources resources;

	protected Dimension documentSize;

	protected Color documentColor;

	protected Map<String, ScriptResource> documentScripts = new HashMap<>();

	public JVGParser_1_0(JVGFactory factory, JVGResources resources) {
		this.factory = factory;
		this.resources = resources;
	}

	@Override
	public JVGResources getResources() {
		return resources;
	}

	@Override
	public Dimension getDocumentSize() {
		return documentSize;
	}

	@Override
	public Color getDocumentColor() {
		return documentColor;
	}

	@Override
	public Map<String, ScriptResource> getDocumentScripts() {
		return documentScripts;
	}

	@Override
	public void parse(Element rootElement, JVGContainer parent) throws JVGParseException {
		parseDocument(rootElement);
		parseResources(rootElement);
		parseDocumentScripts(rootElement);
		parseComponents(parent, rootElement);
	}

	protected void parseDocument(Element rootElement) {
		Element documentElement = rootElement.getChild("document");
		if (documentElement != null) {
			// size
			String widthValue = documentElement.getAttributeValue("width");
			String heightValue = documentElement.getAttributeValue("height");
			if (widthValue != null && heightValue != null) {
				try {
					int width = Integer.parseInt(widthValue);
					int height = Integer.parseInt(heightValue);
					if (width > 1 && height > 1) {
						documentSize = new Dimension(width, height);
					}
				} catch (NumberFormatException exc) {
				}
			}

			// color
			String colorValue = documentElement.getAttributeValue("color");
			if (colorValue != null) {
				documentColor = JVGParseUtil.getColor(colorValue, null);
			}
		}
	}

	protected void parseComponents(JVGContainer parent, Element e) throws JVGParseException {
		Element componentsElement = e.getChild("components");
		if (componentsElement == null) {
			return;
		}

		Iterator<Element> iter = componentsElement.getChildren("component").iterator();
		while (iter.hasNext()) {
			try {
				Element componentElement = iter.next();
				JVGComponent child = parseComponent(parent, componentElement);
				if (child != null) {
					// container
					if (child instanceof JVGContainer) {
						JVGContainer container = (JVGContainer) child;
						parseComponents(container, componentElement);
					}

					// name
					String nameValue = componentElement.getAttributeValue("name");
					child.setName(nameValue);

					// id
					String idValue = componentElement.getAttributeValue("id");
					Long id = JVGParseUtil.getLong(idValue, null);
					child.setID(id);

					// visible
					String visibleValue = componentElement.getAttributeValue("visible");
					if (visibleValue != null) {
						child.setVisible(JVGParseUtil.getBoolean(visibleValue));
					}

					// clipped
					String clipValue = componentElement.getAttributeValue("clip");
					if (clipValue != null) {
						child.setClipped(JVGParseUtil.getBoolean(clipValue));
					}

					// shape
					if (child instanceof JVGShape) {
						JVGShape shape = (JVGShape) child;

						// isclip
						String isClipValue = componentElement.getAttributeValue("isclip");
						if (isClipValue != null && JVGParseUtil.getBoolean(isClipValue)) {
							shape.setComponentType(JVGComponentType.clip);
						}

						// antialias
						String antialiasValue = componentElement.getAttributeValue("antialias");
						if (antialiasValue != null) {
							shape.setAntialias(JVGParseUtil.getBoolean(antialiasValue));
						}

						// transform
						AffineTransform transform = null;
						Element transformElement = componentElement.getChild("transform");
						if (transformElement != null) {
							transform = getTransform(transformElement);
						} else {
							Element transformsElement = componentElement.getChild("transforms");
							if (transformsElement != null) {
								transform = getTransforms(transformsElement);
							}
						}
						if (transform != null) {
							shape.transform(transform);
						}

						// paiters
						boolean thereIsPainter = false;
						Element paintElement = componentElement.getChild("paint");
						if (paintElement != null) {
							Painter painter = getPainter(paintElement);
							if (painter != null) {
								shape.removeAllPainters();
								shape.addPainter(painter);
								thereIsPainter = true;
							}
						} else {
							Element paintsElement = componentElement.getChild("paints");
							if (paintsElement != null) {
								List<Painter> painters = getPainters(paintsElement);
								if (painters != null && painters.size() > 0) {
									shape.removeAllPainters();
									shape.setPainters(painters);
									thereIsPainter = true;
								}
							}
						}
						// shape.setFill(!thereIsPainter ||
						// shape.isPaint(Painter.FILL));
						shape.setFill(thereIsPainter && shape.isPaint(Painter.FILL));

						// alfa
						String alfaValue = componentElement.getAttributeValue("alfa");
						if (alfaValue != null) {
							shape.setAlfa(JVGParseUtil.getInteger(alfaValue, 255));
						}
					}

					// scripts
					parseScripts(child, componentElement.getChild("scripts"));

					// properties
					setProperties(child, componentElement.getChild("properties"));

					parent.add(child);
				}
			} catch (NumberFormatException exc) {
				throw new JVGParseException(exc.getMessage(), exc);
			} catch (NullPointerException exc) {
				throw new JVGParseException(exc.getMessage(), exc);
			}
		}
	}

	protected JVGComponent parseComponent(JVGContainer parent, Element componentElement) throws JVGParseException {
		String typeValue = componentElement.getAttributeValue("type");
		Element formElement = componentElement.getChild("form");
		if ("container".equals(typeValue)) {
			JVGGroup container = factory.createComponent(JVGGroup.class, (Object[]) null);
			if (formElement != null) {
				String drawOrder = formElement.getAttributeValue("draw-order");
				if ("fill-first".equals(drawOrder)) {
					container.setPaintOrderType(JVGGroup.PAINT_ORDER_FILL_FIRST);
				} else if ("outline-first".equals(drawOrder)) {
					container.setPaintOrderType(JVGGroup.PAINT_ORDER_OUTLINE_FIRST);
				}
			}
			return container;
		} else if ("subpath".equalsIgnoreCase(typeValue)) {
			if (parent instanceof JVGGroupPath) {
				JVGGroupPath parentPath = (JVGGroupPath) parent;
				boolean lead = JVGParseUtil.getBoolean(formElement.getAttributeValue("lead"));

				JVGSubPath path;
				if (!lead) {
					float position = JVGParseUtil.getFloat(formElement.getAttributeValue("position"), null);
					path = parentPath.add(position);
				} else {
					path = parentPath.getLead();
				}

				// path stroke
				Resource<? extends Stroke> pathStroke;
				String pathStrokeValue = formElement.getAttributeValue("path-stroke");
				if (pathStrokeValue != null) {
					pathStroke = getStroke(pathStrokeValue);
				} else {
					pathStroke = parentPath.getPathStroke();
				}
				path.setPathStroke(pathStroke);
				return path;
			}
		} else if ("path".equalsIgnoreCase(typeValue) || "pathgroup".equalsIgnoreCase(typeValue)) {
			Class<? extends JVGPath> clazz = "path".equalsIgnoreCase(typeValue) ? JVGPath.class : JVGGroupPath.class;

			// shape
			MutableGeneralPath gp = new MutableGeneralPath();
			JVGParseUtil.getPath(formElement.getAttributeValue("path"), gp);

			// path stroke
			String pathStrokeValue = formElement.getAttributeValue("path-stroke");
			Resource<Stroke> pathStroke = null;
			if (pathStrokeValue != null) {
				pathStroke = getStroke(pathStrokeValue);
			}

			JVGPath path = factory.createComponent(clazz, new Object[] { gp, false, pathStroke });
			return path;
		} else if ("image".equalsIgnoreCase(typeValue)) {
			JVGImage image = factory.createComponent(JVGImage.class, new Object[] { getImage(formElement) });
			return image;
		} else if ("text".equalsIgnoreCase(typeValue)) {
			Resource<Font> font = getFont(formElement.getAttributeValue("font"));
			JVGStyledText text = factory.createComponent(JVGStyledText.class, (Object[]) null, font);

			String wrapValue = formElement.getAttributeValue("wrap");
			if (wrapValue != null) {
				try {
					text.setWrapSize(Double.parseDouble(wrapValue));
					text.setWrap(true);
				} catch (NumberFormatException exc) {
				}
			} else {
				text.setWrap(false);
			}

			setTextDocument(text.getDocument(), formElement);
			return text;
		} else if ("textfield".equalsIgnoreCase(typeValue)) {
			Resource<Font> font = getFont(formElement.getAttributeValue("font"));
			JVGTextField text = factory.createComponent(JVGTextField.class, formElement.getText(), font != null ? font.getResource() : null);
			return text;
		} else if ("shape".equalsIgnoreCase(typeValue)) {
			String urlValue = formElement != null ? formElement.getAttributeValue("source") : null;
			if (urlValue != null) {
				URL url = getURL(urlValue);
				if (url != null) {
					JVGComplexShape shape = factory.createComponent(JVGComplexShape.class, new Object[] { url });
					if (shape != null) {
						Iterator<Element> argIter = formElement.getChildren("arg").iterator();
						while (argIter.hasNext()) {
							Element argElement = argIter.next();
							String id = argElement.getAttributeValue("id");
							String value = argElement.getAttributeValue("value");
							if (id != null && value != null) {
								try {
									shape.getContext().setArgumentValue(id, Double.parseDouble(value));
								} catch (NumberFormatException exc) {
								}
							}
						}
					}
					return shape;
				}
			}
		} else if ("control".equalsIgnoreCase(typeValue)) {
			JVGCustomActionArea control = factory.createComponent(JVGCustomActionArea.class, (Object[]) null);
			setControl(control, componentElement);
			return control;
		}
		return null;
	}

	private void parseScripts(JVGComponent component, Element scriptsElement) {
		if (scriptsElement != null) {
			Iterator<Element> scriptsIterator = scriptsElement.getChildren("script").iterator();
			while (scriptsIterator.hasNext()) {
				Element scriptElement = scriptsIterator.next();
				Type type = Script.getType(scriptElement.getAttributeValue("type"));
				String ref = scriptElement.getAttributeValue("ref");
				String postScriptName = scriptElement.getAttributeValue("post");

				ScriptResource script;
				if (ref != null && ref.length() > 0) {
					script = resources.getResource(ScriptResource.class, ref);
				} else {
					String data = scriptElement.getText();
					script = new ScriptResource(data);
				}

				if (postScriptName != null && postScriptName.length() > 0) {
					ScriptResource postScript = resources.getResource(ScriptResource.class, postScriptName);
					script.setPostScript(postScript);
				}

				if (type != null && script.getResource().getData().length() > 0) {
					component.setClientProperty(type.getActionName(), script);
				}
			}
		}
	}

	private void parseDocumentScripts(Element rootElement) {
		Element scriptsElement = rootElement.getChild("scripts");
		if (scriptsElement != null) {
			Iterator<Element> scriptsIterator = scriptsElement.getChildren("script").iterator();
			while (scriptsIterator.hasNext()) {
				Element scriptElement = scriptsIterator.next();
				Type type = Script.getType(scriptElement.getAttributeValue("type"));
				String ref = scriptElement.getAttributeValue("ref");

				ScriptResource script;
				if (ref != null && ref.length() > 0) {
					script = resources.getResource(ScriptResource.class, ref);
				} else {
					String data = scriptElement.getText();
					script = new ScriptResource(data);
				}

				if (type != null && script != null && script.getResource().getData().length() > 0) {
					documentScripts.put(type.getActionName(), script);
				}
			}
		}
	}

	private void setProperties(JVGComponent component, Element propertiesElement) {
		if (propertiesElement != null) {
			Map<String, String> hash = (Map<String, String>) component.getClientProperty("component-properties");
			if (hash == null) {
				hash = new HashMap<>();
				component.setClientProperty("component-properties", hash);
			}

			for (Iterator<Element> iterProperty = propertiesElement.getChildren("property").iterator(); iterProperty.hasNext();) {
				Element propertyElement = iterProperty.next();
				String key = propertyElement.getAttributeValue("name");
				String value = propertyElement.getAttributeValue("value");
				if (key != null && value != null) {
					hash.put(key, value);
				}
			}
		}
	}

	private URL getURL(String path) {
		URL url = null;

		try {
			url = JVGParser.class.getResource(path);
		} catch (Exception exc) {
		}

		if (url == null) {
			try {
				url = new URL(path);
			} catch (Exception exc) {
			}
		}

		return url;
	}

	private List<Painter> getPainters(Element paintersElement) throws JVGParseException {
		List<Painter> painters = null;
		Iterator<Element> iter = paintersElement.getChildren("paint").iterator();
		while (iter.hasNext()) {
			Element paintElement = iter.next();
			Painter painter = getPainter(paintElement);
			if (painter != null) {
				if (painters == null) {
					painters = new ArrayList<>();
				}
				painters.add(painter);
			}
		}
		return painters;
	}

	private Painter getPainter(Element paintElement) throws JVGParseException {
		String type = paintElement.getAttributeValue("type");
		Painter painter = null;
		if (type != null) {
			if ("fill".equals(type)) {
				painter = new FillPainter();
			} else if ("outline".equals(type)) {
				Resource<Stroke> stroke = getStroke(paintElement.getAttributeValue("stroke"));
				painter = new OutlinePainter(stroke);
			} else if ("shadow".equals(type)) {
				int shadowType = ShadowPainter.DEFAULT;
				String shadowTypeValue = paintElement.getAttributeValue("shadow-type");
				if (shadows.containsKey(shadowTypeValue)) {
					shadowType = shadows.get(shadowTypeValue);
				}
				painter = new ShadowPainter(shadowType);
			} else if ("endings".equals(type)) {
				EndingsPainter epainter = new EndingsPainter();

				String endingsTypeValue = paintElement.getAttributeValue("endings-type");
				if (endings_types.containsKey(endingsTypeValue)) {
					epainter.getEndings().setEndingType(endings_types.get(endingsTypeValue));
				}

				String endingsFigureValue = paintElement.getAttributeValue("figure");
				if (endings_figures.containsKey(endingsFigureValue)) {
					epainter.getEndings().setFigure(endings_figures.get(endingsFigureValue));
				}

				String directValue = paintElement.getAttributeValue("direct");
				if (directValue != null) {
					epainter.getEndings().setDirect(JVGParseUtil.getBoolean(directValue));
				}

				String fillValue = paintElement.getAttributeValue("fill");
				if (fillValue != null) {
					epainter.getEndings().setFill(JVGParseUtil.getBoolean(fillValue));
				}

				painter = epainter;
			}
		}

		if (painter != null) {
			Element drawElement = paintElement.getChild("draw");
			if (drawElement != null) {
				Draw draw = getDraw(drawElement);
				if (draw == null) {
					return null;
				}
				painter.setPaint(draw);
			}
		}
		return painter;
	}

	private AffineTransform getTransforms(Element transformsElement) {
		AffineTransform transform = null;
		Iterator<Element> iter = transformsElement.getChildren("transform").iterator();
		while (iter.hasNext()) {
			Element transformElement = iter.next();
			AffineTransform t = getTransform(transformElement);
			if (t != null) {
				if (transform == null) {
					transform = t;
				} else {
					transform.preConcatenate(t);
				}
			}
		}
		return transform;
	}

	private AffineTransform getTransform(Element transformElement) {
		String type = transformElement.getAttributeValue("type");
		String value = transformElement.getAttributeValue("value");
		if (type != null && value != null) {
			float[] array = JVGParseUtil.getFloatArray(value);
			if (array != null) {
				if ("matrix".equals(type)) {
					if (array.length == 6) {
						return new AffineTransform(array);
					}
				} else if ("translate".equals(type)) {
					if (array.length == 2) {
						return AffineTransform.getTranslateInstance(array[0], array[1]);
					}
				} else if ("scale".equals(type)) {
					if (array.length == 2) {
						return AffineTransform.getScaleInstance(array[0], array[1]);
					}
				} else if ("shear".equals(type)) {
					if (array.length == 2) {
						return AffineTransform.getShearInstance(array[0], array[1]);
					}
				} else if ("rotate".equals(type)) {
					if (array.length == 1) {
						return AffineTransform.getRotateInstance(array[0]);
					} else if (array.length == 3) {
						return AffineTransform.getRotateInstance(array[0], array[1], array[2]);
					}
				}
			}
		}

		return null;
	}

	// color, stroke, font, image, script, linear-gradient, radial-gradient
	public void parseResources(Element parentElement) {
		Element resourcesElement = parentElement.getChild("resources");
		if (resourcesElement != null) {
			Iterator<Element> iter = resourcesElement.getChildren("resource").iterator();
			while (iter.hasNext()) {
				Element resourceElement = iter.next();
				String name = resourceElement.getAttributeValue("name");
				String type = resourceElement.getAttributeValue("type");
				String value = resourceElement.getAttributeValue("value");
				if (value == null) {
					value = resourceElement.getText();
				}

				if (name != null && type != null && value != null) {
					Resource<?> resource = null;
					try {
						if ("color".equals(type)) {
							Color color = JVGParseUtil.getColor(value, null);
							if (color != null) {
								resource = new ColorResource(color);
							}
						} else if ("stroke".equals(type)) {
							resource = getStroke(value);
						} else if ("font".equals(type)) {
							resource = getFont(value);
						} else if ("texture".equals(type)) {
							resource = getTexture(value);
						} else if ("image".equals(type)) {
							String dataType = resourceElement.getAttributeValue("datatype");
							if ("base64".equals(dataType)) {
								byte[] bytes = Base64.getDecoder().decode(value.getBytes(StandardCharsets.UTF_8));
								resource = new ImageResource(bytes);
							} else {
								resource = new ImageResource(new URI(value).toURL());
							}
						} else if ("script".equals(type)) {
							resource = new ScriptResource(value);
						} else if ("linear-gradient".equals(type)) {
							resource = getLinearGradient(resourceElement, value);
						} else if ("radial-gradient".equals(type)) {
							resource = getRadialGradient(resourceElement, value);
						} else if ("transform".equals(type)) {
							// TODO
							resource = null;
						}
					} catch (Exception exc) {
						exc.printStackTrace();
					}

					if (resource != null) {
						resource.setName(name);
						resources.addResource(resource);
					}
				}
			}
		}
	}

	public Resource<Color> getColor(String value) {
		if (value != null) {
			if (value.startsWith(JVGParseUtil.ID_PREFIX)) {
				String id = value.substring(1, value.length());
				Resource<Color> color = resources.getResource(ColorResource.class, id);
				if (color == null) {
					color = ColorResource.getDefault(id);
				}
				return color;
			} else {
				Color color = JVGParseUtil.getColor(value, null);
				if (color != null) {
					return new ColorResource(color);
				} else {
					Resource<Color> defaultColor = ColorResource.getDefault(value);
					if (defaultColor != null) {
						return defaultColor;
					}
				}
			}
		}
		return null;
	}

	public Resource<Texture> getTexture(String url) throws JVGParseException {
		if (url != null && url.startsWith(JVGParseUtil.ID_PREFIX)) {
			String id = url.substring(1, url.length());
			Resource<Texture> texture = resources.getResource(TextureResource.class, id);
			return texture;
		} else {
			try {
				String[] a = JVGParseUtil.getStringArray(url, ";");
				ImageResource<Icon> image = new ImageResource<>(new URL(a[0]));
				Rectangle2D anchor = null;
				String anchorValue = a.length > 1 ? a[1] : null;
				String[] anchorArray = JVGParseUtil.getStringArray(anchorValue, ",");
				if (anchorArray != null) {
					try {
						double anchorX = Double.parseDouble(anchorArray[0]);
						double anchorY = Double.parseDouble(anchorArray[1]);
						double anchorWidth = Double.parseDouble(anchorArray[2]);
						double anchorHeight = Double.parseDouble(anchorArray[3]);
						anchor = new Rectangle2D.Double(anchorX, anchorY, anchorWidth, anchorHeight);
					} catch (NumberFormatException exc) {
						System.err.println("Invalid anchor value: " + anchorValue);
					}
				}
				return new TextureResource(new Texture(image, anchor));
			} catch (Exception exc) {
				throw new JVGParseException("Can't create texture draw", exc);
			}
		}
	}

	public Resource<LinearGradient> getLinearGradient(Element resourceElement, String value) {
		if (value != null && value.startsWith(JVGParseUtil.ID_PREFIX)) {
			String id = value.substring(1, value.length());
			Resource<LinearGradient> gradient = resources.getResource(LinearGradientResource.class, id);
			return gradient;
		} else {
			String[] a = JVGParseUtil.getStringArray(value);
			String[] limits = JVGParseUtil.getStringArray(a[0], ",");
			String cycleMethodValue = a[1];
			String[] points = JVGParseUtil.getStringArray(a[2], " ");

			float startX = JVGParseUtil.getFloat(limits[0], null);
			float startY = JVGParseUtil.getFloat(limits[1], null);
			float endX = JVGParseUtil.getFloat(limits[2], null);
			float endY = JVGParseUtil.getFloat(limits[3], null);

			MultipleGradientPaint.CycleMethodEnum cycleMethod = MultipleGradientPaint.NO_CYCLE;
			if ("repeat".equalsIgnoreCase(cycleMethodValue)) {
				cycleMethod = MultipleGradientPaint.REPEAT;
			} else if ("reflect".equalsIgnoreCase(cycleMethodValue)) {
				cycleMethod = MultipleGradientPaint.REFLECT;
			}

			int len = points.length / 2;
			float[] fractions = new float[len];
			Resource<Color>[] colors = new Resource[len];
			for (int i = 0; i < len; i++) {
				colors[i] = getColor(points[2 * i]);
				fractions[i] = JVGParseUtil.getFloat(points[2 * i + 1], null);
			}

			LinearGradient gradient = new LinearGradient(fractions, colors, cycleMethod, startX, startY, endX, endY);
			if (resourceElement != null) {
				String unitsType = resourceElement.getAttributeValue("unitstype");
				if ("absolute".equals(unitsType)) {
					gradient.setUnitsType(GradientUnitsType.ABSOLUTE);
				}
			}
			return new LinearGradientResource(gradient);
		}
	}

	public Resource<RadialGradient> getRadialGradient(Element resourceElement, String value) {
		if (value != null && value.startsWith(JVGParseUtil.ID_PREFIX)) {
			String id = value.substring(1, value.length());
			Resource<RadialGradient> gradient = resources.getResource(RadialGradientResource.class, id);
			return gradient;
		} else {
			String[] a = JVGParseUtil.getStringArray(value);
			String[] limits = JVGParseUtil.getStringArray(a[0], ",");
			String cycleMethodValue = a[1];
			String[] points = JVGParseUtil.getStringArray(a[2], " ");

			float centerX = JVGParseUtil.getFloat(limits[0], null);
			float centerY = JVGParseUtil.getFloat(limits[1], null);
			float radius = JVGParseUtil.getFloat(limits[2], null);
			float focusX = JVGParseUtil.getFloat(limits[3], null);
			float focusY = JVGParseUtil.getFloat(limits[4], null);

			MultipleGradientPaint.CycleMethodEnum cycleMethod = MultipleGradientPaint.NO_CYCLE;
			if ("repeat".equalsIgnoreCase(cycleMethodValue)) {
				cycleMethod = MultipleGradientPaint.REPEAT;
			} else if ("reflect".equalsIgnoreCase(cycleMethodValue)) {
				cycleMethod = MultipleGradientPaint.REFLECT;
			}

			int len = points.length / 2;
			float[] fractions = new float[len];
			Resource<Color>[] colors = new Resource[len];
			for (int i = 0; i < len; i++) {
				colors[i] = getColor(points[2 * i]);
				fractions[i] = JVGParseUtil.getFloat(points[2 * i + 1], null);
			}

			RadialGradient gradient = new RadialGradient(fractions, colors, cycleMethod, centerX, centerY, focusX, focusY, radius);
			if (resourceElement != null) {
				String unitsType = resourceElement.getAttributeValue("unitstype");
				if ("absolute".equals(unitsType)) {
					gradient.setUnitsType(GradientUnitsType.ABSOLUTE);
				}
			}
			return new RadialGradientResource(gradient);
		}
	}

	public Resource<Font> getFont(String value) {
		if (value != null) {
			value = value.toLowerCase().trim();
			if (value.startsWith(JVGParseUtil.ID_PREFIX)) {
				String id = value.substring(1, value.length());
				return resources.getResource(FontResource.class, id);
			} else {
				Font font = JVGParseUtil.getFont(value, null);
				if (font != null) {
					return new FontResource(font);
				}
			}
		}
		return null;
	}

	public Resource<Stroke> getStroke(String value) {
		if (value != null) {
			if (value.startsWith(JVGParseUtil.ID_PREFIX)) {
				String id = value.substring(1, value.length());
				return resources.getResource(StrokeResource.class, id);
			} else {
				Stroke stroke = JVGParseUtil.getStroke(value);
				return new StrokeResource<>(stroke);
			}
		}
		return null;
	}

	public Draw getDraw(Element drawElement) throws JVGParseException {
		String type = drawElement.getAttributeValue("type");
		if (type != null) {
			double opacity = JVGParseUtil.getDouble(drawElement.getAttributeValue("opacity"), 1);
			if ("color".equals(type)) {
				String value = drawElement.getAttributeValue("color");
				Resource<Color> color = getColor(value);
				return new ColorDraw(color, opacity);
			} else if ("texture".equals(type)) {
				String source = drawElement.getAttributeValue("source");
				Resource<Texture> texture = getTexture(source);
				return new TextureDraw(texture);
			} else if ("linear-gradient".equals(type)) {
				String value = drawElement.getAttributeValue("value");
				Resource<LinearGradient> gradient = getLinearGradient(drawElement, value);
				return new LinearGradientDraw(gradient, opacity);
			} else if ("radial-gradient".equals(type)) {
				String value = drawElement.getAttributeValue("value");
				Resource<RadialGradient> gradient = getRadialGradient(drawElement, value);
				return new RadialGradientDraw(gradient, opacity);
			}
		}
		return null;
	}

	private Resource<ImageIcon> getImage(Element formElement) throws JVGParseException {
		if (formElement != null) {
			String sourceValue = formElement.getAttributeValue("source");
			if (sourceValue != null) {
				sourceValue = sourceValue.trim();
				if (sourceValue.length() > 0) {
					if (sourceValue.startsWith(JVGParseUtil.ID_PREFIX)) {
						sourceValue = sourceValue.substring(1, sourceValue.length()).trim();
						if (sourceValue.length() > 0) {
							Resource<ImageIcon> image = resources.getResource(ImageResource.class, sourceValue);
							if (image != null) {
								return image;
							}
						}
					} else {
						String dataType = formElement.getAttributeValue("datatype");
						try {
							if ("base64".equals(dataType)) {
								byte[] bytes = Base64.getDecoder().decode(sourceValue.getBytes(StandardCharsets.UTF_8));
								return new ImageResource<>(bytes);
							} else {
								return new ImageResource<>((String) null, sourceValue);
							}
						} catch (Exception exc) {
						}
					}
					throw new JVGParseException("Can't load image: " + sourceValue);
				}
			}
		}

		throw new JVGParseException("Can't load image: source is absent");
	}

	private void setTextDocument(javax.swing.text.StyledDocument doc, Element e) {
		try {
			boolean simple = true;
			Iterator<Element> paragraphIter = e.getChildren("paragraph").iterator();
			while (paragraphIter.hasNext()) {
				Element paragraphElement = paragraphIter.next();
				MutableAttributeSet paragraphAttr = getParagraphAttributeSet(paragraphElement);

				Iterator<Element> iter = paragraphElement.getChildren("elem").iterator();
				while (iter.hasNext()) {
					Element elem = iter.next();
					String text = elem.getText();
					if (text != null && text.length() > 0) {
						MutableAttributeSet characterAttr = getCharacterAttributeSet(elem);
						doc.insertString(doc.getLength(), text, characterAttr);
					}
				}

				javax.swing.text.Element paragraph = doc.getParagraphElement(doc.getLength() - 1);
				doc.setParagraphAttributes(paragraph.getStartOffset(), paragraph.getEndOffset() - paragraph.getStartOffset(), paragraphAttr, false);
				simple = false;
			}

			if (simple) {
				String text = e.getText();
				MutableAttributeSet characterAttr = getCharacterAttributeSet(e);
				doc.insertString(0, text, characterAttr);

				MutableAttributeSet paragraphAttr = getParagraphAttributeSet(e);
				javax.swing.text.Element paragraph = doc.getParagraphElement(0);
				doc.setParagraphAttributes(paragraph.getStartOffset(), paragraph.getEndOffset() - paragraph.getStartOffset(), paragraphAttr, false);
			}
		} catch (BadLocationException exc) {
			exc.printStackTrace();
		}
	}

	private Resource getFillResource(String value) {
		if (value != null) {
			if (value.startsWith(JVGParseUtil.ID_PREFIX)) {
				String id = value.substring(1, value.length());

				Resource resource = resources.getResource(ColorResource.class, id);
				if (resource != null) {
					return resource;
				}

				resource = resources.getResource(TextureResource.class, id);
				if (resource != null) {
					return resource;
				}

				resource = resources.getResource(LinearGradient.class, id);
				if (resource != null) {
					return resource;
				}

				resource = resources.getResource(RadialGradient.class, id);
				if (resource != null) {
					return resource;
				}
			} else {
				if (value.startsWith("#")) {
					return getColor(value);
				}
				if (value.startsWith("img(")) {
					return getColor(value.substring(4, value.length() - 1));
				}
				if (value.startsWith("lg(")) {
					return getLinearGradient(null, value.substring(3, value.length() - 1));
				}
				if (value.startsWith("rg(")) {
					return getRadialGradient(null, value.substring(3, value.length() - 1));
				}
			}
		}
		return null;
	}

	private MutableAttributeSet getCharacterAttributeSet(Element element) {
		MutableAttributeSet attr = new SimpleAttributeSet();

		Resource<Font> font = getFont(element.getAttributeValue("font"));
		if (font != null) {
			StyleConstants.setFontFamily(attr, font.getResource().getFamily());
			StyleConstants.setFontSize(attr, font.getResource().getSize());
			StyleConstants.setBold(attr, font.getResource().isBold());
			StyleConstants.setItalic(attr, font.getResource().isItalic());
		}

		Resource foreground = getFillResource(element.getAttributeValue("foreground"));
		if (foreground != null) {
			JVGStyleConstants.setForeground(attr, foreground);
		}

		Resource background = getFillResource(element.getAttributeValue("background"));
		if (background != null) {
			JVGStyleConstants.setBackground(attr, background);
		}

		String styleAttr = element.getAttributeValue("style");
		if (styleAttr != null) {
			Set<String> styles = JVGUtil.toSet(JVGParseUtil.getStringArray(styleAttr.toLowerCase(), ","));
			if (styles.contains("underline")) {
				JVGStyleConstants.setUnderline(attr, JVGStyleConstants.UNDERLINE_LINE);
			} else if (styles.contains("underline-double")) {
				JVGStyleConstants.setUnderline(attr, JVGStyleConstants.UNDERLINE_DOUBLE_LINE);
			} else if (styles.contains("underline-triple")) {
				JVGStyleConstants.setUnderline(attr, JVGStyleConstants.UNDERLINE_TRIPLE_LINE);
			} else if (styles.contains("underline-zigzag")) {
				JVGStyleConstants.setUnderline(attr, JVGStyleConstants.UNDERLINE_ZIGZAG);
			}

			StyleConstants.setStrikeThrough(attr, styles.contains("strike"));
			StyleConstants.setSubscript(attr, styles.contains("sub"));
			StyleConstants.setSuperscript(attr, styles.contains("sup"));
		} else {
			StyleConstants.setUnderline(attr, false);
			StyleConstants.setStrikeThrough(attr, false);
			StyleConstants.setSubscript(attr, false);
			StyleConstants.setSuperscript(attr, false);
		}
		return attr;
	}

	private MutableAttributeSet getParagraphAttributeSet(Element element) {
		MutableAttributeSet attr = new SimpleAttributeSet();

		int alignment = StyleConstants.ALIGN_LEFT;
		String alignAttr = element.getAttributeValue("alignment");
		if (alignAttr != null) {
			alignAttr = alignAttr.toLowerCase().trim();
			if (alignAttr.equals("center")) {
				alignment = StyleConstants.ALIGN_CENTER;
			} else if (alignAttr.equals("right")) {
				alignment = StyleConstants.ALIGN_RIGHT;
			} else if (alignAttr.equals("just")) {
				alignment = StyleConstants.ALIGN_JUSTIFIED;
			}
		}
		StyleConstants.setAlignment(attr, alignment);

		// bullets
		String bulletsType = element.getAttributeValue("bullets");
		if (bulletsType != null) {
			if (bulletsType.equals("circle")) {
				JVGStyleConstants.setParagraphBullets(attr, JVGStyleConstants.BULLETS_CIRCLE);
			} else if (bulletsType.equals("minus")) {
				JVGStyleConstants.setParagraphBullets(attr, JVGStyleConstants.BULLETS_MINUS);
			} else if (bulletsType.equals("square")) {
				JVGStyleConstants.setParagraphBullets(attr, JVGStyleConstants.BULLETS_SQURE);
			} else if (bulletsType.equals("numeric")) {
				JVGStyleConstants.setParagraphBullets(attr, JVGStyleConstants.BULLETS_NUMBER);
			} else if (bulletsType.equals("letter")) {
				JVGStyleConstants.setParagraphBullets(attr, JVGStyleConstants.BULLETS_LETTER);
			}
		}

		String bulletsIndentSize = element.getAttributeValue("bullets-indent-size");
		if (bulletsIndentSize != null) {
			JVGStyleConstants.setParagraphBulletsIndentSize(attr, JVGParseUtil.getInteger(bulletsIndentSize, null));
		}

		String bulletsIndentCount = element.getAttributeValue("bullets-indent-count");
		if (bulletsIndentCount != null) {
			JVGStyleConstants.setParagraphBulletsIndentCount(attr, JVGParseUtil.getInteger(bulletsIndentCount, null));
		}

		String bulletsColor = element.getAttributeValue("bullets-color");
		if (bulletsColor != null) {
			JVGStyleConstants.setParagraphBulletsColor(attr, JVGParseUtil.getColor(bulletsColor, null));
		}

		String bulletsFont = element.getAttributeValue("bullets-font");
		if (bulletsFont != null) {
			JVGStyleConstants.setParagraphBulletsFont(attr, JVGParseUtil.getFont(bulletsFont, null));
		}

		// border
		Border border = JVGParseUtil.getBorder(element);
		JVGStyleConstants.setParagraphBorder(attr, border);

		// insets
		String insets = element.getAttributeValue("insets");
		if (insets != null) {
			String[] i = insets.split(",");
			if (i.length == 4) {
				float i1 = JVGParseUtil.getFloat(i[0], 0f);
				float i2 = JVGParseUtil.getFloat(i[1], 0f);
				float i3 = JVGParseUtil.getFloat(i[2], 0f);
				float i4 = JVGParseUtil.getFloat(i[3], 0f);
				StyleConstants.setSpaceAbove(attr, i1);
				StyleConstants.setLeftIndent(attr, i2);
				StyleConstants.setSpaceBelow(attr, i3);
				StyleConstants.setRightIndent(attr, i4);
			}
		}

		// lines
		String lineSpacing = element.getAttributeValue("line-spacing");
		if (lineSpacing != null) {
			StyleConstants.setLineSpacing(attr, JVGParseUtil.getFloat(lineSpacing, null));
		}
		return attr;
	}

	private void setControl(JVGCustomActionArea control, Element componentElement) {
		if (control != null) {
		}
	}

	public static void main(String[] args) {
		try {
			JVGResources resources = new JVGResources();
			resources.addResource(new ImageResource<ImageIcon>("image", "c:/Fon.tif"));

			JVGParser parser = new JVGParser(JVGFactory.createDefault(), resources);

			JVGPane p = new JVGPane();
			JVGRoot root = parser.parse(JVGParser.class.getResourceAsStream("test.xml"));
			p.setRoot(root);

			JFrame f = new JFrame();
			f.setContentPane(new JScrollPane(p));
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			f.setLocation(400, 400);
			f.pack();
			f.setVisible(true);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	@Override
	public void init(JVGPane pane) {
	}
}
