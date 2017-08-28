package ru.nest.jvg.action;

import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.macros.JVGMacrosCode;
import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.shape.JVGStyledText;
import ru.nest.jvg.shape.text.JVGStyleConstants;

public class FontAction extends JVGAction {
	public FontAction(String nm, Resource<Font> font) {
		super(nm);
		this.font = font;
	}

	private Resource<Font> font;

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGComponent c = getComponent(e);
		if (c instanceof JVGStyledText) {
			JVGStyledText text = (JVGStyledText) c;
			MutableAttributeSet attr = new SimpleAttributeSet();
			JVGStyleConstants.setFont(attr, font);
			text.setCurrentCharacterAttributes(attr, false);

			appendMacrosCode(c.getPane(), "setFont(id, \"%s\", %s, %s);", JVGMacrosCode.ARG_ID, font.getResource().getFontName(), font.getResource().getSize(), font.getResource().getStyle());
		}
	}
}
