package javax.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

public class VerticalSeparator extends JLabel {
	private static final long serialVersionUID = 1L;

	public VerticalSeparator() {
		setPreferredSize(new Dimension(4, 4));
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		g.setColor(Color.lightGray);
		g.drawLine(3, 1, getWidth() - 3, 1);
		g.setColor(Color.white);
		g.drawLine(3, 2, getWidth() - 3, 2);
	}
}
