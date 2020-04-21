package ru.nest.jvg.editor.clipboard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

import ru.nest.fonts.Fonts;

public class ClipboardListRenderer extends JLabel implements ListCellRenderer {
	private Color selectedColor = new Color(180, 230, 180, 100);

	private Color borderColor = new Color(150, 200, 150);

	public ClipboardListRenderer() {
		setFont(Fonts.getFont("Dialog", Font.BOLD, 10));
		setHorizontalAlignment(SwingConstants.CENTER);
		setOpaque(false);
	}

	private int index;

	private JList list;

	private boolean isSelected;

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		this.index = index;
		this.list = list;
		this.isSelected = isSelected;

		JVGClipboardContext ctx = (JVGClipboardContext) value;
		ImageIcon icon = ctx.getSnapshot();
		if (icon != null) {
			setIcon(icon);
		}

		JPanel pnl = new JPanel();
		pnl.setLayout(new BorderLayout());
		if (ctx.getWidth() != -1) {
			pnl.add(new JLabel(ctx.getWidth() + " x " + ctx.getHeight()), BorderLayout.NORTH);
		}
		pnl.add(this, BorderLayout.CENTER);
		return pnl;
	}

	@Override
	public void paintComponent(Graphics g) {
		g.setColor(Color.white);
		g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
		g.setColor(new Color(230, 230, 230));
		for (int x = 0; x < getWidth(); x += 40) {
			for (int y = 0; y < getHeight(); y += 40) {
				g.fillRect(x, y, 20, 20);
				g.fillRect(x + 20, y + 20, 20, 20);
			}
		}
		if (isSelected) {
			g.setColor(selectedColor);
			g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
		}

		g.setColor(borderColor);
		g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);

		super.paintComponent(g);
	}
}
