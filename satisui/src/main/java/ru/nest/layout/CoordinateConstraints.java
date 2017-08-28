package ru.nest.layout;

import java.awt.Component;
import java.awt.Rectangle;

public class CoordinateConstraints {
	public final static int PIXEL = 0;

	public final static int PERCENT = 1;

	public final static int PREFERRED = 2;

	public final static int TOP = 0;

	public final static int LEFT = 0;

	public final static int CENTER = 1;

	public final static int BOTTOM = 2;

	public final static int RIGHT = 2;

	private boolean isBoundsSet = true;

	public CoordinateConstraints() {
		isBoundsSet = false;
	}

	public CoordinateConstraints(double x, double y, double w, double h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	public CoordinateConstraints(double x, double y, double w, double h, int xType, int yType, int wType, int hType) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;

		this.xType = xType;
		this.yType = yType;
		this.wType = wType;
		this.hType = hType;
	}

	public CoordinateConstraints(double x, double y, double w, double h, int xType, int yType, int wType, int hType, int xAlignment, int yAlignment) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;

		this.xType = xType;
		this.yType = yType;
		this.wType = wType;
		this.hType = hType;

		this.xAlignment = xAlignment;
		this.yAlignment = yAlignment;
	}

	public double x;

	public double y;

	public double w;

	public double h;

	public int xType = PIXEL; // PIXEL, PERCENT

	public int yType = PIXEL; // PIXEL, PERCENT

	public int wType = PIXEL; // PIXEL, PERCENT, PREFERRED

	public int hType = PIXEL; // PIXEL, PERCENT, PREFERRED

	public int xAlignment = LEFT; // for PREFERRED type

	public int yAlignment = TOP; // for PREFERRED type

	private Bounds bounds = new Bounds();

	public Bounds getBounds(Component c, Rectangle parentBounds) // in pixels
	{
		if (isBoundsSet) {
			if (xType == PIXEL) {
				bounds.x = parentBounds.x + x;
			} else {
				bounds.x = parentBounds.x + x * parentBounds.width;
			}

			if (yType == PIXEL) {
				bounds.y = parentBounds.y + y;
			} else {
				bounds.y = parentBounds.y + y * parentBounds.height;
			}

			if (wType == PIXEL) {
				bounds.w = w;
			} else if (wType == PERCENT) {
				bounds.w = w * parentBounds.width;
			} else {
				bounds.w = c.getPreferredSize().width;
				if (bounds.w < 20) {
					bounds.w = 20;
				}
			}

			if (hType == PIXEL) {
				bounds.h = h;
			} else if (hType == PERCENT) {
				bounds.h = h * parentBounds.height;
			} else {
				bounds.h = c.getPreferredSize().height;
				if (bounds.h < 20) {
					bounds.h = 20;
				}
			}

			// --- align ---
			if (xAlignment == CENTER) {
				bounds.x -= bounds.w / 2.0;
			} else if (xAlignment == RIGHT) {
				bounds.x -= bounds.w;
			}

			if (yAlignment == CENTER) {
				bounds.y -= bounds.h / 2.0;
			} else if (yAlignment == BOTTOM) {
				bounds.y -= bounds.h;
			}
		} else {
			bounds.x = c.getX();
			bounds.y = c.getY();
			bounds.w = c.getWidth();
			bounds.h = c.getHeight();
		}

		return bounds;
	}

	public void setBounds(Component c, Rectangle parentBounds, Bounds bounds) {
		c.setBounds((int) bounds.x, (int) bounds.y, (int) (bounds.x + bounds.w) - (int) bounds.x, (int) (bounds.y + bounds.h) - (int) bounds.y);

		if (isBoundsSet) {
			// --- align ---
			if (xAlignment == CENTER) {
				bounds.x += bounds.w / 2.0;
			} else if (xAlignment == RIGHT) {
				bounds.x += bounds.w;
			}

			if (yAlignment == CENTER) {
				bounds.y += bounds.h / 2.0;
			} else if (yAlignment == BOTTOM) {
				bounds.y += bounds.h;
			}

			// ---
			if (xType == PIXEL) {
				x = bounds.x - parentBounds.x;
			} else if (xType == PERCENT) {
				x = (bounds.x - parentBounds.x) / parentBounds.width;
			}

			if (yType == PIXEL) {
				y = bounds.y - parentBounds.y;
			} else if (yType == PERCENT) {
				y = (bounds.y - parentBounds.y) / parentBounds.height;
			}

			if (wType == PIXEL) {
				w = bounds.w;
			} else if (wType == PERCENT) {
				w = bounds.w / parentBounds.width;
			}

			if (hType == PIXEL) {
				h = bounds.h;
			} else if (hType == PERCENT) {
				h = bounds.h / parentBounds.height;
			}
		} else {
			x = c.getX();
			y = c.getY();
			w = c.getWidth();
			h = c.getHeight();
		}
	}

	public void setTypes(Component c, Rectangle parentBounds, int xType, int yType, int wType, int hType) {
		Bounds b = getBounds(c, parentBounds);

		this.xType = xType;
		this.yType = yType;
		this.wType = wType;
		this.hType = hType;

		setBounds(c, parentBounds, b);
	}
}
