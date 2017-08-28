package ru.nest.jvg.action;

import java.awt.event.ActionEvent;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.macros.JVGMacrosCode;
import ru.nest.jvg.shape.JVGStyledText;
import ru.nest.jvg.shape.text.JVGStyleConstants;

public class FontUnderlineAction extends JVGAction {
	public FontUnderlineAction(int type) {
		super("font-underline");
		this.type = type;
	}

	private int type;

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGComponent c = getComponent(e);
		if (c instanceof JVGStyledText) {
			JVGStyledText text = (JVGStyledText) c;
			MutableAttributeSet attr = text.getInputAttributes();
			int oldType = JVGStyleConstants.getUnderline(attr);
			SimpleAttributeSet sas = new SimpleAttributeSet();
			int underline = oldType == JVGStyleConstants.UNDERLINE_NONE ? type : JVGStyleConstants.UNDERLINE_NONE;
			JVGStyleConstants.setUnderline(sas, underline);
			text.setCurrentCharacterAttributes(sas, false);

			appendMacrosCode(c.getPane(), "setFontUnderline(id, %s);", JVGMacrosCode.ARG_ID, underline);
		}
	}
}
