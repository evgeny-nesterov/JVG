package javax.swing.menu;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ButtonModel;
import javax.swing.Constants;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicMenuUI;

public class WBarMenu extends JMenu {
	private boolean over;

	public WBarMenu() {
		this("");
	}

	public WBarMenu(String s) {
		super(s);

		setFont(new Font("SanSerif", Font.PLAIN, 12));
		setBorderPainted(false);

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				over = true;
				repaint();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				over = false;
				repaint();
			}
		});

		getPopupMenu().setBackground(new Color(249, 248, 247));
		getPopupMenu().setBorder(new Border() {
			@Override
			public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
				g.setColor(Constants.borderColor);
				g.drawLine(WBarMenu.this.getWidth() - 1, 0, width - 1, 0);
				g.drawLine(0, 0, 0, height - 1);
				g.drawLine(width - 1, 0, width - 1, height - 1);
				g.drawLine(0, height - 1, width - 1, height - 1);

				g.setColor(Constants.iconColor);
				g.drawLine(1, 0, WBarMenu.this.getWidth() - 2, 0);
				g.fillRect(1, 2, 23, height - 4);
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
			}

			@Override
			protected void paintBackground(Graphics g, JMenuItem menuItem, Color bgColor) {
				super.paintBackground(g, menuItem, bgColor);

				ButtonModel model = menuItem.getModel();
				int menuWidth = menuItem.getWidth();
				int menuHeight = menuItem.getHeight();

				Color oldColor = g.getColor();
				if (over && !model.isSelected()) {
					g.setColor(Constants.rolloverBackground);
					g.fillRect(0, 1, menuWidth, menuHeight - 3);

					g.setColor(Constants.rolloverOutlineBackground);
					g.drawRect(0, 1, menuWidth - 1, menuHeight - 3);
				}

				if (model.isSelected()) {
					g.setColor(Constants.borderColor);
					g.drawRect(0, 1, menuWidth - 1, menuHeight - 1);
				}
				g.setColor(oldColor);
			}
		});
	}

	@Override
	protected Point getPopupMenuOrigin() {
		return new Point(0, getHeight());
	}
}
