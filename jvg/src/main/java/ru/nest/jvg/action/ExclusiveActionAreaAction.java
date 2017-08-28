package ru.nest.jvg.action;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.actionarea.JVGScaleActionArea;
import ru.nest.jvg.actionarea.JVGShearActionArea;
import ru.nest.jvg.actionarea.JVGVectorActionArea;
import ru.nest.jvg.macros.JVGMacrosCode;
import ru.nest.jvg.shape.JVGShape;

public class ExclusiveActionAreaAction extends JVGAction {
	public static int NONE = 0;

	public static int SCALE = 1;

	public static int SHEAR = 2;

	public static int VECTOR = 3;

	private final static Map<Class<?>, Integer> areaClasses = new HashMap<Class<?>, Integer>();
	static {
		areaClasses.put(JVGScaleActionArea.class, SCALE);
		areaClasses.put(JVGShearActionArea.class, SHEAR);
		areaClasses.put(JVGVectorActionArea.class, VECTOR);
	}

	public ExclusiveActionAreaAction(int type) {
		super("exclusive-action-area");
		this.type = type;
	}

	public ExclusiveActionAreaAction(int type, JVGShape... shapes) {
		super("exclusive-action-area");
		this.shapes = shapes;
		this.type = type;
	}

	private int type;

	private JVGShape[] shapes;

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGShape[] shapes = this.shapes == null ? getShapes(e) : this.shapes;
		if (shapes != null && shapes.length > 0) {
			JVGPane pane = getPane(e);
			for (JVGShape shape : shapes) {
				for (JVGComponent c : shape.getChildren()) {
					if (areaClasses.containsKey(c.getClass())) {
						int currentType = areaClasses.get(c.getClass());
						c.setVisible(currentType == type);
					}
				}
			}
			pane.repaint();

			appendMacrosCode(pane, "setActionArea(id, getFocus(), %s);", JVGMacrosCode.ARG_ID, type);
		}
	}
}
