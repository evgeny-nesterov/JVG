package ru.nest.jvg.action;

import java.awt.event.ActionEvent;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.shape.JVGStyledText;
import ru.nest.jvg.shape.text.JVGStyleConstants;

public class TextForegroundAction extends JVGAction {
	public TextForegroundAction(String name, Resource foregroundFiller) {
		super(name);
		this.foregroundFiller = foregroundFiller;
	}

	private Resource foregroundFiller;

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGComponent c = getComponent(e);
		if (c instanceof JVGStyledText) {
			JVGStyledText text = (JVGStyledText) c;
			MutableAttributeSet attr = new SimpleAttributeSet();
			JVGStyleConstants.setForeground(attr, foregroundFiller);
			text.setCurrentCharacterAttributes(attr, false);

			// TODO
			// appendMacrosCode(c.getPane(), "setTextForeground(id, %s);", MacrosCode.ARG_ID, foregroundFiller.getResource().getRGB());
		}
	}
}
