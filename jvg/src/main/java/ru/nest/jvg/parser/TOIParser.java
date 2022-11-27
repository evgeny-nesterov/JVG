package ru.nest.jvg.parser;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Stroke;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import org.jdom2.Element;

import ru.nest.jvg.JVGContainer;
import ru.nest.jvg.JVGFactory;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.resource.ImageResource;
import ru.nest.jvg.resource.JVGResources;
import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.resource.ScriptResource;
import ru.nest.jvg.resource.StrokeResource;
import ru.nest.jvg.shape.JVGGroup;
import ru.nest.jvg.shape.JVGGroupPath;
import ru.nest.jvg.shape.JVGImage;
import ru.nest.jvg.shape.JVGPath;
import ru.nest.jvg.shape.JVGShape;
import ru.nest.jvg.shape.JVGSubPath;
import ru.nest.jvg.shape.JVGTextField;
import ru.nest.jvg.shape.paint.ColorDraw;
import ru.nest.jvg.shape.paint.FillPainter;
import ru.nest.jvg.shape.paint.OutlinePainter;
import ru.nest.strokes.ArrowStroke;
import ru.nest.toi.TOIBuilder;
import ru.nest.toi.TOIObject;
import ru.nest.toi.factory.TOIDefaultFactory;
import ru.nest.toi.objects.TOIArrowPathElement;
import ru.nest.toi.objects.TOIGroup;
import ru.nest.toi.objects.TOIImage;
import ru.nest.toi.objects.TOIMultiArrowPath;
import ru.nest.toi.objects.TOIText;
import ru.nest.toi.objects.TOITextPath;

public class TOIParser implements JVGParserInterface {
	private JVGFactory factory;

	public TOIParser(JVGFactory factory, JVGResources resources) {
		this.factory = factory;
	}

	@Override
	public JVGResources getResources() {
		return null;
	}

	@Override
	public Dimension getDocumentSize() {
		return null;
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
		TOIBuilder toiparser = new TOIBuilder(new TOIDefaultFactory());
		try {
			List<TOIObject> toiobjects = toiparser.load(rootElement);
			parseObjects(toiobjects, parent);
		} catch (Exception e) {
			e.printStackTrace();
			throw new JVGParseException("Can't parse toi format", e);
		}
	}

	private void parseObjects(List<? extends TOIObject> objects, JVGContainer parent) {
		for (TOIObject o : objects) {
			JVGShape c = null;
			Double outlineWidth = null;

			if (o instanceof TOIMultiArrowPath) {
				TOIMultiArrowPath toipath = (TOIMultiArrowPath) o;
				Resource<ArrowStroke> pathStroke = new StrokeResource<>(new ArrowStroke(toipath.getWidth(), toipath.getArrowWidth(), toipath.getArrowLength(), ArrowStroke.DIRECTION_DIRECT));

				JVGGroupPath p = factory.createComponent(JVGGroupPath.class, toipath.getPath(), false);
				p.setPathStroke(pathStroke);
				outlineWidth = toipath.getOutlineWidth();
				c = p;
			} else if (o instanceof TOIArrowPathElement) {
				TOIArrowPathElement toiarrow = (TOIArrowPathElement) o;
				if (parent instanceof JVGGroupPath) {
					JVGGroupPath group = (JVGGroupPath) parent;
					JVGSubPath arrow = null;
					if (toiarrow.getPercentPos() != 1) {
						arrow = group.add(toiarrow.getPercentPos());
					} else {
						arrow = group.getLead();
					}
					c = arrow;
				} else {
					Resource<ArrowStroke> pathStroke = new StrokeResource<>(new ArrowStroke(toiarrow.getWidth(), toiarrow.getArrowWidth(), toiarrow.getArrowLength(), ArrowStroke.DIRECTION_DIRECT));
					JVGPath p = factory.createComponent(JVGPath.class, toiarrow.getPath(), false);
					p.setPathStroke(pathStroke);
					c = p;
				}
				outlineWidth = toiarrow.getOutlineWidth();
			} else if (o instanceof TOIGroup) {
				TOIGroup toigroup = (TOIGroup) o;
				JVGGroup g = factory.createComponent(JVGGroup.class);
				if (toigroup.isCombinePathes()) {
					g.setPaintOrderType(JVGGroup.PAINT_ORDER_OUTLINE_FIRST);
				}
				c = g;
			} else if (o instanceof TOITextPath) {
				TOITextPath toitextpath = (TOITextPath) o;
				Resource<Stroke> pathStroke = new StrokeResource<>(toitextpath.getText(), toitextpath.getFont(), true, false);
				JVGPath path = factory.createComponent(JVGPath.class, toitextpath.getPath(), false, pathStroke);
				c = path;
			} else if (o instanceof TOIText) {
				TOIText toitext = (TOIText) o;
				JVGTextField text = factory.createComponent(JVGTextField.class, toitext.getText(), toitext.getFont());
				c = text;
			} else if (o instanceof TOIImage) {
				TOIImage toiimage = (TOIImage) o;
				JVGImage image = factory.createComponent(JVGImage.class, new ImageResource<>(toiimage.getData(), toiimage.getDescr()));
				c = image;
			}

			if (c != null) {
				c.removeAllPainters();
				if (outlineWidth != null) {
					c.addPainter(new OutlinePainter(new StrokeResource<>((float) (2 * outlineWidth)), new ColorDraw(Color.black)));
				}
				if (!(o instanceof TOIImage)) {
					c.addPainter(new FillPainter(new ColorDraw(o.getColor() != null ? o.getColor() : Color.lightGray)));
				}
				c.transform(o.getTransform());
				c.setAntialias(true);
				c.setClientProperty("objectid", o.getId());

				if (!(parent instanceof JVGGroupPath)) {
					parent.add(c);
				}

				if (o instanceof TOIMultiArrowPath) {
					TOIMultiArrowPath toipath = (TOIMultiArrowPath) o;
					parseObjects(toipath.getElements(), c);
				} else if (o instanceof TOIGroup) {
					TOIGroup toigroup = (TOIGroup) o;
					parseObjects(toigroup.getObjects(), c);
				}
			}
		}
	}

	@Override
	public void init(JVGPane pane) {
	}
}
