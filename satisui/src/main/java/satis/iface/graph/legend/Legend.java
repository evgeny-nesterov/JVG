package satis.iface.graph.legend;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JLabel;

import satis.iface.graph.MovableComponent;

public class Legend extends MovableComponent // implements MarkerListener
{
	private static final long serialVersionUID = 1L;

	public Legend() {
		super();
	}

	private LegendRenderer renderer = null;

	public void setRenderer(LegendRenderer renderer) {
		this.renderer = renderer;
	}

	public LegendRenderer getRenderer() {
		return renderer;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		if (renderer != null) {
			int rows = renderer.getRows();
			double h = getHeight() / (double) rows;

			for (int i = 0; i < rows; i++) {
				Component c = renderer.getLegendComponent(this, i);
				if (c != null) {
					c.setBounds(0, 0, getWidth(), (int) h);

					int H = (int) (i * h);
					g.translate(0, H);
					c.paint(g);
					g.translate(0, -H);
				}
			}
		}
	}

	public void pack() {
		setSize(getPreferredSize());
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension size = super.getPreferredSize();
		if (renderer != null) {
			size.width = 0;
			size.height = 0;

			int rows = renderer.getRows();
			for (int i = 0; i < rows; i++) {
				Component c = renderer.getLegendComponent(this, i);
				if (c != null) {
					Dimension s = c.getPreferredSize();
					size.width = Math.max(size.width, s.width);
					size.height += s.height;
				}
			}
		}

		return size;
	}

	@Override
	public boolean isMovableAreaPoint(int x, int y) {
		return true;
	}

	public static void main(String[] arg) {
		JFrame f = new JFrame();

		class MyLegend extends Legend {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isMovableAreaPoint(int x, int y) {
				return true;
			}
		}

		class MyRenderer extends JLabel implements LegendRenderer {
			private static final long serialVersionUID = 1L;

			@Override
			public int getRows() {
				return 3;
			}

			@Override
			public Component getLegendComponent(Legend legend, int row) {
				this.setText("(" + row + ") Plot");
				return this;
			}
		}

		MyLegend legend = new MyLegend();
		legend.setRenderer(new MyRenderer());
		legend.setLocation(10, 10);
		legend.setOpaque(true);
		legend.setBackground(Color.white);
		legend.pack();

		f.getContentPane().setLayout(null);
		f.getContentPane().add(legend);

		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setLocation(200, 200);
		f.setSize(400, 400);
		f.setVisible(true);
		f.toFront();
	}
}
