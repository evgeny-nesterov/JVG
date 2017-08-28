package javax.swing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ArrayListModel<T> extends AbstractListModel<T> {
	private static final long serialVersionUID = 2845055257855562370L;

	private List<T> elements = new ArrayList<T>();

	public ArrayListModel() {
	}

	public List<T> getElements() {
		return elements;
	}

	public void setElements(List<T> elements) {
		this.elements = elements;
		fireContentsChanged(this, 0, 0);
	}

	public void addSort(T element) {
		elements.add(element);
		Collections.sort((List<? extends Comparable>) elements);
		int index = elements.indexOf(element);
		fireIntervalAdded(this, index, index);
	}

	public void add(T element, int index) {
		elements.add(index, element);
		fireIntervalAdded(this, index, index);
	}

	public void add(Collection<T> insElements, int index) {
		int i = index;
		for (T element : insElements) {
			elements.add(i++, element);
		}
		fireContentsChanged(this, 0, 0);
	}

	public void remove(int index) {
		elements.remove(index);
		fireIntervalRemoved(this, index, index);
	}

	@Override
	public int getSize() {
		return elements.size();
	}

	@Override
	public T getElementAt(int index) {
		return elements.get(index);
	}
}
