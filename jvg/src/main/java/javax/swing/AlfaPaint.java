package javax.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.lang.ref.WeakReference;

import javax.swing.gradient.LinearGradientPaint;

import sun.awt.image.IntegerComponentRaster;
import sun.awt.image.IntegerInterleavedRaster;

public class AlfaPaint implements Paint {
	public AlfaPaint(Paint paint1, Paint paint2) {
		if (paint1 == null) {
			paint1 = Color.black;
		}

		if (paint2 == null) {
			paint2 = Color.black;
		}

		boolean isColor1 = paint1 instanceof Color;
		boolean isColor2 = paint2 instanceof Color;

		if (isColor1 || isColor2) {
			if (isColor1 && isColor2) {
				alfa2 = getAlfa((Color) paint2);
				paint2 = null;
			} else {
				if (isColor1) {
					alfa1 = getAlfa((Color) paint1);
					paint1 = null;
				} else if (isColor2) {
					alfa2 = getAlfa((Color) paint2);
					paint2 = null;
				}
			}
		}

		this.paint1 = paint1;
		this.paint2 = paint2;
	}

	private float[] getAlfa(Color color) {
		float[] alfa = new float[4];
		alfa[0] = color.getAlpha() / 255f;
		alfa[1] = color.getRed() / 255f;
		alfa[2] = color.getGreen() / 255f;
		alfa[3] = color.getBlue() / 255f;
		return alfa;
	}

	private Paint paint1, paint2;

	private float[] alfa1, alfa2;

	@Override
	public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds, AffineTransform xform, RenderingHints hints) {
		return new AlfaPaintContext(paint1 != null ? paint1.createContext(cm, deviceBounds, userBounds, xform, hints) : null, paint2 != null ? paint2.createContext(cm, deviceBounds, userBounds, xform, hints) : null, alfa1, alfa2);
	}

	@Override
	public int getTransparency() {
		return TRANSLUCENT;
	}

	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.setContentPane(new JLabel() {
			@Override
			public void paint(Graphics g) {
				super.paint(g);

				float[] dist = { 0.0f, 0.5f, 1.0f };
				Color[] colors = { Color.red, Color.yellow, Color.blue };
				Paint p1 = new LinearGradientPaint(new Point2D.Float(0, 0), new Point2D.Float(getWidth(), getHeight()), dist, colors);
				Paint p2 = new LinearGradientPaint(new Point2D.Float(getWidth(), 0), new Point2D.Float(0, getHeight()), dist, colors);
				Paint p = new AlfaPaint(p1, p2);

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

class AlfaPaintContext implements PaintContext {
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

	private ColorModel model;

	private PaintContext paint1;

	private PaintContext paint2;

	private float[] alfa1, alfa2;

	public AlfaPaintContext(PaintContext paint1, PaintContext paint2, float[] alfa1, float[] alfa2) {
		this.paint1 = paint1;
		this.paint2 = paint2;
		this.alfa1 = alfa1;
		this.alfa2 = alfa2;
		model = ColorModel.getRGBdefault();
	}

	@Override
	public void dispose() {
		if (paint1 != null) {
			paint1.dispose();
		}

		if (paint2 != null) {
			paint2.dispose();
		}

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
		if (paint1 != null && paint2 == null) {
			return getRasterByPaintAndAlfa(paint1, alfa2, x, y, w, h);
		}
		if (paint2 != null && paint1 == null) {
			return getRasterByPaintAndAlfa(paint2, alfa1, x, y, w, h);
		}
		return getRasterByPaintAndPaint(paint1, paint2, x, y, w, h);
	}

	private Raster getRasterByPaintAndAlfa(PaintContext paint, float[] color, int x, int y, int w, int h) {
		IntegerInterleavedRaster raster = (IntegerInterleavedRaster) paint.getRaster(x, y, w, h);
		ColorModel cm = paint.getColorModel();
		boolean hasAlfa = cm.hasAlpha();
		int[] sourcePixels = raster.getDataStorage();
		int sw = raster.getWidth();
		int sh = raster.getWidth();

		Raster rast = model.createCompatibleWritableRaster(w, h); // saved;
		if (rast == null || rast.getWidth() < w || rast.getHeight() < h) {
			rast = getCachedRaster(model, w, h);
			saved = rast;
		}

		IntegerComponentRaster irast = (IntegerComponentRaster) rast;
		int[] pixels = irast.getDataStorage();
		int len = Math.min(sourcePixels.length, pixels.length);

		if (hasAlfa) {
			if (sw == w) {
				for (int i = 0; i < len; i++) {
					int alfa = (int) (((sourcePixels[i] >> 24) & 0xFF) * color[0]);
					int red = (int) (((sourcePixels[i] >> 16) & 0xFF) * color[1]);
					int green = (int) (((sourcePixels[i] >> 8) & 0xFF) * color[2]);
					int blue = (int) (((sourcePixels[i]) & 0xFF) * color[3]);
					pixels[i] = (alfa << 24) + (red << 16) + (green << 8) + blue;
				}
			} else {
				for (int ix = 0; ix < w; ix++) {
					for (int iy = 0; iy < h; iy++) {
						int i = w * iy + ix;
						int si = sw * iy + ix;
						int alfa = (int) (((sourcePixels[si] >> 24) & 0xFF) * color[0]);
						int red = (int) (((sourcePixels[si] >> 16) & 0xFF) * color[1]);
						int green = (int) (((sourcePixels[si] >> 8) & 0xFF) * color[2]);
						int blue = (int) (((sourcePixels[si]) & 0xFF) * color[3]);
						pixels[i] = (alfa << 24) + (red << 16) + (green << 8) + blue;
					}
				}
			}
		} else {
			int alfa = ((int) (255 * color[0]) & 0xFF) << 24;
			if (sw == w) {
				for (int i = 0; i < len; i++) {
					int red = (int) (((sourcePixels[i] >> 16) & 0xFF) * color[1]);
					int green = (int) (((sourcePixels[i] >> 8) & 0xFF) * color[2]);
					int blue = (int) (((sourcePixels[i]) & 0xFF) * color[3]);
					pixels[i] = alfa + (red << 16) + (green << 8) + blue;
				}
			} else {
				for (int ix = 0; ix < w; ix++) {
					for (int iy = 0; iy < h; iy++) {
						int i = w * iy + ix;
						int si = sw * iy + ix;
						int red = (int) (((sourcePixels[si] >> 16) & 0xFF) * color[1]);
						int green = (int) (((sourcePixels[si] >> 8) & 0xFF) * color[2]);
						int blue = (int) (((sourcePixels[si]) & 0xFF) * color[3]);
						pixels[i] = alfa + (red << 16) + (green << 8) + blue;
					}
				}
			}
		}
		return rast;
	}

	private Raster getRasterByPaintAndPaint(PaintContext paint1, PaintContext paint2, int x, int y, int w, int h) {
		IntegerInterleavedRaster raster1 = (IntegerInterleavedRaster) paint1.getRaster(x, y, w, h);
		boolean hasAlfa1 = paint1.getColorModel().hasAlpha();
		int[] pixels1 = raster1.getDataStorage();
		int w1 = raster1.getWidth();
		int h1 = raster1.getWidth();

		IntegerInterleavedRaster raster2 = (IntegerInterleavedRaster) paint2.getRaster(x, y, w, h);
		boolean hasAlfa2 = paint2.getColorModel().hasAlpha();
		int[] pixels2 = raster2.getDataStorage();
		int w2 = raster2.getWidth();
		int h2 = raster2.getWidth();

		int len = Math.min(pixels1.length, pixels2.length);

		Raster rast = model.createCompatibleWritableRaster(w, h); // saved;
		if (rast == null || rast.getWidth() < w || rast.getHeight() < h) {
			rast = getCachedRaster(model, w, h);
			saved = rast;
		}

		IntegerComponentRaster irast = (IntegerComponentRaster) rast;
		int[] pixels = irast.getDataStorage();
		len = Math.min(len, pixels.length);

		if (w1 == w && w2 == w) {
			for (int i = 0; i < len; i++) {
				int alfa1 = hasAlfa1 ? (pixels1[i] >> 24) & 0xFF : 255;
				int alfa2 = hasAlfa2 ? (pixels2[i] >> 24) & 0xFF : 255;
				int alfa = (int) (alfa1 * alfa2 / 255.0);
				int red = (int) (((pixels1[i] >> 16) & 0xFF) * ((pixels2[i] >> 16) & 0xFF) / 255.0);
				int green = (int) (((pixels1[i] >> 8) & 0xFF) * ((pixels2[i] >> 8) & 0xFF) / 255.0);
				int blue = (int) (((pixels1[i]) & 0xFF) * ((pixels2[i]) & 0xFF) / 255.0);
				pixels[i] = (alfa << 24) + (red << 16) + (green << 8) + blue;
			}
		} else {
			for (int ix = 0; ix < w; ix++) {
				for (int iy = 0; iy < h; iy++) {
					int i = w * iy + ix;
					int i1 = w1 * iy + ix;
					int i2 = w2 * iy + ix;
					int alfa1 = hasAlfa1 ? (pixels1[i1] >> 24) & 0xFF : 255;
					int alfa2 = hasAlfa2 ? (pixels2[i2] >> 24) & 0xFF : 255;
					int alfa = (int) (alfa1 * alfa2 / 255.0);
					int red = (int) (((pixels1[i1] >> 16) & 0xFF) * ((pixels2[i2] >> 16) & 0xFF) / 255.0);
					int green = (int) (((pixels1[i1] >> 8) & 0xFF) * ((pixels2[i2] >> 8) & 0xFF) / 255.0);
					int blue = (int) (((pixels1[i1]) & 0xFF) * ((pixels2[i2]) & 0xFF) / 255.0);
					pixels[i] = (alfa << 24) + (red << 16) + (green << 8) + blue;
				}
			}
		}
		return rast;
	}
}
