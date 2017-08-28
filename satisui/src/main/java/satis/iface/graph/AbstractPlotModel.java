package satis.iface.graph;

import java.util.ArrayList;

public abstract class AbstractPlotModel implements PlotModel {
	// --- Bounds ---
	private Bounds prefferedBounds = new Bounds();

	public void setPrefferedBounds(double x, double y, double w, double h) {
		if (prefferedBounds.x == x && prefferedBounds.y == y && prefferedBounds.w == w && prefferedBounds.h == h) {
			return;
		}

		prefferedBounds.x = x;
		prefferedBounds.y = y;
		prefferedBounds.w = w;
		prefferedBounds.h = h;
	}

	@Override
	public Bounds getPrefferedBounds() {
		return prefferedBounds;
	}

	public void pack(boolean packX, boolean packY) {
		double minX = Double.MAX_VALUE;
		double maxX = -Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double maxY = -Double.MAX_VALUE;

		for (int i = 0; i < size(); i++) {
			if (packX) {
				minX = Math.min(minX, getRealX(i));
				maxX = Math.max(maxX, getRealX(i));
			}

			if (packY) {
				minY = Math.min(minY, getRealY(i));
				maxY = Math.max(maxY, getRealY(i));
			}
		}

		if (packX) {
			prefferedBounds.x = minX;
			prefferedBounds.w = maxX - minX;
		}

		if (packY) {
			prefferedBounds.y = minY;
			prefferedBounds.h = maxY - minY;
		}

		if (prefferedBounds.w == 0) {
			prefferedBounds.x -= 1;
			prefferedBounds.w += 2;
		}

		if (prefferedBounds.h == 0) {
			prefferedBounds.y -= 1;
			prefferedBounds.h += 2;
		}
	}

	// --- Listenter ---
	private ArrayList<PlotModelListener> listeners = new ArrayList<PlotModelListener>();

	@Override
	public void addModelListener(PlotModelListener listener) {
		if (listener != null) {
			synchronized (listeners) {
				listeners.add(listener);
			}
		}
	}

	@Override
	public void removeModelListener(PlotModelListener listener) {
		if (listener != null) {
			synchronized (listeners) {
				listeners.remove(listener);
			}
		}
	}

	@Override
	public void fireDataChanged() {
		synchronized (listeners) {
			for (PlotModelListener listener : listeners) {
				try {
					listener.plotDataChanged(this);
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
		}
	}

	@Override
	public void fireBoundsChanged() {
		synchronized (listeners) {
			for (PlotModelListener listener : listeners) {
				try {
					listener.plotBoundsChanged(this);
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
		}
	}

	public String getPlotDimension() {
		return null;
	}
}
