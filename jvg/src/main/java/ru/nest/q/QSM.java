package ru.nest.q;

public class QSM {
	int n;
	int n_minus_1;
	int n_plus_1;
	long startTime;

	QSM(int n) {
		this.n = n;
		this.n_minus_1 = n - 1;
		this.n_plus_1 = n + 1;
		matrix = new int[n_plus_1][n];
		plusCount = new int[n_plus_1];
		minusCount = new int[n_plus_1];
		out = new int[n];
		m = new int[n_minus_1][n];
	}

	int[][] matrix;
	int[] plusCount;
	int[] minusCount;
	int[] out;
	int[][] m;

	void start() {
		startTime = System.currentTimeMillis();
		start(0, 1, 0);
	}

	void start(int startLevel, int endLevel, int quadIndex) {
		int nextLevel = endLevel + 1;
		int nextQuadIndex = quadIndex + 1;
		int[] endMatrixLevel = matrix[endLevel];
		endMatrixLevel[quadIndex] = -1;
		minusCount[endLevel]++;
		for (int level = startLevel; level < endLevel; level++) {
			int[] startMatrixLevel = matrix[level];
			startMatrixLevel[quadIndex] = 1;
			plusCount[level]++;

			if (nextQuadIndex < n) {
				start(level + 1, endLevel, nextQuadIndex);
				start(0, nextLevel, nextQuadIndex);
			} else {
				check(endLevel);
			}

			startMatrixLevel[quadIndex] = 0;
			plusCount[level]--;
		}
		endMatrixLevel[quadIndex] = 0;
		minusCount[endLevel]--;
	}

	int checkCount = 1;

	void check(int levelsCount) {
		if (minusCount[levelsCount] < 2) { //  || plusCount[levelsCount] > 0
			return;
		}
		if (plusCount[0] > minusCount[levelsCount]) {
			return;
		}
		for (int level = levelsCount - 1; level > 0; level--) {
			int plus = plusCount[level];
			if (plus + minusCount[level] < 3 || plus == 0) {
				return;
			}
		}
		if (plusCount[0] < 2) { //  || minusCount[0] > 0
			return;
		}
		for (int x = 0; x < n; x++) {
			if (matrix[0][x] != 0 && matrix[levelsCount][x] != 0) {
				return;
			}
		}

//		{
//			int leftQuadsCount = 0;
//			int l = 0, x = 0;
//			WHILE:
//			while (l < levelsCount) {
//				for (; x < n; x++) {
//					if (matrix[l][x] == 1) {
//						leftQuadsCount++;
//						while (l < levelsCount) {
//							if (matrix[++l][x] == -1) {
//								continue WHILE;
//							}
//						}
//					}
//				}
//			}
//
//			int rightQuadsCount = 0;
//			l = 0;
//			WHILE:
//			while (l < levelsCount) {
//				for (x = n - 1; x >= 0; x--) {
//					if (matrix[l][x] == 1) {
//						rightQuadsCount++;
//						while (l < levelsCount) {
//							if (matrix[++l][x] == -1) {
//								continue WHILE;
//							}
//						}
//					}
//				}
//			}
//
//			if (leftQuadsCount > rightQuadsCount) {
//				return;
//			}
//		}

		for (int l = 1; l < levelsCount; l++) {
			int[] levelArray = matrix[l];
			for (int x = 0; x < n; x++) {
				m[l - 1][x] = levelArray[x];
				if (levelArray[x] == -1) {
					out[x] = l;
				}
			}
		}
		int[] lastLevelMatrix = matrix[levelsCount];
		for (int x = 0; x < n; x++) {
			if (lastLevelMatrix[x] == -1) {
				out[x] = levelsCount;
			}
		}
		fillMatrix(m, levelsCount, 0, levelsCount - 1, new int[n]);

		// System.out.println();
		// printMatrix(m, n_minus_1 - 1);

		boolean[] processed = new boolean[n_minus_1];
		for (int x0 = 0; x0 < n; x0++) {
			int mainLevel = -1;
			int mainValue = 0;
			// for (int l = 0; l < n_minus_1; l++) {
			for (int l = n_minus_1 - 1; l >= 0; l--) {
				if (!processed[l]) {
					mainValue = m[l][x0];
					if (mainValue != 0) {
						mainLevel = l;
						break;
					}
				}
			}
			if (mainLevel == -1) {
				continue;
			}
			int[] mainLevelArray = m[mainLevel];

			processed[mainLevel] = true;
			for (int l = 0; l < n_minus_1; l++) {
				if (l != mainLevel) {
					int[] levelArray = m[l];
					int valueX0 = levelArray[x0];
					if (valueX0 != 0) {
						for (int x = 0; x < n; x++) {
							levelArray[x] = levelArray[x] * mainValue - mainLevelArray[x] * valueX0;
						}
					}
				}
			}
		}

		for (int l = 0; l < n_minus_1; l++) {
			if (m[l][n_minus_1] == 0) {
				return;
			}
		}
//		for (int x = 0; x < n_minus_1; x++) {
//			if (m[0][x] != 0 && ((m[0][x] > 0 && m[0][n_minus_1] > 0) || (m[0][x] < 0 && m[0][n_minus_1] < 0))) {
//				return;
//			}
//			if (m[n_minus_1 - 1][x] != 0 && ((m[n_minus_1 - 1][x] > 0 && m[n_minus_1 - 1][n_minus_1] > 0) || (m[n_minus_1 - 1][x] < 0 && m[n_minus_1 - 1][n_minus_1] < 0))) {
//				return;
//			}
//		}

		int[] A = new int[n];
		int[] B = new int[n_minus_1];
		for (int l = 0; l < n_minus_1; l++) {
			int[] levelArray = m[l];
			for (int x = 0; x < n_minus_1; x++) {
				int a = levelArray[x];
				if (a != 0) {
					int b = levelArray[n_minus_1];
					if (a < 0) {
						a = -a;
					} else {
						b = -b;
					}
					int nod = nod(a, b);
					A[x] = a / nod;
					B[x] = b / nod;
					break;
				}
			}
		}

		int b = A[0];
		for (int i = 1; i < B.length; i++) {
			int a = A[i];
			if (a == 0) {
				return;
			}
			b *= a / nod(a, b);
		}
		for (int i = 0; i < B.length; i++) {
			A[i] = b * B[i] / A[i];
		}
		A[n_minus_1] = b;

		for (int i1 = 0; i1 < A.length; i1++) {
			for (int i2 = i1 + 1; i2 < A.length; i2++) {
				if (A[i1] == A[i2] || A[i1] == -A[i2]) {
					return;
				}
			}
		}

		for (int l = 0; l < m.length; l++) {
			int sum = 0;
			for (int x = 0; x < n; x++) {
				sum += A[x] * m[l][x];
			}
			assert sum == 0;
		}

		int W = 0;
		int H = 0;
		{
			for (int x = 0; x < n; x++) {
				W += matrix[0][x] * A[x];
			}
			int l = 0;
			WHILE:
			while (l < levelsCount) {
				for (int x = 0; x < n; x++) {
					if (matrix[l][x] == 1) {
						H += matrix[l][x] * A[x];
						while (l < levelsCount) {
							if (matrix[++l][x] == -1) {
								continue WHILE;
							}
						}
					}
				}
			}
			if (W < H) {
				return;
			}
		}

		checkCount++;
		printMatrix(levelsCount);
		System.out.println();
		System.out.print("Answer: ");
		for (int i = 0; i < A.length; i++) {
			if (i > 0) {
				System.out.print(", ");
			}
			System.out.print(A[i]);
		}
		System.out.println();
//		printMatrix(m, n_minus_1 - 1);
//		System.out.println();
		System.out.println("Size: " + W + " x " + H + "\n");

		getResult(levelsCount, A);
	}

	int nod(int a, int b) {
		if (a < 0) {
			a = -a;
		}
		if (b < 0) {
			b = -b;
		}
		while (true) {
			if (a > b) {
				if ((a = a % b) == 0) {
					return b;
				}
			} else if ((b = b % a) == 0) {
				return a;
			}
		}
	}

	int fillMatrix(int[][] m, int levelsCount, int level, int index, int[] buf) {
		int[] levelMatrix = matrix[level];
		for (int x = 0; x < n; x++) {
			if (levelMatrix[x] == 1) {
				buf[x] = 1;
				int nextLevel = out[x];
				if (nextLevel < levelsCount) {
					index = fillMatrix(m, levelsCount, nextLevel, index, buf);
				} else {
					if (index < n_minus_1) {
						int[] levelArray = m[index];
						for (int x0 = 0; x0 < n; x0++) {
							levelArray[x0] = buf[x0];
						}
					} else {
						for (int l = levelsCount - 1; l < n_minus_1; l++) {
							int[] levelArray = m[l];
							for (int x0 = 0; x0 < n; x0++) {
								levelArray[x0] -= buf[x0];
							}
						}
					}
					index++;
				}
				buf[x] = 0;
			}
		}
		return index;
	}

	Result getResult(int levelsCount, int[] quadsSizes) {
		Quad[] quads = new Quad[n];
		for (int l = 0; l <= levelsCount; l++) {
			for (int x = 0; x < n; x++) {
				if (matrix[l][x] == 1) {

				}
			}
		}
		return null; // new Result(quads, W, H, System.currentTimeMillis() - startTime);
	}

	void showResult() {

	}

	void printMatrix(int levelsCount) {
		System.out.println("---------- [" + checkCount + "] levelsCount: " + levelsCount + " ----------");
		printMatrix(matrix, levelsCount);
	}

	void printMatrix(int[][] matrix, int levelsCount) {
		for (int l = 0; l <= levelsCount; l++) {
			for (int x = 0; x < n; x++) {
				System.out.print((matrix[l][x] >= 0 ? " " : "") + matrix[l][x] + " ");
			}
			System.out.println();
		}
	}

	public static void main(String[] args) {
		long t = System.currentTimeMillis();
		QSM q = new QSM(10);
		q.start();
		System.out.println("\nchecks: " + q.checkCount + " for " + (System.currentTimeMillis() - t) / 1000.0 + "sec");
	}
}
