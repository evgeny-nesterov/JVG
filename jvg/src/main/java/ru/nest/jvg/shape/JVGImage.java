package ru.nest.jvg.shape;

import ru.nest.jvg.event.JVGPropertyChangeEvent;
import ru.nest.jvg.resource.ImageResource;
import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.shape.paint.Painter;
import ru.nest.jvg.shape.resources.filter.JVGImageFilter;
import ru.nest.swing.AlfaPaint;
import sun.awt.image.ToolkitImage;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class JVGImage extends JVGShape {
	private int width;

	private int height;

	public JVGImage(Resource<ImageIcon> image) {
		this.image = image;
		this.width = image.getResource().getIconWidth();
		this.height = image.getResource().getIconHeight();
		this.i = image.getResource().getImage();
		setOriginalBounds(true);
		setShape(new Rectangle2D.Double(0, 0, width, height));

		// DEBUG
		// setFilter(new GrayFilter(true, 10));
		// addMouseListener(new JVGMouseAdapter()
		// {
		// public void mousePressed(JVGMouseEvent e)
		// {
		// c += 10;
		// setFilter(new GrayFilter(true, c));
		// }
		// });

		setFill(true);
		setName("image");
	}

	int c = 10;

	public JVGImage(URL url) throws Exception {
		this(new ImageResource<>(url));
	}

	static List<Painter> defaultPainters = null;

	public static List<Painter> getDefaultPainters() {
		if (defaultPainters == null) {
			defaultPainters = new ArrayList<>();
		}

		return defaultPainters;
	}

	private Resource<ImageIcon> image;

	private Image i;

	public Resource<ImageIcon> getImage() {
		return image;
	}

	@Override
	public boolean containsShape(double x, double y) {
		Shape shape = getTransformedShape();
		if (shape != null) {
			return shape.contains(x, y);
		} else {
			return false;
		}
	}

	@Override
	public void paintShape(Graphics2D g) {
		super.paintShape(g);
		paintImage(g);
	}

	public void paintImage(Graphics2D g) {
		if (isAntialias()) {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		}

		g.transform(getTransform());

		Image drawImage = inProcessImage != null ? inProcessImage : i;

		Paint composePaint = getDeepComposePaint();
		if (composePaint == null) {
			g.drawImage(drawImage, 0, 0, null, null);
		} else {
			Paint oldPaint = g.getPaint();

			BufferedImage bufImg;
			if (drawImage instanceof BufferedImage) {
				bufImg = (BufferedImage) drawImage;
			} else if (drawImage instanceof ToolkitImage) {
				bufImg = ((ToolkitImage) drawImage).getBufferedImage();
			} else {
				bufImg = null;
			}

			// TODO: cache texturePaint
			Paint texturePaint = new TexturePaint(bufImg, new Rectangle2D.Double(0, 0, width, height));
			Paint paint = new AlfaPaint(texturePaint, composePaint);
			g.setPaint(paint);
			g.fillRect(0, 0, width, height);

			g.setPaint(oldPaint);
		}

		g.transform(getInverseTransform());

		if (isAntialias()) {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		}
	}

	private JVGImageFilter filter = null;

	public JVGImageFilter getFilter() {
		return filter;
	}

	private Image inProcessImage = null;

	public synchronized void setFilter(JVGImageFilter filter) {
		JVGImageFilter oldValue = this.filter;
		this.filter = filter;
		dispatchEvent(new JVGPropertyChangeEvent(this, "filter", oldValue, filter));

		if (filter != null) {
			ImageProducer prod = new FilteredImageSource(image.getResource().getImage().getSource(), filter);
			prod.addConsumer(new ImageFilter() {
				@Override
				public void setDimensions(int width, int height) {
				}

				@Override
				public void setProperties(Hashtable<?, ?> props) {
				}

				@Override
				public void setColorModel(ColorModel model) {
				}

				@Override
				public void setHints(int hintflags) {
				}

				@Override
				public void setPixels(int x, int y, int w, int h, ColorModel model, byte pixels[], int off, int scansize) {
				}

				@Override
				public void setPixels(int x, int y, int w, int h, ColorModel model, int pixels[], int off, int scansize) {
				}

				@Override
				public void imageComplete(int status) {
					if (inProcessImage != null) {
						i = inProcessImage;
						inProcessImage = null;
					}
					repaint();
				}
			});
			inProcessImage = Toolkit.getDefaultToolkit().createImage(prod);
			repaint();
		} else {
			i = image.getResource().getImage();
			repaint();
		}
	}

	public void coptTo(JVGShape dst) {
		super.copyTo(dst);
		if (dst instanceof JVGImage) {
			JVGImage dstImage = (JVGImage) dst;
			dstImage.setFilter(getFilter());
		}
	}
}
