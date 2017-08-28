package ru.nest.jvg.action;

import java.awt.event.ActionEvent;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.shape.JVGShape;

public class InitialBoundsAction extends JVGAction {
	public InitialBoundsAction() {
		super("original-bounds");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGPane pane = getPane(e);
		if (pane != null) {
			JVGComponent focusOwner = pane.getFocusOwner();
			if (focusOwner instanceof JVGShape) {
				JVGShape focusedShape = (JVGShape) focusOwner;
				boolean isOriginalBounds = !focusedShape.isOriginalBounds();

				JVGShape[] shapes = getShapes(e);
				if (shapes != null && shapes.length > 0) {
					for (JVGShape shape : shapes) {
						if (shape.isOriginalBounds() != isOriginalBounds) {
							shape.setOriginalBounds(isOriginalBounds);
							shape.invalidate();
						}
					}
					pane.repaint();
				}
			}
		}
	}
}
