package ru.nest.jvg.shape.paint;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

import ru.nest.jvg.resource.ColorResource;
import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.shape.JVGShape;

public class ColorDraw extends AbstractDraw<Color> {
	public final static Resource<Color> DEFAULT_COLOR = new ColorResource(new Color(225, 225, 235));

	private Resource<Color> resource = null;

	public ColorDraw() {
		this(DEFAULT_COLOR);
	}

	public ColorDraw(Color color) {
		this(new ColorResource(color));
	}

	public ColorDraw(Color color, double opacity) {
		this(new ColorResource(color), opacity);
	}

	public ColorDraw(Resource<Color> color) {
		this(color, 1);
	}

	public ColorDraw(Resource<Color> color, double opacity) {
		setResource(color);
		setOpacity(opacity);
	}

	@Override
	public void setResource(Resource<Color> resource) {
		this.resource = resource;
	}

	@Override
	public Resource<Color> getResource() {
		return resource;
	}

	@Override
	public Paint getPaint(JVGShape component, Shape shape, AffineTransform transform) {
		Color color = resource != null ? resource.getResource() : null;
		if (color == null) {
			color = DEFAULT_COLOR.getResource();
		}
		if (getOpacity() != 1) {
			color = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (getOpacity() * color.getAlpha()));
		}
		return color;
	}
}
