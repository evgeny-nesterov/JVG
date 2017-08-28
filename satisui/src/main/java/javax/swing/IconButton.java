package javax.swing;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class IconButton extends JButton {
	private static final long serialVersionUID = 1L;

	public IconButton() {
		this(null);
	}

	public IconButton(Icon icon) {
		this(icon, 1);
	}

	public IconButton(Icon icon, int margin) {
		this(icon, new Insets(margin, margin, margin, margin));
	}

	public IconButton(Icon icon, Insets margin) {
		super(icon);
		setMargin(margin);
		setOpaque(false);
		setContentAreaFilled(false);
		setBorderPainted(false);
		setPreferredSize(new Dimension(20, 20));
		setRequestFocusEnabled(false);

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				setContentAreaFilled(true);
				setBorderPainted(true);
				repaint();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				setContentAreaFilled(false);
				setBorderPainted(false);
				repaint();
			}
		});
	}
}
