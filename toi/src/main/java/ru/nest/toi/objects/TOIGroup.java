package ru.nest.toi.objects;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.List;

import ru.nest.toi.TOIObject;
import ru.nest.toi.TOIPaintContext;

public class TOIGroup extends TOIObject {
	private List<TOIObject> objects = new ArrayList<>();

	private boolean combinePathes = true;

	@Override
	public void paint(Graphics2D g, Graphics2D gt, TOIPaintContext ctx) {
		if (!combinePathes) {
			for (TOIObject o : objects) {
				o.paint(g, gt, ctx);
			}
		} else {
			for (TOIObject o : objects) {
				if (o instanceof TOIArrowPathElement) {
					TOIArrowPathElement a = (TOIArrowPathElement) o;
					a.paintOutline(g, gt, ctx);
				} else if (o instanceof TOIMultiArrowPath) {
					TOIMultiArrowPath a = (TOIMultiArrowPath) o;
					a.paintOutline(g, gt, ctx);
				}
			}

			for (TOIObject o : objects) {
				if (o instanceof TOIArrowPathElement) {
					TOIArrowPathElement a = (TOIArrowPathElement) o;
					a.paintArrow(g, gt, ctx);
				} else if (o instanceof TOIMultiArrowPath) {
					TOIMultiArrowPath a = (TOIMultiArrowPath) o;
					a.paintArrow(g, gt, ctx);
				} else {
					o.paint(g, gt, ctx);
				}
			}
		}
	}

	public List<TOIObject> getObjects() {
		return objects;
	}

	@Override
	public boolean contains(double x, double y) {
		return getObject(x, y) != null;
	}

	@Override
	public void transform(AffineTransform transform) {
		super.transform(transform);
		for (TOIObject o : objects) {
			o.transform(transform);
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		for (TOIObject o : objects) {
			o.invalidate();
		}
	}

	@Override
	public void validate() {
		if (!isValid()) {
			Area originalShape = null;
			for (TOIObject o : objects) {
				o.validate();
				if (o.getShape() != null) {
					try {
						Shape os = getTransform().createInverse().createTransformedShape(o.getShape());
						if (originalShape == null) {
							originalShape = new Area(os);
						} else {
							originalShape.add(new Area(os));
						}
					} catch (NoninvertibleTransformException e) {
						e.printStackTrace();
					}
				}
			}

			originalBounds = originalShape.getBounds2D();
			shape = getTransform().createTransformedShape(originalShape);
			bounds = shape.getBounds2D();

			super.validate();
		}
	}

	public void setObjects(List<TOIObject> objects) {
		this.objects = objects;
		invalidate();
	}

	public TOIObject getObject(double x, double y) {
		synchronized (objects) {
			for (int i = objects.size() - 1; i >= 0; i--) {
				TOIObject o = objects.get(i);
				if (o.contains(x, y)) {
					return o;
				}
			}
		}
		return null;
	}

	@Override
	public void setColorDeep(Color color) {
		setColor(color);
		for (TOIObject o : objects) {
			o.setColorDeep(color);
		}
	}

	@Override
	public void setFontDeep(Font font) {
		setFont(font);
		for (TOIObject o : objects) {
			o.setFontDeep(font);
		}
	}

	public boolean isCombinePathes() {
		return combinePathes;
	}

	public void setCombinePathes(boolean combinePathes) {
		this.combinePathes = combinePathes;
		invalidate();
	}
}
