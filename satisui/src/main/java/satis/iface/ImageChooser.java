package satis.iface;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;

public class ImageChooser extends JFileChooser {
	private static final long serialVersionUID = 1L;
	private static String fileName;

	public ImageChooser() {
		addChoosableFileFilter(new IFileFilter.ImageFileFilter(IFileFilter.ImageFileFilter.BMP));
		addChoosableFileFilter(new IFileFilter.ImageFileFilter(IFileFilter.ImageFileFilter.JPEG));
		addChoosableFileFilter(new IFileFilter.ImageFileFilter(IFileFilter.ImageFileFilter.JPG));
		addChoosableFileFilter(new IFileFilter.ImageFileFilter(IFileFilter.ImageFileFilter.PNG));
		addChoosableFileFilter(new IFileFilter.ImageFileFilter(IFileFilter.ImageFileFilter.TIF));
		addChoosableFileFilter(new IFileFilter.ImageFileFilter(IFileFilter.ImageFileFilter.TIFF));
		addChoosableFileFilter(new IFileFilter.ImageFileFilter(IFileFilter.ImageFileFilter.GIF));
		addChoosableFileFilter(new IFileFilter.ImageFileFilter(IFileFilter.ALL_SUPPORTED_FORMATS));
		setAcceptAllFileFilterUsed(false);
		setDialogTitle("Выбор изображения");
		setFileSelectionMode(JFileChooser.FILES_ONLY);
		setMultiSelectionEnabled(false);

		if (fileName != null) {
			File file = new File(fileName);
			if (file.exists()) {
				setCurrentDirectory(file);
				setSelectedFile(file);
			}
		}
	}

	public Object getSelectedObject() {
		return getSelectedFile();
	}

	public static Object choose(Component parent, int dialogType, ImageChooser chooser) {
		if (chooser == null) {
			chooser = new ImageChooser();
		}

		int status;
		if (dialogType == JFileChooser.SAVE_DIALOG) {
			status = chooser.showSaveDialog(parent);
		} else {
			status = chooser.showOpenDialog(parent);
		}

		Object object = chooser.getSelectedObject();
		if (status == JFileChooser.APPROVE_OPTION && object != null) {
			if (object instanceof File) {
				File file = (File) object;
				String ext = getExtension(file);
				if (ext == null && dialogType == JFileChooser.SAVE_DIALOG) {
					IFileFilter ff = (IFileFilter) chooser.getFileFilter();
					ext = ff.getExtension();
					file = new File(file.getPath() + "." + ext);
				}

				if (ext != null) {
					fileName = file.getPath();
					return file;
				}
			}
			return object;
		}
		return null;
	}

	public static String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');
		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).trim().toLowerCase();
		}
		return ext;
	}
}
