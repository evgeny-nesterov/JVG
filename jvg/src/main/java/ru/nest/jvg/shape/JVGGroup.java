package ru.nest.jvg.shape;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Set;

import ru.nest.jvg.Filter;
import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.actionarea.JVGActionArea;
import ru.nest.jvg.actionarea.JVGCustomActionArea;
import ru.nest.jvg.geom.MutableGeneralPath;
import ru.nest.jvg.shape.paint.Painter;
import ru.nest.jvg.shape.paint.PainterFilter;

/**
 * Can't have original bounds
 */
public class JVGGroup extends JVGShape {
	// default
	public final static int PAINT_ORDER_COMPONENT = 0;

	public final static int PAINT_ORDER_OUTLINE_FIRST = 1;

	public final static int PAINT_ORDER_FILL_FIRST = 2;

	private boolean locked = true;

	private int paintOrderType = PAINT_ORDER_COMPONENT;

	public JVGGroup() {
		setName("Group");
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	private final static Filter actionFilter = new Filter() {
		@Override
		public boolean pass(JVGComponent component) {
			return component.isVisible() && component instanceof JVGActionArea && !(component instanceof JVGCustomActionArea);
		}
	};

	private final static Filter controlFilter = new Filter() {
		@Override
		public boolean pass(JVGComponent component) {
			return component.isVisible() && component instanceof JVGCustomActionArea;
		}
	};

	@Override
	public JVGComponent getComponent(double x, double y) {
		if (locked) {
			// get controls only
			JVGComponent control = super.getDeepestComponent(x, y, controlFilter);
			if (control != null) {
				return control;
			}

			// get actions only
			JVGComponent c = super.getComponent(x, y, actionFilter);
			if (c != null) {
				return c;
			} else {
				return this;
			}
		} else {
			return super.getComponent(x, y);
		}
	}

	@Override
	public void setSelected(boolean selected, boolean clear) {
		if (locked) {
			super.setSelected(selected, clear);
		}
	}

	@Override
	public boolean contains(double x, double y, Filter filter) {
		return containsChilds(x, y, filter);
	}

	/**
	 * As in the container
	 */
	@Override
	protected Shape computeInitialBounds() {
		return getShape();
	}

	private Shape shape;

	protected Shape computeShape() {
		Shape shape = computeChildsBounds();
		AffineTransform inverseTransform = getInverseTransform();
		if (!inverseTransform.isIdentity()) {
			return inverseTransform.createTransformedShape(shape);
		} else {
			return shape;
		}
	}

	@Override
	public Shape getShape() {
		if (shape == null) {
			validate();
		}
		return shape;
	}

	@Override
	public void paintShape(Graphics2D g) {
		// group is transparent
	}

	@Override
	public void paintChildren(Graphics2D g) {
		switch (paintOrderType) {
			case PAINT_ORDER_COMPONENT:
				super.paintChildren(g);
				break;
			case PAINT_ORDER_FILL_FIRST:
			case PAINT_ORDER_OUTLINE_FIRST:
				final PainterFilter f1 = new PainterFilter() {
					@Override
					public boolean pass(JVGShape c, Painter painter) {
						return painter.getType() == (paintOrderType == PAINT_ORDER_OUTLINE_FIRST ? Painter.OUTLINE : Painter.FILL);
					}
				};

				int childs_count = this.childrenCount;
				if (childs_count > 0) {
					JVGComponent[] childs = this.children;
					for (int i = 0; i < childs_count; i++) {
						JVGComponent c = childs[i];
						if (c.isVisible()) {
							if (c instanceof JVGGroupPath) {
								JVGGroupPath shape = (JVGGroupPath) c;
								shape.setPaintOrderType(paintOrderType);
								shape.setPainterFilter(f1);
							} else if (c instanceof JVGShape) {
								JVGShape shape = (JVGShape) c;
								shape.setPainterFilter(f1);
							}
						}
					}
				}

				super.paintChildren(g);

				if (childs_count > 0) {
					PainterFilter f2 = new PainterFilter() {
						@Override
						public boolean pass(JVGShape c, Painter painter) {
							return !f1.pass(c, painter);
						}
					};

					JVGComponent[] childs = this.children;
					for (int i = 0; i < childs_count; i++) {
						JVGComponent c = childs[i];
						if (c.isVisible() && c instanceof JVGShape) {
							JVGShape shape = (JVGShape) c;
							shape.setPainterFilter(f2);
						}
					}
				}
				super.paintChildren(g);
				break;
		}
	}

	@Override
	public void selectChilds(Shape shape, Set<JVGComponent> selection) {
		if (!locked) {
			super.selectChilds(shape, selection);
		}
	}

	@Override
	public void validate() {
		if (!isValid()) {
			shape = computeShape();
			if (shape == null) {
				shape = new MutableGeneralPath();
			} else {
				shape = new MutableGeneralPath(shape);
			}
			super.validate();
		}
	}

	@Override
	public void invalidate() {
		if (isValid()) {
			super.invalidate();
			shape = null;
		}
	}

	@Override
	public MutableGeneralPath getPath() {
		MutableGeneralPath path = new MutableGeneralPath();

		int childs_count = this.childrenCount;
		JVGComponent[] childs = this.children;
		for (int i = 0; i < childs_count; i++) {
			if (childs[i] instanceof JVGShape) {
				JVGShape child = (JVGShape) childs[i];
				MutableGeneralPath shape = child.getPath();
				shape.transform(child.getTransform());
				shape.transform(getInverseTransform());
				path.append(shape, false);
			}
		}
		return path;
	}

	@Override
	protected boolean isContainedIn(Shape selectionShape) {
		if (selectionShape instanceof Rectangle2D) {
			return super.isContainedIn(selectionShape);
		} else {
			int childs_count = this.childrenCount;
			JVGComponent[] childs = this.children;
			for (int i = 0; i < childs_count; i++) {
				if (childs[i] instanceof JVGShape) {
					JVGShape child = (JVGShape) childs[i];
					if (!child.isContainedIn(selectionShape)) {
						return false;
					}
				}
			}
			return true;
		}
	}

	@Override
	public void setAntialias(boolean antialias) {
		super.setAntialias(antialias);
		int childs_count = this.childrenCount;
		JVGComponent[] childs = this.children;
		for (int i = 0; i < childs_count; i++) {
			if (childs[i] instanceof JVGShape) {
				JVGShape child = (JVGShape) childs[i];
				child.setAntialias(antialias);
			}
		}
	}

	public int getPaintOrderType() {
		return paintOrderType;
	}

	public void setPaintOrderType(int paintOrderType) {
		this.paintOrderType = paintOrderType;
		invalidate();
	}
}
