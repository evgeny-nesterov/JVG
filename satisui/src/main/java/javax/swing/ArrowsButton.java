package javax.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ArrowsButton extends JLabel {
	private static final long serialVersionUID = 1L;

	public final static int NONE = -1;

	public final static int LEFT = 0;

	public final static int RIGHT = 1;

	public final static int TOP = 2;

	public final static int BOTTOM = 3;

	public final static int CENTER = 4;

	public ArrowsButton(final ArrowsListener listener) {
		setRequestFocusEnabled(false);
		setPreferredSize(new Dimension(24, 24));
		setMinimumSize(new Dimension(24, 24));
		setMaximumSize(new Dimension(24, 24));
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				pressedButton = getArrow(e.getX(), e.getY());
				if (pressedButton != NONE) {
					listener.arrowPressed(e, pressedButton);
				}
				repaint();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (pressedButton != NONE) {
					listener.arrowReleased(e, pressedButton);

					pressedButton = NONE;
					repaint();
				}
			}
		});
	}

	private int pressedButton = NONE;

	private int getArrow(int x, int y) {
		if (x >= 8 && x <= 16) {
			if (y <= 8) {
				return TOP;
			} else if (y >= 16) {
				return BOTTOM;
			} else {
				return CENTER;
			}
		} else if (y >= 8 && y <= 16) {
			if (x <= 8) {
				return LEFT;
			} else if (x >= 16) {
				return RIGHT;
			} else {
				return CENTER;
			}
		}
		return NONE;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		paintArrow(g, new int[] { 1, 7, 7 }, new int[] { 12, 8, 16 }, pressedButton == LEFT);
		paintArrow(g, new int[] { 23, 17, 17 }, new int[] { 12, 8, 16 }, pressedButton == RIGHT);
		paintArrow(g, new int[] { 12, 8, 16 }, new int[] { 1, 7, 7 }, pressedButton == TOP);
		paintArrow(g, new int[] { 12, 8, 16 }, new int[] { 23, 17, 17 }, pressedButton == BOTTOM);
	}

	private void paintArrow(Graphics g, int[] x, int y[], boolean pressed) {
		if (!pressed) {
			g.translate(2, 2);
			g.setColor(Color.white);
			g.fillPolygon(x, y, 3);
			g.translate(-2, -2);
		}

		if (pressed) {
			g.translate(1, 1);
		}

		g.setColor(Color.darkGray);
		g.fillPolygon(x, y, 3);
		g.setColor(Color.black);
		g.drawPolygon(x, y, 3);

		if (pressed) {
			g.translate(-1, -1);
		}
	}

	public static interface ArrowsListener {
		public void arrowPressed(MouseEvent e, int array);

		public void arrowReleased(MouseEvent e, int array);
	}
}
