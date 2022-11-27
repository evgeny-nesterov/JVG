package ru.nest.jvg;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import ru.nest.jvg.actionarea.JVGActionArea;
import ru.nest.jvg.event.JVGSelectionEvent;
import ru.nest.jvg.event.JVGSelectionListener;

public class JVGDefaultSelectionModel implements JVGSelectionModel {
	private HashMap<JVGComponent, Boolean> hash;

	public JVGDefaultSelectionModel() {
		hash = new HashMap<>();
	}

	@Override
	public void setSelection(JVGComponent o) {
		if (o != null) {
			setSelection(new JVGComponent[] { o });
		}
	}

	@Override
	public void setSelection(JVGComponent[] o) {
		if (o != null) {
			int newSize = o.length;
			int add = newSize, remove = 0;
			int size = getSelectionCount();
			for (int i = 0; i < o.length; i++) {
				if (isSelected(o[i])) {
					add--;
				} else if (o[i] instanceof JVGActionArea) {
					o[i] = null;
					newSize--;
					add--;
				}
			}
			remove = size - (newSize - add);

			int changeSize = add + remove;
			if (changeSize > 0) {
				JVGComponent[] change = new JVGComponent[changeSize];
				boolean[] isAdded = new boolean[change.length];
				JVGComponent[] newSelection = new JVGComponent[newSize];

				int pos1 = 0, pos2 = 0;
				for (int i = 0; i < o.length; i++) {
					if (o[i] != null) {
						if (!isSelected(o[i])) {
							isAdded[pos1] = true;
							change[pos1++] = o[i];
						}
						newSelection[pos2++] = o[i];
					}
				}

				JVGComponent[] old = selection;
				hash.clear();
				for (int i = 0; i < newSize; i++) {
					hash.put(newSelection[i], Boolean.TRUE);
				}
				selection = newSelection;

				// unselected
				for (int i = 0; i < size; i++) {
					if (!isSelected(old[i])) {
						change[pos1++] = old[i];
					}
				}

				fireChangeSelection(new JVGSelectionEvent(change, isAdded));
			}
		}
	}

	@Override
	public void addSelection(JVGComponent o) {
		if (o != null) {
			JVGComponent[] toAdd = new JVGComponent[1];
			toAdd[0] = o;
			addSelection(toAdd);
		}
	}

	@Override
	public void addSelection(JVGComponent[] o) {
		if (o == null || o.length == 0) {
			return;
		}

		int addSize = o.length;
		int size = getSelectionCount();
		if (size > 0) {
			for (int i = 0; i < o.length; i++) {
				if (isSelected(o[i]) || o[i] instanceof JVGActionArea) {
					o[i] = null;
					addSize--;
				}
			}
		}

		if (addSize > 0) {
			JVGComponent[] newSelection = new JVGComponent[size + addSize];
			if (size > 0) {
				System.arraycopy(selection, 0, newSelection, 0, size);
			}

			int pos = size;
			for (int i = 0; i < o.length; i++) {
				if (o[i] != null) {
					newSelection[pos++] = o[i];
				}
			}

			JVGComponent[] added = new JVGComponent[addSize];
			System.arraycopy(newSelection, size, added, 0, addSize);

			for (int i = 0; i < addSize; i++) {
				hash.put(added[i], Boolean.TRUE);
			}
			selection = newSelection;

			boolean[] isAdded = new boolean[addSize];
			Arrays.fill(isAdded, true);
			fireChangeSelection(new JVGSelectionEvent(added, isAdded));
		}
	}

	@Override
	public void removeSelection(JVGComponent o) {
		if (o != null) {
			removeSelection(new JVGComponent[] { o });
		}
	}

	@Override
	public void removeSelection(JVGComponent[] o) {
		if (o != null && selection != null && o.length > 0) {
			int size = getSelectionCount();
			if (size > 0) {
				int removeSize = o.length;
				for (int i = 0; i < o.length; i++) {
					if (!isSelected(o[i])) {
						removeSize--;
					} else {
						hash.remove(o[i]);
					}
				}

				if (removeSize > 0) {
					JVGComponent[] newSelection = new JVGComponent[size - removeSize];
					JVGComponent[] removed = new JVGComponent[removeSize];

					int pos = 0, pos1 = 0;
					for (int i = 0; i < size; i++) {
						if (isSelected(selection[i])) {
							newSelection[pos++] = selection[i];
						} else {
							removed[pos1++] = selection[i];
						}
					}

					selection = newSelection;
					fireChangeSelection(new JVGSelectionEvent(removed, new boolean[removeSize]));
				}
			}
		}
	}

	@Override
	public void clearSelection() {
		hash.clear();
		int size = getSelectionCount();
		if (size > 0) {
			JVGSelectionEvent event = new JVGSelectionEvent(selection, new boolean[size]);
			selection = null;
			fireChangeSelection(event);
		}
	}

	private JVGComponent[] selection;

	@Override
	public JVGComponent[] getSelection() {
		if (selection != null) {
			int pathSize = selection.length;
			JVGComponent[] result = new JVGComponent[pathSize];
			System.arraycopy(selection, 0, result, 0, pathSize);
			return result;
		}
		return null;
	}

	@Override
	public int getSelectionCount() {
		return (selection == null) ? 0 : selection.length;
	}

	@Override
	public boolean isSelected(JVGComponent o) {
		return (o != null) ? hash.containsKey(o) : false;
	}

	private List<JVGSelectionListener> listeners = new ArrayList<>();

	@Override
	public void addSelectionListener(JVGSelectionListener listener) {
		if (listener != null) {
			listeners.add(listener);
		}
	}

	@Override
	public void removeSelectionListener(JVGSelectionListener listener) {
		if (listener != null) {
			listeners.remove(listener);
		}
	}

	private void fireChangeSelection(JVGSelectionEvent event) {
		for (int i = 0; i < listeners.size(); i++) {
			JVGSelectionListener listener = listeners.get(i);
			listener.selectionChanged(event);
		}

		JVGComponent[] c = event.getChange();
		for (int i = 0; i < c.length; i++) {
			c[i].dispatchEvent(event);
		}

		invalidate();
	}

	private boolean valid = false;

	@Override
	public boolean isValid() {
		return valid;
	}

	@Override
	public void invalidate() {
		if (valid) {
			valid = false;
			selectionBounds = null;
		}
	}

	public void validate() {
		if (!valid) {
			valid = true;
			selectionBounds = computeSelectionBounds();
		}
	}

	private Rectangle2D selectionBounds;

	@Override
	public Rectangle2D getSelectionBounds() {
		validate();
		return selectionBounds;
	}

	public Rectangle2D computeSelectionBounds() {
		Rectangle2D bounds = new Rectangle2D.Double();
		if (selection != null) {
			int pathSize = selection.length;
			if (pathSize > 0) {
				Rectangle2D b = selection[0].getRectangleBounds();
				if (b != null) {
					bounds.setFrame(b);
					for (int i = 1; i < pathSize; i++) {
						b = selection[i].getRectangleBounds();
						if (b != null) {
							Rectangle2D.union(selection[i].getRectangleBounds(), bounds, bounds);
						}
					}
				}
			}
		}
		return bounds;
	}
}
