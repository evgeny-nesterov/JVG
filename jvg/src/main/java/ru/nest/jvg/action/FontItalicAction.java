package ru.nest.jvg.action;

import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.event.UndoableEditEvent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import ru.nest.awt.strokes.TextStroke;
import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.macros.JVGMacrosCode;
import ru.nest.jvg.resource.FontResource;
import ru.nest.jvg.shape.JVGPath;
import ru.nest.jvg.shape.JVGStyledText;
import ru.nest.jvg.shape.JVGTextField;
import ru.nest.jvg.undoredo.PropertyUndoRedo;

public class FontItalicAction extends JVGAction {
	public FontItalicAction() {
		super("font-italic");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGComponent c = getComponent(e);
		if (c instanceof JVGStyledText) {
			JVGStyledText text = (JVGStyledText) c;
			MutableAttributeSet attr = text.getInputAttributes();
			boolean italic = (StyleConstants.isItalic(attr)) ? false : true;
			SimpleAttributeSet sas = new SimpleAttributeSet();
			StyleConstants.setItalic(sas, italic);
			text.setCurrentCharacterAttributes(sas, false);

			appendMacrosCode(c.getPane(), "setFontItalic(id, %s);", JVGMacrosCode.ARG_ID, italic);
		} else if (c instanceof JVGPath) {
			JVGPath path = (JVGPath) c;
			if (path.getPathStroke() != null && path.getPathStroke().getResource() instanceof TextStroke) {
				TextStroke stroke = (TextStroke) path.getPathStroke().getResource();
				boolean italic = !stroke.getFont().isItalic();
				SetPathTextStrokeAction action = new SetPathTextStrokeAction(italic, SetPathTextStrokeAction.TYPE_FONT_ITALIC);
				action.actionPerformed(e);
			}
		} else if (c instanceof JVGTextField) {
			JVGTextField text = (JVGTextField) c;
			Font oldFont = text.getFont().getResource();

			FontResource newFont;
			if (oldFont.isItalic()) {
				newFont = new FontResource(oldFont.deriveFont(oldFont.getStyle() - Font.ITALIC));
			} else {
				newFont = new FontResource(oldFont.deriveFont(oldFont.getStyle() | Font.ITALIC));
			}
			text.setFont(newFont);

			JVGPane pane = getPane(e);
			PropertyUndoRedo edit = new PropertyUndoRedo(getName(), pane, text, "setFont", oldFont, newFont);
			pane.fireUndoableEditUpdate(new UndoableEditEvent(pane, edit));
			pane.repaint();

			appendMacrosCode(text.getPane(), "setFontItalic(id, %s);", JVGMacrosCode.ARG_ID, newFont.getResource().isBold());
		}
	}
}
