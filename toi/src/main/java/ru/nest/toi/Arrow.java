package ru.nest.toi;

public interface Arrow {
	public double getWidth();

	public void setWidth(double width);

	public double getArrowWidth();

	public void setArrowWidth(double arrowWidth);

	public double getArrowLength();

	public void setArrowLength(double arrowLength);

	public void setScale(double scaleWidth, double scaleArrowWidth, double scaleArrowLength);
}
