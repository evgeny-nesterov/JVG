package ru.nest.jvg.action;

import java.awt.event.ActionEvent;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.macros.JVGMacrosCode;
import ru.nest.jvg.shape.JVGStyledText;

public class FontStrikeAction extends JVGAction {
	public FontStrikeAction() {
		super("font-strike");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGComponent c = getComponent(e);
		if (c instanceof JVGStyledText) {
			JVGStyledText text = (JVGStyledText) c;
			MutableAttributeSet attr = text.getInputAttributes();
			boolean strike = (StyleConstants.isStrikeThrough(attr)) ? false : true;
			SimpleAttributeSet sas = new SimpleAttributeSet();
			StyleConstants.setStrikeThrough(sas, strike);
			text.setCurrentCharacterAttributes(sas, false);

			appendMacrosCode(c.getPane(), "setFontStrike(id, %s);", JVGMacrosCode.ARG_ID, strike);
		}
	}
}
