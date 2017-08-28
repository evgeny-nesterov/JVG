package ru.nest.jvg.shape.paint;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;

import ru.nest.jvg.resource.ColorResource;
import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.resource.StrokeResource;
import ru.nest.jvg.shape.JVGShape;

public class OutlinePainter extends Painter {
	public final static Resource<Color> DEFAULT_COLOR = ColorResource.black;

	public OutlinePainter() {
		this(new StrokeResource<Stroke>(1f), DEFAULT_COLOR);
	}

	public OutlinePainter(Resource<Stroke> stroke) {
		this(stroke, DEFAULT_COLOR);
	}

	public OutlinePainter(Resource<? extends Stroke> stroke, Resource<Color> color) {
		this(stroke, new ColorDraw(color));
	}

	public OutlinePainter(Resource<? extends Stroke> stroke, Draw<?> paint) {
		setStroke(stroke);
		setPaint(paint);
	}

	private Resource<? extends Stroke> stroke = null;

	public <S extends Stroke> void setStroke(Resource<S> stroke) {
		this.stroke = stroke;
	}

	public void setStroke(float width, int cap, int join, float miterlimit, float dash[], float dash_phase) {
		setStroke(new StrokeResource<BasicStroke>(width, cap, join, miterlimit, dash, dash_phase));
	}

	public Resource<? extends Stroke> getStroke() {
		return stroke;
	}

	@Override
	public void paint(Graphics2D g, JVGShape component, Shape shape) {
		Stroke oldStroke = null;
		Resource<? extends Stroke> stroke = getStroke();
		if (stroke != null) {
			oldStroke = g.getStroke();
			Stroke s = stroke.getResource();
			if (s != null) {
				g.setStroke(s);
			}
		}

		try {
			g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
			g.draw(shape);
		} catch (Throwable thr) {
			System.err.println("Can't draw outline: " + thr.toString());
		}

		if (stroke != null) {
			g.setStroke(oldStroke);
		}
	}

	@Override
	public int getType() {
		return OUTLINE;
	}
}
