package ru.nest.jvg.shape.text;

import javax.swing.text.AbstractDocument;
import javax.swing.text.Element;
import javax.swing.text.GlyphView;
import javax.swing.text.GlyphView.GlyphPainter;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

import ru.nest.jvg.shape.JVGStyledText;

public class JVGViewFactory implements ViewFactory {
	private JVGStyledText text;

	public JVGViewFactory(JVGStyledText text) {
		this.text = text;
		glyphPainter = new JVGGlyphPainter(text);
	}

	private GlyphPainter glyphPainter;

	@Override
	public View create(Element elem) {
		View v = null;
		String kind = elem.getName();
		if (kind != null) {
			if (kind.equals(AbstractDocument.ContentElementName)) {
				v = new JVGLabelView(elem);
			} else if (kind.equals(AbstractDocument.ParagraphElementName)) {
				v = new JVGParagraphView(elem);
			} else if (kind.equals(AbstractDocument.SectionElementName)) {
				v = new JVGBoxView(elem, View.Y_AXIS);
			} else if (kind.equals(JVGStyleConstants.IconElementName)) {
				v = new JVGIconView(elem);
			}
		}

		if (v == null) {
			v = new JVGLabelView(elem);
		}

		if (v instanceof JVGLabelView) {
			GlyphView gv = (GlyphView) v;
			gv.setGlyphPainter(glyphPainter);
		}
		return v;
	}
}
