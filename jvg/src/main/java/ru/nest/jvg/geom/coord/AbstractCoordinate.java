package ru.nest.jvg.geom.coord;

public abstract class AbstractCoordinate implements Coordinable {
	private boolean isLocked = false;

	@Override
	public boolean isLocked() {
		return isLocked;
	}

	@Override
	public void setLocked(boolean isLocked) {
		this.isLocked = isLocked;
	}

	@Override
	public void shift(double delta) {
		setCoord(getCoord() + delta);
	}
}
