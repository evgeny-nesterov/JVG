package ru.nest.fk;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class FermaKatalanEquation {
	FermaKatalanEquation() {
		start();
	}

	// 1^m + 2^3 = 3^2
	// 2^5 + 7^2 = 3^4
	// 2^9 = 7^3 + 13^2
	// 2^7 + 17^3 = 71^2
	// 3^5 + 11^4 = 122^2
	// 17^7 + 76271^3 = 21063928^2
	// 33^8 + 1549034^2 = 15613^3
	// 43^8 + 96222^3 = 30042907^2
	// 65^7 = 1414^3 + 2213459^2
	// 113^7 = 9262^3 = 15312283^2

	// 53000^15 - 500000^15 - 40000000^2
	void start() {
		int maxXY = 500000;
		int maxPow = 15;
		int maxZ = 40000000;
		BigInteger MAX = BigInteger.valueOf(maxZ).multiply(BigInteger.valueOf(maxZ));

		BigInteger[][] array = new BigInteger[maxXY][maxPow + 1];
		int[] maxPowers = new int[maxXY];
		Map<BigInteger, Integer> map = new HashMap<>(maxZ);
		System.out.println("map created");

		for (int i = 1; i < maxZ; i++) {
			BigInteger d = BigInteger.valueOf(i);
			BigInteger D = d;
			for (int pow = 2; pow <= maxPow; pow++) {
				D = D.multiply(d);
				if (pow == 2 && map.putIfAbsent(D, i) == null) {
					if (map.size() % 100000 == 0) {
						System.out.println("processed: " + map.size());
					}
				}

				if (i < maxXY) {
					if (D.compareTo(MAX) >= 0) {
						break;
					}
					array[i][pow] = D;
					maxPowers[i] = pow;
				} else if (pow > 2) {
					break;
				}
			}
		}
		System.out.println("map filled");

		for (int x = 1; x < maxXY; x++) {
			if (x % 500 == 0) {
				System.out.println("--- " + x + " ---");
			}
			BigInteger[] xpowers = array[x];
			int maxPowX = maxPowers[x];
			for (int y = x + 1; y < maxXY; y++) {
				if (nodMore1(x, y)) {
					continue;
				}
				BigInteger[] ypowers = array[y];
				int maxPowY = maxPowers[y];
				for (int px = 3; px <= maxPowX; px++) {
					double kx = 1.0 / px + 0.5;
					BigInteger X = xpowers[px];
					for (int py = 3; py <= maxPowY; py++) {
						BigInteger Y = ypowers[py];
						if (kx + 1.0 / py >= 1) {
							continue;
						}

						BigInteger Z = X.add(Y);
						Integer z = null;
						if (MAX.compareTo(Z) == 1) {
							z = map.get(Z);
						}
						if (z == null) {
							Z = X.subtract(Y).abs();
							if (MAX.compareTo(Z) == 1) {
								z = map.get(Z);
							}
						}
						if (z != null) {
							System.out.println(x + "^" + px + " + " + y + "^" + py + " = " + z + "^" + 2);
						}
					}
				}
			}
		}
	}

	boolean nodMore1(int a, int b) {
		while (a != 0 && b != 0) {
			if (a >= b) {
				a = a % b;
			} else {
				b = b % a;
			}
		}
		return a > 1 || b > 1;
	}

	public static void main(String[] args) {
		new FermaKatalanEquation();
	}
}
