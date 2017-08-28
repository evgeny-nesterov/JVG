package ru.nest.jvg.resource;

import javax.swing.gradient.RadialGradient;

public class RadialGradientResource extends Resource<RadialGradient> {
	private RadialGradient gradient;

	public RadialGradientResource(RadialGradient gradient) {
		setResource(gradient);
	}

	@Override
	public RadialGradient getResource() {
		return gradient;
	}

	@Override
	public void setResource(RadialGradient gradient) {
		this.gradient = gradient;
	}
}
