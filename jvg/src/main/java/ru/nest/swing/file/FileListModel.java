package ru.nest.swing.file;

import javax.swing.*;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

public class FileListModel extends AbstractListModel {
	public final static FileComparator fileComparator = new FileComparator();

	public FileListModel() {
	}

	private FilenameFilter filter;

	public FilenameFilter getFilter() {
		return filter;
	}

	public void setFilter(FilenameFilter filter) {
		this.filter = filter;
		refresh();
	}

	public void refresh() {
		setRoot(root);
	}

	private File root;

	private File[] files;

	public File getRoot() {
		return root;
	}

	public boolean setRoot(File root) {
		// if(Util.equals(root, this.root))
		// {
		// return false;
		// }

		if (root != null) {
			if (!root.isDirectory()) {
				return false;
			}

			files = root.listFiles(filter);
			if (files != null && fileComparator != null) {
				Arrays.sort(files, fileComparator);
			}
		} else {
			files = File.listRoots();
			if (filter != null && filter != null) {
				int size = 0;
				for (int i = 0; i < files.length; i++) {
					if (filter.accept(files[i], null)) {
						size++;
					} else {
						files[i] = null;
					}
				}

				if (size == 0) {
					files = null;
				} else if (size != files.length) {
					File[] filteredFiles = new File[size];
					int index = 0;
					for (int i = 0; i < files.length; i++) {
						if (files[i] != null) {
							filteredFiles[index++] = files[i];
						}
					}
				}
			}
		}

		if (files == null) {
			files = new File[0];
		}
		this.root = root;
		fireContentsChanged(this, 0, files.length);

		return true;
	}

	public boolean open(int index) {
		if (index >= 0 && index < files.length) {
			File newRoot = files[index];
			return setRoot(newRoot);
		}

		return false;
	}

	public boolean moveToUp() {
		if (root != null) {
			File newRoot = root.getParentFile();
			return setRoot(newRoot);
		}

		return false;
	}

	public void delete(int index) {
		File[] newFiles = new File[files.length - 1];
		System.arraycopy(files, 0, newFiles, 0, index);
		System.arraycopy(files, index + 1, newFiles, index, newFiles.length - index);
		files = newFiles;
		fireIntervalRemoved(this, index, index);
	}

	public File[] getFiles() {
		return files;
	}

	@Override
	public int getSize() {
		return files != null ? files.length : 0;
	}

	@Override
	public Object getElementAt(int i) {
		return files != null && i >= 0 && i < files.length ? files[i] : null;
	}

	public void update(int index) {
		fireContentsChanged(this, index, index);
	}
}
