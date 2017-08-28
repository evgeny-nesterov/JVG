package satis.iface.graph.def;

import satis.iface.graph.Bounds;
import satis.iface.graph.Group;
import satis.iface.graph.grid.Grid;

public class DefaultGrid extends Grid {
	private static final long serialVersionUID = 1L;

	private int orientation;

	private boolean isScaled = false;

	private boolean isContainPoint = false;

	public DefaultGrid(int orientation) {
		this.orientation = orientation;

		setOrientation(orientation);
		setRenderer(new DefaultGridRenderer());
	}

	public DefaultGrid(int orientation, boolean isScaled, boolean isContainPoint) {
		this.orientation = orientation;
		this.isScaled = isScaled;
		this.isContainPoint = isContainPoint;

		setOrientation(orientation);
		setRenderer(new DefaultGridRenderer());
	}

	private double point = 0;

	public void setGridPoint(double point) {
		this.point = point;
	}

	private boolean isAddLineOnThreshhold = false;

	public void setAddLineOnThreshhold(boolean isAddLineOnThreshhold) {
		this.isAddLineOnThreshhold = isAddLineOnThreshhold;
	}

	public boolean isAddLineOnThreshhold() {
		return isAddLineOnThreshhold;
	}

	private int threshhold = 5;

	public void setThreshhold(int threshhold) {
		this.threshhold = threshhold;
	}

	public int getThreshhold() {
		return threshhold;
	}

	private int mainGridPeriod = 1, shiftMain = 0;

	public void setMainGridPeriod(int mainGridPeriod) {
		if (mainGridPeriod < 1) {
			mainGridPeriod = 1;
		}

		this.mainGridPeriod = mainGridPeriod;
	}

	public int getMainGridPeriod() {
		return mainGridPeriod;
	}

	private double minValue, delta;

	private int count = 0, koef = 1;

	@Override
	public void update(Group group) {
		delta = 0;
		count = 0;
		koef = 1;

		Bounds B = group.getBounds();
		Bounds b = group.getInnerBounds();
		int n = getGridCount();

		if (!isScaled) {
			switch (orientation) {
				case Y_AXIS:
					delta = b.h / n;
					if (isContainPoint) {
						minValue = b.y + (point - b.y) % delta;
						if (point < b.y) {
							minValue += delta;
						}

						shiftMain = (int) ((point - minValue) / delta);
					} else {
						minValue = b.y;
						shiftMain = 0;
					}

					count = n;
					if (minValue == b.y) {
						count++;
					}
					break;

				case X_AXIS:
					delta = b.w / n;
					if (isContainPoint) {
						minValue = b.x + (point - b.x) % delta;
						if (point < b.x) {
							minValue += delta;
						}

						shiftMain = (int) ((point - minValue) / delta);
					} else {
						minValue = b.x;
						shiftMain = 0;
					}

					count = n;
					if (minValue == b.x) {
						count++;
					}
					break;
			}
		} else {
			switch (orientation) {
				case Y_AXIS:
					delta = B.h / n;
					if (isContainPoint) {
						minValue = b.y + (point - b.y) % delta;
						shiftMain = (int) ((point - minValue) / delta);
					} else {
						minValue = b.y + (B.y - b.y) % delta;
						shiftMain = (int) ((B.y - minValue) / delta);
					}

					if (delta > 0) {
						count = (int) Math.floor((b.y + b.h - minValue) / delta) + 1;
					}
					break;

				case X_AXIS:
					delta = B.w / n;
					if (isContainPoint) {
						minValue = b.x + (point - b.x) % delta;
						shiftMain = (int) ((point - minValue) / delta);
					} else {
						minValue = b.x + (B.x - b.x) % delta;
						shiftMain = (int) ((B.x - minValue) / delta);
					}

					if (delta > 0) {
						count = (int) Math.floor((b.x + b.w - minValue) / delta) + 1;
					}
					break;
			}
		}
		count++;
		minValue -= delta;

		// if(isPaintGrid())
		// switch(orientation)
		// {
		// case X_AXIS:
		// log.debug("X Count: " + count + ", (" + df.format(new
		// java.util.Date((long)minValue)) + ", " +
		// df.format(new java.util.Date((long)(minValue + delta * (count - 1))))
		// + "), delta = " +
		// df.format(new java.util.Date((long)delta)));
		// break;
		//
		// case Y_AXIS:
		// log.debug("Y Count: " + count + ", (" + minValue + ", " + (minValue +
		// delta * (count - 1)) + "), delta = " + delta);
		// break;
		// }

		if (isAddLineOnThreshhold) {
			double d = 0;
			switch (orientation) {
				case X_AXIS:
					d = b.getW();
					break;

				case Y_AXIS:
					d = b.getH();
					break;
			}

			double c = d / delta;
			if (c < threshhold && c > 0) {
				koef = (int) Math.pow(2, (int) (Math.log(threshhold / c + 1) / Math.log(2)));
				count *= koef;
				delta /= koef;
			}
		}
		// if(isPaintGrid() && orientation == X_AXIS)
		// log.debug("Shift: " + shiftMain);
	}

	// private java.text.SimpleDateFormat df = new
	// java.text.SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");

	@Override
	public int getVisibleLinesCount() {
		return count;
	}

	@Override
	public double getValue(int index) {
		return minValue + index * delta;
	}

	public boolean isMainGridLine(int index) {
		if (koef > 1) {
			if (index % koef != 0) {
				return false;
			} else {
				if (mainGridPeriod <= 1) {
					return true;
				} else {
					return (index / koef - shiftMain) % mainGridPeriod == 0;
				}
			}
		} else {
			if (mainGridPeriod <= 1) {
				return true;
			} else {
				return (index - shiftMain) % mainGridPeriod == 0;
			}
		}
	}
}
