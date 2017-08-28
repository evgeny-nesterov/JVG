package satis.iface.graph;

import java.awt.Dimension;

public interface PlotModel {
	public int size();

	public int getX(int index);

	public int getY(int index);

	public double getRealX(int index);

	public double getRealY(int index);

	public Bounds getPrefferedBounds();

	public void compile(Dimension s, Bounds bound);

	public void addModelListener(PlotModelListener listener);

	public void removeModelListener(PlotModelListener listener);

	public void fireDataChanged();

	public void fireBoundsChanged();
}
