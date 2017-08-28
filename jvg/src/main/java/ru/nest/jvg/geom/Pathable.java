package ru.nest.jvg.geom;

import java.awt.Shape;
import java.awt.geom.Point2D;

public interface Pathable<C> extends Shape {
	public boolean moveTo(C x, C y);

	public boolean insertMoveTo(int index, C x, C y);

	public boolean setMoveTo(int index, C x, C y);

	public void lineTo(C x, C y);

	public void insertLineTo(int index, C x, C y);

	public boolean setLineTo(int index, C x, C y);

	public void quadTo(C x1, C y1, C x2, C y2);

	public void insertQuadTo(int index, C x1, C y1, C x2, C y2);

	public boolean setQuadTo(int index, C x1, C y1, C x2, C y2);

	public void curveTo(C x1, C y1, C x2, C y2, C x3, C y3);

	public void insertCurveTo(int index, C x1, C y1, C x2, C y2, C x3, C y3);

	public boolean setCurveTo(int index, C x1, C y1, C x2, C y2, C x3, C y3);

	public boolean closePath();

	public Point2D getCurrentPoint();

	public void deleteLast();

	public void delete(int index);

	public void deletePoint(int coordIndex);

	public boolean insertPoint(int coordIndex, C x, C y);

	public int getCoordIndex(double x, double y, int start, double radius);

	public C[] getCurvePoints(int index);

	public int getCurvesCount();

	public int getCurveType(int curveIndex);

	public int getCoordsCount();

	public double getCoord(int coordIndex);

	public int getCurveIndex(int coordIndex);
}
