package satis.iface.graph;

import java.awt.Dimension;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import satis.iface.graph.grid.Grid;
import satis.iface.graph.paintobjects.PaintObject;

public class Group implements Serializable, PlotModelListener {
	private static final long serialVersionUID = 1L;

	public Group() {
	}

	private Grid gridHor = null;

	public Grid getGridHor() {
		return gridHor;
	}

	public void setGridY(Grid gridHor) {
		this.gridHor = gridHor;
	}

	private Grid gridVer = null;

	public Grid getGridVer() {
		return gridVer;
	}

	public void setGridX(Grid gridVer) {
		this.gridVer = gridVer;
	}

	protected List<Plot> plots = new ArrayList<Plot>();

	public Iterator<Plot> getPlots() {
		return plots.iterator();
	}

	public int getPlotsCount() {
		return plots.size();
	}

	public void addPlot(Plot plot) {
		addPlot(plots.size(), plot);
	}

	public void addPlot(int index, Plot plot) {
		synchronized (plots) {
			if (plot == null || plots.contains(plot)) {
				return;
			}

			plots.add(index, plot);
			plot.getModel().addModelListener(this);
			pack(true, true);
			firePlotAdded(plot);
		}
	}

	public void removePlot(int index) {
		synchronized (plots) {
			Plot plot = plots.get(index);
			plots.remove(index);
			plot.getModel().removeModelListener(this);
			pack(true, true);
			firePlotRemoved(plot);
		}
	}

	public void removePlot(Plot plot) {
		synchronized (plots) {
			plots.remove(plot);
			plot.getModel().removeModelListener(this);
			pack(true, true);
			firePlotRemoved(plot);
		}
	}

	public Plot getPlot(int index) {
		return plots.get(index);
	}

	public int getPlotIndex(Plot plot) {
		if (plot != null) {
			return plots.indexOf(plot);
		} else {
			return -1;
		}
	}

	protected List<PaintObject> paintObjects = new ArrayList<PaintObject>();

	public int getPaintObjectCount() {
		return paintObjects.size();
	}

	public PaintObject getPaintObject(int index) {
		return paintObjects.get(index);
	}

	public Iterator<PaintObject> getPaintObjects() {
		return paintObjects.iterator();
	}

	public void addPaintObject(PaintObject object) {
		if (object == null || paintObjects.contains(object)) {
			return;
		}

		paintObjects.add(object);
	}

	public void addPaintObject(int index, PaintObject object) {
		if (object == null || paintObjects.contains(object)) {
			return;
		}

		paintObjects.add(index, object);
	}

	public void removePaintObject(PaintObject object) {
		paintObjects.remove(object);
	}

	public void removePaintObject(int index) {
		paintObjects.remove(index);
	}

	public void removeAllPaintObject() {
		paintObjects.clear();
	}

	private Dimension screenSize;

	public Dimension getScreenSize() {
		return screenSize;
	}

	private Bounds bounds = new Bounds();

	public Bounds getBounds() {
		return bounds;
	}

	public void setBounds(Bounds bounds) {
		setBounds(bounds.x, bounds.y, bounds.w, bounds.h);
	}

	public void setBounds(double x, double y, double w, double h) {
		if ((bounds.x != x && x != Double.MAX_VALUE) || (bounds.y != y && y != Double.MAX_VALUE) || (bounds.w != w && w != Double.MAX_VALUE) || (bounds.h != h && h != Double.MAX_VALUE)) {
			if (x != Double.MAX_VALUE) {
				bounds.x = x;
			}

			if (y != Double.MAX_VALUE && !Double.isNaN(y)) {
				bounds.y = y;
			}

			if (w != Double.MAX_VALUE) {
				bounds.w = w;
			}

			if (h != Double.MAX_VALUE && !Double.isNaN(h)) {
				bounds.h = h;
			}

			fireBoundsChanged();
		}
	}

	private Bounds innerBounds = new Bounds();

	public Bounds getInnerBounds() {
		return innerBounds;
	}

	public void setInnerBounds(Bounds innerBound) {
		setInnerBounds(innerBound.x, innerBound.y, innerBound.w, innerBound.h);
	}

	private boolean isBoundsInitiated = false;

	/**
	 * If parameter equals Double.MAX_VALUE then ignore.
	 */
	public void setInnerBounds(double x, double y, double w, double h) {
		if ((innerBounds.x != x && x != Double.MAX_VALUE) || (innerBounds.y != y && y != Double.MAX_VALUE) || (innerBounds.w != w && w != Double.MAX_VALUE) || (innerBounds.h != h && h != Double.MAX_VALUE)) {
			if (x != Double.MAX_VALUE) {
				innerBounds.x = x;
			}

			if (y != Double.MAX_VALUE && !Double.isNaN(y)) {
				innerBounds.y = y;
			}

			if (w != Double.MAX_VALUE) {
				innerBounds.w = w;
			}

			if (h != Double.MAX_VALUE && !Double.isNaN(h)) {
				innerBounds.h = h;
			}

			isBoundsInitiated = true;
			fireInnerBoundsChanged();
		}
	}

	public void setActualBounds(boolean isX, boolean isY) {
		double x, y, w, h;

		if (isX) {
			x = bounds.x;
			w = bounds.w;
		} else {
			x = innerBounds.x;
			w = innerBounds.w;
		}

		if (isY) {
			y = bounds.y;
			h = bounds.h;
		} else {
			y = innerBounds.y;
			h = innerBounds.h;
		}

		setInnerBounds(x, y, w, h);
	}

	public void setActualBounds() {
		setActualBounds(true, true);
	}

	public void compile(Dimension screenSize) {
		if (!isBoundsInitiated) {
			setActualBounds();
		}

		synchronized (plots) {
			this.screenSize = screenSize;
			for (Plot plot : plots) {
				plot.getModel().compile(screenSize, innerBounds);
			}
		}

		for (PaintObject o : paintObjects) {
			o.compile(this);
		}

		if (gridHor != null) {
			gridHor.update(this);
		}

		if (gridVer != null) {
			gridVer.update(this);
		}
	}

	public void pack(boolean packX, boolean packY) {
		packY |= Double.isNaN(bounds.y) || Double.isNaN(bounds.h) || bounds.h == 0;

		double minX = Double.MAX_VALUE;
		double maxX = -Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double maxY = -Double.MAX_VALUE;

		for (Plot plot : plots) {
			Bounds b = plot.getModel().getPrefferedBounds();

			if (packX) {
				minX = Math.min(minX, b.x);
				maxX = Math.max(maxX, b.x + b.w);
			}

			if (packY) {
				minY = Math.min(minY, b.y);
				maxY = Math.max(maxY, b.y + b.h);
			}
		}

		double x = bounds.x, y = bounds.y, w = bounds.w, h = bounds.h;
		if (packX) {
			x = minX;
			w = maxX - minX;
		}
		if (packY) {
			y = minY;
			h = maxY - minY;
		}
		setBounds(x, y, w, h);
	}

	public void correctInnerBounds(boolean isX, boolean isY) {
		if (isX || isY) {
			if (isX) {
				if (innerBounds.x < bounds.x) {
					innerBounds.x = bounds.x;
				}

				if (innerBounds.x + innerBounds.w < bounds.x + bounds.w) {
					innerBounds.w = bounds.x + bounds.w - innerBounds.x;
				}
			}

			if (isY) {
				if (innerBounds.y < bounds.y) {
					innerBounds.y = bounds.y;
				}

				if (innerBounds.y + innerBounds.h < bounds.y + bounds.h) {
					innerBounds.h = bounds.y + bounds.h - innerBounds.y;
				}
			}
			setInnerBounds(innerBounds);
		}
	}

	/**
	 * @return pixels
	 */
	public double modelToViewX(double x) {
		if (screenSize != null) {
			return (screenSize.width - 1) * (x - innerBounds.x) / innerBounds.w;
		} else {
			return 0;
		}
	}

	/**
	 * @return pixels
	 */
	public double modelToViewY(double y) {
		if (screenSize != null) {
			return (screenSize.height - 1) * (1 - (y - innerBounds.y) / innerBounds.h);
		} else {
			return 0;
		}
	}

	public double viewToModelX(double x) {
		if (screenSize != null) {
			return innerBounds.x + x * innerBounds.w / (screenSize.width - 1);
		} else {
			return 0;
		}
	}

	public double viewToModelY(double y) {
		if (screenSize != null) {
			return innerBounds.y + innerBounds.h * (1 - y / (screenSize.height - 1));
		} else {
			return 0;
		}
	}

	// --- Group listener ---
	private List<GroupListener> groupListeners = new ArrayList<GroupListener>();

	public void addGroupListener(GroupListener listener) {
		if (listener != null) {
			synchronized (groupListeners) {
				groupListeners.add(listener);
			}
		}
	}

	public void removeGroupListener(GroupListener listener) {
		if (listener != null) {
			synchronized (groupListeners) {
				groupListeners.remove(listener);
			}
		}
	}

	public void firePlotAdded(Plot plot) {
		synchronized (groupListeners) {
			for (GroupListener l : groupListeners) {
				l.plotAdded(this, plot);
			}
		}
	}

	public void firePlotRemoved(Plot plot) {
		synchronized (groupListeners) {
			for (GroupListener l : groupListeners) {
				l.plotRemoved(this, plot);
			}
		}
	}

	public void firePlotDataChanged(PlotModel model) {
		synchronized (groupListeners) {
			for (GroupListener l : groupListeners) {
				l.plotDataChanged(this, model);
			}
		}
	}

	public void fireBoundsChanged() {
		synchronized (groupListeners) {
			for (GroupListener l : groupListeners) {
				l.boundsChanged(this);
			}
		}
	}

	public void fireInnerBoundsChanged() {
		synchronized (groupListeners) {
			for (GroupListener l : groupListeners) {
				l.innerBoundsChanged(this);
			}
		}
	}

	// --- Plot listener ---
	@Override
	public void plotDataChanged(PlotModel model) {
		firePlotDataChanged(model);
	}

	private boolean autorescaleX = false;

	private boolean autorescaleY = false;

	public void setAutorescale(boolean autorescaleX, boolean autorescaleY) {
		this.autorescaleX = autorescaleX;
		this.autorescaleY = autorescaleY;
	}

	public boolean isAutorescaleX() {
		return autorescaleX;
	}

	public boolean isAutorescaleY() {
		return autorescaleY;
	}

	@Override
	public void plotBoundsChanged(PlotModel model) {
		if (autorescaleX || autorescaleY) {
			Bounds b = model.getPrefferedBounds();
			Bounds ib = getInnerBounds();

			if (autorescaleX) {
				ib.x = b.x;
				ib.w = b.w;
			}

			if (autorescaleY) {
				ib.y = b.h;
				ib.h = b.h;
			}

			setInnerBounds(ib);
		}
	}
}
