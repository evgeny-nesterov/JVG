package ru.nest.q;

/**
 * big quad			W x H
 * quads count		N
 * q1	q2	...					 qn
 * |-W		1 ... 1							|
 * |			...							|
 * matrix			|			-1 ... 1 ... -1 ... 1		|
 * |			...							|
 * |-W								1 ... 1 |
 */
public class QSM {
	int n;

	QSM(int n) {
		this.n = n;
		top = new Q();
		quads = new Q[n];
		for (int i = 0; i < n; i++) {
			Q side = new Q(i);
			side.index = i;
			quads[i] = side;
		}
	}

	Q top;

	int quadPos;

	Q[] quads;

	class Q {
		Q(int index) {
			this.index = index;
			hasQuads[index] = true;
		}

		Q() {
		}

		int index;

		int inCount;

		Q[] in = new Q[n];

		int outCount;

		Q[] out = new Q[n];

		boolean[] hasQuads = new boolean[n];

		void add(Q q) {
			out[outCount++] = q;
			q.in[q.inCount++] = this;
		}

		void remove(Q q) {
			outCount--;
			q.inCount--;
		}

		boolean hasPathQuad(Q q) {
			for (int i = 0; i < outCount; i++) {
				if (out[i] == q) {
					return true;
				}
			}
			return hasParentPathQuad(q);
		}

		boolean hasParentPathQuad(Q q) {
			for (int i = 0; i < inCount; i++) {
				Q inQuad = in[i];
				if (inQuad == q || inQuad.hasParentPathQuad(q)) {
					return true;
				}
			}
			return false;
		}

		void start(int startQuadPos) {
			if (quadPos < n) {
				Q q = quads[quadPos++];
				add(q);
				start(startQuadPos);
				q.start(0);
				remove(q);
				quadPos--;
			} else {
				check();
			}

			for (int i = startQuadPos; i < quadPos; i++) {
				Q q = quads[i];
				if (!hasPathQuad(q)) {
					add(q);
					start(i + 1);
					if (quadPos == n) {
						check();
					}
					q.start(0);
					if (quadPos == n) {
						check();
					}
					remove(q);
				}
			}
		}
	}

	void check() {
		System.out.println("--------------------------------");
		System.out.print("top: ");
		for (int j = 0; j < top.outCount; j++) {
			Q out = top.out[j];
			if (j > 0) {
				System.out.print(", ");
			}
			System.out.print(out.index);
		}
		System.out.println();
		for (int i = 0; i < n; i++) {
			Q q = quads[i];
			System.out.print(q.index + ": ");
			for (int j = 0; j < q.outCount; j++) {
				Q out = q.out[j];
				if (j > 0) {
					System.out.print(", ");
				}
				System.out.print(out.index);
			}
			System.out.println();
		}
	}

	void start() {
		top.start(0);
	}

	public static void main(String[] args) {
		new QSM(4).start();
	}
}
