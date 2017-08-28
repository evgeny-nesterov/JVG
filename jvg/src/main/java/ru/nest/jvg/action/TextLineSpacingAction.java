package ru.nest.jvg.action;

import java.awt.event.ActionEvent;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.macros.JVGMacrosCode;
import ru.nest.jvg.shape.JVGStyledText;

public class TextLineSpacingAction extends JVGAction {
	public TextLineSpacingAction(String nm, float increase) {
		super(nm);
		this.increase = increase;
	}

	private float increase;

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGComponent c = getComponent(e);
		if (c instanceof JVGStyledText) {
			JVGStyledText text = (JVGStyledText) c;
			MutableAttributeSet attr = text.getInputAttributes();
			float lineSpacing = StyleConstants.getLineSpacing(attr) + increase;

			attr = new SimpleAttributeSet();
			StyleConstants.setLineSpacing(attr, lineSpacing);
			text.setCurrentParagraphAttributes(attr, false);

			System.out.println("SET lineSpacing=" + lineSpacing);

			appendMacrosCode(c.getPane(), "setTextLineSpacing(id, %s);", JVGMacrosCode.ARG_ID, lineSpacing);
		}
	}
}
