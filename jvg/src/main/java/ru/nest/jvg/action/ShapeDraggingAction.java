package ru.nest.jvg.action;

import java.awt.event.ActionEvent;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.actionarea.MoveMouseListener;
import ru.nest.jvg.shape.JVGShape;

public class ShapeDraggingAction extends JVGAction {
	public ShapeDraggingAction() {
		super("shape-dragging");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGPane pane = getPane(e);
		if (pane != null) {
			JVGComponent focusOwner = pane.getFocusOwner();
			if (focusOwner instanceof JVGShape) {
				JVGShape focusedShape = (JVGShape) focusOwner;
				boolean draggingEnabled = MoveMouseListener.getListener(focusedShape) != null;
				draggingEnabled = !draggingEnabled;

				JVGShape[] shapes = getShapes(e);
				if (shapes != null && shapes.length > 0) {
					for (JVGShape shape : shapes) {
						if (draggingEnabled) {
							MoveMouseListener.install(shape);
						} else {
							MoveMouseListener.uninstall(shape);
						}
					}
				}

				pane.repaint();
			}
		}
	}
}
