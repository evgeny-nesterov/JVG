package ru.nest.jvg.shape.text;

import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;

import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.GlyphView;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.text.TabExpander;
import javax.swing.text.Utilities;
import javax.swing.text.View;

import ru.nest.jvg.shape.JVGStyledText;
import sun.swing.SwingUtilities2;

class JVGGlyphPainter extends GlyphView.GlyphPainter {
	private JVGStyledText text;

	public JVGGlyphPainter(JVGStyledText text) {
		this.text = text;
	}

	@Override
	public float getSpan(GlyphView v, int p0, int p1, TabExpander e, float x) {
		sync(v);
		Segment text = v.getText(p0, p1);
		float w = (float) f.getStringBounds(text.array, text.offset, text.offset + text.count, this.text.getFRC()).getWidth();
		// SegmentCache.releaseSharedSegment(text);
		return w;
	}

	@Override
	public float getHeight(GlyphView v) {
		sync(v);
		return (float) f.getMaxCharBounds(text.getFRC()).getHeight();
	}

	@Override
	public float getAscent(GlyphView v) {
		sync(v);
		return metrics.getAscent();
	}

	@Override
	public float getDescent(GlyphView v) {
		sync(v);
		return metrics.getDescent();
	}

	@Override
	public void paint(GlyphView v, Graphics g, Shape a, int p0, int p1) {
		sync(v);
		Segment text;
		TabExpander expander = v.getTabExpander();
		Rectangle alloc = (a instanceof Rectangle) ? (Rectangle) a : a.getBounds();

		// determine the x coordinate to render the glyphs
		int x = alloc.x;
		int p = v.getStartOffset();
		if (p != p0) {
			text = v.getText(p, p0);
			int width = (int) f.getStringBounds(text.array, text.offset, text.offset + text.count, this.text.getFRC()).getWidth();
			x += width;
			// SegmentCache.releaseSharedSegment(text);
		}

		// determine the y coordinate to render the glyphs
		int y = alloc.y + metrics.getHeight() - metrics.getDescent();

		// render the glyphs
		text = v.getText(p0, p1);
		g.setFont(f);
		drawTabbedText(v, text, x, y, g, expander, p0);
		// SegmentCache.releaseSharedSegment(text);

		//		sync(v);
		//		Segment text;
		//		TabExpander expander = v.getTabExpander();
		//		Rectangle alloc = (a instanceof Rectangle) ? (Rectangle) a : a.getBounds();
		//
		//		// determine the x coordinate to render the glyphs
		//		int x = alloc.x;
		//		int p = v.getStartOffset();
		//		if (p != p0) {
		//			text = v.getText(p, p0);
		//			int width = Utilities.getTabbedTextWidth(text, metrics, x, expander, p);
		//			x += width;
		//			//			SegmentCache.releaseSharedSegment(text);
		//		}
		//
		//		// determine the y coordinate to render the glyphs
		//		int y = alloc.y + metrics.getHeight() - metrics.getDescent();
		//
		//		// render the glyphs
		//		text = v.getText(p0, p1);
		//		g.setFont(metrics.getFont());
		//
		//		Utilities.drawTabbedText(text, x, y, g, expander, p0);
		//		//		SegmentCache.releaseSharedSegment(text);
	}

	final int drawTabbedText(View view, Segment s, int x, int y, Graphics g, TabExpander e, int startOffset) {
		JComponent component = (JComponent) view.getContainer();
		FontMetrics metrics = SwingUtilities2.getFontMetrics(component, g);

		int nextX = x;
		char[] txt = s.array;
		int txtOffset = s.offset;
		int flushLen = 0;
		int flushIndex = s.offset;
		int n = s.offset + s.count;

		for (int i = txtOffset; i < n; i++) {
			if (txt[i] == '\t') {
				if (flushLen > 0) {
					nextX = SwingUtilities2.drawChars(component, g, txt, flushIndex, flushLen, x, y);
					flushLen = 0;
				}
				flushIndex = i + 1;
				if (e != null) {
					nextX = (int) e.nextTabStop(nextX, startOffset + i - txtOffset);
				} else {
					nextX += metrics.charWidth(' ');
				}
				x = nextX;
			} else if ((txt[i] == '\n') || (txt[i] == '\r')) {
				if (flushLen > 0) {
					nextX = SwingUtilities2.drawChars(component, g, txt, flushIndex, flushLen, x, y);
					flushLen = 0;
				}
				flushIndex = i + 1;
				x = nextX;
			} else {
				flushLen += 1;
			}
		}

		if (flushLen > 0) {
			nextX = SwingUtilities2.drawChars(component, g, txt, flushIndex, flushLen, x, y);
		}
		return nextX;
	}

	@Override
	public Shape modelToView(GlyphView v, int pos, Position.Bias bias, Shape a) throws BadLocationException {
		sync(v);

		Rectangle alloc = (a instanceof Rectangle) ? (Rectangle) a : a.getBounds();
		int p0 = v.getStartOffset();
		int p1 = v.getEndOffset();
		// TabExpander expander = v.getTabExpander();

		if (pos == p1) {
			// The caller of this is left to right and borders a right to
			// left view, return our end location.
			return new Rectangle(alloc.x + alloc.width, alloc.y, 0, metrics.getHeight());
		}

		if ((pos >= p0) && (pos <= p1)) {
			// determine range to the left of the position
			Segment text = v.getText(p0, pos);
			int width = (int) f.getStringBounds(text.array, text.offset, text.offset + text.count, this.text.getFRC()).getWidth();
			// SegmentCache.releaseSharedSegment(text);
			return new Rectangle(alloc.x + width, alloc.y, 0, metrics.getHeight());
		}
		throw new BadLocationException("modelToView - can't convert", p1);
	}

	@Override
	public int viewToModel(GlyphView v, float x, float y, Shape a, Position.Bias[] biasReturn) {
		sync(v);

		Rectangle alloc = (a instanceof Rectangle) ? (Rectangle) a : a.getBounds();
		int p0 = v.getStartOffset();
		int p1 = v.getEndOffset();
		TabExpander expander = v.getTabExpander();
		Segment text = v.getText(p0, p1);

		int offs = Utilities.getTabbedTextOffset(text, metrics, alloc.x, (int) x, expander, p0);
		// SegmentCache.releaseSharedSegment(text);
		int retValue = p0 + offs;
		if (retValue == p1) {
			// No need to return backward bias as GlyphPainter1 is used for
			// ltr text only.
			retValue--;
		}

		biasReturn[0] = Position.Bias.Forward;
		return retValue;
	}

	@Override
	public int getBoundedPosition(GlyphView v, int p0, float x, float len) {
		sync(v);
		TabExpander expander = v.getTabExpander();
		Segment s = v.getText(p0, v.getEndOffset());
		int index = Utilities.getTabbedTextOffset(s, metrics, (int) x, (int) (x + len), expander, p0, false);
		// SegmentCache.releaseSharedSegment(s);
		int p1 = p0 + index;
		return p1;
	}

	private Font f;

	private FontMetrics metrics;

	@SuppressWarnings("deprecation")
	void sync(GlyphView v) {
		f = v.getFont();
		if ((metrics == null) || (!f.equals(metrics.getFont()))) {
			// fetch a new FontMetrics
			Container c = v.getContainer();
			metrics = (c != null) ? c.getFontMetrics(f) : Toolkit.getDefaultToolkit().getFontMetrics(f);
		}
	}
}
