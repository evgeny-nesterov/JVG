package satis.iface.graph.paintobjects;

import satis.iface.graph.Group;

public abstract class Poly extends AbstractPaintObject {
	int count;

	double[] X, Y;

	int[] x, y;

	public Poly(double[] X, double[] Y) {
		this.X = X;
		this.Y = Y;
		count = Math.min(X.length, Y.length);
		x = new int[count];
		y = new int[count];
	}

	@Override
	public void compile(Group group) {
		for (int i = 0; i < count; i++) {
			x[i] = (int) group.modelToViewX(X[i]);
			y[i] = (int) group.modelToViewY(Y[i]);
		}
	}
}
