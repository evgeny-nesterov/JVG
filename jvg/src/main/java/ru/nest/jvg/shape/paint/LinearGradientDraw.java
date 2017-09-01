package ru.nest.jvg.shape.paint;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.swing.gradient.Gradient.GradientUnitsType;
import javax.swing.gradient.LinearGradient;
import javax.swing.gradient.LinearGradientPaint;
import javax.swing.gradient.MultipleGradientPaint;

import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.shape.JVGShape;

public class LinearGradientDraw extends AbstractDraw<LinearGradient> {
	private Resource<LinearGradient> resource;

	protected transient MultipleGradientPaint paint;

	public LinearGradientDraw(Resource<LinearGradient> resource) {
		this(resource, 1);
	}

	public LinearGradientDraw(Resource<LinearGradient> resource, double opacity) {
		this.resource = resource;
		setOpacity(opacity);
	}

	@Override
	public Resource<LinearGradient> getResource() {
		return resource;
	}

	@Override
	public void setResource(Resource<LinearGradient> resource) {
		this.resource = resource;
		paint = null;
	}

	@Override
	public Paint getPaint(JVGShape component, Shape shape, AffineTransform transform) {
		if (paint == null) {
			Shape ib = component.getInitialBounds();
			Rectangle2D r = ib instanceof Rectangle2D ? (Rectangle2D) ib : ib.getBounds2D();

			// copy colors
			LinearGradient gradient = resource.getResource();
			Resource<Color>[] colorResources = gradient.getColors();
			if (colorResources != null) {
				double opacity = getOpacity();
				Color[] colors = new Color[colorResources.length];
				for (int i = 0; i < colors.length; i++) {
					if (opacity == 1) {
						colors[i] = colorResources[i].getResource();
					} else {
						Color color = colorResources[i].getResource();
						colors[i] = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (opacity * color.getAlpha()));
					}
				}

				float startX = gradient.getX1();
				float startY = gradient.getY1();
				float endX = gradient.getX2();
				float endY = gradient.getY2();

				// update paint on change
				float x1, y1, x2, y2;
				if (gradient.getUnitsType() == GradientUnitsType.BOUNDS) {
					x1 = (float) r.getX() + (float) r.getWidth() * startX;
					y1 = (float) r.getY() + (float) r.getHeight() * startY;
					x2 = (float) r.getX() + (float) r.getWidth() * endX;
					y2 = (float) r.getY() + (float) r.getHeight() * endY;
				} else {
					x1 = startX;
					y1 = startY;
					x2 = endX;
					y2 = endY;
				}

				if (x1 != x2 || y1 != y2) {
					if (gradient.getTransform() != null && transform != null) {
						transform = (AffineTransform) transform.clone();
						transform.concatenate(gradient.getTransform());
					}
					paint = new LinearGradientPaint(x1, y1, x2, y2, gradient.getFractions(), colors, gradient.getCycleMethod(), transform);
				}
			}
		}
		return paint;
	}
}
