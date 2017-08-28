package satis.iface;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.filechooser.FileFilter;

public abstract class IFileFilter extends FileFilter {
	public final static int ALL_SUPPORTED_FORMATS = -1;

	public abstract List<String> getExtensions();

	public IFileFilter(int filterIndex) {
		this.filterIndex = filterIndex;
	}

	private int filterIndex = ALL_SUPPORTED_FORMATS;

	public String getExtension() {
		if (filterIndex == ALL_SUPPORTED_FORMATS) {
			return null;
		} else {
			return getExtensions().get(filterIndex);
		}
	}

	@Override
	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}

		String name = f.getName();
		int index = name.lastIndexOf(".");
		if (index < 0) {
			return false;
		}

		String ext = name.substring(index + 1, name.length()).trim().toLowerCase();
		if (filterIndex == ALL_SUPPORTED_FORMATS) {
			return getExtensions().contains(ext);
		} else {
			return getExtensions().get(filterIndex).equals(ext);
		}
	}

	@Override
	public String getDescription() {
		if (filterIndex == ALL_SUPPORTED_FORMATS) {
			return "Все поддерживаемые форматы";
		} else {
			return "*." + getExtensions().get(filterIndex);
		}
	}

	// --- Implementations ---
	public static class ImageFileFilter extends IFileFilter {
		public final static int BMP = 0;

		public final static int PNG = 1;

		public final static int JPG = 2;

		public final static int JPEG = 3;

		public final static int TIF = 4;

		public final static int TIFF = 5;

		public final static int GIF = 6;

		private List<String> extensions = new ArrayList<String>();

		@Override
		public List<String> getExtensions() {
			return extensions;
		}

		public ImageFileFilter(int filterIndex) {
			super(filterIndex);

			extensions.add("bmp");
			extensions.add("png");
			extensions.add("jpg");
			extensions.add("jpeg");
			extensions.add("tif");
			extensions.add("tiff");
			extensions.add("gif");
		}
	}

	public static class XMLFileFilter extends IFileFilter {
		public final static int XML = 0;

		private List<String> extensions = new ArrayList<String>();

		@Override
		public List<String> getExtensions() {
			return extensions;
		}

		public XMLFileFilter(int filterIndex) {
			super(filterIndex);
			extensions.add("xml");
		}
	}

	public static class SchemaFileFilter extends IFileFilter {
		public final static int SVG = 0;

		private List<String> extensions = new ArrayList<String>();

		@Override
		public List<String> getExtensions() {
			return extensions;
		}

		public SchemaFileFilter(int filterIndex) {
			super(filterIndex);
			extensions.add("svg");
		}
	}

	public static class TableFileFilter extends IFileFilter {
		public final static int HTML = 0;

		public final static int XLS = 1;

		public final static int XML = 2;

		public final static int TXT = 3;

		private List<String> extensions = new ArrayList<String>();

		@Override
		public List<String> getExtensions() {
			return extensions;
		}

		public TableFileFilter(int filterIndex) {
			super(filterIndex);
			extensions.add("html");
			extensions.add("xls");
			extensions.add("xml");
			extensions.add("txt");
		}
	}

	public static class DocumentFileFilter extends IFileFilter {
		public final static int JCM = 0;

		private List<String> extensions = new ArrayList<String>();

		@Override
		public List<String> getExtensions() {
			return extensions;
		}

		public DocumentFileFilter(int filterIndex) {
			super(filterIndex);
			extensions.add("jcm");
		}
	}
}
