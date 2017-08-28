package satis.iface.graph.paintobjects;

import java.awt.Graphics;
import java.awt.event.MouseEvent;

import satis.iface.graph.Group;

public interface ActivePaintObject extends PaintObject {
	public boolean contains(int x, int y);

	public void processMouseEvent(Group group, MouseEvent e);

	public void paintActive(Group group, Graphics g);
}
