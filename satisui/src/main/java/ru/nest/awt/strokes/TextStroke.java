package ru.nest.awt.strokes;

import java.awt.Font;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

public class TextStroke implements Stroke {
	private String text;

	private Font font;

	private boolean stretchToFit = true;

	private boolean repeat = false;

	private AffineTransform t = new AffineTransform();

	private static final float FLATNESS = 1;

	public TextStroke(String text, Font font) {
		this(text, font, true, false);
	}

	public TextStroke(String text, Font font, boolean stretchToFit, boolean repeat) {
		this.text = text;
		this.font = font;
		this.stretchToFit = stretchToFit;
		this.repeat = repeat;
	}

	public String getText() {
		return text;
	}

	public Font getFont() {
		return font;
	}

	public boolean isStretchToFit() {
		return stretchToFit;
	}

	public boolean isRepeat() {
		return repeat;
	}

	public Shape createStrokedShape(Shape shape) {
		FontRenderContext frc = new FontRenderContext(null, true, true);
		GlyphVector glyphVector = font.createGlyphVector(frc, text);

		GeneralPath result = new GeneralPath();
		PathIterator it = new FlatteningPathIterator(shape.getPathIterator(null), FLATNESS);
		double points[] = new double[6];
		double moveX = 0, moveY = 0;
		double lastX = 0, lastY = 0;
		double thisX = 0, thisY = 0;
		int type = 0;
		boolean first = false;
		double next = 0;
		int currentChar = 0;
		int length = glyphVector.getNumGlyphs();

		if (length == 0) {
			return result;
		}

		double curveLength = measurePathLength(shape);
		double gw = glyphVector.getLogicalBounds().getWidth();
		double factor = stretchToFit ? curveLength / gw : 1.0f;
		double nextAdvance = 0;

		while (currentChar < length && !it.isDone()) {
			type = it.currentSegment(points);
			switch (type) {
				case PathIterator.SEG_MOVETO:
					moveX = lastX = points[0];
					moveY = lastY = points[1];
					result.moveTo(moveX, moveY);
					first = true;
					nextAdvance = glyphVector.getGlyphMetrics(currentChar).getAdvance() * 0.5f;
					next = nextAdvance;
					break;

				case PathIterator.SEG_CLOSE:
					points[0] = moveX;
					points[1] = moveY;
					// Fall into....

				case PathIterator.SEG_LINETO:
					thisX = points[0];
					thisY = points[1];
					double dx = thisX - lastX;
					double dy = thisY - lastY;
					double distance = Math.sqrt(dx * dx + dy * dy);
					if (distance >= next) {
						double r = 1.0f / distance;
						double angle = Math.atan2(dy, dx);
						while (currentChar < length && distance >= next) {
							Shape glyph = glyphVector.getGlyphOutline(currentChar);
							Point2D p = glyphVector.getGlyphPosition(currentChar);
							double px = p.getX();
							double py = p.getY();
							double x = lastX + next * dx * r;
							double y = lastY + next * dy * r;
							double advance = nextAdvance;
							nextAdvance = currentChar < length - 1 ? glyphVector.getGlyphMetrics(currentChar + 1).getAdvance() * 0.5f : 0;
							t.setToTranslation(x, y);
							t.rotate(angle);
							t.translate(-px - advance, -py);
							result.append(t.createTransformedShape(glyph), false);
							next += (advance + nextAdvance) * factor;
							currentChar++;
							if (repeat) {
								currentChar %= length;
							}
						}
					}
					next -= distance;
					first = false;
					lastX = thisX;
					lastY = thisY;
					break;
			}
			it.next();
		}
		return result;
	}

	public double measurePathLength(Shape shape) {
		PathIterator it = new FlatteningPathIterator(shape.getPathIterator(null), FLATNESS);
		double points[] = new double[6];
		double moveX = 0, moveY = 0;
		double lastX = 0, lastY = 0;
		double thisX = 0, thisY = 0;
		int type = 0;
		double total = 0;

		while (!it.isDone()) {
			type = it.currentSegment(points);
			switch (type) {
				case PathIterator.SEG_MOVETO:
					moveX = lastX = points[0];
					moveY = lastY = points[1];
					break;

				case PathIterator.SEG_CLOSE:
					points[0] = moveX;
					points[1] = moveY;
					// Fall into....

				case PathIterator.SEG_LINETO:
					thisX = points[0];
					thisY = points[1];
					double dx = thisX - lastX;
					double dy = thisY - lastY;
					total += Math.sqrt(dx * dx + dy * dy);
					lastX = thisX;
					lastY = thisY;
					break;
			}
			it.next();
		}
		return total;
	}
}
