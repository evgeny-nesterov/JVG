package javax.swing.menu;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.Constants;
import javax.swing.JComponent;

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
