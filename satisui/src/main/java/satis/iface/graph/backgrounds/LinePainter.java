package satis.iface.graph.backgrounds;

import java.awt.Color;
import java.awt.Graphics;

import satis.iface.graph.Painter;

public class LinePainter implements Painter {
	private Color color = Color.lightGray;

	public LinePainter(Color color) {
		this.color = color;
	}

	@Override
	public void paint(Graphics g, int width, int height) {
		g.setColor(color);
		for (int i = 0; i < height; i += 2) {
			g.drawLine(0, i, width, i);
		}
	}
}
