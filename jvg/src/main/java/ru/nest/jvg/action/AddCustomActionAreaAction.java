package ru.nest.jvg.action;

import java.awt.event.ActionEvent;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGContainer;
import ru.nest.jvg.actionarea.JVGCustomActionArea;
import ru.nest.jvg.resource.Script;
import ru.nest.jvg.resource.ScriptResource;

public class AddCustomActionAreaAction extends JVGAction {
	public final static String PROPERTY_TYPE = "custom-action-area-runtime-context";

	public AddCustomActionAreaAction() {
		super("add-custom-action-area");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGComponent component = getComponent(e);
		if (component instanceof JVGContainer) {
			JVGContainer container = (JVGContainer) component;

			JVGCustomActionArea a = new JVGCustomActionArea();
			a.setActive(true);

			String scriptValidate = "long pid = jvg.getParent(jvg.id);\n"; // on default
			scriptValidate += "jvg.setProperty(jvg.id, \"" + PROPERTY_TYPE + "\", \"x\", \"\" + (jvg.getX(pid) + jvg.getWidth(pid) / 2));\n";
			scriptValidate += "jvg.setProperty(jvg.id, \"" + PROPERTY_TYPE + "\", \"y\", \"\" + (jvg.getY(pid) + jvg.getHeight(pid) / 2));";
			a.setClientProperty(Script.VALIDATION.getActionName(), new ScriptResource(scriptValidate));

			String scriptMouseDragged = "long pid = jvg.getParent(jvg.id);\n";
			scriptMouseDragged += "jvg.translate(pid, jvg.adjusteddx, jvg.adjusteddy);\n";
			scriptMouseDragged += "jvg.repaint();";
			a.setClientProperty(Script.MOUSE_DRAGGED.getActionName(), new ScriptResource(scriptMouseDragged));

			String scriptPaint = "int x = jvg.getX(jvg.id);\n";
			scriptPaint += "int y = jvg.getY(jvg.id);\n";
			scriptPaint += "int w = jvg.getWidth(jvg.id);\n";
			scriptPaint += "int h = jvg.getHeight(jvg.id);\n";
			scriptPaint += "g.setColor(255, 255, 0);\n";
			scriptPaint += "g.fillRect(x, y, w, h);\n";
			scriptPaint += "g.setColor(0, 0, 0);\n";
			scriptPaint += "g.drawRect(x, y, w, h);\n";
			a.setClientProperty(Script.PAINT.getActionName(), new ScriptResource(scriptPaint));

			container.add(a);
			container.repaint();
		}
	}
}
