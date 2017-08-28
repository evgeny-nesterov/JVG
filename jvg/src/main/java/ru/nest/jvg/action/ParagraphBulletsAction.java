package ru.nest.jvg.action;

import java.awt.event.ActionEvent;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.macros.JVGMacrosCode;
import ru.nest.jvg.shape.JVGStyledText;
import ru.nest.jvg.shape.text.JVGStyleConstants;

public class ParagraphBulletsAction extends JVGAction {
	public ParagraphBulletsAction(int type) {
		super("bullets");
		this.type = type;
	}

	private int type;

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGComponent c = getComponent(e);
		if (c instanceof JVGStyledText) {
			JVGStyledText text = (JVGStyledText) c;
			MutableAttributeSet attr = text.getInputAttributes();
			int bulletsType = JVGStyleConstants.getParagraphBullets(attr);

			attr = new SimpleAttributeSet();
			JVGStyleConstants.setParagraphBullets(attr, bulletsType != type ? type : JVGStyleConstants.BULLETS_NONE);
			text.setCurrentParagraphAttributes(attr, false);

			appendMacrosCode(c.getPane(), "setParagraphBullets(id, %s);", JVGMacrosCode.ARG_ID, type);
		}
	}
}
