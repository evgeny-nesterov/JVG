package javax.swing.dock.grid;

import java.awt.Component;
import java.awt.Rectangle;

public class GridDockConstraints {
	public enum GridDockPosition {
		BORDER_TOP, BORDER_LEFT, BORDER_BOTTOM, BORDER_RIGHT, HOR_LEFT, HOR_CENTER, HOR_RIGHT, VER_TOP, VER_CENTER, VER_BOTTOM, FIRST_INSERT
	}

	public int x;

	public int y;

	public int w;

	public int h;

	public GridDockPosition type;

	public Component comp;

	public Rectangle rect = new Rectangle();

	public GridDockConstraints(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}
}
