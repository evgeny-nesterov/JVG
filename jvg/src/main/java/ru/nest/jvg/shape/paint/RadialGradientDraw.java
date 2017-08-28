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
			Resource<Color>[] colorResources = resource.getResource().getColors();
			if (colorResources != null) {
				// copy colors
				double opacity = getOpacity();
				Color[] colors = new Color[colorResources.length];
				for (int i = 0; i < colorResources.length; i++) {
					Color color = colorResources[i].getResource();;
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

				float rad;
				if (resource.getResource().getUnits() == GradientUnitsType.BOUNDS) {
					rad = (float) (r.getWidth() * resource.getResource().getR());
				} else {
					rad = resource.getResource().getR();
				}

				if (rad > 0) {
					// update paint on change
					float cx, cy, fx, fy;
					if (resource.getResource().getUnits() == GradientUnitsType.BOUNDS) {
						cx = (float) r.getX() + (float) r.getWidth() * resource.getResource().getCX();
						cy = (float) r.getY() + (float) r.getHeight() * resource.getResource().getCY();
						fx = (float) r.getX() + (float) r.getWidth() * resource.getResource().getFX();
						fy = (float) r.getY() + (float) r.getHeight() * resource.getResource().getFY();

						double scale = (r.getHeight() != 0 ? r.getHeight() : 1) / (r.getWidth() != 0 ? r.getWidth() : 1);
						AffineTransform scaleTransform = AffineTransform.getTranslateInstance(cx, cy);
						scaleTransform.scale(1, scale);
						scaleTransform.translate(-cx, -cy);

						transform = (AffineTransform) transform.clone();
						transform.concatenate(scaleTransform);
					} else {
						cx = resource.getResource().getCX();
						cy = resource.getResource().getCY();
						fx = resource.getResource().getFX();
						fy = resource.getResource().getFY();
					}

					if (resource.getResource().getTransform() != null) {
						transform = (AffineTransform) transform.clone();
						transform.concatenate(resource.getResource().getTransform());
					}
					paint = new RadialGradientPaint(cx, cy, rad, fx, fy, resource.getResource().getFractions(), colors, transform, resource.getResource().getCycleMethod());
				}
			}
		}
		return paint;
	}
}
