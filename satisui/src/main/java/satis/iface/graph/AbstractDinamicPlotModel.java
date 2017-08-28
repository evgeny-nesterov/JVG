package satis.iface.graph;

public abstract class AbstractDinamicPlotModel extends AbstractPlotModel implements DinamicPlotModel {
	private int markerIndex;

	private double markerX, markerY, markerAlfa;

	private double realMarkerX, realMarkerY;

	@Override
	public int getMarkerIndex() {
		return markerIndex;
	}

	@Override
	public double getMarkerX() {
		return markerX;
	}

	@Override
	public double getMarkerY() {
		return markerY;
	}

	@Override
	public double getRealMarkerX() {
		return realMarkerX;
	}

	@Override
	public double getRealMarkerY() {
		return realMarkerY;
	}

	private boolean isDrawMarker = true;

	@Override
	public boolean isDrawMarker() {
		return isDrawMarker;
	}

	@Override
	public void setDrawMarker(boolean isDrawMarker) {
		this.isDrawMarker = isDrawMarker;
	}

	@Override
	public void proectMarker(int markerX, int markerY, int screenWidth, int screenHeight, boolean isUpdate) {
		if (markerX < getX(0) || markerX > getX(size() - 1)) {
			isDrawMarker = false;
			return;
		}

		if (!isUpdate) {
			this.markerIndex = findIndex(markerX);
			this.markerAlfa = (markerX - getX(markerIndex - 1)) / (double) (getX(markerIndex) - getX(markerIndex - 1));

			this.markerX = markerX;
			this.markerY = getY(markerIndex - 1) + markerAlfa * (getY(markerIndex) - getY(markerIndex - 1));

			this.realMarkerX = getRealX(markerIndex - 1) + markerAlfa * (getRealX(markerIndex) - getRealX(markerIndex - 1));
			this.realMarkerY = getRealY(markerIndex - 1) + markerAlfa * (getRealY(markerIndex) - getRealY(markerIndex - 1));
		} else {
			if (markerIndex <= 0) {
				return;
			}

			this.markerX = getX(markerIndex - 1) + markerAlfa * (getX(markerIndex) - getX(markerIndex - 1));
			this.markerY = getY(markerIndex - 1) + markerAlfa * (getY(markerIndex) - getY(markerIndex - 1));
		}

		isDrawMarker = true;
	}

	public int findIndex(double x) {
		int MAX_NUMBER_OF_RECURSION = 20, CUR_NUMBER = 0;
		int n = size();
		int i = 0, i1 = 0, i2 = n - 1;
		if (x < getX(0)) {
			return 0;
		}

		if (x > getX(n - 1)) {
			return n - 1;
		}

		while (true) {
			i = (int) Math.ceil(i1 + (i2 - i1) / 2.0);
			if (x >= getX(i1) && x < getX(i)) {
				i2 = i;
			} else if (x >= getX(i) && x <= getX(i2)) {
				i1 = i;
			}

			if (CUR_NUMBER >= MAX_NUMBER_OF_RECURSION) {
				return 0;
			}

			if (i2 - i1 == 1) {
				return i2;
			}

			CUR_NUMBER++;
		}
	}
}
