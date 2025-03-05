package ru.nest;

public class Q {
	int X;

	Q(int X) { // X < Y
		this.X = X;
		front = new int[X];
		busy = new boolean[X + 1];
		squares = new int[X + 1];
		for (int i = 0; i < X + 1; i++) {
			squares[i] = i * i;
		}
		sequence = new int[X];
	}

	int[] front;

	boolean[] busy;

	int[] squares;

	int[] sequence;

	void start() {
		start(0);
	}

	void start(int n) {
		int minLevel = Integer.MAX_VALUE;
		int minLevelX1 = 0;
		int minLevelX2 = 0;
		for (int x = 0; x < X; x++) {
			int f = front[x];
			if (f < minLevel) {
				minLevel = f;
				minLevelX1 = x;
				while (++x < X && front[x] == f) ;
				minLevelX2 = x;
				x--;
			}
		}

		if (minLevel > 0 && minLevelX1 == 0 && minLevelX2 == X) {
			print(n);
		} else {
			start(n, minLevel, minLevelX1, minLevelX2);
		}
	}

	/*
			|
			|_______          |
			|   S1 |        __|
			|______|_______|__| Sx < S1
	 */
	void start(int n, int minLevel, int minLevelX1, int minLevelX2) {
		int minLevelLength = minLevelX2 - minLevelX1;
		for (int quad = 1; quad <= minLevelLength; quad++) {
			if (!busy[quad]) {
				int x2 = minLevelX1 + quad;
				if (minLevel == 0 && x2 == X && quad > front[0]) {
					continue;
				}

				busy[quad] = true;
				int newLevel = minLevel + quad;
				for (int x = minLevelX1; x < x2; x++) {
					front[x] = newLevel;
				}
				sequence[n] = quad;

				if (quad < minLevelLength) {
					start(n + 1, minLevel, minLevelX1 + quad, minLevelX2);
				} else {
					start(n + 1);
				}

				for (int x = minLevelX1; x < x2; x++) {
					front[x] = minLevel;
				}
				busy[quad] = false;
			}
		}
	}

	void print(int n) {
		System.out.print("RESULT: ");
		for (int i = 0; i <= n; i++) {
			if (i > 0) {
				System.out.print(", ");
			}
			System.out.print(sequence[i]);
		}
		System.out.println();
	}

	public static void main(String[] args) {
		// 32-33	23ms	18, 14, 4, 10, 15, 7, 1, 9, 8
		// 61-69	61sec	36, 25, 9, 16, 2, 7, 33, 5, 28
		// 74-112
		long t = System.currentTimeMillis();
		new Q(74).start();
		System.out.println("time: " + (System.currentTimeMillis() - t) / 1000.0 + "sec");
	}
}
