package ru.nest.jvg;

import ru.nest.swing.AlfaPaint;

import java.awt.*;

public class JVGComposeGraphics extends JVGGraphics {
	private Paint composePaint;

	public JVGComposeGraphics(Graphics2D g, Paint composePaint) {
		super(g);
		this.composePaint = composePaint;
	}

	@Override
	public Graphics create() {
		return new JVGComposeGraphics(getGraphics2D(), composePaint);
	}

	private Paint paint;

	@Override
	public Paint getPaint() {
		return paint;
	}

	@Override
	public void setPaint(Paint paint) {
		this.paint = paint;
		super.setPaint(new AlfaPaint(paint, composePaint));
	}

	private Color color;

	public Color getCOlor() {
		return color;
	}

	@Override
	public void setColor(Color color) {
		this.color = color;
		super.setColor(color);
		setPaint(color);
	}
}
