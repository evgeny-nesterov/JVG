package ru.nest.jvg.resource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class ImageResource<V extends Icon> extends Resource<V> {
	public ImageResource(String name, String path) throws Exception {
		super(name);
		try {
			setResource(new URL(path));
		} catch (Exception exc) {
			setResource(new File(path));
		}
	}

	public ImageResource(URL url) throws Exception {
		setResource(url);
	}

	public ImageResource(File file) throws Exception {
		this(file.toURI().toURL());
	}

	public ImageResource(byte[] data) {
		this.url = null;
		setResource(data);
	}

	public ImageResource(byte[] data, String descr) {
		this.url = null;
		setResource(data, descr);
	}

	private V image;

	@Override
	public V getResource() {
		return image;
	}

	@Override
	public void setResource(V image) {
		this.image = image;
	}

	public void setResource(File file) throws Exception {
		setResource(file.toURI().toURL());
	}

	public void setResource(URL url) throws Exception {
		InputStream is = url.openStream();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		int c;
		while ((c = is.read()) != -1) {
			os.write((char) c);
		}

		byte[] data = os.toByteArray();
		setResource(data);
		this.url = url.toExternalForm();
	}

	public void setResource(byte[] data) {
		setResource(data, "");
	}

	public void setResource(byte[] data, String descr) {
		ImageIcon icon = new ImageIcon(data, descr);
		setResource((V) icon);
		this.data = data;
	}

	private byte[] data;

	public byte[] getData() {
		return data;
	}

	private String url;

	public String getSource() {
		return url;
	}
}
