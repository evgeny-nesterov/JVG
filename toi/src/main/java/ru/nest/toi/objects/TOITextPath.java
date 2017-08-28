package ru.nest.toi.objects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.NoninvertibleTransformException;

import ru.nest.strokes.TextStroke;
import ru.nest.toi.TOIPaintContext;
import ru.nest.toi.Text;

public class TOITextPath extends TOIPath implements Text {
	private TextStroke stroke;

	public TOITextPath() {
		setColor(Color.lightGray);
		setFont(TOIText.defaultFont);
	}

	public TextStroke getStroke() {
		validate();
		return stroke;
	}

	@Override
	public void paintPath(Graphics2D g, Graphics2D gt, TOIPaintContext ctx) {
		gt.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		Stroke oldStroke = gt.getStroke();
		gt.setColor(ctx.getColorRenderer().getColor(this, Color.lightGray));
		gt.setStroke(getStroke());
		gt.draw(path);
		gt.setStroke(oldStroke);
		gt.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
	}

	@Override
	public void validate() {
		if (!isValid()) {
			stroke = new TextStroke(getText(), getFont());
			shape = stroke.createStrokedShape(path);
			bounds = shape.getBounds2D();
			try {
				originalBounds = getTransform().createInverse().createTransformedShape(shape).getBounds2D();
			} catch (NoninvertibleTransformException e) {
				e.printStackTrace();
			}
		}
		super.validate();
	}

	@Override
	public boolean contains(double x, double y) {
		if (getText().length() == 0) {
			return path.intersects(x - 3, y - 3, 6, 6);
		} else {
			return getShape().contains(x, y) || getShape().intersects(x - 3, y - 3, 6, 6);
		}
	}
}
