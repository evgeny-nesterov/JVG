package javax.swing;

import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class IconToggleButton extends JToggleButton {
	private static final long serialVersionUID = 1L;

	public IconToggleButton(Icon icon) {
		super(icon);
		setMargin(new Insets(1, 1, 1, 1));
		setOpaque(false);
		setContentAreaFilled(false);
		setBorderPainted(false);

		addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				setBorderPainted(isSelected());
			}
		});

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (isEnabled()) {
					repaint();
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (isEnabled()) {
					repaint();
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				setOpaque(true);
				setContentAreaFilled(true);
				setBorderPainted(true);
				repaint();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				if (!isSelected()) {
					setOpaque(false);
					setContentAreaFilled(false);
					setBorderPainted(false);
				}
				repaint();
			}
		});
	}

	@Override
	public void setSelected(boolean isSelected) {
		super.setSelected(isSelected);
		if (isSelected) {
			setOpaque(true);
			setContentAreaFilled(true);
			setBorderPainted(true);
		}
	}

	public void setSelectedNoEvent(boolean isSelected) {
		ActionListener[] ls = getActionListeners();
		for (ActionListener l : ls) {
			removeActionListener(l);
		}
		setSelected(isSelected);
		for (ActionListener l : ls) {
			addActionListener(l);
		}
	}
}
