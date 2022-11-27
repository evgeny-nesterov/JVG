package ru.nest.toi.objects;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import ru.nest.toi.Arrow;
import ru.nest.toi.MutablePath;
import ru.nest.toi.PathElementPoint;
import ru.nest.toi.TOIPaintContext;
import ru.nest.toi.TOIUtil;

public class TOIMultiArrowPath extends TOIPath implements Arrow {
	private double width = 14;

	private double arrowWidth = 12;

	private double arrowLength = 6;

	private List<TOIArrowPathElement> elements = new ArrayList<>();

	private Stroke stroke;

	private double outlineWidth = 2;

	public TOIMultiArrowPath() {
	}

	@Override
	public boolean contains(double x, double y) {
		for (int i = elements.size() - 1; i >= 0; i--) {
			if (elements.get(i).contains(x, y)) {
				return true;
			}
		}
		return false;
	}

	public TOIArrowPathElement addElement(double percentPos) {
		TOIArrowPathElement e = new TOIArrowPathElement(this, percentPos);
		e.setColor(getColor());

		elements.add(e);
		Collections.sort(elements);
		invalidate();
		return e;
	}

	public void removeElement(TOIArrowPathElement e) {
		elements.remove(e);
		invalidate();
	}

	@Override
	public void paintPath(Graphics2D g, Graphics2D gt, TOIPaintContext ctx) {
		for (int i = elements.size() - 1; i >= 0; i--) {
			TOIArrowPathElement e = elements.get(i);
			e.paint(g, gt, ctx);
			e.paintControls(g, gt, ctx);
		}
	}

	public void paintArrow(Graphics2D g, Graphics2D gt, TOIPaintContext ctx) {
		for (int i = elements.size() - 1; i >= 0; i--) {
			TOIArrowPathElement e = elements.get(i);
			e.paintArrow(g, gt, ctx);
			e.paintControls(g, gt, ctx);
		}
	}

	public void paintOutline(Graphics2D g, Graphics2D gt, TOIPaintContext ctx) {
		for (int i = elements.size() - 1; i >= 0; i--) {
			TOIArrowPathElement e = elements.get(i);
			e.paintOutline(g, gt, ctx);
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		for (TOIArrowPathElement e : elements) {
			e.invalidate();
		}
	}

	public Stroke getStroke() {
		if (stroke == null) {
			stroke = new BasicStroke((float) width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
		}
		return stroke;
	}

	@Override
	public void validate() {
		if (!isValid()) {
			Collections.sort(elements);

			// calculate path length
			length = TOIUtil.measurePathLength(path);

			// compute arrows shapes
			PathIterator it = new FlatteningPathIterator(path.getPathIterator(null), 1);
			double points[] = new double[6];
			double moveX = 0, moveY = 0;
			double lastX = 0, lastY = 0;
			double thisX = 0, thisY = 0;
			int type = 0;
			double totalLength = 0;
			MutablePath.Double elementPath = new MutablePath.Double();
			while (!it.isDone()) {
				type = it.currentSegment(points);
				switch (type) {
					case PathIterator.SEG_MOVETO:
						moveX = lastX = points[0];
						moveY = lastY = points[1];
						elementPath.moveTo(moveX, moveY);
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
						double r = (float) Math.sqrt(dx * dx + dy * dy);
						double nextLength = totalLength + r;

						double r1 = totalLength / length;
						double r2 = nextLength / length;
						for (TOIArrowPathElement o : elements) {
							if (o.getPercentPos() >= r1 && o.getPercentPos() <= r2) {
								double deltaRatio = (o.getPercentPos() - r1) / (r2 - r1);
								double x = lastX + deltaRatio * (thisX - lastX);
								double y = lastY + deltaRatio * (thisY - lastY);

								elementPath.lineTo(x, y);
								o.setPath(elementPath);

								// start collecting path for next element
								elementPath = new MutablePath.Double();
								elementPath.moveTo(x, y);
							}
						}

						elementPath.lineTo(thisX, thisY);

						totalLength = nextLength;
						lastX = thisX;
						lastY = thisY;
						break;
				}
				it.next();
			}

			shape = getStroke().createStrokedShape(path);
			bounds = shape.getBounds2D();
			try {
				originalBounds = getTransform().createInverse().createTransformedShape(shape).getBounds2D();
			} catch (NoninvertibleTransformException e) {
				e.printStackTrace();
			}
		}
		super.validate();
	}

	public TOIArrowPathElement getElementAt(double x, double y) {
		for (TOIArrowPathElement e : elements) {
			if (e.contains(x, y)) {
				return e;
			}
		}
		return null;
	}

	public PathElementPoint getClosestPoint(double x, double y) {
		PathElementPoint closestPoint = null;
		double minDistance = Double.MAX_VALUE;
		for (TOIArrowPathElement e : elements) {
			PathElementPoint point = e.getClosestPoint(x, y);
			if (point != null && (closestPoint == null || point.dist < minDistance)) {
				closestPoint = point;
				minDistance = point.dist;
			}
		}
		return closestPoint;
	}

	public TOIArrowPathElement getPathElementEndingAt(double x, double y) {
		for (TOIArrowPathElement e : elements) {
			if (e.endPoint != null) {
				double dx = x - e.endPoint.getX();
				double dy = y - e.endPoint.getY();
				double r = Math.sqrt(dx * dx + dy * dy);
				if (r < 5) {
					return e;
				}
			}
		}
		return null;
	}

	public List<TOIArrowPathElement> getElements() {
		return elements;
	}

	@Override
	public void setColorDeep(Color color) {
		setColor(color);
		for (TOIArrowPathElement e : elements) {
			e.setColor(color);
		}
	}

	@Override
	public double getWidth() {
		return width;
	}

	@Override
	public void setWidth(double width) {
		this.width = width;
		stroke = null;
		invalidate();
	}

	@Override
	public double getArrowWidth() {
		return arrowWidth;
	}

	@Override
	public void setArrowWidth(double arrowWidth) {
		this.arrowWidth = arrowWidth;
		invalidate();
	}

	@Override
	public double getArrowLength() {
		return arrowLength;
	}

	@Override
	public void setArrowLength(double arrowLength) {
		this.arrowLength = arrowLength;
		invalidate();
	}

	@Override
	public void setScale(double scaleWidth, double scaleArrowWidth, double scaleArrowLength) {
		width = 14 * scaleWidth;
		arrowWidth = 12 * scaleArrowWidth;
		arrowLength = 6 * scaleArrowLength;
		stroke = null;
		invalidate();
	}

	public double getOutlineWidth() {
		return outlineWidth;
	}

	public void setOutlineWidth(double outlineWidth) {
		this.outlineWidth = outlineWidth;
		invalidate();
	}

	public static void main(String[] args) {
		try {
			Set<Character> set = new TreeSet<>();
			for (File f : new File("C:/Users/john/Dropbox/Satis Soft/Рабочие материалы/Common/ЦОДД/Контроллеры/Пофазки_общ/contoller-log").listFiles()) {
				BufferedReader r = new BufferedReader(new FileReader(f));
				String line;
				while ((line = r.readLine()) != null) {
					int index = line.lastIndexOf(' ');
					if (index != -1) {
						String code = line.substring(index + 1);
						for (char c : code.toCharArray()) {
							set.add(c);
							if (c == '4') {
								System.out.println(f);
								break;
							}
						}
					}
				}
				r.close();
			}
			System.out.print(set);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
}
