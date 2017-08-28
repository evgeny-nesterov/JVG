package satis.iface.graph.paintobjects;

import java.awt.Graphics;

import satis.iface.graph.Group;

public interface PaintObject {
	public void paint(Group group, Graphics g);

	public void compile(Group group);

	public boolean isPaintOver();

	public boolean isVisible();
}
