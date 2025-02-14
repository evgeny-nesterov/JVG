package ru.nest.swing;

import ru.nest.jvg.JVGGraphics;

import javax.swing.*;
import java.awt.*;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public class CompositeTest {
	public CompositeTest() {
	}

	public static void main(String[] args) {
		JLabel lbl = new JLabel() {
			@Override
			public void paint(Graphics g) {
				super.paint(g);

				Graphics2D g2d = new JVGGraphics((Graphics2D) g);

				g.setColor(Color.red);
				g.fillRect(20, 20, 100, 100);

				g.setColor(Color.green);
				g.fillRect(30, 30, 100, 100);

				g.setColor(Color.yellow);
				g.fillRect(40, 40, 100, 100);

				g.setColor(Color.black);
				g.drawString("Однажды в студёную зимнюю пору я вышел был сильный мороз", 50, 50);

				g2d.setComposite(new C());

				g.setColor(Color.blue);
				g.fillRect(50, 0, 200, 200);
			}
		};

		JFrame f = new JFrame();
		f.setContentPane(lbl);
		f.setBounds(300, 200, 300, 300);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}
}

class C implements Composite {
	@Override
	public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
		return new CC();
	}
}

class CC implements CompositeContext {
	@Override
	public void dispose() {
	}

	@Override
	public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
		float alfa = 0.7f;

		int[] a = new int[4];
		int[] b = new int[4];
		int w = src.getWidth();
		int h = src.getHeight();
		for (int x = src.getMinX(); x < src.getMinX() + w; x++) {
			for (int y = src.getMinY(); y < src.getMinY() + h; y++) {
				a[0] = a[1] = a[2] = a[3] = 0;
				float n = 0;
				int avg = 2;
				int x1 = Math.max(dstIn.getMinX(), x - avg);
				int x2 = Math.min(dstIn.getMinX() + w - 1, x + avg);
				int y1 = Math.max(dstIn.getMinY(), y - avg);
				int y2 = Math.min(dstIn.getMinY() + h - 1, y + avg);
				for (int i = x1; i <= x2; i++) {
					for (int j = y1; j <= y2; j++) {
						dstIn.getPixel(i, j, b);
						for (int k = 0; k < 4; k++)
							a[k] += b[k];
						n++;
					}
				}
				for (int j = 0; j < 4; j++)
					a[j] /= n;

				src.getPixel(x, y, b);

				a[0] = (int) (b[0] * (1 - alfa) + a[0] * alfa);
				a[1] = (int) (b[1] * (1 - alfa) + a[1] * alfa);
				a[2] = (int) (b[2] * (1 - alfa) + a[2] * alfa);
				a[3] = (int) (b[3] * (1 - alfa) + a[3] * alfa);
				dstOut.setPixel(x, y, a);
			}
		}
	}
}
