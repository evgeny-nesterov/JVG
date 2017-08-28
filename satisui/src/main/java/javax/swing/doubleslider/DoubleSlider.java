package javax.swing.doubleslider;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class DoubleSlider extends JPanel {
	private static final long serialVersionUID = 1L;

	private int dx = 3;

	private int x;

	public DoubleSlider() {
		setOpaque(false);
		enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
	}

	private double startValue = 0;

	private double endValue = 10;

	public void setLimits(double startValue, double endValue) {
		this.startValue = startValue;
		this.endValue = endValue;
	}

	public double getStartValue() {
		return startValue;
	}

	public double getEndValue() {
		return endValue;
	}

	private int majorTickSpacing = 2;

	public void setMajorTickSpacing(int majorTickSpacing) {
		this.majorTickSpacing = majorTickSpacing;
	}

	public int getMajorTickSpacing() {
		return majorTickSpacing;
	}

	private int minorTickSpacing = 1;

	public void setMinorTickSpacing(int minorTickSpacing) {
		this.minorTickSpacing = minorTickSpacing;
	}

	public int getMinorTickSpacing() {
		return minorTickSpacing;
	}

	private double x1 = 0, x2 = 10;

	public void setSliderPos(double x1, double x2) {
		double nx1 = Math.min(x1, x2);
		double nx2 = Math.max(x1, x2);
		if (this.x1 != nx1 || this.x2 != nx2) {
			this.x1 = x1;
			this.x2 = x2;
			fireSliderChanged(this.x1, this.x2);
			repaint();
		}
	}

	public double getX1() {
		return x1;
	}

	public double getX2() {
		return x2;
	}

	private boolean isDiscret = false;

	public void setSnapToTicks(boolean isDiscret) {
		this.isDiscret = isDiscret;
	}

	public boolean isDiscret() {
		return isDiscret;
	}

	private int ticCount = 11;

	public void setTicCount(int ticCount) {
		if (ticCount < 2) {
			ticCount = 2;
		}
		this.ticCount = ticCount;
	}

	public int getTicCount() {
		return ticCount;
	}

	private String[] values = null;

	public void setValues(String[] values) {
		this.values = values;
	}

	@Override
	public void processMouseEvent(MouseEvent e) {
		type = NONE;
		if (e.getButton() == MouseEvent.BUTTON1 && isEnabled()) {
			if (e.getID() == MouseEvent.MOUSE_PRESSED) {
				x = e.getX();
				double w = getInnerWidth();

				int X1 = dx + (int) (w * (x1 - startValue) / (endValue - startValue));
				int X2 = dx + (int) (w * (x2 - startValue) / (endValue - startValue));

				if (x <= X1 + dx && x >= X1 - dx) {
					type = LEFT;
				} else if (x <= X2 + dx && x >= X2 - dx) {
					type = RIGHT;
				} else if (x > X1 + dx && x < X2 - dx) {
					type = CENTER;
				}
				repaint();
			} else if (e.getID() == MouseEvent.MOUSE_RELEASED) {
				repaint();
			}
		}

		super.processMouseEvent(e);
	}

	private final static int LEFT = 0;

	private final static int CENTER = 1;

	private final static int RIGHT = 2;

	private final static int NONE = 3;

	private int type = NONE;

	@Override
	public void processMouseMotionEvent(MouseEvent e) {
		if (e.getID() == MouseEvent.MOUSE_DRAGGED && isEnabled()) {
			double w = getInnerWidth();
			double newX1 = x1, newX2 = x2;
			int mx = e.getX();

			switch (type) {
				case LEFT:
					if (isDiscret) {
						double d = w / (ticCount - 1);
						double sx = mx - (mx - dx) % d + d / 2.0;
						double MX = (mx < sx) ? (sx - d / 2.0) : (sx + d / 2.0);
						mx = (int) MX;
						newX1 = startValue + (endValue - startValue) * (MX - dx) / w;
					} else {
						newX1 = startValue + (endValue - startValue) * (mx - dx) / w;
					}

					newX2 = x2;
					if (newX1 < startValue) {
						newX1 = startValue;
					} else if (newX1 > endValue) {
						newX1 = endValue;
					}

					if (newX1 > newX2) {
						type = RIGHT;
					}
					break;

				case RIGHT:
					if (isDiscret) {
						double d = w / (ticCount - 1);
						double sx = mx - (mx - dx) % d + d / 2.0;
						double MX = (mx < sx) ? (sx - d / 2.0) : (sx + d / 2.0);
						mx = (int) MX;
						newX2 = startValue + (endValue - startValue) * (MX - dx) / w;
					} else {
						newX2 = startValue + (endValue - startValue) * (mx - dx) / w;
					}

					newX1 = x1;
					if (newX2 < startValue) {
						newX2 = startValue;
					} else if (newX2 > endValue) {
						newX2 = endValue;
					}

					if (newX1 > newX2) {
						type = LEFT;
					}
					break;

				case CENTER:
					double dX = (endValue - startValue) * (mx - x) / w;
					if (isDiscret) {
						double d = (endValue - startValue) / (ticCount - 1);
						if (Math.abs(dX) > d / 2.0) {
							dX = Math.signum(dX) * d;
						} else {
							dX = 0;
						}
						x += (int) (w / (ticCount - 1) * Math.signum(dX));
					} else {
						x = mx;
					}
					newX1 = x1 + dX;
					newX2 = x2 + dX;
					if (newX1 < startValue) {
						newX2 = startValue + (newX2 - newX1);
						newX1 = startValue;
					} else if (newX2 > endValue) {
						newX1 = endValue - (newX2 - newX1);
						newX2 = endValue;
					}
					break;
			}

			setSliderPos(newX1, newX2);
		}

		super.processMouseMotionEvent(e);
	}

	public int getInnerWidth() {
		return getWidth() - dx * 2 - 1;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		Color black = (isEnabled() ? Color.black : Color.lightGray);
		Color grey = (isEnabled() ? Color.gray : Color.lightGray);
		Color lightGray = (isEnabled() ? Color.lightGray : Color.lightGray);
		Color darkGray = (isEnabled() ? Color.darkGray : Color.lightGray);
		Color white = (isEnabled() ? Color.white : Color.lightGray);
		int bottomH = 0;
		if (values != null) {
			bottomH = g.getFontMetrics().getHeight() + 2;
		}
		double w = getInnerWidth();
		int h = getHeight() - bottomH;
		int lineH = 4;
		int centerY = (h - lineH) / 2;

		if (majorTickSpacing > 0) {
			g.setColor(grey);
			double d = w / (ticCount - 1);
			for (int i = 0; i <= ticCount; i++) {
				if (i % majorTickSpacing == 0) {
					int x = (int) (dx + i * d);
					g.drawLine(x, h / 2, x, h);
				}
			}
		}

		if (minorTickSpacing > 0) {
			g.setColor(grey);
			double d = w / (ticCount - 1);
			for (int i = 0; i <= ticCount; i++) {
				if (i % minorTickSpacing == 0) {
					int x = (int) (dx + i * d);
					g.drawLine(x, h / 2, x, centerY + lineH + 3);
				}
			}
		}

		int X1 = dx + (int) (w * (x1 - startValue) / (endValue - startValue));
		int X2 = dx + (int) (w * (x2 - startValue) / (endValue - startValue));

		// --- draw rect ---
		g.setColor(lightGray);
		g.fillRect(dx, centerY, (int) w + 1, lineH);
		g.setColor(darkGray);
		g.drawLine(X1, centerY - 1, X2, centerY - 1);
		g.setColor(white);
		g.drawLine(X1, centerY + lineH, X2, centerY + lineH);

		// --- draw text ---
		if (values != null) {
			double d = w / (ticCount - 1);
			g.setColor(black);
			for (int i = 0; i < Math.min(ticCount, values.length); i++) {
				if (values[i] != null && values[i].length() > 0) {
					g.drawString(values[i], (int) (dx + i * d) - g.getFontMetrics().stringWidth(values[i]) / 2, getHeight() - 1);
				}
			}
		}

		// --- draw sliders ---
		g.setColor(lightGray);
		int y = h - lineH / 2 - 1;
		g.fillRect(X1 - dx, 0, 2 * dx, y);
		g.fillRect(X2 - dx, 0, 2 * dx, y);
		paintBorder(g, X1 - dx, 0, X1 + dx, y, true, white, grey);
		paintBorder(g, X2 - dx, 0, X2 + dx, y, true, white, grey);
		paintLines(g, X1 - dx, 0, X1 + dx, y, type != LEFT, white, darkGray);
		paintLines(g, X2 - dx, 0, X2 + dx, y, type != RIGHT, white, darkGray);
	}

	public void paintBorder(Graphics g, int x1, int y1, int x2, int y2, boolean isRised, Color white, Color gray) {
		g.setColor(isRised ? white : gray);
		g.drawLine(x1, y1, x1, y2);
		g.drawLine(x1, y1, x2, y1);

		g.setColor(isRised ? gray : white);
		g.drawLine(x1, y2, x2, y2);
		g.drawLine(x2, y1, x2, y2);
	}

	public void paintLines(Graphics g, int x1, int y1, int x2, int y2, boolean isRised, Color white, Color gray) {
		int y = (y1 + y2) / 2;
		g.setColor(isRised ? white : gray);
		g.drawLine(x1 + 2, y, x2 - 2, y);
		g.drawLine(x1 + 2, y - 3, x2 - 2, y - 3);
		g.drawLine(x1 + 2, y + 3, x2 - 2, y + 3);

		g.setColor(isRised ? gray : white);
		g.drawLine(x1 + 2, y + 1, x2 - 2, y + 1);
		g.drawLine(x1 + 2, y - 2, x2 - 2, y - 2);
		g.drawLine(x1 + 2, y + 4, x2 - 2, y + 4);
	}

	private ArrayList<SliderListener> listeners = new ArrayList<SliderListener>();

	public void addSliderListener(SliderListener listener) {
		if (listener != null) {
			synchronized (listeners) {
				listeners.add(listener);
			}
		}
	}

	public void removeSliderListener(SliderListener listener) {
		if (listener != null) {
			synchronized (listeners) {
				listeners.remove(listener);
			}
		}
	}

	public void fireSliderChanged(double x1, double x2) {
		synchronized (listeners) {
			for (SliderListener listener : listeners) {
				listener.sliderChanged(x1, x2);
			}
		}
	}

	public static void main(String[] args) {
		DoubleSlider slider = new DoubleSlider();
		slider.setPreferredSize(new Dimension(200, 20));
		// slider.addSliderListener(new SliderListener()
		// {
		// public void sliderChanged(double x1, double x2)
		// {
		// log.debug("x1 = " + x1 + ", x2 = " + x2);
		// }
		// });

		JFrame f = new JFrame();
		f.setBounds(200, 400, 400, 100);
		f.getContentPane().setLayout(new FlowLayout());
		f.getContentPane().add(slider);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}
}
