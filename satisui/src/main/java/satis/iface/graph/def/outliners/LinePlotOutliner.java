package satis.iface.graph.def.outliners;

import java.awt.geom.GeneralPath;

import satis.iface.graph.PlotModel;

public class LinePlotOutliner implements PlotOutliner {
	@Override
	public void start(GeneralPath path, PlotModel model) {
	}

	@Override
	public void addPoints(GeneralPath path, PlotModel model, int index) {
		path.lineTo(model.getX(index), model.getY(index));
	}

	@Override
	public void end(GeneralPath path, PlotModel model) {
	}
}
