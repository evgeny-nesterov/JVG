package javax.swing.statusbar;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.GUIUtils;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class StatusBarLabel extends JLabel {
	private static final long serialVersionUID = 1L;

	/** Indicates to paint the left border */
	private boolean paintLeft;

	/** Indicates to paint the right border */
	private boolean paintRight;

	/** Indicates to paint the top border */
	private boolean paintTop;

	/** Indicates to paint the bottom border */
	private boolean paintBottom;

	/** the border colour */
	private Color borderColour;

	/** the label height */
	private int height;

	public StatusBarLabel(boolean paintTop, boolean paintLeft, boolean paintBottom, boolean paintRight) {
		this(paintTop, paintLeft, paintBottom, paintRight, 20);
	}

	public StatusBarLabel(boolean paintTop, boolean paintLeft, boolean paintBottom, boolean paintRight, int height) {
		this.paintTop = paintTop;
		this.paintLeft = paintLeft;
		this.paintBottom = paintBottom;
		this.paintRight = paintRight;
		this.height = height;
		setVerticalAlignment(SwingConstants.CENTER);
		borderColour = GUIUtils.getDefaultBorderColour();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(borderColour);

		int width = getWidth();
		int height = getHeight();

		if (paintTop) {
			g.drawLine(0, 0, width, 0);
		}
		if (paintBottom) {
			g.drawLine(0, height - 1, width, height - 1);
		}

		if (paintLeft) {
			g.drawLine(0, 0, 0, height);
		}
		if (paintRight) {
			g.drawLine(width - 1, 0, width - 1, height);
		}
	}
}
