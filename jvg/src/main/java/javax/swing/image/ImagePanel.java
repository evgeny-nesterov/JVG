package javax.swing.image;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;

import javax.swing.Icon;
import javax.swing.JLabel;

public class ImagePanel extends JLabel {
	private boolean mousePressed = false;

	private int mx, my;

	public ImagePanel() {
		setOpaque(true);
		setBackground(Color.darkGray);
		setRequestFocusEnabled(true);
		setFocusable(true);

		addComponentListener(new ComponentAdapter() {
			boolean first = true;

			@Override
			public void componentShown(ComponentEvent e) {
				centrate();
			}

			@Override
			public void componentResized(ComponentEvent e) {
				centrate();
			}
		});

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				requestFocus();

				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
					mousePressed = true;
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					mx = e.getX();
					my = e.getY();
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				setCursor(Cursor.getDefaultCursor());
				mousePressed = false;
			}
		});

		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if (mousePressed) {
					translate(e.getX() - mx, e.getY() - my);
					mx = e.getX();
					my = e.getY();
				}
			}
		});

		addKeyListener(new KeyAdapter() {
			private Mover mover;

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_0) {
					scale(1.05);
					repaint();
				} else if (e.getKeyCode() == KeyEvent.VK_MINUS) {
					scale(1 / 1.05);
					repaint();
				} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
					move(Mover.LEFT);
				} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
					move(Mover.RIGHT);
				} else if (e.getKeyCode() == KeyEvent.VK_UP) {
					move(Mover.UP);
				} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
					move(Mover.DOWN);
				}
			}

			private synchronized void move(int type) {
				if (mover == null || !mover.isAlive()) {
					mover = new Mover(type);
					mover.start();
				} else {
					mover.add(type);
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_LEFT) {
					mover.remove(Mover.LEFT);
				} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
					mover.remove(Mover.RIGHT);
				} else if (e.getKeyCode() == KeyEvent.VK_UP) {
					mover.remove(Mover.UP);
				} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
					mover.remove(Mover.DOWN);
				}
			}
		});
	}

	private Icon icon;

	public void setImage(Icon icon) {
		this.icon = icon;
		if (icon != null) {
			if (icon.getIconWidth() <= 0 || icon.getIconHeight() <= 0) {
				icon = null;
			}
		}
		centrate();
	}

	public void centrate() {
		if (icon != null) {
			double scale = getMinScale();

			int iw = icon.getIconWidth();
			int ih = icon.getIconHeight();

			transform.setToIdentity();
			transform.translate(getWidth() / 2.0, getHeight() / 2.0);
			transform.scale(scale, scale);
			transform.translate(-iw / 2.0, -ih / 2.0);
		}
		repaint();
	}

	private double getMinScale() {
		int iw = icon.getIconWidth();
		int ih = icon.getIconHeight();
		double scalex = getWidth() / (double) iw;
		double scaley = getHeight() / (double) ih;
		return Math.min(scalex, scaley);
	}

	private AffineTransform transform = new AffineTransform();

	public void setActualSize() {
		transform.setToIdentity();
		check();
		repaint();
	}

	public void translate(double dx, double dy) {
		transform.preConcatenate(AffineTransform.getTranslateInstance(dx, dy));
		check();
		repaint();
	}

	public void scale(double scale) {
		double minScale = getMinScale();
		double totalScale = scale * transform.getScaleX();
		if (totalScale < minScale) {
			scale = minScale / transform.getScaleX();
		}

		transform.preConcatenate(AffineTransform.getTranslateInstance(-getWidth() / 2.0, -getHeight() / 2.0));
		transform.preConcatenate(AffineTransform.getScaleInstance(scale, scale));
		transform.preConcatenate(AffineTransform.getTranslateInstance(getWidth() / 2.0, getHeight() / 2.0));
		check();
		repaint();
	}

	private void check() {
		int iw = icon.getIconWidth();
		int ih = icon.getIconHeight();
		double[] points = { 0, 0, iw, ih };
		transform.transform(points, 0, points, 0, 2);

		double x1_, x2_, y1_, y2_;
		double x1 = x1_ = points[0];
		double y1 = y1_ = points[1];
		double x2 = x2_ = points[2];
		double y2 = y2_ = points[3];
		double w = x2 - x1;
		double h = y2 - y1;

		if (w < getWidth()) {
			x1_ = (getWidth() - w) / 2.0;
			x2_ = (getWidth() + w) / 2.0;
		} else {
			if (x1_ > 0) {
				x1_ = 0;
				x2_ = w;
			} else if (x2_ < getWidth()) {
				x2_ = getWidth();
				x1_ = x2_ - w;
			}
		}

		if (h < getHeight()) {
			y1_ = (getHeight() - h) / 2.0;
			y2_ = (getHeight() + h) / 2.0;
		} else {
			if (y1_ > 0) {
				y1_ = 0;
				y2_ = h;
			} else if (y2_ < getHeight()) {
				y2_ = getHeight();
				y1_ = y2_ - h;
			}
		}

		if (x1 != x1_ || x2 != x2_ || y1 != y1_ || y2 != y2_) {
			double dx = x1_ - x1;
			double dy = y1_ - y1;
			transform.preConcatenate(AffineTransform.getTranslateInstance(dx, dy));
		}
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		if (icon != null) {
			int iw = icon.getIconWidth();
			int ih = icon.getIconHeight();

			Graphics2D g2d = (Graphics2D) g;
			g2d.setTransform(transform);
			icon.paintIcon(this, g2d, 0, 0);
		}
	}

	class Mover extends Thread {
		public final static int UP = 1;

		public final static int DOWN = 2;

		public final static int LEFT = 4;

		public final static int RIGHT = 8;

		public final static int DELTA = 10;

		public Mover(int type) {
			this.type = type;
		}

		private int type;

		public void add(int type) {
			this.type |= type;
		}

		public void remove(int type) {
			this.type &= ~type;
		}

		@Override
		public void run() {
			while (!interrupted() && type != 0) {
				double dx = 0;
				double dy = 0;

				if ((type & UP) != 0) {
					dy += DELTA;
				}

				if ((type & DOWN) != 0) {
					dy -= DELTA;
				}

				if ((type & LEFT) != 0) {
					dx += DELTA;
				}

				if ((type & RIGHT) != 0) {
					dx -= DELTA;
				}

				double r = Math.sqrt(dx * dx + dy * dy);
				if (r != 0) {
					double koef = DELTA / r;
					dx *= koef;
					dy *= koef;
				}

				translate(dx, dy);

				try {
					sleep(20);
				} catch (InterruptedException exc) {
				}
			}
		}
	}
}
