package javax.swing.gradient;

import java.awt.Color;
import java.awt.geom.AffineTransform;

import ru.nest.jvg.resource.Resource;

public abstract class Gradient {
	public Gradient(float[] fractions, Resource<Color>[] colors, MultipleGradientPaint.CycleMethodEnum cycleMethod) {
		this.fractions = fractions;
		this.colors = colors;
		this.cycleMethod = cycleMethod;
	}

	public static enum GradientUnitsType {
		ABSOLUTE, BOUNDS
	}

	private GradientUnitsType unitsType = GradientUnitsType.BOUNDS;

	public GradientUnitsType getUnitsType() {
		return unitsType;
	}

	public void setUnitsType(GradientUnitsType unitsType) {
		this.unitsType = unitsType;
	}

	private float[] fractions;

	public float[] getFractions() {
		return fractions;
	}

	private Resource<Color>[] colors;

	public Resource<Color>[] getColors() {
		return colors;
	}

	private Color[] c;

	public Color[] convertColors() {
		if (c == null) {
			c = new Color[colors.length];
		}

		for (int i = 0; i < colors.length; i++) {
			c[i] = colors[i].getResource();
		}
		return c;
	}

	private MultipleGradientPaint.CycleMethodEnum cycleMethod;

	public MultipleGradientPaint.CycleMethodEnum getCycleMethod() {
		return cycleMethod;
	}

	private AffineTransform transform;

	public AffineTransform getTransform() {
		return transform;
	}

	public void setTransform(AffineTransform transform) {
		this.transform = transform;
	}
}
