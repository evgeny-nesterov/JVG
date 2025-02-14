package ru.nest.jvg.shape.paint;

import ru.nest.jvg.shape.JVGShape;
import ru.nest.swing.AlfaPaint;

import java.awt.*;
import java.awt.geom.AffineTransform;

public abstract class Painter implements Cloneable {
	public final static int UNDEFINED = 0;

	public final static int FILL = 1;

	public final static int OUTLINE = 2;

	public final static int ENDINGS = 3;

	public final static int SHADOW = 4;

	private Draw<?> paint;

	public void setPaint(Draw<?> paint) {
		this.paint = paint;
	}

	public Draw<?> getPaint() {
		return paint;
	}

	private boolean draw = true;

	public void setDraw(boolean draw) {
		this.draw = draw;
	}

	public boolean isDraw() {
		return draw;
	}

	public void paint(Graphics2D g, JVGShape component) {
		Shape shape = getShape(component);
		if (shape != null && isDraw() && this.paint != null) {
			AffineTransform transform = getTransform(component, shape);
			Paint paint = this.paint.getPaint(component, shape, transform);
			if (paint != null) {
				Paint composePaint = component.getDeepComposePaint();
				if (composePaint != null) {
					paint = new AlfaPaint(paint, composePaint);
				}

				Paint oldPaint = g.getPaint();
				g.setPaint(paint);
				paint(g, component, shape);
				g.setPaint(oldPaint);
			}
		}
	}

	public Shape getShape(JVGShape component) {
		return component.getShape();
	}

	protected abstract void paint(Graphics2D g, JVGShape component, Shape shape);

	private AffineTransform t = new AffineTransform();

	public AffineTransform getTransform(JVGShape component, Shape shape) {
		return t;
	}

	public int getType() {
		return UNDEFINED;
	}

	@Override
	public Object clone() {
		try {
			Painter painter = (Painter) super.clone();
			if (paint != null) {
				painter.paint = (Draw<?>) paint.clone();
			}
			return painter;
		} catch (CloneNotSupportedException exc) {
			exc.printStackTrace();
			return null;
		}
	}
}
