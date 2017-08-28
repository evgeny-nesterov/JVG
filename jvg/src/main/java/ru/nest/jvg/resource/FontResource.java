package ru.nest.jvg.resource;

import java.awt.Font;

import ru.nest.fonts.Fonts;

public class FontResource extends Resource<Font> {
	public final static String DEFAULT_FAMILY = "Dialog";

	public final static int DEFAULT_FONT_SIZE = 12;

	public final static Font DEFAULT_FONT = Fonts.getFont(DEFAULT_FAMILY, Font.PLAIN, DEFAULT_FONT_SIZE);

	public FontResource(Font font) {
		setResource(font);
	}

	private Font font;

	@Override
	public Font getResource() {
		return font;
	}

	@Override
	public void setResource(Font font) {
		this.font = font;
	}
}
