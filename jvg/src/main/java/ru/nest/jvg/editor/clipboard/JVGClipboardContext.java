package ru.nest.jvg.editor.clipboard;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import javax.swing.ImageIcon;

import ru.nest.jvg.editor.SnapshotData;

public class JVGClipboardContext {
	public final static int SNAPSHOT_WIDTH = 120;

	public final static int SNAPSHOT_HEIGHT = 80;

	public final static int INSETS = 20;

	private int width = -1;

	private int height = -1;

	private String data;

	private String file;

	private ImageIcon snapshot;

	private int hashCode;

	public JVGClipboardContext(String data, int width, int height) {
		this.data = data;
		hashCode = data.hashCode();
		this.width = width;
		this.height = height;
	}

	public JVGClipboardContext(String file) {
		this.file = file;
		try {
			Reader r = new InputStreamReader(new FileInputStream(file), "UTF-8");
			char[] buf = new char[1024];
			int len;
			StringBuilder s = new StringBuilder();
			while ((len = r.read(buf)) != -1) {
				s.append(buf, 0, len);
			}
			r.close();

			data = s.toString();
			hashCode = data.hashCode();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public String getData() {
		return data;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public ImageIcon getSnapshot() {
		if (snapshot == null) {
			try {
				SnapshotData snapshotData = ru.nest.jvg.editor.Util.getSnapshot(new ByteArrayInputStream(data.getBytes("UTF-8")), SNAPSHOT_WIDTH, SNAPSHOT_HEIGHT, INSETS);
				snapshot = snapshotData.getImage();
				if (width == -1) {
					width = snapshotData.getWidth();
					height = snapshotData.getHeight();
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return snapshot;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		return data.equals(((JVGClipboardContext) o).data);
	}
}
