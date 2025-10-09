package ru.nest.q;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
		matrixTop = new int[n];
		matrixBottom = new int[n];
		plusCount = new int[n_plus_1];
		minusCount = new int[n_plus_1];
		matrix = new BigInteger[n][n];
		maxLevel = 2 * (n + 1) / 3;
		buf = new BigInteger[n];
		A = new BigInteger[n];
		B = new BigInteger[n_minus_1];
	}

	int[] matrixTop;

	int[] matrixBottom;

	int[] plusCount;

	int[] minusCount;

	int fillCount;

	BigInteger[][] matrix;

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
		int endMinusCount = minusCount[endLevel];
		int maxRestCount = n - quadIndex;
		if (endMinusCount >= 3) {
			restCount++;
		} else if (endMinusCount < 2) {
			maxRestCount++;
		}
		if (restCount > maxRestCount) {
			return;
		} else if (restCount == maxRestCount && endMinusCount > 1) {
			return;
		}

		int nextQuadIndex = quadIndex + 1;
		matrixTop[quadIndex] = endLevel;
		boolean isFilledRest1 = endMinusCount < 3 && endMinusCount + plusCount[endLevel] < 3;
		if (isFilledRest1) {
			fillCount++;
		}
		minusCount[endLevel] = endMinusCount + 1;

		for (int level = startLevel; level < endLevel; level++) {
			matrixBottom[quadIndex] = level;
			int levelPlusCount = plusCount[level];
			boolean isFilledRest2 = level == 0 ? levelPlusCount < 2 : minusCount[level] + levelPlusCount < 3;
			if (isFilledRest2) {
				fillCount++;
			}
			plusCount[level] = levelPlusCount + 1;

			if (nextQuadIndex < n) {
				iterateLevel(0, endLevel + 1, nextQuadIndex);
				iterateLevel(level + 1, endLevel, nextQuadIndex);
			} else {
				check(endLevel);
			}

			plusCount[level] = levelPlusCount;
			if (isFilledRest2) {
				fillCount--;
			}
		}
		minusCount[endLevel] = endMinusCount;
		if (isFilledRest1) {
			fillCount--;
		}
	}

	int resultsCount = 0;
	BigInteger MINUS_ONE = BigInteger.ONE.negate();

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
		//		for (int x = 0; x < n; x++) {
		//			int level = matrixTop[x];
		//			if (level == 0 && level == levelsCount) {
		//				return;
		//			}
		//		}

		////////////////////////////////////////////////////////////////////////
		// check results split (horizontal and vertical)
		{
			// vertical
			int prevMaxTopLevel = 1;
			for (int x = 1; x < n; x++) {
				int levelBottom = matrixBottom[x];
				int levelTop = matrixTop[x];
				if (levelTop == levelsCount) {
					prevMaxTopLevel = levelsCount;
					break;
				} else if (levelBottom < prevMaxTopLevel) {
					prevMaxTopLevel = levelTop;
				}
			}
			if (prevMaxTopLevel < levelsCount) {
				return;
			}
		}

		////////////////////////////////////////////////////////////////////////
		// Create equations matrix
		for (int x = 0; x < n; x++) {
			for (int level = 0; level < n; level++) {
				matrix[level][x] = BigInteger.ZERO;
			}
		}
		for (int x = 0; x < n; x++) {
			int level = matrixTop[x];
			if (level != levelsCount) {
				matrix[level - 1][x] = MINUS_ONE;
			}
			level = matrixBottom[x];
			if (level != 0) {
				matrix[level - 1][x] = BigInteger.ONE;
			}
		}
		int equationsCount = n;
		for (int i = 0; i < n; i++) {
			buf[i] = BigInteger.ZERO;
		}
		int processedIndex = processVerticalChains(0, matrix, levelsCount, 0, levelsCount - 1, buf);
		if (processedIndex != -1) {
			BigInteger[] lastLine = matrix[processedIndex - 1];
			for (int l = levelsCount - 1; l < processedIndex; l++) {
				BigInteger[] levelArray = matrix[l];
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
		boolean[] processedLevels = new boolean[equationsCount];
		int bIndex = n_minus_1;
		for (int x0 = 0; x0 < n; x0++) {
			if (x0 == n_minus_1 && bIndex == n_minus_1) {
				break;
			}
			int mainLevel = -1;
			BigInteger mainValueX0 = BigInteger.ZERO;
			for (int l = equationsCount - 1; l >= 0; l--) {
				if (!processedLevels[l]) {
					mainValueX0 = matrix[l][x0];
					if (mainValueX0.signum() != 0) {
						mainLevel = l;
						break;
					}
				}
			}
			if (mainLevel == -1) {
				if (bIndex == n_minus_1) {
					bIndex = x0;
					continue;
				} else {
					return;
				}
			}
			processedLevels[mainLevel] = true;

			BigInteger[] mainLevelArray = matrix[mainLevel];
			for (int l = 0; l < equationsCount; l++) {
				if (l != mainLevel) {
					BigInteger[] levelArray = matrix[l];
					BigInteger valueX0 = levelArray[x0];
					if (valueX0.signum() != 0) {
						for (int x = 0; x < n; x++) {
							if (x != x0) {
								levelArray[x] = levelArray[x].multiply(mainValueX0).subtract(mainLevelArray[x].multiply(valueX0));
							}
						}
						levelArray[x0] = BigInteger.ZERO;
					}
				}
			}
		}

		int nonNullLines = 0;
		for (int l = 0; l < equationsCount; l++) {
			if (matrix[l][bIndex].signum() != 0) {
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
			BigInteger[] levelArray = matrix[l];
			BigInteger b = levelArray[bIndex];
			for (int x = 0; x < n; x++) {
				int ax = x < bIndex ? x : x - 1;
				BigInteger a = levelArray[x];
				if (a.signum() != 0) {
					if (a.signum() == b.signum()) {
						return;
					}
					if (a.signum() == -1) {
						a = a.negate();
					} else {
						b = b.negate();
					}
					BigInteger nod = nod(a, b);
					A[ax] = a.divide(nod);
					B[ax] = b.divide(nod);
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

		//		// check answer
		//		for (int level = 0; level < m.length; level++) {
		//			BigInteger sum = BigInteger.ZERO;
		//			for (int x = 0; x < n; x++) {
		//				sum = sum.add(A[x].multiply(m[level][x]));
		//			}
		//			assert sum.signum() == 0;
		//		}

		////////////////////////////////////////////////////////////////////////
		// Compute rect size
		W = BigInteger.ZERO;
		H = BigInteger.ZERO;
		{
			for (int x = 0; x < n; x++) {
				if (matrixBottom[x] == 0) {
					W = W.add(A[x]);
				}
			}
			int level = 0, x = 0;
			while (level < levelsCount) {
				for (; x < n; x++) {
					if (matrixBottom[x] == level) {
						H = H.add(A[x]);
						level = matrixTop[x];
					}
				}
			}
			if (W.compareTo(H) < 0) {
				return;
			}
		}

		// check vertical results split
		for (int level = 1; level < levelsCount; level++) {
			BigInteger sum = BigInteger.ZERO;
			for (int x = 0; x < n; x++) {
				if (matrix[level][x].signum() > 0) {
					sum = sum.add(A[x]);
				}
			}
			if (sum.equals(W)) {
				return;
			}
			// assert sum.compareTo(W) == -1;
		}

		////////////////////////////////////////////////////////////////////////
		// Show result
		resultsCount++;
		// System.out.println("---------- [" + n + " / " + resultsCount + "] levelsCount: " + levelsCount + " ----------");
		// printMatrix(matrixBottom, matrixTop, levelsCount);
		// System.out.println();
		// System.out.print("Answer: ");
		// for (int i = 0; i < A.length; i++) {
		// 	if (i > 0) {
		// 		System.out.print(", ");
		// 	}
		// 	System.out.print(A[i]);
		// }
		// System.out.println();
		//		printMatrix(m, n_minus_1 - 1);
		//		System.out.println();

		Collection<Result> results = getResult(levelsCount, A);
		for (Result result : results) {
			Result rotatedResult = result.rotate();
			if (this.results.contains(rotatedResult)) {
				continue;
			}
			rotatedResult = rotatedResult.rotate();
			if (this.results.contains(rotatedResult)) {
				continue;
			}
			rotatedResult = rotatedResult.rotate();
			if (this.results.contains(rotatedResult)) {
				continue;
			}
			rotatedResult = result.flipHor();
			if (this.results.contains(rotatedResult)) {
				continue;
			}
			rotatedResult = rotatedResult.rotate();
			if (this.results.contains(rotatedResult)) {
				continue;
			}
			rotatedResult = rotatedResult.rotate();
			if (this.results.contains(rotatedResult)) {
				continue;
			}
			rotatedResult = rotatedResult.rotate();
			if (this.results.contains(rotatedResult)) {
				continue;
			}

			this.results.add(result);
			System.out.println("[" + this.results.size() + "] " + result);
		}

		//		if (this.results.size() > 30) {
		//			throw new RuntimeException();
		//		}

		// System.out.println("Size: " + W + " x " + H);
		// System.out.println();
		//		System.exit(0);
	}

	Set<Result> results = new HashSet<>();

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

	static int C;
	static int NC;

	int processVerticalChains(int x, BigInteger[][] matrix, int levelsCount, int level, int index, BigInteger[] buf) {
		for (; x < n; x++) {
			if (matrixBottom[x] == level) {
				buf[x] = BigInteger.ONE;
				int nextLevel = matrixTop[x];
				if (nextLevel < levelsCount) {
					index = processVerticalChains(x, matrix, levelsCount, nextLevel, index, buf);
					if (index == -1) {
						return -1;
					}
				} else {
					if (index < n) {
						BigInteger[] levelArray = matrix[index];
						for (int x0 = 0; x0 < n; x0++) {
							levelArray[x0] = buf[x0];
						}
					} else {
						for (int l = levelsCount - 1; l < n; l++) {
							BigInteger[] levelArray = matrix[l];
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

	Collection<Result> getResult(int levelsCount, BigInteger[] quadsSizes) {
		int[] quadIndex = new int[n];
		for (int x = 0; x < n; x++) {
			quadIndex[x] = -1;
		}
		Collection<Result> results = new HashSet<>();
		getResult(0, levelsCount, quadsSizes, new boolean[n], quadIndex, results);
		return results;
	}

	void getResult(int level, int levelsCount, BigInteger[] quadsSizes, boolean busy[], int[] quadIndex, Collection<Result> results) {
		int qx = -1;
		for (int x = 0; x < n; x++) {
			if (matrixBottom[x] == level && quadIndex[x] == -1) {
				qx = x;
				break;
			}
		}
		if (qx != -1) {
			for (int x = 0; x < n; x++) {
				if (matrixBottom[x] == level && !busy[x]) {
					busy[x] = true;
					quadIndex[qx] = x;
					getResult(level, levelsCount, quadsSizes, busy, quadIndex, results);
					busy[x] = false;
				}
			}
			quadIndex[qx] = -1;
		} else if (level < levelsCount) {
			getResult(level + 1, levelsCount, quadsSizes, busy, quadIndex, results);
		} else {
			Quad[] quads = new Quad[n];
			for (int x = 0; x < n; x++) {
				quads[x] = new Quad();
			}
			for (int l = 0; l <= levelsCount; l++) {
				int _x = 0, _y = 0;
				if (l > 0) {
					for (int x = 0; x < n; x++) {
						int x1 = quadIndex[x];
						if (matrixTop[x1] == l) {
							if (_x == 0 || _x > quads[x].x) {
								_x = quads[x].x;
								_y = quads[x].y + quads[x].size;
							}
						}
					}
				}
				for (int x = 0; x < n; x++) {
					int x1 = quadIndex[x];
					if (matrixBottom[x1] == l) {
						quads[x].set(_x, _y, quadsSizes[x1].intValue(), x);
						_x += quads[x].size;
					}
				}
			}
			Result result = new Result(quads, W.intValue(), H.intValue());
			if (result.isValid()) {
				results.add(result);
			}
		}
	}

	void printMatrix(int[] matrixBottom, int[] matrixTop, int levelsCount) {
		for (int l = 0; l <= levelsCount; l++) {
			for (int x = 0; x < n; x++) {
				if (matrixBottom[x] == l) {
					System.out.print(" 1 ");
				} else if (matrixTop[x] == l) {
					System.out.print("-1 ");
				} else {
					System.out.print(" 0 ");
				}
			}
			System.out.println();
		}
	}

	static boolean paused;

	public static void main(String[] args) throws InterruptedException {
		long t1 = System.currentTimeMillis();
		int N1 = 13;
		int N2 = 13;
		List<Result> results = new ArrayList<>();
		for (int i = N1; i <= N2; i++) {
			long t2 = System.currentTimeMillis();
			QSM q = new QSM(i);
			try {
				q.start();
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
			results.addAll(q.results);
			System.out.println("\nchecks: " + q.resultsCount + " for " + (System.currentTimeMillis() - t2) / 1000.0 + "sec");
		}
		System.out.println("TIME: " + (System.currentTimeMillis() - t1) / 1000.0 + "sec");
		System.out.println("NC: " + NC + " / " + C);

		//		QFrame frame = new QFrame(10);
		//		frame.setTitle("Quads: " + N1 + " - " + N2);
		//		frame.setVisible(true);
		//
		//		int index = 0;
		//		while (index < results.size()) {
		//			for (int n = 0; n < 10 && index < results.size(); n++) {
		//				Result result = results.get(index);
		//				QFrame.QPanel panel = frame.getQPanel(n);
		//				panel.setResult(result);
		//				index++;
		//			}
		//			paused = true;
		//			while (paused) {
		//				Thread.sleep(100);
		//				for (QFrame.QPanel panel : frame.panels) {
		//					if (panel.paused) {
		//						paused = false;
		//						panel.paused = false;
		//					}
		//				}
		//			}
		//		}
	}
}
