package ru.nest.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;

public class MyRadialGradientPaint implements Paint {
	double x, y, a, b;

	Color color1;

	Color color2;

	boolean cyclic;

	public MyRadialGradientPaint(double x, double y, double a, double b, Color color1, Color color2, boolean cyclic) {
		if ((color1 == null) || (color2 == null)) {
			throw new NullPointerException("Colors cannot be null");
		}

		this.x = x;
		this.y = y;
		this.a = a;
		this.b = b;
		this.color1 = color1;
		this.color2 = color2;
		this.cyclic = cyclic;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getA() {
		return a;
	}

	public double getB() {
		return b;
	}

	public Color getColor1() {
		return color1;
	}

	public Color getColor2() {
		return color2;
	}

	public boolean isCyclic() {
		return cyclic;
	}

	@Override
	public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds, AffineTransform xform, RenderingHints hints) {
		return new MyRadialGradientPaintContext(cm, x, y, a, b, color1, color2, xform, cyclic);
	}

	@Override
	public int getTransparency() {
		int a1 = color1.getAlpha();
		int a2 = color2.getAlpha();
		return (((a1 & a2) == 0xff) ? OPAQUE : TRANSLUCENT);
	}

	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setBounds(100, 100, 600, 600);
		f.setContentPane(new JLabel() {
			MyRadialGradientPaint grad = new MyRadialGradientPaint(300, 300, 200, 100, Color.blue, Color.red, true);

			// GradientPaint grad = new GradientPaint(300f, 300f, Color.blue,
			// 200f, 100f, Color.white, true);
			@Override
			public void paint(Graphics g) {
				super.paint(g);

				Graphics2D g2d = (Graphics2D) g;
				g2d.setPaint(grad);
				g2d.fillRect(0, 0, 1600, 1600);
			}
		});
		f.setVisible(true);
	}
}
