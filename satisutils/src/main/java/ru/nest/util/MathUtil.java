package ru.nest.util;

import java.math.BigInteger;

public class MathUtil {
	public static BigInteger noc(BigInteger... list) {
		BigInteger noc = list[0];
		for (int i = 1; i < list.length; i++) {
			noc = noc(noc, list[i]);
		}
		return noc;
	}

	public static BigInteger noc(BigInteger i1, BigInteger i2) {
		if (i1.signum() == 0) {
			return i2;
		}

		if (i2.signum() == 0) {
			return i1;
		}

		BigInteger nod = nod(i1, i2);
		if (nod.compareTo(i1) == 0) {
			return i2;
		}
		if (nod.compareTo(i2) == 0) {
			return i1;
		}
		return i1.multiply(i2).divide(nod);
	}

	public static BigInteger nod(BigInteger i1, BigInteger i2) {
		if (i1.signum() == -1) {
			i1 = i1.negate();
		} else if (i1.signum() == 0) {
			return i2;
		}

		if (i2.signum() == -1) {
			i2 = i2.negate();
		} else if (i2.signum() == 0) {
			return i1;
		}

		int compare = i1.compareTo(i2);
		if (compare == 0) {
			return i2;
		} else if (compare < 0) {
			BigInteger tmp = i1;
			i1 = i2;
			i2 = tmp;
		}

		BigInteger r = null;
		while (true) {
			r = i1.remainder(i2);
			if (r.signum() == 0) {
				return i2;
			}

			i1 = i2;
			i2 = r;
		}
	}

	public static long noc(long... list) {
		long noc = list[0];
		for (int i = 1; i < list.length; i++) {
			noc = noc(noc, list[i]);
		}
		return noc;
	}

	public static long noc(long i1, long i2) {
		if (i1 == 0) {
			return i2;
		}

		if (i2 == 0) {
			return i1;
		}

		long nod = nod(i1, i2);
		if (nod == i1) {
			return i2;
		}
		if (nod == i2) {
			return i1;
		}
		return i1 * i2 / nod;
	}

	public static long nod(long i1, long i2) {
		if (i1 < 0) {
			i1 = -i1;
		} else if (i1 == 0) {
			return i2;
		}

		if (i2 < 0) {
			i2 = -i2;
		} else if (i2 == 0) {
			return i1;
		}

		if (i1 == i2) {
			return i2;
		} else if (i1 < i2) {
			long tmp = i1;
			i1 = i2;
			i2 = tmp;
		}

		long r = 1;
		while (true) {
			r = i1 % i2;
			if (r == 0) {
				return i2;
			}

			i1 = i2;
			i2 = r;
		}
	}

	public static double min(double... v) {
		double min = v[0];
		for (int i = 1; i < v.length; i++) {
			if (min > v[i]) {
				min = v[i];
			}
		}
		return min;
	}

	public static double max(double... v) {
		double max = v[0];
		for (int i = 1; i < v.length; i++) {
			if (max < v[i]) {
				max = v[i];
			}
		}
		return max;
	}
}
