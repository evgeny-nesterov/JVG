package ru.nest.swing.toolbar;

import ru.nest.swing.ToolBarBorder;
import ru.nest.swing.ToolbarLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class WToolBar extends JComponent {
	private int mx;

	private int my;

	private boolean pressed = false;

	private int minShift = 7;

	private WDragWindow dragWindow = null;

	private Robot robot;

	private String title;

	private Container parent = null;

	private int index = -1;

	public WToolBar(String title) {
		this.title = title;

		setBorder(new ToolBarBorder());
		setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
					mx = e.getX();
					my = e.getY();
					pressed = true;

					if (dragWindow != null) {
						dragWindow.hideHeader();
					}
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					pressed = false;
					if (dragWindow != null) {
						dragWindow.showHeader();
					}
				}
			}
		});

		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if (pressed) {
					int dx = e.getX() - mx;
					int dy = e.getY() - my;
					if (dragWindow != null || Math.abs(dx) >= minShift || Math.abs(dy) >= minShift) {
						replace(e.getX(), e.getY(), mx, my);
					}
				}
			}
		});

		try {
			robot = new Robot();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		GradientPaint paintGradiaent = new GradientPaint(0, 0, new Color(250, 250, 250), 0, getHeight(), new Color(220, 220, 220));

		Graphics2D g2d = (Graphics2D) g;
		Paint oldPaint = g2d.getPaint();
		g2d.setPaint(paintGradiaent);
		g2d.fillRect(0, 0, getWidth(), getHeight());
		g2d.setPaint(oldPaint);

		super.paintComponent(g);
	}

	private void replace(int cx, int cy, int mx, int my) {
		Point screenLocation = getLocationOnScreen();
		int x = cx + screenLocation.x;
		int y = cy + screenLocation.y;

		if (dragWindow == null) {
			Container parent = getParent();
			if (isContains(parent, x, y)) {
				if (insert(parent, cx, cy, mx, my)) {
					return;
				}
			}

			showWindow(x - mx, y - my, mx, my);
		} else {
			Container parent = getToolbarParent(x, y);
			if (parent != null) {
				Point parentScreenLocation = parent.getLocationOnScreen();
				if (insert(parent, dragWindow.getX() - parentScreenLocation.x, dragWindow.getY() - parentScreenLocation.y, mx, my)) {
					return;
				}
			}

			dragWindow.setLocation(dragWindow.getX() + cx - mx, dragWindow.getY() + cy - my);
		}
	}

	private Container getToolbarParent(int x, int y) {
		Component c = getDeepestComponentAt(x, y);
		Container parent = c instanceof Container || c == null ? (Container) c : c.getParent();
		while (parent != null) {
			if (parent.getLayout() instanceof ToolbarLayout) {
				break;
			}
			parent = parent.getParent();
		}
		return parent;
	}

	private boolean insert(Container parent, int x, int y, int mx, int my) {
		int index = -1;

		Component c = parent.getComponentAt(x, y);
		if (c != null) {
			if (x >= c.getX() + c.getWidth() - 5) {
				index = parent.getComponentZOrder(c) + 1;
			} else if (x <= c.getX() + 5) {
				index = parent.getComponentZOrder(c);
			}
		} else {
			int minDistance = Integer.MAX_VALUE;
			for (int i = 0; i < parent.getComponentCount(); i++) {
				c = parent.getComponent(i);
				if (y >= c.getY() && y < c.getY() + c.getHeight()) {
					int dist = Integer.MAX_VALUE;
					if (x < c.getX()) {
						dist = c.getX() - x;
					} else if (x > c.getX() + c.getWidth()) {
						dist = x - c.getX() - c.getWidth();
					}

					if (dist < minDistance) {
						minDistance = dist;
						index = i;
					}
				}
			}
		}

		if (parent.getComponentCount() == 0) {
			index = 0;
		}

		if (getParent() == parent && parent.getComponentZOrder(this) == index) {
			return true;
		}

		if (index != -1) {
			if (getParent() != parent) {
				setParent(parent, index);
				press(mx, my);
				return true;
			}
		}
		return false;
	}

	private void press(int mx, int my) {
		Point screenLocation = getLocationOnScreen();
		robot.mouseRelease(InputEvent.BUTTON1_MASK);
		robot.mouseMove(screenLocation.x + mx, screenLocation.y + my);
		robot.mousePress(InputEvent.BUTTON1_MASK);
	}

	private boolean isContains(Component c, int screenX, int screenY) {
		Point screenLocation = c.getLocationOnScreen();
		Dimension parentSize = c.getSize();
		int px1 = screenLocation.x;
		int py1 = screenLocation.y;
		int px2 = px1 + parentSize.width;
		int py2 = py1 + parentSize.height;
		return screenX >= px1 && screenX <= px2 && screenY >= py1 && screenY <= py2;
	}

	public Component getDeepestComponentAt(int x, int y) {
		for (Window w : Window.getWindows()) {
			Component c = getDeepestComponentAt(w, x - w.getX(), y - w.getY());
			if (c != null) {
				return c;
			}
		}
		return null;
	}

	public Component getDeepestComponentAt(Component parent, int x, int y) {
		if (!parent.contains(x, y)) {
			return null;
		}

		if (parent instanceof Container) {
			Component components[] = ((Container) parent).getComponents();
			for (int i = 0; i < components.length; i++) {
				Component comp = components[i];
				if (comp != null && comp != dragWindow && comp.isVisible()) {
					Point loc = comp.getLocation();
					if (comp instanceof Container) {
						comp = getDeepestComponentAt(comp, x - loc.x, y - loc.y);
					} else {
						comp = comp.getComponentAt(x - loc.x, y - loc.y);
					}

					if (comp != null && comp.isVisible()) {
						return comp;
					}
				}
			}
		}
		return parent;
	}

	private void showWindow(int x, int y, int mx, int my) {
		Container parent = getParent();
		int index = parent != null ? parent.getComponentZOrder(this) : -1;

		if (parent != null) {
			parent.remove(this);
			parent.validate();
			parent.repaint();
		}

		this.parent = parent;
		this.index = index;

		dragWindow = new WDragWindow(this, x, y);
		dragWindow.pack();
		dragWindow.setVisible(true);

		press(mx, my);
	}

	public void setParent(Container parent, int index) {
		boolean isWindow = dragWindow != null;
		if (isWindow) {
			dragWindow.setVisible(false);
			dragWindow.remove(this);
			dragWindow.dispose();
			dragWindow = null;
		}

		parent.add(this, index);
		parent.validate();
		parent.repaint();
	}

	@Override
	public void setVisible(boolean isVisible) {
		super.setVisible(isVisible);
		if (dragWindow != null) {
			dragWindow.setVisible(isVisible);
		}
	}

	public String getTitle() {
		return title;
	}

	public int getLastIndex() {
		return index;
	}

	public Container getLastParent() {
		return parent;
	}

	public static void main(String[] args) {
		WToolBar w1 = new WToolBar("test 1");
		w1.add(new JLabel("toolbar 1"));

		WToolBar w2 = new WToolBar("test 2");
		w2.add(new JTextField("toolbar 2"));

		WToolBar w3 = new WToolBar("test 3");
		w3.add(new JLabel("toolbar 3"));

		JPanel pnl1 = new JPanel();
		pnl1.setBackground(Color.red);
		pnl1.setLayout(new ToolbarLayout());
		pnl1.add(w1);
		pnl1.add(w2);
		pnl1.add(w3);

		WToolBar w4 = new WToolBar("test 4");
		w4.add(new JLabel("toolbar 4"));

		WToolBar w5 = new WToolBar("test 5");
		w5.add(new JLabel("toolbar 5"));

		WToolBar w6 = new WToolBar("test 6");
		w6.add(new JLabel("toolbar 6"));

		JPanel pnl2 = new JPanel();
		pnl2.setBackground(Color.green);
		pnl2.setLayout(new ToolbarLayout());
		pnl2.add(w4);
		pnl2.add(w5);
		pnl2.add(w6);

		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setBounds(300, 100, 600, 400);
		f.getContentPane().add(pnl1, BorderLayout.CENTER);
		f.getContentPane().add(pnl2, BorderLayout.SOUTH);
		f.setVisible(true);
	}
}
