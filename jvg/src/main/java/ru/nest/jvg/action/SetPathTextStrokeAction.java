package ru.nest.jvg.action;

import java.awt.Font;

import ru.nest.fonts.Fonts;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.resource.FontResource;
import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.resource.StrokeResource;
import ru.nest.jvg.undoredo.CompoundUndoRedo;
import ru.nest.strokes.TextStroke;

public class SetPathTextStrokeAction extends SetPathStrokeAction<TextStroke> {
	public final static int TYPE_TEXT = 1;

	public final static int TYPE_FONT_FAMILY = 2;

	public final static int TYPE_FONT_SIZE = 4;

	public final static int TYPE_FONT_BOLD = 8;

	public final static int TYPE_FONT_ITALIC = 16;

	public final static int TYPE_STRETCH = 32;

	public final static int TYPE_REPEAT = 64;

	public final static int TYPE_STROKE = TYPE_TEXT | TYPE_FONT_FAMILY | TYPE_FONT_SIZE | TYPE_FONT_BOLD | TYPE_FONT_ITALIC | TYPE_STRETCH | TYPE_REPEAT;

	public static String getName(int type) {
		String name = "pathstroke-text-stroke";
		switch (type) {
			case TYPE_TEXT:
				name += "-text";
				break;
			case TYPE_FONT_FAMILY:
				name += "-font-family";
				break;
			case TYPE_FONT_SIZE:
				name += "-font-size";
				break;
			case TYPE_FONT_BOLD:
				name += "-font-bold";
				break;
			case TYPE_FONT_ITALIC:
				name += "-font-italic";
				break;
			case TYPE_STRETCH:
				name += "-stretch";
				break;
			case TYPE_REPEAT:
				name += "-repeat";
				break;
		}
		return name;
	}

	public SetPathTextStrokeAction() {
	}

	public SetPathTextStrokeAction(String value, int type) {
		super(getName(type));
		TextStroke stroke = null;
		switch (type) {
			case TYPE_TEXT:
				stroke = new TextStroke(value, FontResource.DEFAULT_FONT);
				break;
			case TYPE_FONT_FAMILY:
				stroke = new TextStroke("", Fonts.getFont(value, Font.PLAIN, 12), true, false);
				break;
		}
		set(stroke, type);
	}

	public SetPathTextStrokeAction(int size) {
		this(TYPE_FONT_SIZE, new TextStroke("", Fonts.getFont("Dialog", Font.PLAIN, size)));
	}

	public SetPathTextStrokeAction(boolean value, int type) {
		super(getName(type));
		TextStroke stroke = null;
		switch (type) {
			case TYPE_FONT_BOLD:
				stroke = new TextStroke("", Fonts.getFont(FontResource.DEFAULT_FAMILY, value ? Font.BOLD : Font.PLAIN, 12), true, false);
				break;
			case TYPE_FONT_ITALIC:
				stroke = new TextStroke("", Fonts.getFont(FontResource.DEFAULT_FAMILY, value ? Font.ITALIC : Font.PLAIN, 12), true, false);
				break;
			case TYPE_STRETCH:
				stroke = new TextStroke("", FontResource.DEFAULT_FONT, value, false);
				break;
			case TYPE_REPEAT:
				stroke = new TextStroke("", FontResource.DEFAULT_FONT, true, value);
				break;
		}
		set(stroke, type);
	}

	public SetPathTextStrokeAction(int type, TextStroke stroke) {
		super(getName(type));
		set(stroke, type);
	}

	@Override
	public Resource<TextStroke> getStroke(JVGPane pane, Resource<TextStroke> oldStrokeResource, Resource<TextStroke> newStrokeResource, String actionName, CompoundUndoRedo edit, int type) {
		TextStroke oldStroke = oldStrokeResource.getResource();
		TextStroke newStroke = newStrokeResource.getResource();

		String text = (oldStroke == null || (type & TYPE_TEXT) != 0) ? newStroke.getText() : oldStroke.getText();
		String fontFamily = (oldStroke == null || (type & TYPE_FONT_FAMILY) != 0) ? newStroke.getFont().getFamily() : oldStroke.getFont().getFamily();
		int fontSize = (oldStroke == null || (type & TYPE_FONT_SIZE) != 0) ? newStroke.getFont().getSize() : oldStroke.getFont().getSize();
		boolean fontBold = (oldStroke == null || (type & TYPE_FONT_BOLD) != 0) ? newStroke.getFont().isBold() : oldStroke.getFont().isBold();
		boolean fontItalic = (oldStroke == null || (type & TYPE_FONT_ITALIC) != 0) ? newStroke.getFont().isItalic() : oldStroke.getFont().isItalic();
		boolean stretch = (oldStroke == null || (type & TYPE_STRETCH) != 0) ? newStroke.isStretchToFit() : oldStroke.isStretchToFit();
		boolean repeat = (oldStroke == null || (type & TYPE_STRETCH) != 0) ? newStroke.isRepeat() : oldStroke.isRepeat();

		int fontStyle = Font.PLAIN;
		if (fontBold) {
			fontStyle |= Font.BOLD;
		}
		if (fontItalic) {
			fontStyle |= Font.ITALIC;
		}
		Font font = Fonts.getFont(fontFamily, fontStyle, fontSize);

		TextStroke stroke = new TextStroke(text, font, stretch, repeat);
		boolean changed = oldStroke == null;
		changed |= oldStroke != null && !oldStroke.getText().equals(text);
		changed |= oldStroke != null && !oldStroke.getFont().equals(font);
		changed |= oldStroke != null && oldStroke.isStretchToFit() != stretch;
		changed |= oldStroke != null && oldStroke.isRepeat() != repeat;

		if (changed) {
			return new StrokeResource<TextStroke>(stroke);
		} else {
			return null;
		}
	}
}
