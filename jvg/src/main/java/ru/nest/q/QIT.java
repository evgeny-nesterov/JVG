package ru.nest.q;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QIT {
	static Set<Result> allResults = new LinkedHashSet<>();

	QIT(int X, int fromX, int toX, Set<Result> results) {
		this.X = X;
		this.fromX = fromX;
		this.toX = toX;
		this.results = results;
		front = new int[X];
		busy = new boolean[X + 1];
		sequence = new int[X];
		levels = new Level[X / 3];
		for (int i = 0; i < levels.length; i++) {
			levels[i] = new Level();
		}
		levelsCount = levels.length - 1;
		firstLevel = levels[levelsCount--];
		firstLevel.x2 = X;
		levelsX = new Level[X];
		levelsX[0] = firstLevel;
		bestLevel = firstLevel;
	}

	int X, fromX, toX;

	boolean[] busy;

	int[] sequence;

	int n;

	Set<Result> results;

	int iterationsCount = 0;

	long startTime = System.currentTimeMillis();

	Level[] levels;

	Level[] levelsX;

	int levelsCount;

	Level firstLevel;

	Level bestLevel;

	Level stopTL = new Level();
	Level stopTR = new Level();

	class Level {
		int x1, x2, y;

		Level prev, next;

		Level addLevel(int size, boolean checkTop) {
			int newX1 = x1 + size;
			int newY = y + size;

			if (checkTop) {
				int firstQuad = sequence[0];
				if (x1 > 0 && x1 <= firstQuad && X - newY <= firstQuad) {
					return stopTL;
				}
				if (newX1 < X && X - newX1 <= firstQuad && X - newY <= firstQuad) {
					return stopTR;
				}
			}

			boolean prevNotSame = prev == null || prev.y != newY;
			if (newX1 != x2) {
				if (prevNotSame) {
					Level l = levels[levelsCount--];
					l.x1 = x1;
					l.x2 = newX1;
					l.y = newY;
					l.prev = prev;
					l.next = this;
					if (x1 == 0) {
						firstLevel = l;
					}
					if (prev != null) {
						prev.next = l;
						if (prev.y < newY && prev.x2 - prev.x1 <= x2 - newX1 && (prev.prev == null || prev.prev.y > prev.y)) {
							bestLevel = prev;
						} // else assume bestLevel = this
					}
					levelsX[x1] = l;

					x1 = newX1;
					prev = l;
					levelsX[x1] = this;
					return l;
				} else {
					prev.x2 = newX1;
					x1 = newX1;
					levelsX[x1] = this;
					// assume bestLevel = this;
					return this;
				}
			} else {
				boolean nextNotSame = next == null || next.y != newY;
				if (prevNotSame && nextNotSame) {
					y = newY;
					// assume bestLevel = this;
					if ((prev != null && prev.y < y) || (next != null && next.y < y)) {
						bestLevel = null; // full search
					}
					return this;
				} else if (prevNotSame) {
					next.x1 = x1;
					next.prev = prev;
					if (prev != null) {
						prev.next = next;
					}
					levels[++levelsCount] = this;
					if (x1 == 0) {
						firstLevel = next;
					}
					levelsX[x1] = next;
					bestLevel = null; // full search
					return next;
				} else if (nextNotSame) {
					prev.x2 = x2;
					prev.next = next;
					if (next != null) {
						next.prev = prev;
					}
					levels[++levelsCount] = this;
					bestLevel = null; // full search
					return prev;
				} else {
					prev.x2 = next.x2;
					prev.next = next.next;
					if (next.next != null) {
						next.next.prev = prev;
					}
					levels[++levelsCount] = this;
					levels[++levelsCount] = next;
					bestLevel = null; // full search
					return prev;
				}
			}
		}

		void removeLevel(int lx1, int lx2, int size) {
			int newY = y - size;
			if (lx1 == x1 && lx2 == x2) {
				y = newY;
				if (next != null && next.y == y) {
					x2 = next.x2;
					if (next.next != null) {
						next.next.prev = this;
					}
					levels[++levelsCount] = next;
					next = next.next;
				}
			} else if (lx2 == x2) {
				if (next != null && next.y == newY) {
					x2 = lx1;
					next.x1 = lx1;
					levelsX[lx1] = next;
				} else {
					Level l = levels[levelsCount--];
					l.x1 = lx1;
					l.x2 = lx2;
					l.y = newY;
					l.prev = this;
					l.next = next;
					if (next != null) {
						next.prev = l;
					}
					levelsX[lx1] = l;

					next = l;
					x2 = lx1;
				}
			} else if (lx1 == x1) {
				Level l = levels[levelsCount--];
				l.x1 = lx1;
				l.x2 = lx2;
				l.y = newY;
				l.prev = prev;
				if (prev != null) {
					prev.next = l;
				}
				l.next = this;
				if (lx1 == 0) {
					firstLevel = l;
				}
				levelsX[lx1] = l;

				x1 = lx2;
				prev = l;
				levelsX[x1] = this;
			} else {
				Level l2 = levels[levelsCount--];
				l2.x1 = lx2;
				l2.x2 = x2;
				l2.y = y;
				l2.next = next;
				if (next != null) {
					next.prev = l2;
				}
				levelsX[lx2] = l2;

				Level l1 = levels[levelsCount--];
				l1.x1 = lx1;
				l1.x2 = lx2;
				l1.y = newY;
				l1.prev = this;
				l1.next = l2;
				l2.prev = l1;
				levelsX[lx1] = l1;

				x2 = lx1;
				next = l1;
			}
		}

		Level getPrevLevel(int x) {
			Level l = this.prev;
			while (l != null) {
				if (x >= l.x1 && x < l.x2) {
					return l;
				}
				l = l.prev;
			}
			return this.prev;
		}

		public String toString() {
			return y + " (" + x1 + "-" + x2 + ")";
		}
	}

	void printProcess() {
//		if (++iterationsCount % 10_000_000 == 0) {
//			System.out.print(X + " [" + (System.currentTimeMillis() - startTime) / 1000 + "sec]: results=" + results.size() + ", sequence=");
//			for (int i = 0; i < n; i++) {
//				System.out.print((i > 0 ? ", " : "") + sequence[i]);
//			}
//			System.out.println();
//		}
		if (panel != null && ++iterationsCount % 1_000_000 == 0) {
			panel.setSequence(X, sequence, n);
			waitClick();
		}
	}

	void removeLevel(int levelX1, int levelX2, int size) {
		Level l = firstLevel;
		while (true) {
			if (levelX2 <= l.x2 && levelX1 >= l.x1) {
				l.removeLevel(levelX1, levelX2, size);
				return;
			}
			l = l.next;
		}
	}

	void printLevels() {
		System.out.print("Levels: ");
		Level l = firstLevel;
		while (l != null) {
			if (l != firstLevel) {
				System.out.print(", ");
			}
			System.out.print(l);
			l = l.next;
		}
		System.out.println();
	}

	void start() {
		startLevel(0, 0, X);
	}

	void startLevel(int level, int levelX1, int levelX2) {
		int startQuad = 1;
		int endQuad = levelX2 - levelX1;
		if (level > 0) {
			if (level + endQuad > X) {
				endQuad = X - level;
			}
		} else if (n > 0) {
			if (levelX2 == X && endQuad <= sequence[0]) { // level == 0
				return;
			}
		} else {
			startQuad = fromX;
			endQuad = toX;
		}
		for (int quad = startQuad; quad <= endQuad; quad++) {
			if (!busy[quad]) {
				bestLevel = levelsX[levelX1];
				Level currentLevel = bestLevel.addLevel(quad, true);
				if (currentLevel == stopTL) {
					break;
				} else if (currentLevel == stopTR) {
					if (levelX2 < X || level + levelX2 - levelX1 > X || busy[endQuad]) {
						break;
					}
					quad = endQuad;
					currentLevel = bestLevel.addLevel(quad, false);
				}
				busy[quad] = true;
				sequence[n++] = quad;

				printProcess();
				// printLevels();

				int x2 = levelX1 + quad;
				if (x2 != levelX2 && (levelX1 == 0 || currentLevel.prev.y >= level + quad)) { // 50%
					startLevel(level, x2, levelX2);
				} else if (firstLevel.next != null) {
					if (bestLevel == null) {  // 20%
						Level l = firstLevel;
						if ((l.prev == null || l.prev.y > l.y) && l.next.y > l.y) {
							bestLevel = l;
						}
						l = l.next;
						if (bestLevel == null) {
							while (true) {
								if (l.prev.y > l.y && (l.next == null || l.next.y > l.y)) {
									bestLevel = l;
									l = l.next;
									break;
								}
								l = l.next;
							}
						}
						while (l != null) {
							if (l.x2 - l.x1 < bestLevel.x2 - bestLevel.x1 && l.prev.y > l.y && (l.next == null || l.next.y > l.y)) {
								bestLevel = l;
							}
							l = l.next;
						}
					}
					startLevel(bestLevel.y, bestLevel.x1, bestLevel.x2);
				} else {
					result(firstLevel.y);
				}

				removeLevel(levelX1, x2, quad);
				busy[quad] = false;
				n--;
			}
		}
	}

	// artifact
	int[] front;

	void startLevelSimple(int level, int levelX1, int levelX2) {
		int levelLength = levelX2 - levelX1;
		if (level == 0 && levelX2 == X && levelLength < sequence[0]) {
			return;
		}
		for (int quad = 1; quad <= levelLength; quad++) {
			if (!busy[quad]) {
				busy[quad] = true;
				int x2 = levelX1 + quad, newLevel = level + quad;
				for (int x = levelX1; x < x2; x++) {
					front[x] = newLevel;
				}
				sequence[n++] = quad;

				// printProcess();

				if (x2 != levelX2 && (levelX1 == 0 || front[levelX1 - 1] >= newLevel)) {
					startLevel(level, x2, levelX2);
				} else {
					//--- find level -----------------------------
					int currentLevel = front[0], bestLevel = currentLevel, bestLevelX1 = 0, bestLevelX2 = X, lx1 = 0, lx2 = 0, ly;
					while (lx2 < X) {
						ly = front[lx2];
						while (++lx2 < X && (ly = front[lx2]) == currentLevel) ;
						if (ly > currentLevel || lx2 == X) {
							if (lx2 - lx1 < bestLevelX2 - bestLevelX1) {
								bestLevel = currentLevel;
								bestLevelX1 = lx1;
								bestLevelX2 = lx2;
							}
							while (++lx2 < X && (ly = front[lx2]) >= currentLevel) {
								currentLevel = ly;
							}
						}
						currentLevel = ly;
						lx1 = lx2;
					}

					if (bestLevelX1 == 0 && bestLevelX2 == X) {
						if (bestLevel >= X) {
							result(front[0]);
						}
					} else {
						startLevel(bestLevel, bestLevelX1, bestLevelX2);
					}
					//--------------------------------------------
				}

				for (int x = levelX1; x < x2; x++) {
					front[x] = level;
				}
				busy[quad] = false;
				n--;
			}
		}
	}

	void result(int height) {
		if (isScaled()) {
			return;
		}
		Result result1 = getResult(height);
		if (result1.isVertical()) {
			return;
		}
		Result result2 = result1.flipHor();
		if (allResults.contains(result2)) {
			return;
		}
		Result result3 = result1.flipVer();
		if (allResults.contains(result3)) {
			return;
		}
		Result result4 = result1.flipHorVer();
		if (allResults.contains(result4)) {
			return;
		}
		if (result1.width == result1.height) {
			Result result5 = result1.rotate();
			if (allResults.contains(result5)) {
				return;
			}
			Result result6 = result5.flipHor();
			if (allResults.contains(result6)) {
				return;
			}
			Result result7 = result5.flipVer();
			if (allResults.contains(result7)) {
				return;
			}
			Result result8 = result5.flipHorVer();
			if (allResults.contains(result8)) {
				return;
			}
		}
		Result bestResult = result1;
		if (result2.getFirstQuadSize() < bestResult.getFirstQuadSize()) {
			bestResult = result2;
		}
		if (result3.getFirstQuadSize() < bestResult.getFirstQuadSize()) {
			bestResult = result3;
		}
		if (result4.getFirstQuadSize() < bestResult.getFirstQuadSize()) {
			bestResult = result4;
		}
		results.add(bestResult);
		allResults.add(bestResult);

		if (panel != null) {
			System.out.println(bestResult);
			panel.paused = true;
			waitClick();
		}
	}

	void waitClick() {
		if (panel.paused) {
			panel.setSequence(X, sequence, n);
			while (panel.paused) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	boolean isScaled() {
		int nod = sequence[0];
		for (int i = 1; i < n && nod > 1; i++) {
			nod = nod(nod, sequence[i]);
		}
		return nod > 1;
	}

	int nod(int a, int b) {
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

	Result getResult(int height) {
		Quad[] quads = new Quad[n];
		int x1 = 0, x2, y = 0, index = 0;
		int[] front = new int[X];
		while (index < n) {
			Quad quad = new Quad(x1, y, sequence[index], index);
			quads[index++] = quad;
			for (int i = quad.x; i < quad.x + quad.size; i++) {
				front[i] += quad.size;
			}

			int lx1 = 0, lx2 = 0, ly, currentLevel = front[0];
			x1 = 0;
			x2 = X;
			y = currentLevel;
			while (lx2 < X) {
				ly = front[lx2];
				while (++lx2 < X && (ly = front[lx2]) == currentLevel) ;
				if (ly > currentLevel || lx2 == X) {
					if (lx2 - lx1 < x2 - x1) {
						y = currentLevel;
						x1 = lx1;
						x2 = lx2;
					}
					while (++lx2 < X && (ly = front[lx2]) >= currentLevel) {
						currentLevel = ly;
					}
				}
				currentLevel = ly;
				lx1 = lx2;
			}
		}
		return new Result(quads, X, height, System.currentTimeMillis() - startTime);
	}

	static void print(Collection<Result> results, long startTime, long quadStartTime) {
		if (results.size() > 0) {
			int index = 1;
			for (Result result : results) {
				System.out.println("// [" + (index < 10 ? "0" : "") + index++ + "] " + result);
			}
			System.out.println("// time: " + (System.currentTimeMillis() - quadStartTime) / 1000.0 + "sec, total time: " + (System.currentTimeMillis() - startTime) / 1000.0 + "sec\n//");
		}
	}

	QFrame.QPanel panel;

	static void start(int startQuad, int endQuad, int threads) {
		QFrame frame = new QFrame();
		frame.setQuadsCount(threads);
		frame.setVisible(true);

		long startTime = System.currentTimeMillis();
		if (threads > 1) {
			boolean[] threadsBusy = new boolean[threads];
			for (int n = startQuad; n <= endQuad; n++) {
				frame.setTitle("Quads: " + n);
				Set<Result> results = ConcurrentHashMap.newKeySet();
				long quadStartTime = System.currentTimeMillis();
				int _n = n;
				ExecutorService executor = Executors.newFixedThreadPool(threads);
				int maxN = n / 2;
				if (n % 2 == 0) {
					maxN--;
				}
				for (int quad = 3; quad <= maxN; quad++) {
					int _quad = quad;
					executor.execute(() -> {
						int index = 0;
						synchronized (threadsBusy) {
							WHILE:
							while (true) {
								for (int i = 0; i < threadsBusy.length; i++) {
									if (!threadsBusy[i]) {
										threadsBusy[i] = true;
										index = i;
										break WHILE;
									}
								}
								Thread.yield();
							}
						}

						QIT q = new QIT(_n, _quad, _quad, results);
						q.panel = frame.getQPanel(index);
						q.start();
						q.panel.clear();

						synchronized (threadsBusy) {
							threadsBusy[index] = false;
						}
					});
				}
				executor.shutdown();
				try {
					executor.awaitTermination(Integer.MAX_VALUE, java.util.concurrent.TimeUnit.DAYS);
				} catch (InterruptedException e) {
				}
				print(results, startTime, quadStartTime);
			}
		} else {
			for (int n = startQuad; n <= endQuad; n++) {
				Set<Result> results = new LinkedHashSet<>();
				QIT q = new QIT(n, 1, n, results);
				q.start();
				print(results, startTime, q.startTime);
			}
		}
	}

	public static void main(String[] args) {
		start(32, 171, 8); // 106
	}
}

// 33-32 (9)	0.007sec	18 15x14, 15 0x17, 14 19x0, 10 9x0, 9 0x0, 8 0x9, 7 8x10, 4 15x10, 1 8x9
// time: 0.037sec, total time: 0.037sec
//
// 57-55 (10)	0.012sec	30 27x25, 27 0x28, 25 32x0, 17 15x0, 15 0x0, 13 0x15, 11 13x17, 8 24x17, 3 24x25, 2 13x15
// time: 0.033sec, total time: 0.425sec
//
// 65-32 (10)	0.013sec	32 33x0, 18 15x14, 15 0x17, 14 19x0, 10 9x0, 9 0x0, 8 0x9, 7 8x10, 4 15x10, 1 8x9
// 65-47 (10)	0.024sec	25 0x22, 24 41x0, 23 42x24, 22 0x0, 19 22x0, 17 25x30, 11 25x19, 6 36x24, 5 36x19, 3 22x19
// 65-33 (10)	0.001sec	33 32x0, 18 0x15, 15 0x0, 14 18x19, 10 22x9, 9 23x0, 8 15x0, 7 15x8, 4 18x15, 1 22x8
// 65-32 (10)	0.011sec	32 33x0, 18 0x14, 15 18x17, 14 0x0, 10 14x0, 9 24x0, 8 25x9, 7 18x10, 4 14x10, 1 24x9
// 65-33 (10)	0.005sec	33 32x0, 18 14x15, 15 17x0, 14 0x19, 10 0x9, 9 0x0, 8 9x0, 7 10x8, 4 10x15, 1 9x8
// time: 0.141sec, total time: 1.108sec
//
// 69-61 (9)	0.021sec	36 0x25, 33 36x28, 28 41x0, 25 0x0, 16 25x0, 9 25x16, 7 34x16, 5 36x23, 2 34x23
// time: 0.241sec, total time: 1.971sec
//
// 75-73 (16)	0.121sec	41 34x32, 34 0x39, 32 43x0, 23 0x0, 20 23x0, 16 0x23, 12 31x20, 9 16x30, 8 23x20, 7 16x23, 6 25x28, 5 25x34, 4 30x35, 3 31x32, 2 23x28, 1 30x34
// time: 0.63sec, total time: 4.862sec
//
// 79-65 (20)	0.236sec	32 0x33, 25 32x40, 24 55x0, 23 32x0, 22 57x43, 19 60x24, 18 0x0, 17 32x23, 15 0x18, 14 18x0, 11 49x29, 10 22x14, 9 23x24, 8 15x25, 7 15x18, 6 49x23, 5 55x24, 4 18x14, 3 57x40, 1 22x24
// 79-65 (20)	0.296sec	32 0x33, 25 54x40, 24 32x0, 23 56x0, 22 32x43, 19 32x24, 18 14x0, 17 62x23, 15 17x18, 14 0x0, 11 51x29, 10 0x14, 9 0x24, 8 9x25, 7 10x18, 6 56x23, 5 51x24, 4 10x14, 3 51x40, 1 9x24
// 79-65 (20)	0.235sec	32 0x33, 25 54x0, 24 32x41, 23 56x42, 22 32x0, 19 32x22, 18 0x0, 17 62x25, 15 0x18, 14 18x0, 11 51x25, 10 22x14, 9 23x24, 8 15x25, 7 15x18, 6 56x36, 5 51x36, 4 18x14, 3 51x22, 1 22x24
// 79-65 (20)	0.005sec	32 0x33, 25 32x40, 24 55x0, 23 32x0, 22 57x43, 19 60x24, 18 14x15, 17 32x23, 15 17x0, 14 0x19, 11 49x29, 10 0x9, 9 0x0, 8 9x0, 7 10x8, 6 49x23, 5 55x24, 4 10x15, 3 57x40, 1 9x8
// 79-65 (20)	0.296sec	32 0x33, 25 32x0, 24 55x41, 23 32x42, 22 57x0, 19 60x22, 18 14x0, 17 32x25, 15 17x18, 14 0x0, 11 49x25, 10 0x14, 9 0x24, 8 9x25, 7 10x18, 6 49x36, 5 55x36, 4 10x14, 3 57x22, 1 9x24
// 79-65 (20)	0.006sec	32 0x33, 25 32x0, 24 55x41, 23 32x42, 22 57x0, 19 60x22, 18 0x15, 17 32x25, 15 0x0, 14 18x19, 11 49x25, 10 22x9, 9 23x0, 8 15x0, 7 15x8, 6 49x36, 5 55x36, 4 18x15, 3 57x22, 1 22x8
// 79-65 (20)	0.005sec	32 0x33, 25 54x0, 24 32x41, 23 56x42, 22 32x0, 19 32x22, 18 14x15, 17 62x25, 15 17x0, 14 0x19, 11 51x25, 10 0x9, 9 0x0, 8 9x0, 7 10x8, 6 56x36, 5 51x36, 4 10x15, 3 51x22, 1 9x8
// 79-65 (20)	0.295sec	32 0x33, 25 54x0, 24 32x41, 23 56x42, 22 32x0, 19 32x22, 18 14x0, 17 62x25, 15 17x18, 14 0x0, 11 51x25, 10 0x14, 9 0x24, 8 9x25, 7 10x18, 6 56x36, 5 51x36, 4 10x14, 3 51x22, 1 9x24
// 79-65 (20)	0.006sec	32 0x33, 25 54x40, 24 32x0, 23 56x0, 22 32x43, 19 32x24, 18 14x15, 17 62x23, 15 17x0, 14 0x19, 11 51x29, 10 0x9, 9 0x0, 8 9x0, 7 10x8, 6 56x23, 5 51x24, 4 10x15, 3 51x40, 1 9x8
// 79-74 (16)	0.008sec	45 34x29, 34 0x40, 29 50x0, 23 0x17, 17 0x0, 15 35x14, 14 36x0, 12 23x17, 11 23x29, 10 26x0, 9 17x0, 8 17x9, 7 25x10, 4 32x10, 3 32x14, 1 25x9
// 79-65 (20)	0.005sec	32 0x33, 25 32x40, 24 55x0, 23 32x0, 22 57x43, 19 60x24, 18 0x15, 17 32x23, 15 0x0, 14 18x19, 11 49x29, 10 22x9, 9 23x0, 8 15x0, 7 15x8, 6 49x23, 5 55x24, 4 18x15, 3 57x40, 1 22x8
// 79-65 (20)	0.005sec	32 0x33, 25 54x0, 24 32x41, 23 56x42, 22 32x0, 19 32x22, 18 0x15, 17 62x25, 15 0x0, 14 18x19, 11 51x25, 10 22x9, 9 23x0, 8 15x0, 7 15x8, 6 56x36, 5 51x36, 4 18x15, 3 51x22, 1 22x8
// 79-65 (20)	0.295sec	32 0x33, 25 32x40, 24 55x0, 23 32x0, 22 57x43, 19 60x24, 18 14x0, 17 32x23, 15 17x18, 14 0x0, 11 49x29, 10 0x14, 9 0x24, 8 9x25, 7 10x18, 6 49x23, 5 55x24, 4 10x14, 3 57x40, 1 9x24
// 79-65 (20)	0.25sec	32 0x33, 25 32x0, 24 55x41, 23 32x42, 22 57x0, 19 60x22, 18 0x0, 17 32x25, 15 0x18, 14 18x0, 11 49x25, 10 22x14, 9 23x24, 8 15x25, 7 15x18, 6 49x36, 5 55x36, 4 18x14, 3 57x22, 1 22x24
// 79-65 (20)	0.006sec	32 0x33, 25 32x0, 24 55x41, 23 32x42, 22 57x0, 19 60x22, 18 14x15, 17 32x25, 15 17x0, 14 0x19, 11 49x25, 10 0x9, 9 0x0, 8 9x0, 7 10x8, 6 49x36, 5 55x36, 4 10x15, 3 57x22, 1 9x8
// 79-65 (20)	0.006sec	32 0x33, 25 54x40, 24 32x0, 23 56x0, 22 32x43, 19 32x24, 18 0x15, 17 62x23, 15 0x0, 14 18x19, 11 51x29, 10 22x9, 9 23x0, 8 15x0, 7 15x8, 6 56x23, 5 51x24, 4 18x15, 3 51x40, 1 22x8
// 79-65 (20)	0.238sec	32 0x33, 25 54x40, 24 32x0, 23 56x0, 22 32x43, 19 32x24, 18 0x0, 17 62x23, 15 0x18, 14 18x0, 11 51x29, 10 22x14, 9 23x24, 8 15x25, 7 15x18, 6 56x23, 5 51x24, 4 18x14, 3 51x40, 1 22x24
// time: 1.378sec, total time: 9.45sec
//
// 81-80 (12)	0.422sec	44 37x36, 37 0x43, 36 45x0, 23 22x0, 22 0x0, 21 0x22, 13 32x23, 11 21x23, 9 21x34, 7 30x36, 2 30x34, 1 21x22
// time: 2.012sec, total time: 13.372sec
//
// 82-75 (17)	0.396sec	43 39x32, 39 0x36, 32 50x0, 19 0x17, 18 32x0, 17 0x0, 15 17x0, 14 36x18, 11 19x25, 10 19x15, 7 29x18, 6 30x25, 5 30x31, 4 35x32, 3 29x15, 2 17x15, 1 35x31
// time: 2.033sec, total time: 15.405sec
//
// 83-77 (14)	0.33sec	43 40x34, 40 0x37, 34 49x0, 25 24x0, 24 0x0, 13 0x24, 12 28x25, 9 40x25, 8 20x29, 7 13x30, 6 13x24, 5 19x24, 4 24x25, 1 19x29
// time: 2.377sec, total time: 17.782sec
//
// 84-60 (17)	0.992sec	33 28x27, 28 0x32, 27 36x0, 23 61x37, 21 63x0, 19 17x0, 17 0x0, 16 68x21, 15 0x17, 13 15x19, 8 28x19, 7 61x30, 5 63x21, 4 64x26, 3 61x27, 2 15x17, 1 63x26
// time: 2.947sec, total time: 20.729sec
//
// 88-74 (22)	1.108sec	36 0x38, 28 60x0, 27 61x47, 25 36x49, 23 37x0, 20 0x0, 19 69x28, 18 0x20, 17 20x0, 15 31x23, 14 46x23, 13 18x25, 12 47x37, 11 36x38, 10 59x37, 9 60x28, 8 23x17, 6 31x17, 5 18x20, 3 20x17, 2 59x47, 1 46x37
// 88-65 (18)	1.503sec	33 55x0, 32 56x33, 31 25x34, 25 0x40, 20 17x0, 18 37x0, 17 0x0, 16 39x18, 14 25x20, 13 0x27, 12 13x28, 10 0x17, 8 17x20, 7 10x17, 4 13x24, 3 10x24, 2 37x18, 1 55x33
// time: 6.0sec, total time: 39.103sec
//
// 89-71 (20)	1.694sec	38 51x33, 33 56x0, 28 0x43, 23 28x48, 22 0x21, 21 0x0, 20 21x0, 17 22x20, 15 41x0, 12 39x36, 11 28x37, 10 46x15, 9 39x27, 8 48x25, 7 39x20, 6 22x37, 5 41x15, 3 48x33, 2 46x25, 1 21x20
// 89-80 (22)	0.898sec	33 0x47, 32 57x48, 27 37x0, 26 0x21, 25 64x0, 24 33x56, 23 66x25, 21 0x0, 16 21x0, 15 39x27, 14 43x42, 13 26x27, 12 54x27, 11 26x16, 10 33x46, 9 57x39, 7 26x40, 6 33x40, 5 21x16, 4 39x42, 3 54x39, 2 64x25
// time: 6.383sec, total time: 45.486sec
//
// 91-81 (18)	0.426sec	52 39x29, 39 0x42, 29 62x0, 27 0x0, 19 27x0, 16 46x0, 15 0x27, 13 49x16, 12 27x19, 11 28x31, 10 39x19, 8 15x27, 7 15x35, 6 22x36, 5 23x31, 4 23x27, 3 46x16, 1 22x35
// time: 8.891sec, total time: 62.553sec
//
// 94-63 (17)	6.784sec	36 58x27, 35 23x28, 28 19x0, 27 67x0, 23 0x40, 20 47x0, 19 0x0, 12 11x28, 11 0x29, 10 0x19, 9 10x19, 8 47x20, 7 60x20, 5 55x20, 3 55x25, 2 58x25, 1 10x28
// time: 14.978sec, total time: 101.359sec
//
// 97-65 (11)	1.73sec	65 32x0, 32 0x33, 18 14x0, 15 17x18, 14 0x0, 10 0x14, 9 0x24, 8 9x25, 7 10x18, 4 10x14, 1 9x24
// 97-65 (11)	0.055sec	65 32x0, 32 0x33, 18 14x15, 15 17x0, 14 0x19, 10 0x9, 9 0x0, 8 9x0, 7 10x8, 4 10x15, 1 9x8
// 97-96 (11)	1.541sec	56 41x40, 41 0x55, 40 57x0, 31 0x0, 26 31x0, 24 0x31, 17 24x38, 14 43x26, 12 31x26, 7 24x31, 2 41x38
// 97-94 (18)	5.159sec	54 43x0, 40 57x54, 31 0x63, 26 31x68, 24 0x39, 23 0x0, 20 23x0, 19 24x37, 17 26x20, 16 0x23, 14 43x54, 12 31x56, 10 16x23, 7 24x56, 6 16x33, 4 22x33, 3 23x20, 2 22x37
// 97-65 (11)	1.39sec	65 32x0, 32 0x33, 18 0x0, 15 0x18, 14 18x0, 10 22x14, 9 23x24, 8 15x25, 7 15x18, 4 18x14, 1 22x24
// 97-65 (11)	0.076sec	65 32x0, 32 0x33, 18 0x15, 15 0x0, 14 18x19, 10 22x9, 9 23x0, 8 15x0, 7 15x8, 4 18x15, 1 22x8
// time: 23.243sec, total time: 162.816sec
//
// 98-65 (11)	0.147sec	65 33x0, 33 0x32, 18 0x14, 15 18x17, 14 0x0, 10 14x0, 9 24x0, 8 25x9, 7 18x10, 4 14x10, 1 24x9
// 98-97 (13)	2.678sec	56 42x41, 42 0x55, 41 57x0, 29 0x0, 28 29x0, 26 0x29, 16 26x39, 13 44x28, 11 33x28, 7 26x32, 4 29x28, 3 26x29, 2 42x39
// 98-65 (11)	2.217sec	65 33x0, 33 0x32, 18 0x0, 15 18x0, 14 0x18, 10 14x22, 9 24x23, 8 25x15, 7 18x15, 4 14x18, 1 24x22
// 98-86 (11)	0.947sec	51 0x35, 47 51x39, 39 59x0, 35 0x0, 24 35x0, 11 35x24, 8 51x31, 7 52x24, 6 46x24, 5 46x30, 1 51x30
// 98-95 (11)	7.42sec	50 48x45, 48 0x47, 45 53x0, 28 25x0, 25 0x0, 22 0x25, 19 22x28, 12 41x28, 7 41x40, 5 48x40, 3 22x25
// 98-65 (11)	0.091sec	65 33x0, 33 0x32, 18 15x14, 15 0x17, 14 19x0, 10 9x0, 9 0x0, 8 0x9, 7 8x10, 4 15x10, 1 8x9
// 98-65 (11)	2.321sec	65 33x0, 33 0x32, 18 15x0, 15 0x0, 14 19x18, 10 9x22, 9 0x23, 8 0x15, 7 8x15, 4 15x18, 1 8x22
// time: 27.587sec, total time: 190.403sec
//
// 99-96 (19)	1.667sec	54 45x42, 45 0x51, 42 57x0, 28 0x23, 23 0x0, 20 37x0, 17 28x34, 14 23x0, 12 45x30, 11 28x23, 10 47x20, 9 23x14, 8 39x20, 6 39x28, 5 32x14, 4 32x19, 3 36x20, 2 45x28, 1 36x19
// 99-96 (12)	2.869sec	55 44x41, 44 0x52, 41 58x0, 31 0x0, 27 31x0, 21 0x31, 14 44x27, 13 31x27, 12 32x40, 11 21x41, 10 21x31, 1 31x40
// time: 33.883sec, total time: 224.286sec
//
// 100-88 (19)	16.524sec	47 53x0, 41 59x47, 36 0x52, 31 22x0, 23 36x65, 22 0x0, 21 17x31, 17 0x35, 15 38x31, 13 0x22, 12 47x53, 11 36x54, 9 13x22, 8 38x46, 7 46x46, 6 53x47, 4 13x31, 2 36x52, 1 46x53
// time: 40.365sec, total time: 264.651sec
//
// 101-91 (17)	0.006sec	52 49x39, 49 0x42, 39 62x0, 27 0x15, 23 39x0, 19 27x23, 16 46x23, 15 0x0, 12 27x11, 11 28x0, 8 15x7, 7 15x0, 6 22x0, 5 23x6, 4 23x11, 3 46x39, 1 22x6
// time: 44.659sec, total time: 309.31sec
//
// 103-72 (16)	0.437sec	43 0x29, 39 64x0, 33 70x39, 29 0x0, 27 43x45, 21 43x24, 15 29x0, 14 29x15, 13 51x11, 11 53x0, 9 44x0, 8 43x16, 7 44x9, 6 64x39, 2 51x9, 1 43x15
// 103-96 (16)	2.667sec	57 46x39, 46 0x50, 39 64x0, 26 0x0, 24 0x26, 22 24x28, 21 43x0, 18 46x21, 17 26x0, 11 26x17, 6 37x17, 5 37x23, 4 42x24, 3 43x21, 2 24x26, 1 42x23
// 103-74 (16)	21.059sec	41 62x33, 33 70x0, 32 0x42, 30 32x44, 27 23x0, 23 0x0, 20 50x0, 19 0x23, 17 34x27, 15 19x27, 13 57x20, 11 51x33, 7 50x20, 6 51x27, 4 19x23, 2 32x42
// time: 61.449sec, total time: 423.804sec
//
// 104-91 (21)	2.021sec	43 0x48, 32 72x28, 31 73x60, 30 43x61, 29 43x32, 28 76x0, 25 0x23, 23 0x0, 20 56x0, 19 37x0, 18 25x30, 14 23x0, 13 43x19, 12 56x20, 11 32x19, 9 23x14, 8 68x20, 7 25x23, 5 32x14, 4 68x28, 1 72x60
// 104-78 (20)	14.775sec	41 63x37, 37 67x0, 34 0x44, 29 34x49, 24 0x0, 22 24x0, 21 46x0, 20 0x24, 17 34x32, 16 51x21, 14 20x30, 12 51x37, 10 34x22, 8 26x22, 7 44x25, 6 20x24, 4 47x21, 3 44x22, 2 24x22, 1 46x21
// 104-99 (18)	5.744sec	44 0x55, 36 68x0, 34 70x65, 31 0x24, 29 75x36, 28 31x27, 27 41x0, 26 44x73, 24 0x0, 18 44x55, 17 24x0, 16 59x36, 13 62x52, 10 31x17, 9 59x27, 8 62x65, 7 24x17, 3 59x52
// 104-78 (20)	14.716sec	41 63x0, 37 67x41, 34 0x44, 33 34x45, 24 0x0, 22 24x0, 20 0x24, 17 46x0, 16 47x29, 14 20x30, 13 34x32, 12 51x17, 10 34x22, 8 26x22, 7 44x22, 6 20x24, 5 46x17, 4 63x41, 3 44x29, 2 24x22
// 104-83 (19)	21.07sec	43 61x0, 40 64x43, 36 0x47, 34 27x0, 28 36x55, 27 0x0, 20 0x27, 13 20x34, 12 52x43, 11 41x34, 10 42x45, 9 52x34, 8 33x34, 7 20x27, 6 36x49, 5 33x42, 4 38x45, 3 38x42, 2 36x47
// time: 74.189sec, total time: 497.993sec
//
// 105-88 (17)	7.211sec	51 54x37, 37 68x0, 33 0x26, 29 0x59, 26 0x0, 25 29x63, 23 45x0, 21 33x42, 19 26x0, 14 54x23, 12 33x19, 11 33x31, 10 44x32, 9 45x23, 7 26x19, 4 29x59, 1 44x31
// 105-87 (18)	8.705sec	41 0x46, 40 65x0, 36 41x51, 28 77x59, 27 38x0, 25 0x21, 24 41x27, 21 0x0, 19 86x40, 17 21x0, 16 25x30, 13 25x17, 11 65x40, 10 76x40, 9 77x50, 4 21x17, 3 38x27, 1 76x50
// 105-104 (10)	7.243sec	60 45x44, 45 0x59, 44 61x0, 33 0x0, 28 33x0, 26 0x33, 19 26x40, 16 45x28, 12 33x28, 7 26x33
// 105-71 (16)	3.481sec	41 0x30, 36 69x0, 35 70x36, 30 0x0, 29 41x42, 20 49x0, 19 30x0, 15 41x27, 13 56x20, 11 30x19, 9 56x33, 8 41x19, 7 49x20, 5 65x37, 4 65x33, 1 69x36
// time: 88.85sec, total time: 586.844sec
//
// 106-105 (12)	9.235sec	60 46x45, 46 0x59, 45 61x0, 31 0x0, 30 31x0, 28 0x31, 18 28x41, 15 46x30, 11 35x30, 7 28x34, 4 31x30, 3 28x31
// 106-75 (14)	11.397sec	42 64x33, 36 28x39, 33 73x0, 28 0x47, 27 0x0, 24 49x0, 22 27x0, 20 0x27, 17 32x22, 15 49x24, 12 20x27, 9 64x24, 8 20x39, 5 27x22
// 106-99 (12)	12.607sec	58 48x41, 48 0x51, 41 65x0, 28 0x23, 24 41x0, 23 0x0, 20 28x31, 18 23x0, 17 48x24, 13 28x18, 7 41x24, 5 23x18
// 106-71 (14)	12.824sec	40 29x31, 37 69x34, 34 72x0, 31 41x0, 29 0x42, 23 0x0, 19 0x23, 18 23x0, 13 28x18, 10 19x32, 9 19x23, 5 23x18, 3 69x31, 1 28x31
// 106-99 (13)	19.438sec	55 51x44, 51 0x48, 44 62x0, 33 29x0, 29 0x0, 19 0x29, 15 36x33, 11 51x33, 10 19x29, 9 19x39, 8 28x40, 7 29x33, 1 28x39
// 106-82 (18)	0.232sec	57 0x25, 49 57x33, 33 73x0, 25 0x0, 17 56x0, 16 57x17, 13 25x12, 12 25x0, 11 38x14, 10 46x0, 9 37x0, 8 49x17, 7 49x10, 5 40x9, 4 45x10, 3 37x9, 2 38x12, 1 45x9
// 106-105 (15)	6.461sec	59 47x46, 47 0x58, 46 60x0, 35 0x23, 25 35x21, 23 0x0, 21 39x0, 16 23x0, 12 35x46, 7 23x16, 5 34x16, 4 30x16, 3 30x20, 2 33x21, 1 33x20
// time: 99.93sec, total time: 686.774sec
//
// 107-101 (19)	23.414sec	58 49x43, 49 0x52, 43 64x0, 23 19x0, 22 42x0, 21 43x22, 19 0x0, 18 0x34, 17 26x23, 15 0x19, 12 28x40, 11 15x23, 10 18x42, 9 40x43, 8 18x34, 4 15x19, 3 40x40, 2 26x40, 1 42x22
// 107-89 (23)	11.095sec	50 57x39, 39 68x0, 30 27x59, 27 0x62, 26 0x0, 25 32x34, 22 46x0, 20 26x0, 19 0x43, 17 0x26, 15 17x26, 14 32x20, 13 19x41, 12 46x22, 10 58x22, 8 19x54, 7 61x32, 6 26x20, 5 27x54, 4 57x35, 3 58x32, 2 17x41, 1 57x34
// time: 118.572sec, total time: 805.346sec
//
