package satis.iface.graph;

import java.util.ArrayList;
import java.util.List;

public class GraphGroup implements GraphListener {
	public GraphGroup() {
	}

	private List<GraphContainer> graphs = new ArrayList<GraphContainer>();

	public void add(GraphContainer graph) {
		if (graph != null) {
			graphs.add(graph);
			graph.setGroup(this);

			DinamicGraphArea g = (DinamicGraphArea) graph.getObject(GraphContainer.GRAPHICS);
			g.addGraphListener(this);
		}
	}

	public void remove(GraphContainer graph) {
		if (graph != null) {
			graphs.remove(graph);
			graph.setGroup(null);

			DinamicGraphArea g = (DinamicGraphArea) graph.getObject(GraphContainer.GRAPHICS);
			g.removeGraphListener(this);
		}
	}

	public int getCount() {
		return graphs.size();
	}

	public GraphContainer get(int index) {
		return graphs.get(index);
	}

	@Override
	public void graphResized(StaticGraphArea graph) {
	}

	@Override
	public void groupRescaled(StaticGraphArea graph, Group group) {
		if (group != null) {
			Bounds b = group.getInnerBounds();
			for (GraphContainer gc : graphs) {
				DinamicGraphArea g = (DinamicGraphArea) gc.getObject(GraphContainer.GRAPHICS);
				if (g != graph) {
					g.setInnerBounds(b.x, b.y, b.w, b.h);
				}
			}
		}
	}

	@Override
	public void groupAdded(StaticGraphArea graph, Group group) {
	}

	@Override
	public void groupRemoved(StaticGraphArea graph, Group group) {
	}
}
