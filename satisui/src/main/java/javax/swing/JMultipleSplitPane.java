package javax.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class JMultipleSplitPane extends JPanel {
	private static final long serialVersionUID = 1L;

	public JMultipleSplitPane() {
		super.setLayout(new SplitLayout());

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					if (e.getClickCount() == 1) {
						update(e.getX());
						if (index >= 0) {
							Component c = getComponent(index);
							Dimension size = c.getPreferredSize();
							w = size.width;

							mx = e.getX();
							pressed = true;
						}
					} else if (e.getClickCount() == 2) {
						if (index >= 0) {
							Component c = getComponent(index);
							c.setPreferredSize(null);
							revalidate();
							repaint();
						}
					}
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				pressed = false;
				update(e.getX());
			}

			@Override
			public void mouseExited(MouseEvent e) {
				if (!pressed) {
					setCursor(Cursor.getDefaultCursor());
				}
			}
		});

		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				if (!pressed) {
					update(e.getX());
				}
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				if (pressed) {
					Component c = getComponent(index);

					Dimension min = c.getMinimumSize();
					Dimension size = c.getPreferredSize();
					size.width = w + e.getX() - mx;
					if (size.width < min.width) {
						size.width = min.width;
					}
					c.setPreferredSize(size);

					revalidate();
					repaint();
				}
			}
		});
	}

	@Override
	public void setLayout(LayoutManager layout) {
	}

	private void update(int pos) {
		index = getIndex(pos);
		if (index >= 0) {
			setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
		} else {
			setCursor(Cursor.getDefaultCursor());
		}
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		int x = 0;
		Insets insets = getInsets();
		if (insets != null) {
			x = insets.left;
		}

		g.setColor(Color.lightGray);
		for (int i = 0; i < getComponentCount(); i++) {
			Component c = getComponent(i);
			if (!c.isVisible()) {
				continue;
			}

			Dimension d = c.getPreferredSize();
			x += d.width;
			int nextX = x + dividerWidth;

			for (int j = 15; j < getHeight(); j += 3) {
				g.drawLine(x, j, nextX, j);
			}

			x = nextX;
		}
	}

	private int index = -1, mx, w;

	private boolean pressed = false;

	private int getIndex(int pos) {
		int x = 0;
		Insets insets = getInsets();
		if (insets != null) {
			x = insets.left;
		}

		for (int i = 0; i < getComponentCount(); i++) {
			Component c = getComponent(i);
			if (!c.isVisible()) {
				continue;
			}

			Dimension d = c.getPreferredSize();
			x += d.width + dividerWidth;

			if (pos <= x && pos >= x - dividerWidth) {
				return i;
			}
		}

		return -1;
	}

	private int dividerWidth = 3;

	class SplitLayout implements LayoutManager {
		@Override
		public void addLayoutComponent(String name, Component comp) {
		}

		@Override
		public void removeLayoutComponent(Component comp) {
		}

		@Override
		public Dimension preferredLayoutSize(Container parent) {
			int w = 0, h = 0, count = 0;
			for (Component c : parent.getComponents()) {
				if (!c.isVisible()) {
					continue;
				}

				Dimension d = c.getPreferredSize();
				w += d.width;
				if (h < d.height) {
					h = d.height;
				}

				count++;
			}

			w += dividerWidth * count;

			Insets insets = parent.getInsets();
			if (insets != null) {
				w += insets.left + insets.right;
				h += insets.top + insets.bottom;
			}

			return new Dimension(w, h);
		}

		@Override
		public Dimension minimumLayoutSize(Container parent) {
			int w = 0, h = 0, count = 0;
			for (Component c : parent.getComponents()) {
				if (!c.isVisible()) {
					continue;
				}

				Dimension d = c.getMinimumSize();
				w += d.width;
				if (h < d.height) {
					h = d.height;
				}

				count++;
			}

			w += dividerWidth * count;

			Insets insets = parent.getInsets();
			if (insets != null) {
				w += insets.left + insets.right;
				h += insets.top + insets.bottom;
			}

			return new Dimension(w, h);
		}

		@Override
		public void layoutContainer(Container parent) {
			int x = 0, y = 0, h = parent.getHeight();
			Insets insets = parent.getInsets();
			if (insets != null) {
				x = insets.left;
				y = insets.top;
				h -= insets.top + insets.bottom;
			}

			for (Component c : parent.getComponents()) {
				if (!c.isVisible()) {
					continue;
				}

				Dimension d = c.getPreferredSize();
				d.height = h;
				c.setSize(d);
				c.setLocation(x, y);
				x += d.width + dividerWidth;
			}
		}
	}

	public static void main(String[] args) {
		JMultipleSplitPane p = new JMultipleSplitPane();
		p.setBackground(Color.white);

		JLabel lbl = new JLabel("[label 1]");
		lbl.setOpaque(true);
		p.add(lbl);

		lbl = new JLabel("[label 2]");
		lbl.setOpaque(true);
		p.add(lbl);

		lbl = new JLabel("[label 3]");
		lbl.setOpaque(true);
		p.add(lbl);

		JFrame f = new JFrame();
		f.setContentPane(p);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setBounds(200, 200, 600, 600);
		f.setVisible(true);
	}
}
