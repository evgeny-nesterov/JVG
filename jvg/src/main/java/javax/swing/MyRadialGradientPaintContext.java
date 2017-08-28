package javax.swing;

import java.awt.Color;
import java.awt.PaintContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.lang.ref.WeakReference;

import sun.awt.image.IntegerComponentRaster;

class MyRadialGradientPaintContext implements PaintContext {
	static ColorModel xrgbmodel = new DirectColorModel(24, 0x00ff0000, 0x0000ff00, 0x000000ff);

	static ColorModel xbgrmodel = new DirectColorModel(24, 0x000000ff, 0x0000ff00, 0x00ff0000);

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
		cached = new WeakReference<Raster>(ras);
	}

	Raster saved;

	ColorModel model;

	double x, y, a, b;

	int a1, r1, g1, b1, da, dr, dg, db;

	boolean cyclic;

	public MyRadialGradientPaintContext(ColorModel cm, double x, double y, double a, double b, Color c1, Color c2, AffineTransform xform, boolean cyclic) {
		Point2D point = new Point2D.Double(x, y);
		xform.transform(point, point);
		x = point.getX();
		y = point.getY();

		this.x = x;
		this.y = y;
		this.a = a;
		this.b = b;
		this.cyclic = cyclic;

		int rgb1 = c1.getRGB();
		int rgb2 = c2.getRGB();
		a1 = (rgb1 >> 24) & 0xff;
		r1 = (rgb1 >> 16) & 0xff;
		g1 = (rgb1 >> 8) & 0xff;
		b1 = (rgb1) & 0xff;
		da = ((rgb2 >> 24) & 0xff) - a1;
		dr = ((rgb2 >> 16) & 0xff) - r1;
		dg = ((rgb2 >> 8) & 0xff) - g1;
		db = ((rgb2) & 0xff) - b1;
		if (a1 == 0xff && da == 0) {
			model = xrgbmodel;
			if (cm instanceof DirectColorModel) {
				DirectColorModel dcm = (DirectColorModel) cm;
				int tmp = dcm.getAlphaMask();
				if ((tmp == 0 || tmp == 0xff) && dcm.getRedMask() == 0xff && dcm.getGreenMask() == 0xff00 && dcm.getBlueMask() == 0xff0000) {
					model = xbgrmodel;
					tmp = r1;
					r1 = b1;
					b1 = tmp;
					tmp = dr;
					dr = db;
					db = tmp;
				}
			}
		} else {
			model = ColorModel.getRGBdefault();
		}
	}

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
		int off = irast.getDataOffset(0);
		int adjust = irast.getScanlineStride() - w;
		int[] pixels = irast.getDataStorage();

		double X = (x - this.x) / a;
		double Y = (y - this.y) / b;
		for (int i = 0; i < h; i++) {
			double dy = Y + i / b;
			double dy_sq = dy * dy;
			for (int j = 0; j < w; j++) {
				double dx = X + j / a;
				double alfa = Math.sqrt(dx * dx + dy_sq);
				if (alfa > 1) {
					if (cyclic) {
						alfa %= 2;
						if (alfa > 1) {
							alfa = 2 - alfa;
						}
					} else {
						alfa = 1;
					}
				}

				int a = (int) (a1 + alfa * da);
				int red = (int) (r1 + alfa * dr);
				int green = (int) (g1 + alfa * dg);
				int blue = (int) (b1 + alfa * db);
				pixels[off++] = (a << 24) + (red << 16) + (green << 8) + blue;
			}
			off += adjust;
		}
		return rast;
	}
}
