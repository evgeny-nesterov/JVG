package ru.nest.jvg.resource;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Stroke;

import ru.nest.strokes.TextStroke;

public class StrokeResource<V extends Stroke> extends Resource<V> {
	public final static StrokeResource DEFAULT = new StrokeResource(1);

	public StrokeResource(V stroke) {
		setResource(stroke);
	}

	public StrokeResource(float width, float[] dash) {
		this(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, dash, 0.0f);
	}

	public StrokeResource(float width, int cap, int join, float miterlimit) {
		setResource((V) new BasicStroke(width, cap, join, miterlimit));
	}

	public StrokeResource(float width, int cap, int join, float miterlimit, float dash[], float dash_phase) {
		setResource((V) new BasicStroke(width, cap, join, miterlimit, dash, dash_phase));
	}

	public StrokeResource(float width) {
		setResource((V) new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
	}

	public StrokeResource(String text, Font font, boolean stretchToFit, boolean repeat) {
		if (font == null) {
			font = FontResource.DEFAULT_FONT;
		}
		setResource((V) new TextStroke(text, font, stretchToFit, repeat));
	}

	private V stroke;

	@Override
	public V getResource() {
		return stroke;
	}

	@Override
	public void setResource(V stroke) {
		this.stroke = stroke;
	}
}
