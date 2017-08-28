package javax.swing.text.html;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.View;

public class MyListView extends BlockView // MyBlockView
{
	private ListPainter painter;

	public MyListView(Element elem) {
		super(elem, View.Y_AXIS);
	}

	@Override
	public float getAlignment(int axis) {
		switch (axis) {
			case View.X_AXIS:
				return 0.5f;

			case View.Y_AXIS:
				return 0.5f;

			default:
				throw new IllegalArgumentException("Invalid axis: " + axis);
		}
	}

	@Override
	public void paint(Graphics g, Shape allocation) {
		super.paint(g, allocation);
		Rectangle alloc = allocation.getBounds();
		alloc.x += 50;

		Rectangle clip = g.getClipBounds();
		if ((clip.x + clip.width) < (alloc.x + getLeftInset())) {
			Rectangle childRect = alloc;
			alloc = getInsideAllocation(allocation);
			int n = getViewCount();
			int endY = clip.y + clip.height;
			for (int i = 0; i < n; i++) {
				childRect.setBounds(alloc);
				childAllocation(i, childRect);
				if (childRect.y < endY) {
					if ((childRect.y + childRect.height) >= clip.y) {
						painter.paint(g, childRect.x, childRect.y, childRect.width, childRect.height, this, i);
					}
				} else {
					break;
				}
			}
		}
	}

	@Override
	protected void paintChild(Graphics g, Rectangle alloc, int index) {
		painter.paint(g, alloc.x, alloc.y, alloc.width, alloc.height, this, index);
		super.paintChild(g, alloc, index);
	}

	@Override
	protected void setPropertiesFromAttributes() {
		super.setPropertiesFromAttributes();
		painter = new ListPainter(getAttributes(), getStyleSheet());
	}

	public static class ListPainter implements Serializable {
		private static final long serialVersionUID = 1L;

		public ListPainter(AttributeSet attr, StyleSheet ss) {
			this.ss = ss;
			String imgstr = (String) attr.getAttribute(CSS.Attribute.LIST_STYLE_IMAGE);
			type = null;
			if (imgstr != null && !imgstr.equals("none")) {
				String tmpstr = null;
				try {
					StringTokenizer st = new StringTokenizer(imgstr, "()");
					if (st.hasMoreTokens()) {
						tmpstr = st.nextToken();
					}

					if (st.hasMoreTokens()) {
						tmpstr = st.nextToken();
					}

					URL u = new URL(tmpstr);
					img = new ImageIcon(u);
				} catch (MalformedURLException e) {
					if (tmpstr != null && ss != null && ss.getBase() != null) {
						try {
							URL u = new URL(ss.getBase(), tmpstr);
							img = new ImageIcon(u);
						} catch (MalformedURLException murle) {
							img = null;
						}
					} else {
						img = null;
					}
				}
			}

			if (img == null) {
				Object o = attr.getAttribute(CSS.Attribute.LIST_STYLE_TYPE);
				if (o != null) {
					type = o.toString();
				}
			}
			start = 1;

			paintRect = new Rectangle();
		}

		static HashMap<String, Integer> shapes = new HashMap<String, Integer>();

		static String[] shapes_array = { "disc", "circle", "square" };
		static {
			shapes.put("disc", 0);
			shapes.put("circle", 1);
			shapes.put("square", 2);
		}

		public static boolean isShape(String type) {
			return shapes.containsKey(type);
		}

		public static int getShapeIndex(String type) {
			Integer index = shapes.get(type);
			if (index != null) {
				return index;
			} else {
				return -1;
			}
		}

		private String getChildType(View childView) {
			Object o = childView.getAttributes().getAttribute(CSS.Attribute.LIST_STYLE_TYPE);
			String childtype = "circle";

			if (o == null) {
				if (type == null) {
					View v = childView.getParent();
					HTMLDocument doc = (HTMLDocument) v.getDocument();
					if (HTMLDocument.matchNameAttribute(v.getElement().getAttributes(), HTML.Tag.OL)) {
						childtype = "decimal";
					} else {
						childtype = "disc";
					}
				} else {
					childtype = type;
				}
			} else {
				childtype = o.toString();

				if (childtype != null && isShape(childtype)) {
					int len = -1;
					AttributeSet parentAttr = childView.getAttributes().getResolveParent();
					while (parentAttr != null) {
						o = parentAttr.getAttribute(CSS.Attribute.LIST_STYLE_TYPE);
						if (o != null) {
							String t = o.toString();
							if (isShape(t)) {
								childtype = t;
								parentAttr = parentAttr.getResolveParent();
								len++;
							} else {
								break;
							}
						} else {
							break;
						}
					}

					if (childtype.equals("circle")) {
						len += 1;
					} else if (childtype.equals("square")) {
						len += 2;
					}

					len %= 3;
					childtype = shapes_array[len];
				}
			}

			return childtype;
		}

		private void getStart(View parent) {
			checkedForStart = true;
			Element element = parent.getElement();
			if (element != null) {
				AttributeSet attr = element.getAttributes();
				Object startValue;
				if (attr != null && attr.isDefined(HTML.Attribute.START) && (startValue = attr.getAttribute(HTML.Attribute.START)) != null && (startValue instanceof String)) {
					try {
						start = Integer.parseInt((String) startValue);
					} catch (NumberFormatException nfe) {
					}
				}
			}
		}

		private int getRenderIndex(View parentView, int childIndex) {
			if (!checkedForStart) {
				getStart(parentView);
			}
			int retIndex = childIndex;
			for (int counter = childIndex; counter >= 0; counter--) {
				AttributeSet as = parentView.getElement().getElement(counter).getAttributes();
				if (as.getAttribute(StyleConstants.NameAttribute) != HTML.Tag.LI) {
					retIndex--;
				} else if (as.isDefined(HTML.Attribute.VALUE)) {
					Object value = as.getAttribute(HTML.Attribute.VALUE);
					if (value != null && (value instanceof String)) {
						try {
							int iValue = Integer.parseInt((String) value);
							return retIndex - counter + iValue;
						} catch (NumberFormatException nfe) {
						}
					}
				}
			}
			return retIndex + start;
		}

		public void paint(Graphics g, float x, float y, float w, float h, View v, int item) {
			View cv = v.getView(item);
			Object name = cv.getElement().getAttributes().getAttribute(StyleConstants.NameAttribute);
			if (!(name instanceof HTML.Tag) || name != HTML.Tag.LI) {
				return;
			}
			isLeftToRight = cv.getContainer().getComponentOrientation().isLeftToRight();

			float align = 0;
			if (cv.getViewCount() > 0) {
				View pView = cv.getView(0);
				Object cName = pView.getElement().getAttributes().getAttribute(StyleConstants.NameAttribute);
				if ((cName == HTML.Tag.P || cName == HTML.Tag.IMPLIED) && pView.getViewCount() > 0) {
					paintRect.setBounds((int) x, (int) y, (int) w, (int) h);
					Shape shape = cv.getChildAllocation(0, paintRect);
					if (shape != null && (shape = pView.getView(0).getChildAllocation(0, shape)) != null) {
						Rectangle rect = (shape instanceof Rectangle) ? (Rectangle) shape : shape.getBounds();

						align = pView.getView(0).getAlignment(View.Y_AXIS);
						y = rect.y;
						h = rect.height;
					}
				}
			}

			if (ss != null) {
				g.setColor(ss.getForeground(cv.getAttributes()));
			} else {
				g.setColor(Color.black);
			}

			if (img != null) {
				drawIcon(g, (int) x, (int) y, (int) w, (int) h, align, v.getContainer());
				return;
			}

			String childtype = getChildType(cv);
			Font font = ((StyledDocument) cv.getDocument()).getFont(cv.getAttributes());
			if (font != null) {
				g.setFont(font);
			}

			if (isShape(childtype)) {
				drawShape(g, getShapeIndex(childtype), (int) x, (int) y, (int) w, (int) h, align);
			} else if (childtype.equals("decimal")) {
				drawLetter(g, '1', (int) x, (int) y, (int) w, (int) h, align, getRenderIndex(v, item));
			} else if (childtype.equals("lower-alpha")) {
				drawLetter(g, 'a', (int) x, (int) y, (int) w, (int) h, align, getRenderIndex(v, item));
			} else if (childtype.equals("upper-alpha")) {
				drawLetter(g, 'A', (int) x, (int) y, (int) w, (int) h, align, getRenderIndex(v, item));
			} else if (childtype.equals("lower-roman")) {
				drawLetter(g, 'i', (int) x, (int) y, (int) w, (int) h, align, getRenderIndex(v, item));
			} else if (childtype.equals("upper-roman")) {
				drawLetter(g, 'I', (int) x, (int) y, (int) w, (int) h, align, getRenderIndex(v, item));
			}
		}

		void drawIcon(Graphics g, int ax, int ay, int aw, int ah, float align, Component c) {
			int gap = isLeftToRight ? -(img.getIconWidth() + bulletgap) : (aw + bulletgap);
			int x = ax + gap;
			int y = Math.max(ay, ay + (int) (align * ah) - img.getIconHeight());
			img.paintIcon(c, g, x, y);
		}

		void drawShape(Graphics g, int type, int ax, int ay, int aw, int ah, float align) {
			// Align to bottom of shape.
			int gap = isLeftToRight ? -(bulletgap + 8) : (aw + bulletgap);
			int x = ax + gap;
			int y = Math.max(ay, ay + (int) (align * ah) - 8);

			if (type == 2) {
				g.fillRect(x, y, 4, 4);
			} else if (type == 1) {
				g.drawOval(x, y, 4, 4);
			} else {
				g.fillOval(x, y, 4, 4);
			}
		}

		void drawLetter(Graphics g, char letter, int ax, int ay, int aw, int ah, float align, int index) {
			String str = formatItemNum(index, letter);
			str = isLeftToRight ? str + "." : "." + str;
			// jdk1.5
			// FontMetrics fm = SwingUtilities2.getFontMetrics(null, g);
			// jdk1.6
			FontMetrics fm = g.getFontMetrics();

			// int stringwidth = SwingUtilities2.stringWidth(null, fm, str);
			int stringwidth = fm.stringWidth(str);
			int gap = isLeftToRight ? -(stringwidth + bulletgap) : (aw + bulletgap);
			int x = ax + gap;
			int y = Math.max(ay + fm.getAscent(), ay + (int) (ah * align));
			// SwingUtilities2.drawString(null, g, str, x, y);
			g.drawString(str, x, y);
		}

		String formatItemNum(int itemNum, char type) {
			String numStyle = "1";

			boolean uppercase = false;

			String formattedNum;

			switch (type) {
				case '1':
				default:
					formattedNum = String.valueOf(itemNum);
					break;

				case 'A':
					uppercase = true;

				case 'a':
					formattedNum = formatAlphaNumerals(itemNum);
					break;

				case 'I':
					uppercase = true;

				case 'i':
					formattedNum = formatRomanNumerals(itemNum);
			}

			if (uppercase) {
				formattedNum = formattedNum.toUpperCase();
			}

			return formattedNum;
		}

		String formatAlphaNumerals(int itemNum) {
			String result = "";

			if (itemNum > 26) {
				result = formatAlphaNumerals(itemNum / 26) + formatAlphaNumerals(itemNum % 26);
			} else {
				result = String.valueOf((char) ('a' + itemNum - 1));
			}

			return result;
		}

		static final char romanChars[][] = { { 'i', 'v' }, { 'x', 'l' }, { 'c', 'd' }, { 'm', '?' }, };

		String formatRomanNumerals(int num) {
			return formatRomanNumerals(0, num);
		}

		String formatRomanNumerals(int level, int num) {
			if (num < 10) {
				return formatRomanDigit(level, num);
			} else {
				return formatRomanNumerals(level + 1, num / 10) + formatRomanDigit(level, num % 10);
			}
		}

		String formatRomanDigit(int level, int digit) {
			String result = "";
			if (digit == 9) {
				result = result + romanChars[level][0];
				result = result + romanChars[level + 1][0];
				return result;
			} else if (digit == 4) {
				result = result + romanChars[level][0];
				result = result + romanChars[level][1];
				return result;
			} else if (digit >= 5) {
				result = result + romanChars[level][1];
				digit -= 5;
			}

			for (int i = 0; i < digit; i++) {
				result = result + romanChars[level][0];
			}

			return result;
		}

		private Rectangle paintRect;

		private boolean checkedForStart;

		private int start;

		private String type;

		URL imageurl;

		private StyleSheet ss = null;

		Icon img = null;

		private int bulletgap = 5;

		private boolean isLeftToRight;
	}
}
