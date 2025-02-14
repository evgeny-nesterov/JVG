package ru.nest.swing;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ActionChooser extends JComponent {
	private boolean isSelected = false;

	private boolean isOver = false;

	private boolean choose = false;

	protected int space = 12;

	public ActionChooser(Icon icon, ActionListener action, final JMenu menu) {
		init(icon, action, menu);
	}

	public ActionChooser() {
	}

	public void init(Icon icon, ActionListener action, final JMenu menu) {
		this.action = action;

		setIcon(icon);
		setOpaque(false);

		int w = 24, h = 24;
		if (icon != null && icon.getIconWidth() > 0 && icon.getIconHeight() > 0) {
			w = icon.getIconWidth();
			h = icon.getIconHeight();
		}
		setPreferredSize(new Dimension(w + space, h));

		menu.getPopupMenu().addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				choose = false;
				repaint();
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				choose = false;
				repaint();
			}
		});

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (isEnabled()) {
					isSelected = true;
					if (e.getX() > getWidth() - space) {
						choose = !menu.getPopupMenu().isVisible();
						if (choose) {
							menu.setSize(getWidth(), getHeight());
							menu.getPopupMenu().show(ActionChooser.this, 0, getHeight());
						} else {
							menu.getPopupMenu().setVisible(false);
						}
					}

					repaint();
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				isSelected = false;
				if (isEnabled()) {
					repaint();
				}

				if (!choose && ActionChooser.this.action != null) {
					ActionChooser.this.action.actionPerformed(new ActionEvent(e.getSource(), 0, "action", e.getWhen(), e.getModifiers()));
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				if (isEnabled()) {
					isOver = true;
					repaint();
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				isOver = false;
				if (isEnabled()) {
					repaint();
				}
			}
		});
	}

	private ActionListener action = null;

	public void setAction(ActionListener action) {
		this.action = action;
	}

	public ActionListener getAction() {
		return action;
	}

	private Icon icon;

	private Image disabledIcon;

	public void setIcon(Icon icon) {
		this.icon = icon;
		if (icon != null) {
			disabledIcon = Util.createDisabledImage(icon);
		}
	}

	@Override
	public void paint(Graphics g) {
		int w = 0;
		int h = 0;
		if (icon != null) {
			w = icon.getIconWidth();
			h = icon.getIconHeight();
		}
		int W = getWidth();
		int H = getHeight();
		int x = 3;
		int y = (H - h) / 2;

		if (isSelected && !choose) {
			x++;
			y++;
		}

		if (isEnabled()) {
			if (choose) {
				g.setColor(Constants.borderColor);
				g.drawRect(0, 1, W - 1, H);
			} else if (isSelected || isOver) {
				g.setColor(Constants.rolloverBackground);
				g.fillRect(0, 0, W, H);

				g.setColor(Constants.rolloverOutlineBackground);
				g.drawRect(0, 0, W - 1, H - 1);

				int sepX = W - space;
				g.drawLine(sepX, 0, sepX, H);
			}
		}

		if (isEnabled() && icon != null) {
			icon.paintIcon(this, g, x, y);
		} else if (!isEnabled() && disabledIcon != null) {
			g.drawImage(disabledIcon, x, y, null);
		}

		y = H / 2 - 1;
		g.setColor(isEnabled() ? Color.black : Color.gray);
		g.drawLine(W - 8, y, W - 4, y);
		g.drawLine(W - 7, y + 1, W - 5, y + 1);
		g.drawLine(W - 6, y + 2, W - 6, y + 2);
	}
}
