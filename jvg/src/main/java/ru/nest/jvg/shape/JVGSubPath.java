package ru.nest.jvg.shape;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.Set;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.actionarea.JVGActionArea;
import ru.nest.jvg.actionarea.JVGMoveSubPathActionArea;
import ru.nest.jvg.geom.GeomUtil;
import ru.nest.jvg.geom.MutableGeneralPath;

public class JVGSubPath extends JVGPath implements Comparable<JVGSubPath> {
	private double position;

	protected boolean lead = false;

	protected double length;

	public JVGSubPath(double position) {
		this(position, false);
	}

	protected JVGSubPath(double position, boolean lead) {
		super(new MutableGeneralPath());
		this.lead = lead;
		setPosition(position);
		setSelectable(false);
	}

	public double getPosition() {
		return position;
	}

	public void setPosition(double position) {
		this.position = position;
		invalidate();
	}

	@Override
	public int compareTo(JVGSubPath o) {
		if (o != null) {
			if (position < o.position) {
				return -1;
			}
			if (position > o.position) {
				return 1;
			}
		} else {
			return 1;
		}
		return 0;
	}

	public boolean isLead() {
		return lead;
	}

	@Override
	public void validate() {
		if (!isValid()) {
			super.validate();

			length = GeomUtil.getPathLength(getPath());

			if (isSelectable()) {
				JVGComponent[] a = getChilds(JVGMoveSubPathActionArea.class);
				if (a != null && a.length > 0) {
					JVGGroupPath path = (JVGGroupPath) getParent();
					JVGActionArea aa = (JVGActionArea) a[0];
					boolean active = path.getEditMode() == JVGGroupPath.EDIT_MODE__MOVE_SUBPATH && aa.getParent().isFocused();
					aa.setActive(active);
				}
			}
		}
	}

	@Override
	public void transformComponent(AffineTransform transform, Set<JVGShape> locked) {
		// can't be transformed
		// transform is the same as in the parent
	}

	@Override
	public AffineTransform getTransform() {
		return parent instanceof JVGShape ? ((JVGShape) parent).getTransform() : null;
	}

	@Override
	public AffineTransform getInverseTransform() {
		return parent instanceof JVGShape ? ((JVGShape) parent).getInverseTransform() : null;
	}

	@Override
	public void setSelectable(boolean selectable) {
		if (isSelectable() != selectable) {
			super.setSelectable(selectable);
			if (selectable && !lead) {
				add(new JVGMoveSubPathActionArea());
			} else {
				JVGComponent[] a = getChilds(JVGMoveSubPathActionArea.class);
				if (a != null && a.length > 0) {
					remove(a[0]);
				}
			}
		}
	}

	@Override
	protected Shape getSelectionShape() {
		return getTransformedPathShape();
	}
}
