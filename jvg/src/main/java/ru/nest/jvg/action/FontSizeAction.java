package ru.nest.jvg.action;

import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.event.UndoableEditEvent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.macros.JVGMacrosCode;
import ru.nest.jvg.resource.FontResource;
import ru.nest.jvg.shape.JVGPath;
import ru.nest.jvg.shape.JVGStyledText;
import ru.nest.jvg.shape.JVGTextField;
import ru.nest.jvg.undoredo.PropertyUndoRedo;
import ru.nest.strokes.TextStroke;

public class FontSizeAction extends JVGAction {
	public FontSizeAction(String nm, int size) {
		super(nm);
		this.size = size;
	}

	private int size;

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGComponent c = getComponent(e);
		if (c instanceof JVGStyledText) {
			JVGStyledText text = (JVGStyledText) c;
			MutableAttributeSet attr = new SimpleAttributeSet();
			StyleConstants.setFontSize(attr, size);
			text.setCurrentCharacterAttributes(attr, false);

			appendMacrosCode(c.getPane(), "setFontSize(id, %s);", JVGMacrosCode.ARG_ID, size);
		} else if (c instanceof JVGPath) {
			JVGPath path = (JVGPath) c;
			if (path.getPathStroke() != null && path.getPathStroke().getResource() instanceof TextStroke) {
				SetPathTextStrokeAction action = new SetPathTextStrokeAction(size);
				action.actionPerformed(e);
			}
		} else if (c instanceof JVGTextField) {
			JVGTextField text = (JVGTextField) c;
			Font oldFont = text.getFont().getResource();
			if (size != oldFont.getSize()) {
				FontResource newFont = new FontResource(oldFont.deriveFont((float) size));
				text.setFont(newFont);

				JVGPane pane = getPane(e);
				PropertyUndoRedo edit = new PropertyUndoRedo(getName(), pane, text, "setFont", oldFont, newFont);
				pane.fireUndoableEditUpdate(new UndoableEditEvent(pane, edit));
				pane.repaint();
			}

			appendMacrosCode(c.getPane(), "setFontSize(id, %s);", JVGMacrosCode.ARG_ID, size);
		}
	}

	public int getSize() {
		return size;
	}
}
