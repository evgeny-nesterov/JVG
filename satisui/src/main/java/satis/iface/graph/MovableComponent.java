package satis.iface.graph;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

public abstract class MovableComponent extends JPanel {
	private static final long serialVersionUID = 1L;

	public MovableComponent() {
		setOpaque(false);
		enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
	}

	private int x, y;

	private boolean isMove = false;

	@Override
	public void processMouseEvent(MouseEvent e) {
		if (e.getID() == MouseEvent.MOUSE_PRESSED) {
			Container parent = getParent();
			isMove = isMovableAreaPoint(e.getX(), e.getY()) && parent.getLayout() == null;
			x = e.getX();
			y = e.getY();
		}

		super.processMouseEvent(e);
	}

	@Override
	public void processMouseMotionEvent(MouseEvent e) {
		if (e.getID() == MouseEvent.MOUSE_DRAGGED && isMove) {
			int dx = e.getX() - x;
			int dy = e.getY() - y;
			int X = getX() + dx;
			int Y = getY() + dy;

			if (X < 0) {
				X = 0;
			}

			if (Y < 0) {
				Y = 0;
			}

			Container parent = getParent();
			if (X > parent.getWidth() - getWidth()) {
				X = parent.getWidth() - getWidth();
			}

			if (Y > parent.getHeight() - getHeight()) {
				Y = parent.getHeight() - getHeight();
			}

			setLocation(X, Y);
		}

		super.processMouseMotionEvent(e);
	}

	public abstract boolean isMovableAreaPoint(int x, int y);

	public static void main(String[] arg) {
		JFrame f = new JFrame();

		MovableComponent c = new MovableComponent() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isMovableAreaPoint(int x, int y) {
				return x < getWidth() / 2 && y < getHeight() / 2;
			}
		};
		c.setBounds(10, 10, 40, 40);
		c.setOpaque(true);
		c.setBackground(Color.white);

		f.getContentPane().setLayout(null);
		f.getContentPane().add(c);

		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setLocation(200, 200);
		f.setSize(400, 400);
		f.setVisible(true);
		f.toFront();
	}
}
