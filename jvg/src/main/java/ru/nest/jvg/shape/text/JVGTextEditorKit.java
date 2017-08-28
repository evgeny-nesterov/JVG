package ru.nest.jvg.shape.text;

import javax.swing.text.StyledEditorKit;
import javax.swing.text.ViewFactory;

import ru.nest.jvg.shape.JVGStyledText;

public class JVGTextEditorKit extends StyledEditorKit {
	private JVGStyledText text;

	public JVGTextEditorKit(JVGStyledText text) {
		this.text = text;
		viewFactory = new JVGViewFactory(text);
	}

	@Override
	public Object clone() {
		return new JVGTextEditorKit(text);
	}

	private final ViewFactory viewFactory;

	@Override
	public ViewFactory getViewFactory() {
		return viewFactory;
	}
}
