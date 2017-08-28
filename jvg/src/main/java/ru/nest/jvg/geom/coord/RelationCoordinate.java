package ru.nest.jvg.geom.coord;

import java.awt.geom.AffineTransform;

import ru.nest.jvg.geom.CoordinablePath;
import ru.nest.jvg.shape.JVGShape;

public class RelationCoordinate extends AbstractCoordinate {
	public final static int X = 0;

	public final static int Y = 1;

	public RelationCoordinate(JVGShape src, JVGShape dst, int pointIndex, int type) {
		this.src = src;
		this.dst = dst;
		this.pointIndex = pointIndex;
		this.type = type;
	}

	private JVGShape src;

	private JVGShape dst;

	private int pointIndex;

	private int type;

	private double[] matrix = new double[6];

	@Override
	public double getCoord() {
		CoordinablePath path = (CoordinablePath) src.getShape();

		int i = 2 * pointIndex;
		double x = path.pointCoords[i].getCoord();
		double y = path.pointCoords[i + 1].getCoord();

		// from src
		AffineTransform t = src.getTransform();
		t.getMatrix(matrix);

		double M00 = matrix[0];
		double M10 = matrix[1];
		double M01 = matrix[2];
		double M11 = matrix[3];
		double M02 = matrix[4];
		double M12 = matrix[5];

		x = M00 * x + M01 * y + M02;
		y = M10 * x + M11 * y + M12;

		// to dst
		t = dst.getInverseTransform();
		t.getMatrix(matrix);

		M00 = matrix[0];
		M10 = matrix[1];
		M01 = matrix[2];
		M11 = matrix[3];
		M02 = matrix[4];
		M12 = matrix[5];

		if (type == X) {
			return M00 * x + M01 * y + M02;
		} else {
			return M10 * x + M11 * y + M12;
		}
	}

	@Override
	public void setCoord(double coord) {
	}
}
