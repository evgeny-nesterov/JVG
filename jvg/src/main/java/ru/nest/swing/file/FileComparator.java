package ru.nest.swing.file;

import java.io.File;

public class FileComparator implements java.util.Comparator<File> {
	@Override
	public int compare(File f1, File f2) {
		if (f1.isDirectory() && !f2.isDirectory()) {
			return -1;
		}

		if (!f1.isDirectory() && f2.isDirectory()) {
			return 1;
		}

		if (!f1.isDirectory()) {
			return f1.getName().compareToIgnoreCase(f2.getName());
		}

		return f1.compareTo(f2);
	}
}
