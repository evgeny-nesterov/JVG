package javax.swing.gradient;

import java.awt.Color;

import ru.nest.jvg.CommonUtil;
import ru.nest.jvg.resource.Resource;

public class RadialGradient extends Gradient {
	public RadialGradient(float[] fractions, Resource<Color>[] colors, MultipleGradientPaint.CycleMethodEnum cycleMethod, float cx, float cy, float fx, float fy, float r) {
		super(fractions, colors, cycleMethod);
		this.cx = cx;
		this.cy = cy;
		this.fx = fx;
		this.fy = fy;
		this.r = r;
	}

	private float cx;

	public float getCX() {
		return cx;
	}

	private float cy;

	public float getCY() {
		return cy;
	}

	private float fx;

	public float getFX() {
		return fx;
	}

	private float fy;

	public float getFY() {
		return fy;
	}

	private float r;

	public float getR() {
		return r;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (o instanceof RadialGradient) {
			RadialGradient g = (RadialGradient) o;
			if (g.cx != cx || g.cy != cy || g.r != r || g.fx != fx || g.fy != fy || !CommonUtil.equals(g.getFractions(), getFractions()) || !CommonUtil.equals(g.getColors(), getColors()) || g.getCycleMethod() != getCycleMethod()) {
				return false;
			}
			return true;
		}
		return false;
	}
}
