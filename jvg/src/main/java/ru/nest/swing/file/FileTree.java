package ru.nest.swing.file;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class FileTree extends JTree {
	public final static FileComparator fileComparator = new FileComparator();

	private FileTreeModel model;

	public FileTree() {
		this(null);
	}

	public FileTree(FilenameFilter filter) {
		model = new FileTreeModel(filter, fileComparator);
		setModel(model);
		setCellRenderer(new FileTreeCellRenderer());

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					TreePath path = getPathForLocation(e.getX(), e.getY());
					if (path != null) {
						if (e.getClickCount() == 1) {
							filePressed(e, path);
						} else if (e.getClickCount() == 2) {
							// Desktop.open(file); // JRE 1.6
						}
					}
				}
			}
		});
	}

	protected void filePressed(MouseEvent e, TreePath path) {
	}

	public TreePath getPath(File file) {
		ArrayList<File> files = new ArrayList<>();
		while (file != null) {
			files.add(file);
			file = file.getParentFile();
		}

		TreePath path = new TreePath(model.getRoot());
		for (int i = files.size() - 1; i >= 0; i--) {
			path = path.pathByAddingChild(files.get(i));
		}

		return path;
	}

	public static void main(String[] args) {
		ru.nest.jvg.editor.Util.installDefaultFont();

		FileTree tree = new FileTree(null);

		JFrame f = new JFrame();
		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		f.setBounds(300, 100, 600, 400);
		f.setContentPane(new JScrollPane(tree));
		f.setVisible(true);
	}
}
