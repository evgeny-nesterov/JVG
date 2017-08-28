package satis.iface.graph.backgrounds;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import satis.iface.graph.Painter;

public class ImagePainter implements Painter {
	private Image image = null;

	public ImagePainter(Image image) {
		this.image = image;
	}

	@Override
	public void paint(Graphics g, int width, int height) {
		if (image != null) {
			int w = image.getWidth(null);
			int h = image.getHeight(null);

			if (w > 0 && h > 0) {
				for (int i = 0; i < width / w + 1; i++) {
					for (int j = 0; j < height / h + 1; j++) {
						g.drawImage(image, i * w, j * h, Color.white, null);
					}
				}
			}
		}
	}
}
