package ru.nest;

import java.awt.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Q {
	static class Result {
		int width, height;

		Quad[] quads;

		long duration;

		Result(Quad[] quads, int width, int height, long duration) {
			Arrays.sort(quads);
			this.quads = quads;
			this.width = width;
			this.height = height;
			this.duration = duration;
		}

		public int getFirstQuadSize() {
			for (int i = 0; i < quads.length; i++) {
				if (quads[i].x == 0 && quads[i].y == 0) {
					return quads[i].size;
				}
			}
			return 0;
		}

		public boolean equals(Object o) {
			Result result = (Result) o;
			if (quads.length != result.quads.length) {
				return false;
			}
			for (int i = 0; i < quads.length; i++) {
				if (!quads[i].equals(result.quads[i])) {
					return false;
				}
			}
			return true;
		}

		public int hashCode() {
			int hashCode = 0;
			for (int i = 0; i < quads.length; i++) {
				hashCode = 37 * hashCode + quads[i].hashCode();
			}
			return hashCode;
		}

		public String toString() {
			String string = "// " + width + "-" + height + " (" + quads.length + ")\t" + (duration / 1000.0) + "sec\t";
			for (int i = 0; i < quads.length; i++) {
				if (i > 0) {
					string += ", ";
				}
				string += quads[i].toString();
			}
			return string;
		}

		Result flipHor() {
			Quad[] quads = new Quad[this.quads.length];
			for (int i = 0; i < this.quads.length; i++) {
				quads[i] = this.quads[i].flipHor(width);
			}
			computeSequence(quads, width);
			return new Result(quads, width, height, duration);
		}

		Result flipVer() {
			Quad[] quads = new Quad[this.quads.length];
			for (int i = 0; i < this.quads.length; i++) {
				quads[i] = this.quads[i].flipVer(height);
			}
			computeSequence(quads, width);
			return new Result(quads, width, height, duration);
		}

		Result flipHorVer() {
			Quad[] quads = new Quad[this.quads.length];
			for (int i = 0; i < this.quads.length; i++) {
				quads[i] = this.quads[i].flipHorVer(width, height);
			}
			computeSequence(quads, width);
			return new Result(quads, width, height, duration);
		}

		Result rotate() {
			Quad[] quads = new Quad[this.quads.length];
			for (int i = 0; i < this.quads.length; i++) {
				quads[i] = this.quads[i].rotate();
			}
			computeSequence(quads, width);
			return new Result(quads, width, height, duration);
		}

		static void computeSequence(Quad[] quads, int width) {
			int[] front = new int[width];
			Map<Point, Quad> map = new HashMap();
			for (int i = 0; i < quads.length; i++) {
				map.put(new Point(quads[i].x, quads[i].y), quads[i]);
			}
			int index = 0, x = 0, y = 0;
			do {
				Quad q = map.get(new Point(x, y));
				q.index = index++;
				for (int i = x; i < x + q.size; i++) {
					front[i] += q.size;
				}

				int currentLevel = front[0], bestLevel = currentLevel, bestLevelX1 = 0, bestLevelX2 = width, lx1 = 0, lx2 = 0, ly;
				while (lx2 < width) {
					ly = front[lx2];
					while (++lx2 < width && (ly = front[lx2]) == currentLevel)
						;
					if ((ly > currentLevel || lx2 == width) && lx2 - lx1 < bestLevelX2 - bestLevelX1) {
						bestLevel = currentLevel;
						bestLevelX1 = lx1;
						bestLevelX2 = lx2;
						while (++lx2 < width && (ly = front[lx2]) >= currentLevel) {
							currentLevel = ly;
						}
					}
					currentLevel = ly;
					lx1 = lx2;
				}

				x = bestLevelX1;
				y = bestLevel;
			} while (index < quads.length);
		}

		int[] getSequence() {
			int[] sequence = new int[width];
			for (int i = 0; i < quads.length; i++) {
				sequence[quads[i].index] = quads[i].size;
			}
			return sequence;
		}
	}

	static class Quad implements Comparable<Quad> {
		int x, y, size, index;

		Quad(int x, int y, int size, int index) {
			this.x = x;
			this.y = y;
			this.size = size;
			this.index = index;
		}

		public boolean equals(Object o) {
			Quad q = (Quad) o;
			return x == q.x && y == q.y && size == q.size;
		}

		public int hashCode() {
			return x + 37 * (y + 37 * size);
		}

		public String toString() {
			return size + " " + x + "x" + y;
		}

		public int compareTo(Quad quad) {
			return quad.size - size;
		}

		Quad flipHor(int width) {
			return new Quad(width - x - size, y, size, -1);
		}

		Quad flipVer(int height) {
			return new Quad(x, height - y - size, size, -1);
		}

		Quad flipHorVer(int width, int height) {
			return new Quad(width - x - size, height - y - size, size, -1);
		}

		Quad rotate() {
			return new Quad(y, x, size, -1);
		}
	}

	static class Q1 {
		static Set<Result> allResults = new LinkedHashSet<>();

		Q1(int X) {
			this.X = X;
			front = new int[X];
			busy = new boolean[X + 1];
			sequence = new int[X];
			levels = new L[X / 3];
			for (int i = 0; i < levels.length; i++) {
				levels[i] = new L();
			}
			levelsCount = levels.length - 1;
			firstLevel = levels[levelsCount--];
			firstLevel.x2 = X;
			levelsX = new L[X];
			levelsX[0] = firstLevel;
			bestLevel = firstLevel;
		}

		int X;

		int[] front;

		boolean[] busy;

		int[] sequence;

		int n;

		Set<Result> results = new LinkedHashSet<>();

		int iterationsCount = 0;

		long startTime = System.currentTimeMillis();

		L[] levels;

		L[] levelsX;

		int levelsCount;

		L firstLevel;

		L bestLevel;

		class L {
			int x1, x2, y;

			L prev, next;

			L addLevel(int size) {
				int newX1 = x1 + size;
				int newY = y + size;
				boolean prevNotSame = prev == null || prev.y != newY;
				if (newX1 != x2) {
					if (prevNotSame) {
						L l = levels[levelsCount--];
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
						L l = levels[levelsCount--];
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
					L l = levels[levelsCount--];
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
				} else if (lx1 > x1 && lx2 < x2) {
					L l2 = levels[levelsCount--];
					l2.x1 = lx2;
					l2.x2 = x2;
					l2.y = y;
					l2.next = next;
					if (next != null) {
						next.prev = l2;
					}
					levelsX[lx2] = l2;

					L l1 = levels[levelsCount--];
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

			L getPrevLevel(int x) {
				L l = this.prev;
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
			if (++iterationsCount % 10_000_000 == 0) {
				System.out.print(X + " [" + (System.currentTimeMillis() - startTime) / 1000 + "sec]: results=" + results.size() + ", sequence=");
				for (int i = 0; i < n; i++) {
					System.out.print((i > 0 ? ", " : "") + sequence[i]);
				}
				System.out.println();
			}
		}

		void start() {
			startLevelOpt(0, 0, X);
		}

		void removeLevel(int levelX1, int levelX2, int size) {
			L l = firstLevel;
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
			L l = firstLevel;
			while (l != null) {
				if (l != firstLevel) {
					System.out.print(", ");
				}
				System.out.print(l);
				l = l.next;
			}
			System.out.println();
		}

		void startLevelOpt(int level, int levelX1, int levelX2) {
			int levelLength = levelX2 - levelX1;
			if (level == 0 && levelX2 == X && levelLength < sequence[0]) {
				return;
			}
			for (int quad = 1; quad <= levelLength; quad++) {
				if (!busy[quad]) {
					busy[quad] = true;
					bestLevel = levelsX[levelX1];
					L currentLevel = bestLevel.addLevel(quad);
					sequence[n++] = quad;

					// printProcess();
					// printLevels();

					int x2 = levelX1 + quad;
					if (x2 != levelX2 && (levelX1 == 0 || currentLevel.prev.y >= level + quad)) { // 50%
						startLevelOpt(level, x2, levelX2);
					} else if (firstLevel.next != null) {
						if (bestLevel == null) {  // 20%
							L l = firstLevel;
							while (true) {
								if ((l.prev == null || l.prev.y > l.y) && (l.next == null || l.next.y > l.y)) {
									bestLevel = l;
									break;
								}
								l = l.next;
							}
							while (l != null) {
								if ((l.prev == null || l.prev.y > l.y) && (l.next == null || l.next.y > l.y) && l.x2 - l.x1 < bestLevel.x2 - bestLevel.x1) {
									bestLevel = l;
								}
								l = l.next;
							}
						}
						startLevelOpt(bestLevel.y, bestLevel.x1, bestLevel.x2);
					} else if (firstLevel.y >= X && n > 1) {
						result(firstLevel.y);
					}

					removeLevel(levelX1, x2, quad);
					busy[quad] = false;
					n--;
				}
			}
		}

		void startLevel(int level, int levelX1, int levelX2) {
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
							while (++lx2 < X && (ly = front[lx2]) == currentLevel)
								;
							if ((ly > currentLevel || lx2 == X) && lx2 - lx1 < bestLevelX2 - bestLevelX1) {
								bestLevel = currentLevel;
								bestLevelX1 = lx1;
								bestLevelX2 = lx2;
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
			Result result2 = result1.flipHor();
			if (results.contains(result2)) {
				return;
			}
			Result result3 = result1.flipVer();
			if (results.contains(result3)) {
				return;
			}
			Result result4 = result1.flipHorVer();
			if (results.contains(result4)) {
				return;
			}
			if (result1.width == result1.height) {
				Result result5 = result1.rotate();
				if (results.contains(result5)) {
					return;
				}
				Result result6 = result5.flipHor();
				if (results.contains(result6)) {
					return;
				}
				Result result7 = result5.flipVer();
				if (results.contains(result7)) {
					return;
				}
				Result result8 = result5.flipHorVer();
				if (results.contains(result8)) {
					return;
				}
			}
			Result bestResult = result1;
			if (result2.getFirstQuadSize() > bestResult.getFirstQuadSize()) {
				bestResult = result2;
			}
			if (result3.getFirstQuadSize() > bestResult.getFirstQuadSize()) {
				bestResult = result3;
			}
			if (result4.getFirstQuadSize() > bestResult.getFirstQuadSize()) {
				bestResult = result4;
			}
			results.add(bestResult);
			allResults.add(bestResult);
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
					while (++lx2 < X && (ly = front[lx2]) == currentLevel)
						;
					if ((ly > currentLevel || lx2 == X) && lx2 - lx1 < x2 - x1) {
						y = currentLevel;
						x1 = lx1;
						x2 = lx2;
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

		public void print() {
			if (results.size() > 0) {
				for (Result result : results) {
					System.out.println(result);
				}
				System.out.println("// time: " + (System.currentTimeMillis() - startTime) / 1000.0 + "sec\n//");
			}
		}
	}

	static void q1(int startQuad, int endQuad, int threads) {
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		if (threads > 1) {
			for (int n = startQuad; n <= endQuad; n++) {
				int _n = n;
				executor.execute(() -> {
					Q1 q = new Q1(_n);
					q.start();
					synchronized (Q.class) {
						q.print();
					}
				});
			}
		} else {
			for (int n = startQuad; n <= endQuad; n++) {
				Q1 q = new Q1(n);
				q.start();
				q.print();
			}
		}
	}

	static class Smith {
		int n;

		int halfN;

		Smith(int n) {
			this.n = n;
			halfN = n / 2;
			top = new Side();
			bottom = new Side();
			sides = new Side[n];
			for (int i = 0; i < n; i++) {
				Side side = new Side();
				side.index = i;
				sides[i] = side;
			}
		}

		Side top;

		Side bottom;

		int sidesPos;

		Side[] sides;

		class Side {
			int index;

			int count;

			Side[] out = new Side[n];

			void start() {
				for (int i = 0; i < halfN && sidesPos < n; i++) {
					Side s = sides[sidesPos++];
					out[count++] = s;
					s.start();
				}
			}
		}

		void start() {
			top.start();
		}
	}

	static void smith() {
		new Smith(17).start();
	}

	public static void main(String[] args) {
		q1(71, 71, 1);
		// smith();
	}
}

// 32-33 (9)	0.003sec	18 0x0, 15 0x18, 14 18x0, 10 22x14, 9 23x24, 8 15x25, 7 15x18, 4 18x14, 1 22x24
// time: 0.021sec
//
// 47-65 (10)	0.046sec	25 0x0, 24 23x41, 23 0x42, 22 25x0, 19 28x22, 17 0x25, 11 17x25, 6 17x36, 5 23x36, 3 25x22
// time: 0.06sec
//
// 55-57 (10)	0.06sec	30 0x0, 27 0x30, 25 30x0, 17 38x25, 15 40x42, 13 27x44, 11 27x33, 8 30x25, 3 27x30, 2 38x42
// time: 0.275sec
//
// 60-84 (17)	0.175sec	33 0x28, 28 0x0, 27 33x36, 23 0x61, 21 39x63, 19 41x17, 17 43x0, 16 23x68, 15 28x0, 13 28x15, 8 33x28, 7 23x61, 5 34x63, 4 30x64, 3 30x61, 2 41x15, 1 33x63
// time: 0.754sec
//
// 61-69 (9)	0.365sec	36 0x0, 33 0x36, 28 33x41, 25 36x0, 16 45x25, 9 36x25, 7 38x34, 5 33x36, 2 36x34
// time: 0.91sec
//
// 63-94 (17)	0.278sec	36 0x0, 35 0x36, 28 35x47, 27 36x0, 23 0x71, 20 43x27, 19 44x75, 12 23x71, 11 23x83, 10 34x84, 9 35x75, 8 35x39, 7 36x27, 5 38x34, 3 35x36, 2 36x34, 1 34x83
// time: 1.285sec
//
// 65-88 (18)	0.292sec	33 0x0, 32 33x0, 31 34x32, 25 40x63, 20 0x51, 18 0x33, 17 0x71, 16 18x33, 14 20x49, 13 27x75, 12 28x63, 10 17x78, 8 20x63, 7 17x71, 4 24x71, 3 24x75, 2 18x49, 1 33x32
// time: 1.903sec
//
// 69-115 (17)	1.568sec	40 0x0, 39 0x76, 36 0x40, 30 39x85, 29 40x0, 21 48x64, 19 50x45, 16 53x29, 14 36x50, 13 40x29, 12 36x64, 9 39x76, 8 42x42, 6 36x44, 4 36x40, 3 50x42, 2 40x42
// 69-118 (15)	2.436sec	38 0x0, 37 32x81, 32 0x86, 31 38x0, 28 0x38, 26 43x55, 24 45x31, 20 0x66, 17 28x38, 15 28x55, 12 20x74, 11 32x70, 8 20x66, 7 38x31, 4 28x70
// time: 4.109sec
//
// 71-106 (14)	1.234sec	40 0x37, 37 0x0, 34 37x0, 31 40x34, 29 0x77, 23 48x83, 19 29x87, 18 53x65, 13 40x65, 10 29x77, 9 39x78, 5 48x78, 3 37x34, 1 39x77
// 71-105 (16)	2.207sec	41 0x0, 36 35x69, 35 0x70, 30 41x0, 29 0x41, 20 51x49, 19 52x30, 15 29x41, 13 38x56, 11 41x30, 9 29x56, 8 44x41, 7 44x49, 5 29x65, 4 34x65, 1 34x69
// 71-89 (20)	3.773sec	38 0x0, 33 38x0, 28 0x61, 23 0x38, 22 28x67, 21 50x68, 20 51x48, 17 34x50, 15 56x33, 12 23x38, 11 23x50, 10 46x33, 9 35x41, 8 38x33, 7 44x43, 6 28x61, 5 51x43, 3 35x38, 2 44x41, 1 50x67
// time: 5.655sec
//
// 72-103 (16)	3.026sec	43 0x0, 39 33x64, 33 0x70, 29 43x0, 27 0x43, 21 27x43, 15 57x29, 14 43x29, 13 48x51, 11 61x53, 9 63x44, 8 48x43, 7 56x44, 6 27x64, 2 61x51, 1 56x43
// 72-123 (19)	3.865sec	41 0x0, 40 0x41, 32 40x52, 31 41x0, 27 23x96, 23 0x100, 22 50x101, 21 51x31, 19 0x81, 17 55x84, 15 19x81, 12 43x84, 11 40x41, 10 41x31, 9 34x87, 6 34x81, 5 50x96, 4 19x96, 3 40x84
// 72-173 (20)	5.821sec	39 0x97, 38 0x0, 37 0x136, 35 37x138, 34 38x0, 33 39x105, 31 0x66, 28 0x38, 26 46x54, 25 47x80, 20 52x34, 18 28x48, 16 31x81, 15 31x66, 14 38x34, 10 28x38, 8 39x97, 6 46x48, 2 37x136, 1 46x80
// time: 7.068sec
//
// 73-75 (16)	1.594sec	41 0x0, 34 0x41, 32 41x0, 23 50x52, 20 53x32, 16 34x59, 12 41x32, 9 34x50, 8 45x44, 7 43x52, 6 39x44, 5 34x45, 4 34x41, 3 38x41, 2 43x50, 1 38x44
// time: 8.049sec
//
// 74-79 (16)	1.009sec	45 0x0, 34 0x45, 29 45x0, 23 34x56, 17 57x62, 15 45x29, 14 60x29, 12 45x44, 11 34x45, 10 64x43, 9 65x53, 8 57x54, 7 57x47, 4 60x43, 3 57x44, 1 64x53
// 74-88 (22)	1.479sec	36 0x0, 28 46x60, 27 0x61, 25 0x36, 23 51x37, 20 54x0, 19 27x69, 18 36x0, 17 57x20, 15 36x31, 14 37x46, 13 36x18, 12 25x47, 11 25x36, 10 27x59, 9 37x60, 8 49x23, 6 51x31, 5 49x18, 3 54x20, 2 25x59, 1 36x46
// 74-103 (16)	1.857sec	41 0x0, 33 41x0, 32 0x71, 30 0x41, 27 47x53, 23 51x80, 20 54x33, 19 32x84, 17 30x52, 15 32x69, 13 41x33, 11 30x41, 7 47x46, 6 41x46, 4 47x80, 2 30x69
// time: 9.163sec
//
// 75-82 (17)	1.136sec	43 0x0, 39 0x43, 32 43x0, 19 39x63, 18 57x32, 17 58x65, 15 60x50, 14 43x32, 11 39x52, 10 50x53, 7 50x46, 6 44x46, 5 39x47, 4 39x43, 3 57x50, 2 58x63, 1 43x46
// 75-109 (19)	1.449sec	42 0x39, 39 0x0, 36 39x0, 33 42x36, 28 0x81, 21 54x69, 19 56x90, 15 28x81, 13 28x96, 12 42x69, 11 43x81, 9 47x92, 8 48x101, 7 41x102, 6 41x96, 4 43x92, 3 39x36, 2 54x90, 1 47x101
// 75-112 (13)	2.827sec	42 0x39, 39 0x0, 36 39x0, 33 42x36, 31 0x81, 24 51x88, 20 31x92, 19 56x69, 14 42x69, 11 31x81, 9 42x83, 5 51x83, 3 39x36
// 75-123 (20)	3.34sec	50 0x0, 41 0x82, 34 41x89, 32 0x50, 25 50x0, 23 32x50, 20 55x51, 18 57x71, 16 41x73, 14 61x25, 12 63x39, 11 50x25, 9 32x73, 8 50x36, 7 56x44, 6 50x44, 5 58x39, 3 58x36, 2 55x71, 1 55x50
// 75-106 (14)	3.966sec	42 0x0, 36 0x42, 33 42x0, 28 0x78, 27 48x79, 24 51x33, 22 53x57, 20 28x86, 17 36x57, 15 36x42, 12 36x74, 9 42x33, 8 28x78, 5 48x74
// 75-112 (13)	6.757sec	42 0x0, 39 0x73, 36 39x76, 33 42x0, 31 0x42, 24 51x33, 20 31x42, 19 56x57, 14 42x62, 11 31x62, 9 42x33, 5 51x57, 3 39x73
// time: 12.227sec
//
// 77-83 (14)	3.124sec	43 0x0, 40 0x43, 34 43x0, 25 52x34, 24 53x59, 13 40x70, 12 40x43, 9 43x34, 8 40x55, 7 40x63, 6 47x64, 5 48x59, 4 48x55, 1 47x63
// 77-141 (20)	6.038sec	46 0x45, 45 0x0, 32 45x0, 31 46x69, 28 26x113, 26 0x115, 24 0x91, 23 54x118, 22 24x91, 20 57x32, 18 59x100, 17 60x52, 14 46x55, 13 46x100, 12 45x32, 11 46x44, 5 54x113, 3 57x52, 2 24x113, 1 45x44
// time: 15.3sec
//
// 78-104 (20)	3.779sec	41 0x0, 37 41x0, 34 44x70, 33 45x37, 24 0x80, 22 0x58, 20 24x84, 17 0x41, 16 29x41, 14 30x70, 13 32x57, 12 17x41, 10 22x60, 8 22x70, 7 22x53, 6 24x78, 5 17x53, 4 41x37, 3 29x57, 2 22x78
// 78-104 (20)	3.779sec	41 0x0, 37 41x0, 34 0x70, 29 0x41, 24 54x80, 22 56x58, 21 57x37, 20 34x84, 17 29x53, 16 41x37, 14 34x70, 12 29x41, 10 46x60, 8 48x70, 7 46x53, 6 48x78, 4 53x53, 3 53x57, 2 54x78, 1 56x57
// time: 18.314sec
//
// 79-112 (17)	2.634sec	44 0x0, 36 0x44, 35 44x0, 32 0x80, 28 32x84, 26 53x35, 23 36x61, 20 59x61, 19 60x93, 17 36x44, 12 67x81, 9 44x35, 7 60x86, 5 62x81, 4 32x80, 3 59x81, 2 60x84
// 79-140 (19)	9.544sec	45 0x0, 42 37x98, 37 0x103, 34 45x0, 32 0x71, 26 0x45, 25 32x73, 23 56x34, 22 57x76, 19 60x57, 18 26x45, 16 44x57, 12 44x45, 11 45x34, 10 34x63, 8 26x63, 5 32x98, 3 57x73, 2 32x71
// 79-130 (10)	9.813sec	45 0x0, 44 0x45, 41 0x89, 38 41x92, 35 44x57, 34 45x0, 23 56x34, 12 44x45, 11 45x34, 3 41x89
// 79-110 (17)	16.325sec	42 0x0, 41 0x69, 38 41x72, 37 42x0, 27 0x42, 19 42x37, 18 61x37, 17 62x55, 16 46x56, 15 27x42, 12 27x57, 7 39x60, 5 41x67, 4 42x56, 3 39x57, 2 39x67, 1 61x55
// 79-123 (22)	17.842sec	42 0x0, 37 42x0, 32 47x37, 30 21x93, 28 51x95, 27 20x42, 26 53x69, 24 29x69, 21 0x102, 20 0x42, 15 0x75, 14 15x79, 13 0x62, 12 0x90, 10 19x69, 9 12x93, 7 13x62, 6 13x69, 5 42x37, 4 15x75, 3 12x90, 2 51x93
// time: 22.977sec
//
// 80-81 (12)	4.873sec	44 0x0, 37 0x44, 36 44x0, 23 57x36, 22 58x59, 21 37x60, 13 44x36, 11 46x49, 9 37x51, 7 37x44, 2 44x49, 1 57x59
// 80-89 (22)	7.042sec	33 0x0, 32 0x57, 27 53x37, 26 33x0, 25 55x64, 24 0x33, 23 32x66, 21 59x0, 16 64x21, 15 38x39, 14 24x43, 13 40x26, 12 41x54, 11 53x26, 10 24x33, 9 32x57, 7 33x26, 6 34x33, 5 59x21, 4 34x39, 3 38x54, 2 53x64
// time: 29.309sec
//
// 81-91 (18)	7.155sec	52 0x0, 39 0x52, 29 52x0, 27 54x64, 19 62x45, 16 65x29, 15 39x76, 13 52x29, 12 50x52, 11 39x52, 10 52x42, 8 46x68, 7 39x69, 6 39x63, 5 45x63, 4 50x64, 3 62x42, 1 45x68
// 81-119 (16)	19.5sec	44 0x0, 42 39x77, 39 0x80, 37 44x0, 36 0x44, 22 59x37, 18 63x59, 15 44x37, 14 49x63, 13 36x64, 12 36x52, 11 48x52, 8 36x44, 4 59x59, 3 36x77, 1 48x63
// 81-112 (11)	24.612sec	43 0x0, 41 40x71, 40 0x72, 38 43x0, 33 48x38, 29 0x43, 19 29x43, 10 29x62, 9 39x62, 5 43x38, 1 39x71
// time: 33.3sec
//
// 82-106 (18)	6.832sec	57 0x0, 49 0x57, 33 49x73, 25 57x0, 17 65x56, 16 49x57, 13 57x25, 12 70x25, 11 57x38, 10 72x46, 9 73x37, 8 57x49, 7 65x49, 5 68x40, 4 68x45, 3 70x37, 2 68x38, 1 72x45
// 82-155 (19)	10.969sec	50 0x0, 45 37x110, 44 0x50, 38 44x72, 37 0x118, 32 50x0, 24 0x94, 22 44x50, 18 50x32, 16 66x56, 14 68x32, 13 24x105, 11 24x94, 10 72x46, 9 35x94, 7 37x103, 6 66x50, 4 68x46, 2 35x103
// 82-155 (19)	11.116sec	50 0x0, 45 37x110, 44 0x50, 38 44x72, 37 0x118, 32 50x0, 24 0x94, 22 60x50, 18 64x32, 16 44x56, 14 50x32, 13 24x105, 11 24x94, 10 50x46, 9 35x94, 7 37x103, 6 44x50, 4 60x46, 2 35x103
// 82-155 (19)	22.405sec	50 32x61, 45 0x0, 44 38x111, 38 0x117, 37 45x0, 32 0x85, 24 58x37, 22 0x45, 18 0x67, 16 22x45, 14 18x71, 13 45x37, 11 47x50, 10 22x61, 9 38x52, 7 38x45, 6 32x111, 4 18x67, 2 45x50
// time: 36.39sec
//
// 83-104 (19)	8.637sec	43 0x0, 40 43x0, 36 47x68, 34 0x43, 28 55x40, 27 0x77, 20 27x84, 13 34x71, 12 43x40, 11 34x52, 10 45x52, 9 34x43, 8 34x63, 7 27x77, 6 49x62, 5 42x66, 4 45x62, 3 42x63, 2 47x66
// 83-128 (18)	21.066sec	47 0x0, 44 0x47, 39 44x61, 36 47x0, 28 55x100, 25 58x36, 20 19x108, 19 0x109, 18 0x91, 17 18x91, 16 39x112, 14 44x47, 12 43x100, 11 47x36, 9 35x91, 8 35x100, 4 39x108, 1 18x108
// 83-112 (13)	40.415sec	44 0x0, 42 41x70, 41 0x71, 39 44x0, 31 52x39, 27 0x44, 14 27x44, 13 27x58, 12 40x58, 11 41x47, 8 44x39, 3 41x44, 1 40x70
// time: 70.883sec
//
// 84-122 (20)	12.038sec	48 0x0, 39 0x83, 36 48x0, 35 0x48, 27 57x71, 24 60x98, 22 35x61, 21 39x101, 19 65x36, 18 39x83, 17 48x36, 16 68x55, 13 35x48, 11 57x60, 8 48x53, 7 56x53, 5 63x55, 3 57x98, 2 63x53, 1 56x60
// time: 61.94sec
//
// 85-112 (13)	11.066sec	44 0x43, 43 0x0, 42 43x0, 41 44x42, 29 56x83, 25 0x87, 17 39x95, 14 25x98, 12 44x83, 11 25x87, 8 36x87, 3 36x95, 1 43x42
// 85-113 (19)	39.518sec	47 0x0, 38 47x0, 36 0x47, 30 0x83, 29 56x38, 27 58x67, 24 30x89, 22 36x67, 20 36x47, 19 66x94, 12 54x101, 9 47x38, 7 59x94, 6 30x83, 5 54x96, 4 54x89, 3 54x93, 2 57x94, 1 57x93
// time: 68.933sec
//
// 86-148 (22)	10.865sec	46 0x0, 41 0x107, 40 46x0, 34 52x40, 32 0x46, 29 0x78, 27 59x97, 24 62x124, 23 63x74, 21 41x127, 20 32x46, 19 44x74, 18 41x109, 16 43x93, 15 29x78, 14 29x93, 12 32x66, 8 44x66, 6 46x40, 4 59x93, 3 59x124, 2 41x107
// 86-98 (11)	25.057sec	51 0x0, 47 0x51, 39 47x59, 35 51x0, 24 62x35, 11 51x35, 8 47x51, 7 55x52, 6 56x46, 5 51x46, 1 55x51
// 86-176 (24)	32.234sec	49 0x0, 48 0x49, 44 42x132, 42 0x134, 38 48x62, 37 49x0, 32 54x100, 25 61x37, 20 19x97, 19 0x97, 18 0x116, 17 18x117, 15 39x106, 13 48x49, 12 49x37, 11 43x121, 9 39x97, 8 35x121, 6 48x100, 5 35x129, 4 35x117, 3 40x129, 2 40x132, 1 18x116
// 86-148 (22)	56.64sec	46 0x0, 40 46x0, 37 0x78, 34 52x40, 33 0x115, 32 0x46, 31 55x74, 29 33x119, 24 62x124, 20 32x46, 19 67x105, 18 37x85, 16 37x103, 14 53x105, 12 32x66, 11 44x74, 8 44x66, 7 37x78, 6 46x40, 5 62x119, 4 33x115, 2 53x103
// 86-140 (19)	63.017sec	45 0x0, 44 42x96, 42 0x98, 41 45x0, 32 0x45, 31 55x65, 24 62x41, 23 32x58, 21 0x77, 17 45x41, 15 40x81, 13 32x45, 11 21x77, 10 21x88, 9 31x89, 8 32x81, 7 55x58, 2 40x96, 1 31x88
// time: 75.526sec
//
// 87-105 (18)	9.547sec	41 0x0, 40 47x65, 36 0x41, 28 0x77, 27 60x38, 25 41x0, 24 36x41, 21 66x0, 19 28x86, 17 70x21, 16 41x25, 13 57x25, 11 36x65, 10 37x76, 9 28x77, 4 66x21, 3 57x38, 1 36x76
// 87-119 (19)	17.023sec	47 0x0, 40 47x0, 37 0x47, 35 0x84, 29 58x63, 27 60x92, 25 35x94, 23 64x40, 21 37x57, 17 47x40, 16 42x78, 10 37x47, 7 35x87, 6 58x57, 5 37x78, 4 38x83, 3 35x84, 2 58x92, 1 37x83
// 87-153 (16)	26.08sec	56 0x0, 51 0x56, 46 0x107, 41 46x112, 36 51x76, 31 56x0, 20 51x56, 17 70x31, 16 71x60, 14 56x31, 12 75x48, 11 56x45, 8 67x48, 5 46x107, 4 71x56, 3 67x45
// time: 100.182sec
//
// 88-100 (19)	11.808sec	47 0x0, 41 47x0, 36 52x64, 31 0x47, 23 65x41, 22 0x78, 21 31x62, 17 35x83, 15 31x47, 13 22x87, 12 53x41, 11 54x53, 9 22x78, 8 46x54, 7 46x47, 6 47x41, 4 31x83, 2 52x62, 1 53x53
// 88-208 (21)	30.441sec	53 35x117, 47 0x45, 45 0x0, 43 45x0, 41 47x43, 38 50x170, 35 0x122, 33 55x84, 30 0x92, 28 0x180, 25 30x92, 23 0x157, 22 28x186, 16 34x170, 12 23x157, 11 23x169, 8 47x84, 6 28x180, 5 30x117, 2 45x43, 1 34x169
// 88-145 (18)	54.614sec	52 0x0, 50 0x95, 43 0x52, 38 50x107, 36 52x0, 26 62x81, 24 43x52, 21 67x60, 19 43x76, 16 52x36, 13 75x47, 12 50x95, 11 77x36, 9 68x36, 8 67x52, 7 68x45, 5 62x76, 2 75x45
// 88-105 (17)	59.938sec	51 0x0, 37 51x0, 33 29x72, 29 0x76, 26 62x79, 25 0x51, 23 65x37, 21 25x51, 19 69x60, 14 51x37, 12 57x60, 11 46x61, 10 46x51, 9 56x51, 7 62x72, 4 25x72, 1 56x60
// 88-174 (21)	61.5sec	56 0x51, 51 0x0, 46 42x128, 42 0x132, 37 51x0, 32 56x60, 25 0x107, 23 65x37, 21 48x107, 19 69x109, 17 71x92, 15 56x92, 14 51x37, 13 25x119, 12 25x107, 11 37x107, 10 38x118, 9 56x51, 4 38x128, 2 69x107, 1 37x118
// 88-128 (20)	63.572sec	51 0x0, 46 0x82, 42 46x86, 37 51x0, 31 0x51, 26 62x37, 23 65x63, 19 46x67, 16 31x51, 15 31x67, 11 51x37, 9 47x58, 8 54x48, 7 47x51, 6 56x56, 5 56x62, 4 61x63, 3 51x48, 2 54x56, 1 61x62
// 88-209 (24)	91.312sec	48 0x0, 47 41x117, 45 43x164, 44 0x48, 43 0x166, 41 0x125, 40 48x0, 33 0x92, 28 60x40, 25 63x68, 24 64x93, 19 44x68, 17 47x100, 16 44x52, 14 33x103, 13 44x87, 12 48x40, 11 33x92, 8 33x117, 7 57x93, 6 57x87, 4 44x48, 3 44x100, 2 41x164
// time: 123.706sec
//
// 89-121 (19)	17.149sec	48 0x0, 43 0x48, 41 48x0, 30 0x91, 29 60x41, 26 63x70, 25 64x96, 20 43x70, 18 46x103, 17 43x53, 16 30x105, 14 30x91, 13 44x90, 12 48x41, 7 57x96, 6 57x90, 5 43x48, 2 44x103, 1 43x90
// 89-199 (28)	38.287sec	53 0x0, 52 37x147, 50 0x53, 37 0x162, 36 53x0, 35 0x103, 29 35x118, 25 64x122, 24 0x138, 23 66x58, 22 67x36, 21 68x101, 20 69x81, 19 50x81, 18 50x100, 16 50x65, 15 35x103, 14 53x36, 13 24x149, 11 24x138, 9 50x56, 8 59x50, 7 59x58, 6 53x50, 4 64x118, 3 50x53, 2 35x147, 1 68x100
// 89-107 (23)	56.15sec	50 0x0, 39 50x0, 30 0x50, 27 0x80, 26 63x81, 25 30x50, 22 67x39, 20 69x61, 19 27x88, 17 46x90, 15 48x75, 14 55x61, 13 35x75, 12 55x49, 10 57x39, 8 27x80, 7 50x39, 6 63x75, 5 30x75, 4 50x46, 3 54x46, 2 46x88, 1 54x49
// time: 132.405sec
//
// 90-132 (18)	231.175sec	51 0x0, 47 0x85, 43 47x89, 39 51x0, 34 0x51, 26 64x63, 24 66x39, 21 34x51, 17 47x72, 13 34x72, 11 55x52, 9 55x63, 8 51x39, 7 59x39, 6 60x46, 5 55x47, 4 51x47, 1 59x46
// 90-130 (20)	235.217sec	50 0x0, 47 0x83, 43 47x87, 40 50x0, 33 0x50, 24 66x63, 23 67x40, 19 47x68, 17 50x40, 14 33x69, 11 50x57, 10 33x59, 9 33x50, 8 42x50, 7 43x58, 6 61x57, 5 61x63, 4 43x65, 3 47x65, 1 42x58
// 90-148 (16)	262.428sec	50 0x0, 49 41x99, 41 0x107, 40 50x0, 33 0x74, 30 60x40, 29 61x70, 28 33x71, 24 0x50, 21 39x50, 15 24x50, 10 50x40, 9 24x65, 8 33x99, 6 33x65, 1 60x70
// 90-136 (17)	271.889sec	51 0x0, 49 0x87, 41 49x95, 39 51x0, 36 0x51, 33 57x62, 23 67x39, 21 36x66, 16 51x39, 15 36x51, 8 49x87, 7 60x55, 6 51x60, 5 51x55, 4 56x55, 3 57x59, 1 56x59
// 90-114 (16)	349.36sec	61 0x0, 53 0x61, 37 53x77, 29 61x0, 21 69x56, 16 53x61, 15 61x29, 14 76x29, 13 77x43, 9 61x44, 8 61x53, 7 70x44, 5 72x51, 3 69x53, 2 70x51, 1 76x43
// time: 367.996sec
//
// 91-104 (21)	110.094sec	43 0x0, 32 31x72, 31 0x73, 30 0x43, 29 30x43, 28 63x76, 25 43x0, 23 68x0, 20 71x56, 19 72x37, 18 43x25, 14 77x23, 13 59x43, 12 59x56, 11 61x32, 9 68x23, 8 63x68, 7 61x25, 5 72x32, 4 59x68, 1 30x72
// 91-136 (18)	159.99sec	53 0x0, 46 0x53, 38 53x0, 37 0x99, 28 37x108, 26 65x110, 25 66x61, 24 67x86, 23 68x38, 21 46x87, 20 46x67, 15 53x38, 14 46x53, 9 37x99, 8 60x53, 6 60x61, 2 65x108, 1 66x86
// 91-120 (12)	255.993sec	48 0x0, 47 44x73, 44 0x76, 43 48x0, 30 61x43, 28 0x48, 17 44x56, 16 28x60, 13 48x43, 12 28x48, 8 40x48, 4 40x56
// 91-101 (17)	292.94sec	52 0x0, 49 0x52, 39 52x0, 27 49x74, 23 68x39, 19 49x55, 16 52x39, 15 76x86, 12 68x62, 11 80x62, 8 76x78, 7 84x79, 6 85x73, 5 80x73, 4 76x74, 3 49x52, 1 84x78
// 91-153 (19)	360.68sec	58 0x0, 51 0x102, 44 0x58, 40 51x113, 33 58x0, 29 62x84, 26 44x58, 21 70x63, 18 44x84, 17 74x33, 16 58x33, 13 78x50, 11 51x102, 9 58x49, 8 70x55, 6 67x49, 5 73x50, 3 67x55, 1 73x49
// time: 434.647sec
//
// 92-155 (22)	268.501sec	60 0x0, 55 0x60, 40 0x115, 37 55x65, 32 60x0, 29 63x126, 24 68x102, 23 40x132, 18 74x32, 17 40x115, 15 77x50, 14 60x32, 13 55x102, 11 57x115, 10 60x46, 9 60x56, 8 69x57, 7 70x50, 6 57x126, 5 55x60, 4 70x46, 1 69x56
// 92-155 (22)	268.577sec	60 0x0, 55 0x60, 40 0x115, 37 55x65, 32 60x0, 29 63x126, 24 68x102, 23 40x132, 18 74x47, 17 40x115, 15 77x32, 14 60x51, 13 55x102, 11 57x115, 10 60x41, 9 60x32, 8 69x32, 7 70x40, 6 57x126, 5 55x60, 4 70x47, 1 69x40
// 92-155 (22)	268.578sec	60 0x0, 55 0x60, 40 0x115, 37 55x65, 32 60x0, 29 63x126, 24 68x102, 23 40x132, 18 60x32, 17 40x115, 15 60x50, 14 78x32, 13 55x102, 11 57x115, 10 82x46, 9 83x56, 8 75x57, 7 75x50, 6 57x126, 5 55x60, 4 78x46, 1 82x56
// 92-155 (22)	268.585sec	60 0x0, 55 0x60, 40 0x115, 37 55x65, 32 60x0, 29 63x126, 24 68x102, 23 40x132, 18 60x47, 17 40x115, 15 60x32, 14 78x51, 13 55x102, 11 57x115, 10 82x41, 9 83x32, 8 75x32, 7 75x40, 6 57x126, 5 55x60, 4 78x47, 1 82x40
// 92-155 (22)	268.827sec	60 0x0, 55 0x60, 40 0x115, 37 55x65, 32 60x33, 29 63x126, 24 68x102, 23 40x132, 18 74x0, 17 40x115, 15 77x18, 14 60x0, 13 55x102, 11 57x115, 10 60x14, 9 60x24, 8 69x25, 7 70x18, 6 57x126, 5 55x60, 4 70x14, 1 69x24
// 92-155 (22)	268.827sec	60 0x0, 55 0x60, 40 0x115, 37 55x65, 32 60x33, 29 63x126, 24 68x102, 23 40x132, 18 74x15, 17 40x115, 15 77x0, 14 60x19, 13 55x102, 11 57x115, 10 60x9, 9 60x0, 8 69x0, 7 70x8, 6 57x126, 5 55x60, 4 70x15, 1 69x8
// 92-155 (22)	268.827sec	60 0x0, 55 0x60, 40 0x115, 37 55x65, 32 60x33, 29 63x126, 24 68x102, 23 40x132, 18 60x0, 17 40x115, 15 60x18, 14 78x0, 13 55x102, 11 57x115, 10 82x14, 9 83x24, 8 75x25, 7 75x18, 6 57x126, 5 55x60, 4 78x14, 1 82x24
// 92-155 (22)	268.828sec	60 0x0, 55 0x60, 40 0x115, 37 55x65, 32 60x33, 29 63x126, 24 68x102, 23 40x132, 18 60x15, 17 40x115, 15 60x0, 14 78x19, 13 55x102, 11 57x115, 10 82x9, 9 83x0, 8 75x0, 7 75x8, 6 57x126, 5 55x60, 4 78x15, 1 82x8
// 92-125 (20)	286.619sec	45 0x0, 41 0x84, 39 0x45, 34 58x30, 33 59x64, 30 62x0, 28 64x97, 23 41x102, 20 39x64, 19 39x45, 18 41x84, 17 45x0, 13 45x32, 9 53x17, 8 45x17, 7 45x25, 6 52x26, 5 59x97, 4 58x26, 1 52x25
// 92-126 (18)	318.28sec	49 0x0, 46 46x80, 43 49x0, 37 55x43, 33 0x49, 27 19x99, 22 33x49, 19 0x107, 17 14x82, 15 31x84, 14 0x82, 13 33x71, 11 0x96, 9 46x71, 8 11x99, 6 49x43, 3 11x96, 2 31x82
// 92-155 (22)	356.36sec	49 0x0, 47 45x108, 45 0x110, 43 49x0, 37 55x43, 30 25x49, 28 64x80, 25 0x49, 20 0x74, 19 45x89, 17 28x93, 16 0x94, 14 31x79, 12 16x98, 11 20x79, 10 45x79, 9 55x80, 8 20x90, 6 49x43, 5 20x74, 4 16x94, 3 28x90
// time: 631.455sec
//
// 93-208 (23)	188.674sec	50 0x125, 49 0x0, 44 49x0, 43 50x136, 40 0x85, 36 0x49, 34 59x70, 33 0x175, 32 61x104, 29 64x179, 26 67x44, 23 36x62, 21 40x104, 19 40x85, 18 49x44, 17 33x175, 16 33x192, 15 49x193, 14 50x179, 13 36x49, 11 50x125, 8 59x62, 1 49x192
// 93-112 (13)	247.201sec	51 0x0, 42 51x0, 37 56x75, 36 0x51, 33 60x42, 25 0x87, 24 36x51, 20 36x75, 17 39x95, 14 25x98, 11 25x87, 9 51x42, 3 36x95
// 93-126 (17)	294.085sec	55 0x0, 42 51x84, 39 0x55, 38 55x0, 32 0x94, 29 39x55, 25 68x59, 21 72x38, 19 32x107, 17 55x38, 12 39x84, 11 40x96, 8 32x99, 5 32x94, 4 68x55, 3 37x96, 2 37x94
// 93-138 (20)	303.792sec	54 0x0, 44 0x94, 40 0x54, 39 54x0, 29 64x109, 27 66x61, 26 40x68, 22 71x39, 21 72x88, 20 44x118, 17 54x39, 15 57x94, 14 40x54, 13 44x94, 12 54x56, 11 44x107, 9 55x109, 6 66x88, 5 66x56, 2 55x107
// 93-144 (17)	385.96sec	52 0x0, 48 0x96, 45 48x99, 44 0x52, 41 52x0, 32 61x67, 26 67x41, 17 44x72, 15 52x41, 12 44x60, 11 56x56, 10 51x89, 8 44x52, 7 44x89, 5 56x67, 4 52x56, 3 48x96
// 93-157 (18)	439.166sec	53 0x0, 49 44x108, 44 0x113, 41 52x67, 40 53x0, 32 0x81, 28 0x53, 27 66x40, 24 28x53, 20 32x77, 14 52x53, 13 53x40, 11 41x97, 9 32x97, 7 32x106, 5 39x108, 4 28x77, 2 39x106
// time: 737.461sec
//
// 94-97 (18)	183.017sec	54 0x0, 40 54x0, 31 63x66, 26 68x40, 24 39x73, 23 0x74, 20 0x54, 19 37x54, 17 20x54, 16 23x81, 14 54x40, 12 56x54, 10 23x71, 7 56x66, 6 33x75, 4 33x71, 3 20x71, 2 37x73
// 94-218 (30)	297.642sec	56 0x54, 54 0x0, 46 48x131, 41 53x177, 40 54x0, 38 56x66, 33 0x110, 31 0x187, 27 67x104, 26 68x40, 25 23x143, 23 0x143, 22 31x196, 21 0x166, 19 21x168, 18 33x110, 16 51x115, 15 33x128, 14 54x40, 13 40x183, 12 56x54, 11 56x104, 9 31x187, 8 40x168, 7 40x176, 6 47x177, 5 51x110, 3 48x128, 2 21x166, 1 47x176
// 94-216 (25)	344.735sec	51 0x0, 50 44x118, 48 46x168, 46 0x170, 44 0x126, 43 51x0, 40 54x78, 35 59x43, 32 0x51, 27 32x51, 25 0x83, 18 0x108, 17 37x78, 16 25x95, 15 29x111, 13 41x95, 12 25x83, 11 18x115, 10 44x108, 8 51x43, 7 18x108, 5 32x78, 4 25x111, 3 41x108, 2 44x168
// 94-143 (21)	438.633sec	55 0x0, 50 0x93, 39 55x0, 38 0x55, 30 64x62, 27 67x92, 26 38x55, 24 70x119, 23 71x39, 20 50x123, 17 50x95, 16 55x39, 14 50x81, 12 38x81, 11 50x112, 7 64x55, 6 61x112, 5 61x118, 4 66x119, 3 64x92, 1 66x118
// 94-135 (18)	510.05sec	52 0x0, 51 0x84, 43 51x92, 42 52x0, 32 0x52, 27 67x65, 23 71x42, 20 32x52, 19 52x42, 16 51x76, 15 52x61, 12 32x72, 7 44x77, 5 44x72, 4 67x61, 3 49x72, 2 49x75, 1 51x75
// 94-111 (13)	664.743sec	56 0x0, 55 0x56, 39 55x72, 38 56x0, 20 74x38, 18 56x38, 16 55x56, 14 80x58, 9 71x63, 5 75x58, 4 71x59, 3 71x56, 1 74x58
// 94-115 (10)	679.983sec	60 0x0, 55 0x60, 39 55x76, 34 60x0, 23 71x53, 19 75x34, 16 55x60, 15 60x34, 11 60x49, 4 71x49
// time: 737.595sec
//
// 95-130 (20)	207.373sec	53 0x0, 44 0x53, 42 53x0, 33 32x97, 31 64x42, 30 65x100, 27 68x73, 24 44x73, 20 44x53, 18 0x97, 15 0x115, 14 18x97, 11 53x42, 10 22x111, 9 23x121, 8 15x122, 7 15x115, 4 18x111, 3 65x97, 1 22x121
// 95-130 (20)	207.391sec	53 0x0, 44 0x53, 42 53x0, 33 32x97, 31 64x42, 30 65x100, 27 68x73, 24 44x73, 20 44x53, 18 0x112, 15 0x97, 14 18x116, 11 53x42, 10 22x106, 9 23x97, 8 15x97, 7 15x105, 4 18x112, 3 65x97, 1 22x105
// 95-130 (20)	207.408sec	53 0x0, 44 0x53, 42 53x0, 33 32x97, 31 64x42, 30 65x100, 27 68x73, 24 44x73, 20 44x53, 18 14x97, 15 17x115, 14 0x97, 11 53x42, 10 0x111, 9 0x121, 8 9x122, 7 10x115, 4 10x111, 3 65x97, 1 9x121
// 95-130 (20)	207.43sec	53 0x0, 44 0x53, 42 53x0, 33 32x97, 31 64x42, 30 65x100, 27 68x73, 24 44x73, 20 44x53, 18 14x112, 15 17x97, 14 0x116, 11 53x42, 10 0x106, 9 0x97, 8 9x97, 7 10x105, 4 10x112, 3 65x97, 1 9x105
// 95-130 (20)	234.172sec	53 0x0, 44 0x53, 42 53x0, 33 0x97, 31 64x42, 30 65x100, 27 68x73, 24 44x73, 20 44x53, 18 47x97, 15 50x115, 14 33x97, 11 53x42, 10 33x111, 9 33x121, 8 42x122, 7 43x115, 4 43x111, 3 65x97, 1 42x121
// 95-130 (20)	234.622sec	53 0x0, 44 0x53, 42 53x0, 33 0x97, 31 64x42, 30 65x100, 27 68x73, 24 44x73, 20 44x53, 18 47x112, 15 50x97, 14 33x116, 11 53x42, 10 33x106, 9 33x97, 8 42x97, 7 43x105, 4 43x112, 3 65x97, 1 42x105
// 95-130 (20)	234.683sec	53 0x0, 44 0x53, 42 53x0, 33 0x97, 31 64x42, 30 65x100, 27 68x73, 24 44x73, 20 44x53, 18 33x97, 15 33x115, 14 51x97, 11 53x42, 10 55x111, 9 56x121, 8 48x122, 7 48x115, 4 51x111, 3 65x97, 1 55x121
// 95-130 (20)	235.599sec	53 0x0, 44 0x53, 42 53x0, 33 0x97, 31 64x42, 30 65x100, 27 68x73, 24 44x73, 20 44x53, 18 33x112, 15 33x97, 14 51x116, 11 53x42, 10 55x106, 9 56x97, 8 48x97, 7 48x105, 4 51x112, 3 65x97, 1 55x105
// 95-184 (23)	292.311sec	55 0x48, 48 0x0, 45 0x103, 40 55x53, 36 0x148, 32 63x152, 30 65x93, 29 66x123, 28 67x25, 27 36x157, 25 70x0, 22 48x0, 21 45x123, 20 45x103, 19 48x22, 13 45x144, 12 55x41, 10 55x93, 9 36x148, 8 58x144, 7 48x41, 5 58x152, 3 67x22
// 95-112 (17)	329.737sec	49 0x0, 46 49x0, 38 57x74, 33 0x49, 30 0x82, 28 67x46, 27 30x85, 18 49x46, 16 33x49, 13 44x72, 11 33x74, 10 57x64, 9 33x65, 8 49x64, 7 42x65, 3 30x82, 2 42x72
// 95-113 (24)	342.324sec	45 0x0, 39 56x74, 37 0x45, 34 61x40, 31 0x82, 25 31x88, 24 37x45, 23 72x17, 19 37x69, 17 78x0, 16 45x29, 15 45x14, 14 45x0, 12 60x17, 11 61x29, 10 59x0, 9 69x0, 8 70x9, 7 63x10, 6 31x82, 5 56x69, 4 59x10, 3 60x14, 1 69x9
// 95-98 (11)	408.191sec	50 0x0, 48 0x50, 45 50x0, 28 67x45, 25 70x73, 22 48x76, 19 48x57, 12 55x45, 7 48x50, 5 50x45, 3 67x73
// 95-194 (24)	509.735sec	52 0x93, 50 0x0, 49 0x145, 46 49x148, 45 50x0, 43 52x105, 31 64x74, 29 66x45, 23 22x70, 22 0x71, 21 0x50, 20 21x50, 19 45x74, 16 50x45, 13 53x61, 12 52x93, 9 41x50, 8 45x66, 7 41x59, 5 48x61, 4 41x66, 3 49x145, 2 48x59, 1 21x70
// 95-194 (18)	621.422sec	61 0x52, 52 0x0, 51 44x143, 44 0x150, 43 52x0, 37 0x113, 34 61x43, 30 37x113, 28 67x115, 20 75x95, 18 77x77, 16 61x77, 14 61x93, 9 52x43, 8 67x107, 7 37x143, 6 61x107, 2 75x93
// time: 941.542sec
//
// 96-166 (28)	306.275sec	51 0x0, 45 51x0, 38 0x51, 35 61x131, 33 63x98, 32 0x134, 30 66x68, 29 32x137, 28 38x70, 25 0x89, 23 73x45, 21 42x98, 20 0x114, 19 38x51, 18 35x119, 17 25x102, 16 57x45, 15 20x119, 13 25x89, 10 53x119, 9 57x61, 8 53x129, 7 66x61, 6 51x45, 5 20x114, 4 38x98, 3 32x134, 2 61x129
// 96-139 (22)	346.114sec	57 0x0, 45 0x57, 39 57x0, 37 0x102, 29 67x84, 27 45x57, 26 70x113, 24 72x60, 22 45x84, 21 75x39, 18 57x39, 17 37x122, 16 54x123, 12 37x110, 11 49x106, 10 60x113, 8 37x102, 7 60x106, 6 54x117, 5 49x117, 4 45x106, 3 72x57
// 96-178 (22)	415.279sec	51 0x49, 49 0x0, 47 49x0, 45 51x47, 44 0x100, 38 58x140, 34 0x144, 27 69x113, 25 44x115, 24 34x154, 21 75x92, 16 59x92, 15 44x100, 14 44x140, 10 34x144, 8 51x92, 7 59x108, 5 70x108, 4 66x108, 3 66x112, 2 49x47, 1 69x112
// 96-97 (11)	489.258sec	56 0x0, 41 0x56, 40 56x0, 31 65x66, 26 70x40, 24 41x73, 17 41x56, 14 56x40, 12 58x54, 7 58x66, 2 56x54
// 96-99 (12)	546.709sec	55 0x0, 44 0x55, 41 55x0, 31 65x68, 27 69x41, 21 44x78, 14 55x41, 13 56x55, 12 44x55, 11 44x67, 10 55x68, 1 55x67
// 96-99 (19)	569.38sec	54 0x0, 45 0x54, 42 54x0, 28 45x71, 23 73x76, 20 76x42, 17 45x54, 14 82x62, 12 54x42, 11 62x60, 10 66x42, 9 73x67, 8 68x52, 6 62x54, 5 77x62, 4 73x63, 3 73x60, 2 66x52, 1 76x62
// 96-103 (16)	570.935sec	57 0x0, 46 0x57, 39 57x0, 26 70x77, 24 46x79, 22 46x57, 21 75x39, 18 57x39, 17 79x60, 11 68x66, 6 73x60, 5 68x61, 4 68x57, 3 72x57, 2 68x77, 1 72x60
// 96-155 (18)	613.732sec	55 0x0, 51 0x55, 49 0x106, 47 49x108, 41 55x0, 27 69x41, 23 73x85, 22 51x86, 18 51x55, 17 79x68, 14 55x41, 13 51x73, 10 69x68, 8 64x78, 7 72x78, 5 64x73, 2 49x106, 1 72x85
// 96-155 (20)	780.006sec	60 0x0, 51 0x104, 45 51x110, 44 0x60, 36 60x0, 27 69x57, 26 70x84, 25 44x60, 21 75x36, 15 60x36, 14 56x85, 12 44x85, 11 59x99, 9 60x51, 8 51x102, 7 44x97, 6 69x51, 5 51x97, 3 56x99, 1 69x84
// 96-109 (13)	810.134sec	57 0x0, 52 0x57, 39 57x0, 25 71x84, 24 72x60, 21 75x39, 20 52x57, 19 52x90, 18 57x39, 13 52x77, 7 65x77, 6 65x84, 3 72x57
// 96-145 (14)	815.414sec	53 0x0, 52 0x93, 44 52x101, 43 53x0, 40 0x53, 33 63x43, 25 71x76, 23 40x53, 17 40x76, 14 57x76, 11 60x90, 10 53x43, 8 52x93, 3 57x90
// 96-240 (23)	954.024sec	56 0x55, 55 0x0, 53 43x187, 43 0x197, 42 54x145, 41 55x0, 40 56x68, 37 59x108, 34 25x111, 31 23x145, 27 69x41, 25 0x111, 23 0x152, 22 0x175, 21 22x176, 16 0x136, 14 55x41, 13 56x55, 11 43x176, 9 16x136, 7 16x145, 3 56x108, 1 22x175
// time: 1177.761sec
//
// 97-136 (17)	443.913sec	55 0x0, 43 0x55, 42 55x0, 38 0x98, 33 38x103, 29 68x42, 26 71x110, 25 43x55, 23 43x80, 20 77x71, 19 78x91, 13 55x42, 12 66x91, 11 66x80, 9 68x71, 7 71x103, 5 38x98
// 97-98 (13)	495.96sec	56 0x0, 42 0x56, 41 56x0, 29 68x69, 28 69x41, 26 42x72, 16 42x56, 13 56x41, 11 58x54, 7 58x65, 4 65x65, 3 65x69, 2 56x54
// 97-191 (23)	505.884sec	53 0x50, 50 0x0, 47 50x0, 44 53x47, 43 54x148, 38 0x103, 32 65x91, 28 26x163, 27 38x103, 26 0x165, 25 72x123, 24 0x141, 18 54x130, 17 37x146, 16 38x130, 13 24x150, 12 53x91, 9 24x141, 7 65x123, 5 33x141, 4 33x146, 3 50x47, 2 24x163
// 97-119 (20)	508.567sec	51 0x0, 46 51x0, 44 53x75, 36 0x51, 32 0x87, 29 68x46, 21 32x98, 17 51x46, 15 36x51, 13 36x66, 12 56x63, 11 32x87, 10 43x88, 9 44x79, 8 36x79, 7 49x68, 5 51x63, 4 49x75, 2 49x66, 1 43x87
// 97-143 (18)	797.341sec	56 0x0, 51 0x92, 46 51x97, 41 56x0, 36 0x56, 30 67x41, 26 71x71, 21 36x56, 20 51x77, 15 36x77, 11 56x41, 10 57x59, 8 57x69, 7 60x52, 6 65x71, 4 56x52, 3 57x56, 2 65x69
// 97-127 (15)	807.009sec	54 0x0, 52 45x75, 45 0x82, 43 54x0, 32 65x43, 28 0x54, 21 44x54, 16 28x54, 12 28x70, 11 54x43, 5 40x77, 4 40x70, 3 40x74, 2 43x75, 1 43x74
// 97-162 (24)	821.468sec	57 0x0, 52 0x110, 45 52x117, 40 57x0, 38 59x79, 31 28x79, 28 0x82, 25 0x57, 22 25x57, 20 61x59, 19 57x40, 16 81x63, 14 47x65, 12 85x51, 11 86x40, 10 76x40, 9 76x50, 8 47x57, 7 52x110, 6 55x59, 4 81x59, 3 25x79, 2 55x57, 1 85x50
// time: 1298.641sec
//
// 98-122 (16)	292.907sec	53 0x0, 45 53x0, 40 0x53, 31 40x66, 29 0x93, 27 71x69, 26 72x96, 25 47x97, 24 74x45, 21 53x45, 18 29x104, 13 40x53, 11 29x93, 7 40x97, 3 71x66, 1 71x96
// 98-219 (16)	659.099sec	60 0x57, 57 0x0, 52 0x117, 50 0x169, 48 50x171, 46 52x125, 41 57x0, 38 60x87, 25 73x41, 21 77x66, 17 60x70, 16 57x41, 13 60x57, 8 52x117, 4 73x66, 2 50x169
// 98-248 (29)	895.485sec	54 0x0, 53 0x195, 50 48x120, 48 0x147, 45 53x203, 44 54x0, 42 56x78, 40 0x54, 34 64x44, 33 65x170, 30 0x94, 26 30x94, 24 40x54, 23 0x124, 17 48x170, 16 40x78, 14 34x120, 13 35x134, 12 23x135, 11 23x124, 10 54x44, 9 56x187, 8 48x187, 7 58x196, 5 53x198, 4 30x120, 3 53x195, 2 56x196, 1 34x134
// 98-111 (10)	975.225sec	57 0x0, 54 0x57, 44 54x67, 41 57x0, 26 72x41, 15 57x41, 11 61x56, 7 54x60, 4 57x56, 3 54x57
// 98-168 (21)	1110.483sec	65 0x0, 55 0x113, 48 0x65, 43 55x125, 33 65x0, 31 67x94, 29 48x65, 21 77x73, 19 48x94, 17 65x33, 16 82x33, 15 65x50, 13 85x60, 12 55x113, 11 87x49, 8 77x65, 7 80x53, 5 80x60, 4 83x49, 3 80x50, 1 82x49
// 98-113 (12)	1148.911sec	57 0x0, 56 0x57, 42 56x71, 41 57x0, 30 68x41, 12 56x59, 11 57x41, 7 61x52, 4 57x52, 3 58x56, 2 56x57, 1 57x56
// time: 1355.336sec
//
// 99-104 (18)	516.853sec	44 0x0, 36 63x68, 34 0x70, 31 44x0, 29 34x75, 28 44x31, 27 72x41, 26 0x44, 24 75x0, 18 26x44, 17 82x24, 16 47x59, 13 34x62, 10 72x31, 9 63x59, 8 26x62, 7 75x24, 3 44x59
// 99-157 (16)	528.423sec	52 0x0, 47 52x0, 44 0x52, 40 59x81, 36 63x121, 34 65x47, 32 31x125, 31 0x126, 30 0x96, 29 30x96, 21 44x60, 15 44x81, 13 52x47, 8 44x52, 4 59x121, 1 30x125
// 99-112 (15)	609.899sec	53 0x0, 46 53x0, 38 61x74, 31 30x81, 30 0x82, 29 0x53, 28 71x46, 18 53x46, 17 44x64, 15 29x66, 13 29x53, 11 42x53, 10 61x64, 2 42x64, 1 29x81
// 99-120 (19)	626.182sec	50 0x0, 49 50x0, 39 60x81, 36 0x84, 34 0x50, 32 67x49, 24 36x96, 18 34x66, 17 50x49, 16 34x50, 15 52x66, 12 36x84, 8 52x81, 7 53x89, 5 48x91, 4 48x84, 3 48x88, 2 51x89, 1 51x88
// 99-112 (12)	647.625sec	56 0x0, 43 56x0, 39 60x73, 31 29x81, 30 69x43, 29 0x83, 27 0x56, 25 27x56, 17 52x56, 13 56x43, 8 52x73, 2 27x81
// 99-146 (20)	790.471sec	54 0x0, 48 0x98, 45 54x0, 44 0x54, 36 63x45, 28 48x118, 24 75x81, 23 76x123, 20 48x98, 19 44x54, 18 81x105, 17 58x81, 14 44x84, 13 68x105, 11 44x73, 9 54x45, 8 55x73, 7 68x98, 5 76x118, 3 55x81
// 99-106 (12)	790.512sec	58 0x0, 48 0x58, 41 58x0, 28 48x78, 24 75x41, 23 76x83, 20 48x58, 18 81x65, 17 58x41, 13 68x65, 7 68x58, 5 76x78
// 99-146 (21)	800.423sec	55 0x0, 49 0x97, 44 55x0, 42 0x55, 29 42x68, 28 71x80, 27 49x119, 24 55x44, 23 76x123, 22 49x97, 20 79x44, 16 83x64, 15 84x108, 13 42x55, 12 71x68, 8 76x115, 7 77x108, 6 71x108, 5 71x114, 4 79x64, 1 76x114
// 99-106 (13)	974.723sec	55 0x0, 51 0x55, 44 55x0, 33 66x44, 29 70x77, 19 51x87, 15 51x55, 11 55x44, 10 60x77, 9 51x78, 8 51x70, 7 59x70, 1 59x77
// time: 1851.261sec
//
// 100-136 (22)	455.529sec	52 0x0, 48 52x0, 39 30x97, 33 0x52, 31 69x105, 30 0x106, 26 33x71, 25 75x48, 23 52x48, 21 0x85, 19 33x52, 18 69x87, 16 59x71, 14 75x73, 13 87x92, 12 21x85, 11 89x73, 10 59x87, 9 21x97, 8 92x84, 5 87x87, 3 89x84
// 100-136 (20)	751.513sec	53 0x44, 47 53x53, 44 0x0, 39 0x97, 36 64x100, 30 70x0, 26 44x0, 25 39x111, 23 77x30, 18 44x26, 15 62x38, 14 39x97, 11 53x100, 9 53x44, 8 69x30, 7 62x31, 5 62x26, 3 67x26, 2 67x29, 1 69x29
// 100-169 (21)	1069.98sec	63 0x61, 61 0x0, 45 0x124, 39 61x0, 37 63x82, 28 72x141, 27 45x142, 23 77x39, 22 78x119, 20 80x62, 18 45x124, 17 63x65, 16 61x39, 15 63x119, 10 67x55, 8 63x134, 7 71x134, 6 61x55, 4 63x61, 3 77x62, 1 71x141
// 100-160 (24)	1093.622sec	63 0x0, 51 0x63, 46 0x114, 37 63x0, 33 67x98, 29 71x131, 26 74x72, 25 46x135, 23 51x75, 21 46x114, 19 63x37, 18 82x37, 17 83x55, 16 51x98, 12 51x63, 11 63x56, 9 74x56, 8 63x67, 7 76x65, 5 71x67, 4 67x131, 3 71x72, 2 74x65, 1 82x55
// 100-160 (24)	1093.684sec	63 0x0, 51 0x63, 46 0x114, 37 63x0, 33 67x98, 29 71x131, 26 74x72, 25 46x135, 23 51x75, 21 46x114, 19 63x37, 18 82x37, 17 83x55, 16 51x98, 12 51x63, 11 63x64, 9 74x63, 8 63x56, 7 76x56, 5 71x56, 4 67x131, 3 71x61, 2 74x61, 1 82x55
// 100-155 (19)	1145.082sec	58 0x0, 49 0x58, 48 0x107, 42 58x0, 31 69x68, 29 71x99, 27 73x128, 26 74x42, 25 48x130, 23 48x107, 20 49x73, 16 58x42, 15 49x58, 14 49x93, 10 64x58, 8 63x99, 6 63x93, 5 64x68, 2 71x128
// 100-185 (18)	1272.288sec	59 0x0, 51 49x134, 49 0x136, 42 58x92, 41 59x0, 39 0x59, 38 0x98, 33 39x59, 28 72x64, 23 77x41, 20 38x105, 18 59x41, 13 45x92, 11 38x125, 9 49x125, 7 38x98, 6 39x92, 5 72x59
// 100-215 (23)	1354.397sec	58 0x0, 56 0x107, 52 0x163, 49 0x58, 48 52x167, 44 56x123, 42 58x0, 31 69x68, 26 74x42, 24 76x99, 20 49x73, 16 58x42, 15 49x58, 14 49x93, 13 63x99, 11 65x112, 10 64x58, 9 56x114, 7 56x107, 6 63x93, 5 64x68, 4 52x163, 2 63x112
// 100-139 (23)	1742.748sec	55 0x0, 45 55x0, 40 0x55, 35 65x45, 33 67x80, 27 40x80, 26 74x113, 25 40x55, 23 17x95, 21 16x118, 19 55x120, 18 37x121, 17 0x95, 16 0x123, 14 40x107, 13 54x107, 11 0x112, 10 55x45, 7 67x113, 6 11x112, 5 11x118, 3 37x118, 1 54x120
// time: 2218.569sec
//
// 101-113 (18)	730.674sec	57 0x0, 44 57x0, 38 63x75, 31 70x44, 29 23x57, 27 36x86, 23 0x57, 19 0x94, 18 52x57, 17 19x96, 14 0x80, 13 57x44, 11 52x75, 10 26x86, 9 14x80, 7 19x89, 5 14x89, 3 23x86
// 101-144 (19)	821.829sec	53 0x0, 51 0x53, 48 53x0, 40 0x104, 36 65x108, 31 70x48, 29 72x79, 25 40x119, 21 51x79, 19 51x60, 15 40x104, 12 58x48, 10 55x109, 9 55x100, 8 64x100, 7 51x53, 5 53x48, 4 51x100, 1 64x108
// 101-120 (12)	890.423sec	52 0x0, 49 52x0, 41 60x79, 36 0x52, 32 0x88, 30 71x49, 28 32x92, 24 36x68, 19 52x49, 16 36x52, 11 60x68, 4 32x88
// 101-184 (23)	968.917sec	55 0x52, 52 0x0, 49 52x0, 46 55x49, 44 0x140, 33 0x107, 30 44x154, 27 74x157, 26 75x95, 25 44x129, 22 33x107, 20 55x95, 19 82x138, 17 84x121, 15 69x121, 14 55x115, 13 69x136, 11 33x129, 8 74x149, 6 69x115, 5 69x149, 3 52x49, 2 82x136
// 101-107 (19)	1018.763sec	58 0x0, 49 0x58, 43 58x0, 23 78x65, 22 79x43, 21 58x43, 19 82x88, 18 49x89, 17 61x64, 15 67x92, 12 49x67, 11 67x81, 10 49x79, 9 49x58, 8 59x81, 4 78x88, 3 58x64, 2 59x79, 1 78x64
// 101-154 (17)	1234.675sec	57 0x0, 52 0x102, 49 52x105, 45 0x57, 44 57x0, 33 68x72, 28 73x44, 23 45x69, 16 57x44, 13 55x92, 12 45x57, 10 45x92, 9 57x60, 7 66x60, 5 68x67, 3 52x102, 2 66x67
// 101-257 (21)	1417.814sec	56 0x0, 53 0x204, 51 50x115, 50 0x100, 48 53x209, 45 56x0, 44 0x56, 43 58x166, 36 65x79, 34 67x45, 30 28x174, 28 0x176, 26 0x150, 24 26x150, 23 44x56, 21 44x79, 15 50x100, 11 56x45, 8 50x166, 5 53x204, 2 26x174
// 101-165 (17)	1458.631sec	60 0x0, 53 0x112, 52 0x60, 48 53x117, 41 60x0, 29 72x88, 25 76x63, 24 52x60, 22 79x41, 20 52x84, 19 60x41, 13 59x104, 7 52x104, 6 53x111, 4 72x84, 3 76x60, 1 52x111
// 101-179 (19)	1734.374sec	64 0x60, 60 0x0, 55 0x124, 46 55x133, 41 60x0, 37 64x96, 22 79x41, 21 64x75, 19 60x41, 16 85x80, 15 64x60, 12 79x63, 10 91x63, 9 55x124, 7 94x73, 5 85x75, 4 90x76, 3 91x73, 1 90x75
// 101-224 (25)	1832.407sec	58 0x0, 56 45x168, 51 0x58, 45 0x179, 44 57x124, 43 58x0, 36 0x109, 34 0x145, 28 73x70, 27 74x43, 26 75x98, 24 51x100, 23 34x145, 22 51x78, 21 36x124, 16 58x43, 15 36x109, 13 51x65, 11 34x168, 10 64x59, 9 64x69, 7 51x58, 6 58x59, 2 73x98, 1 73x69
// time: 2450.597sec
//
// 102-111 (17)	369.995sec	64 0x0, 47 0x64, 38 64x0, 28 47x83, 27 75x84, 24 78x60, 22 80x38, 19 47x64, 16 64x38, 12 66x64, 10 64x54, 7 66x76, 6 74x54, 5 73x76, 4 74x60, 3 75x81, 2 73x81
// 102-128 (17)	424.372sec	53 0x0, 49 53x0, 42 60x86, 37 65x49, 32 28x96, 28 0x100, 24 0x76, 23 0x53, 22 23x53, 21 24x75, 20 45x61, 15 45x81, 12 53x49, 8 45x53, 5 60x81, 4 24x96, 1 23x75
// 102-149 (20)	686.998sec	55 0x0, 50 0x55, 47 55x0, 44 0x105, 36 66x47, 34 68x115, 32 70x83, 24 44x125, 20 50x83, 16 50x67, 14 44x111, 12 58x103, 11 55x47, 10 58x115, 9 57x58, 8 50x103, 7 50x60, 6 44x105, 5 50x55, 2 55x58
// 102-147 (21)	923.605sec	59 0x0, 52 0x95, 50 52x97, 43 59x0, 36 0x59, 32 70x65, 22 80x43, 20 36x59, 18 52x79, 16 36x79, 14 56x65, 12 68x43, 10 70x55, 9 59x43, 8 62x57, 6 56x59, 5 63x52, 4 59x52, 3 59x56, 2 68x55, 1 62x56
// 102-225 (27)	1389.073sec	56 0x0, 53 0x172, 49 53x176, 46 56x0, 43 59x82, 41 0x131, 40 0x56, 36 66x46, 35 0x96, 31 41x125, 30 72x125, 26 40x56, 24 35x101, 21 81x155, 19 40x82, 16 41x156, 13 68x163, 11 57x165, 10 56x46, 9 57x156, 8 73x155, 7 66x156, 6 35x125, 5 35x96, 4 53x172, 2 66x163, 1 72x155
// 102-149 (18)	1463.994sec	55 0x0, 53 0x96, 49 53x100, 47 55x0, 41 0x55, 28 74x72, 25 77x47, 22 55x47, 17 41x69, 16 58x69, 15 59x85, 14 41x55, 10 41x86, 8 51x86, 6 53x94, 3 74x69, 2 51x94, 1 58x85
// 102-212 (23)	1528.465sec	55 0x0, 52 50x160, 50 0x162, 47 55x0, 42 0x90, 39 63x47, 38 64x122, 36 66x86, 35 0x55, 30 0x132, 28 35x55, 24 42x98, 22 42x122, 18 30x144, 16 48x144, 15 42x83, 12 30x132, 9 57x89, 8 55x47, 7 35x83, 6 57x83, 3 63x86, 2 48x160
// 102-216 (20)	1847.839sec	57 0x53, 54 0x110, 53 0x0, 52 0x164, 50 52x166, 49 53x0, 45 57x49, 29 73x94, 25 77x141, 23 54x143, 19 54x110, 18 84x123, 16 57x94, 14 54x129, 11 73x123, 9 68x134, 7 77x134, 5 68x129, 4 53x49, 2 52x164
// time: 2046.64sec
//
// 103-152 (24)	379.369sec	55 0x0, 48 55x0, 41 62x48, 37 0x55, 33 0x92, 32 71x120, 31 72x89, 27 0x125, 25 37x55, 23 48x129, 21 27x131, 20 33x96, 19 53x89, 16 37x80, 15 33x116, 13 48x116, 11 61x108, 10 61x119, 9 53x80, 8 53x108, 7 55x48, 6 27x125, 4 33x92, 1 71x119
// 103-169 (23)	850.627sec	56 0x0, 47 56x0, 44 0x85, 40 0x129, 37 66x132, 32 71x100, 31 44x69, 29 0x56, 28 75x72, 27 44x100, 26 40x143, 25 78x47, 22 56x47, 16 50x127, 15 29x70, 14 29x56, 13 43x56, 10 40x133, 6 44x127, 5 66x127, 4 40x129, 3 75x69, 1 43x69
// 103-176 (16)	906.398sec	63 0x0, 59 0x63, 54 0x122, 49 54x127, 44 59x83, 40 63x0, 24 79x59, 20 59x63, 19 84x40, 12 63x51, 11 63x40, 10 74x40, 9 75x50, 5 54x122, 4 75x59, 1 74x50
// 103-152 (17)	985.855sec	60 0x0, 54 0x98, 49 54x103, 43 60x0, 38 0x60, 28 75x75, 24 60x43, 22 38x60, 21 54x82, 19 84x43, 16 38x82, 15 60x67, 13 90x62, 8 75x67, 7 83x68, 6 84x62, 1 83x67
// 103-187 (23)	1019.493sec	60 0x0, 52 0x135, 51 52x136, 43 60x0, 41 0x60, 35 68x69, 34 0x101, 32 71x104, 27 41x60, 26 77x43, 19 52x117, 18 34x117, 17 60x43, 16 34x101, 14 41x87, 13 55x87, 11 50x106, 10 61x107, 9 68x60, 7 61x100, 6 55x100, 5 50x101, 3 68x104
// 103-193 (18)	1026.789sec	60 0x0, 55 0x138, 48 55x145, 43 60x0, 41 62x104, 40 0x60, 38 0x100, 35 68x69, 28 40x60, 26 77x43, 24 38x114, 17 60x43, 16 52x88, 14 38x100, 12 40x88, 10 52x104, 9 68x60, 7 55x138
// 103-118 (12)	1097.389sec	60 0x0, 58 0x60, 45 58x73, 43 60x0, 30 73x43, 13 60x43, 9 64x56, 8 65x65, 7 58x66, 6 58x60, 4 60x56, 1 64x65
// 103-208 (15)	1187.608sec	60 0x59, 59 0x0, 52 51x156, 51 0x157, 44 59x0, 43 60x73, 40 63x116, 38 0x119, 29 74x44, 25 38x119, 15 59x44, 14 60x59, 13 38x144, 12 51x144, 3 60x116
// 103-199 (25)	1682.784sec	56 0x0, 55 0x144, 48 55x151, 47 56x0, 41 62x110, 38 0x56, 34 28x110, 33 70x77, 32 38x78, 30 73x47, 28 0x116, 22 0x94, 17 56x47, 16 22x94, 14 50x64, 12 38x66, 10 38x56, 9 64x64, 8 48x56, 7 55x144, 6 22x110, 5 64x73, 4 69x73, 2 48x64, 1 69x77
// 103-242 (26)	2097.853sec	60 43x93, 54 0x0, 52 0x190, 51 52x191, 49 54x0, 44 59x49, 43 0x107, 40 0x150, 38 65x153, 29 0x78, 25 40x153, 24 0x54, 20 39x54, 19 40x74, 15 24x54, 14 29x93, 13 52x178, 12 40x178, 11 29x82, 9 24x69, 7 33x75, 6 33x69, 5 54x49, 4 29x78, 3 40x150, 1 39x74
// 103-218 (22)	2269.595sec	56 0x53, 53 0x0, 52 51x166, 51 0x167, 50 53x0, 47 56x50, 35 68x97, 34 69x132, 30 0x137, 28 0x109, 23 45x109, 21 30x146, 18 51x148, 17 28x109, 16 53x132, 14 39x132, 12 56x97, 11 28x126, 9 30x137, 6 39x126, 3 53x50, 2 51x146
// time: 2506.733sec
//
// 105-106 (15)	343.111sec	59 0x0, 47 0x59, 46 59x0, 35 47x71, 25 59x46, 23 82x83, 21 84x46, 16 89x67, 12 47x59, 7 82x76, 5 84x67, 4 85x72, 3 82x73, 2 82x71, 1 84x72
// 105-203 (19)	385.376sec	63 42x87, 57 0x0, 53 52x150, 48 57x0, 42 0x93, 39 66x48, 36 0x57, 30 36x57, 28 24x175, 27 0x135, 25 27x150, 24 0x179, 17 0x162, 15 27x135, 10 17x162, 9 57x48, 7 17x172, 6 36x87, 3 24x172
// 105-128 (23)	393.679sec	49 0x0, 43 0x85, 40 65x54, 36 0x49, 34 71x94, 32 49x0, 29 36x49, 28 43x100, 24 81x0, 22 43x78, 17 49x32, 16 89x24, 14 91x40, 13 66x32, 12 79x42, 10 79x32, 9 70x45, 8 81x24, 7 36x78, 6 65x94, 5 65x49, 4 66x45, 2 89x40
// 105-223 (28)	547.891sec	65 0x117, 63 0x54, 54 0x0, 42 63x57, 41 0x182, 40 65x147, 36 69x187, 30 75x27, 28 41x195, 27 78x0, 26 79x99, 24 54x0, 22 83x125, 21 54x24, 18 65x129, 16 63x99, 14 65x115, 13 41x182, 12 63x45, 9 54x45, 8 61x187, 7 54x188, 6 54x182, 5 60x182, 4 79x125, 3 75x24, 2 63x115, 1 60x187
// 105-263 (33)	547.899sec	65 0x117, 63 0x54, 54 0x0, 45 0x182, 42 63x57, 41 64x222, 40 65x147, 36 0x227, 35 70x187, 30 75x27, 28 36x235, 27 78x0, 26 79x99, 25 45x197, 24 54x0, 22 83x125, 21 54x24, 18 65x129, 16 63x99, 15 45x182, 14 65x115, 13 51x222, 12 63x45, 10 60x187, 9 54x45, 8 36x227, 7 44x228, 6 45x222, 5 60x182, 4 79x125, 3 75x24, 2 63x115, 1 44x227
// 105-223 (23)	569.535sec	60 0x47, 54 51x128, 51 0x107, 47 0x0, 45 60x50, 41 64x182, 36 0x187, 33 72x95, 31 47x0, 29 0x158, 28 36x195, 27 78x0, 23 82x27, 22 29x158, 21 51x107, 19 63x31, 16 47x31, 15 36x180, 13 51x182, 12 60x95, 7 29x180, 4 78x27, 3 60x47
// 105-174 (22)	581.281sec	64 0x0, 63 0x64, 47 0x127, 42 63x82, 41 64x0, 31 47x143, 27 78x147, 24 81x58, 23 82x124, 19 63x124, 18 63x64, 17 88x41, 16 47x127, 13 64x41, 11 77x41, 10 64x54, 7 74x57, 6 82x52, 5 77x52, 4 78x143, 3 74x54, 1 81x57
// 105-181 (22)	582.839sec	57 0x54, 54 0x0, 51 54x0, 48 57x51, 46 59x135, 36 69x99, 32 27x149, 27 0x154, 24 45x111, 23 0x111, 22 23x111, 20 0x134, 16 29x133, 14 45x135, 12 57x99, 9 20x138, 7 20x147, 5 24x133, 4 20x134, 3 54x51, 2 27x147, 1 23x133
// 105-120 (20)	715.379sec	65 0x0, 44 61x76, 40 65x0, 32 29x88, 29 0x91, 26 0x65, 23 26x65, 21 65x40, 19 86x40, 17 88x59, 15 73x61, 12 49x76, 11 49x65, 8 65x61, 7 66x69, 6 60x70, 5 60x65, 3 26x88, 2 86x59, 1 65x69
// 105-137 (16)	740.354sec	56 0x44, 49 56x55, 44 0x0, 37 0x100, 33 72x104, 32 44x0, 29 76x0, 26 79x29, 23 56x32, 19 37x100, 18 37x119, 17 55x120, 16 56x104, 12 44x32, 3 76x29, 1 55x119
// 105-106 (12)	888.758sec	60 0x0, 46 0x60, 45 60x0, 31 74x75, 30 75x45, 28 46x78, 18 46x60, 15 60x45, 11 64x60, 7 64x71, 4 71x71, 3 71x75
// 105-134 (27)	923.054sec	41 0x0, 39 0x95, 38 67x96, 36 69x60, 33 41x0, 31 74x0, 30 0x41, 29 76x31, 28 39x106, 27 49x33, 24 0x71, 22 47x60, 19 30x41, 18 24x77, 17 30x60, 14 55x82, 13 42x82, 11 39x95, 10 57x96, 8 41x33, 7 50x99, 6 24x71, 5 42x77, 4 50x95, 3 54x96, 2 74x31, 1 54x95
// 105-158 (23)	998.736sec	61 0x0, 50 0x61, 47 0x111, 44 61x0, 32 73x126, 30 75x96, 28 50x61, 27 78x44, 26 47x132, 25 50x89, 18 47x114, 17 61x44, 14 78x71, 13 92x71, 12 93x84, 11 82x85, 10 65x114, 8 65x124, 7 75x89, 4 78x85, 3 47x111, 2 73x124, 1 92x84
// 105-135 (19)	1041.811sec	56 0x0, 49 56x0, 40 0x56, 39 0x96, 34 39x101, 32 73x103, 30 75x73, 24 81x49, 23 40x56, 22 53x79, 18 63x49, 13 40x79, 12 63x67, 9 44x92, 7 56x49, 6 75x67, 5 39x96, 4 40x92, 2 73x101
// 105-176 (28)	1100.682sec	48 0x90, 46 0x0, 44 0x46, 42 63x63, 39 66x137, 38 0x138, 33 72x0, 32 73x105, 30 75x33, 28 38x148, 26 46x0, 25 48x105, 20 46x26, 19 44x71, 18 48x130, 17 58x46, 15 48x90, 14 44x46, 11 44x60, 10 38x138, 9 66x37, 8 55x63, 7 66x130, 6 66x26, 5 66x32, 4 71x33, 3 55x60, 1 71x32
// 105-174 (23)	1112.264sec	55 0x0, 50 55x0, 47 0x55, 45 60x129, 42 63x87, 39 0x102, 37 68x50, 33 0x141, 27 33x147, 24 39x102, 21 39x126, 16 47x86, 13 55x50, 12 47x74, 11 47x63, 10 58x63, 9 59x73, 8 47x55, 6 33x141, 5 63x82, 4 59x82, 3 60x126, 1 58x73
// 105-206 (25)	1126.341sec	55 0x0, 50 55x0, 47 0x55, 45 0x102, 44 61x162, 40 65x87, 37 68x50, 35 70x127, 33 0x173, 28 33x178, 26 0x147, 25 45x137, 21 47x63, 20 45x102, 19 26x147, 18 47x84, 16 45x162, 15 45x122, 13 55x50, 12 33x166, 10 60x127, 8 47x55, 7 26x166, 5 60x122, 3 65x84
// 105-215 (21)	1363.987sec	57 0x0, 54 51x120, 51 0x99, 48 57x0, 42 0x57, 41 64x174, 39 66x48, 36 0x179, 33 72x87, 29 0x150, 28 36x187, 24 42x57, 22 29x150, 21 51x99, 18 42x81, 15 36x172, 13 51x174, 12 60x87, 9 57x48, 7 29x172, 6 60x81
// 105-198 (27)	1378.369sec	59 0x0, 46 59x0, 42 63x120, 41 64x79, 39 0x159, 38 0x93, 36 69x162, 34 0x59, 33 72x46, 30 39x168, 28 0x131, 26 38x93, 25 38x119, 24 39x144, 20 52x59, 18 34x59, 16 34x77, 14 50x79, 13 59x46, 11 28x148, 10 28x131, 7 28x141, 6 63x162, 4 35x144, 3 35x141, 2 50x77, 1 63x119
// 105-304 (23)	1419.839sec	58 0x107, 56 0x165, 54 0x53, 53 0x0, 52 53x0, 51 54x52, 49 56x182, 47 58x103, 46 0x221, 40 65x264, 37 0x267, 33 72x231, 32 73x150, 28 37x276, 26 46x231, 19 46x257, 17 56x165, 15 58x150, 10 46x221, 9 37x267, 7 65x257, 4 54x103, 1 53x52
// 105-134 (17)	1473.312sec	68 0x0, 66 0x68, 39 66x95, 37 68x0, 23 82x57, 20 85x37, 17 68x37, 16 66x68, 15 90x80, 14 68x54, 11 66x84, 8 82x80, 7 83x88, 6 77x89, 5 77x84, 3 82x54, 1 82x88
// 105-141 (16)	2747.105sec	56 0x0, 55 0x86, 50 55x91, 49 56x0, 42 63x49, 30 0x56, 17 30x56, 16 47x56, 13 30x73, 11 52x72, 9 43x77, 8 55x83, 7 56x49, 5 47x72, 4 43x73, 3 52x83
// 105-308 (34)	2930.81sec	57 48x197, 55 0x0, 54 51x254, 51 0x257, 50 55x0, 48 0x209, 46 0x93, 43 62x154, 38 0x55, 37 68x117, 36 0x173, 35 70x82, 34 0x139, 32 73x50, 26 36x171, 24 46x93, 22 46x117, 21 38x72, 19 34x139, 18 55x50, 17 38x55, 15 53x139, 14 59x68, 13 34x158, 12 36x197, 11 59x82, 9 53x154, 8 54x163, 7 47x164, 6 47x158, 4 55x68, 3 48x254, 2 34x171, 1 53x163
// time: 3581.068sec
//
// 106-118 (25)	380.152sec	64 0x0, 42 64x0, 30 0x64, 27 79x65, 26 80x92, 25 55x93, 24 0x94, 23 83x42, 22 30x64, 19 64x42, 18 24x100, 17 62x76, 15 64x61, 14 30x86, 13 42x105, 12 52x64, 11 44x86, 10 52x76, 8 47x97, 7 55x86, 6 24x94, 5 42x100, 4 79x61, 3 44x97, 1 79x92
// 106-148 (22)	1214.909sec	59 0x0, 47 59x0, 46 0x102, 43 0x59, 36 70x112, 35 71x47, 30 76x82, 28 43x59, 24 46x124, 18 58x87, 15 43x87, 13 46x111, 12 59x47, 11 59x113, 9 46x102, 8 61x105, 7 69x105, 6 55x105, 5 71x82, 3 55x102, 2 59x111, 1 69x112
// 106-128 (22)	1272.717sec	63 0x0, 43 63x0, 37 69x91, 36 0x92, 33 36x95, 29 0x63, 25 81x43, 23 83x68, 19 29x63, 18 63x43, 17 52x78, 15 48x63, 14 69x77, 13 39x82, 11 63x61, 10 29x82, 9 74x68, 7 74x61, 6 63x72, 5 69x72, 4 48x78, 3 36x92
// 106-177 (17)	1670.051sec	61 0x0, 60 0x117, 56 0x61, 46 60x131, 45 61x0, 32 74x99, 28 78x45, 26 80x73, 24 56x75, 18 56x99, 17 61x45, 14 60x117, 13 65x62, 9 56x66, 5 56x61, 4 61x62, 2 78x73
// 106-180 (26)	1908.407sec	60 0x0, 54 52x126, 52 0x128, 46 60x0, 38 0x60, 31 75x46, 30 0x98, 26 80x100, 25 38x81, 23 83x77, 22 30x106, 21 38x60, 20 63x77, 17 63x97, 16 59x61, 15 60x46, 12 68x114, 11 52x106, 9 52x117, 8 30x98, 7 61x119, 5 63x114, 4 59x77, 3 80x97, 2 61x117, 1 59x60
// 106-278 (30)	3252.132sec	62 0x56, 59 0x164, 56 0x0, 55 0x223, 51 55x227, 50 56x0, 47 59x180, 46 0x118, 44 62x50, 31 75x149, 28 78x121, 27 79x94, 19 59x130, 17 62x94, 16 59x164, 15 60x149, 14 46x150, 13 46x137, 12 57x118, 11 46x118, 10 69x111, 9 69x121, 8 46x129, 7 62x111, 6 56x50, 5 54x132, 4 55x223, 3 54x129, 2 57x130, 1 59x149
// time: 3802.39sec
//
// 107-275 (28)	158.577sec	59 0x109, 56 0x168, 55 0x54, 54 0x0, 53 54x0, 52 55x53, 51 56x186, 48 59x105, 38 69x237, 33 74x153, 28 19x247, 23 20x224, 22 47x253, 20 0x224, 19 0x256, 18 56x168, 16 53x237, 15 59x153, 13 43x224, 12 0x244, 10 43x237, 7 12x249, 6 47x247, 5 12x244, 4 55x105, 3 17x244, 2 17x247, 1 54x53
// 107-232 (27)	158.617sec	64 43x130, 59 0x0, 48 59x0, 45 62x85, 44 0x59, 43 0x138, 38 69x194, 37 70x48, 35 0x103, 28 19x204, 27 35x103, 26 44x59, 23 20x181, 22 47x210, 20 0x181, 19 0x213, 18 44x85, 16 53x194, 12 0x201, 11 59x48, 10 43x194, 8 35x130, 7 12x206, 6 47x204, 5 12x201, 3 17x201, 2 17x204
// 107-150 (23)	178.953sec	56 0x0, 51 56x0, 41 36x75, 36 0x79, 35 0x115, 34 35x116, 30 77x78, 27 80x51, 24 56x51, 23 0x56, 22 85x108, 20 87x130, 19 37x56, 18 69x132, 16 69x116, 14 23x56, 9 23x70, 8 77x108, 5 32x70, 4 32x75, 3 77x75, 2 85x130, 1 35x115
// 107-256 (24)	388.252sec	64 0x62, 62 0x0, 59 48x197, 48 0x208, 45 0x126, 44 63x153, 43 64x75, 37 0x171, 35 72x118, 28 79x47, 27 45x126, 26 37x171, 24 83x23, 23 84x0, 22 62x0, 21 62x22, 18 45x153, 17 62x43, 15 64x60, 11 37x197, 8 64x118, 4 79x43, 2 62x60, 1 83x22
// 107-141 (19)	824.397sec	55 0x0, 52 55x0, 46 0x95, 40 0x55, 34 73x52, 32 46x109, 29 78x112, 26 81x86, 23 58x86, 18 55x52, 17 40x70, 16 57x70, 15 40x55, 12 46x97, 10 48x87, 8 40x87, 3 78x109, 2 46x95, 1 57x86
// 107-225 (24)	1717.053sec	65 0x0, 58 49x167, 49 0x176, 44 0x65, 42 65x0, 40 0x136, 37 44x65, 34 40x133, 33 74x134, 32 75x102, 31 44x102, 27 0x109, 26 81x76, 23 65x42, 19 88x42, 17 27x109, 15 92x61, 11 81x65, 10 27x126, 9 40x167, 7 37x126, 4 88x61, 3 37x133, 1 74x133
// 107-184 (24)	1795.952sec	64 0x0, 58 49x126, 49 0x135, 43 64x0, 40 0x95, 37 70x89, 31 0x64, 30 40x96, 24 83x43, 22 85x67, 21 31x64, 19 64x43, 18 52x78, 15 70x74, 14 52x64, 12 66x62, 11 41x85, 10 31x85, 9 40x126, 7 78x67, 5 78x62, 4 66x74, 2 64x62, 1 40x95
// 107-210 (19)	1954.061sec	62 0x0, 55 0x101, 54 0x156, 53 54x157, 52 55x105, 45 62x0, 39 0x62, 32 75x73, 28 79x45, 23 39x62, 20 55x85, 17 62x45, 16 39x85, 13 62x72, 10 62x62, 7 72x62, 4 75x69, 3 72x69, 1 54x156
// 107-122 (13)	2060.864sec	62 0x0, 60 0x62, 47 60x75, 45 62x0, 30 77x45, 15 62x45, 9 60x66, 8 69x67, 7 70x60, 6 64x60, 4 60x62, 2 62x60, 1 69x66
// 107-264 (29)	2433.986sec	60 0x0, 58 0x60, 56 51x208, 51 0x213, 49 58x79, 47 60x0, 43 0x170, 41 66x128, 39 68x169, 32 75x47, 28 20x118, 25 43x169, 24 19x146, 23 43x146, 20 0x118, 19 0x151, 18 48x128, 17 58x62, 15 60x47, 14 54x194, 13 0x138, 11 43x194, 10 48x118, 8 43x205, 7 13x138, 6 13x145, 3 51x205, 2 58x60, 1 19x145
// 107-160 (15)	2762.617sec	59 0x0, 55 52x105, 52 0x108, 49 0x59, 48 59x0, 32 75x73, 26 49x79, 25 82x48, 20 49x59, 13 69x60, 12 70x48, 11 59x48, 6 69x73, 3 49x105, 1 69x59
// 107-171 (20)	3920.632sec	56 0x0, 55 52x116, 52 0x119, 51 56x0, 35 72x51, 32 0x87, 31 0x56, 30 77x86, 25 52x91, 24 48x67, 20 32x99, 17 31x70, 16 56x51, 14 31x56, 12 32x87, 11 45x56, 8 44x91, 5 72x86, 4 44x87, 3 45x67
// time: 4722.391sec
//
// 108-240 (27)	369.263sec	69 0x57, 63 0x126, 57 0x0, 51 0x189, 45 63x132, 39 69x93, 33 75x177, 30 78x210, 29 57x0, 28 57x29, 27 51x213, 24 51x189, 23 85x37, 22 86x0, 20 69x73, 19 89x74, 16 69x57, 15 93x22, 14 94x60, 12 63x177, 9 85x60, 8 85x29, 7 86x22, 6 63x126, 5 89x69, 4 85x69, 3 75x210
// 108-204 (22)	1752.64sec	58 0x0, 57 0x99, 51 57x92, 50 58x0, 48 0x156, 42 66x50, 41 0x58, 33 75x171, 28 80x143, 27 48x177, 25 41x58, 21 48x156, 16 41x83, 13 57x143, 11 69x160, 10 70x143, 9 57x83, 8 58x50, 7 73x153, 6 69x171, 4 69x156, 3 70x153
// 108-132 (22)	1824.105sec	57 0x0, 51 57x0, 47 61x85, 34 74x51, 33 0x99, 28 33x104, 24 22x57, 23 38x81, 22 0x57, 20 0x79, 18 20x81, 17 57x51, 13 46x68, 11 46x57, 9 65x76, 8 66x68, 7 59x68, 6 59x75, 5 33x99, 4 61x81, 2 20x79, 1 65x75
// 108-249 (23)	1999.53sec	60 0x0, 58 50x116, 53 0x60, 50 0x113, 48 60x0, 47 0x202, 41 67x174, 39 0x163, 36 72x48, 34 74x215, 32 76x84, 28 39x174, 27 47x222, 23 53x93, 20 47x202, 19 53x60, 14 53x79, 12 60x48, 11 39x163, 9 67x84, 7 67x215, 5 67x79, 3 50x113
// 108-189 (24)	2249.708sec	57 0x0, 51 57x0, 44 64x145, 42 0x57, 39 69x81, 38 31x99, 35 0x154, 31 0x99, 30 78x51, 29 35x160, 27 42x72, 25 83x120, 24 0x130, 23 41x137, 21 57x51, 17 24x137, 15 42x57, 14 69x120, 11 72x134, 9 69x72, 8 64x137, 7 24x130, 6 35x154, 3 69x134
// 108-143 (23)	2355.936sec	55 0x0, 53 55x0, 45 0x98, 43 0x55, 36 72x107, 29 79x78, 27 45x116, 25 83x53, 21 62x53, 19 43x66, 18 45x98, 17 62x74, 16 63x91, 13 43x85, 11 43x55, 9 63x107, 8 54x58, 7 56x91, 6 56x85, 5 57x53, 4 79x74, 3 54x55, 2 55x53
// 108-196 (22)	2495.572sec	61 47x91, 55 0x0, 53 55x0, 47 0x112, 44 64x152, 38 70x53, 37 0x159, 31 0x55, 27 37x169, 26 0x86, 23 47x68, 21 26x91, 17 47x152, 16 31x75, 15 55x53, 13 42x55, 11 31x55, 10 37x159, 9 31x66, 7 40x68, 5 26x86, 2 40x66
// 108-170 (18)	2791.037sec	61 0x0, 51 57x80, 47 61x0, 42 0x128, 39 69x131, 36 0x61, 33 75x47, 31 0x97, 27 42x143, 26 31x102, 21 36x81, 20 36x61, 19 56x61, 15 42x128, 14 61x47, 12 57x131, 5 31x97, 1 56x80
// 108-135 (22)	2844.789sec	57 0x0, 51 57x0, 40 0x95, 39 69x96, 38 0x57, 29 40x106, 27 57x51, 24 84x51, 21 87x75, 19 38x57, 18 69x78, 16 53x90, 13 40x93, 12 57x78, 10 47x76, 9 38x76, 8 38x85, 7 46x86, 4 53x86, 3 84x75, 2 38x93, 1 46x85
// 108-187 (18)	3342.74sec	66 0x63, 63 0x0, 58 0x129, 50 58x137, 45 63x0, 42 66x95, 27 81x45, 18 63x45, 17 66x78, 15 66x63, 13 83x82, 12 96x83, 11 97x72, 10 87x72, 8 58x129, 6 81x72, 4 83x78, 1 96x82
// time: 6230.5sec
//
// 111-201 (20)	993.725sec	68 0x64, 64 0x0, 50 61x151, 47 64x0, 43 68x77, 36 25x165, 33 28x132, 31 80x120, 30 81x47, 28 0x132, 25 0x176, 19 61x132, 17 64x47, 16 0x160, 13 68x64, 12 68x120, 9 16x167, 7 16x160, 5 23x160, 2 23x165
// 111-119 (20)	1174.795sec	64 0x0, 47 64x0, 41 70x78, 31 80x47, 28 0x64, 27 0x92, 23 28x64, 22 48x97, 21 27x98, 19 51x78, 16 64x47, 15 65x63, 14 51x64, 11 33x87, 7 44x87, 6 27x92, 5 28x87, 4 44x94, 3 48x94, 1 64x63
// 111-145 (14)	1254.403sec	60 0x0, 52 59x93, 51 60x0, 42 69x51, 36 0x60, 33 36x60, 32 27x113, 27 0x118, 22 0x96, 20 39x93, 17 22x96, 9 60x51, 5 22x113, 3 36x93
// 111-159 (22)	1377.05sec	63 0x0, 53 0x106, 48 63x0, 43 0x63, 33 78x48, 31 80x81, 30 53x129, 28 83x131, 27 53x102, 21 59x81, 19 92x112, 18 60x63, 17 43x63, 16 43x80, 15 63x48, 12 80x112, 10 43x96, 7 85x124, 6 53x96, 5 80x124, 2 83x129, 1 59x80
// 111-201 (20)	1387.623sec	68 0x64, 64 0x0, 50 61x151, 47 64x0, 43 68x77, 36 25x132, 33 28x168, 31 80x120, 30 81x47, 28 0x173, 25 0x132, 19 61x132, 17 64x47, 16 0x157, 13 68x64, 12 68x120, 9 16x157, 7 16x166, 5 23x168, 2 23x166
// 111-224 (26)	1863.136sec	60 0x0, 51 60x0, 46 65x178, 45 66x133, 42 69x51, 40 71x93, 37 29x128, 36 0x60, 35 36x93, 34 31x190, 33 36x60, 31 0x193, 29 0x136, 28 0x165, 25 28x165, 21 0x115, 19 0x96, 17 19x96, 15 21x113, 13 53x165, 12 53x178, 9 60x51, 8 21x128, 5 66x128, 3 28x190, 2 19x113
// 111-201 (20)	2257.336sec	68 0x64, 64 0x0, 50 61x151, 47 64x0, 43 68x77, 36 0x132, 33 0x168, 31 80x120, 30 81x47, 28 33x173, 25 36x132, 19 61x132, 17 64x47, 16 45x157, 13 68x64, 12 68x120, 9 36x157, 7 38x166, 5 33x168, 2 36x166
// 111-127 (15)	2310.158sec	63 0x0, 48 63x0, 43 68x84, 36 75x48, 35 33x92, 33 0x94, 31 0x63, 29 31x63, 15 60x69, 12 63x48, 9 66x60, 8 60x84, 6 60x63, 3 63x60, 2 31x92
// 111-185 (21)	2873.339sec	60 0x57, 57 0x0, 54 57x0, 51 60x54, 40 36x145, 36 0x149, 35 76x150, 32 0x117, 28 32x117, 26 85x105, 25 60x105, 19 92x131, 15 60x130, 11 81x139, 9 75x130, 8 84x131, 6 75x139, 5 76x145, 4 32x145, 3 57x54, 1 84x130
// 111-201 (20)	2889.627sec	68 0x64, 64 0x0, 50 61x151, 47 64x0, 43 68x77, 36 0x165, 33 0x132, 31 80x120, 30 81x47, 28 33x132, 25 36x176, 19 61x132, 17 64x47, 16 45x160, 13 68x64, 12 68x120, 9 36x167, 7 38x160, 5 33x160, 2 36x165
// 111-328 (26)	4323.903sec	68 0x64, 64 0x0, 61 0x132, 60 0x268, 51 60x277, 50 61x151, 47 64x0, 43 68x77, 42 69x235, 39 0x193, 36 0x232, 34 77x201, 33 36x235, 31 80x120, 30 81x47, 22 39x193, 20 39x215, 19 61x132, 18 59x217, 17 64x47, 16 61x201, 13 68x64, 12 68x120, 9 60x268, 3 36x232, 2 59x215
// 111-217 (20)	4799.991sec	63 0x0, 62 0x155, 60 0x63, 51 60x81, 49 62x168, 48 63x0, 36 75x132, 33 78x48, 32 0x123, 23 52x132, 20 32x135, 18 60x63, 15 63x48, 13 62x155, 12 32x123, 9 51x123, 7 44x123, 5 44x130, 3 49x132, 2 49x130
// 111-145 (14)	5282.165sec	62 0x0, 60 51x85, 51 0x94, 49 62x0, 36 75x49, 32 0x62, 23 52x62, 20 32x62, 13 62x49, 12 32x82, 7 44x87, 5 44x82, 3 49x82, 2 49x85
// 111-158 (17)	5871.735sec	61 0x0, 59 52x99, 52 0x106, 50 61x0, 45 0x61, 29 82x50, 22 45x77, 21 61x50, 20 91x79, 16 45x61, 15 67x71, 13 67x86, 11 80x88, 9 82x79, 7 45x99, 6 61x71, 2 80x86
// 111-259 (25)	6292.654sec	61 0x0, 60 0x105, 57 0x202, 54 57x205, 51 60x89, 50 61x0, 44 0x61, 39 72x50, 37 0x165, 36 75x169, 29 82x140, 28 44x61, 22 60x140, 20 37x182, 18 57x187, 17 37x165, 16 44x89, 13 62x174, 12 63x162, 11 61x50, 9 54x165, 8 54x174, 7 75x162, 5 57x182, 3 60x162
// 111-224 (16)	6297.335sec	61 0x0, 60 0x105, 59 0x165, 52 59x172, 51 60x89, 50 61x0, 44 0x61, 39 72x50, 32 79x140, 28 44x61, 19 60x140, 16 44x89, 13 66x159, 11 61x50, 7 59x165, 6 60x159
// 111-271 (21)	7398.103sec	66 45x84, 63 48x150, 59 0x0, 58 53x213, 53 0x218, 52 59x0, 48 0x170, 45 0x94, 35 0x59, 32 79x52, 31 0x139, 25 35x59, 19 60x65, 17 31x153, 14 31x139, 13 66x52, 10 35x84, 7 59x52, 6 60x59, 5 48x213, 3 45x150
// 111-208 (22)	7497.506sec	59 0x0, 56 55x152, 55 0x153, 54 0x99, 52 59x0, 45 66x52, 40 0x59, 30 54x122, 27 84x125, 26 40x59, 25 54x97, 17 79x97, 15 96x97, 14 40x85, 13 98x112, 12 54x85, 11 87x114, 8 79x114, 7 59x52, 3 84x122, 2 96x112, 1 54x152
// 111-208 (22)	7502.47sec	59 0x0, 56 55x152, 55 0x153, 54 0x99, 52 59x0, 45 66x52, 40 0x59, 30 81x122, 27 54x125, 26 40x59, 25 86x97, 17 69x97, 15 54x97, 14 40x85, 13 54x112, 12 54x85, 11 67x114, 8 78x114, 7 59x52, 3 78x122, 2 67x112, 1 54x152
// 111-208 (22)	7503.762sec	59 0x0, 56 55x152, 55 0x153, 54 0x99, 52 59x0, 45 66x52, 40 0x59, 30 54x97, 27 84x97, 26 40x59, 25 54x127, 17 79x135, 15 96x137, 14 40x85, 13 98x124, 12 54x85, 11 87x124, 8 79x127, 7 59x52, 3 84x124, 2 96x135, 1 54x152
// 111-208 (22)	7508.318sec	59 0x0, 56 55x152, 55 0x153, 54 0x99, 52 59x0, 45 66x52, 40 0x59, 30 81x97, 27 54x97, 26 40x59, 25 86x127, 17 69x135, 15 54x137, 14 40x85, 13 54x124, 12 54x85, 11 67x124, 8 78x127, 7 59x52, 3 78x124, 2 67x135, 1 54x152
// 111-152 (17)	7869.782sec	58 0x0, 56 0x96, 55 56x97, 53 58x0, 38 0x58, 28 83x53, 25 58x53, 20 38x58, 19 56x78, 18 38x78, 16 95x81, 11 75x86, 9 86x88, 8 75x78, 7 88x81, 5 83x81, 2 86x86
// time: 9802.498sec
//
// 112-161 (27)	498.242sec	58 0x43, 54 58x41, 43 0x0, 41 71x0, 34 78x95, 32 80x129, 30 50x131, 28 43x0, 27 23x134, 23 0x138, 22 56x109, 21 35x101, 19 0x119, 18 0x101, 17 18x101, 16 19x118, 15 43x28, 14 64x95, 13 58x28, 12 35x122, 9 47x122, 8 56x101, 6 58x95, 4 19x134, 3 47x131, 2 78x129, 1 18x118
// 112-115 (20)	756.484sec	57 0x0, 55 57x0, 32 0x57, 31 81x55, 29 83x86, 26 0x89, 25 32x57, 24 57x55, 21 46x94, 20 26x95, 16 67x99, 15 57x79, 13 32x82, 12 45x82, 11 72x88, 9 72x79, 6 26x89, 5 67x94, 2 81x86, 1 45x94
// 112-112 (21)	851.45sec	50 0x0, 42 70x70, 37 75x33, 35 0x50, 33 79x0, 29 50x0, 27 0x85, 25 50x29, 24 46x88, 19 27x93, 18 52x70, 17 35x65, 16 59x54, 15 35x50, 11 35x82, 9 50x54, 8 27x85, 7 52x63, 6 46x82, 4 75x29, 2 50x63
// 112-148 (24)	1034.339sec	48 0x0, 45 67x103, 43 69x60, 39 0x48, 36 48x0, 35 32x113, 32 0x116, 30 39x57, 29 0x87, 28 84x0, 26 29x87, 24 69x36, 21 48x36, 19 93x41, 14 55x87, 13 99x28, 12 55x101, 9 39x48, 8 84x28, 7 92x28, 6 93x35, 3 29x113, 2 67x101, 1 92x35
// 112-163 (22)	1601.799sec	65 0x0, 52 0x111, 47 65x0, 46 0x65, 36 76x95, 32 80x131, 30 46x81, 28 52x135, 26 86x47, 24 52x111, 22 90x73, 21 65x47, 16 46x65, 14 76x81, 13 62x68, 8 82x73, 7 75x74, 6 75x68, 5 81x68, 4 76x131, 3 62x65, 1 81x73
// 112-185 (20)	2010.495sec	64 48x74, 50 0x0, 48 0x50, 47 65x138, 40 72x34, 35 0x150, 34 78x0, 30 35x155, 28 50x0, 27 0x123, 25 0x98, 24 48x50, 23 25x98, 22 50x28, 21 27x121, 17 48x138, 13 35x142, 8 27x142, 6 72x28, 2 25x121
// 112-167 (19)	2058.614sec	60 0x0, 52 60x0, 47 0x120, 44 68x52, 37 75x96, 36 32x60, 34 78x133, 32 0x60, 31 47x136, 28 0x92, 24 28x96, 23 52x96, 17 58x119, 11 47x125, 8 60x52, 6 52x119, 5 47x120, 4 28x92, 3 75x133
// 112-212 (23)	2659.763sec	65 0x59, 59 0x0, 53 59x0, 47 65x53, 46 0x166, 42 0x124, 38 42x124, 37 75x175, 32 80x143, 29 46x183, 24 65x100, 23 89x100, 21 46x162, 20 92x123, 13 67x162, 12 80x131, 8 67x175, 7 80x124, 6 59x53, 5 87x126, 4 42x162, 3 89x123, 2 87x124
// 112-174 (23)	2851.157sec	60 0x0, 52 60x0, 43 69x89, 42 70x132, 38 0x136, 37 75x52, 36 33x89, 33 0x103, 32 38x142, 29 24x60, 24 0x60, 22 53x67, 19 0x84, 17 44x125, 15 60x52, 14 19x89, 11 33x125, 9 61x133, 8 61x125, 7 53x60, 6 38x136, 5 19x84, 1 69x132
// 112-222 (21)	3015.271sec	62 0x115, 57 0x0, 55 57x0, 50 62x133, 46 66x55, 45 0x177, 39 73x183, 35 31x80, 32 80x101, 31 0x84, 28 45x194, 27 0x57, 23 27x57, 18 62x115, 17 45x177, 16 50x64, 14 66x101, 11 62x183, 9 57x55, 7 50x57, 4 27x80
// 112-196 (22)	5321.946sec	61 0x0, 59 0x137, 53 59x143, 51 61x0, 41 71x51, 40 0x61, 36 0x101, 31 40x61, 27 59x116, 26 86x117, 25 87x92, 24 63x92, 23 36x114, 14 49x92, 13 36x101, 10 61x51, 9 40x92, 8 49x106, 6 57x106, 4 59x112, 2 57x112, 1 86x116
// time: 7213.561sec
//
// 113-189 (18)	1407.168sec	66 0x0, 64 0x66, 59 0x130, 54 59x135, 49 64x86, 31 82x55, 28 85x27, 27 86x0, 20 66x0, 19 66x33, 18 64x68, 16 66x52, 13 66x20, 7 79x20, 6 79x27, 5 59x130, 3 82x52, 2 64x66
// 113-226 (23)	1541.287sec	68 0x65, 65 0x0, 52 0x133, 48 65x0, 45 68x79, 41 0x185, 38 75x160, 36 77x124, 31 82x48, 28 85x198, 26 41x200, 25 52x133, 23 52x158, 19 56x181, 18 67x208, 17 65x48, 15 41x185, 14 68x65, 10 75x198, 9 68x124, 8 67x200, 4 52x181, 2 75x158
// 113-137 (21)	1609.882sec	58 0x0, 45 68x92, 42 71x50, 40 0x58, 39 0x98, 31 40x58, 29 39x108, 28 85x0, 27 58x0, 22 91x28, 19 49x89, 18 58x27, 15 76x35, 13 58x45, 10 39x98, 9 40x89, 8 76x27, 7 84x28, 5 71x45, 3 68x89, 1 84x27
// 113-144 (25)	2052.386sec	60 0x0, 53 60x0, 48 0x96, 36 0x60, 32 81x53, 31 82x113, 28 85x85, 24 36x60, 22 48x84, 21 60x53, 20 48x106, 18 48x126, 16 66x128, 15 70x85, 14 68x114, 12 36x84, 11 70x74, 10 60x74, 9 76x100, 8 68x106, 6 70x100, 5 76x109, 4 81x109, 2 66x126, 1 81x113
// 113-211 (29)	2354.721sec	61 0x0, 52 61x0, 48 65x163, 45 0x61, 44 69x82, 42 0x106, 37 76x126, 33 32x178, 32 0x179, 31 0x148, 30 83x52, 27 42x106, 24 45x82, 21 45x61, 19 57x133, 18 47x160, 17 66x65, 16 31x162, 15 42x133, 14 31x148, 13 70x52, 12 45x148, 11 65x152, 9 61x52, 8 57x152, 7 69x126, 4 66x61, 2 45x160, 1 31x178
// 113-211 (29)	2356.475sec	61 0x0, 52 61x0, 48 65x163, 45 0x61, 44 69x82, 42 0x106, 37 76x126, 33 32x178, 32 0x179, 31 0x148, 30 83x52, 27 42x106, 24 45x82, 21 45x61, 19 31x159, 18 42x133, 17 66x65, 16 60x133, 15 50x163, 14 62x149, 13 70x52, 12 50x151, 11 31x148, 9 61x52, 8 42x151, 7 69x126, 4 66x61, 2 60x149, 1 31x178
// 113-273 (21)	4235.587sec	62 0x115, 58 0x57, 57 0x0, 56 57x0, 55 58x56, 54 0x177, 51 62x111, 42 0x231, 41 72x232, 36 77x162, 34 79x198, 30 42x243, 25 54x200, 23 54x177, 18 54x225, 15 62x162, 12 42x231, 7 72x225, 4 58x111, 2 77x198, 1 57x56
// 113-250 (24)	4385.364sec	60 53x146, 58 0x57, 57 0x0, 56 57x0, 55 58x56, 53 0x152, 45 0x205, 44 69x206, 37 0x115, 35 78x111, 24 45x226, 21 37x115, 20 58x111, 16 37x136, 15 63x131, 13 45x213, 11 58x215, 10 53x136, 9 60x206, 8 45x205, 7 53x206, 5 58x131, 2 58x213, 1 57x56
// 113-207 (23)	4450.359sec	68 0x0, 62 51x145, 51 0x156, 48 0x68, 45 68x0, 44 69x101, 40 0x116, 29 40x116, 27 48x68, 23 68x45, 22 91x45, 21 48x95, 20 75x81, 18 95x83, 16 97x67, 13 75x68, 11 40x145, 9 88x72, 6 69x95, 5 92x67, 4 88x68, 2 95x81, 1 91x67
// 113-165 (15)	4944.389sec	66 0x0, 58 0x107, 55 58x110, 47 66x0, 41 0x66, 35 78x75, 28 85x47, 24 41x66, 20 58x90, 19 66x47, 17 41x90, 13 65x77, 11 65x66, 9 76x66, 2 76x75
// 113-190 (17)	5179.0sec	66 0x0, 64 0x126, 60 0x66, 49 64x141, 47 66x0, 34 79x107, 31 82x76, 29 84x47, 22 60x85, 19 60x107, 18 66x47, 15 64x126, 13 60x72, 11 73x65, 9 73x76, 7 66x65, 6 60x66
// 113-207 (24)	5189.341sec	65 0x0, 64 49x143, 56 57x87, 49 0x158, 48 65x0, 34 0x124, 30 0x94, 29 0x65, 28 29x65, 27 30x93, 23 34x120, 22 91x65, 20 71x67, 19 65x48, 17 96x48, 15 34x143, 14 57x73, 12 84x48, 8 57x65, 7 84x60, 6 65x67, 5 91x60, 4 30x120, 1 29x93
// 113-207 (24)	5194.807sec	65 0x0, 64 49x143, 56 57x87, 49 0x158, 48 65x0, 34 0x124, 30 0x94, 29 0x65, 28 29x65, 27 30x93, 23 34x120, 22 57x65, 20 93x67, 19 94x48, 17 65x48, 15 34x143, 14 79x73, 12 82x48, 8 79x65, 7 87x60, 6 87x67, 5 82x60, 4 30x120, 1 29x93
// 113-167 (20)	5289.043sec	65 0x0, 58 0x109, 55 58x112, 48 65x0, 44 0x65, 36 77x76, 28 85x48, 20 65x48, 18 44x65, 17 60x83, 16 44x83, 15 62x68, 12 65x100, 10 44x99, 8 77x68, 7 58x105, 6 54x99, 5 60x100, 4 54x105, 3 62x65
// 113-247 (17)	5545.935sec	68 0x65, 65 0x0, 63 0x184, 51 0x133, 50 63x197, 48 65x0, 45 68x79, 37 76x160, 36 77x124, 31 82x48, 26 51x133, 25 51x159, 17 65x48, 14 68x65, 13 63x184, 9 68x124, 1 76x159
// 113-161 (18)	5914.058sec	64 0x0, 59 0x102, 54 59x107, 49 64x0, 38 0x64, 30 83x49, 28 85x79, 26 59x81, 21 38x81, 19 64x49, 17 38x64, 13 70x68, 9 55x64, 8 55x73, 7 63x74, 6 64x68, 2 83x79, 1 63x73
// 113-199 (21)	6225.71sec	64 0x0, 63 50x136, 53 60x83, 50 0x149, 49 64x0, 37 0x112, 34 79x49, 32 28x64, 28 0x64, 23 37x113, 20 0x92, 19 60x64, 17 43x96, 15 64x49, 13 37x136, 12 20x100, 11 32x96, 8 20x92, 6 37x107, 5 32x107, 4 28x96
// time: 12387.971sec
//