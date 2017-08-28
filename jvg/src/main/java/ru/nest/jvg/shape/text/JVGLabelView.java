package ru.nest.jvg.shape.text;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

import javax.swing.JComponent;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.gradient.LinearGradient;
import javax.swing.gradient.LinearGradientPaint;
import javax.swing.gradient.RadialGradient;
import javax.swing.gradient.RadialGradientPaint;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.GlyphView;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.LayeredHighlighter;
import javax.swing.text.Segment;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.TabableView;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

import ru.nest.jvg.resource.ColorResource;
import ru.nest.jvg.resource.LinearGradientResource;
import ru.nest.jvg.resource.RadialGradientResource;
import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.resource.TextureResource;
import ru.nest.jvg.shape.JVGStyledText;
import ru.nest.jvg.shape.paint.FillPainter;
import ru.nest.jvg.shape.paint.Painter;
import sun.swing.SwingUtilities2;

public class JVGLabelView extends GlyphView implements TabableView {
	public JVGLabelView(Element elem) {
		super(elem);
	}

	final void sync() {
		if (font == null) {
			setPropertiesFromAttributes();
		}
	}

	protected void setUnderline(int u) {
		underline = u;
	}

	protected void setUnderlineColor(Resource<Color> c) {
		underlineColor = c;
	}

	protected void setStrikeThrough(boolean s) {
		strike = s;
	}

	protected void setSuperscript(boolean s) {
		superscript = s;
	}

	protected void setSubscript(boolean s) {
		subscript = s;
	}

	protected void setBackground(Resource bg) {
		this.bg = bg;
	}

	protected void setPropertiesFromAttributes() {
		AttributeSet attr = getAttributes();
		if (attr != null) {
			Document d = getDocument();
			if (d instanceof StyledDocument) {
				StyledDocument doc = (StyledDocument) d;
				font = doc.getFont(attr);

				fg = null;
				if (attr.isDefined(JVGStyleConstants.Foreground)) {
					fg = JVGStyleConstants.getForeground(attr);
				}

				fgdef = null;
				if (fg == null) {
					JTextPane c = (JTextPane) getContainer();
					JVGStyledText t = (JVGStyledText) c.getClientProperty("jvgtext");
					fgdef = t.getPainter(FillPainter.class);
				}

				if (attr.isDefined(JVGStyleConstants.Background)) {
					bg = JVGStyleConstants.getBackground(attr);
				} else {
					bg = null;
				}

				setUnderline(JVGStyleConstants.getUnderline(attr));
				setUnderlineColor(JVGStyleConstants.getUnderlineColor(attr));
				setStrikeThrough(StyleConstants.isStrikeThrough(attr));
				setSuperscript(StyleConstants.isSuperscript(attr));
				setSubscript(StyleConstants.isSubscript(attr));
			} else {
				throw new StateInvariantError("LabelView needs StyledDocument");
			}
		}
	}

	@Deprecated
	protected FontMetrics getFontMetrics() {
		sync();
		Container c = getContainer();
		return (c != null) ? c.getFontMetrics(font) : Toolkit.getDefaultToolkit().getFontMetrics(font);
	}

	public Resource getBackgroundResource() {
		sync();
		return bg;
	}

	public Resource getForegroundResource() {
		sync();
		if (fg != null) {
			return fg;
		} else if (fgdef != null && fgdef.getPaint() != null) {
			return fgdef.getPaint().getResource();
		}
		return null;
	}

	@Override
	public Font getFont() {
		sync();
		return font;
	}

	public int getUnderline() {
		sync();
		return underline;
	}

	@Override
	public boolean isStrikeThrough() {
		sync();
		return strike;
	}

	@Override
	public boolean isSubscript() {
		sync();
		return subscript;
	}

	@Override
	public boolean isSuperscript() {
		sync();
		return superscript;
	}

	@Override
	public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
		font = null;
		super.changedUpdate(e, a, f);
	}

	private Font font;

	private Resource fg;

	private Painter fgdef;

	private Resource bg;

	private int underline;

	private Resource<Color> underlineColor;

	private boolean strike;

	private boolean superscript;

	private boolean subscript;

	private GlyphPainter painter;

	@Override
	public void setGlyphPainter(GlyphPainter p) {
		super.setGlyphPainter(p);
		painter = p;
	}

	public static Paint getPaint(Resource c, Rectangle alloc) {
		if (c instanceof ColorResource) {
			ColorResource color = (ColorResource) c;
			return color.getResource();
		} else if (c instanceof LinearGradientResource) {
			LinearGradientResource gradientResource = (LinearGradientResource) c;
			LinearGradient gradient = gradientResource.getResource();
			float x1 = (float) alloc.getX() + (float) alloc.getWidth() * gradient.getX1();
			float y1 = (float) alloc.getY() + (float) alloc.getHeight() * gradient.getY1();
			float x2 = (float) alloc.getX() + (float) alloc.getWidth() * gradient.getX2();
			float y2 = (float) alloc.getY() + (float) alloc.getHeight() * gradient.getY2();
			Color[] colors = new Color[gradient.getColors().length];
			for (int i = 0; i < colors.length; i++) {
				colors[i] = gradient.getColors()[i].getResource();
			}
			return new LinearGradientPaint(x1, y1, x2, y2, gradient.getFractions(), colors, gradient.getCycleMethod());
		} else if (c instanceof RadialGradientResource) {
			RadialGradientResource gradientResource = (RadialGradientResource) c;
			RadialGradient gradient = gradientResource.getResource();
			float cx = (float) alloc.getX() + (float) alloc.getWidth() * gradient.getCX();
			float cy = (float) alloc.getY() + (float) alloc.getHeight() * gradient.getCY();
			float fx = (float) alloc.getX() + (float) alloc.getWidth() * gradient.getFX();
			float fy = (float) alloc.getY() + (float) alloc.getHeight() * gradient.getFY();
			Color[] colors = new Color[gradient.getColors().length];
			for (int i = 0; i < colors.length; i++) {
				colors[i] = gradient.getColors()[i].getResource();
			}
			return new RadialGradientPaint(cx, cy, gradient.getR(), fx, fy, gradient.getFractions(), colors, null, gradient.getCycleMethod());
		} else if (c instanceof TextureResource) {
			TextureResource texture = (TextureResource) c;
			return texture.getResource().getPaint();
		}
		return null;
	}

	private void paintText(Graphics g, Shape a, Object c, int p0, int p1) {
		// render the glyphs
		if (c instanceof Resource) {
			Graphics2D g2d = (Graphics2D) g;
			Rectangle alloc = (a instanceof Rectangle) ? (Rectangle) a : a.getBounds();
			g2d.setPaint(getPaint((Resource) c, alloc));
		} else if (c instanceof Color) {
			g.setColor((Color) c);
		} else {
			// on default
			g.setColor(Color.black);
		}

		painter.paint(this, g, a, p0, p1);

		// render underline or strikethrough if set.
		int underline = getUnderline();
		boolean strike = isStrikeThrough();
		if (underline != JVGStyleConstants.UNDERLINE_NONE || strike) {
			// calculate x coordinates
			Rectangle alloc = (a instanceof Rectangle) ? (Rectangle) a : a.getBounds();
			View parent = getParent();
			if ((parent != null) && (parent.getEndOffset() == p1)) {
				// strip whitespace on end
				Segment s = getText(p0, p1);
				//while (Character.isWhitespace(s.last())) {
				//    p1 -= 1;
				//    s.count -= 1;
				//}
				while (s.count > 0 && (Character.isWhitespace(s.array[s.count - 1]))) {
					p1 -= 1;
					s.count -= 1;
				}
				// TODO
				// SegmentCache.releaseSharedSegment(s);
			}
			int x0 = alloc.x;
			int p = getStartOffset();
			if (p != p0) {
				x0 += (int) painter.getSpan(this, p, p0, getTabExpander(), x0);
			}
			int x1 = x0 + (int) painter.getSpan(this, p0, p1, getTabExpander(), x0);

			// calculate y coordinate
			int d = (int) painter.getDescent(this);
			int y = alloc.y + alloc.height - (int) painter.getDescent(this);

			if (underline != JVGStyleConstants.UNDERLINE_NONE) {
				int yTmp = y;
				yTmp += 1;

				if (underlineColor != null) {
					g.setColor(underlineColor.getResource());
				}

				switch (underline) {
					case JVGStyleConstants.UNDERLINE_LINE:
						g.drawLine(x0, yTmp, x1, yTmp);
						break;

					case JVGStyleConstants.UNDERLINE_DOUBLE_LINE:
						g.drawLine(x0, yTmp, x1, yTmp);
						g.drawLine(x0, yTmp + 2, x1, yTmp + 2);
						break;

					case JVGStyleConstants.UNDERLINE_TRIPLE_LINE:
						g.drawLine(x0, yTmp, x1, yTmp);
						g.drawLine(x0, yTmp + 2, x1, yTmp + 2);
						g.drawLine(x0, yTmp + 4, x1, yTmp + 4);
						break;

					case JVGStyleConstants.UNDERLINE_ZIGZAG:
						int shift = 0;
						for (int i = x0; i <= x1; i += 3) {
							g.drawLine(i, yTmp + shift, i + 3, yTmp + shift);
							shift = shift == 0 ? 1 : 0;
						}
						break;
				}
			}

			if (strike) {
				int yTmp = y;
				// move y coordinate above baseline
				yTmp -= (int) (painter.getAscent(this) * 0.3f);
				g.drawLine(x0, yTmp, x1, yTmp);
			}
		}
	}

	/**
	 * Used by paint() to store highlighted view positions
	 */
	private byte[] selections = null;

	/**
	 * Lazily initializes the selections field
	 */
	private void initSelections(int p0, int p1) {
		int viewPosCount = p1 - p0 + 1;
		if (selections == null || viewPosCount > selections.length) {
			selections = new byte[viewPosCount];
			return;
		}
		for (int i = 0; i < viewPosCount; selections[i++] = 0);
	}

	@Override
	public void paint(Graphics g, Shape a) {
		checkPainter();

		boolean paintedText = false;
		Component c = getContainer();
		int p0 = getStartOffset();
		int p1 = getEndOffset();
		Rectangle alloc = (a instanceof Rectangle) ? (Rectangle) a : a.getBounds();
		Resource bg = getBackgroundResource();
		Resource fg = getForegroundResource();

		// draw background
		if (bg != null) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setPaint(getPaint(bg, alloc));
			g2d.fillRect(alloc.x, alloc.y, alloc.width, alloc.height);
		}

		if (c instanceof JTextComponent) {
			JTextComponent tc = (JTextComponent) c;
			Highlighter h = tc.getHighlighter();
			if (h instanceof LayeredHighlighter) {
				((LayeredHighlighter) h).paintLayeredHighlights(g, p0, p1, a, tc, this);
			}
		}

		if (isComposedTextElement(getElement())) {
			paintComposedText(g, a.getBounds(), this);
			paintedText = true;
		} else if (c instanceof JTextComponent) {
			JTextComponent tc = (JTextComponent) c;
			Color selFG = tc.getSelectedTextColor();
			if ((tc.getHighlighter() != null) && (selFG != null) && !selFG.equals(fg)) {
				Highlighter.Highlight[] h = tc.getHighlighter().getHighlights();
				if (h.length != 0) {
					boolean initialized = false;
					int viewSelectionCount = 0;
					for (int i = 0; i < h.length; i++) {
						Highlighter.Highlight highlight = h[i];
						int hStart = highlight.getStartOffset();
						int hEnd = highlight.getEndOffset();

						if (hStart > p1 || hEnd < p0) {
							// the selection is out of this view
							continue;
						}

						if (!SwingUtilities2.useSelectedTextColor(highlight, tc)) {
							continue;
						}

						if (hStart <= p0 && hEnd >= p1) {
							// the whole view is selected
							paintText(g, a, selFG, p0, p1);
							paintedText = true;
							break;
						}

						// the array is lazily created only when the view
						// is partially selected
						if (!initialized) {
							initSelections(p0, p1);
							initialized = true;
						}

						hStart = Math.max(p0, hStart);
						hEnd = Math.min(p1, hEnd);
						paintText(g, a, selFG, hStart, hEnd);
						// the array represents view positions [0, p1-p0+1]
						// later will iterate this array and sum its
						// elements. Positions with sum == 0 are not selected.
						selections[hStart - p0]++;
						selections[hEnd - p0]--;

						viewSelectionCount++;
					}

					if (!paintedText && viewSelectionCount > 0) {
						// the view is partially selected
						int curPos = -1;
						int startPos = 0;
						int viewLen = p1 - p0;
						while (curPos++ < viewLen) {
							// searching for the next selection start
							while (curPos < viewLen && selections[curPos] == 0) {
								curPos++;
							}

							if (startPos != curPos) {
								// paint unselected text
								paintText(g, a, fg, p0 + startPos, p0 + curPos);
							}

							int checkSum = 0;
							// searching for next start position of unselected
							// text
							while (curPos < viewLen && (checkSum += selections[curPos]) != 0) {
								curPos++;
							}
							startPos = curPos;
						}
						paintedText = true;
					}
				}
			}
		}

		if (!paintedText) {
			paintText(g, a, fg, p0, p1);
		}
	}

	static boolean isComposedTextElement(Document doc, int offset) {
		Element elem = doc.getDefaultRootElement();
		while (!elem.isLeaf()) {
			elem = elem.getElement(elem.getElementIndex(offset));
		}
		return isComposedTextElement(elem);
	}

	static boolean isComposedTextElement(Element elem) {
		AttributeSet as = elem.getAttributes();
		return isComposedTextAttributeDefined(as);
	}

	static boolean isComposedTextAttributeDefined(AttributeSet as) {
		return ((as != null) && (as.isDefined(StyleConstants.ComposedTextAttribute)));
	}

	static void paintComposedText(Graphics g, Rectangle alloc, JVGLabelView v) {
		if (g instanceof Graphics2D) {
			Graphics2D g2d = (Graphics2D) g;
			int p0 = v.getStartOffset();
			int p1 = v.getEndOffset();
			AttributeSet attrSet = v.getElement().getAttributes();
			AttributedString as = (AttributedString) attrSet.getAttribute(StyleConstants.ComposedTextAttribute);
			int start = v.getElement().getStartOffset();
			int y = alloc.y + alloc.height - (int) v.getGlyphPainter().getDescent(v);
			int x = alloc.x;

			// Add text attributes
			as.addAttribute(TextAttribute.FONT, v.getFont());
			as.addAttribute(TextAttribute.FOREGROUND, v.getForegroundResource());
			if (StyleConstants.isBold(v.getAttributes())) {
				as.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
			}
			if (StyleConstants.isItalic(v.getAttributes())) {
				as.addAttribute(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
			}
			if (v.getUnderline() != JVGStyleConstants.UNDERLINE_NONE) {
				as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
			}
			if (v.isStrikeThrough()) {
				as.addAttribute(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
			}
			if (v.isSuperscript()) {
				as.addAttribute(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUPER);
			}
			if (v.isSubscript()) {
				as.addAttribute(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUB);
			}

			// draw
			AttributedCharacterIterator aci = as.getIterator(null, p0 - start, p1 - start);
			SwingUtilities2.drawString(getJComponent(v), g2d, aci, x, y);
		}
	}

	static JComponent getJComponent(View view) {
		if (view != null) {
			Component component = view.getContainer();
			if (component instanceof JComponent) {
				return (JComponent) component;
			}
		}
		return null;
	}
}
