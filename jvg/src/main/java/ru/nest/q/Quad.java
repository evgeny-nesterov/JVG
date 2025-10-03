package ru.nest.q;

public class Quad implements Comparable<Quad> {
	int x, y, size, index;

	Quad(int x, int y, int size, int index) {
		set(x, y, size, index);
	}

	Quad() {
	}

	void set(int x, int y, int size, int index) {
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
		return (size < 100 ? " " : "") + size + " " + (x < 10 ? "  " : (x < 100 ? " " : "")) + x + "x" + y + (y < 10 ? "  " : (y < 100 ? " " : ""));
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