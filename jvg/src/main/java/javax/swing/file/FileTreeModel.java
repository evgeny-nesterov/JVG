package javax.swing.file;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.EventListener;
import java.util.HashMap;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class FileTreeModel implements TreeModel {
	private FilenameFilter filter;

	private java.util.Comparator<File> fileComparator;

	public FileTreeModel(FilenameFilter filter, java.util.Comparator<File> fileComparator) {
		this.filter = filter;
		this.fileComparator = fileComparator;
	}

	private HashMap<String, SoftReference<File[]>> cache = new HashMap<String, SoftReference<File[]>>();

	private File[] getFiles(Object node) {
		if (root.equals(node)) {
			return getFiles(null);
		} else if (node instanceof File) {
			return getFiles((File) node);
		} else {
			return null;
		}
	}

	private File[] getFiles(File parent) {
		String path = parent != null ? parent.getAbsolutePath() : null;
		SoftReference<File[]> ref = cache.get(path);;
		File[] files = null;
		if (ref != null) {
			files = ref.get();
		}

		if (files == null) {
			if (parent == null) {
				files = File.listRoots();
			} else {
				File dir = parent;
				files = dir.listFiles(filter);
				if (files != null && fileComparator != null) {
					Arrays.sort(files, fileComparator);
				}
			}
			cache.put(path, new SoftReference<File[]>(files));
		}

		return files;
	}

	public void clear() {
		cache.clear();
	}

	class FileRoot {
		private String name = System.getProperty("file.computer.name", "Computer");

		@Override
		public String toString() {
			return name;
		}

		@Override
		public boolean equals(Object o) {
			return o instanceof FileRoot;
		}

		@Override
		public int hashCode() {
			return name.hashCode();
		}
	}

	private FileRoot root = new FileRoot();

	@Override
	public Object getRoot() {
		return root;
	}

	@Override
	public Object getChild(Object parent, int index) {
		return getFiles(parent)[index];
	}

	@Override
	public int getChildCount(Object parent) {
		return getFiles(parent).length;
	}

	@Override
	public boolean isLeaf(Object node) {
		if (root.equals(node)) {
			return false;
		}

		if (node instanceof File) {
			File file = (File) node;
			if (!file.isDirectory()) {
				return true;
			}

			File[] files = getFiles(file);
			return files == null || files.length == 0;
		}

		return false;
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		File[] files = getFiles(parent);
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				if (files[i] == child) {
					return i;
				}
			}
		}
		return -1;
	}

	protected EventListenerList listenerList = new EventListenerList();

	@Override
	public void addTreeModelListener(TreeModelListener l) {
		listenerList.add(TreeModelListener.class, l);
	}

	@Override
	public void removeTreeModelListener(TreeModelListener l) {
		listenerList.remove(TreeModelListener.class, l);
	}

	public TreeModelListener[] getTreeModelListeners() {
		return listenerList.getListeners(TreeModelListener.class);
	}

	protected void fireTreeNodesChanged(Object source, Object[] path, int[] childIndices, Object[] children) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TreeModelListener.class) {
				// Lazily create the event:
				if (e == null)
					e = new TreeModelEvent(source, path, childIndices, children);
				((TreeModelListener) listeners[i + 1]).treeNodesChanged(e);
			}
		}
	}

	protected void fireTreeNodesInserted(Object source, Object[] path, int[] childIndices, Object[] children) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TreeModelListener.class) {
				// Lazily create the event:
				if (e == null)
					e = new TreeModelEvent(source, path, childIndices, children);
				((TreeModelListener) listeners[i + 1]).treeNodesInserted(e);
			}
		}
	}

	protected void fireTreeNodesRemoved(Object source, Object[] path, int[] childIndices, Object[] children) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TreeModelListener.class) {
				// Lazily create the event:
				if (e == null)
					e = new TreeModelEvent(source, path, childIndices, children);
				((TreeModelListener) listeners[i + 1]).treeNodesRemoved(e);
			}
		}
	}

	protected void fireTreeStructureChanged(Object source, Object[] path, int[] childIndices, Object[] children) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TreeModelListener.class) {
				// Lazily create the event:
				if (e == null)
					e = new TreeModelEvent(source, path, childIndices, children);
				((TreeModelListener) listeners[i + 1]).treeStructureChanged(e);
			}
		}
	}

	private void fireTreeStructureChanged(Object source, TreePath path) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TreeModelListener.class) {
				// Lazily create the event:
				if (e == null)
					e = new TreeModelEvent(source, path);
				((TreeModelListener) listeners[i + 1]).treeStructureChanged(e);
			}
		}
	}

	public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
		return listenerList.getListeners(listenerType);
	}
}
