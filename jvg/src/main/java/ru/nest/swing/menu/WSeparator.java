package ru.nest.swing.menu;

import ru.nest.swing.Constants;

import javax.swing.*;
import java.awt.*;

public class WSeparator extends JComponent {
	public WSeparator() {
		setPreferredSize(new Dimension(0, 2));
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		g.setColor(Constants.separatorColor);
		g.drawLine(28, 0, getWidth(), 0);
	}
}
