package ru.nest;

public class Q {
	int X;

	Q(int X) {
		this.X = X;
		front = new int[X];
		busy = new boolean[X + 1];
		sequence = new int[X];
	}

	int[] front;

	boolean[] busy;

	int[] sequence;

	int foundCount = 0;

	int n;

	boolean start() {
		startLevel0(0);
		return foundCount > 0;
	}

	/*
			|
			|_______          |
			|   S1 |        __|
			|______|_______|__| Sx < S1
	 */
	void startLevel0(int levelX1) {
		int levelLength = X - levelX1;
		for (int quad = 3; quad <= levelLength; quad++) {
			if (!busy[quad]) {
				int x2 = levelX1 + quad;
				if (x2 == X && (quad > sequence[0] || quad <= 3)) {
					continue;
				}

				busy[quad] = true;
				for (int x = levelX1; x < x2; x++) {
					front[x] = quad;
				}
				sequence[n++] = quad;

				if (quad < levelLength) {
					startLevel0(x2);
				} else {
					findLevel();
				}

				for (int x = levelX1; x < x2; x++) {
					front[x] = 0;
				}
				busy[quad] = false;
				n--;
			}
		}
	}

	void startLevel(int level, int levelX1, int levelX2) {
		int levelLength = levelX2 - levelX1;
		for (int quad = 1; quad <= levelLength; quad++) {
			if (!busy[quad]) {
				int x2 = levelX1 + quad;

				busy[quad] = true;
				int newLevel = level + quad;
				for (int x = levelX1; x < x2; x++) {
					front[x] = newLevel;
				}
				sequence[n++] = quad;

				if (quad < levelLength) {
					startLevel(level, x2, levelX2);
				} else {
					findLevel();
				}

				for (int x = levelX1; x < x2; x++) {
					front[x] = level;
				}
				busy[quad] = false;
				n--;
			}
		}
	}

	void findLevel() {
		int currentLevel = front[0];
		int bestLevel = currentLevel;
		int bestLevelX1 = 0;
		int bestLevelX2 = X;
		int x1 = 0, x2 = 0, y;
		while (x2 < X) {
			y = front[x2];
			while (++x2 < X && (y = front[x2]) == currentLevel) ;
			if ((y > currentLevel || x2 == X) && x2 - x1 < bestLevelX2 - bestLevelX1) {
				bestLevel = currentLevel;
				bestLevelX1 = x1;
				bestLevelX2 = x2;
				while (++x2 < X && (y = front[x2]) >= currentLevel) {
					currentLevel = y;
				}
			}
			currentLevel = y;
			x1 = x2;
		}

		if (bestLevelX1 == 0 && bestLevelX2 == X && bestLevel > 0) {
			if (bestLevel > X) {
				print(n);
				foundCount++;
			}
		} else {
			startLevel(bestLevel, bestLevelX1, bestLevelX2);
		}
	}

	void print(int n) {
		System.out.print("RESULT " + X + "x" + front[0] + ": ");
		for (int i = 0; i <= n; i++) {
			if (i > 0) {
				System.out.print(", ");
			}
			System.out.print(sequence[i]);
		}
		System.out.println();
	}

	public static void main(String[] args) {
		// 32-33	6ms		18, 14, 4, 10, 15, 7, 1, 9, 8
		// 47-65	200ms	24, 23, 6, 17, 19, 5, 11, 3, 25, 22, 1
		// 55-57	1sec	27, 13, 15, 11, 2, 17, 3, 8, 30, 25, 1
		// 60-84	4sec	23, 16, 21, 7, 4, 5, 3, 1, 27, 33, 8, 19, 28, 13, 2, 17, 15, 6
		// 61-69	5sec	36, 25, 9, 16, 2, 7, 33, 5, 28
		// 63-94	9sec	23, 11, 10, 19, 1, 9, 12, 28, 35, 8, 20, 3, 5, 36, 2, 7, 27, 4
		// 64-66	12sec	30, 16, 18, 14, 2, 20, 36, 8, 28, 1
		// 65-88	12sec	25, 13, 10, 17, 3, 7, 12, 4, 8, 20, 31, 14, 2, 18, 16, 1, 33, 32, 5
		// 69-115	38sec	39, 30, 9, 21, 36, 12, 14, 19, 6, 8, 4, 2, 3, 16, 13, 40, 29, 1
		// 69-118	38sec	37, 32, 12, 20, 26, 11, 4, 8, 15, 28, 24, 17, 7, 38, 31, 1
		// 71-89	56sec	28, 22, 21, 1, 20, 6, 17, 23, 11, 12, 9, 7, 5, 15, 2, 10, 3, 8, 38, 33, 4
		// 71-106	56sec	29, 19, 23, 10, 9, 5, 18, 1, 13, 40, 31, 37, 3, 34, 2
		// 71-105	56sec	36, 35, 1, 5, 29, 20, 13, 4, 9, 7, 15, 19, 8, 11, 41, 30, 2
		// 72-123	74sec	23, 27, 22, 5, 17, 19, 4, 15, 9, 12, 6, 3, 32, 40, 11, 21, 41, 10, 31, 1
		// 72-173	74sec	37, 35, 2, 33, 39, 8, 25, 31, 16, 15, 1, 26, 28, 18, 6, 20, 10, 14, 38, 34, 4
		// 72-103	74sec	39, 33, 6, 27, 11, 13, 21, 9, 2, 7, 8, 15, 1, 14, 43, 29, 3
		// 73-75	92sec	34, 16, 23, 9, 7, 2, 8, 20, 5, 6, 4, 1, 3, 12, 41, 32, 10
		// 74-88	110sec	28, 19, 27, 9, 10, 2, 25, 23, 14, 12, 1, 11, 15, 17, 6, 36, 8, 13, 3, 5, 20, 18, 4
		// 74-103	110sec	32, 19, 23, 15, 4, 27, 30, 2, 17, 7, 20, 11, 6, 13, 41, 33, 1
		// 74-79	110sec	34, 23, 17, 8, 9, 11, 12, 7, 1, 10, 3, 4, 45, 15, 14, 29, 2
		// 74-112
		for (int n = 61; n <= 61; n++) {
			long t = System.currentTimeMillis();
			if (new Q(n).start()) {
				System.out.println("time: " + (System.currentTimeMillis() - t) / 1000.0 + "sec\n");
			}
		}
	}
}
