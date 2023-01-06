package ru.nest.jvg.geom.coord;

public interface Coordinable {
	double getCoord();

	void setCoord(double coord);

	void shift(double delta);

	void setLocked(boolean isLocked);

	boolean isLocked();
}
