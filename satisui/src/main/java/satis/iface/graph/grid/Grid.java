package satis.iface.graph.grid;

import java.awt.Graphics;
import java.io.Serializable;

import satis.iface.graph.Bounds;
import satis.iface.graph.Group;

public abstract class Grid implements Serializable {
	private static final long serialVersionUID = 1L;

	public final static int Y_AXIS = 0;

	public final static int X_AXIS = 1;

	private boolean isPaintGrid = true;

	public void setPaintGrid(boolean isPaintGrid) {
		this.isPaintGrid = isPaintGrid;
	}

	public boolean isPaintGrid() {
		return isPaintGrid;
	}

	private int orientation = Y_AXIS;

	public void setOrientation(int orientation) {
		this.orientation = orientation;
	}

	public int getOrientation() {
		return orientation;
	}

	private GridRenderer renderer;

	public void setRenderer(GridRenderer renderer) {
		this.renderer = renderer;
	}

	public GridRenderer getRenderer() {
		return renderer;
	}

	private double desirableGridCount = 10;

	public int getGridCount() {
		return (int) desirableGridCount;
	}

	public void setGridCount(int desirableGridCount) {
		this.desirableGridCount = desirableGridCount;
	}

	public void paintGrid(Graphics g, Group group, int width, int height) {
		if (renderer == null || group == null || !isPaintGrid) {
			return;
		}

		Bounds b = group.getInnerBounds();
		int x = 0, y = 0, w = 0, h = 0;
		switch (orientation) {
			case Y_AXIS:
				w = width - 1;
				break;

			case X_AXIS:
				h = height - 1;
				break;
		}

		for (int index = 0; index < getVisibleLinesCount(); index++) {
			double val = getValue(index);
			switch (orientation) {
				case Y_AXIS:
					if (b.getH() != 0) {
						y = (int) ((height - 1) * (1 - (val - b.y) / b.h));
					}
					break;

				case X_AXIS:
					if (b.getW() != 0) {
						x = (int) ((width - 1) * (val - b.x) / b.w);
					}
					break;
			}

			renderer.paintGridLine(this, g, x, y, w, h, index);
		}
	}

	// --- Abstract methods ---
	public abstract void update(Group group);

	public abstract int getVisibleLinesCount();

	public abstract double getValue(int index);
}
