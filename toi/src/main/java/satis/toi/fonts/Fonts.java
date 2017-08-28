package satis.toi.fonts;

import java.awt.Font;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import satis.toi.TOIEditor;

public class Fonts {
	private static Map<String, Font> fonts = new HashMap<String, Font>();

	static {
		try {
			registerFont(TOIEditor.class.getResourceAsStream("fonts/pixel_8pt.ttf"));
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static void registerFont(InputStream is) {
		try {
			Font font = Font.createFont(Font.TRUETYPE_FONT, TOIEditor.class.getResourceAsStream("fonts/pixel_8pt.ttf"));
			fonts.put(font.getName(), font);
			System.out.println("create font: " + font.getName());
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static Font getFont(String name) {
		if (fonts.containsKey(name)) {
			return fonts.get(name);
		} else {
			Font font = new Font(name, Font.PLAIN, 12);
			fonts.put(font.getName(), font);
			System.out.println("register font: " + font.getName());
			return font;
		}
	}

	public static Font getPixelFont() {
		return getFont("DPix_8pt");
	}

	public static Font getFont(String name, int style, int size) {
		return getFont(name).deriveFont(style, size);
	}
}
