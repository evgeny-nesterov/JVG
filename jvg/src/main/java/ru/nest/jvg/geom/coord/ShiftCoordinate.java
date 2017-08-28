package ru.nest.jvg.geom.coord;

public class ShiftCoordinate extends AbstractCoordinate {
	public ShiftCoordinate(Coordinable coord, double shift) {
		this.coord = coord;
		this.shift = shift;
	}

	private Coordinable coord;

	private double shift;

	@Override
	public double getCoord() {
		return coord.getCoord() + shift;
	}

	@Override
	public void setCoord(double coord) {
		this.shift = coord - this.coord.getCoord();
	}

	@Override
	public boolean isLocked() {
		return true;
	}
}
