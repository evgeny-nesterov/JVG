package ru.nest.q;

import ru.nest.layout.PageLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class QFrame extends JFrame {
	public QFrame() {
		setTitle("Quads");
		setLayout(new PageLayout(10, 10));
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	List<QPanel> panels = new ArrayList<>();

	public void setQuadsCount(int count) {
		for (int i = 0; i < count; i++) {
			QPanel p = new QPanel();
			add(p);
			panels.add(p);
		}
	}

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
			setPreferredSize(new Dimension(650, 650));
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					paused = false;
				}
			});
		}

		int quadSize;

		int[] sequence;

		boolean paused;

		public void setSequence(int quadSize, int[] sequence, int n) {
			this.quadSize = quadSize;
			this.sequence = new int[n];
			for (int i = 0; i < n; i++) {
				this.sequence[i] = sequence[i];
			}
			repaint();
		}

		public void clear() {
			sequence = null;
			repaint();
		}

		public void paint(Graphics g) {
			super.paint(g);
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, getWidth(), getHeight());
			if (sequence != null) {
				int X = quadSize;
				int n = sequence.length;
				List<Quad> quads = new ArrayList<>();

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

				double panelSize = getWidth();
				for (Quad q : quads) {
					int qx = (int) (panelSize * q.x / quadSize);
					int qy = (int) (panelSize * (quadSize - q.y - q.size) / quadSize);
					int qw = (int) (panelSize * q.size / quadSize);
					g.setColor(paused ? Color.blue : Color.darkGray);
					g.fillRect(qx + 1, qy + 1, qw - 2, qw - 2);
					if (qw > 10) {
						g.setColor(Color.white);
						String s = Integer.toString(q.size);
						g.drawString(s, qx + (qw - s.length() * 5) / 2, qy + qw / 2 + 4);
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		QFrame f = new QFrame();
		f.setQuadsCount(6);
		f.setQuad(0, 171, new int[] {15, 8, 9, 7, 1, 33, 10, 18, 4, 14, 35, 27, 17, 16, 5, 22, 24, 20, 2, 25, 6, 3, 12});
		f.setQuad(1, 171, new int[] {14, 10, 9, 26, 1, 8, 4, 7, 18, 15, 46, 6, 20, 39, 66, 25, 41, 64, 21, 45, 38, 24, 22, 27, 11, 35, 5, 2});
		f.setQuad(2, 171, new int[] {12, 14, 10, 2, 18, 11, 5, 6, 4, 17, 16, 15, 8, 7, 3, 61});
		f.setQuad(3, 171, new int[] {11, 14, 8, 3, 26, 5, 12, 13, 32, 6, 25, 7, 19, 16, 15, 35, 1, 10, 2});
		f.setQuad(4, 171, new int[] {13, 11, 20, 2, 9, 15, 41, 8, 21, 23, 39, 47, 4, 5, 30, 22, 25, 10, 7, 37, 3, 1});
		f.setQuad(5, 171, new int[] {9, 8, 15, 1, 7, 10, 33, 4, 18, 14, 52, 54, 38, 27, 30, 19, 3, 11, 16, 17, 23, 5, 2});
		f.setVisible(true);
	}
}
