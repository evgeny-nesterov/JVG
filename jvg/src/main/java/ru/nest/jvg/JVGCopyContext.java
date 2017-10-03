package ru.nest.jvg;

import java.io.File;
import java.io.Serializable;
import java.net.URL;

public class JVGCopyContext implements Serializable {
	public JVGCopyContext(String xml, int width, int height) {
		this.xml = xml;
		this.width = width;
		this.height = height;
		time = System.currentTimeMillis();
	}

	public JVGCopyContext(File file) {
		this.file = file;
		time = System.currentTimeMillis();
	}

	public JVGCopyContext(URL complexUrl) {
		this.complexUrl = complexUrl;
		this.width = -1;
		this.height = -1;
		time = System.currentTimeMillis();
	}

	private File file;

	private URL complexUrl;

	private String xml;

	private int width, height;

	public File getFile() {
		return file;
	}

	public URL getComplexURL() {
		return complexUrl;
	}

	public String getData() {
		return xml;
	}

	private long time;

	public long getTime() {
		return time;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}
