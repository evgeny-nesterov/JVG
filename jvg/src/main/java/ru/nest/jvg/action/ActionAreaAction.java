package ru.nest.jvg.action;

import java.awt.event.ActionEvent;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.actionarea.JVGActionArea;
import ru.nest.jvg.shape.JVGShape;

public class ActionAreaAction<A extends JVGActionArea> extends JVGAction {
	public ActionAreaAction(Class<A> type) {
		super("action-area");
		this.type = type;
	}

	private Class<A> type;

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGPane pane = getPane(e);
		if (pane != null) {
			JVGComponent focusOwner = pane.getFocusOwner();
			if (focusOwner instanceof JVGShape) {
				JVGShape focusedShape = (JVGShape) focusOwner;
				JVGComponent[] areas = focusedShape.getChilds(type);
				if (areas != null) {
					boolean visible = false;
					for (JVGComponent area : areas) {
						if (area.isVisible()) {
							visible = true;
							break;
						}
					}

					visible = !visible;

					JVGShape[] shapes = getShapes(e);
					if (shapes != null && shapes.length > 0) {
						for (JVGShape shape : shapes) {
							areas = shape.getChilds(type);
							for (JVGComponent area : areas) {
								area.setVisible(visible);
							}
						}
					}
					pane.repaint();
				}
			}
		}
	}
}
