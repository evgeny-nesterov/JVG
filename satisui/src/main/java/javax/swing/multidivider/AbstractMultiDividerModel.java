package javax.swing.multidivider;

import java.util.ArrayList;

public abstract class AbstractMultiDividerModel implements MultiDividerModel {
	private ArrayList<MultiDividerListener> listeners = new ArrayList<MultiDividerListener>();

	@Override
	public void addListener(MultiDividerListener listener) {
		if (listener != null) {
			synchronized (listeners) {
				listeners.add(listener);
			}
		}
	}

	@Override
	public void removeListener(MultiDividerListener listener) {
		if (listener != null) {
			synchronized (listeners) {
				listeners.remove(listener);
			}
		}
	}

	@Override
	public void fireStructureChanged() {
		synchronized (listeners) {
			for (MultiDividerListener listener : listeners) {
				listener.structureChanged();
			}
		}
	}

	@Override
	public void firePositionsChanged() {
		synchronized (listeners) {
			for (MultiDividerListener listener : listeners) {
				listener.positionsChanged();
			}
		}
	}

	@Override
	public void fireDividerIndexChanged(int index) {
		synchronized (listeners) {
			for (MultiDividerListener listener : listeners) {
				listener.dividerIndexChanged(index);
			}
		}
	}
}
