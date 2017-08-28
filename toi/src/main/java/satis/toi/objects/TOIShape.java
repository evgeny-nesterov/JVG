package satis.toi.objects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.NoninvertibleTransformException;

import satis.toi.TOIObject;
import satis.toi.TOIPaintContext;
import satis.toi.TOIPane;

public abstract class TOIShape extends TOIObject {
	public TOIShape() {
	}

	@Override
	public boolean contains(double x, double y) {
		return getBounds().contains(x, y);
	}

	@Override
	public void paint(Graphics2D g, Graphics2D gt, TOIPaintContext ctx) {
		if (ctx.getLayer() == TOIPaintContext.MAIN_LAYER) {
			gt.transform(getTransform());

			paintShape(g, gt, ctx);

			try {
				gt.transform(getTransform().createInverse());
			} catch (NoninvertibleTransformException e) {
				e.printStackTrace();
			}

			if (isSelected()) {
				Shape transformedPath = ctx.getTransform().createTransformedShape(getBounds());
				Stroke oldStroke = g.getStroke();
				g.setStroke(TOIPane.selectedStroke1);
				g.setColor(new Color(220, 220, 220));
				g.draw(transformedPath);
				g.setStroke(TOIPane.selectedStroke2);
				g.setColor(Color.black);
				g.draw(transformedPath);
				g.setStroke(oldStroke);
			}
		}
	}

	public abstract void paintShape(Graphics2D g, Graphics2D gt, TOIPaintContext ctx);
}
