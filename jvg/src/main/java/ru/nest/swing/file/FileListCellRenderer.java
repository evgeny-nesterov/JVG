package ru.nest.swing.file;

import ru.nest.swing.Util;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class FileListCellRenderer extends DefaultListCellRenderer {
	private ImageIcon imgFile = new ImageIcon(ExplorerPanel.class.getResource("../img/file.gif"));

	private ImageIcon imgDrive = new ImageIcon(ExplorerPanel.class.getResource("../img/drive.png"));

	private ImageIcon imgDirClose = new ImageIcon(ExplorerPanel.class.getResource("../img/DirectoryClose.gif"));

	private IconByExtention iconByExtention;

	public void setIconByExtention(IconByExtention iconByExtention) {
		this.iconByExtention = iconByExtention;
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if (value instanceof File) {
			File file = (File) value;
			if (file.getParent() != null) {
				lbl.setText(file.getName());
				if (file.isDirectory()) {
					lbl.setIcon(imgDirClose);
				} else {
					Icon icon = null;
					if (iconByExtention != null) {
						String ext = Util.getFileExtention(file.getName());
						icon = iconByExtention.getIcon(ext);
					}

					if (icon == null) {
						icon = imgFile;
					}

					lbl.setIcon(icon);
				}
			} else {
				lbl.setIcon(imgDrive);
				lbl.setText(file.getPath());
			}
		}
		return lbl;
	}
}
