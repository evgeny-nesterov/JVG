package javax.swing.dock;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.glass.GlassPanePanel;

public class DockGlassPanel extends GlassPanePanel {
	private static final long serialVersionUID = 1L;

	private int dragX;

	private int dragY;

	private boolean mxSet = false;

	private int mx;

	private int my;

	private BufferedImage img;

	private Container dst;

	private Dockable<Object> dockable;

	private Object constraints;

	private DockFlow flow;

	private boolean same;

	private Cursor lastCursor;

	public DockGlassPanel(DockFlow flow, int dragX, int dragY) {
		this.flow = flow;
		this.dragX = dragX;
		this.dragY = dragY;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		setVisible(false);
		if (constraints != null && dst != null && !DockUtils.isAncestorOf(flow.getUndockComponent(), dst) && !same) {
			ContainerListener undockListener = null;
			Container flowParent = flow.getUndockComponent().getParent();
			if (flowParent instanceof Dockable && flowParent != dockable) {
				final Dockable<?> flowDockParent = (Dockable<?>) flowParent;
				undockListener = new ContainerAdapter() {
					@Override
					public void componentRemoved(ContainerEvent e) {
						if (e.getChild() == flow.getUndockComponent()) {
							flowDockParent.undock(flow);
						}
					}
				};
				flowParent.addContainerListener(undockListener);
			}

			dockable.dock(flow, constraints);

			if (undockListener != null) {
				flowParent.removeContainerListener(undockListener);
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (!isVisible()) {
			return;
		}

		mx = e.getLocationOnScreen().x;
		my = e.getLocationOnScreen().y;
		mxSet = true;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (!isVisible()) {
			return;
		}

		mx = e.getLocationOnScreen().x;
		my = e.getLocationOnScreen().y;
		mxSet = true;

		int imgHeight = Math.min(flow.getUndockComponent().getHeight(), 100);
		// TODO check on size < 0
		if (img == null || img.getWidth() != flow.getUndockComponent().getWidth() || img.getHeight() != imgHeight) {
			img = new BufferedImage(flow.getUndockComponent().getWidth(), imgHeight, BufferedImage.TYPE_INT_ARGB);
			flow.getUndockComponent().paintAll(img.getGraphics());
		}

		findDockPanel(mx, my);
		repaint();
	}

	public void findDockPanel(int screenX, int screenY) {
		Container root = getTopLevelAncestor();
		Point p = root.getLocationOnScreen();

		int x = screenX - p.x;
		int y = screenY - p.y;

		Component c = DockUtils.getDeepestComponentAt(root, x, y);

		constraints = null;
		dst = null;

		Cursor cursor = null;

		Dockable<?> undockParent = null;
		Container flowParent = flow.getUndockComponent().getParent();
		if (flowParent instanceof Dockable) {
			undockParent = (Dockable<?>) flowParent;
		}

		// find dock container
		same = false;
		Component d = c;
		while (d != null) {
			same |= d == flow.getUndockComponent();
			if (d instanceof Dockable && d instanceof Container) {
				dst = (Container) d;
				dockable = (Dockable<Object>) d;

				boolean accessed = true;
				if (undockParent != null && !undockParent.acceptUndock(flow, dockable)) {
					accessed = false;
				} else if (!dockable.acceptDock(flow)) {
					accessed = false;
				}

				if (accessed) {
					Point dstLoc = dst.getLocationOnScreen();
					constraints = dockable.getDockConstraints(flow, screenX - dstLoc.x, screenY - dstLoc.y);
					if (constraints != null && !same) {
						cursor = dockable.getDockCursor(flow, constraints);
					} else {
						cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
					}
					break;
				}
			}
			d = d.getParent();
		}
		// set cursor
		cursor = cursor != null ? cursor : Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
		if (lastCursor != cursor) {
			setCursor(cursor);
			repaint();
			lastCursor = cursor;
		}
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		if (!flow.getUndockComponent().isShowing()) {
			return;
		}

		Point p1 = getLocationOnScreen();
		Point p2 = flow.getUndockComponent().getLocationOnScreen();

		Graphics2D g2d = (Graphics2D) g.create();
		if (mxSet) {
			int dx = p2.x - p1.x + mx - dragX;
			int dy = p2.y - p1.y + my - dragY;
			if (img != null) {
				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
				g2d.drawImage(img, dx, dy, null);
				g2d.setPaintMode();
			}
		}

		if (dst != null) {
			Point dstLoc = dst.getLocationOnScreen();
			int x = dstLoc.x - p1.x;
			int y = dstLoc.y - p1.y;

			if (!same) {
				if (constraints != null) {
					g.translate(x, y);
					dockable.drawDockPlace(g, flow, constraints);
					g.translate(-x, -y);
				}
			} else {
				x = p2.x - p1.x;
				y = p2.y - p1.y;

				// TODO tab is incorrect
				DockUtils.drawDockRect(g2d, x, y, flow.getUndockComponent().getWidth(), flow.getUndockComponent().getHeight());
			}
		}
	}
}
