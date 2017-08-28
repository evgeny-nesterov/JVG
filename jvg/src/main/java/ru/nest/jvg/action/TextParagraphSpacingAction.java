package ru.nest.jvg.action;

import java.awt.event.ActionEvent;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.macros.JVGMacrosCode;
import ru.nest.jvg.shape.JVGStyledText;

public class TextParagraphSpacingAction extends JVGAction {
	public final static int ABSOLUTE_VALUES = 0;

	public final static int INCREASE = 1;

	public TextParagraphSpacingAction(String nm, int type, float top, float left, float bottom, float right) {
		super(nm);
		this.type = type;
		this.top = top;
		this.left = left;
		this.bottom = bottom;
		this.right = right;
	}

	private int type;

	private float top;

	private float left;

	private float bottom;

	private float right;

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGComponent c = getComponent(e);
		if (c instanceof JVGStyledText) {
			JVGStyledText text = (JVGStyledText) c;
			MutableAttributeSet attr = text.getInputAttributes();
			float spaceAbove = top;
			float spaceLeft = left;
			float spaceBelow = bottom;
			float spaceRight = right;
			if (type == INCREASE) {
				spaceAbove += StyleConstants.getSpaceAbove(attr);
				spaceLeft += StyleConstants.getLeftIndent(attr);
				if (spaceLeft < 0) {
					spaceLeft = 0;
				}
				spaceBelow += StyleConstants.getSpaceBelow(attr);
				spaceRight += StyleConstants.getRightIndent(attr);
			}

			attr = new SimpleAttributeSet();
			StyleConstants.setSpaceAbove(attr, spaceAbove);
			StyleConstants.setLeftIndent(attr, spaceLeft);
			StyleConstants.setSpaceBelow(attr, spaceBelow);
			StyleConstants.setRightIndent(attr, spaceRight);
			text.setCurrentParagraphAttributes(attr, false);

			appendMacrosCode(c.getPane(), "setParagraphInsets(id, %s, %s, %s, %s);", JVGMacrosCode.ARG_ID, spaceAbove, spaceLeft, spaceBelow, spaceRight);
		}
	}
}
