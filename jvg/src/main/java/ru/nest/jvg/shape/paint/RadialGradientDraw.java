package ru.nest.jvg.shape.paint;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.swing.gradient.Gradient.GradientUnitsType;
import javax.swing.gradient.MultipleGradientPaint;
import javax.swing.gradient.RadialGradient;
import javax.swing.gradient.RadialGradientPaint;

import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.shape.JVGShape;

public class RadialGradientDraw extends AbstractDraw<RadialGradient> {
	private Resource<RadialGradient> resource;

	protected transient MultipleGradientPaint paint;

	public RadialGradientDraw(Resource<RadialGradient> resource) {
		this(resource, 1);
	}

	public RadialGradientDraw(Resource<RadialGradient> resource, double opacity) {
		this.resource = resource;
		setOpacity(opacity);
	}

	@Override
	public Resource<RadialGradient> getResource() {
		return resource;
	}

	@Override
	public void setResource(Resource<RadialGradient> resource) {
		this.resource = resource;
		paint = null;
	}

	@Override
	public Paint getPaint(JVGShape component, Shape shape, AffineTransform transform) {
		if (paint == null) {
			RadialGradient gradient = resource.getResource();
			Resource<Color>[] colorResources = gradient.getColors();
			if (colorResources != null) {
				// copy colors
				double opacity = getOpacity();
				Color[] colors = new Color[colorResources.length];
				for (int i = 0; i < colorResources.length; i++) {
					Color color = colorResources[i].getResource();
					if (color == null) {
						color = Color.white;
						System.err.println("Error in RadialGradientDraw: colorResources[" + i + "] is null!");
					}
					if (opacity != 1) {
						color = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (opacity * color.getAlpha()));
					}
					colors[i] = color;
				}

				Shape ib = component.getInitialBounds();
				Rectangle2D r = ib instanceof Rectangle2D ? (Rectangle2D) ib : ib.getBounds2D();

				float rad = gradient.getR();
				if (rad > 0) {
					// update paint on change
					if (gradient.getUnitsType() == GradientUnitsType.ABSOLUTE) {
						float cx = (float) ((gradient.getCX() - r.getX()) / r.getWidth());
						float cy = (float) ((gradient.getCY() - r.getY()) / r.getHeight());
						float fx = (float) ((gradient.getFX() - r.getX()) / r.getWidth());
						float fy = (float) ((gradient.getFY() - r.getY()) / r.getHeight());
						rad = (float) (rad / r.getWidth());
						gradient = new RadialGradient(gradient.getFractions(), gradient.getColors(), gradient.getCycleMethod(), cx, cy, fx, fy, rad);
						resource.setResource(gradient);
					}

					float cx = (float) r.getX() + (float) r.getWidth() * gradient.getCX();
					float cy = (float) r.getY() + (float) r.getHeight() * gradient.getCY();
					float fx = (float) r.getX() + (float) r.getWidth() * gradient.getFX();
					float fy = (float) r.getY() + (float) r.getHeight() * gradient.getFY();
					rad = (float) (r.getWidth() * gradient.getR());

					if (transform != null) {
						double scale = (r.getHeight() != 0 ? r.getHeight() : 1) / (r.getWidth() != 0 ? r.getWidth() : 1);
						AffineTransform scaleTransform = AffineTransform.getTranslateInstance(cx, cy);
						scaleTransform.scale(1, scale);
						scaleTransform.translate(-cx, -cy);

						transform = (AffineTransform) transform.clone();
						transform.concatenate(scaleTransform);
					}

					if (gradient.getTransform() != null && transform != null) {
						transform = (AffineTransform) transform.clone();
						transform.concatenate(gradient.getTransform());
					}
					paint = new RadialGradientPaint(cx, cy, rad, fx, fy, gradient.getFractions(), colors, transform, gradient.getCycleMethod());
				}
			}
		}
		return paint;
	}
}
