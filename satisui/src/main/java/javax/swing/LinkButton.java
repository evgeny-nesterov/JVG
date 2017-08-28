package javax.swing;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.border.Border;

public class LinkButton extends JButton {
	private static final long serialVersionUID = 1L;

	private static final Color LINK_COLOR = Color.blue;

	private static final Border LINK_BORDER = BorderFactory.createEmptyBorder(0, 0, 1, 0);

	private static final Border HOVER_BORDER = BorderFactory.createMatteBorder(0, 0, 1, 0, LINK_COLOR);

	/** Creates a new instance of LinkButton */
	public LinkButton(String text) {
		super(text);
		setBorder(null);
		setBorder(LINK_BORDER);
		setForeground(LINK_COLOR);
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		setFocusPainted(false);
		setRequestFocusEnabled(false);
		setContentAreaFilled(false);
		addMouseListener(new LinkMouseListener());
	}

	private class LinkMouseListener extends MouseAdapter {
		@Override
		public void mouseEntered(MouseEvent e) {
			((JComponent) e.getComponent()).setBorder(HOVER_BORDER);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			((JComponent) e.getComponent()).setBorder(HOVER_BORDER);
		}

		@Override
		public void mouseExited(MouseEvent e) {
			((JComponent) e.getComponent()).setBorder(LINK_BORDER);
		}
	};
}
