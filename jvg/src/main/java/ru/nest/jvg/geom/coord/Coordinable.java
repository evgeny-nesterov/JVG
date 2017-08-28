package ru.nest.jvg.geom.coord;

public interface Coordinable {
	public double getCoord();

	public void setCoord(double coord);

	public void shift(double delta);

	public void setLocked(boolean isLocked);

	public boolean isLocked();
}
