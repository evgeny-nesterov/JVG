package ru.nest.jvg.action;

import java.awt.event.ActionEvent;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGEditorKit;
import ru.nest.jvg.macros.JVGMacrosCode;

public class TravelsalAction extends JVGAction {
	public final static int NEXT = 0;

	public final static int PREV = 1;

	public TravelsalAction(int type) {
		super(JVGEditorKit.traversalActions[type]);
		this.type = type;
	}

	private int type;

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGComponent o = getComponent(e);
		if (o != null) {
			if (type == NEXT) {
				o = o.requestNextFocus();
			} else {
				o = o.requestPrevFocus();
			}

			if (o != null) {
				o.setSelected(true, true);
			}

			if (type == NEXT) {
				appendMacrosCode(o.getPane(), "nextFocus();", JVGMacrosCode.ARG_NONE);
			} else {
				appendMacrosCode(o.getPane(), "prevFocus();", JVGMacrosCode.ARG_NONE);
			}
		}
	}
}
