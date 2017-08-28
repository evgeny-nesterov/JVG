package ru.nest.jvg.editor.editoraction;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import ru.nest.jvg.JVGPane;
import ru.nest.jvg.action.JVGAction;
import ru.nest.jvg.editor.Editor;
import ru.nest.jvg.editor.JVGEditPane;

public abstract class EditorAction extends JVGAction implements Editor {
	public EditorAction(String name) {
		super(name);
	}

	private JVGEditPane editorPane;

	public JVGEditPane getEditorPane() {
		return editorPane;
	}

	public void setEditorPane(JVGEditPane editorPane) {
		this.editorPane = editorPane;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGPane pane = getPane(e);
		if (pane instanceof JVGEditPane) {
			editorPane = (JVGEditPane) pane;
			editorPane.setEditor(this);
		}
	}

	@Override
	public void processMouseEvent(MouseEvent e, double x, double y, double adjustedX, double adjustedY) {
		switch (e.getID()) {
			case MouseEvent.MOUSE_PRESSED:
				mousePressed(e, x, y, adjustedX, adjustedY);
				break;

			case MouseEvent.MOUSE_RELEASED:
				mouseReleased(e, x, y, adjustedX, adjustedY);
				break;

			case MouseEvent.MOUSE_DRAGGED:
				mouseDragged(e, x, y, adjustedX, adjustedY);
				break;

			case MouseEvent.MOUSE_MOVED:
				mouseMoved(e, x, y, adjustedX, adjustedY);
				break;
		}

		if (consumeMouseEvents) {
			e.consume();
		}
	}

	@Override
	public void processKeyEvent(KeyEvent e) {
		if (e.getID() == KeyEvent.KEY_PRESSED) {
			JVGEditPane editorPane = getEditorPane();
			switch (e.getKeyCode()) {
				case KeyEvent.VK_ESCAPE:
					editorPane.setEditor(null);
					break;
			}
		}
	}

	private boolean consumeMouseEvents = false;

	public void consumeMouseEvents(boolean consumeMouseEvents) {
		this.consumeMouseEvents = consumeMouseEvents;
	}

	public void mousePressed(MouseEvent e, double x, double y, double adjustedX, double adjustedY) {
	}

	public void mouseReleased(MouseEvent e, double x, double y, double adjustedX, double adjustedY) {
	}

	public void mouseDragged(MouseEvent e, double x, double y, double adjustedX, double adjustedY) {
	}

	public void mouseMoved(MouseEvent e, double x, double y, double adjustedX, double adjustedY) {
	}

	@Override
	public void paint(Graphics2D g) {
	}

	@Override
	public void start() {
	}

	@Override
	public void finish() {
	}
}
