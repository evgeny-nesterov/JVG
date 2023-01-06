package ru.nest.jvg.shape.paint;

import ru.nest.jvg.shape.JVGShape;

public interface PainterFilter {
	boolean pass(JVGShape c, Painter painter);
}
