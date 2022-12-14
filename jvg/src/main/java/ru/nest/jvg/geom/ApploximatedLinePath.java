package ru.nest.jvg.geom;

import java.util.ArrayList;
import java.util.List;

public class ApploximatedLinePath extends MutableGeneralPath {
	public ApploximatedLinePath() {
	}

	@Override
	public boolean moveTo(Double x, Double y) {
		int size = X.size();
		if (size > 0) {
			lineTo(X.get(size - 1), Y.get(size - 1));
			X.clear();
			Y.clear();
			X.add(x);
			Y.add(y);
		}
		return super.moveTo(x, y);
	}

	private List<Double> X = new ArrayList<>();

	private List<Double> Y = new ArrayList<>();

	private double[] result = new double[4];

	@Override
	public void lineTo(Double x, Double y) {
		if (numCoords > 0 && pointCoords[numCoords - 2] == x && pointCoords[numCoords - 1] == y) {
			return;
		}

		if (numCoords > 4) {
			if (pointCoords[numCoords - 2] == x && pointCoords[numCoords - 4] == x && (pointCoords[numCoords - 1] - pointCoords[numCoords - 3]) * (y - pointCoords[numCoords - 1]) > 0) {
				pointCoords[numCoords - 1] = y;
				return;
			}

			if (pointCoords[numCoords - 1] == y && pointCoords[numCoords - 3] == y && (pointCoords[numCoords - 2] - pointCoords[numCoords - 4]) * (x - pointCoords[numCoords - 2]) > 0) {
				pointCoords[numCoords - 2] = x;
				return;
			}
		}

		X.add(x);
		Y.add(y);
		int size = X.size();
		if (numCoords > 0 && size > 2) {
			double disp = interpolate(X, Y, size, result);
			if (disp > 2) {
				X.clear();
				Y.clear();
				X.add(x);
				Y.add(y);
			} else {
				double dx1 = pointCoords[numCoords - 2] - pointCoords[numCoords - 4];
				double dx2 = x - pointCoords[numCoords - 2];
				double dy1 = pointCoords[numCoords - 1] - pointCoords[numCoords - 3];
				double dy2 = y - pointCoords[numCoords - 1];
				double a1 = Math.sqrt(dx1 * dx1 + dy1 * dy1);
				double a2 = Math.sqrt(dx2 * dx2 + dy2 * dy2);
				double cos = (dx1 * dx2 + dy1 * dy2) / (a1 * a2);
				double alfa = Math.acos(cos);
				if (Math.abs(alfa) > Math.PI / 3.0) {
					X.clear();
					Y.clear();
					X.add(x);
					Y.add(y);
				} else {
					pointCoords[numCoords - 2] = x;
					pointCoords[numCoords - 1] = y;
					return;
				}
			}
		}

		super.lineTo(x, y);
	}

	public static double interpolate(List<Double> x, List<Double> y, int length, double[] result) {
		// sum[(aX+b - Y)^2] -> min, i=1,N

		// sum[aX+b] = sum[Y]
		// sum[(aX+b)X] = sum[XY]

		// a sum[X] + b N = sum[Y]
		// a sum[X^2] + b sum[X] = sum[XY]

		// D = sum[X]^2 - N sum[X^2]
		// if D = 0 then x(y) = sum[X] / N else
		// a = (sum[Y] sum[X] - N sum[XY]) / D
		// b = (sum[X] sum[XY] - sum[Y] sum[X^2]) / D

		int N = length;
		double Sx = 0, Sx2 = 0, Sy = 0, Sxy = 0;
		for (int i = 0; i < length; i++) {
			Sx += x.get(i);
			Sy += y.get(i);
			Sx2 += x.get(i) * x.get(i);
			Sxy += x.get(i) * y.get(i);
		}
		double D = Sx * Sx - N * Sx2;
		if (D == 0) {
			double ax = Math.sqrt(Sx2 / N);
			result[0] = ax;
			result[1] = y.get(0);
			result[2] = ax;
			result[3] = y.get(length - 1);

			double disp = 0;
			for (int i = 0; i < length; i++) {
				double delta = ax - x.get(i);
				disp += delta * delta;
			}
			disp = Math.sqrt(disp / N);
			return disp;
		} else {
			double a = (Sy * Sx - N * Sxy) / D;
			double b = (Sx * Sxy - Sy * Sx2) / D;

			result[0] = a * x.get(0) + b;
			result[1] = y.get(0);
			result[2] = a * x.get(length - 1) + b;
			result[3] = y.get(length - 1);

			double disp = 0;
			for (int i = 0; i < length; i++) {
				double delta = a * x.get(i) + b - y.get(i);
				disp += delta * delta;
			}
			disp = Math.sqrt(disp / N);
			return disp;
		}
	}

	@Override
	public synchronized void quadTo(Double x1, Double y1, Double x2, Double y2) {
		throw new RuntimeException("quadTo method is not supported");
	}

	@Override
	public synchronized void curveTo(Double x1, Double y1, Double x2, Double y2, Double x3, Double y3) {
		throw new RuntimeException("curveTo method is not supported");
	}
}
