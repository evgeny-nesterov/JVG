package ru.nest.jvg.geom;

import java.awt.Shape;
import java.awt.geom.Point2D;

public interface Pathable<C> extends Shape {
	boolean moveTo(C x, C y);

	boolean insertMoveTo(int index, C x, C y);

	boolean setMoveTo(int index, C x, C y);

	void lineTo(C x, C y);

	void insertLineTo(int index, C x, C y);

	boolean setLineTo(int index, C x, C y);

	void quadTo(C x1, C y1, C x2, C y2);

	void insertQuadTo(int index, C x1, C y1, C x2, C y2);

	boolean setQuadTo(int index, C x1, C y1, C x2, C y2);

	void curveTo(C x1, C y1, C x2, C y2, C x3, C y3);

	void insertCurveTo(int index, C x1, C y1, C x2, C y2, C x3, C y3);

	boolean setCurveTo(int index, C x1, C y1, C x2, C y2, C x3, C y3);

	boolean closePath();

	Point2D getCurrentPoint();

	void deleteLast();

	void delete(int index);

	void deletePoint(int coordIndex);

	boolean insertPoint(int coordIndex, C x, C y);

	int getCoordIndex(double x, double y, int start, double radius);

	C[] getCurvePoints(int index);

	int getCurvesCount();

	int getCurveType(int curveIndex);

	int getCoordsCount();

	double getCoord(int coordIndex);

	int getCurveIndex(int coordIndex);
}
