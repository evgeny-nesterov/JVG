package satis.iface.graph;

public class DinamicPlot extends Plot {
	private static final long serialVersionUID = 1L;

	protected int markerIndex = 1;

	protected double markerX, markerY, markerAlfa;

	protected double realMarkerX, realMarkerY;

	public DinamicPlot() {
	}

	public int getMarkerIndex() {
		return markerIndex;
	}

	public double getMarkerX() {
		return markerX;
	}

	public double getMarkerY() {
		return markerY;
	}

	public double getRealMarkerX() {
		return realMarkerX;
	}

	public double getRealMarkerY() {
		return realMarkerY;
	}

	public double getMarkerAlfa() {
		return markerAlfa;
	}

	protected boolean isDrawMarker = false;

	public boolean isDrawMarker() {
		return isDrawMarker;
	}

	public void setDrawMarker(boolean isDrawMarker) {
		this.isDrawMarker = isDrawMarker;
	}

	public boolean isMarkerOnPoint() {
		return markerAlfa == 1;
	}

	public boolean setMarker(int markerIndex) {
		PlotModel model = getModel();
		if (markerIndex < 0 || markerIndex > model.size() - 1) {
			isDrawMarker = false;
			return false;
		}

		this.markerIndex = markerIndex;
		markerAlfa = 1;

		markerX = model.getX(markerIndex);
		markerY = model.getY(markerIndex);

		realMarkerX = model.getRealX(markerIndex);
		realMarkerY = model.getRealY(markerIndex);

		isDrawMarker = true;
		return true;
	}

	public boolean setMarker(Group group, double markerX, double markerY) {
		PlotModel model = getModel();
		if (markerX < model.getX(0) || markerX > model.getX(model.size() - 1)) {
			isDrawMarker = false;
			return false;
		}

		markerIndex = GraphUtil.findIndex(model, markerX);
		if (markerIndex == 0) {
			return setMarker(0);
		}

		double x0 = model.getX(markerIndex - 1);
		double x1 = model.getX(markerIndex);
		if (x0 != x1) {
			markerAlfa = (markerX - x0) / (x1 - x0);
			this.markerX = markerX;

			double y0 = model.getY(markerIndex - 1);
			double y1 = model.getY(markerIndex);
			this.markerY = y0 + markerAlfa * (y1 - y0);

			x0 = model.getRealX(markerIndex - 1);
			x1 = model.getRealX(markerIndex);
			realMarkerX = x0 + markerAlfa * (x1 - x0);

			y0 = model.getRealY(markerIndex - 1);
			y1 = model.getRealY(markerIndex);
			realMarkerY = y0 + markerAlfa * (y1 - y0);

			isDrawMarker = true;
			return true;
		} else {
			return setMarker(markerIndex);
		}
	}

	public boolean setMarkerByReal(Group group, double realMarkerX) {
		PlotModel model = getModel();
		if (realMarkerX < model.getRealX(0) || realMarkerX > model.getRealX(model.size() - 1)) {
			isDrawMarker = false;
			return false;
		}

		markerIndex = GraphUtil.findIndexByReal(model, realMarkerX);
		if (markerIndex == 0) {
			return setMarker(0);
		}

		double x0 = model.getRealX(markerIndex - 1);
		double x1 = model.getRealX(markerIndex);
		if (x0 != x1) {
			markerAlfa = (realMarkerX - x0) / (x1 - x0);

			this.realMarkerX = realMarkerX;

			double y0 = model.getRealY(markerIndex - 1);
			double y1 = model.getRealY(markerIndex);
			realMarkerY = y0 + markerAlfa * (y1 - y0);

			x0 = model.getX(markerIndex - 1);
			x1 = model.getX(markerIndex);
			markerX = x0 + markerAlfa * (x1 - x0);

			y0 = model.getY(markerIndex - 1);
			y1 = model.getY(markerIndex);
			markerY = y0 + markerAlfa * (y1 - y0);

			isDrawMarker = true;
			return true;
		} else {
			return setMarker(markerIndex);
		}
	}

	public void updateMarker() {
		if (markerIndex <= 0 && !isDrawMarker) {
			return;
		}

		PlotModel model = getModel();
		if (markerIndex > 0 && markerIndex < model.size()) {
			double x0 = model.getX(markerIndex - 1);
			double x1 = model.getX(markerIndex);
			markerX = x0 + markerAlfa * (x1 - x0);

			double y0 = model.getY(markerIndex - 1);
			double y1 = model.getY(markerIndex);
			markerY = y0 + markerAlfa * (y1 - y0);

			y0 = model.getRealY(markerIndex - 1);
			y1 = model.getRealY(markerIndex);
			realMarkerY = y0 + markerAlfa * (y1 - y0);
		} else {
			isDrawMarker = false;
		}
	}
}
