package ru.nest.toi;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

public interface TOIObjectControl {
	boolean contains(TOIPane pane, double x, double y);

	void processMouseEvent(MouseEvent e, double x, double y, double adjustX, double adjustY);

	void paint(Graphics2D g, Graphics2D gt, TOIPaintContext ctx);

	Cursor getCursor();
}
