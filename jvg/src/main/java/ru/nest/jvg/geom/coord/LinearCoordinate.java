package ru.nest.jvg.geom.coord;

public class LinearCoordinate extends AbstractCoordinate {
	public LinearCoordinate(Coordinable[] coordinates, double[] koefs) {
		this.coordinates = coordinates;
		this.koefs = koefs;
	}

	private Coordinable[] coordinates;

	private double[] koefs;

	@Override
	public double getCoord() {
		double value = 0;
		for (int i = 0; i < coordinates.length; i++) {
			value += coordinates[i].getCoord() * koefs[i];
		}
		return value;
	}

	@Override
	public void setCoord(double coord) {
	}

	@Override
	public boolean isLocked() {
		return true;
	}
}
