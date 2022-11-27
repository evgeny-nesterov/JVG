package ru.nest.jvg.geom;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.IllegalPathStateException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Vector;

import ru.nest.jvg.geom.coord.Coordinable;
import ru.nest.jvg.geom.coord.Coordinate;
import sun.awt.geom.Crossings;
import sun.awt.geom.Curve;

public class CoordinablePath implements Pathable<Coordinable> {
	public static final int WIND_EVEN_ODD = PathIterator.WIND_EVEN_ODD;

	public static final int WIND_NON_ZERO = PathIterator.WIND_NON_ZERO;

	private static final byte SEG_MOVETO = (byte) PathIterator.SEG_MOVETO;

	private static final byte SEG_LINETO = (byte) PathIterator.SEG_LINETO;

	private static final byte SEG_QUADTO = (byte) PathIterator.SEG_QUADTO;

	private static final byte SEG_CUBICTO = (byte) PathIterator.SEG_CUBICTO;

	private static final byte SEG_CLOSE = (byte) PathIterator.SEG_CLOSE;

	public byte[] pointTypes;

	public Coordinable[] pointCoords;

	public int numTypes;

	public int numCoords;

	int windingRule;

	static final int INIT_SIZE = 20;

	static final int EXPAND_MAX = 500;

	public CoordinablePath() {
		this(WIND_NON_ZERO, INIT_SIZE, INIT_SIZE);
	}

	public CoordinablePath(int rule) {
		this(rule, INIT_SIZE, INIT_SIZE);
	}

	public CoordinablePath(int rule, int initialCapacity) {
		this(rule, initialCapacity, initialCapacity);
	}

	CoordinablePath(int rule, int initialTypes, int initialCoords) {
		setWindingRule(rule);
		pointTypes = new byte[initialTypes];
		pointCoords = new Coordinable[initialCoords * 2];
	}

	public CoordinablePath(Shape s) {
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
			Coordinable[] arr = new Coordinable[size + grow];
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
	public boolean moveTo(Coordinable x, Coordinable y) {
		return insertMoveTo(numTypes, x, y);
	}

	@Override
	public synchronized boolean insertMoveTo(int index, Coordinable x, Coordinable y) {
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
	public synchronized boolean setMoveTo(int index, Coordinable x, Coordinable y) {
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
	public void lineTo(Coordinable x, Coordinable y) {
		insertLineTo(numTypes, x, y);
	}

	@Override
	public synchronized void insertLineTo(int index, Coordinable x, Coordinable y) {
		int coordIndex = getCoordIndex(index);
		needRoom(1, 2, true);
		shift(index, SEG_LINETO);

		pointTypes[index] = SEG_LINETO;
		pointCoords[coordIndex] = x;
		pointCoords[coordIndex + 1] = y;

		numTypes++;
		numCoords += 2;
	}

	@Override
	public synchronized boolean setLineTo(int index, Coordinable x, Coordinable y) {
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
	public void quadTo(Coordinable x1, Coordinable y1, Coordinable x2, Coordinable y2) {
		insertQuadTo(numTypes, x1, y1, x2, y2);
	}

	@Override
	public synchronized void insertQuadTo(int index, Coordinable x1, Coordinable y1, Coordinable x2, Coordinable y2) {
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
	public synchronized boolean setQuadTo(int index, Coordinable x1, Coordinable y1, Coordinable x2, Coordinable y2) {
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
	public synchronized void curveTo(Coordinable x1, Coordinable y1, Coordinable x2, Coordinable y2, Coordinable x3, Coordinable y3) {
		insertCurveTo(numTypes, x1, y1, x2, y2, x3, y3);
	}

	@Override
	public synchronized void insertCurveTo(int index, Coordinable x1, Coordinable y1, Coordinable x2, Coordinable y2, Coordinable x3, Coordinable y3) {
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
	public synchronized boolean setCurveTo(int index, Coordinable x1, Coordinable y1, Coordinable x2, Coordinable y2, Coordinable x3, Coordinable y3) {
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

	public void curveTo(int type, Coordinable[] c) {
		if (c != null) {
			switch (type) {
				case SEG_MOVETO:
					if (c.length >= 2) {
						moveTo(c[0], c[1]);
					}
					break;

				case SEG_LINETO:
					if (c.length >= 2) {
						lineTo(c[0], c[1]);
					}
					break;

				case SEG_QUADTO:
					if (c.length >= 4) {
						quadTo(c[0], c[1], c[2], c[3]);
					}
					break;

				case SEG_CUBICTO:
					if (c.length >= 6) {
						curveTo(c[0], c[1], c[2], c[3], c[4], c[5]);
					}
					break;

				case SEG_CLOSE:
					closePath();
					break;
			}
		}
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
						moveTo(new Coordinate(coords[0]), new Coordinate(coords[1]));
						break;
					}

					if (pointTypes[numTypes - 1] != SEG_CLOSE && pointCoords[numCoords - 2].getCoord() == coords[0] && pointCoords[numCoords - 1].getCoord() == coords[1]) {
						break;
					}

				case SEG_LINETO:
					lineTo(new Coordinate(coords[0]), new Coordinate(coords[1]));
					break;

				case SEG_QUADTO:
					quadTo(new Coordinate(coords[0]), new Coordinate(coords[1]), new Coordinate(coords[2]), new Coordinate(coords[3]));
					break;

				case SEG_CUBICTO:
					curveTo(new Coordinate(coords[0]), new Coordinate(coords[1]), new Coordinate(coords[2]), new Coordinate(coords[3]), new Coordinate(coords[4]), new Coordinate(coords[5]));
					break;

				case SEG_CLOSE:
					closePath();
					break;
			}
			pi.next();
			connect = false;
		}
	}

	@Override
	public void deleteLast() {
		delete(numTypes - 1);
	}

	@Override
	public synchronized void delete(int index) {
		if (index >= 0 && index < numTypes) {
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

	public void copy(CoordinablePath s) {
		numCoords = s.numCoords;
		numTypes = s.numTypes;
		windingRule = s.windingRule;

		if (pointTypes.length < s.pointTypes.length) {
			pointTypes = s.pointTypes.clone();
		} else {
			System.arraycopy(s.pointTypes, 0, pointTypes, 0, numTypes);
		}

		if (pointCoords.length < s.pointCoords.length) {
			pointCoords = new Coordinable[numCoords];
		}

		for (int i = 0; i < numCoords; i++) {
			Coordinable c = s.pointCoords[i];
			pointCoords[i] = new Coordinate(c.getCoord());
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

	public synchronized void reset() {
		numTypes = numCoords = 0;
		Arrays.fill(pointCoords, null);
	}

	private double[] coords = new double[0];

	public double[] getCoordinates() {
		if (coords.length != numCoords) {
			coords = new double[numCoords];
		}

		for (int i = 0; i < numCoords; i++) {
			coords[i] = pointCoords[i].getCoord();
		}
		return coords;
	}

	public void transform(AffineTransform at) {
		// float[] coords = getCoordinates();
		// at.transform(coords, 0, coords, 0, numCoords / 2);
		//
		// for (int i = 0; i < numCoords; i++)
		// {
		// if (!pointCoords[i].isLocked())
		// {
		// pointCoords[i].setCoord(coords[i]);
		// pointCoords[i].setLocked(true);
		// }
		// }

		transform(at, pointCoords, 0, numCoords / 2);
	}

	private double[] matrix;

	public void transform(AffineTransform at, Coordinable[] pts, int srcOff, int numPts) {
		if (matrix == null) {
			matrix = new double[6];
		}
		at.getMatrix(matrix);
		double M00 = matrix[0];
		double M01 = matrix[2];
		double M02 = matrix[4];
		double M10 = matrix[1];
		double M11 = matrix[3];
		double M12 = matrix[5];

		while (--numPts >= 0) {
			Coordinable cx = pts[srcOff++];
			Coordinable cy = pts[srcOff++];
			boolean lockedX = cx.isLocked();
			boolean lockedY = cy.isLocked();

			if (!lockedX || !lockedY) {
				double x = cx.getCoord();
				double y = cy.getCoord();
				if (!lockedX) {
					cx.setCoord(M00 * x + M01 * y + M02);
					cx.setLocked(true);
				}

				if (!lockedY) {
					cy.setCoord(M10 * x + M11 * y + M12);
					cy.setLocked(true);
				}
			}
		}
	}

	public void unlock(Coordinable[] pts, int srcOff, int numPts) {
		while (--numPts >= 0) {
			pts[srcOff++].setLocked(false);
			pts[srcOff++].setLocked(false);
		}
	}

	public synchronized Shape createTransformedShape(AffineTransform at) {
		CoordinablePath gp = (CoordinablePath) clone();
		if (at != null) {
			gp.transform(at);
		}
		return gp;
	}

	@Override
	public Rectangle getBounds() {
		return getBounds2D().getBounds();
	}

	@Override
	public synchronized Rectangle2D getBounds2D() {
		return getRealBounds();
		// return getBounds2D(pointCoords, 0, numCoords);
	}

	public synchronized Rectangle2D getRealBounds() {
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
				coords[j] = pointCoords[index + j].getCoord();
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
		Curve.insertLine(curves, curx, cury, movx, movy);

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

	public static Rectangle2D getBounds2D(Coordinable[] pointCoords, int start, int numCoords) {
		double x1, y1, x2, y2;
		int i = numCoords;
		if (i > start) {
			y1 = y2 = pointCoords[--i].getCoord();
			x1 = x2 = pointCoords[--i].getCoord();
			while (i > start) {
				double y = pointCoords[--i].getCoord();
				double x = pointCoords[--i].getCoord();

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
		Crossings c = Crossings.findCrossings(getPathIterator(null), x, y, x + w, y + h);
		return c == null;
	}

	@Override
	public boolean intersects(Rectangle2D r) {
		return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
	}

	@Override
	public PathIterator getPathIterator(AffineTransform at) {
		return new CoordinablePathIterator(this, at);
	}

	@Override
	public PathIterator getPathIterator(AffineTransform at, double flatness) {
		return new FlatteningPathIterator(getPathIterator(at), flatness);
	}

	@Override
	public Object clone() {
		try {
			CoordinablePath copy = (CoordinablePath) super.clone();
			copy.pointTypes = pointTypes.clone();
			copy.pointCoords = pointCoords.clone();
			return copy;
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}

	CoordinablePath(int windingRule, byte[] pointTypes, int numTypes, Coordinable[] pointCoords, int numCoords) {
		this.windingRule = windingRule;
		this.pointTypes = pointTypes;
		this.numTypes = numTypes;
		this.pointCoords = pointCoords;
		this.numCoords = numCoords;
	}

	@Override
	public int getCoordIndex(double x, double y, int start, double radius) {
		int coord = 0;
		for (int t = 0; t < numTypes; t++) {
			int type = pointTypes[t];
			int numcoords = CoordinablePathIterator.curvesize[type];
			for (int i = 0; i < numcoords; i += 2) {
				int px = (int) pointCoords[coord].getCoord();
				int py = (int) pointCoords[coord + 1].getCoord();

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

	public int getSegmentIndex(double x, double y, int start, double radius) {
		int coord = 0, segment = 0;
		for (int t = 0; t < numTypes; t++) {
			int type = pointTypes[t];
			int numcoords = CoordinablePathIterator.curvesize[type];
			for (int i = 0; i < numcoords; i += 2) {
				int px = (int) pointCoords[coord].getCoord();
				int py = (int) pointCoords[coord + 1].getCoord();
				if (x >= px - radius && x <= px + radius && y >= py - radius && y <= py + radius) {
					if (segment >= start) {
						return segment;
					}
				}
				coord += 2;
			}
			segment++;
		}
		return -1;
	}

	public void lock() {
		for (int i = 0; i < numCoords; i++) {
			pointCoords[i].setLocked(true);
		}
	}

	public void unlock() {
		for (int i = 0; i < numCoords; i++) {
			pointCoords[i].setLocked(false);
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
	public synchronized boolean insertPoint(int coordIndex, Coordinable x, Coordinable y) {
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
		return pointCoords[coordIndex].getCoord();
	}

	@Override
	public Coordinable[] getCurvePoints(int index) {
		int type = pointTypes[index];
		int curvesize = CoordinablePathIterator.curvesize[type];
		if (curvesize == 0) {
			return null;
		}

		int coordsIndex = getCoordIndex(index);
		Coordinable[] c = new Coordinable[curvesize];
		for (int i = 0; i < curvesize; i++) {
			c[i] = pointCoords[coordsIndex + i];
		}
		return c;
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
		return new Point2D.Double(pointCoords[index - 2].getCoord(), pointCoords[index - 1].getCoord());
	}
}
