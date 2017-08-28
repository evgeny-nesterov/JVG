package ru.nest.jvg.geom;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;

public class CoordinablePathIterator implements PathIterator {
	int typeIdx = 0;

	int pointIdx = 0;

	CoordinablePath path;

	AffineTransform affine;

	public static final int curvesize[] = { 2, 2, 4, 6, 0 };

	CoordinablePathIterator(CoordinablePath path) {
		this(path, null);
	}

	CoordinablePathIterator(CoordinablePath path, AffineTransform at) {
		this.path = path;
		this.affine = at;
	}

	@Override
	public int getWindingRule() {
		return path.getWindingRule();
	}

	@Override
	public boolean isDone() {
		return (typeIdx >= path.numTypes);
	}

	@Override
	public void next() {
		int type = path.pointTypes[typeIdx++];
		pointIdx += curvesize[type];
	}

	@Override
	public int currentSegment(float[] coords) {
		int type = path.pointTypes[typeIdx];
		int numCoords = curvesize[type];

		if (numCoords > 0 && affine != null) {
			double[] pointCoords = path.getCoordinates();
			affine.transform(pointCoords, pointIdx, coords, 0, numCoords / 2);
		} else {
			for (int i = 0; i < numCoords; i++) {
				coords[i] = (float) path.pointCoords[pointIdx + i].getCoord();
			}
		}
		return type;
	}

	@Override
	public int currentSegment(double[] coords) {
		int type = path.pointTypes[typeIdx];
		int numCoords = curvesize[type];

		double[] pointCoords = path.getCoordinates();
		if (numCoords > 0 && affine != null) {
			affine.transform(pointCoords, pointIdx, coords, 0, numCoords / 2);
		} else {
			System.arraycopy(pointCoords, pointIdx, coords, 0, numCoords);
		}
		return type;
	}
}
