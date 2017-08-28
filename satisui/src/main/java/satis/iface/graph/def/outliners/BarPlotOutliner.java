package satis.iface.graph.def.outliners;

import java.awt.geom.GeneralPath;

import satis.iface.graph.PlotModel;

public class BarPlotOutliner implements PlotOutliner {
	public BarPlotOutliner() {
		this(0);
	}

	public BarPlotOutliner(int delta) {
		this(delta, 0.5);
	}

	public BarPlotOutliner(int delta, double shiftKoef) {
		setDelta(delta);
		setShiftKoef(shiftKoef);
	}

	private int delta = 0;

	public void setDelta(int delta) {
		if (delta < 0) {
			delta = 0;
		}
		this.delta = delta;
	}

	public int getDelta() {
		return delta;
	}

	private int len;

	private double last;

	private double shiftKoef = 0.5;

	@Override
	public void start(GeneralPath path, PlotModel model) {
		last = shiftKoef * model.getX(0) + (1 - shiftKoef) * model.getX(1);
		path.lineTo(last - delta, model.getY(0));

		if (delta > 0) {
			path.lineTo(last - delta, Short.MAX_VALUE);
			path.lineTo(last + delta, Short.MAX_VALUE);
		}

		len = model.size();
	}

	@Override
	public void addPoints(GeneralPath path, PlotModel model, int index) {
		if (index > 0 && index < len - 1) {
			int y = model.getY(index);

			path.lineTo(last + delta, y);
			last = shiftKoef * model.getX(index) + (1 - shiftKoef) * model.getX(index + 1);
			path.lineTo(last - delta, y);

			if (delta > 0) {
				path.lineTo(last - delta, Short.MAX_VALUE);
				path.lineTo(last + delta, Short.MAX_VALUE);
			}
		}
	}

	@Override
	public void end(GeneralPath path, PlotModel model) {
		path.lineTo(shiftKoef * model.getX(len - 2) + (1 - shiftKoef) * model.getX(len - 1) + delta, model.getY(len - 1));
		path.lineTo(model.getX(len - 1), model.getY(len - 1));
	}

	public double getShiftKoef() {
		return shiftKoef;
	}

	public void setShiftKoef(double shiftKoef) {
		this.shiftKoef = shiftKoef;
	}
}
