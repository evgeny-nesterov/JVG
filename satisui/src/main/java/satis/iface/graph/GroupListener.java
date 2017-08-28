package satis.iface.graph;

public interface GroupListener {
	public void plotAdded(Group group, Plot plot);

	public void plotRemoved(Group group, Plot plot);

	public void plotDataChanged(Group group, PlotModel model);

	public void boundsChanged(Group group);

	public void innerBoundsChanged(Group group);
}
