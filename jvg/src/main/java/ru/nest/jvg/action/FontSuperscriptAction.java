package ru.nest.jvg.action;

import java.awt.event.ActionEvent;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.macros.JVGMacrosCode;
import ru.nest.jvg.shape.JVGStyledText;

public class FontSuperscriptAction extends JVGAction {
	public FontSuperscriptAction() {
		super("font-superscript");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGComponent c = getComponent(e);
		if (c instanceof JVGStyledText) {
			JVGStyledText text = (JVGStyledText) c;
			MutableAttributeSet attr = text.getInputAttributes();
			boolean superscript = (StyleConstants.isSuperscript(attr)) ? false : true;
			SimpleAttributeSet sas = new SimpleAttributeSet();
			StyleConstants.setSuperscript(sas, superscript);
			text.setCurrentCharacterAttributes(sas, false);

			appendMacrosCode(c.getPane(), "setFontSuperscript(id, %s);", JVGMacrosCode.ARG_ID, superscript);
		}
	}
}
