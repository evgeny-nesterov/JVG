package ru.nest.toi;

import java.awt.geom.AffineTransform;

import ru.nest.toi.TOIPane.ControlFilter;

public class TOIPaintContext {
	public final static int MIN_LAYER = -1;

	public final static int MAX_LAYER = 1;

	public final static int BOTTOM_LAYER = -1;

	public final static int MAIN_LAYER = 0;

	public final static int TOP_LAYER = 1;

	private TOIController controller;

	private TOIColor colorRenderer = new TOIDefaultColor();

	private AffineTransform transform = new AffineTransform();

	private ControlFilter controlFilter;

	private TOIPane pane;

	private int layer = 0;

	public TOIController getController() {
		return controller;
	}

	public void setController(TOIController controller) {
		this.controller = controller;
	}

	public TOIColor getColorRenderer() {
		return colorRenderer;
	}

	public void setColorRenderer(TOIColor colorRenderer) {
		this.colorRenderer = colorRenderer;
	}

	public AffineTransform getTransform() {
		return transform;
	}

	public void setTransform(AffineTransform transform) {
		this.transform = transform;
	}

	public TOIPane getPane() {
		return pane;
	}

	public void setPane(TOIPane pane) {
		this.pane = pane;
	}

	public ControlFilter getControlFilter() {
		return controlFilter;
	}

	public void setControlFilter(ControlFilter controlFilter) {
		this.controlFilter = controlFilter;
	}

	public int getLayer() {
		return layer;
	}

	public void setLayer(int layer) {
		this.layer = layer;
	}
}
