package ru.nest.layout;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class PageLayout implements LayoutManager {
	public PageLayout() {
	}

	public PageLayout(int hgap, int vgap) {
		this.hgap = hgap;
		this.vgap = vgap;
	}

	private int hgap = 0;

	private int vgap = 0;

	@Override
	public void layoutContainer(Container parent) {
		synchronized (parent.getTreeLock()) {
			Insets insets = parent.getInsets();
			Dimension parent_size = parent.getSize();
			int parent_width = parent_size.width - insets.left - insets.right;

			int x = 0;
			int y = 0;
			int h = 0;

			for (Component c : parent.getComponents()) {
				Dimension size = c.getPreferredSize();
				if (x + size.width < parent_width) {
					if (h < size.height) {
						h = size.height;
					}
				} else {
					x = 0;
					y += h + hgap;
					h = 0;
				}

				c.setBounds(x + insets.left, y + insets.top, size.width, size.height);
				x += size.width + vgap;
			}
		}
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return preferredLayoutSize(parent);
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		synchronized (parent.getTreeLock()) {
			Insets insets = parent.getInsets();
			int parent_width = parent.getWidth() - insets.left - insets.right;

			int w = insets.left;
			int h = insets.top;
			int row_width = 0;
			int row_height = 0;

			for (Component c : parent.getComponents()) {
				Dimension size = c.getPreferredSize();
				if (row_width + size.width < parent_width) {
					row_width += size.width + hgap;
					if (row_height < size.height) {
						row_height = size.height;
					}
				} else {
					if (w < row_width) {
						w = row_width;
					}
					h += row_height + vgap;

					row_width = size.width;
					row_height = size.height;
				}
			}

			if (w < row_width) {
				w = row_width;
			}
			h += row_height;

			return new Dimension(w + insets.left + insets.right, h + insets.top + insets.bottom);
		}
	}

	@Override
	public void removeLayoutComponent(Component comp) {
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {
	}

	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setBounds(200, 200, 400, 400);

		JPanel pnl = new JPanel();
		pnl.setLayout(new PageLayout());
		for (int i = 0; i < 100; i++) {
			pnl.add(new JLabel() {
				private static final long serialVersionUID = 1L;

				int w, h;

				Color color;
				{
					w = 20 + (int) (60 * Math.random());
					h = 15 + (int) (10 * Math.random());
					w = 40;
					h = 60;
					setPreferredSize(new Dimension(w, h));
					color = new Color((int) (256 * Math.random()), (int) (256 * Math.random()), (int) (256 * Math.random()));
				}

				@Override
				public void paint(Graphics g) {
					super.paint(g);
					g.setColor(color);
					g.fillRect(1, 1, w - 2, h - 2);
				}
			});
		}
		f.setContentPane(new JScrollPane(pnl));
		f.setVisible(true);
	}

	public int getHgap() {
		return hgap;
	}

	public void setHgap(int hgap) {
		this.hgap = hgap;
	}

	public int getVgap() {
		return vgap;
	}

	public void setVgap(int vgap) {
		this.vgap = vgap;
	}
}
