package ru.nest.jvg.editor;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public interface Editor {
	public void processMouseEvent(MouseEvent e, double x, double y, double adjustedX, double adjustedY);

	public void processKeyEvent(KeyEvent e);

	public void paint(Graphics2D g);

	public void start();

	public void finish();

	public boolean isCustomActionsManager();
}
