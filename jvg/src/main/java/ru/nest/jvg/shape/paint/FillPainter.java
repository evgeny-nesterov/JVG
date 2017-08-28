package ru.nest.jvg.shape.paint;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;

import ru.nest.jvg.resource.ColorResource;
import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.shape.JVGShape;

public class FillPainter extends Painter {
	public final static Resource<Color> DEFAULT_COLOR = ColorResource.white;

	public FillPainter() {
		this((Resource<Color>) null);
	}

	public FillPainter(Draw<?> paint) {
		setPaint(paint);
	}

	public FillPainter(Resource<Color> color) {
		if (color == null) {
			color = DEFAULT_COLOR;
		}
		setPaint(new ColorDraw(color));
	}

	@Override
	public void paint(Graphics2D g, JVGShape component, Shape shape) {
		g.fill(shape);
	}

	@Override
	public int getType() {
		return FILL;
	}
}
