package ru.nest.jvg.editor;

import javax.swing.ImageIcon;

import ru.nest.jvg.JVGPane;

public class SnapshotData {
	private ImageIcon image;

	private int width;

	private int height;

	private JVGPane pane;

	public SnapshotData(ImageIcon image, int width, int height, JVGPane pane) {
		this.image = image;
		this.width = width;
		this.height = height;
		this.pane = pane;
	}

	public ImageIcon getImage() {
		return image;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public JVGPane getPane() {
		return pane;
	}
}
