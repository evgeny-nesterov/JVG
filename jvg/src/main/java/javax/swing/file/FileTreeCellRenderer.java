package javax.swing.file;

import java.awt.Component;
import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

public class FileTreeCellRenderer extends DefaultTreeCellRenderer {
	private static ImageIcon imgComp = new ImageIcon(ExplorerPanel.class.getResource("../img/computer.gif"));

	private static ImageIcon imgFile = new ImageIcon(ExplorerPanel.class.getResource("../img/file.gif"));

	private static ImageIcon imgDrive = new ImageIcon(ExplorerPanel.class.getResource("../img/drive.png"));

	private static ImageIcon imgDirOpen = new ImageIcon(ExplorerPanel.class.getResource("../img/DirectoryOpen.gif"));

	private static ImageIcon imgDirClose = new ImageIcon(ExplorerPanel.class.getResource("../img/DirectoryClose.gif"));

	public static Icon getIcon(Object node, boolean expanded) {
		if (node instanceof File) {
			File file = (File) node;
			Object parent = file.getParent();
			if (parent != null) {
				if (file.isDirectory()) {
					return expanded ? imgDirOpen : imgDirClose;
				} else {
					return imgFile;
				}
			} else {
				return imgDrive;
			}
		} else {
			return imgComp;
		}
	}

	public static String getText(Object node, boolean path) {
		if (node instanceof File) {
			File file = (File) node;
			Object parent = file.getParent();
			if (!path && parent != null) {
				return file.getName();
			} else {
				return file.getPath();
			}
		}

		return node == null ? "" : node.toString();
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		JLabel lbl = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		lbl.setIcon(getIcon(value, expanded));
		lbl.setText(getText(value, false));
		return lbl;
	}
}
