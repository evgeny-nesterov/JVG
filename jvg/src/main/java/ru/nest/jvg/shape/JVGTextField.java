package ru.nest.jvg.shape;

import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;

import ru.nest.jvg.event.JVGPropertyChangeEvent;
import ru.nest.jvg.geom.MutableGeneralPath;
import ru.nest.jvg.resource.ColorResource;
import ru.nest.jvg.resource.FontResource;
import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.shape.paint.ColorDraw;
import ru.nest.jvg.shape.paint.FillPainter;

public class JVGTextField extends JVGShape {
	public final static String PROPERTY_ANCHOR = "anchor";

	private String text;

	private Resource<Font> font;

	private FontRenderContext frc;

	public enum TextAnchor {
		start, middle, end
	}

	private TextAnchor anchor;

	public JVGTextField(String text) {
		this(text, (Font) null);
	}

	public JVGTextField(String text, Font font) {
		this(text, font != null ? new FontResource(font) : null);
	}

	public JVGTextField(String text, Resource<Font> font) {
		setText(text);
		setFont(font);
		setAntialias(true);
		setOriginalBounds(true);
		setPainter(new FillPainter(new ColorDraw(ColorResource.black)));
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
		invalidate();
	}

	public Resource<Font> getFont() {
		return font;
	}

	public void setFont(Resource<Font> font) {
		if (font == null) {
			font = new FontResource(FontResource.DEFAULT_FONT);
		}
		this.font = font;
		invalidate();
	}

	@Override
	public void validate() {
		if (!isValid()) {

			//		Line2D line = new Line2D.Double(0, 0, width, 0);
			//		Resource<Stroke> pathStroke = new StrokeResource<Stroke>(text, font, false, false);
			//		JVGPath path = factory.createComponent(JVGPath.class, line, false, pathStroke);
			//		path.transform(transform);

			frc = new FontRenderContext(null, isAntialias(), true);
			GlyphVector glyphVector = font.getResource().createGlyphVector(frc, text);
			Shape shape = glyphVector.getOutline();
			if (anchor != null && anchor != TextAnchor.start) {
				double width = font.getResource().getStringBounds(text, frc).getWidth();
				AffineTransform transform;
				if (anchor == TextAnchor.end) {
					transform = AffineTransform.getTranslateInstance(-width, 0);
				} else {
					transform = AffineTransform.getTranslateInstance(-width / 2.0, 0);
				}
				MutableGeneralPath path = new MutableGeneralPath(shape);
				path.transform(transform);
				shape = path;
			}
			setShape(shape);
			super.validate();
		}
	}

	public TextAnchor getAnchor() {
		return anchor;
	}

	public void setAnchor(TextAnchor anchor) {
		TextAnchor oldValue = this.anchor;
		this.anchor = anchor;
		dispatchEvent(new JVGPropertyChangeEvent(this, PROPERTY_ANCHOR, oldValue, anchor));
	}
}
