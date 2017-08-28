package ru.nest.jvg.action;

import java.awt.event.ActionEvent;

import ru.nest.jvg.JVGPane;
import ru.nest.jvg.editor.JVGEditor;

public class PaneZoomAction extends JVGAction {
	private double zoom;

	public PaneZoomAction(String name, double zoom) {
		super(name);
		this.zoom = zoom;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGEditor editor = getEditor(e);
		JVGPane pane = getPane(e);
		if (pane != null && editor != null) {
			editor.getEditorActions().setZoom(100 * pane.getZoom() * zoom);
			pane.repaint();
		}
	}
}
