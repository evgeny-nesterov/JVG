package javax.swing.text;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.VerticalLabelUI;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTextPaneUI;

public class OrientTextPane extends JTextPane {
	private static final long serialVersionUID = 1L;

	public final static int HORIZONTAL = 0;

	public final static int VERTICAL = 1;

	private VerticalTextPaneUI ui = new VerticalTextPaneUI();

	public OrientTextPane() {
		setUI(ui);
		enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);

		setOrientation(HORIZONTAL);
		setClockwise(false);
	}

	private int orientation = HORIZONTAL;

	public void setOrientation(int orientation) {
		this.orientation = orientation;
		ui.orientation = orientation;

		if (orientation == VERTICAL) {
			setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
		} else {
			setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		}
	}

	public int getOrientation() {
		return orientation;
	}

	public void setClockwise(boolean clockwise) {
		ui.clockwise = clockwise;
	}

	public boolean getClockwise() {
		return ui.clockwise;
	}

	@Override
	public void processKeyEvent(KeyEvent e) {
		if (orientation == VERTICAL) {
			if (e.getKeyCode() == KeyEvent.VK_DOWN) {
				e.setKeyCode(KeyEvent.VK_LEFT);
			} else if (e.getKeyCode() == KeyEvent.VK_UP) {
				e.setKeyCode(KeyEvent.VK_RIGHT);
			} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
				e.setKeyCode(KeyEvent.VK_UP);
			} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
				e.setKeyCode(KeyEvent.VK_DOWN);
			}
		}
		super.processKeyEvent(e);
	}

	@Override
	public void processMouseEvent(MouseEvent e) {
		if (orientation == VERTICAL) {
			int x = e.getX();
			int y = e.getY();
			e.translatePoint(getHeight() - y - e.getX(), x - e.getY());
		}
		super.processMouseEvent(e);
	}

	@Override
	public void processMouseMotionEvent(MouseEvent e) {
		if (orientation == VERTICAL) {
			int x = e.getX();
			int y = e.getY();
			e.translatePoint(getHeight() - y - e.getX(), x - e.getY());
		}
		super.processMouseMotionEvent(e);
	}

	@Override
	public void repaint(long tm, int x, int y, int w, int h) {
		// super.repaint(tm, y, x, getWidth() - h, getHeight() - w);
		if (orientation == VERTICAL) {
			super.repaint(tm, 0, 0, getWidth(), getHeight());
		} else {
			super.repaint(tm, x, y, w, h);
		}
	}

	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setBounds(300, 300, 300, 300);
		f.getContentPane().setLayout(new BorderLayout());

		OrientTextPane t1 = new OrientTextPane();
		t1.setOrientation(OrientTextPane.VERTICAL);
		t1.setText("12345");
		f.getContentPane().add(t1, BorderLayout.WEST);

		JTextPane t2 = new JTextPane();
		t2.setText("12345");
		f.getContentPane().add(t2, BorderLayout.CENTER);

		f.setVisible(true);
	}
}

class VerticalTextPaneUI extends BasicTextPaneUI {
	public static ComponentUI createUI(JComponent c) {
		return new VerticalLabelUI(false);
	}

	protected int orientation = OrientTextPane.HORIZONTAL;

	protected boolean clockwise = false;

	public VerticalTextPaneUI() {
		super();
	}

	@Override
	public Dimension getPreferredSize(JComponent c) {
		if (orientation == OrientTextPane.VERTICAL) {
			JTextPane editor = (JTextPane) getComponent();
			Document doc = editor.getDocument();
			View rootView = getRootView(editor);

			Insets i = c.getInsets();
			Dimension d = c.getSize();
			if (doc instanceof AbstractDocument) {
				((AbstractDocument) doc).readLock();
			}

			try {
				if ((d.width > (i.left + i.right)) && (d.height > (i.top + i.bottom))) {
					rootView.setSize(d.height - i.top - i.bottom, d.width - i.left - i.right);
				} else if (d.width == 0 && d.height == 0) {
					rootView.setSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
				}

				d.height = (int) Math.min((long) rootView.getPreferredSpan(View.X_AXIS) + i.left + i.right + 1, Integer.MAX_VALUE);
				d.width = (int) Math.min((long) rootView.getPreferredSpan(View.Y_AXIS) + i.top + i.bottom, Integer.MAX_VALUE);
			} finally {
				if (doc instanceof AbstractDocument) {
					((AbstractDocument) doc).readUnlock();
				}
			}

			return d;
		} else {
			return super.getPreferredSize(c);
		}
	}

	@Override
	protected Rectangle getVisibleEditorRect() {
		Rectangle r = super.getVisibleEditorRect();

		if (orientation == OrientTextPane.VERTICAL && r != null) {
			int x = r.x;
			int y = r.y;
			int w = r.width;
			int h = r.height;
			r.setBounds(y, x, h, w);
		}

		return r;
	}

	@Override
	protected void paintBackground(Graphics g) {
		if (orientation == OrientTextPane.VERTICAL) {
			Component editor = getComponent();
			g.setColor(editor.getBackground());
			g.fillRect(0, 0, editor.getHeight(), editor.getWidth());
		} else {
			super.paintBackground(g);
		}
	}

	@Override
	protected void paintSafely(Graphics g) {
		if (orientation == OrientTextPane.VERTICAL) {
			Graphics2D g2d = (Graphics2D) g;

			if (clockwise) {
				g2d.rotate(Math.PI / 2);
				g2d.translate(0, -getComponent().getWidth());
			} else {
				g2d.rotate(-Math.PI / 2);
				g2d.translate(-getComponent().getHeight(), 0);
			}
		}

		super.paintSafely(g);
	}
}
