package ru.nest.swing;

import javax.swing.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

public class ToolSeparator extends JComponent {
	public ToolSeparator(int w, int h) {
		setPreferredSize(new Dimension(w, h));
	}

	@Override
	public void paint(Graphics g) {
		int w = getWidth();
		int h = getHeight() - 4;
		int x = w > 2 ? 1 : 0;
		int y = 2;

		g.setColor(Color.white);
		g.drawLine(x, y, x, h);
		g.setColor(Color.gray);
		g.drawLine(x + 1, y, x + 1, h);
	}
}
