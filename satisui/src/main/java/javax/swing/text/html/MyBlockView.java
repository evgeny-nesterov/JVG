package javax.swing.text.html;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.lang.reflect.Method;

import javax.swing.SizeRequirements;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BoxView;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public class MyBlockView extends BoxView {
	public MyBlockView(Element elem, int axis) {
		super(elem, axis);
	}

	@Override
	public void setParent(View parent) {
		super.setParent(parent);
		if (parent != null) {
			setPropertiesFromAttributes();
		}
	}

	@Override
	protected SizeRequirements calculateMajorAxisRequirements(int axis, SizeRequirements r) {
		if (r == null) {
			r = new SizeRequirements();
		}

		if (!spanSetFromAttributes(axis, r, cssWidth, cssHeight)) {
			r = super.calculateMajorAxisRequirements(axis, r);
		} else {
			SizeRequirements parentR = super.calculateMajorAxisRequirements(axis, null);
			int margin = (axis == X_AXIS) ? getLeftInset() + getRightInset() : getTopInset() + getBottomInset();
			r.minimum -= margin;
			r.preferred -= margin;
			r.maximum -= margin;
			constrainSize(axis, r, parentR);
		}

		return r;
	}

	@Override
	protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) {
		if (r == null) {
			r = new SizeRequirements();
		}

		if (!spanSetFromAttributes(axis, r, cssWidth, cssHeight)) {
			r = super.calculateMinorAxisRequirements(axis, r);
		} else {
			SizeRequirements parentR = super.calculateMinorAxisRequirements(axis, null);
			int margin = (axis == X_AXIS) ? getLeftInset() + getRightInset() : getTopInset() + getBottomInset();
			r.minimum -= margin;
			r.preferred -= margin;
			r.maximum -= margin;
			constrainSize(axis, r, parentR);
		}

		if (axis == X_AXIS) {
			Object o = getAttributes().getAttribute(CSS.Attribute.TEXT_ALIGN);
			if (o != null) {
				String align = o.toString();
				if (align.equals("center")) {
					r.alignment = 0.5f;
				} else if (align.equals("right")) {
					r.alignment = 1.0f;
				} else {
					r.alignment = 0.0f;
				}
			}
		}

		return r;
	}

	boolean isPercentage(int axis, AttributeSet a) {
		if (axis == X_AXIS) {
			if (cssWidth != null) {
				return cssWidth.isPercentage();
			}
		} else {
			if (cssHeight != null) {
				return cssHeight.isPercentage();
			}
		}

		return false;
	}

	static boolean spanSetFromAttributes(int axis, SizeRequirements r, CSS.LengthValue cssWidth, CSS.LengthValue cssHeight) {
		if (axis == X_AXIS) {
			if ((cssWidth != null) && (!cssWidth.isPercentage())) {
				r.minimum = r.preferred = r.maximum = (int) cssWidth.getValue();
				return true;
			}
		} else {
			if ((cssHeight != null) && (!cssHeight.isPercentage())) {
				r.minimum = r.preferred = r.maximum = (int) cssHeight.getValue();
				return true;
			}
		}

		return false;
	}

	@Override
	protected void layoutMinorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
		int n = getViewCount();
		Object key = (axis == X_AXIS) ? CSS.Attribute.WIDTH : CSS.Attribute.HEIGHT;
		for (int i = 0; i < n; i++) {
			View v = getView(i);
			int min = (int) v.getMinimumSpan(axis);
			int max;

			AttributeSet a = v.getAttributes();
			Object lv = a.getAttribute(key);

			boolean isPercentage = false;
			if (lv != null) {
				try {
					Method m = lv.getClass().getMethod("isPercentage", new Class[] {});
					Boolean is = (Boolean) m.invoke(lv, new Object[] {});
					if (is != null) {
						isPercentage = is;
					}
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}

			if ((lv != null) && isPercentage) {
				try {
					Method m = lv.getClass().getMethod("getValue", new Class[] { Float.class });
					Float f = (Float) m.invoke(lv, new Object[] { targetSpan });
					if (f != null) {
						min = Math.max(f.intValue(), min);
					}
				} catch (Exception exc) {
					exc.printStackTrace();
				}
				max = min;
			} else {
				max = (int) v.getMaximumSpan(axis);
			}

			if (max < targetSpan) {
				float align = v.getAlignment(axis);
				offsets[i] = (int) ((targetSpan - max) * align);
				spans[i] = max;
			} else {
				offsets[i] = 0;
				spans[i] = Math.max(min, targetSpan);
			}
		}
	}

	@Override
	public void paint(Graphics g, Shape allocation) {
		Rectangle a = (Rectangle) allocation;
		painter.paint(g, a.x, a.y, a.width, a.height, this);
		super.paint(g, a);
	}

	@Override
	public AttributeSet getAttributes() {
		if (attr == null) {
			StyleSheet sheet = getStyleSheet();
			attr = sheet.getViewAttributes(this);
		}
		return attr;
	}

	@Override
	public int getResizeWeight(int axis) {
		switch (axis) {
			case View.X_AXIS:
				return 1;

			case View.Y_AXIS:
				return 0;

			default:
				throw new IllegalArgumentException("Invalid axis: " + axis);
		}
	}

	@Override
	public float getAlignment(int axis) {
		switch (axis) {
			case View.X_AXIS:
				return 0;

			case View.Y_AXIS:
				if (getViewCount() == 0) {
					return 0;
				}
				float span = getPreferredSpan(View.Y_AXIS);
				View v = getView(0);
				float above = v.getPreferredSpan(View.Y_AXIS);
				float a = (((int) span) != 0) ? (above * v.getAlignment(View.Y_AXIS)) / span : 0;
				return a;

			default:
				throw new IllegalArgumentException("Invalid axis: " + axis);
		}
	}

	@Override
	public void changedUpdate(DocumentEvent changes, Shape a, ViewFactory f) {
		super.changedUpdate(changes, a, f);
		int pos = changes.getOffset();
		if (pos <= getStartOffset() && (pos + changes.getLength()) >= getEndOffset()) {
			setPropertiesFromAttributes();
		}
	}

	@Override
	public float getPreferredSpan(int axis) {
		return super.getPreferredSpan(axis);
	}

	@Override
	public float getMinimumSpan(int axis) {
		return super.getMinimumSpan(axis);
	}

	@Override
	public float getMaximumSpan(int axis) {
		return super.getMaximumSpan(axis);
	}

	protected void setPropertiesFromAttributes() {
		StyleSheet sheet = getStyleSheet();
		attr = sheet.getViewAttributes(this);

		painter = sheet.getBoxPainter(attr);
		if (attr != null) {
			setInsets((short) painter.getInset(TOP, this), (short) painter.getInset(LEFT, this), (short) painter.getInset(BOTTOM, this), (short) painter.getInset(RIGHT, this));
		}

		cssWidth = (CSS.LengthValue) attr.getAttribute(CSS.Attribute.WIDTH);
		cssHeight = (CSS.LengthValue) attr.getAttribute(CSS.Attribute.HEIGHT);
	}

	protected StyleSheet getStyleSheet() {
		HTMLDocument doc = (HTMLDocument) getDocument();
		return doc.getStyleSheet();
	}

	private void constrainSize(int axis, SizeRequirements want, SizeRequirements min) {
		if (min.minimum > want.minimum) {
			want.minimum = want.preferred = min.minimum;
			want.maximum = Math.max(want.maximum, min.maximum);
		}
	}

	private AttributeSet attr;

	private StyleSheet.BoxPainter painter;

	private CSS.LengthValue cssWidth;

	private CSS.LengthValue cssHeight;
}
