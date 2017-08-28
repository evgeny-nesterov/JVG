package satis.toi;

import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.io.File;

import satis.toi.fonts.Fonts;

public class TOIUtil {
	public static double measurePathLength(Shape shape) {
		PathIterator it = new FlatteningPathIterator(shape.getPathIterator(null), 1);
		double points[] = new double[6];
		double moveX = 0, moveY = 0;
		double lastX = 0, lastY = 0;
		double thisX = 0, thisY = 0;
		int type = 0;
		double total = 0;
		while (!it.isDone()) {
			type = it.currentSegment(points);
			switch (type) {
				case PathIterator.SEG_MOVETO:
					moveX = lastX = points[0];
					moveY = lastY = points[1];
					break;

				case PathIterator.SEG_CLOSE:
					points[0] = moveX;
					points[1] = moveY;
					// Fall into....

				case PathIterator.SEG_LINETO:
					thisX = points[0];
					thisY = points[1];
					double dx = thisX - lastX;
					double dy = thisY - lastY;
					total += (float) Math.sqrt(dx * dx + dy * dy);
					lastX = thisX;
					lastY = thisY;
					break;
			}
			it.next();
		}
		return total;
	}

	public static String getColor(Color color) {
		return String.format("#%02X%02X%02X%02X", color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue());
	}

	public static Color getColor(String value) {
		if (value != null && value.startsWith("#")) {
			try {
				value = value.replaceAll("\\s", "");
				int argb = (int) Long.parseLong(value.substring(1), 16);
				return new Color(argb, true);
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
		return null;
	}

	public static String getFont(Font font) {
		String style = "plain";
		if ((font.getStyle() & Font.BOLD) != 0) {
			if ((font.getStyle() & Font.ITALIC) != 0) {
				style = "bold,italic";
			} else {
				style = "bold";
			}
		} else if ((font.getStyle() & Font.ITALIC) != 0) {
			style = "italic";
		}
		return font.getFamily() + ";" + style + ";" + font.getSize();
	}

	public static Font getFont(String value) {
		if (value != null) {
			String[] fontAttr = value.split(";");
			if (fontAttr.length == 3) {
				int style = Font.PLAIN;
				if (fontAttr[1].indexOf("bold") != -1) {
					style |= Font.BOLD;
				}
				if (fontAttr[1].indexOf("italic") != -1) {
					style |= Font.ITALIC;
				}
				return Fonts.getFont(fontAttr[0], style, Integer.parseInt(fontAttr[2]));
			}
		}
		return null;
	}

	public static String getTransform(AffineTransform transform) {
		double[] matrix = new double[6];
		transform.getMatrix(matrix);
		return matrix[0] + " " + matrix[1] + " " + matrix[2] + " " + matrix[3] + " " + matrix[4] + " " + matrix[5];
	}

	public static AffineTransform getTransform(String value) {
		if (value != null) {
			String[] matrix = value.split(" ");
			if (matrix.length == 6) {
				return new AffineTransform(Double.parseDouble(matrix[0]), Double.parseDouble(matrix[1]), Double.parseDouble(matrix[2]), Double.parseDouble(matrix[3]), Double.parseDouble(matrix[4]), Double.parseDouble(matrix[5]));
			}
		}
		return null;
	}

	public static void main(String[] args) {
		System.out.println(String.format("'%04d'", 128));
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
