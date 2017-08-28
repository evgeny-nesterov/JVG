package ru.nest.jvg.geom.coord;

public class Coordinate extends AbstractCoordinate {
	public Coordinate(double coord) {
		this.coord = coord;
	}

	private double coord;

	@Override
	public double getCoord() {
		return coord;
	}

	@Override
	public void setCoord(double coord) {
		this.coord = coord;
	}
}
