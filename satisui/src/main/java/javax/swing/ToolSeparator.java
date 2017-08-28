package javax.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

public class ToolSeparator extends JLabel {
	private static final long serialVersionUID = 1L;
	public Dimension separatorSize;

	public ToolSeparator(int h) {
		this(9, h);
	}

	public ToolSeparator(int w, int h) {
		super();

		separatorSize = new Dimension(w, h);
		setOpaque(false);
		setPreferredSize(separatorSize);
		setSize(separatorSize);
		setMinimumSize(separatorSize);
		setMaximumSize(separatorSize);
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		g.setColor(Color.gray);
		g.drawLine(getWidth() / 2, 1, getWidth() / 2, getHeight() - 3);

		g.setColor(Color.white);
		g.drawLine(getWidth() / 2 + 1, 1, getWidth() / 2 + 1, getHeight() - 3);
	}
}
