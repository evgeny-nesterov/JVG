package javax.swing.text.html;

import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public class MyHTMLEditorKit extends HTMLEditorKit {
	private static final long serialVersionUID = 1L;

	public MyHTMLEditorKit(boolean ignoreImageMinimum) {
		this.ignoreImageMinimum = ignoreImageMinimum;
		// setStyleSheet(new StyleSheet());
	}

	private boolean ignoreImageMinimum;

	@Override
	public ViewFactory getViewFactory() {
		return defaultFactory;
	}

	private ViewFactory defaultFactory = new MyHTMLFactory();

	class MyHTMLFactory extends HTMLFactory {
		@Override
		public View create(Element elem) {
			Object o = elem.getAttributes().getAttribute(StyleConstants.NameAttribute);
			if (o instanceof HTML.Tag) {
				HTML.Tag kind = (HTML.Tag) o;
				if ((kind == HTML.Tag.UL) || (kind == HTML.Tag.OL)) {
					return new MyListView(elem);
				} else if (kind == HTML.Tag.IMG) {
					return new ImageView(elem) {
						@Override
						public float getMinimumSpan(int axis) {
							return ignoreImageMinimum ? 0 : super.getMinimumSpan(axis);
						}
					};
				}
				// else if ((kind == HTML.Tag.LI) ||
				// (kind == HTML.Tag.CENTER) ||
				// (kind == HTML.Tag.DL) ||
				// (kind == HTML.Tag.DD) ||
				// (kind == HTML.Tag.DIV) ||
				// (kind == HTML.Tag.BLOCKQUOTE) ||
				// (kind == HTML.Tag.PRE) ||
				// (kind == HTML.Tag.FORM))
				// {
				// return new MyBlockView(elem, View.Y_AXIS);
				// }
			}

			return super.create(elem);
		}
	};
}
