package ru.nest.q;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QFrame extends JFrame {
	public QFrame(int count) {
		setTitle("Quads");
		setLayout(new QuadLayout(10));
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		for (int i = 0; i < count; i++) {
			QPanel p = new QPanel();
			add(p);
			panels.add(p);
		}
	}

	List<QPanel> panels = new ArrayList<>();

	public QPanel getQPanel(int index) {
		return panels.get(index);
	}

	public void setQuad(int index, int quadSize, int[] sequence) {
		setQuad(index, quadSize, sequence, sequence.length);
	}

	public void setQuad(int index, int quadSize, int[] sequence, int n) {
		panels.get(index).setSequence(quadSize, sequence, n);
	}

	class QPanel extends JComponent {
		QPanel() {
			addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					paused = !paused;
				}
			});
		}

		int quadSize;

		int[] sequence;

		Result result;

		boolean paused;

		public void setSequence(int quadSize, int[] sequence, int n) {
			this.quadSize = quadSize;
			this.sequence = new int[n];
			for (int i = 0; i < n; i++) {
				this.sequence[i] = sequence[i];
			}
			repaint();
		}

		public void setResult(Result result) {
			this.result = result;
			this.quadSize = result.width;
			repaint();
		}

		public void clear() {
			sequence = null;
			repaint();
		}

		Random r = new Random();

		public void paint(Graphics g) {
			super.paint(g);
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, getWidth(), getHeight());

			List<Quad> quads = new ArrayList<>();
			if (sequence != null) {
				int X = quadSize;
				int n = sequence.length;

				int x1 = 0, x2, y = 0, index = 0;
				int[] front = new int[X];
				while (index < n) {
					Quad quad = new Quad(x1, y, sequence[index], index++);
					quads.add(quad);
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
			} else if (result != null) {
				quads = List.of(result.quads);
			}

			double panelSize = getWidth();
			for (Quad q : quads) {
				int qx = (int) (panelSize * q.x / quadSize);
				int qy = (int) (panelSize * (quadSize - q.y - q.size) / quadSize);
				int qx2 = (int) (panelSize * (q.x + q.size) / quadSize);
				int qy2 = (int) (panelSize * (quadSize - q.y) / quadSize);
				g.setColor(paused ? Color.blue : new Color(r.nextInt(200), r.nextInt(200), r.nextInt(200)));
				g.fillRect(qx + 1, qy + 1, qx2 - qx - 1, qy2 - qy - 1);
				int qw = qx2 - qx;
				if (qw > 10) {
					g.setColor(Color.white);
					String s = Integer.toString(q.size);
					g.drawString(s, qx + (qw - s.length() * 5) / 2, qy + qw / 2 + 4);
				}
			}
		}
	}

	public static void main(String[] args) {
		QFrame f = new QFrame(16);
		f.setQuad(0, 171, new int[] {15, 8, 9, 7, 1, 33, 10, 18, 4, 14, 35, 27, 17, 16, 5, 22, 24, 20, 2, 25, 6, 3, 12});
		f.setQuad(1, 171, new int[] {14, 10, 9, 26, 1, 8, 4, 7, 18, 15, 46, 6, 20, 39, 66, 25, 41, 64, 21, 45, 38, 24, 22, 27, 11, 35, 5, 2});
		f.setQuad(2, 171, new int[] {12, 14, 10, 2, 18, 11, 5, 6, 4, 17, 16, 15, 8, 7, 3, 61});
		f.setQuad(3, 171, new int[] {11, 14, 8, 3, 26, 5, 12, 13, 32, 6, 25, 7, 19, 16, 15, 35, 1, 10, 2});
		f.setQuad(4, 171, new int[] {13, 11, 20, 2, 9, 15, 41, 8, 21, 23, 39, 47, 4, 5, 30, 22, 25, 10, 7, 37, 3, 1});
		f.setQuad(5, 171, new int[] {9, 8, 15, 1, 7, 10, 33, 4, 18, 14, 52, 54, 38, 27, 30, 19, 3, 11, 16, 17, 23, 5, 2});
		f.setQuad(6, 171, new int[] {9, 8, 15, 1, 7, 10, 33, 4, 18, 14, 52, 54, 38, 27, 30, 19, 3, 11, 16, 17, 23, 5, 2});
		f.setQuad(7, 171, new int[] {9, 8, 15, 1, 7, 10, 33, 4, 18, 14, 52, 54, 38, 27, 30, 19, 3, 11, 16, 17, 23, 5, 2});
		f.setQuad(8, 171, new int[] {9, 8, 15, 1, 7, 10, 33, 4, 18, 14, 52, 54, 38, 27, 30, 19, 3, 11, 16, 17, 23, 5, 2});
		f.setQuad(9, 171, new int[] {9, 8, 15, 1, 7, 10, 33, 4, 18, 14, 52, 54, 38, 27, 30, 19, 3, 11, 16, 17, 23, 5, 2});
		f.setQuad(10, 171, new int[] {9, 8, 15, 1, 7, 10, 33, 4, 18, 14, 52, 54, 38, 27, 30, 19, 3, 11, 16, 17, 23, 5, 2});
		f.setQuad(11, 171, new int[] {9, 8, 15, 1, 7, 10, 33, 4, 18, 14, 52, 54, 38, 27, 30, 19, 3, 11, 16, 17, 23, 5, 2});
		f.setQuad(12, 171, new int[] {9, 8, 15, 1, 7, 10, 33, 4, 18, 14, 52, 54, 38, 27, 30, 19, 3, 11, 16, 17, 23, 5, 2});
		f.setQuad(13, 171, new int[] {9, 8, 15, 1, 7, 10, 33, 4, 18, 14, 52, 54, 38, 27, 30, 19, 3, 11, 16, 17, 23, 5, 2});
		f.setQuad(14, 171, new int[] {9, 8, 15, 1, 7, 10, 33, 4, 18, 14, 52, 54, 38, 27, 30, 19, 3, 11, 16, 17, 23, 5, 2});
		f.setQuad(15, 171, new int[] {9, 8, 15, 1, 7, 10, 33, 4, 18, 14, 52, 54, 38, 27, 30, 19, 3, 11, 16, 17, 23, 5, 2});
		f.setVisible(true);
	}
}

class QuadLayout implements LayoutManager {
	public QuadLayout() {
	}

	public QuadLayout(int gap) {
		this.gap = gap;
	}

	private int gap = 0;

	@Override
	public void layoutContainer(Container parent) {
		synchronized (parent.getTreeLock()) {
			Component[] components = parent.getComponents();
			int N = components.length;
			Insets insets = parent.getInsets();
			Dimension parent_size = parent.getSize();
			int W = parent_size.width - insets.left - insets.right;
			int H = parent_size.height - insets.top - insets.bottom;

			// a = |W/NX| = |H/NY|, NX x NY >= N, NX x (NY - 1) < N
			int A = 200;
			FOR:
			for (int nx = 1; nx <= N; nx++) {
				for (int ny = 1; ny <= N; ny++) {
					int a = (W - (nx - 1) * gap) / nx;
					if (a * ny + (ny - 1) * gap <= H && nx * ny >= N && nx * (ny - 1) < N) {
						A = a;
						break FOR;
					}
				}
			}

			int x = 0;
			int y = 0;
			int h = 0;
			for (Component c : components) {
				if (x + A < W) {
					if (h < A) {
						h = A;
					}
				} else {
					x = 0;
					y += h + gap;
					h = 0;
				}

				c.setBounds(x + insets.left, y + insets.top, A, A);
				x += A + gap;
			}
		}
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return preferredLayoutSize(parent);
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		int A = 100;
		synchronized (parent.getTreeLock()) {
			Insets insets = parent.getInsets();
			int parent_width = parent.getWidth() - insets.left - insets.right;

			int w = insets.left;
			int h = insets.top;
			int row_width = 0;
			int row_height = 0;

			for (Component c : parent.getComponents()) {
				if (row_width + A < parent_width) {
					row_width += A + gap;
					if (row_height < A) {
						row_height = A;
					}
				} else {
					if (w < row_width) {
						w = row_width;
					}
					h += row_height + gap;

					row_width = A;
					row_height = A;
				}
			}

			if (w < row_width) {
				w = row_width;
			}
			h += row_height;
			return new Dimension(w + insets.left + insets.right, h + insets.top + insets.bottom);
		}
	}

	@Override
	public void removeLayoutComponent(Component comp) {
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {
	}

	public int getGap() {
		return gap;
	}

	public void setGap(int gap) {
		this.gap = gap;
	}
}