package ru.nest.jvg.shape.paint;

import java.awt.Color;

import javax.swing.gradient.MultipleGradientPaint;

import ru.nest.jvg.CommonUtil;
import ru.nest.jvg.resource.Resource;

public abstract class GradientDraw extends AbstractDraw {
	public GradientDraw(float[] fractions, Resource<Color>[] colors, MultipleGradientPaint.CycleMethodEnum cycleMethod) {
		this.fractions = fractions;
		this.colors = colors;
		this.cycleMethod = cycleMethod;
	}

	protected float[] fractions;

	public float[] getFractions() {
		return fractions;
	}

	public void setFractions(float[] fractions) {
		if (!CommonUtil.equals(fractions, getFractions())) {
			this.fractions = fractions;
			paint = null;
		}
	}

	protected Resource<Color>[] colors;

	public Resource<Color>[] getColors() {
		return colors;
	}

	public void setColors(Resource<Color>[] colors) {
		if (!CommonUtil.equals(colors, getColors())) {
			this.colors = colors;
			paint = null;
		}
	}

	private MultipleGradientPaint.CycleMethodEnum cycleMethod;

	public MultipleGradientPaint.CycleMethodEnum getCycleMethod() {
		return cycleMethod;
	}

	public void setCycleMethod(MultipleGradientPaint.CycleMethodEnum cycleMethod) {
		this.cycleMethod = cycleMethod;
		paint = null;
	}

	protected transient MultipleGradientPaint paint;
}
