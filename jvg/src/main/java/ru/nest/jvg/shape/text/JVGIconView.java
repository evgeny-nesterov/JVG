package ru.nest.jvg.shape.text;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.Icon;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.View;

import ru.nest.jvg.resource.Resource;

public class JVGIconView extends View {
	public JVGIconView(Element elem) {
		super(elem);
		AttributeSet attr = elem.getAttributes();
		c = JVGStyleConstants.getIcon(attr);
	}

	@Override
	public void paint(Graphics g, Shape a) {
		Rectangle alloc = a.getBounds();
		c.getResource().paintIcon(getContainer(), g, alloc.x, alloc.y);
	}

	@Override
	public float getPreferredSpan(int axis) {
		switch (axis) {
			case View.X_AXIS:
				return c.getResource().getIconWidth();

			case View.Y_AXIS:
				return c.getResource().getIconHeight();

			default:
				throw new IllegalArgumentException("Invalid axis: " + axis);
		}
	}

	@Override
	public float getAlignment(int axis) {
		switch (axis) {
			case View.Y_AXIS:
				return 1;

			default:
				return super.getAlignment(axis);
		}
	}

	@Override
	public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
		int p0 = getStartOffset();
		int p1 = getEndOffset();
		if ((pos >= p0) && (pos <= p1)) {
			Rectangle r = a.getBounds();
			if (pos == p1) {
				r.x += r.width;
			}
			r.width = 0;
			return r;
		}
		throw new BadLocationException(pos + " not in range " + p0 + "," + p1, pos);
	}

	@Override
	public int viewToModel(float x, float y, Shape a, Position.Bias[] bias) {
		Rectangle alloc = (Rectangle) a;
		if (x < alloc.x + (alloc.width / 2)) {
			bias[0] = Position.Bias.Forward;
			return getStartOffset();
		}
		bias[0] = Position.Bias.Backward;
		return getEndOffset();
	}

	private Resource<Icon> c;
}
