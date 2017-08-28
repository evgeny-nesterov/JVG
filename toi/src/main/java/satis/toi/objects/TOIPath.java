package satis.toi.objects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import satis.toi.MutablePath;
import satis.toi.TOIObject;
import satis.toi.TOIPaintContext;
import satis.toi.TOIPane;
import satis.toi.TOIUtil;

public abstract class TOIPath extends TOIObject {
	public final static int[] curvesizes = { 2, 2, 4, 6, 0 };

	protected MutablePath.Double path = new MutablePath.Double();

	protected double length;

	public TOIPath() {
	}

	public abstract void paintPath(Graphics2D g, Graphics2D gt, TOIPaintContext ctx);

	@Override
	public void paint(Graphics2D g, Graphics2D gt, TOIPaintContext ctx) {
		paintPath(g, gt, ctx);
	}

	public void paintSelection(Graphics2D g, Graphics2D gt, TOIPaintContext ctx) {
		Shape transformedPath = ctx.getTransform().createTransformedShape(path);
		Stroke oldStroke = g.getStroke();
		g.setStroke(TOIPane.selectedStroke1);
		g.setColor(new Color(220, 220, 220));
		g.draw(transformedPath);
		g.setStroke(TOIPane.selectedStroke2);
		g.setColor(Color.black);
		g.draw(transformedPath);
		g.setStroke(oldStroke);

		if (isFocused()) {
			Shape transformedBounds = getTransform().createTransformedShape(getOriginalBounds());
			transformedBounds = ctx.getTransform().createTransformedShape(transformedBounds);
			g.setStroke(TOIPane.selectedStroke1);
			g.setColor(new Color(220, 220, 220));
			g.draw(transformedBounds);
			g.setStroke(TOIPane.selectedStroke2);
			g.setColor(Color.black);
			g.draw(transformedBounds);
			g.setStroke(oldStroke);
		}
	}

	public int getCoordIndex(double x, double y) {
		for (int i = 0; i < path.numCoords; i += 2) {
			if (x >= path.doubleCoords[i] - 3 && x <= path.doubleCoords[i] + 3 && y >= path.doubleCoords[i + 1] - 3 && y <= path.doubleCoords[i + 1] + 3) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public void validate() {
		if (!isValid()) {
			length = TOIUtil.measurePathLength(path);
		}
		super.validate();
	}

	@Override
	public void transform(AffineTransform transform) {
		if (transform != null) {
			path.transform(transform);
			invalidate();
		}
		super.transform(transform);
	}

	@Override
	public boolean contains(double x, double y) {
		return path.intersects(x - 3, y - 3, 6, 6);
	}

	public MutablePath.Double getPath() {
		return path;
	}

	public void setPath(MutablePath.Double path) {
		this.path = path;
		invalidate();
	}

	public double getLength() {
		return length;
	}

	public void deleteLastSegment() {
		if (getPath().numTypes > 2) {
			int type = getPath().pointTypes[getPath().numTypes - 1];
			int curvesize = curvesizes[type];
			getPath().numTypes--;
			getPath().numCoords -= curvesize;
			invalidate();
		}
	}
}
