package javax.swing.dock;

import java.applet.Applet;
import java.awt.AWTException;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JFrame;

public class DockFlowSupport {
	protected static Robot robot;
	static {
		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	private boolean isMovable = true;

	private DockFlow flow;

	private Point dragPoint;

	private boolean pressed;

	public DockFlowSupport(Component draggedComponent, Component dropComponent, Component undockComponent, String name, Icon icon) {
		this(new DockFlow(draggedComponent, dropComponent, undockComponent, name, icon));
	}

	private DockFlowSupport(final DockFlow flow) {
		this.flow = flow;

		flow.getDraggedComponent().addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
					dragPoint = e.getLocationOnScreen();
					pressed = true;
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				pressed = false;
			}
		});
		flow.getDraggedComponent().addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if (pressed) {
					Point p = e.getLocationOnScreen();
					double dist = p.distance(dragPoint);
					if (dist > 15) {
						startFloating(dragPoint);
					}
				}
			}
		});
	}

	private Container getTopLevelAncestor(Component c) {
		for (Container p = c.getParent(); p != null; p = p.getParent()) {
			if (p instanceof Window || p instanceof Applet) {
				return p;
			}
		}
		return null;
	}

	private void startFloating(Point screenPoint) {
		if (!isMovable) {
			return;
		}

		Container top = getTopLevelAncestor(flow.getDropComponent());
		if (top instanceof JFrame) {
			JFrame f = (JFrame) top;
			try {
				robot.mouseRelease(InputEvent.BUTTON1_MASK);

				DockGlassPanel glass = new DockGlassPanel(flow, screenPoint.x, screenPoint.y);
				f.setGlassPane(glass);
				glass.setVisible(true);

				robot.mousePress(InputEvent.BUTTON1_MASK);
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
	}

	public void setMovable(boolean isMovable) {
		this.isMovable = isMovable;
	}

	public boolean isMovable() {
		return isMovable;
	}
}
