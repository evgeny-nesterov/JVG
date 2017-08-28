package ru.nest.jvg;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;

import javax.swing.AlfaPaint;

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
