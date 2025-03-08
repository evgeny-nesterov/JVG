package ru.nest;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
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
					while (++lx2 < width && (ly = front[lx2]) == currentLevel) ;
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

		class L {
			int x1, x2, y;
			L prev, next;

			L addLevel(int size) {
				int newX1 = x1 + size;
				int newY = y + size;
				if (newX1 == x2) {
					if (prev != null && next != null && prev.y == newY && next.y == newY) {
						prev.x2 = next.x2;
						prev.next = next.next;
						if (next.next != null) {
							next.next.prev = prev;
						}
						levels[++levelsCount] = this;
						levels[++levelsCount] = next;
						return prev;
					} else if (prev != null && prev.y == newY) {
						prev.x2 = x2;
						prev.next = next;
						if (next != null) {
							next.prev = prev;
						}
						levels[++levelsCount] = this;
						return prev;
					} else if (next != null && next.y == newY) {
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
						return next;
					} else {
						y = newY;
						return this;
					}
				} else if (prev != null && prev.y == newY) {
					prev.x2 = newX1;
					x1 = newX1;
					levelsX[x1] = this;
					return this;
				} else {
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
					}
					levelsX[x1] = l;

					x1 = newX1;
					prev = l;
					levelsX[x1] = this;
					return l;
				}
			}

			void removeLevel(int lx1, int lx2, int size) {
				int newY = y - size;
				if (lx1 > x1 && lx2 < x2) {
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
				} else if (lx1 == x1 && lx2 == x2) {
					y = newY;
					if (next != null && next.y == y) {
						x2 = next.x2;
						if (next.next != null) {
							next.next.prev = this;
						}
						levels[++levelsCount] = next;
						next = next.next;
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

		boolean start() {
			startLevelOpt(0, 0, X);
			return results.size() > 0;
		}

		void removeLevel(int levelX1, int levelX2, int size) {
			L l = firstLevel;
			while (l != null) {
				if (levelX1 >= l.x1 && levelX2 <= l.x2) {
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
			if (levelX2 == X && level == 0 && levelLength < sequence[0]) {
				return;
			}
			for (int quad = 1; quad <= levelLength; quad++) {
				if (!busy[quad]) {
					busy[quad] = true;
					L curerntLevel = levelsX[levelX1].addLevel(quad);
					sequence[n++] = quad;

					// printProcess();
					// printLevels();

					int x2 = levelX1 + quad;
					if (x2 != levelX2 && (levelX1 == 0 || curerntLevel.prev.y >= level + quad)) {
						startLevelOpt(level, x2, levelX2);
					} else {
						if (firstLevel.next == null) {
							if (firstLevel.y >= X && n > 1) {
								result(firstLevel.y);
							}
						} else {
							L bestLevel = null;
							L l = firstLevel;
							while (l != null) {
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
							startLevelOpt(bestLevel.y, bestLevel.x1, bestLevel.x2);
						}
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
							while (++lx2 < X && (ly = front[lx2]) == currentLevel) ;
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
					while (++lx2 < X && (ly = front[lx2]) == currentLevel) ;
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
		q1(32, 112, 8);
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