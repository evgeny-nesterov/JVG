package ru.nest.jvg.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.JVGSelectionModel;
import ru.nest.jvg.JVGUtil;
import ru.nest.jvg.editor.JVGEditPane;
import ru.nest.jvg.editor.JVGEditor;
import ru.nest.jvg.macros.JVGMacrosCode;
import ru.nest.jvg.shape.JVGPath;
import ru.nest.jvg.shape.JVGShape;

public abstract class JVGAction extends AbstractAction {
	public JVGAction(String name) {
		super(name);
		this.name = name;
	}

	private String name;

	public String getName() {
		return name;
	}

	public void doAction() {
		actionPerformed(null);
	}

	protected final static JVGPane getPane(ActionEvent e) {
		if (e != null) {
			Object o = e.getSource();
			if (o instanceof JVGPane) {
				return (JVGPane) o;
			}
		}

		return getFocusedPane();
	}

	protected final static JVGPane getFocusedPane() {
		return JVGPane.getFocusedPane();
	}

	protected final static JVGComponent getComponent(ActionEvent e) {
		JVGPane pane = getPane(e);
		JVGComponent focus = null;
		if (pane != null) {
			focus = pane.getFocusOwner();
			if (focus == null) {
				JVGSelectionModel selectionModel = pane.getSelectionManager();
				if (selectionModel != null && selectionModel.getSelectionCount() == 1) {
					focus = selectionModel.getSelection()[0];
				}
			}
		}
		return focus;
	}

	protected final JVGComponent getFocusedComponent() {
		JVGPane pane = getFocusedPane();
		if (pane != null) {
			return pane.getFocusOwner();
		} else {
			return null;
		}
	}

	public JVGSelectionModel getSelectionManager(ActionEvent e) {
		JVGPane pane = getPane(e);
		if (pane != null) {
			return pane.getSelectionManager();
		} else {
			return null;
		}
	}

	public JVGShape[] getShapes(ActionEvent e) {
		JVGPane pane = getPane(e);
		JVGShape[] shapes = getShapes(pane);
		if (pane != null && shapes == null) {
			JVGComponent component = getComponent(e);
			if (component instanceof JVGShape) {
				shapes = new JVGShape[] { (JVGShape) component };
			}
		}
		return shapes;
	}

	public JVGShape[] getShapes(JVGPane pane) {
		JVGShape[] shapes = null;
		if (pane != null) {
			JVGSelectionModel selectionModel = pane.getSelectionManager();
			if (selectionModel != null && selectionModel.getSelectionCount() > 0) {
				shapes = JVGUtil.getComponents(JVGShape.class, selectionModel.getSelection());
			}
		}
		return shapes;
	}

	public JVGComponent[] getComponents(ActionEvent e) {
		JVGPane pane = getPane(e);
		if (pane != null) {
			JVGSelectionModel selectionModel = pane.getSelectionManager();
			if (selectionModel != null && selectionModel.getSelectionCount() > 0) {
				return selectionModel.getSelection();
			}
		}

		JVGComponent component = getComponent(e);
		if (component != null) {
			return new JVGComponent[] { component };
		}
		return null;
	}

	@Override
	public String toString() {
		return (String) getValue(Action.NAME);
	}

	public JVGPath[] getPathes(ActionEvent e) {
		JVGPath[] pathes = null;
		JVGPane pane = getPane(e);
		if (pane != null) {
			JVGSelectionModel selectionModel = pane.getSelectionManager();
			if (selectionModel != null && selectionModel.getSelectionCount() > 0) {
				pathes = JVGUtil.getComponents(JVGPath.class, selectionModel.getSelection());
			}
		}
		return pathes;
	}

	public void appendMacrosCode(JVGPane pane, String format, int argType, Object... args) {
		if (pane != null && format != null) {
			if (pane.isMacrosActive()) {
				JVGMacrosCode code = new JVGMacrosCode(argType, format + "\n", args);
				pane.appendMacrosCode(code);
			}
		}
	}

	public JVGEditor getEditor(ActionEvent e) {
		JVGPane pane = getPane(e);
		if (pane instanceof JVGEditPane) {
			JVGEditPane ep = (JVGEditPane) pane;
			return ep.getEditor();
		}
		return null;
	}
}
