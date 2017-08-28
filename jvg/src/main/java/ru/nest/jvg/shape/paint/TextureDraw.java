package ru.nest.jvg.shape.paint;

import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.swing.Icon;

import ru.nest.jvg.resource.ColorResource;
import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.resource.Texture;
import ru.nest.jvg.resource.TextureResource;
import ru.nest.jvg.shape.JVGShape;

public class TextureDraw extends AbstractDraw<Texture> {
	public TextureDraw(Resource<Icon> image, Rectangle2D anchor) {
		this(new TextureResource(new Texture(image, anchor)));
	}

	public TextureDraw(Resource<Texture> texture) {
		setResource(texture);
	}

	private Resource<Texture> texture;

	@Override
	public Resource<Texture> getResource() {
		return texture;
	}

	@Override
	public void setResource(Resource<Texture> texture) {
		this.texture = texture;
	}

	@Override
	public Paint getPaint(JVGShape component, Shape shape, AffineTransform transform) {
		return texture != null ? texture.getResource().getPaint() : ColorResource.white.getResource();
	}
}
