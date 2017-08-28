package satis.iface.graph.def;

import java.awt.Color;
import java.awt.Dimension;

import satis.iface.graph.AbstractPlotModel;
import satis.iface.graph.Bounds;

public class DefaultPlotModel extends AbstractPlotModel {
	private int size = 0;

	public DefaultPlotModel() {
		this(new double[0], new double[0]);
	}

	public DefaultPlotModel(Object X, Object Y) {
		setData(convert(X), convert(Y));
	}

	private double[] convert(Object array) {
		double[] double_array = null;
		if (array instanceof double[]) {
			double_array = (double[]) array;
		} else if (array instanceof int[]) {
			int[] int_array = (int[]) array;
			double_array = new double[int_array.length];
			for (int i = 0; i < int_array.length; i++) {
				double_array[i] = int_array[i];
			}
		} else if (array instanceof long[]) {
			long[] long_array = (long[]) array;
			double_array = new double[long_array.length];
			for (int i = 0; i < long_array.length; i++) {
				double_array[i] = long_array[i];
			}
		} else if (array instanceof short[]) {
			short[] short_array = (short[]) array;
			double_array = new double[short_array.length];
			for (int i = 0; i < short_array.length; i++) {
				double_array[i] = short_array[i];
			}
		} else if (array instanceof byte[]) {
			byte[] byte_array = (byte[]) array;
			double_array = new double[byte_array.length];
			for (int i = 0; i < byte_array.length; i++) {
				double_array[i] = byte_array[i];
			}
		} else if (array instanceof String[]) {
			String[] string_array = (String[]) array;
			double_array = new double[string_array.length];
			for (int i = 0; i < string_array.length; i++) {
				try {
					double_array[i] = Double.parseDouble(string_array[i]);
				} catch (Exception exc) {
					double_array[i] = -Double.MAX_VALUE;
				}
			}
		}

		if (double_array == null) {
			double_array = new double[0];
		}
		return double_array;
	}

	public DefaultPlotModel(double[] X, double[] Y) {
		setData(X, Y);
	}

	private double[] x;

	public double[] getX() {
		return x;
	}

	private double[] y;

	public double[] getY() {
		return y;
	}

	private double[] X;

	public double[] getRealX() {
		return X;
	}

	private double[] Y;

	public double[] getRealY() {
		return Y;
	}

	public void setData(Object X, Object Y) {
		setData(convert(X), convert(Y));
	}

	public void setData(double[] X, double[] Y) {
		int size = 0;
		if (X != null && Y != null) {
			size = Math.min(X.length, Y.length);
		}
		setData(X, Y, size);
	}

	public void setData(double[] X, double[] Y, int size) {
		this.size = size;
		boolean isNeedPack = (this.X == null) || (this.X.length == 0);

		this.X = X;
		if (x == null || x.length != size) {
			this.x = new double[size];
		}

		this.Y = Y;
		if (y == null || y.length != size) {
			this.y = new double[size];
		}

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
}
