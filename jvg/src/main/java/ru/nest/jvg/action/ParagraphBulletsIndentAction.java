package ru.nest.jvg.action;

import java.awt.event.ActionEvent;

import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.macros.JVGMacrosCode;
import ru.nest.jvg.shape.JVGStyledText;
import ru.nest.jvg.shape.text.JVGStyleConstants;

public class ParagraphBulletsIndentAction extends JVGAction {
	public final static int LEFT = 0;

	public final static int RIGHT = 1;

	public ParagraphBulletsIndentAction(int direction) {
		super("bullets-indent");
		this.direction = direction;
	}

	private int direction;

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGComponent c = getComponent(e);
		if (c instanceof JVGStyledText) {
			JVGStyledText text = (JVGStyledText) c;
			StyledDocument doc = text.getDocument();

			int indent = 0;
			for (Element paragraph : text.getSelectedParagraphs()) {
				AttributeSet attr = paragraph.getAttributes();

				indent = JVGStyleConstants.getParagraphBulletsIndentCount(attr);
				indent += direction == LEFT ? -1 : 1;
				if (indent < 0) {
					indent = 0;
				}

				MutableAttributeSet newattr = new SimpleAttributeSet();
				JVGStyleConstants.setParagraphBulletsIndentCount(newattr, indent);
				doc.setParagraphAttributes(paragraph.getStartOffset(), paragraph.getEndOffset() - paragraph.getStartOffset(), newattr, false);
			}

			appendMacrosCode(c.getPane(), "setParagraphBulletsIndent(id, %s);", JVGMacrosCode.ARG_ID, indent);
		}
	}
}
