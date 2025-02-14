package ru.nest.swing.gradient;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;

public final class RadialGradientPaint extends MultipleGradientPaint {
	private Point2D focus;

	private Point2D center;

	private float radius;

	public RadialGradientPaint(float cx, float cy, float radius, float[] fractions, Color[] colors) {
		this(cx, cy, radius, cx, cy, fractions, colors);
	}

	public RadialGradientPaint(Point2D center, float radius, float[] fractions, Color[] colors) {
		this(center, radius, center, fractions, colors);
	}

	public RadialGradientPaint(float cx, float cy, float radius, float fx, float fy, float[] fractions, Color[] colors) {
		this(new Point2D.Float(cx, cy), radius, new Point2D.Float(fx, fy), fractions, colors, NO_CYCLE, SRGB);
	}

	public RadialGradientPaint(float cx, float cy, float radius, float fx, float fy, float[] fractions, Color[] colors, AffineTransform gradientTransform) {
		this(new Point2D.Float(cx, cy), radius, new Point2D.Float(fx, fy), fractions, colors, NO_CYCLE, SRGB, gradientTransform);
	}

	public RadialGradientPaint(float cx, float cy, float radius, float fx, float fy, float[] fractions, Color[] colors, AffineTransform gradientTransform, CycleMethodEnum cycleMethod) {
		this(new Point2D.Float(cx, cy), radius, new Point2D.Float(fx, fy), fractions, colors, cycleMethod, SRGB, gradientTransform);
	}

	public RadialGradientPaint(Point2D center, float radius, Point2D focus, float[] fractions, Color[] colors) {
		this(center, radius, focus, fractions, colors, NO_CYCLE, SRGB);
	}

	public RadialGradientPaint(Point2D center, float radius, Point2D focus, float[] fractions, Color[] colors, CycleMethodEnum cycleMethod, ColorSpaceEnum colorSpace) {
		this(center, radius, focus, fractions, colors, cycleMethod, colorSpace, new AffineTransform());
	}

	public RadialGradientPaint(RadialGradient gradient, int x, int y, int w, int h) {
		this(x + w * gradient.getCX(), y + h * gradient.getCY(), h * gradient.getR(), x + w * gradient.getFX(), y + h * gradient.getFY(), gradient.getFractions(), gradient.convertColors(), new AffineTransform(), gradient.getCycleMethod());
	}

	public RadialGradientPaint(Point2D center, float radius, Point2D focus, float[] fractions, Color[] colors, CycleMethodEnum cycleMethod, ColorSpaceEnum colorSpace, AffineTransform gradientTransform) {
		super(fractions, colors, cycleMethod, colorSpace, gradientTransform);

		if (center == null) {
			throw new NullPointerException("Center point should not be null.");
		}

		if (focus == null) {
			throw new NullPointerException("Focus point should not be null.");
		}

		if (radius <= 0) {
			throw new IllegalArgumentException("radius should be greater than zero");
		}

		this.center = (Point2D) center.clone();
		this.focus = (Point2D) focus.clone();
		this.radius = radius;
	}

	public RadialGradientPaint(Rectangle2D gradientBounds, float[] fractions, Color[] colors) {
		this((float) gradientBounds.getX() + ((float) gradientBounds.getWidth() / 2), (float) gradientBounds.getY() + ((float) gradientBounds.getWidth() / 2), (float) gradientBounds.getWidth() / 2, fractions, colors);
	}

	@Override
	public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds, AffineTransform transform, RenderingHints hints) {
		transform = new AffineTransform(transform);
		transform.concatenate(gradientTransform);

		try {
			return new RadialGradientPaintContext(cm, deviceBounds, userBounds, transform, hints, (float) center.getX(), (float) center.getY(), radius, (float) focus.getX(), (float) focus.getY(), fractions, colors, cycleMethod, colorSpace);
		} catch (NoninvertibleTransformException e) {
			throw new IllegalArgumentException("transform should be invertible");
		}
	}

	public Point2D getCenterPoint() {
		return new Point2D.Double(center.getX(), center.getY());
	}

	public Point2D getFocusPoint() {
		return new Point2D.Double(focus.getX(), focus.getY());
	}

	public float getRadius() {
		return radius;
	}

	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.setContentPane(new JLabel() {
			@Override
			public void paint(Graphics g) {
				super.paint(g);

				float[] dist = { 0.0f, 0.2f, 1.0f };
				Color[] colors = { Color.red, Color.white, Color.blue };
				RadialGradientPaint p = new RadialGradientPaint(300, 200, 150, 320, 220, dist, colors, AffineTransform.getScaleInstance(1.3f, 1));

				Graphics2D g2d = (Graphics2D) g;
				g2d.setPaint(p);
				g2d.fillRect(0, 0, getWidth(), getHeight());
			}
		});
		f.setBounds(200, 200, 600, 400);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}
}
