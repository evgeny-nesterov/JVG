package javax.swing.menu;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.ButtonModel;
import javax.swing.Constants;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.plaf.basic.BasicMenuItemUI;

public class WMenuItem extends JMenuItem {
	public WMenuItem(String s) {
		this(s, new EmptyIcon());
	}

	public WMenuItem() {
		this("", new EmptyIcon());
	}

	public WMenuItem(Icon i) {
		this("", i);
	}

	public WMenuItem(String s, Icon i) {
		super(s, i);

		setFont(new Font("SanSerif", Font.PLAIN, 12));
		setBorderPainted(false);
		setOpaque(false);

		setUI(new BasicMenuItemUI() {
			@Override
			protected void installDefaults() {
				super.installDefaults();
				selectionBackground = new Color(0, 0, 0, 0);
				defaultTextIconGap = 4;
			}

			@Override
			public Dimension getPreferredSize(JComponent c) {
				Dimension s = super.getPreferredSize(c);
				s.height += 2;
				return s;
			}

			@Override
			protected void paintBackground(Graphics g, JMenuItem menuItem, Color bgColor) {
				super.paintBackground(g, menuItem, bgColor);

				ButtonModel model = menuItem.getModel();
				int menuWidth = menuItem.getWidth();
				int menuHeight = menuItem.getHeight();

				Color oldColor = g.getColor();
				if (model.isArmed()) {
					g.setColor(Constants.rolloverBackground);
					g.fillRect(1, 0, menuWidth - 3, menuHeight - 2);

					g.setColor(Constants.rolloverOutlineBackground);
					g.drawRect(1, 0, menuWidth - 3, menuHeight - 2);
				}
				g.setColor(oldColor);
			}
		});
	}

	@Override
	public void setIcon(Icon i) {
		if (!(i instanceof WrappedIcon)) {
			i = new WrappedIcon(i);
		}
		super.setIcon(i);
		super.setDisabledIcon(i);
	}
}
