package ru.nest.swing.menu;

import java.awt.*;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;

public class GrayFilter extends RGBImageFilter {
	public static Image createShadowImage(Image i) {
		GrayFilter filter = new GrayFilter();
		ImageProducer prod = new FilteredImageSource(i.getSource(), filter);
		Image grayImage = Toolkit.getDefaultToolkit().createImage(prod);
		return grayImage;
	}

	public GrayFilter() {
	}

	@Override
	public int filterRGB(int x, int y, int rgb) {
		int gray = (int) ((0.30 * ((rgb >> 16) & 0xff) + 0.59 * ((rgb >> 8) & 0xff) + 0.11 * (rgb & 0xff)) / 3);

		gray = (255 - ((255 - gray) * (100 - 50) / 100));

		int limit = 200;
		if (gray < limit) {
			return (rgb & 0xff000000) | (140 << 16) | (140 << 8) | (180 << 0);
		} else {
			return 0x00ffffff;
		}
	}
}
