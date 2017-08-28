package javax.swing;

import java.util.Vector;

public class DynamicComboBoxModel extends DefaultComboBoxModel<Object> {
	private static final long serialVersionUID = 1L;

	/** the selected object */
	private Object selectedObject;

	/** the items vector */
	private Vector<Object> objects;

	/** Creates a new instance of DynamicComboBoxModel */
	public DynamicComboBoxModel() {
	}

	/** Creates a new instance of DynamicComboBoxModel */
	public DynamicComboBoxModel(Vector<Object> objects) {
		this.objects = objects;
		if (objects != null && !objects.isEmpty()) {
			setSelectedItem(objects.elementAt(0));
		}
	}

	public void setElements(Object[] items) {
		if (objects != null && !objects.isEmpty()) {
			removeAllElements();
			/*
			 * int size = objects.size(); objects.clear(); fireIntervalRemoved(this, 0, size - 1);
			 */
		} else {
			objects = new Vector<Object>(items.length);
		}
		for (int i = 0; i < items.length; i++) {
			objects.add(items[i]);
		}
		int size = objects.size();
		fireContentsChanged(this, 0, (size > 0) ? size - 1 : 0);
		setSelectedItem(objects.get(0));
	}

	public void setElements(Vector<Object> elements) {
		if (objects != null && !objects.isEmpty()) {
			removeAllElements();
			/*
			 * int size = objects.size(); objects.clear(); if (size > 0) { fireIntervalRemoved(this, 0, size - 1); } else {
			 * fireIntervalRemoved(this, 0, 0); }
			 */
		}
		objects = elements;
		int size = objects.size();
		// fireContentsChanged(this, 0, (size > 0) ? size - 1 : 0);
		fireContentsChanged(this, -1, -1);
		if (size > 0) {
			setSelectedItem(objects.get(0));
		}
	}

	/**
	 * Set the value of the selected item. The selected item may be null.
	 * <p>
	 * 
	 * @param anObject
	 *            The combo box value or null for no selection.
	 */
	@Override
	public void setSelectedItem(Object anObject) {
		if ((selectedObject != null && !selectedObject.equals(anObject)) || selectedObject == null && anObject != null) {
			selectedObject = anObject;
			fireContentsChanged(this, -1, -1);
		}
	}

	// implements javax.swing.ComboBoxModel
	@Override
	public Object getSelectedItem() {
		return selectedObject;
	}

	// implements javax.swing.ListModel
	@Override
	public int getSize() {
		if (objects == null) {
			return 0;
		}
		return objects.size();
	}

	// implements javax.swing.ListModel
	@Override
	public Object getElementAt(int index) {
		if (index >= 0 && index < objects.size())
			return objects.elementAt(index);
		else
			return null;
	}

	/**
	 * Returns the index-position of the specified object in the list.
	 * 
	 * @param anObject
	 * @return an int representing the index position, where 0 is the first position
	 */
	@Override
	public int getIndexOf(Object anObject) {
		return objects.indexOf(anObject);
	}

	// implements javax.swing.MutableComboBoxModel
	@Override
	public void addElement(Object anObject) {
		if (objects == null) {
			objects = new Vector<Object>();
		}
		objects.addElement(anObject);
		fireIntervalAdded(this, objects.size() - 1, objects.size() - 1);
		if (objects.size() == 1 && selectedObject == null && anObject != null) {
			setSelectedItem(anObject);
		}
	}

	public void contentsChanged() {
		fireContentsChanged(this, -1, -1);
	}

	// implements javax.swing.MutableComboBoxModel
	@Override
	public void insertElementAt(Object anObject, int index) {
		objects.insertElementAt(anObject, index);
		fireIntervalAdded(this, index, index);
	}

	// implements javax.swing.MutableComboBoxModel
	@Override
	public void removeElementAt(int index) {
		if (getElementAt(index) == selectedObject) {
			if (index == 0) {
				setSelectedItem(getSize() == 1 ? null : getElementAt(index + 1));
			} else {
				setSelectedItem(getElementAt(index - 1));
			}
		}

		objects.removeElementAt(index);
		fireIntervalRemoved(this, index, index);
	}

	// implements javax.swing.MutableComboBoxModel
	@Override
	public void removeElement(Object anObject) {
		int index = objects.indexOf(anObject);
		if (index != -1) {
			removeElementAt(index);
		}
	}

	/**
	 * Empties the list.
	 */
	@Override
	public void removeAllElements() {
		if (objects == null) {
			return;
		}
		if (objects.size() > 0) {
			int firstIndex = 0;
			int lastIndex = objects.size() - 1;
			objects.removeAllElements();
			selectedObject = null;
			fireIntervalRemoved(this, firstIndex, lastIndex);
		} else {
			selectedObject = null;
		}
	}
}
