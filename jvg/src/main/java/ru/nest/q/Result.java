package ru.nest.q;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Result {
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
		String size = (width < 100 ? " " : "") + width + "-" + height + (height < 100 ? " " : "");
		String length = (quads.length < 10 ? " " : "") + " (" + quads.length + ")";
		String durationSec = "" + (duration / 1000.0);
		while (durationSec.indexOf('.') <= 3) {
			durationSec = " " + durationSec;
		}
		while (durationSec.length() - durationSec.indexOf('.') <= 3) {
			durationSec += "0";
		}

		String string = size + length + " " + durationSec + "sec\t";
		for (int i = 0; i < quads.length; i++) {
			if (i > 0) {
				string += " ";
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
				if (ly > currentLevel || lx2 == width) {
					if (lx2 - lx1 < bestLevelX2 - bestLevelX1) {
						bestLevel = currentLevel;
						bestLevelX1 = lx1;
						bestLevelX2 = lx2;
					}
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
		int[] sequence = new int[quads.length];
		for (int i = 0; i < quads.length; i++) {
			sequence[quads[i].index] = quads[i].size;
		}
		return sequence;
	}

	boolean isVertical() {
		int[] heights = new int[width];
		for (int i = 0; i < quads.length; i++) {
			heights[quads[i].x] += quads[i].size;
		}
		for (int i = 1; i < width; i++) {
			if (heights[i] == height) {
				return true;
			}
		}
		return false;
	}
}