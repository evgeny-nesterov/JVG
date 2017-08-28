package ru.nest.jvg.editor.clipboard;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;

public class ClipboardListModel extends AbstractListModel {
	private List<JVGClipboardContext> list = new ArrayList<JVGClipboardContext>();

	public void clear() {
		int size = list.size();
		if (size > 0) {
			list.clear();
			fireIntervalRemoved(this, 0, size);
		}
	}

	@Override
	public int getSize() {
		return list.size();
	}

	@Override
	public Object getElementAt(int i) {
		return list.get(i);
	}

	public void add(JVGClipboardContext ctx) {
		list.add(ctx);
		fireIntervalAdded(this, list.size(), list.size());
	}

	public void remove(JVGClipboardContext ctx) {
		int index = list.indexOf(ctx);
		if (index >= 0) {
			list.remove(index);
			fireIntervalRemoved(this, index, index);
		}
	}

	public void update(JVGClipboardContext ctx) {
		int index = list.indexOf(ctx);
		if (index >= 0) {
			fireContentsChanged(this, index, index);
		}
	}
}
