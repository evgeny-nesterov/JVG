package javax.swing;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class Util {
	private final static Color transparentColor = new Color(0, 0, 0, 0);

	public static Image createDisabledImage(Icon src) {
		BufferedImage dst = new BufferedImage(src.getIconWidth(), src.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		src.paintIcon(null, dst.getGraphics(), 0, 0);
		for (int i = 0; i < dst.getWidth(); i++) {
			for (int j = 0; j < dst.getHeight(); j++) {
				int color = dst.getRGB(i, j);
				if (((color >> 24) & 0xff) != 0) {
					int r = (color >> 16) & 0xff;
					int g = (color >> 8) & 0xff;
					int b = color & 0xff;
					int c = (r + g + b) / 3;
					if (c > 192) {
						c = 192 + (c - 192) / 2;
					} else {
						c = 192 + (c - 192) / 4;
					}
					color = 0xff000000 + (c << 16) + (c << 8) + c;
					dst.setRGB(i, j, color);
				} else {
					dst.setRGB(i, j, 0x00000000);
				}
			}
		}

		return dst;
	}

	public static String getFileExtention(String fileName) {
		if (fileName != null) {
			int index = fileName.lastIndexOf('.');
			if (index == -1) {
				return "";
			}

			return fileName.substring(index + 1).toLowerCase();
		} else {
			return "";
		}
	}
}
