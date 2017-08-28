package ru.nest.jvg.resource;

import java.awt.geom.AffineTransform;

public class TransformResource extends Resource<AffineTransform> {
	public TransformResource(AffineTransform transform) {
		setResource(transform);
	}

	private AffineTransform transform;

	@Override
	public AffineTransform getResource() {
		return transform;
	}

	@Override
	public void setResource(AffineTransform transform) {
		this.transform = transform;
	}
}
