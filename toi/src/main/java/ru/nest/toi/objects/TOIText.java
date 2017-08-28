package ru.nest.toi.objects;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;

import ru.nest.toi.TOIPaintContext;
import ru.nest.toi.Text;
import ru.nest.toi.fonts.Fonts;

public class TOIText extends TOIShape implements Text {
	private final static FontRenderContext frc = new FontRenderContext(null, true, true);

	public static Font defaultFont = Fonts.getPixelFont().deriveFont(16f);

	public TOIText() {
		setColor(Color.lightGray);
		setFont(defaultFont);
	}

	@Override
	public void validate() {
		if (!isValid()) {
			GlyphVector v = getFont().createGlyphVector(frc, getText());
			Shape originalShape = v.getOutline();
			originalBounds = v.getVisualBounds();
			shape = getTransform().createTransformedShape(originalShape);
			bounds = getTransform().createTransformedShape(originalBounds);
		}
		super.validate();
	}

	@Override
	public void paintShape(Graphics2D g, Graphics2D gt, TOIPaintContext ctx) {
		gt.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		gt.setFont(getFont());
		gt.setColor(ctx.getColorRenderer().getColor(this, Color.lightGray));
		gt.drawString(getText(), 0, 0);
		gt.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
	}
}
