package satis.iface.graph;

public interface GraphListener {
	public void graphResized(StaticGraphArea graph);

	public void groupRescaled(StaticGraphArea graph, Group group);

	public void groupAdded(StaticGraphArea graph, Group group);

	public void groupRemoved(StaticGraphArea graph, Group group);
}
