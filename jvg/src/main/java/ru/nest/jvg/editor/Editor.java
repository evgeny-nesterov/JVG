package ru.nest.jvg.editor;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public interface Editor {
	void processMouseEvent(MouseEvent e, double x, double y, double adjustedX, double adjustedY);

	void processKeyEvent(KeyEvent e);

	void paint(Graphics2D g);

	void start();

	void finish();

	boolean isCustomActionsManager();
}
