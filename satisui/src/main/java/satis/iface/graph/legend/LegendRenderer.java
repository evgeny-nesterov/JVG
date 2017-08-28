package satis.iface.graph.legend;

import java.awt.Component;

public interface LegendRenderer {
	public abstract int getRows();

	public abstract Component getLegendComponent(Legend legend, int row);
}
