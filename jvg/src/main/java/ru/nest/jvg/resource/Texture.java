package ru.nest.jvg.resource;

import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class Texture {
	public Texture(Resource<? extends Icon> image) {
		this(image, new Rectangle(0, 0, image.getResource().getIconWidth(), image.getResource().getIconHeight()));
	}

	public Texture(Resource<? extends Icon> image, Rectangle2D anchor) {
		setImage(image);
		setAnchor(anchor);
	}

	private Resource<? extends Icon> image;

	public Resource<? extends Icon> getImage() {
		return image;
	}

	public void setImage(Resource<? extends Icon> image) {
		this.image = image;
		paint = null;
	}

	private Rectangle2D anchor;

	public void setAnchor(Rectangle2D anchor) {
		if (anchor == null && image != null && image.getResource() != null) {
			int w = image.getResource().getIconWidth();
			int h = image.getResource().getIconHeight();
			anchor = new Rectangle2D.Double(0, 0, w, h);
		}

		this.anchor = anchor;
		paint = null;
	}

	public double getAnchorX() {
		return anchor != null ? anchor.getX() : 0;
	}

	public double getAnchorY() {
		return anchor != null ? anchor.getY() : 0;
	}

	public double getAnchorWidth() {
		return anchor.getWidth();
	}

	public double getAnchorHeight() {
		return anchor.getHeight();
	}

	public boolean hasAnchor() {
		if (anchor == null || anchor.getX() != 0 || anchor.getY() != 0) {
			return false;
		}

		if (image != null && image.getResource() != null) {
			int w = image.getResource().getIconWidth();
			int h = image.getResource().getIconHeight();
			return anchor.getWidth() != w && anchor.getHeight() != h;
		}
		return true;
	}

	private TexturePaint paint;

	public TexturePaint getPaint() {
		if (paint == null && image != null) {
			Icon img = image.getResource();
			if (img != null) {
				int w = img.getIconWidth();
				int h = img.getIconHeight();

				BufferedImage bi;
				if (img instanceof ImageIcon && ((ImageIcon) img).getImage() instanceof BufferedImage) {
					bi = (BufferedImage) ((ImageIcon) img).getImage();
				} else {
					bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
					img.paintIcon(null, bi.getGraphics(), 0, 0);
				}
				paint = new TexturePaint(bi, anchor);
			}
		}
		return paint;
	}
}
