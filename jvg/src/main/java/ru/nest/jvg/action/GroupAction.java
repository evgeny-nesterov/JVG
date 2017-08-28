package ru.nest.jvg.action;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.event.UndoableEditEvent;

import ru.nest.jvg.ComponentOrderComparator;
import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGContainer;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.JVGSelectionModel;
import ru.nest.jvg.JVGUtil;
import ru.nest.jvg.actionarea.JVGActionArea;
import ru.nest.jvg.macros.JVGMacrosCode;
import ru.nest.jvg.shape.JVGGroup;
import ru.nest.jvg.undoredo.AddUndoRedo;
import ru.nest.jvg.undoredo.CompoundUndoRedo;
import ru.nest.jvg.undoredo.RemoveUndoRedo;

/**
 * Group selected shapes and ungroup shape represented by JVGGroup object
 */
public class GroupAction extends JVGAction {
	public GroupAction() {
		super("group / ungroup");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGPane pane = getPane(e);
		if (pane != null) {
			JVGSelectionModel selectionModel = getSelectionManager(e);
			if (selectionModel != null) {
				if (selectionModel.getSelectionCount() == 1) {
					// ungroup
					JVGComponent[] selection = selectionModel.getSelection();
					JVGComponent c = selection[0];
					if (c instanceof JVGGroup) {
						JVGGroup g = (JVGGroup) c;
						ungroup(pane, g);
						return;
					}

					appendMacrosCode(c.getPane(), "ungroup(id);", JVGMacrosCode.ARG_ID);
				} else if (selectionModel.getSelectionCount() > 1) {
					// group
					JVGComponent[] selection = selectionModel.getSelection();
					JVGComponent[] roots = JVGUtil.getRoots(selection);

					selectionModel.clearSelection();
					if (pane.getFocusOwner() != null) {
						pane.getFocusOwner().setFocused(false);
					}

					group(pane, roots);
					appendMacrosCode(pane, "group(ids);", JVGMacrosCode.ARG_IDS);
				}
			}
		}
	}

	public static JVGComponent[] ungroup(JVGPane pane, JVGGroup g) {
		JVGContainer p = g.getParent();
		if (p != null) {
			CompoundUndoRedo edit = new CompoundUndoRedo("ungroup", pane);

			edit.add(new RemoveUndoRedo(pane, g, p, p.getChildIndex(g)));
			p.remove(g);

			pane.getSelectionManager().clearSelection();

			int childs_count = g.getChildCount();
			JVGComponent[] childs = g.getChildren().clone();
			for (int i = 0; i < childs_count; i++) {
				JVGComponent child = childs[i];
				if (!(child instanceof JVGActionArea)) {
					int oldIndex = child.getParent() != null ? child.getParent().getChildIndex(child) : -1;
					edit.add(new AddUndoRedo(pane, child, child.getParent(), oldIndex, p, -1));
					p.add(child);
					child.setSelected(true, false);
				}
			}

			pane.fireUndoableEditUpdate(new UndoableEditEvent(pane, edit));
			pane.repaint();

			return childs;
		}

		return null;
	}

	private static Comparator<JVGComponent> orderComparator = new ComponentOrderComparator();

	public static JVGGroup group(JVGPane pane, JVGComponent[] components) {
		Arrays.sort(components, orderComparator);

		CompoundUndoRedo edit = new CompoundUndoRedo("group", pane);

		JVGComponent focusOwner = pane.getFocusOwner();
		JVGContainer parent = null;
		if (focusOwner != null) {
			parent = focusOwner.getParent();
		}

		JVGGroup g = pane.getEditorKit().getFactory().createComponent(JVGGroup.class, (Object[]) null);
		for (int i = 0; i < components.length; i++) {
			JVGComponent c = components[i];
			if (!(c instanceof JVGActionArea)) {
				if (parent == null || parent == c) {
					parent = c.getParent();
				}

				int oldIndex = c.getParent() != null ? c.getParent().getChildIndex(c) : -1;
				edit.add(new AddUndoRedo(pane, c, c.getParent(), oldIndex, g, -1));
				g.add(c);
			}
		}

		for (int i = g.getChildCount() - 1; i >= 0; i--) {
			JVGComponent child = g.getChild(i);
			if (child instanceof JVGGroup) {
				JVGGroup child_group = (JVGGroup) child;
				int count = 0;
				for (int j = 0; j < child_group.getChildCount(); j++) {
					if (!(child_group.getChild(j) instanceof JVGActionArea)) {
						count++;
					}
				}

				if (count == 0) {
					edit.add(new RemoveUndoRedo(pane, child_group, g, g.getChildIndex(child_group)));
					g.remove(child_group);
				}
			}
		}

		if (parent != null) {
			int oldIndex = g.getParent() != null ? g.getParent().getChildIndex(g) : -1;
			edit.add(new AddUndoRedo(pane, g, g.getParent(), oldIndex, parent, -1));
			parent.add(g);

			g.setSelected(true, true);
			g.setFocused(true);
		}

		pane.fireUndoableEditUpdate(new UndoableEditEvent(pane, edit));
		pane.repaint();
		return g;
	}
}
