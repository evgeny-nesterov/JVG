package ru.nest.fonts;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;

public class FontComboBox extends JComboBox<Font> {
	private static final long serialVersionUID = -8740738997362097624L;

	private List<Font> lastFonts = new ArrayList<Font>();

	public FontComboBox() {
		this((Font[]) null);
	}

	public FontComboBox(Font... addfonts) {
		if (addfonts != null) {
			for (Font f : addfonts) {
				if (f != null) {
					addItem(f.deriveFont(12f));
				}
			}
		}

		Font[] fonts = Fonts.getFonts();
		for (Font f : fonts) {
			if (f != null) {
				addItem(f.deriveFont(12f));
			}
		}

		setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1770106059826140374L;

			private int index;

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				this.index = index;
				Font font = (Font) value;

				JLabel lbl = (JLabel) super.getListCellRendererComponent(list, font != null ? font.getFamily() : null, index, isSelected, cellHasFocus);
				lbl.setOpaque(true);
				if (!isSelected) {
					lbl.setBackground(Color.white);
				}
				lbl.setFont(font);
				return lbl;
			}

			@Override
			public void paint(Graphics g) {
				super.paint(g);
				if (lastFonts.size() > 0 && index == lastFonts.size() - 1) {
					g.setColor(Color.gray);
					g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
				}
			}
		});

		addActionListener(new ActionListener() {
			private boolean ignore = false;

			@Override
			public void actionPerformed(ActionEvent e) {
				Font font = (Font) getSelectedItem();
				if (font != null && !ignore) {
					ignore = true;
					if (lastFonts.contains(font)) {
						int index = lastFonts.indexOf(font);
						FontComboBox.this.removeItemAt(index);
						lastFonts.remove(font);
					} else if (lastFonts.size() == 5) {
						FontComboBox.this.removeItemAt(4);
						lastFonts.remove(lastFonts.size() - 1);
					}
					lastFonts.add(0, font);
					FontComboBox.this.insertItemAt(font, 0);
					setSelectedIndex(0);
					ignore = false;
				}
			}
		});
	}

	public void setSelectedFont(Font selectedFont) {
		Font font = null;
		if (selectedFont != null) {
			for (int i = 0; i < getModel().getSize(); i++) {
				Font c = getModel().getElementAt(i);
				if (c.getName().equals(selectedFont.getName())) {
					font = c;
					break;
				}
			}
		}
		setSelectedItem(font);
	}
}
