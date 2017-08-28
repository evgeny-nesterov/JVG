package ru.nest.jvg.geom.coord;

public class ProportionCoordinate extends AbstractCoordinate {
	public ProportionCoordinate(Coordinable coord1, Coordinable coord2, double koef) {
		this.coord1 = coord1;
		this.coord2 = coord2;
		this.koef = koef;
	}

	private Coordinable coord1;

	private Coordinable coord2;

	private double koef;

	@Override
	public double getCoord() {
		return (1 - koef) * coord1.getCoord() + koef * coord2.getCoord();
	}

	@Override
	public void setCoord(double coord) {
		double c1 = coord1.getCoord();
		double c2 = coord2.getCoord();
		if (c1 != c2) {
			koef = (coord - c1) / (c2 - c1);
		}
	}

	@Override
	public boolean isLocked() {
		return true;
	}
}
