package ru.nest.toi;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public interface TOIController {
	int PAINT_BOTTOM = 0;

	int PAINT_TOP = 1;

	int PAINT_UNDER_OBJECT = 2;

	int PAINT_OVER_OBJECT = 3;

	int PAINT_UNDER_SELECTION = 4;

	int PAINT_OVER_SELECTION = 5;

	void processMouseEvent(MouseEvent e, double x, double y, double adjustX, double adjustY);

	void processKeyEvent(KeyEvent e);

	void paint(Graphics2D g, Graphics2D gt, TOIObject o, int type);

	void install(TOIPane pane);

	void uninstall();
}
