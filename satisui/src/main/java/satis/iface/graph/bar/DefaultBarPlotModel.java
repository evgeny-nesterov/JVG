package satis.iface.graph.bar;

import java.awt.Color;
import java.awt.Dimension;

import satis.iface.graph.Bounds;

public class DefaultBarPlotModel extends BarPlotModel {
	private int size = 0;

	private double[] x, y, X, Y;

	private Color[] colors;

	public DefaultBarPlotModel() {
		this(new double[0], new double[0], null);
	}

	public DefaultBarPlotModel(double[] X, double[] Y) {
		setData(X, Y, null);
	}

	public DefaultBarPlotModel(double[] X, double[] Y, Color[] colors) {
		setData(X, Y, colors);
	}

	public void setData(double[] X, double[] Y, Color[] colors) {
		boolean isNeedPack = (this.X == null) || (this.X.length == 0);

		if (X != null && Y != null) {
			size = Math.min(X.length, Y.length);
		}

		this.X = X;
		if (x == null || x.length != size) {
			this.x = new double[size];
		}

		this.Y = Y;
		if (y == null || y.length != size) {
			this.y = new double[size];
		}

		this.colors = colors;

		if (isNeedPack) {
			pack(true, true);
		}
	}

	@Override
	public void compile(Dimension s, Bounds bound) {
		for (int i = 0; i < size; i++) {
			x[i] = (s.width - 1) * (X[i] - bound.x) / bound.w;
			y[i] = (s.height - 1) * (1 - (Y[i] - bound.y) / bound.h);
		}
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public double getRealX(int index) {
		return X[index];
	}

	@Override
	public double getRealY(int index) {
		return Y[index];
	}

	@Override
	public int getX(int index) {
		return (int) x[index];
	}

	@Override
	public int getY(int index) {
		return (int) y[index];
	}

	public double[] xArray() {
		return X;
	}

	public double[] yArray() {
		return Y;
	}

	@Override
	public Color getColor(int index) {
		if (colors != null && index < colors.length) {
			return colors[index];
		} else {
			return null;
		}
	}
}
