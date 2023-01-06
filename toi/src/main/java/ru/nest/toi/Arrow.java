package ru.nest.toi;

public interface Arrow {
	double getWidth();

	void setWidth(double width);

	double getArrowWidth();

	void setArrowWidth(double arrowWidth);

	double getArrowLength();

	void setArrowLength(double arrowLength);

	void setScale(double scaleWidth, double scaleArrowWidth, double scaleArrowLength);
}
