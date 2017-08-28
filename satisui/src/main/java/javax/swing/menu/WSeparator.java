package javax.swing.menu;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;

@SuppressWarnings("serial")
public class WSeparator extends JComponent {
	public static Color separatorColor = new Color(220, 220, 220);

	public WSeparator() {
		setPreferredSize(new Dimension(0, 2));
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		g.setColor(separatorColor);
		g.drawLine(30, 0, getWidth(), 0);
		g.setColor(Color.white);
		g.drawLine(30, 1, getWidth(), 1);
	}
}
