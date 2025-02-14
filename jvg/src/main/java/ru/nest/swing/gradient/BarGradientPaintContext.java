package ru.nest.swing.gradient;

import sun.awt.image.IntegerComponentRaster;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.lang.ref.WeakReference;
import java.util.Arrays;

class BarGradientPaintContext implements PaintContext {
	private int x, w;

	private float a02;

	public BarGradientPaintContext(Color color, int x, int w, AffineTransform transform) throws NoninvertibleTransformException {
		this.x = x;
		this.w = w;

		interp = new int[w];
		for (int i = 0; i < w; i++) {
			double X = Math.abs(i - this.w / 3.0) / w;
			double koef = 1 - X;
			if (koef > 1) {
				koef = 1;
			} else if (koef < 0) {
				koef = 0;
			}

			int r = color.getRed();
			int g = color.getGreen();
			int b = color.getBlue();

			double fr = r + 70 / (1 + (double) r);
			double fg = g + 70 / (1 + (double) g);
			double fb = b + 70 / (1 + (double) b);

			double s = (fr + fg + fb) / 5.0;
			double d = s * koef * koef * koef;
			double k = 0.7 * koef;

			r = (int) (r * k + d);
			g = (int) (g * k + d);
			b = (int) (b * k + d);

			if (r > 255)
				r = 255;
			if (g > 255)
				g = 255;
			if (b > 255)
				b = 255;

			interp[i] = (r << 16) + (g << 8) + b;
		}

		AffineTransform tInv = transform.createInverse();
		double m[] = new double[6];
		tInv.getMatrix(m);
		a02 = (float) m[4];
	}

	static ColorModel cachedModel;

	static WeakReference<Raster> cached;

	static synchronized Raster getCachedRaster(ColorModel cm, int w, int h) {
		if (cm == cachedModel) {
			if (cached != null) {
				Raster ras = cached.get();
				if (ras != null && ras.getWidth() >= w && ras.getHeight() >= h) {
					cached = null;
					return ras;
				}
			}
		}
		return cm.createCompatibleWritableRaster(w, h);
	}

	static synchronized void putCachedRaster(ColorModel cm, Raster ras) {
		if (cached != null) {
			Raster cras = cached.get();
			if (cras != null) {
				int cw = cras.getWidth();
				int ch = cras.getHeight();
				int iw = ras.getWidth();
				int ih = ras.getHeight();
				if (cw >= iw && ch >= ih) {
					return;
				}
				if (cw * ch >= iw * ih) {
					return;
				}
			}
		}
		cachedModel = cm;
		cached = new WeakReference<>(ras);
	}

	private int interp[];

	private Raster saved;

	private ColorModel model = new DirectColorModel(24, 0x00ff0000, 0x0000ff00, 0x000000ff);

	@Override
	public void dispose() {
		if (saved != null) {
			putCachedRaster(model, saved);
			saved = null;
		}
	}

	@Override
	public ColorModel getColorModel() {
		return model;
	}

	@Override
	public Raster getRaster(int x, int y, int w, int h) {
		Raster rast = saved;
		if (rast == null || rast.getWidth() < w || rast.getHeight() < h) {
			rast = getCachedRaster(model, w, h);
			saved = rast;
		}

		IntegerComponentRaster irast = (IntegerComponentRaster) rast;
		int[] pixels = irast.getDataStorage();
		int W = irast.getScanlineStride();
		int size = pixels.length;

		int X1 = this.x;
		int X2 = X1 + this.w;
		int x1 = (int) (x + a02);
		int x2 = x1 + w;

		int leftW = Math.min(X1 - x1, w);

		int rightPos1 = Math.max(0, X2 - x1);
		int rightW = Math.min(w - rightPos1, w);

		int cx1 = Math.max(x1, X1);
		int cx2 = Math.min(x2, X2);
		int cw = cx2 - cx1;
		int cx1_X1 = cx1 - X1;
		int cx1_x1 = cx1 - x1;

		for (int i = 0; i < size; i += W) {
			if (leftW > 0 && x < 100) {
				Arrays.fill(pixels, i, i + leftW, interp[0]);
			}

			if (rightW > 0) {
				Arrays.fill(pixels, i + rightPos1, i + W, interp[interp.length - 1]);
			}

			if (cw > 0) {
				System.arraycopy(interp, cx1_X1, pixels, i + cx1_x1, cw);
			}
		}

		return rast;
	}
}
