package ru.nest.jvg.action;

import java.awt.event.ActionEvent;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.macros.JVGMacrosCode;
import ru.nest.jvg.shape.JVGStyledText;

public class TextAlignmentAction extends JVGAction {
	public TextAlignmentAction(String nm, int alignment) {
		super(nm);
		this.alignment = alignment;
	}

	private int alignment;

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGComponent c = getComponent(e);
		if (c instanceof JVGStyledText) {
			JVGStyledText text = (JVGStyledText) c;
			MutableAttributeSet attr = new SimpleAttributeSet();
			StyleConstants.setAlignment(attr, alignment);
			text.setCurrentParagraphAttributes(attr, false);
			appendMacrosCode(c.getPane(), "setTextAlignment(id, %s);", JVGMacrosCode.ARG_ID, alignment);
		}
	}
}
