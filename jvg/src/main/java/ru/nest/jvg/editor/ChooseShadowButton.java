package ru.nest.jvg.editor;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.ActionChooser;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import ru.nest.jvg.action.ShadowAction;
import ru.nest.jvg.shape.paint.ShadowPainter;

public class ChooseShadowButton extends ActionChooser {
	public ChooseShadowButton() {
		JPopupMenu popup = choosePanel.getPopupMenu();
		popup.setLayout(new GridBagLayout());

		int x = 0, y = 1;
		int i = 0;
		popup.add(new ShadowButton(ShadowPainter.STRAIGHT__LEFT_TOP), new GridBagConstraints(x++, y, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(i, i, i, i), 0, 0));
		ShadowButton btn = new ShadowButton(ShadowPainter.STRAIGHT__RIGHT_TOP);
		setCurrentAction(btn);
		popup.add(btn, new GridBagConstraints(x++, y, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(i, i, i, i), 0, 0));
		popup.add(new ShadowButton(ShadowPainter.SLOPE_LEFT_BACK), new GridBagConstraints(x++, y, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(i, i, i, i), 0, 0));
		popup.add(new ShadowButton(ShadowPainter.SLOPE_RIGHT_BACK), new GridBagConstraints(x++, y, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(i, i, i, i), 0, 0));

		x = 0;
		y++;
		popup.add(new ShadowButton(ShadowPainter.STRAIGHT__LEFT_BOTTOM), new GridBagConstraints(x++, y, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(i, i, i, i), 0, 0));
		popup.add(new ShadowButton(ShadowPainter.STRAIGHT__RIGHT_BOTTOM), new GridBagConstraints(x++, y, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(i, i, i, i), 0, 0));

		popup.add(new ShadowButton(ShadowPainter.SLOPE_LEFT_FORWARD), new GridBagConstraints(x++, y, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(i, i, i, i), 0, 0));
		popup.add(new ShadowButton(ShadowPainter.SLOPE_RIGHT_FORWARD), new GridBagConstraints(x++, y, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(i, i, i, i), 0, 0));

		init(btn.icon, getAction(), choosePanel);
	}

	private JMenu choosePanel = new JMenu();

	public JPopupMenu getPopup() {
		return choosePanel.getPopupMenu();
	}

	private Map<Integer, ShadowButton> map = new HashMap<>();

	class ShadowButton extends JMenuItem implements ActionListener {
		protected Action action;

		protected int type;

		protected ImageIcon icon;

		public ShadowButton(int type) {
			this.type = type;
			map.put(type, this);

			setPreferredSize(new Dimension(24, 24));
			setOpaque(false);
			setBorderPainted(false);
			setRequestFocusEnabled(false);

			action = new ShadowAction(type);
			addActionListener(action);
			addActionListener(this);

			switch (type) {
				case ShadowPainter.STRAIGHT__LEFT_BOTTOM:
					icon = new ImageIcon(ChooseShadowButton.class.getResource("img/stright_left_bottom.png"));
					break;

				case ShadowPainter.STRAIGHT__RIGHT_BOTTOM:
					icon = new ImageIcon(ChooseShadowButton.class.getResource("img/stright_right_bottom.png"));
					break;

				case ShadowPainter.STRAIGHT__LEFT_TOP:
					icon = new ImageIcon(ChooseShadowButton.class.getResource("img/stright_left_top.png"));
					break;

				case ShadowPainter.STRAIGHT__RIGHT_TOP:
					icon = new ImageIcon(ChooseShadowButton.class.getResource("img/stright_right_top.png"));
					break;

				case ShadowPainter.SLOPE_LEFT_BACK:
					icon = new ImageIcon(ChooseShadowButton.class.getResource("img/slope_left_back.png"));
					break;

				case ShadowPainter.SLOPE_LEFT_FORWARD:
					icon = new ImageIcon(ChooseShadowButton.class.getResource("img/slope_left_forward.png"));
					break;

				case ShadowPainter.SLOPE_RIGHT_BACK:
					icon = new ImageIcon(ChooseShadowButton.class.getResource("img/slope_right_back.png"));
					break;

				case ShadowPainter.SLOPE_RIGHT_FORWARD:
					icon = new ImageIcon(ChooseShadowButton.class.getResource("img/slope_right_forward.png"));
					break;
			}
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);
			if (icon != null) {
				icon.paintIcon(this, g, 0, 0);
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			setCurrentAction(this);
		}
	}

	private void setCurrentAction(ShadowButton action) {
		if (action != null) {
			type = action.type;
			setIcon(action.icon);
			setAction(action.action);
			repaint();
		}
	}

	private int type;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		setCurrentAction(map.get(type));
	}
}
