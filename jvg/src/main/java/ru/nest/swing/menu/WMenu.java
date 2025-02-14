package ru.nest.swing.menu;

import ru.nest.swing.Constants;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicMenuUI;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class WMenu extends JMenu {
	public WMenu(String text) {
		this(text, new EmptyIcon());
	}

	public WMenu() {
		this("", new EmptyIcon());
	}

	public WMenu(String text, Icon icon) {
		super(text);

		setIcon(icon);
		setFont(new Font("SanSerif", Font.PLAIN, 12));
		setBorderPainted(false);
		setIconTextGap(10);

		final JPopupMenu popup = getPopupMenu();
		popup.setBackground(new Color(249, 248, 247));
		popup.setBorder(new Border() {
			private Set<Integer> positions = new HashSet<>();

			@Override
			public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
				positions.clear();
				for (int i = 0; i < popup.getComponentCount(); i++) {
					positions.add(popup.getComponent(i).getX());
				}

				g.drawLine(width - 1, 0, width - 1, height - 1);
				g.drawLine(0, height - 1, width - 1, height - 1);
				g.drawLine(0, 0, width - 1, 0);

				for (int pos : positions) {
					g.setColor(Constants.borderColor);
					g.drawLine(pos - 1, 0, pos - 1, height);

					g.setColor(Constants.iconColor);
					g.fillRect(pos, 2, 23, height - 4);
				}
			}

			@Override
			public Insets getBorderInsets(Component c) {
				return new Insets(2, 1, 1, 1);
			}

			@Override
			public boolean isBorderOpaque() {
				return true;
			}
		});

		setUI(new BasicMenuUI() {
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
				if (model.isSelected()) {
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
	protected Point getPopupMenuOrigin() {
		return new Point(getWidth(), 0);
	}

	@Override
	public void setIcon(Icon i) {
		if (!(i instanceof WrappedIcon)) {
			i = new WrappedIcon(i);
		}
		super.setIcon(i);
	}
}
