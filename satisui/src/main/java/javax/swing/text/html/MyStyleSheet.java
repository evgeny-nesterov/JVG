package javax.swing.text.html;

import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;

public class MyStyleSheet extends StyleSheet {
	private static final long serialVersionUID = 1L;
	private CSS css;

	public MyStyleSheet() {
		css = new CSS();
	}

	@Override
	public AttributeSet getViewAttributes(View v) {
		return new MyViewAttributeSet(v);
	}

	class MyViewAttributeSet extends ViewAttributeSet {
		private static final long serialVersionUID = 1L;

		MyViewAttributeSet(View v) {
			super(v);
		}

		@Override
		public Object getAttribute(Object key) {
			if (key instanceof StyleConstants) {
				Object cssKey = css.styleConstantsKeyToCSSKey((StyleConstants) key);
				if (cssKey != null) {
					Object value = doGetAttribute(cssKey);
					if (value instanceof CSS.CssValue) {
						return ((CSS.CssValue) value).toStyleConstants((StyleConstants) key, host);
					}
				}
			}
			return doGetAttribute(key);
		}

		@Override
		Object doGetAttribute(Object key) {
			Object retValue = super.getAttribute(key);
			if (retValue != null) {
				return retValue;
			}

			if (key instanceof CSS.Attribute) {
				CSS.Attribute css = (CSS.Attribute) key;
				if (css.isInherited()) {
					AttributeSet parent = getResolveParent();
					if (parent != null) {
						return parent.getAttribute(key);
					}
				}
			}
			return null;
		}
	}
}
