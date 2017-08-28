package satis.iface.graph;

import java.io.Serializable;

import satis.iface.graph.def.DefaultPlotModel;
import satis.iface.graph.def.DefaultPlotRenderer;

public class Plot implements Serializable {
	private static final long serialVersionUID = 1L;

	private static String DEFAULT_NAME = "plot";

	private static int count = 1;

	private static Object lock = new Object();

	public Plot() {
		this(null);
	}

	public Plot(String name) {
		setTitle(name);
	}

	private String title;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		if (title == null) {
			synchronized (lock) {
				title = DEFAULT_NAME + " " + (count++);
			}
		}

		this.title = title;
	}

	@Override
	public String toString() {
		return title;
	}

	private PlotRenderer renderer = new DefaultPlotRenderer();

	public PlotRenderer getPlotRenderer() {
		return renderer;
	}

	public void setRenderer(PlotRenderer renderer) {
		this.renderer = renderer;
	}

	private PlotModel model = null;

	public PlotModel getModel() {
		if (model == null) {
			setModel(new DefaultPlotModel());
		}

		return model;
	}

	public void setModel(PlotModel model) {
		if (model != null) {
			this.model = model;
		}
	}

	private boolean visible = true;

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
}
