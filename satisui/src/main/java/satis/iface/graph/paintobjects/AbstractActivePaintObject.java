package satis.iface.graph.paintobjects;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import satis.iface.graph.Group;
import satis.iface.graph.paintobjects.AbstractActivePaintObject.ChangeListener;

public abstract class AbstractActivePaintObject extends AbstractPaintObject implements ActivePaintObject {
	public AbstractActivePaintObject() {
	}

	public AbstractActivePaintObject(boolean consume) {
		this.consume = consume;
	}

	private boolean consume = false;

	private double mx, my;

	@Override
	public void processMouseEvent(Group group, MouseEvent e) {
		if (group != null) {
			if (e.getID() == MouseEvent.MOUSE_PRESSED) {
				mx = group.viewToModelX(e.getX());
				my = group.viewToModelY(e.getY());
			} else if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
				double newX = group.viewToModelX(e.getX());
				double newY = group.viewToModelY(e.getY());

				translate(newX - mx, newY - my);
				compile(group);
				fireChangesOccurred();

				mx = newX;
				my = newY;
			}

			if (consume) {
				e.consume();
			}
		}
	}

	public abstract void translate(double modelX, double modelY);

	@Override
	public void paintActive(Group group, Graphics g) {
		paint(group, g);
	}

	private ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();

	public void addListener(ChangeListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	public void removeListener(ChangeListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	public void fireChangesOccurred() {
		synchronized (listeners) {
			for (ChangeListener l : listeners) {
				l.changesOccurred();
			}
		}
	}

	public static interface ChangeListener {
		public void changesOccurred();
	}
}
