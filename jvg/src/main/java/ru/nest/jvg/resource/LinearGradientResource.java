package ru.nest.jvg.resource;

import javax.swing.gradient.LinearGradient;

public class LinearGradientResource extends Resource<LinearGradient> {
	private LinearGradient gradient;

	public LinearGradientResource(LinearGradient gradient) {
		setResource(gradient);
	}

	@Override
	public LinearGradient getResource() {
		return gradient;
	}

	@Override
	public void setResource(LinearGradient gradient) {
		this.gradient = gradient;
	}
}
