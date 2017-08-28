package ru.nest.jvg.action;

import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.event.UndoableEditEvent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import ru.nest.awt.strokes.TextStroke;
import ru.nest.fonts.Fonts;
import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.macros.JVGMacrosCode;
import ru.nest.jvg.resource.FontResource;
import ru.nest.jvg.shape.JVGPath;
import ru.nest.jvg.shape.JVGStyledText;
import ru.nest.jvg.shape.JVGTextField;
import ru.nest.jvg.undoredo.PropertyUndoRedo;

public class FontFamilyAction extends JVGAction {
	public FontFamilyAction(String nm, String family) {
		super(nm);
		this.family = family;
	}

	private String family;

	public String getFamily() {
		return family;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGComponent c = getComponent(e);
		if (c instanceof JVGStyledText) {
			JVGStyledText text = (JVGStyledText) c;
			MutableAttributeSet attr = new SimpleAttributeSet();
			StyleConstants.setFontFamily(attr, family);
			text.setCurrentCharacterAttributes(attr, false);

			appendMacrosCode(c.getPane(), "setFontFamily(id, \"%s\");", JVGMacrosCode.ARG_ID, family);
		} else if (c instanceof JVGPath) {
			JVGPath path = (JVGPath) c;
			if (path.getPathStroke() != null && path.getPathStroke().getResource() instanceof TextStroke) {
				SetPathTextStrokeAction action = new SetPathTextStrokeAction(family, SetPathTextStrokeAction.TYPE_FONT_FAMILY);
				action.actionPerformed(e);
			}
		} else if (c instanceof JVGTextField) {
			JVGTextField text = (JVGTextField) c;
			Font oldFont = text.getFont().getResource();
			if (!oldFont.getFamily().equals(family)) {
				FontResource newFont = new FontResource(Fonts.getFont(family, oldFont.getStyle(), oldFont.getSize()));
				text.setFont(newFont);

				JVGPane pane = getPane(e);
				PropertyUndoRedo edit = new PropertyUndoRedo(getName(), pane, text, "setFont", oldFont, newFont);
				pane.fireUndoableEditUpdate(new UndoableEditEvent(pane, edit));
				pane.repaint();
			}

			appendMacrosCode(c.getPane(), "setFontFamily(id, \"%s\");", JVGMacrosCode.ARG_ID, family);
		}
	}
}
