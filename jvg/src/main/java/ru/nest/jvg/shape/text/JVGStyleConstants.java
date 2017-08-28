package ru.nest.jvg.shape.text;

import java.awt.Color;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.border.Border;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;

import ru.nest.fonts.Fonts;
import ru.nest.jvg.resource.ColorResource;
import ru.nest.jvg.resource.FontResource;
import ru.nest.jvg.resource.Resource;

public class JVGStyleConstants {
	public static final String IconElementName = "icon";

	public final static int BULLETS_NONE = 0;

	public final static int BULLETS_CIRCLE = 1;

	public final static int BULLETS_SQURE = 2;

	public final static int BULLETS_MINUS = 3;

	public final static int BULLETS_NUMBER = 4;

	public final static int BULLETS_LETTER = 5;

	public final static int UNDERLINE_NONE = 0;

	public final static int UNDERLINE_LINE = 1;

	public final static int UNDERLINE_DOUBLE_LINE = 2;

	public final static int UNDERLINE_TRIPLE_LINE = 3;

	public final static int UNDERLINE_ZIGZAG = 4;

	public final static Resource<Font> defaultBulletsFont = new FontResource(Fonts.getFont("Dialog", Font.BOLD, 12));

	public final static int defaultBulletsIndentSize = 40;

	public JVGStyleConstants(String name) {
		this.name = name;
	}

	private String name;

	@Override
	public String toString() {
		return name;
	}

	// --- Paragraph ---
	public static final Object ParagraphBorder = new JVGBorderConstants("ParagraphBorder");

	public static final Object ParagraphBackground = new JVGColorConstants("ParagraphBackground");

	public static final Object ParagraphBullets = new JVGParagraphConstants("ParagraphBullets");

	public static final Object ParagraphBulletsFont = new JVGFontConstants("ParagraphBulletsFont");

	public static final Object ParagraphBulletsColor = new JVGColorConstants("ParagraphBulletsColor");

	public static final Object ParagraphBulletsIndentSize = new JVGParagraphConstants("ParagraphBulletsIndentSize");

	public static final Object ParagraphBulletsIndentCount = new JVGParagraphConstants("ParagraphBulletsIndentCount");

	// --- Characters ---
	public static final Object Foreground = new JVGColorConstants("foreground");

	public static final Object Background = new JVGColorConstants("background");

	public static final Object IconAttribute = new JVGCharacterConstants("icon");

	public static final Object Underline = new JVGCharacterConstants("underline");

	public static final Object UnderlineColor = new JVGColorConstants("underlineColor");

	public static Border getParagraphBorder(AttributeSet attr) {
		return (Border) attr.getAttribute(ParagraphBorder);
	}

	public static void setParagraphBorder(MutableAttributeSet attr, Border b) {
		if (b != null) {
			attr.addAttribute(ParagraphBorder, b);
		} else {
			attr.removeAttribute(ParagraphBorder);
		}
	}

	public static Resource<Color> getParagraphBackground(AttributeSet attr) {
		return (Resource<Color>) attr.getAttribute(ParagraphBackground);
	}

	public static void setParagraphBackground(MutableAttributeSet attr, Resource<Color> c) {
		attr.addAttribute(ParagraphBackground, c);
	}

	public static void setParagraphBulletsFont(MutableAttributeSet attr, Font value) {
		attr.addAttribute(ParagraphBulletsFont, value);
	}

	public static Resource<Font> getParagraphBulletsFont(AttributeSet attr) {
		Resource<Font> value = (Resource<Font>) attr.getAttribute(ParagraphBulletsFont);
		if (value != null) {
			return value;
		} else {
			return defaultBulletsFont;
		}
	}

	public static void setParagraphBulletsColor(MutableAttributeSet attr, Color value) {
		if (value != null) {
			attr.addAttribute(ParagraphBulletsColor, value);
		} else {
			attr.removeAttribute(ParagraphBulletsColor);
		}
	}

	public static Resource<Color> getParagraphBulletsColor(AttributeSet attr) {
		Resource<Color> value = (Resource<Color>) attr.getAttribute(ParagraphBulletsColor);
		if (value != null) {
			return value;
		} else {
			return ColorResource.black;
		}
	}

	public static int getParagraphBullets(AttributeSet attr) {
		Integer value = (Integer) attr.getAttribute(ParagraphBullets);
		if (value != null) {
			return value;
		} else {
			return BULLETS_NONE;
		}
	}

	public static void setParagraphBullets(MutableAttributeSet attr, int value) {
		attr.addAttribute(ParagraphBullets, value);
	}

	public static int getParagraphBulletsIndentSize(AttributeSet attr) {
		Integer value = (Integer) attr.getAttribute(ParagraphBulletsIndentSize);
		if (value != null) {
			return value;
		} else {
			return defaultBulletsIndentSize;
		}
	}

	public static void setParagraphBulletsIndentSize(MutableAttributeSet attr, int value) {
		attr.addAttribute(ParagraphBulletsIndentSize, value);
	}

	public static int getParagraphBulletsIndentCount(AttributeSet attr) {
		Integer value = (Integer) attr.getAttribute(ParagraphBulletsIndentCount);
		if (value != null) {
			return value;
		} else {
			return 0;
		}
	}

	public static void setParagraphBulletsIndentCount(MutableAttributeSet attr, int value) {
		attr.addAttribute(ParagraphBulletsIndentCount, value);
	}

	public static int getUnderline(AttributeSet attr) {
		Integer value = (Integer) attr.getAttribute(Underline);
		if (value != null) {
			return value;
		} else {
			return UNDERLINE_NONE;
		}
	}

	public static void setUnderline(MutableAttributeSet attr, int value) {
		attr.addAttribute(Underline, value);
	}

	public static Resource<Color> getUnderlineColor(AttributeSet attr) {
		Resource<Color> color = (Resource<Color>) attr.getAttribute(UnderlineColor);
		return color;
	}

	public static void setUnderlineColor(MutableAttributeSet attr, Resource<Color> c) {
		attr.addAttribute(UnderlineColor, c);
	}

	public static Resource getForeground(AttributeSet attr) {
		Resource fg = (Resource) attr.getAttribute(Foreground);
		if (fg == null) {
			fg = ColorResource.black;
		}
		return fg;
	}

	public static void setForeground(MutableAttributeSet attr, Resource fg) {
		if (fg != null) {
			attr.addAttribute(Foreground, fg);
		} else {
			attr.removeAttribute(Foreground);
		}
	}

	public static Resource getBackground(AttributeSet attr) {
		Resource bg = (Resource) attr.getAttribute(Background);
		if (bg == null) {
			bg = ColorResource.white;
		}
		return bg;
	}

	public static void setBackground(MutableAttributeSet attr, Resource bg) {
		if (bg != null) {
			attr.addAttribute(Background, bg);
		} else {
			attr.removeAttribute(Background);
		}
	}

	public static Resource<Icon> getIcon(AttributeSet a) {
		return (Resource<Icon>) a.getAttribute(IconAttribute);
	}

	public static void setIcon(MutableAttributeSet a, Resource<Icon> c) {
		a.addAttribute(AbstractDocument.ElementNameAttribute, IconElementName);
		a.addAttribute(IconAttribute, c);
	}

	public static void setFont(MutableAttributeSet attr, Resource<Font> font) {
		if (font != null && font.getResource() != null) {
			StyleConstants.setFontFamily(attr, font.getResource().getFamily());
			StyleConstants.setFontSize(attr, font.getResource().getSize());
			StyleConstants.setBold(attr, font.getResource().isBold());
			StyleConstants.setItalic(attr, font.getResource().isItalic());
		}
	}

	static class JVGBorderConstants extends JVGStyleConstants {
		public JVGBorderConstants(String name) {
			super(name);
		}
	}

	public static class JVGColorConstants extends JVGStyleConstants implements AttributeSet.ColorAttribute, AttributeSet.CharacterAttribute {
		private JVGColorConstants(String name) {
			super(name);
		}
	}

	public static class JVGFontConstants extends JVGStyleConstants implements AttributeSet.ColorAttribute, AttributeSet.CharacterAttribute {
		private JVGFontConstants(String name) {
			super(name);
		}
	}

	public static class JVGCharacterConstants extends JVGStyleConstants implements AttributeSet.CharacterAttribute {
		private JVGCharacterConstants(String name) {
			super(name);
		}
	}

	public static class JVGParagraphConstants extends JVGStyleConstants implements AttributeSet.CharacterAttribute {
		private JVGParagraphConstants(String name) {
			super(name);
		}
	}
}
