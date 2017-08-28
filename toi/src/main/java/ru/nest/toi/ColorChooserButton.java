package ru.nest.toi;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JColorChooser;

import ru.nest.swing.IconButton;

public class ColorChooserButton extends IconButton {
	private static final long serialVersionUID = 9137741011286401021L;

	private Color color = Color.lightGray;

	public ColorChooserButton() {
		super(new Icon() {
			@Override
			public void paintIcon(Component c, Graphics g, int x, int y) {
				ColorChooserButton b = (ColorChooserButton) c;
				g.setColor(b.getColor());
				g.fillRect(x + 2, y + 2, 12, 12);
				g.setColor(Color.black);
				g.drawRect(x + 2, y + 2, 12, 12);
			}

			@Override
			public int getIconWidth() {
				return 16;
			}

			@Override
			public int getIconHeight() {
				return 16;
			}
		});

		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!ignoreShowDialog) {
					Color color = JColorChooser.showDialog(null, "Выберите цвет", getColor());
					if (color != null) {
						setColor(color);
					}
				}
			}
		});
	}

	public void setColor(Color color) {
		setColor(color, true);
	}

	private boolean ignoreShowDialog = false;

	public void setColor(Color color, boolean event) {
		if (color != null && !color.equals(this.color)) {
			this.color = color;
			repaint();
			if (event) {
				ignoreShowDialog = true;
				try {
					fireActionPerformed(new ActionEvent(this, 0, ""));
				} finally {
					ignoreShowDialog = false;
				}
			}
		}
	}

	public Color getColor() {
		return color;
	}
}
