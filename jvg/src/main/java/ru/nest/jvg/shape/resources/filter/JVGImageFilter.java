package ru.nest.jvg.shape.resources.filter;

import java.awt.image.RGBImageFilter;

import ru.nest.jvg.shape.resources.JVGResource;

public abstract class JVGImageFilter extends RGBImageFilter implements JVGResource {
	public JVGImageFilter() {
		canFilterIndexColorModel = true;
	}
}
