package satis.iface.graph.def.outliners;

import java.awt.geom.GeneralPath;

import satis.iface.graph.PlotModel;

public interface PlotOutliner {
	public void start(GeneralPath path, PlotModel model);

	public void addPoints(GeneralPath path, PlotModel model, int index);

	public void end(GeneralPath path, PlotModel model);
}
