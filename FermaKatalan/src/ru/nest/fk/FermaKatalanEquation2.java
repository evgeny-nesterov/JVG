package ru.nest.fk;

import java.math.BigInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FermaKatalanEquation2 {
	int maxXY = 100000;
	int maxPow = 10;
	BigInteger[][] array;
	long startTime;

	FermaKatalanEquation2() {
		array = new BigInteger[maxXY][maxPow + 1];
		startTime = System.currentTimeMillis();
		prepare();
		start();
	}

	void prepare() {
		for (int i = 1; i < maxXY; i++) {
			BigInteger d = BigInteger.valueOf(i);
			BigInteger D = d;
			for (int pow = 2; pow <= maxPow; pow++) {
				D = D.multiply(d);
				array[i][pow] = D;
			}
		}
		System.out.println("data prepared");
	}

	void start() {
		int delta = 10;
		ExecutorService asyncExecutor = Executors.newFixedThreadPool(8);
		for (int i = 1; i < maxXY; i += delta) {
			final int startX = i;
			final int endX = Math.min(startX + delta, maxXY);
			asyncExecutor.execute(new Runnable() {
				@Override
				public void run() {
					start(startX, endX);
				}
			});
		}
		asyncExecutor.shutdown();
		try {
			asyncExecutor.awaitTermination(24, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	void start(int startX, int endX) {
		for (int x = startX; x < endX; x++) {
			System.out.println("--- " + x + " ---");
			BigInteger[] xpowers = array[x];
			for (int y = x + 1; y < maxXY; y++) {
				if (nodMore1(x, y)) {
					continue;
				}
				BigInteger[] ypowers = array[y];
				for (int px = 3; px <= maxPow; px++) {
					double kx = 1.0 / px + 0.5;
					BigInteger X = xpowers[px];
					for (int py = 3; py <= maxPow; py++) {
						BigInteger Y = ypowers[py];
						if (kx + 1.0 / py >= 1) {
							continue;
						}

						BigInteger Z = X.add(Y);
						BigInteger z = sqrt(Z);
						if (z.multiply(z).equals(Z)) {
							System.out.println(x + "^" + px + " + " + y + "^" + py + " = " + z + "^" + 2 + " (" + (System.currentTimeMillis() - startTime) / 1000 + "sec)");
						} else {
							Z = X.subtract(Y).abs();
							z = sqrt(Z);
							if (z.multiply(z).equals(Z)) {
								System.out.println(x + "^" + px + " + " + y + "^" + py + " = " + z + "^" + 2 + " (" + (System.currentTimeMillis() - startTime) / 1000 + "sec)");
							}
						}
					}
				}
			}
		}
	}

	public BigInteger sqrt(BigInteger x) {
		BigInteger div = BigInteger.ZERO.setBit(x.bitLength() / 2);
		BigInteger div2 = div;
		for (;;) {
			BigInteger y = div.add(x.divide(div)).shiftRight(1);
			if (y.equals(div) || y.equals(div2)) {
				return y;
			}
			div2 = div;
			div = y;
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
		new FermaKatalanEquation2();
	}
}
