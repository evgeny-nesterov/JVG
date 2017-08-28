package ru.nest.jvg.action;

import java.awt.event.ActionEvent;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.actionarea.JVGPathGeomActionArea;
import ru.nest.jvg.macros.JVGMacrosCode;
import ru.nest.jvg.shape.JVGPath;

public class EditPathAction extends JVGAction {
	public EditPathAction() {
		super("edit-action");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGPath[] pathes = getPathes(e);
		if (pathes != null && pathes.length > 0) {
			JVGPane pane = getPane(e);
			JVGComponent focus = getComponent(e);
			if (!(focus instanceof JVGPath)) {
				focus = pathes[0];
			}

			boolean isEdit = !isEdited((JVGPath) focus);
			for (JVGPath path : pathes) {
				setEdited(path, isEdit);
			}

			pane.repaint();

			appendMacrosCode(pane, "editPath(id);", JVGMacrosCode.ARG_ID);
		}
	}

	public static boolean isEdited(JVGPath path) {
		int child_count = path.getChildCount();
		JVGComponent[] childs = path.getChildren();
		for (int i = 0; i < child_count; i++) {
			if (childs[i] instanceof JVGPathGeomActionArea) {
				return childs[i].isVisible();
			}
		}
		return false;
	}

	public static void setEdited(JVGPath path, boolean isEdit) {
		setEdited(path, isEdit, true);
	}

	public static void setEdited(JVGPath path, boolean isEdit, boolean activate) {
		int child_count = path.getChildCount();
		JVGComponent[] childs = path.getChildren();
		for (int i = 0; i < child_count; i++) {
			if (childs[i] instanceof JVGPathGeomActionArea) {
				if (isEdit) {
					JVGPathGeomActionArea action = (JVGPathGeomActionArea) childs[i];
					action.setActive(activate);
				}
				childs[i].setVisible(isEdit);
				return;
			}
		}

		if (isEdit) {
			JVGPathGeomActionArea action = new JVGPathGeomActionArea();
			action.setActive(activate);
			path.add(action);
		}
	}
}
