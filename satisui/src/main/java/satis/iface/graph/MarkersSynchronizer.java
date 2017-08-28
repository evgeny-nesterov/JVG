package satis.iface.graph;

import java.util.ArrayList;

public class MarkersSynchronizer {
	private ArrayList<DinamicGraphArea> graphs = new ArrayList<DinamicGraphArea>();

	private boolean doAction = false;

	public void add(final DinamicGraphArea graph) {
		if (graphs.contains(graph)) {
			return;
		}

		graphs.add(graph);
		graph.addMarkerListener(new MarkerListener() {
			@Override
			public void markersHided() {
				hideMarkers();
			}

			@Override
			public void markersMoved() {
				DinamicPlot plot = (DinamicPlot) graph.getGroup(0).getPlot(0);
				setMarkers(plot.getRealMarkerX());
			}
		});
	}

	public void setMarkers(double realX) {
		if (!doAction) {
			try {
				doAction = true;
				int size = graphs.size();
				for (int i = 0; i < size; i++) {
					DinamicGraphArea g = graphs.get(i);
					if (g.getWidth() > 0) {
						g.setMarkers(realX);
						g.repaint();
					}
				}
			} finally {
				doAction = false;
			}
		}
	}

	public void hideMarkers() {
		if (!doAction) {
			try {
				doAction = true;
				int size = graphs.size();
				for (int i = 0; i < size; i++) {
					DinamicGraphArea graph = graphs.get(i);
					graph.hideMarkers();
				}
			} finally {
				doAction = false;
			}
		}
	}
}
