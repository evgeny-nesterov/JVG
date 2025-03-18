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
		m = new int[n][n];
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

	int checkCount = 0;

	void check(int levelsCount) {
		////////////////////////////////////////////////////////////////////////
		// Check matrix structure
		if (minusCount[levelsCount] < 2) {
			return;
		}
		// assert plusCount[levelsCount] == 0;
		for (int level = levelsCount - 1; level > 0; level--) {
			int plus = plusCount[level];
			if (plus + minusCount[level] < 3 || plus == 0) {
				return;
			}
			// assert minusCount[level] > 0;
		}
		if (plusCount[0] < 2) {
			return;
		}
		// assert minusCount[0] == 0;
		int[] firstLevelArray = matrix[0];
		for (int x = 0; x < n; x++) {
			if (firstLevelArray[x] != 0 && matrix[levelsCount][x] != 0) {
				return;
			}
		}

		//printMatrix(levelsCount);

		////////////////////////////////////////////////////////////////////////
		// Create equations matrix
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
		int equationsCount = n;
		int processedIndex = processVerticalChains(m, levelsCount, 0, levelsCount - 1, new int[n]);
		if (processedIndex != -1) {
			int[] lastLine = m[processedIndex - 1];
			for (int l = levelsCount - 1; l < processedIndex; l++) {
				int[] levelArray = m[l];
				for (int x0 = 0; x0 < n; x0++) {
					levelArray[x0] -= lastLine[x0];
				}
			}
			equationsCount = processedIndex - 1;
		}
		// System.out.println();
		// printMatrix(m, n_minus_1 - 1);

		////////////////////////////////////////////////////////////////////////
		// Resolve system of linear equations
		boolean[] processed = new boolean[equationsCount];
		for (int x0 = 0; x0 < n; x0++) {
			int mainLevel = -1;
			int mainValue = 0;
			// for (int l = 0; l < equationsCount; l++) {
			for (int l = equationsCount - 1; l >= 0; l--) {
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
			for (int l = 0; l < equationsCount; l++) {
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

		int nonNullLines = 0;
		for (int l = 0; l < equationsCount; l++) {
			if (m[l][n_minus_1] != 0) {
				nonNullLines++;
			}
		}
		if (nonNullLines < n - 1) {
			return;
		}

		int[] A = new int[n]; // answer
		int[] B = new int[n_minus_1];
		for (int l = 0; l < equationsCount; l++) {
			int[] levelArray = m[l];
			for (int x = 0; x < n_minus_1; x++) {
				int a = levelArray[x];
				if (a != 0) {
					int b = levelArray[n_minus_1];
					if ((a < 0 && b < 0) || (a > 0 && b > 0)) {
						return;
					}
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

		////////////////////////////////////////////////////////////////////////
		// Exclude results with the same quads
		for (int i1 = 0; i1 < A.length; i1++) {
			int a1 = A[i1];
			for (int i2 = i1 + 1; i2 < A.length; i2++) {
				int a2 = A[i2];
				if (a1 == a2 || a1 == -a2) {
					return;
				}
			}
		}

		// check answer
//		for (int l = 0; l < m.length; l++) {
//			int sum = 0;
//			for (int x = 0; x < n; x++) {
//				sum += A[x] * m[l][x];
//			}
//			assert sum == 0;
//		}

		////////////////////////////////////////////////////////////////////////
		// Compute rect size
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
			if (W < 0) {
				W = -W;
				H = -H;
			}
			if (W < H) {
				return;
			}
		}

		////////////////////////////////////////////////////////////////////////
		// Show result
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
		System.out.println("Size: " + W + " x " + H);
		System.out.println();

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

	int processVerticalChains(int[][] m, int levelsCount, int level, int index, int[] buf) {
		int[] levelMatrix = matrix[level];
		for (int x = 0; x < n; x++) {
			if (levelMatrix[x] == 1) {
				buf[x] = 1;
				int nextLevel = out[x];
				if (nextLevel < levelsCount) {
					index = processVerticalChains(m, levelsCount, nextLevel, index, buf);
					if (index == -1) {
						return -1;
					}
				} else {
					if (index < n) {
						int[] levelArray = m[index];
						for (int x0 = 0; x0 < n; x0++) {
							levelArray[x0] = buf[x0];
						}
					} else {
						for (int l = levelsCount - 1; l < n; l++) {
							int[] levelArray = m[l];
							for (int x0 = 0; x0 < n; x0++) {
								levelArray[x0] -= buf[x0];
							}
						}
						return -1;
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
		QSM q = new QSM(19);
		q.start();
		System.out.println("\nchecks: " + q.checkCount + " for " + (System.currentTimeMillis() - t) / 1000.0 + "sec");
	}
}
