package ru.nest.jvg.action;

import java.awt.event.ActionEvent;

import javax.swing.event.UndoableEditEvent;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.macros.JVGMacrosCode;
import ru.nest.jvg.shape.JVGStyledText;
import ru.nest.jvg.undoredo.PropertyUndoRedo;

public class TextWrapAction extends JVGAction {
	public TextWrapAction() {
		super("text-wrap");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGComponent c = getComponent(e);
		if (c instanceof JVGStyledText) {
			JVGStyledText text = (JVGStyledText) c;
			boolean isWrap = text.isWrap();
			text.setWrap(!isWrap);

			JVGPane pane = text.getPane();
			pane.fireUndoableEditUpdate(new UndoableEditEvent(pane, new PropertyUndoRedo(getName(), pane, text, "setWrap", isWrap, !isWrap)));

			appendMacrosCode(pane, "setTextWrap(id, %s);", JVGMacrosCode.ARG_ID, isWrap);
		}
	}
}
