package javax.swing.gradient;

import java.awt.Color;

import ru.nest.jvg.resource.Resource;
import ru.nest.util.CommonUtil;

public class LinearGradient extends Gradient {
	public LinearGradient(float[] fractions, Resource<Color>[] colors, MultipleGradientPaint.CycleMethodEnum cycleMethod, float x1, float y1, float x2, float y2) {
		super(fractions, colors, cycleMethod);
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}

	private float x1;

	public float getX1() {
		return x1;
	}

	private float y1;

	public float getY1() {
		return y1;
	}

	private float x2;

	public float getX2() {
		return x2;
	}

	private float y2;

	public float getY2() {
		return y2;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (o instanceof LinearGradient) {
			LinearGradient g = (LinearGradient) o;
			if (g.x1 != x1 || g.y1 != y1 || g.x2 != x2 || g.y2 != y2 || !CommonUtil.equals(g.getFractions(), getFractions()) || !CommonUtil.equals(g.getColors(), getColors()) || g.getCycleMethod() != getCycleMethod()) {
				return false;
			}
			return true;
		}
		return false;
	}
}
