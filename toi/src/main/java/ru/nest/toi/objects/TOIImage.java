package ru.nest.toi.objects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.ImageIcon;

import ru.nest.toi.TOIPaintContext;

public class TOIImage extends TOIShape {
	private ImageIcon icon;

	private byte[] data;

	private String descr;

	public TOIImage() {
	}

	@Override
	public void paintShape(Graphics2D g, Graphics2D gt, TOIPaintContext ctx) {
		Color color = ctx.getColorRenderer().getColor(this, null);
		if (color != null) {
			gt.setColor(color);
			gt.fillRect(0, 0, icon.getIconWidth(), icon.getIconHeight());
		}
		icon.paintIcon(null, gt, 0, 0);
	}

	public ImageIcon getIcon() {
		return icon;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data, String descr) {
		this.data = data;
		icon = new ImageIcon(data, descr);
		invalidate();
	}

	public String getDescr() {
		return descr;
	}

	@Override
	public void validate() {
		if (!isValid()) {
			originalBounds = new Rectangle(0, 0, icon.getIconWidth(), icon.getIconHeight());
			shape = getTransform().createTransformedShape(originalBounds);
			bounds = shape;
		}
		super.validate();
	}
}
