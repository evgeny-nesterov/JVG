package ru.nest.jvg.editor;

import javax.swing.ImageIcon;

public class Images {
	public static ImageIcon getImage(String name) {
		return new ImageIcon(Images.class.getResource("img/" + name));
	}
}
