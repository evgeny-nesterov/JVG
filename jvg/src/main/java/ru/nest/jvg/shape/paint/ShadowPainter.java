package ru.nest.jvg.shape.paint;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import ru.nest.jvg.resource.ColorResource;
import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.shape.JVGShape;

public class ShadowPainter extends Painter {
	public final static Resource<Color> DEFAULT_COLOR = ColorResource.gray;

	public final static int STRAIGHT__RIGHT_TOP = 0; // ON DEFAULT

	public final static int STRAIGHT__RIGHT_BOTTOM = 1;

	public final static int STRAIGHT__LEFT_TOP = 2;

	public final static int STRAIGHT__LEFT_BOTTOM = 3;

	public final static int SLOPE_LEFT_BACK = 4;

	public final static int SLOPE_RIGHT_BACK = 5;

	public final static int SLOPE_LEFT_FORWARD = 6;

	public final static int SLOPE_RIGHT_FORWARD = 7;

	public final static int DEFAULT = STRAIGHT__RIGHT_TOP;

	private float shear = 1.5f;

	public ShadowPainter() {
		this(DEFAULT);
	}

	public ShadowPainter(int shadowType) {
		this(shadowType, null);
	}

	public ShadowPainter(int shadowType, Draw draw) {
		setShadowType(shadowType);
		setPaint(draw);
	}

	private int shadowType;

	public int getShadowType() {
		return shadowType;
	}

	public void setShadowType(int shadowType) {
		this.shadowType = shadowType;
	}

	@Override
	public Shape getShape(JVGShape component) {
		return component.getTransformedShape();
	}

	@Override
	public void paint(Graphics2D g, JVGShape component, Shape shape) {
		float koefX = 0.0f, koefY = 0.0f;
		switch (shadowType) {
			case SLOPE_LEFT_BACK:
			case SLOPE_RIGHT_BACK:
				koefX = 0.5f;
				koefY = 1.0f;
				break;

			case SLOPE_LEFT_FORWARD:
			case SLOPE_RIGHT_FORWARD:
				koefX = 0.5f;
				koefY = 1.0f;
				break;
		}

		Shape boundsShape = component.getBounds();
		Rectangle2D bounds = boundsShape instanceof Rectangle2D ? (Rectangle2D) boundsShape : boundsShape.getBounds2D();

		int translateX = (int) (bounds.getX() + koefX * bounds.getWidth());
		int translateY = (int) (bounds.getY() + koefY * bounds.getHeight());

		g.transform(component.getInverseTransform());
		g.translate(translateX, translateY);
		switch (shadowType) {
			case STRAIGHT__RIGHT_TOP:
				g.translate(5, -5);
				break;

			case STRAIGHT__RIGHT_BOTTOM:
				g.translate(5, 5);
				break;

			case STRAIGHT__LEFT_TOP:
				g.translate(-5, -5);
				break;

			case STRAIGHT__LEFT_BOTTOM:
				g.translate(-5, 5);
				break;

			case SLOPE_LEFT_BACK:
				g.shear(shear, 0);
				g.scale(1, 0.5);
				break;

			case SLOPE_RIGHT_BACK:
				g.shear(-shear, 0);
				g.scale(1, 0.5);
				break;

			case SLOPE_LEFT_FORWARD:
				g.shear(-shear, 0);
				g.scale(1, -0.5);
				break;

			case SLOPE_RIGHT_FORWARD:
				g.shear(shear, 0);
				g.scale(1, -0.5);
				break;
		}
		g.translate(-translateX, -translateY);

		if (component.isFill()) {
			g.fill(shape);
		} else {
			g.draw(shape);
		}

		// transform back
		g.translate(translateX, translateY);
		switch (shadowType) {
			case STRAIGHT__RIGHT_TOP:
				g.translate(-5, 5);
				break;

			case STRAIGHT__RIGHT_BOTTOM:
				g.translate(-5, -5);
				break;

			case STRAIGHT__LEFT_TOP:
				g.translate(5, 5);
				break;

			case STRAIGHT__LEFT_BOTTOM:
				g.translate(5, -5);
				break;

			case SLOPE_LEFT_BACK:
				g.scale(1, 2);
				g.shear(-shear, 0);
				break;

			case SLOPE_RIGHT_BACK:
				g.scale(1, 2);
				g.shear(shear, 0);
				break;

			case SLOPE_LEFT_FORWARD:
				g.scale(1, -2);
				g.shear(shear, 0);
				break;

			case SLOPE_RIGHT_FORWARD:
				g.scale(1, -2);
				g.shear(-shear, 0);
				break;
		}
		g.translate(-translateX, -translateY);

		g.transform(component.getTransform());
	}

	@Override
	public int getType() {
		return SHADOW;
	}
}
