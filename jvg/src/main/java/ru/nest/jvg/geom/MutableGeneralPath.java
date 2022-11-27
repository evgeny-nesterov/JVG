package ru.nest.jvg.geom;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.IllegalPathStateException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

import sun.awt.geom.Crossings;
import sun.awt.geom.Curve;

public class MutableGeneralPath implements Pathable<Double>, Cloneable {
	public static final int WIND_EVEN_ODD = PathIterator.WIND_EVEN_ODD;

	public static final int WIND_NON_ZERO = PathIterator.WIND_NON_ZERO;

	private static final byte SEG_MOVETO = (byte) PathIterator.SEG_MOVETO;

	private static final byte SEG_LINETO = (byte) PathIterator.SEG_LINETO;

	private static final byte SEG_QUADTO = (byte) PathIterator.SEG_QUADTO;

	private static final byte SEG_CUBICTO = (byte) PathIterator.SEG_CUBICTO;

	private static final byte SEG_CLOSE = (byte) PathIterator.SEG_CLOSE;

	public byte[] pointTypes;

	public double[] pointCoords;

	public int numTypes;

	public int numCoords;

	int windingRule;

	static final int INIT_SIZE = 20;

	static final int EXPAND_MAX = 500;

	public MutableGeneralPath() {
		this(WIND_NON_ZERO, INIT_SIZE, INIT_SIZE);
	}

	public MutableGeneralPath(int rule) {
		this(rule, INIT_SIZE, INIT_SIZE);
	}

	public MutableGeneralPath(int rule, int initialCapacity) {
		this(rule, initialCapacity, initialCapacity);
	}

	MutableGeneralPath(int rule, int initialTypes, int initialCoords) {
		setWindingRule(rule);
		pointTypes = new byte[initialTypes];
		pointCoords = new double[initialCoords * 2];
	}

	public MutableGeneralPath(Shape s) {
		this(WIND_NON_ZERO, INIT_SIZE, INIT_SIZE);
		PathIterator pi = s.getPathIterator(null);
		setWindingRule(pi.getWindingRule());
		append(pi, false);
	}

	private void needRoom(int newTypes, int newCoords, boolean needMove) {
		if (needMove && numTypes == 0) {
			throw new IllegalPathStateException("missing initial moveto " + "in path definition");
		}
		int size = pointCoords.length;
		if (numCoords + newCoords > size) {
			int grow = size;
			if (grow > EXPAND_MAX * 2) {
				grow = EXPAND_MAX * 2;
			}
			if (grow < newCoords) {
				grow = newCoords;
			}
			double[] arr = new double[size + grow];
			System.arraycopy(pointCoords, 0, arr, 0, numCoords);
			pointCoords = arr;
		}
		size = pointTypes.length;
		if (numTypes + newTypes > size) {
			int grow = size;
			if (grow > EXPAND_MAX) {
				grow = EXPAND_MAX;
			}
			if (grow < newTypes) {
				grow = newTypes;
			}

			byte[] arr = new byte[size + grow];
			System.arraycopy(pointTypes, 0, arr, 0, numTypes);
			pointTypes = arr;
		}
	}

	public void clean() {
		numTypes = 0;
		numCoords = 0;
	}

	public int getCoordIndex(int curveIndex) {
		if (curveIndex == numTypes) {
			return numCoords;
		} else {
			int coord = 0;
			for (int t = 0; t < curveIndex; t++) {
				int type = pointTypes[t];
				int curvesize = CoordinablePathIterator.curvesize[type];
				coord += curvesize;
			}
			return coord;
		}
	}

	private void shift(int index, int type) {
		if (index < numTypes) {
			int curveindex = getCoordIndex(index);
			int curvesize = CoordinablePathIterator.curvesize[type];
			System.arraycopy(pointCoords, curveindex, pointCoords, curveindex + curvesize, numCoords - curveindex - curvesize);
			System.arraycopy(pointTypes, index, pointTypes, index + 1, numTypes - index - 1);
		}
	}

	@Override
	public boolean moveTo(Double x, Double y) {
		return insertMoveTo(numTypes, x, y);
	}

	@Override
	public synchronized boolean insertMoveTo(int index, Double x, Double y) {
		if (numTypes > 0 && pointTypes[index - 1] == SEG_MOVETO) {
			int prevCoordIndex = getCoordIndex(index - 1);
			pointCoords[prevCoordIndex] = x;
			pointCoords[prevCoordIndex + 1] = y;
			return false;
		}

		int coordIndex = getCoordIndex(index);
		needRoom(1, 2, false);
		shift(index, SEG_MOVETO);

		pointTypes[index] = SEG_MOVETO;
		pointCoords[coordIndex++] = x;
		pointCoords[coordIndex] = y;

		numTypes++;
		numCoords += 2;
		return true;
	}

	@Override
	public synchronized boolean setMoveTo(int index, Double x, Double y) {
		int type = pointTypes[index];
		pointTypes[index] = SEG_MOVETO;

		int coordIndex = getCoordIndex(index);
		switch (type) {
			case SEG_MOVETO:
			case SEG_LINETO:
				break;
			case SEG_QUADTO:
				numCoords -= 2;
				System.arraycopy(pointCoords, coordIndex + 2, pointCoords, coordIndex, numCoords - coordIndex);
				break;
			case SEG_CUBICTO:
				numCoords -= 4;
				System.arraycopy(pointCoords, coordIndex + 4, pointCoords, coordIndex, numCoords - coordIndex);
				break;
		}
		pointCoords[coordIndex++] = x;
		pointCoords[coordIndex] = y;
		return true;
	}

	@Override
	public void lineTo(Double x, Double y) {
		if (numTypes > 0) {
			insertLineTo(numTypes, x, y);
		} else {
			insertMoveTo(numTypes, x, y);
		}
	}

	@Override
	public synchronized void insertLineTo(int index, Double x, Double y) {
		int coordIndex = getCoordIndex(index);
		needRoom(1, 2, true);
		shift(index, SEG_LINETO);

		pointTypes[index] = SEG_LINETO;
		pointCoords[coordIndex++] = x;
		pointCoords[coordIndex] = y;

		numTypes++;
		numCoords += 2;
	}

	@Override
	public synchronized boolean setLineTo(int index, Double x, Double y) {
		int type = pointTypes[index];
		pointTypes[index] = SEG_LINETO;

		int coordIndex = getCoordIndex(index);
		switch (type) {
			case SEG_MOVETO:
			case SEG_LINETO:
				break;
			case SEG_QUADTO:
				numCoords -= 2;
				System.arraycopy(pointCoords, coordIndex + 2, pointCoords, coordIndex, numCoords - coordIndex);
				break;
			case SEG_CUBICTO:
				numCoords -= 4;
				System.arraycopy(pointCoords, coordIndex + 4, pointCoords, coordIndex, numCoords - coordIndex);
				break;
		}
		pointCoords[coordIndex++] = x;
		pointCoords[coordIndex] = y;
		return true;
	}

	@Override
	public void quadTo(Double x1, Double y1, Double x2, Double y2) {
		insertQuadTo(numTypes, x1, y1, x2, y2);
	}

	@Override
	public synchronized void insertQuadTo(int index, Double x1, Double y1, Double x2, Double y2) {
		int coordIndex = getCoordIndex(index);
		needRoom(1, 4, true);
		shift(index, SEG_QUADTO);

		pointTypes[index] = SEG_QUADTO;
		pointCoords[coordIndex++] = x1;
		pointCoords[coordIndex++] = y1;
		pointCoords[coordIndex++] = x2;
		pointCoords[coordIndex] = y2;

		numTypes++;
		numCoords += 4;
	}

	@Override
	public synchronized boolean setQuadTo(int index, Double x1, Double y1, Double x2, Double y2) {
		int type = pointTypes[index];
		pointTypes[index] = SEG_QUADTO;

		int coordIndex = getCoordIndex(index);
		switch (type) {
			case SEG_MOVETO:
			case SEG_LINETO:
				needRoom(0, 2, true);
				System.arraycopy(pointCoords, coordIndex, pointCoords, coordIndex + 2, numCoords - coordIndex);
				numCoords += 2;
				break;
			case SEG_QUADTO:
				break;
			case SEG_CUBICTO:
				numCoords -= 2;
				System.arraycopy(pointCoords, coordIndex + 2, pointCoords, coordIndex, numCoords - coordIndex);
				break;
		}
		pointCoords[coordIndex++] = x1;
		pointCoords[coordIndex++] = y1;
		pointCoords[coordIndex++] = x2;
		pointCoords[coordIndex] = y2;
		return true;
	}

	@Override
	public synchronized void curveTo(Double x1, Double y1, Double x2, Double y2, Double x3, Double y3) {
		insertCurveTo(numTypes, x1, y1, x2, y2, x3, y3);
	}

	@Override
	public synchronized void insertCurveTo(int index, Double x1, Double y1, Double x2, Double y2, Double x3, Double y3) {
		int coordIndex = getCoordIndex(index);
		needRoom(1, 6, true);
		shift(index, SEG_CUBICTO);

		pointTypes[index] = SEG_CUBICTO;
		pointCoords[coordIndex++] = x1;
		pointCoords[coordIndex++] = y1;
		pointCoords[coordIndex++] = x2;
		pointCoords[coordIndex++] = y2;
		pointCoords[coordIndex++] = x3;
		pointCoords[coordIndex] = y3;

		numTypes++;
		numCoords += 6;
	}

	@Override
	public synchronized boolean setCurveTo(int index, Double x1, Double y1, Double x2, Double y2, Double x3, Double y3) {
		int type = pointTypes[index];
		pointTypes[index] = SEG_CUBICTO;

		int coordIndex = getCoordIndex(index);
		switch (type) {
			case SEG_MOVETO:
			case SEG_LINETO:
				needRoom(0, 4, true);
				System.arraycopy(pointCoords, coordIndex, pointCoords, coordIndex + 4, numCoords - coordIndex);
				numCoords += 4;
				break;
			case SEG_QUADTO:
				needRoom(0, 2, true);
				System.arraycopy(pointCoords, coordIndex, pointCoords, coordIndex + 2, numCoords - coordIndex);
				numCoords += 2;
				break;
			case SEG_CUBICTO:
				break;
		}
		pointCoords[coordIndex++] = x1;
		pointCoords[coordIndex++] = y1;
		pointCoords[coordIndex++] = x2;
		pointCoords[coordIndex++] = y2;
		pointCoords[coordIndex++] = x3;
		pointCoords[coordIndex] = y3;
		return true;
	}

	@Override
	public synchronized boolean closePath() {
		if (numTypes == 0 || pointTypes[numTypes - 1] != SEG_CLOSE) {
			needRoom(1, 0, true);
			pointTypes[numTypes++] = SEG_CLOSE;
			return true;
		} else {
			return false;
		}
	}

	public void append(Shape s, boolean connect) {
		PathIterator pi = s.getPathIterator(null);
		append(pi, connect);
	}

	public void append(PathIterator pi, boolean connect) {
		double coords[] = new double[6];
		while (!pi.isDone()) {
			switch (pi.currentSegment(coords)) {
				case SEG_MOVETO:
					if (!connect || numTypes < 1 || numCoords < 2) {
						moveTo(coords[0], coords[1]);
						break;
					}
					if (pointTypes[numTypes - 1] != SEG_CLOSE && pointCoords[numCoords - 2] == coords[0] && pointCoords[numCoords - 1] == coords[1]) {
						// Collapse out initial moveto/lineto
						break;
					}
					// NO BREAK;

				case SEG_LINETO:
					lineTo(coords[0], coords[1]);
					break;

				case SEG_QUADTO:
					quadTo(coords[0], coords[1], coords[2], coords[3]);
					break;

				case SEG_CUBICTO:
					curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
					break;

				case SEG_CLOSE:
					closePath();
					break;
			}
			pi.next();
			connect = false;
		}
	}

	public synchronized int getWindingRule() {
		return windingRule;
	}

	public void setWindingRule(int rule) {
		if (rule != WIND_EVEN_ODD && rule != WIND_NON_ZERO) {
			throw new IllegalArgumentException("winding rule must be WIND_EVEN_ODD or WIND_NON_ZERO");
		}
		windingRule = rule;
	}

	@Override
	public synchronized Point2D getCurrentPoint() {
		if (numTypes < 1 || numCoords < 2) {
			return null;
		}

		int index = numCoords;
		if (pointTypes[numTypes - 1] == SEG_CLOSE) {
			loop: for (int i = numTypes - 2; i > 0; i--) {
				switch (pointTypes[i]) {
					case SEG_MOVETO:
						break loop;

					case SEG_LINETO:
						index -= 2;
						break;

					case SEG_QUADTO:
						index -= 4;
						break;

					case SEG_CUBICTO:
						index -= 6;
						break;

					case SEG_CLOSE:
						break;
				}
			}
		}
		return new Point2D.Double(pointCoords[index - 2], pointCoords[index - 1]);
	}

	public synchronized void reset() {
		numTypes = numCoords = 0;
	}

	public void transform(AffineTransform at) {
		at.transform(pointCoords, 0, pointCoords, 0, numCoords / 2);
	}

	public synchronized Shape createTransformedShape(AffineTransform at) {
		MutableGeneralPath gp = (MutableGeneralPath) clone();
		if (at != null) {
			gp.transform(at);
		}
		return gp;
	}

	@Override
	public java.awt.Rectangle getBounds() {
		return getBounds2D().getBounds();
	}

	public boolean isPolygonal() {
		int index = 0;
		for (int i = 0; i < numTypes; i++) {
			int type = pointTypes[i];
			int coordsCount = CoordinablePathIterator.curvesize[type];

			if (pointTypes[i] == SEG_QUADTO) {
				double x1 = pointCoords[index - 2];
				double y1 = pointCoords[index - 1];
				double xc = pointCoords[index];
				double yc = pointCoords[index + 1];
				double x2 = pointCoords[index + 2];
				double y2 = pointCoords[index + 3];

				xc -= x1;
				yc -= y1;
				x2 -= x1;
				y2 -= y1;

				if (x2 * yc != y2 * xc) {
					return false;
				}
			} else if (pointTypes[i] == SEG_CUBICTO) {
				double x1 = pointCoords[index - 2];
				double y1 = pointCoords[index - 1];
				double xc1 = pointCoords[index];
				double yc1 = pointCoords[index + 1];
				double xc2 = pointCoords[index + 2];
				double yc2 = pointCoords[index + 3];
				double x2 = pointCoords[index + 4];
				double y2 = pointCoords[index + 5];

				xc1 -= x1;
				yc1 -= y1;
				xc2 -= x1;
				yc2 -= y1;
				x2 -= x1;
				y2 -= y1;

				if (xc1 * yc2 != yc1 * xc2 || x2 * yc2 != y2 * xc2) {
					return false;
				}
			}

			index += coordsCount;
		}
		return true;
	}

	public boolean isHorizintalLine() {
		if (numCoords > 0) {
			double y = pointCoords[1];
			for (int i = 3; i < numCoords; i += 2) {
				if (y != pointCoords[i]) {
					return false;
				}
			}
		}
		return true;
	}

	private Rectangle2D getRealBounds() {
		if (isPolygonal()) {
			return getPolygonalBounds();
		}

		int index = 0;
		double coords[] = new double[23];
		double movx = 0, movy = 0;
		double curx = 0, cury = 0;
		double newx, newy;
		Vector<Curve> curves = new Vector<>();
		for (int i = 0; i < numTypes; i++) {
			int type = pointTypes[i];
			int coordsCount = CoordinablePathIterator.curvesize[type];
			for (int j = 0; j < coordsCount; j++) {
				coords[j] = pointCoords[index + j];
			}

			switch (type) {
				case SEG_MOVETO:
					Curve.insertLine(curves, curx, cury, movx, movy);
					curx = movx = coords[0];
					cury = movy = coords[1];
					Curve.insertMove(curves, movx, movy);
					break;

				case SEG_LINETO:
					newx = coords[0];
					newy = coords[1];
					Curve.insertLine(curves, curx, cury, newx, newy);
					curx = newx;
					cury = newy;
					break;

				case SEG_QUADTO:
					newx = coords[2];
					newy = coords[3];
					Curve.insertQuad(curves, curx, cury, coords);
					curx = newx;
					cury = newy;
					break;

				case SEG_CUBICTO:
					newx = coords[4];
					newy = coords[5];
					Curve.insertCubic(curves, curx, cury, coords);
					curx = newx;
					cury = newy;
					break;

				case SEG_CLOSE:
					Curve.insertLine(curves, curx, cury, movx, movy);
					curx = movx;
					cury = movy;
					break;
			}
			index += coordsCount;
		}

		// ??? seg_close
		// Curve.insertLine(curves, curx, cury, movx, movy);

		Rectangle2D r = new Rectangle2D.Double();
		if (curves.size() > 0) {
			Curve c = curves.get(0);
			r.setRect(c.getX0(), c.getY0(), 0, 0);
			for (int i = 1; i < curves.size(); i++) {
				curves.get(i).enlarge(r);
			}
		}
		return r;
	}

	private Rectangle2D getPolygonalBounds() {
		double x1, y1, x2, y2;
		int i = numCoords;
		if (i > 0) {
			y1 = y2 = pointCoords[--i];
			x1 = x2 = pointCoords[--i];
			while (i > 0) {
				double y = pointCoords[--i];
				double x = pointCoords[--i];
				if (x < x1) {
					x1 = x;
				}
				if (y < y1) {
					y1 = y;
				}
				if (x > x2) {
					x2 = x;
				}
				if (y > y2) {
					y2 = y;
				}
			}
		} else {
			x1 = y1 = x2 = y2 = 0.0f;
		}
		return new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1);
	}

	@Override
	public synchronized Rectangle2D getBounds2D() {
		return getRealBounds();
	}

	@Override
	public boolean contains(double x, double y) {
		if (numTypes < 2) {
			return false;
		}

		int cross = Curve.pointCrossingsForPath(getPathIterator(null), x, y);
		if (windingRule == WIND_NON_ZERO) {
			return (cross != 0);
		} else {
			return ((cross & 1) != 0);
		}
	}

	@Override
	public boolean contains(Point2D p) {
		return contains(p.getX(), p.getY());
	}

	@Override
	public boolean contains(double x, double y, double w, double h) {
		Crossings c = Crossings.findCrossings(getPathIterator(null), x, y, x + w, y + h);
		return (c != null && c.covers(y, y + h));
	}

	@Override
	public boolean contains(Rectangle2D r) {
		return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
	}

	@Override
	public boolean intersects(double x, double y, double w, double h) {
		// fix issue: for horizontal line it always returns false
		if (isHorizintalLine()) {
			Rectangle2D bounds = getPolygonalBounds();
			double x0 = bounds.getX();
			double y0 = bounds.getY();
			return x + w > x0 && y + h > y0 && x < x0 + bounds.getWidth() && y < y0 + bounds.getHeight();

		}

		Crossings c = Crossings.findCrossings(getPathIterator(null), x, y, x + w, y + h);
		return c == null;
	}

	@Override
	public boolean intersects(Rectangle2D r) {
		return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
	}

	@Override
	public PathIterator getPathIterator(AffineTransform at) {
		return new MutableGeneralPathIterator(this, at);
	}

	@Override
	public PathIterator getPathIterator(AffineTransform at, double flatness) {
		return new FlatteningPathIterator(getPathIterator(at), flatness);
	}

	@Override
	public Object clone() {
		try {
			MutableGeneralPath copy = (MutableGeneralPath) super.clone();
			copy.pointTypes = pointTypes.clone();
			copy.pointCoords = pointCoords.clone();
			return copy;
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}

	MutableGeneralPath(int windingRule, byte[] pointTypes, int numTypes, double[] pointCoords, int numCoords) {
		this.windingRule = windingRule;
		this.pointTypes = pointTypes;
		this.numTypes = numTypes;
		this.pointCoords = pointCoords;
		this.numCoords = numCoords;
	}

	@Override
	public void deleteLast() {
		delete(numTypes - 1);
	}

	@Override
	public synchronized void delete(int index) {
		// first moveto is not allowed to delete
		if (index > 0 && index < numTypes) {
			int type = pointTypes[index];
			int size = CoordinablePathIterator.curvesize[type];
			if (size > 0) {
				int coordIndex = 0;
				for (int i = 0; i < index; i++) {
					coordIndex += CoordinablePathIterator.curvesize[pointTypes[i]];
				}
				System.arraycopy(pointCoords, coordIndex + size, pointCoords, coordIndex, numCoords - coordIndex - size);
			}
			if (index < numTypes - 1) {
				System.arraycopy(pointTypes, index + 1, pointTypes, index, numTypes - index - 1);
			}

			numCoords -= size;
			numTypes--;
		}
	}

	@Override
	public synchronized void deletePoint(int coordIndex) {
		if (coordIndex >= 0 && coordIndex < numCoords) {
			int index = getCurveIndex(coordIndex);
			int type = pointTypes[index];
			int curvesize = CoordinablePathIterator.curvesize[type];
			if (curvesize == 1) {
				delete(index);
			} else if (curvesize > 1) {
				if (type == SEG_QUADTO) {
					pointTypes[index] = SEG_LINETO;
				} else if (type == SEG_CUBICTO) {
					pointTypes[index] = SEG_QUADTO;
				}

				System.arraycopy(pointCoords, coordIndex + 2, pointCoords, coordIndex, numCoords - index - 2);
				numCoords -= 2;
			}
		}
	}

	@Override
	public synchronized boolean insertPoint(int coordIndex, Double x, Double y) {
		if (coordIndex >= 0 && coordIndex < numCoords) {
			int index = getCurveIndex(coordIndex);
			int type = pointTypes[index];
			if (type == SEG_LINETO || type == SEG_QUADTO) {
				needRoom(0, 2, false);

				if (type == SEG_LINETO) {
					pointTypes[index] = SEG_QUADTO;
				} else if (type == SEG_QUADTO) {
					pointTypes[index] = SEG_CUBICTO;
				}

				System.arraycopy(pointCoords, coordIndex, pointCoords, coordIndex + 2, numCoords - index);
				numCoords += 2;

				pointCoords[coordIndex++] = x;
				pointCoords[coordIndex] = y;
				return true;
			}
		}
		return false;
	}

	@Override
	public int getCoordIndex(double x, double y, int start, double radius) {
		int coord = 0;
		for (int t = 0; t < numTypes; t++) {
			int type = pointTypes[t];
			int numcoords = CoordinablePathIterator.curvesize[type];
			for (int i = 0; i < numcoords; i += 2) {
				int px = (int) pointCoords[coord];
				int py = (int) pointCoords[coord + 1];
				if (x >= px - radius && x <= px + radius && y >= py - radius && y <= py + radius) {
					int index = coord / 2;
					if (index >= start) {
						return index;
					}
				}
				coord += 2;
			}
		}
		return -1;
	}

	@Override
	public Double[] getCurvePoints(int index) {
		int type = pointTypes[index];
		int curvesize = CoordinablePathIterator.curvesize[type];
		if (curvesize == 0) {
			return null;
		}

		int coordsIndex = getCoordIndex(index);
		Double[] c = new Double[curvesize];
		for (int i = 0; i < curvesize; i++) {
			c[i] = pointCoords[coordsIndex + i];
		}
		return c;
	}

	@Override
	public int getCurvesCount() {
		return numTypes;
	}

	@Override
	public int getCurveType(int curveIndex) {
		return pointTypes[curveIndex];
	}

	@Override
	public int getCoordsCount() {
		return numCoords;
	}

	@Override
	public double getCoord(int coordIndex) {
		return pointCoords[coordIndex];
	}

	@Override
	public int getCurveIndex(int coordIndex) {
		int coord = 0;
		for (int t = 0; t < numTypes; t++) {
			int type = pointTypes[t];
			int curvesize = CoordinablePathIterator.curvesize[type];
			int next = coord + curvesize;
			if (coordIndex >= coord && coordIndex < next) {
				return t;
			}
			coord = next;
		}
		return -1;
	}
}
