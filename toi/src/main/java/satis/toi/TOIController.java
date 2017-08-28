package satis.toi;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public interface TOIController {
	public final static int PAINT_BOTTOM = 0;

	public final static int PAINT_TOP = 1;

	public final static int PAINT_UNDER_OBJECT = 2;

	public final static int PAINT_OVER_OBJECT = 3;

	public final static int PAINT_UNDER_SELECTION = 4;

	public final static int PAINT_OVER_SELECTION = 5;

	public void processMouseEvent(MouseEvent e, double x, double y, double adjustX, double adjustY);

	public void processKeyEvent(KeyEvent e);

	public void paint(Graphics2D g, Graphics2D gt, TOIObject o, int type);

	public void install(TOIPane pane);

	public void uninstall();
}
