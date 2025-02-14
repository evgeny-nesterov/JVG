package ru.nest.swing.menu;

import ru.nest.swing.Constants;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicCheckBoxMenuItemUI;
import java.awt.*;

public class WCheckBoxMenuItem extends JCheckBoxMenuItem {
	private final static ImageIcon imgChecked = new ImageIcon(WCheckBoxMenuItem.class.getResource("checked.gif"));

	private final static ImageIcon imgUnchecked = new ImageIcon(WCheckBoxMenuItem.class.getResource("unchecked.gif"));

	public WCheckBoxMenuItem(String s) {
		this(s, null);
	}

	private boolean toggle;

	public WCheckBoxMenuItem(String s, Icon icon) {
		super(s);

		setFont(new Font("SanSerif", Font.PLAIN, 12));
		setBorderPainted(false);
		setOpaque(false);

		toggle = icon != null;
		if (toggle) {
			icon = new ToggleIcon(icon);
			setIcons(icon, icon);
			setDisabledIcon(icon);
		} else {
			setIcons(imgChecked, imgUnchecked);
		}

		addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				updateIcon();
			}
		});

		setUI(new BasicCheckBoxMenuItemUI() {
			@Override
			protected void installDefaults() {
				super.installDefaults();
				selectionBackground = new Color(0, 0, 0, 0);
				defaultTextIconGap = 4;
				arrowIcon = null;
				checkIcon = null;
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
	}

	@Override
	public void setSelectedIcon(Icon i) {
		if (!(i instanceof WrappedIcon)) {
			i = new WrappedIcon(i);
		}
		super.setSelectedIcon(i);
	}

	public void setIcons(Icon checked, Icon unchecked) {
		checkedIcon = new WrappedIcon(checked);
		uncheckedIcon = new WrappedIcon(unchecked);
		updateIcon();
	}

	private WrappedIcon checkedIcon;

	private WrappedIcon uncheckedIcon;

	public void updateIcon() {
		if (isSelected()) {
			setIcon(checkedIcon);
		} else {
			setIcon(uncheckedIcon);
		}
	}
}
