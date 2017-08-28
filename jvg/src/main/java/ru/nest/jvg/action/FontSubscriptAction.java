package ru.nest.jvg.action;

import java.awt.event.ActionEvent;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.macros.JVGMacrosCode;
import ru.nest.jvg.shape.JVGStyledText;

public class FontSubscriptAction extends JVGAction {
	public FontSubscriptAction() {
		super("font-subscript");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGComponent c = getComponent(e);
		if (c instanceof JVGStyledText) {
			JVGStyledText text = (JVGStyledText) c;
			MutableAttributeSet attr = text.getInputAttributes();
			boolean subscript = (StyleConstants.isSubscript(attr)) ? false : true;
			SimpleAttributeSet sas = new SimpleAttributeSet();
			StyleConstants.setSubscript(sas, subscript);
			text.setCurrentCharacterAttributes(sas, false);

			appendMacrosCode(c.getPane(), "setFontSubscript(id, %s);", JVGMacrosCode.ARG_ID, subscript);
		}
	}
}
