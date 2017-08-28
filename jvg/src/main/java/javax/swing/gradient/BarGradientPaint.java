package javax.swing.gradient;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class BarGradientPaint implements Paint {
	private Color color;

	private int x, w;

	public BarGradientPaint(Color color, int x, int w) {
		this.color = color;
		this.x = x;
		this.w = w;
	}

	@Override
	public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds, AffineTransform xform, RenderingHints hints) {
		try {
			return new BarGradientPaintContext(color, x, w, xform);
		} catch (NoninvertibleTransformException exc) {
			throw new IllegalArgumentException("transform should be invertible");
		}
	}

	@Override
	public int getTransparency() {
		return Transparency.OPAQUE;
	}

	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.setContentPane(new JLabel() {
			@Override
			public void paint(Graphics g) {
				super.paint(g);

				BarGradientPaint p = new BarGradientPaint(Color.blue, 160, getWidth() - 320);

				Graphics2D g2d = (Graphics2D) g;
				g2d.setPaint(p);
				g2d.fillRect(0, 0, getWidth(), getHeight());
			}
		});
		f.setBounds(200, 200, 600, 400);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}
}
