package ru.nest.jvg.action;

import java.awt.event.ActionEvent;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.shape.JVGStyledText;
import ru.nest.jvg.shape.text.JVGStyleConstants;

public class TextBackgroundAction extends JVGAction {
	public TextBackgroundAction(String name, Resource fillResource) {
		super(name);
		this.fillResource = fillResource;
	}

	private Resource fillResource;

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGComponent c = getComponent(e);
		if (c instanceof JVGStyledText) {
			JVGStyledText text = (JVGStyledText) c;
			MutableAttributeSet attr = new SimpleAttributeSet();
			JVGStyleConstants.setBackground(attr, fillResource);
			text.setCurrentCharacterAttributes(attr, false);

			// TODO
			// appendMacrosCode(c.getPane(), "setTextBackground(id, %s);", MacrosCode.ARG_ID, color.getResource().getRGB());
		}
	}
}
