package ru.nest.jvg.editor;

import ru.nest.jvg.JVGPane;
import ru.nest.jvg.shape.JVGShape;

public interface ShapeCreator {
	JVGShape create(JVGPane pane);
}
