package ru.nest.jvg.shape;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.net.URL;

import ru.nest.jvg.complexshape.ComplexShapeParser;
import ru.nest.jvg.geom.CoordinablePath;
import ru.nest.jvg.geom.MutableGeneralPath;
import ru.nest.jvg.geom.coord.Coordinable;
import ru.nest.jvg.shape.paint.FillPainter;
import ru.nest.jvg.shape.paint.OutlinePainter;

public class JVGComplexShape extends JVGShape {
	public JVGComplexShape(URL url) throws Exception {
		this(url, true);
	}

	public JVGComplexShape(URL url, boolean editable) throws Exception {
		ComplexShapeParser ctx = new ComplexShapeParser();
		ctx.parse(url);
		init(ctx, editable);
	}

	public JVGComplexShape(ComplexShapeParser ctx) throws Exception {
		this(ctx.getURL());
	}

	public JVGComplexShape(ComplexShapeParser ctx, boolean editable) throws Exception {
		init(ctx, editable);
	}

	private CoordinablePath path;

	private void init(ComplexShapeParser ctx, boolean editable) {
		this.url = ctx.getURL();
		this.ctx = ctx;
		setOriginalBounds(true);

		path = ctx.getPath();
		boundsCoordinates = ctx.getBoundsPoints();

		if (editable) {
			coordinates = ctx.getCoordinates();
			setShape(path);
		} else {
			coordinates = null;
			setShape(new MutableGeneralPath(path));
		}

		setFill(ctx.isFill());
		if (isFill()) {
			addPainter(new FillPainter(FillPainter.DEFAULT_COLOR));
		}
		addPainter(new OutlinePainter(null, OutlinePainter.DEFAULT_COLOR));
		// addPainter(new EndingsPainter(EndingsPainter.TYPE_BOTH_ENDING, false,
		// EndingsPainter.FIGURE_ARROW, true, ColorResource.black));
	}

	private URL url;

	public URL getURL() {
		return url;
	}

	private ComplexShapeParser ctx;

	public ComplexShapeParser getContext() {
		return ctx;
	}

	// public void transformShape(AffineTransform transform, HashSet<JVGShape>
	// locked)
	// {
	// if (coordinates != null)
	// {
	// path.transform(transform, coordinates, 0, coordinates.length / 2);
	// }
	// else
	// {
	// super.transformShape(transform, locked);
	// }
	// }

	@Override
	public void unlock() {
		if (coordinates != null) {
			path.unlock(coordinates, 0, coordinates.length / 2);
		} else {
			super.unlock();
		}
	}

	@Override
	public Shape computeInitialBounds() {
		if (boundsCoordinates != null && boundsCoordinates.length > 2) {
			return CoordinablePath.getBounds2D(boundsCoordinates, 0, boundsCoordinates.length);
		} else {
			return super.computeBounds();
		}
	}

	@Override
	protected Shape computeBounds() {
		if (isOriginalBounds()) {
			if (originalBounds == null) {
				originalBounds = computeOriginalBounds();
			}
			return originalBounds;
		} else {
			if (boundsCoordinates != null && boundsCoordinates.length > 2) {
				AffineTransform transform = getTransform();
				if (transform.isIdentity()) {
					if (boundsCoordinates != null && boundsCoordinates.length > 2) {
						return CoordinablePath.getBounds2D(boundsCoordinates, 0, boundsCoordinates.length);
					} else {
						return super.computeBounds();
					}
				} else {
					double[] p = new double[2];
					p[0] = boundsCoordinates[0].getCoord();
					p[1] = boundsCoordinates[1].getCoord();
					transform.transform(p, 0, p, 0, 1);

					double minX, minY, maxX, maxY;
					minX = maxX = p[0];
					minY = maxY = p[1];

					for (int i = 2; i < boundsCoordinates.length; i += 2) {
						p[0] = boundsCoordinates[i].getCoord();
						p[1] = boundsCoordinates[i + 1].getCoord();
						transform.transform(p, 0, p, 0, 1);
						if (minX > p[0]) {
							minX = p[0];
						} else if (maxX < p[0]) {
							maxX = p[0];
						}

						if (minY > p[1]) {
							minY = p[1];
						} else if (maxY < p[1]) {
							maxY = p[1];
						}
					}

					return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
				}
			} else {
				return super.computeInitialBounds();
			}
		}
	}

	@Override
	protected Shape computeOriginalBounds() {
		AffineTransform transform = getTransform();
		if (transform.isIdentity()) {
			if (boundsCoordinates != null && boundsCoordinates.length > 2) {
				return CoordinablePath.getBounds2D(boundsCoordinates, 0, boundsCoordinates.length);
			} else {
				return super.computeBounds();
			}
		} else {
			if (initialBounds == null) {
				initialBounds = computeInitialBounds();
			}

			Rectangle2D bounds;
			if (initialBounds instanceof Rectangle2D) {
				bounds = (Rectangle2D) initialBounds;
			} else {
				bounds = initialBounds.getBounds2D();
			}

			MutableGeneralPath path = new MutableGeneralPath(bounds);
			path.transform(transform);
			return path;
		}
	}

	private Coordinable[] coordinates;

	public Coordinable[] getCoordinates() {
		return coordinates;
	}

	private Coordinable[] boundsCoordinates;

	public Coordinable[] getBoundsCoordinates() {
		return boundsCoordinates;
	}
}
