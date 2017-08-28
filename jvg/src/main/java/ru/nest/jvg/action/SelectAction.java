package ru.nest.jvg.action;

import java.awt.event.ActionEvent;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGEditorKit;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.JVGSelectionModel;
import ru.nest.jvg.macros.JVGMacrosCode;

public class SelectAction extends JVGAction {
	public final static int SELECT_FOCUSED = 0;

	public final static int SELECT_ALL = 1;

	public final static int UNSELECT_ALL = 2;

	public SelectAction(int type) {
		super(JVGEditorKit.selectionActions[type]);
		this.type = type;
	}

	private int type;

	@Override
	public void actionPerformed(ActionEvent e) {
		switch (type) {
			case SELECT_FOCUSED:
				JVGComponent o = getComponent(e);
				if (o != null) {
					o.setSelected(!o.isSelected(), false);
					appendMacrosCode(o.getPane(), "selectFocused();", JVGMacrosCode.ARG_NONE);
				}
				break;

			case SELECT_ALL:
				JVGPane pane = getPane(e);
				if (pane != null) {
					boolean selectionChanged = pane.selectAll();
					if (selectionChanged) {
						pane.repaint();
					}
					appendMacrosCode(pane, "selectAll();", JVGMacrosCode.ARG_NONE);
				}
				break;

			case UNSELECT_ALL:
				pane = getPane(e);
				if (pane != null) {
					JVGSelectionModel selectionModel = pane.getSelectionManager();
					if (selectionModel != null && selectionModel.getSelectionCount() > 0) {
						selectionModel.clearSelection();
						pane.repaint();

						appendMacrosCode(pane, "clearSelection();", JVGMacrosCode.ARG_NONE);
					}
				}
				break;
		}
	}
}
