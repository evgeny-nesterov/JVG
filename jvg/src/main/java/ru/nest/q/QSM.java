package ru.nest.q;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class QSM {
	int n;

	int n_minus_1;

	int n_plus_1;

	long startTime;

	int maxLevel;

	QSM(int n) {
		this.n = n;
		this.n_minus_1 = n - 1;
		this.n_plus_1 = n + 1;
		matrix = new int[n_plus_1][n];
		plusCount = new int[n_plus_1];
		minusCount = new int[n_plus_1];
		out = new int[n];
		m = new BigInteger[n][n];
		maxLevel = 2 * (n + 1) / 3;
		buf = new BigInteger[n];
		A = new BigInteger[n];
		B = new BigInteger[n_minus_1];
	}

	int[][] matrix;

	int[] plusCount;

	int[] minusCount;

	int[] out;

	BigInteger[][] m;

	int fillCount;

	BigInteger[] buf;

	BigInteger W, H;

	BigInteger[] A; // answer

	BigInteger[] B;

	void start() {
		startTime = System.currentTimeMillis();
		iterateLevel(0, 1, 0);
	}

	void iterateLevel(int startLevel, int endLevel, int quadIndex) {
		int restCount = 3 * endLevel - 2 - fillCount;
		if (plusCount[0] >= 3) {
			restCount++;
		}
		int endMinusCount = minusCount[endLevel];
		if (endMinusCount >= 3) {
			restCount++;
		}
		int maxRestCount = 2 * (n - quadIndex);
		if (restCount > maxRestCount) {
			return;
		} else if (restCount == maxRestCount && endMinusCount >= 3) {
			return;
		}

		int nextLevel = endLevel + 1;
		int nextQuadIndex = quadIndex + 1;
		int[] endMatrixLevel = matrix[endLevel];
		endMatrixLevel[quadIndex] = -1;
		boolean isFilledRest1 = endMinusCount < 3 && endMinusCount + plusCount[endLevel] < 3;
		if (isFilledRest1) {
			fillCount++;
		}
		minusCount[endLevel] = endMinusCount + 1;

		for (int level = startLevel; level < endLevel; level++) {
			int[] startMatrixLevel = matrix[level];
			startMatrixLevel[quadIndex] = 1;
			int levelPlusCount = plusCount[level];
			boolean isFilledRest2 = minusCount[level] + levelPlusCount < 3;
			if (isFilledRest2) {
				fillCount++;
			}
			plusCount[level] = levelPlusCount + 1;

			if (nextQuadIndex < n) {
				iterateLevel(0, nextLevel, nextQuadIndex);
				iterateLevel(level + 1, endLevel, nextQuadIndex);
			} else {
				check(endLevel);
			}

			startMatrixLevel[quadIndex] = 0;
			plusCount[level] = levelPlusCount;
			if (isFilledRest2) {
				fillCount--;
			}
		}
		endMatrixLevel[quadIndex] = 0;
		minusCount[endLevel] = endMinusCount;
		if (isFilledRest1) {
			fillCount--;
		}
	}

	int resultsCount = 0;

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

		////////////////////////////////////////////////////////////////////////
		// Create equations matrix
		for (int l = 1; l < levelsCount; l++) {
			int[] levelArray = matrix[l];
			for (int x = 0; x < n; x++) {
				m[l - 1][x] = BigInteger.valueOf(levelArray[x]);
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
		for (int i = 0; i < n; i++) {
			buf[i] = BigInteger.ZERO;
		}
		int processedIndex = processVerticalChains(m, levelsCount, 0, levelsCount - 1, buf);
		if (processedIndex != -1) {
			BigInteger[] lastLine = m[processedIndex - 1];
			for (int l = levelsCount - 1; l < processedIndex; l++) {
				BigInteger[] levelArray = m[l];
				for (int x0 = 0; x0 < n; x0++) {
					levelArray[x0] = levelArray[x0].subtract(lastLine[x0]);
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
			BigInteger mainValue = BigInteger.ZERO;
			// for (int l = 0; l < equationsCount; l++) {
			for (int l = equationsCount - 1; l >= 0; l--) {
				if (!processed[l]) {
					mainValue = m[l][x0];
					if (mainValue.signum() != 0) {
						mainLevel = l;
						break;
					}
				}
			}
			if (mainLevel == -1) {
				continue;
			}
			processed[mainLevel] = true;

			BigInteger[] mainLevelArray = m[mainLevel];
			for (int l = 0; l < equationsCount; l++) {
				if (l != mainLevel) {
					BigInteger[] levelArray = m[l];
					BigInteger valueX0 = levelArray[x0];
					if (valueX0.signum() != 0) {
						for (int x = 0; x < n; x++) {
							levelArray[x] = levelArray[x].multiply(mainValue).subtract(mainLevelArray[x].multiply(valueX0));
						}
					}
				}
			}
		}

		int nonNullLines = 0;
		for (int l = 0; l < equationsCount; l++) {
			if (m[l][n_minus_1].signum() != 0) {
				nonNullLines++;
			}
		}
		if (nonNullLines < n - 1) {
			return;
		}

		for (int x = 0; x < n; x++) {
			A[x] = BigInteger.ZERO;
		}
		for (int x = 0; x < n_minus_1; x++) {
			B[x] = BigInteger.ZERO;
		}
		for (int l = 0; l < equationsCount; l++) {
			BigInteger[] levelArray = m[l];
			for (int x = 0; x < n_minus_1; x++) {
				BigInteger a = levelArray[x];
				if (a.signum() != 0) {
					BigInteger b = levelArray[n_minus_1];
					if (a.signum() == b.signum()) {
						return;
					}
					if (a.signum() == -1) {
						a = a.negate();
					} else {
						b = b.negate();
					}
					BigInteger nod = nod(a, b);
					A[x] = a.divide(nod);
					B[x] = b.divide(nod);
					break;
				}
			}
		}

		BigInteger b = A[0];
		for (int i = 1; i < B.length; i++) {
			BigInteger a = A[i];
			if (a.signum() == 0) {
				return;
			}
			b = b.multiply(a.divide(nod(a, b)));
		}
		for (int i = 0; i < B.length; i++) {
			A[i] = b.multiply(B[i]).divide(A[i]);
		}
		A[n_minus_1] = b;

		////////////////////////////////////////////////////////////////////////
		// Exclude results with the same quads
		for (int i1 = 0; i1 < A.length; i1++) {
			BigInteger a1 = A[i1];
			for (int i2 = i1 + 1; i2 < A.length; i2++) {
				BigInteger a2 = A[i2];
				if (a1.equals(a2) || a1.equals(a2.negate())) {
					return;
				}
			}
		}

		// check answer
		//		for (int l = 0; l < m.length; l++) {
		//			BigInteger sum = BigInteger.ZERO;
		//			for (int x = 0; x < n; x++) {
		//				sum = sum.add(A[x].multiply(m[l][x]));
		//			}
		//			assert sum.signum() == 1;
		//		}

		////////////////////////////////////////////////////////////////////////
		// Compute rect size
		W = BigInteger.ZERO;
		H = BigInteger.ZERO;
		{
			for (int x = 0; x < n; x++) {
				W = W.add(A[x].multiply(BigInteger.valueOf(matrix[0][x])));
			}
			int l = 0;
			WHILE:
			while (l < levelsCount) {
				for (int x = 0; x < n; x++) {
					if (matrix[l][x] == 1) {
						H = H.add(A[x].multiply(BigInteger.valueOf(matrix[l][x])));
						while (l < levelsCount) {
							if (matrix[++l][x] == -1) {
								continue WHILE;
							}
						}
					}
				}
			}
			//			if (W.signum() == -1) {
			//				W = W.negate();
			//				H = H.negate();
			//			}
			if (W.compareTo(H) < 0) {
				return;
			}
		}

		////////////////////////////////////////////////////////////////////////
		// Show result
		resultsCount++;
		System.out.println("---------- [" + n + " / " + resultsCount + "] levelsCount: " + levelsCount + " ----------");
		printMatrix(matrix, levelsCount);
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

		Result result = getResult(levelsCount, A);
		results.add(result);
		System.out.println(result);

		System.out.println("Size: " + W + " x " + H);
		System.out.println();
	}

	List<Result> results = new ArrayList<>();

	BigInteger nod(BigInteger a, BigInteger b) {
		if (a.signum() == -1) {
			a = a.negate();
		}
		if (b.signum() == -1) {
			b = b.negate();
		}
		while (true) {
			if (a.compareTo(b) > 0) {
				if ((a = a.mod(b)).signum() == 0) {
					return b;
				}
			} else if ((b = b.mod(a)).signum() == 0) {
				return a;
			}
		}
	}

	int processVerticalChains(BigInteger[][] m, int levelsCount, int level, int index, BigInteger[] buf) {
		int[] levelMatrix = matrix[level];
		for (int x = 0; x < n; x++) {
			if (levelMatrix[x] == 1) {
				buf[x] = BigInteger.ONE;
				int nextLevel = out[x];
				if (nextLevel < levelsCount) {
					index = processVerticalChains(m, levelsCount, nextLevel, index, buf);
					if (index == -1) {
						return -1;
					}
				} else {
					if (index < n) {
						BigInteger[] levelArray = m[index];
						for (int x0 = 0; x0 < n; x0++) {
							levelArray[x0] = buf[x0];
						}
					} else {
						for (int l = levelsCount - 1; l < n; l++) {
							BigInteger[] levelArray = m[l];
							for (int x0 = 0; x0 < n; x0++) {
								levelArray[x0] = levelArray[x0].subtract(buf[x0]);
							}
						}
						return -1;
					}
					index++;
				}
				buf[x] = BigInteger.ZERO;
			}
		}
		return index;
	}

	Result getResult(int levelsCount, BigInteger[] quadsSizes) {
		Quad[] quads = new Quad[n];
		int[] quadIndex = new int[];
		for (int x = 0; x < n; x++) {
			quads[x] = new Quad();
			quadIndex[x] = -1;
		}
		for (int l = 0; l <= levelsCount; l++) {
			int _x = 0, _y = 0;
			for (int x = 0; x < n; x++) {
				if (_y == 0 && matrix[l][x] == -1) {
					_x = quads[x].x;
					_y = quads[x].y + quads[x].size;
				} else if (matrix[l][x] == 1) {
					quads[x].set(_x, _y, quadsSizes[x].intValue(), x);
					_x += quads[x].size;
				}
			}
		}
		return new Result(quads, W.intValue(), H.intValue(), 0L);
	}

	void getResult(int l, int levelsCount, BigInteger[] quadsSizes, Quad[] quads, int[] quadIndex, int firstQuadIndex) {
		Quad firstQuad = quads[firstQuadIndex];
		int _x = firstQuad.x;
		int _y = firstQuad.y + firstQuad.size;
	}

	void showResult() {

	}

	void printMatrix(int[][] matrix, int levelsCount) {
		for (int l = 0; l <= levelsCount; l++) {
			for (int x = 0; x < n; x++) {
				System.out.print((matrix[l][x] >= 0 ? " " : "") + matrix[l][x] + " ");
			}
			System.out.println();
		}
	}

	static boolean paused;

	public static void main(String[] args) throws InterruptedException {
		long t1 = System.currentTimeMillis();
		int N1 = 11;
		int N2 = 11;
		List<Result> results = new ArrayList<>();
		for (int i = N1; i <= N2; i++) {
			long t2 = System.currentTimeMillis();
			QSM q = new QSM(i);
			q.start();
			results.addAll(q.results);
			System.out.println("\nchecks: " + q.resultsCount + " for " + (System.currentTimeMillis() - t2) / 1000.0 + "sec");
		}
		System.out.println("TIME: " + (System.currentTimeMillis() - t1) / 1000.0 + "sec");

		QFrame frame = new QFrame(10);
		frame.setTitle("Quads: " + N1 + " - " + N2);
		frame.setVisible(true);

		int index = 0;
		while (index < results.size()) {
			for (int n = 0; n < 10 && index < results.size(); n++) {
				Result result = results.get(index);
				QFrame.QPanel panel = frame.getQPanel(n);
				panel.setResult(result);
				index++;
			}
			paused = true;
			while (paused) {
				Thread.sleep(100);
				for (QFrame.QPanel panel : frame.panels) {
					if (panel.paused) {
						paused = false;
						panel.paused = false;
					}
				}
			}
		}
	}
}
