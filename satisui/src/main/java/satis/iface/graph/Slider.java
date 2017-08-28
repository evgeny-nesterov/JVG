package satis.iface.graph;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;

public class Slider extends JComponent {
	private static final long serialVersionUID = 1L;

	public Slider(Group group) {
		this();
		init(group);
	}

	private GroupListener listener;

	public void init(Group group) {
		if (this.group != null && listener != null) {
			this.group.removeGroupListener(listener);
		}

		if (group != null) {
			listener = new GroupAdapter() {
				@Override
				public void boundsChanged(Group group) {
					repaint();
				}

				@Override
				public void innerBoundsChanged(Group group) {
					repaint();
				}
			};
			group.addGroupListener(listener);
		}
		this.group = group;
	}

	public Slider() {
		enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.COMPONENT_EVENT_MASK);
		setPreferredSize(new Dimension(100, H));
		setOpaque(false);
	}

	private Group group;

	public Group getGroup() {
		return group;
	}

	private int H = 10;

	public int getSliderHeight() {
		return H;
	}

	@Override
	public void processComponentEvent(ComponentEvent e) {
		super.processComponentEvent(e);

		if (e.getID() == ComponentEvent.COMPONENT_RESIZED) {
			repaint();
		}
	}

	private Color fillColor = new Color(240, 240, 240);

	private Color borderColor = new Color(220, 220, 220);

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		g.setColor(fillColor);
		g.fillRect(0, 0, getWidth(), getHeight());

		g.setColor(borderColor);
		g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

		drawSlider(g);
	}

	private Color sliderFillColor = new Color(190, 190, 190);

	private Color sliderBorderColor = new Color(150, 150, 150);

	public void drawSlider(Graphics g) {
		if (group != null) {
			int width = getWidth() - 2;
			Bounds b = group.getBounds();
			Bounds ib = group.getInnerBounds();
			double min = b.x;
			double max = min + b.w;
			double f1 = ib.x, f2 = f1 + ib.w;

			if (f1 > min || f2 < max) {
				double koef = width / (max - min);
				int x1 = (int) (koef * (f1 - min)) + 1;
				int x2 = (int) (koef * (f2 - min)) + 1;
				int w = x2 - x1;
				int y = H / 2 - 4, h = 7;

				g.setColor(sliderFillColor);
				g.fillRect(x1, y, w, h);

				g.setColor(sliderBorderColor);
				g.drawRect(x1, y, w, h);

				Rectangle oldClip = g.getClipBounds();

				g.setClip(x1, y, w, h);
				int cy = H / 2 - 1;
				int cx = (x2 + x1) / 2;

				g.setColor(fillColor);
				g.drawLine(cx - 2, cy, cx + 2, cy);
				g.setColor(sliderBorderColor);
				g.drawLine(cx - 2, cy + 1, cx + 2, cy + 1);

				g.setClip(oldClip);
			}
		}
	}

	public boolean isOnSlider(int pos) {
		Bounds b = group.getBounds();
		Bounds ib = group.getInnerBounds();

		double min = b.x;
		double max = min + b.w;
		double f1 = ib.x, f2 = f1 + ib.w;
		if (f1 > min || f2 < max) {
			int w = getWidth() - 2;
			double koef = w / (max - min);

			int x1 = (int) (koef * (f1 - min)) + 1;
			int x2 = (int) (koef * (f2 - min)) + 1;

			return pos >= x1 && pos <= x2;
		} else {
			return false;
		}
	}

	private int dx;

	private boolean sliderPressed = false;

	@Override
	protected void processMouseEvent(MouseEvent e) {
		super.processMouseEvent(e);

		if (group != null) {
			switch (e.getID()) {
				case MouseEvent.MOUSE_PRESSED:
					int w = getWidth() - 2;
					Bounds b = group.getBounds();
					Bounds ib = group.getInnerBounds();
					double min = b.x;
					double max = min + b.w;
					double f1 = ib.x, f2 = f1 + ib.w;
					double koef = w / (max - min);
					int x1 = (int) (koef * (f1 - min)) + 1;
					int x2 = (int) (koef * (f2 - min)) + 1;

					if (e.getX() >= x1 && e.getX() <= x2) {
						dx = (x1 + x2) / 2 - e.getX();
						sliderPressed = true;
						setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					} else {
						sliderPressed = false;
						updatePosition(e.getX());
					}
					break;

				case MouseEvent.MOUSE_RELEASED:
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					break;
			}
		}
	}

	@Override
	protected void processMouseMotionEvent(MouseEvent e) {
		super.processMouseMotionEvent(e);

		if (group != null) {
			if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
				if (sliderPressed) {
					updatePosition(e.getX() + dx);
				}
			}
		}
	}

	private void updatePosition(int x) {
		if (group != null) {
			Bounds b = group.getBounds();
			Bounds ib = group.getInnerBounds();
			double min = b.x;
			double max = min + b.w;
			double x1 = ib.x, x2 = x1 + ib.w;
			double w = getWidth() - 2;

			double cw = (x2 - x1) / 2.0;
			double cf = min + (max - min) * x / w;
			ib.x = cf - cw;
			if (ib.x < b.x) {
				ib.x = b.x;
			} else if (ib.x + ib.w > b.x + b.w) {
				ib.x = b.x + b.w - ib.w;
			}
			group.fireInnerBoundsChanged();
		}
	}

	public double getX(int pos) {
		double w = getWidth() - 2;
		Bounds b = group.getBounds();
		return b.x + b.w * pos / w;
	}
}
