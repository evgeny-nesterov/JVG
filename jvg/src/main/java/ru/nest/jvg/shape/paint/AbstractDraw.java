package ru.nest.jvg.shape.paint;

public abstract class AbstractDraw<V> implements Draw<V> {
	private double opacity = 1;

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException exc) {
			exc.printStackTrace();
			return null;
		}
	}

	public double getOpacity() {
		return opacity;
	}

	public void setOpacity(double opacity) {
		this.opacity = opacity;
	}
}
