package satis.iface.graph;

public class GraphUtil {
	public static int findIndex(PlotModel model, double x) {
		int MAX_NUMBER_OF_RECURSION = 32, CUR_NUMBER = 0;
		int n = model.size();
		int ic = 0, i1 = 0, i2 = n - 1;
		if (x <= model.getX(0)) {
			return 0;
		}

		if (x >= model.getX(n - 1)) {
			return n - 1;
		}

		while (true) {
			ic = (int) Math.ceil(i1 + (i2 - i1) / 2.0);

			double x1 = model.getX(i1);
			double xc = model.getX(ic);
			double x2 = model.getX(i2);

			if (x > x1 && x < xc) {
				i2 = ic;
			} else if (x > xc && x < x2) {
				i1 = ic;
			} else if (x == xc) {
				return ic;
			}

			if (CUR_NUMBER >= MAX_NUMBER_OF_RECURSION) {
				return 0;
			}

			if (i2 - i1 == 1) {
				return i2;
			}

			CUR_NUMBER++;
		}
	}

	public static int findIndexByReal(PlotModel model, double x) {
		int MAX_NUMBER_OF_RECURSION = 32, CUR_NUMBER = 0;
		int n = model.size();
		int ic = 0, i1 = 0, i2 = n - 1;
		if (x <= model.getRealX(0)) {
			return 0;
		}

		if (x >= model.getRealX(n - 1)) {
			return n - 1;
		}

		while (true) {
			ic = (int) Math.ceil(i1 + (i2 - i1) / 2.0);

			double x1 = model.getRealX(i1);
			double xc = model.getRealX(ic);
			double x2 = model.getRealX(i2);

			if (x > x1 && x < xc) {
				i2 = ic;
			} else if (x > xc && x < x2) {
				i1 = ic;
			} else if (x == xc) {
				return ic;
			}

			if (CUR_NUMBER >= MAX_NUMBER_OF_RECURSION) {
				return 0;
			}

			if (i2 - i1 == 1) {
				return i2;
			}

			CUR_NUMBER++;
		}
	}

	public static int findIndex(double[] data, double x) {
		int MAX_NUMBER_OF_RECURSION = 32, CUR_NUMBER = 0;
		int n = data.length;
		int ic = 0, i1 = 0, i2 = n - 1;
		if (x <= data[0]) {
			return 0;
		}

		if (x >= data[n - 1]) {
			return n - 1;
		}

		while (true) {
			ic = (int) Math.ceil(i1 + (i2 - i1) / 2.0);

			double x1 = data[i1];
			double xc = data[ic];
			double x2 = data[i2];

			if (x > x1 && x < xc) {
				i2 = ic;
			} else if (x > xc && x < x2) {
				i1 = ic;
			} else if (x == xc) {
				return ic;
			}

			if (CUR_NUMBER >= MAX_NUMBER_OF_RECURSION) {
				return 0;
			}

			if (i2 - i1 == 1) {
				return i2;
			}

			CUR_NUMBER++;
		}
	}
}
