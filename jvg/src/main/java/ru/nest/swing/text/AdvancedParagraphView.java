package ru.nest.swing.text;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.Element;
import javax.swing.text.FlowView;
import javax.swing.text.GlyphView;
import javax.swing.text.ParagraphView;
import javax.swing.text.Position;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import java.awt.*;

public class AdvancedParagraphView extends ParagraphView {
	public AdvancedParagraphView(Element elem) {
		super(elem);
		strategy = new AdvancedFlowStrategy();
	}

	@Override
	protected View createRow() {
		Element elem = getElement();
		return new AdvancedRow(elem);
	}

	protected static int getSpaceCount(String content) {
		int result = 0;
		int index = content.indexOf(' ');
		while (index >= 0) {
			result++;
			index = content.indexOf(' ', index + 1);
		}
		return result;
	}

	protected static int[] getSpaceIndexes(String content, int shift) {
		int cnt = getSpaceCount(content);
		int[] result = new int[cnt];
		int counter = 0;
		int index = content.indexOf(' ');
		while (index >= 0) {
			result[counter] = index + shift;
			counter++;
			index = content.indexOf(' ', index + 1);
		}
		return result;
	}

	static class AdvancedFlowStrategy extends FlowStrategy {
		@Override
		public void layout(FlowView fv) {
			super.layout(fv);
			AttributeSet attr = fv.getElement().getAttributes();
			float lineSpacing = StyleConstants.getLineSpacing(attr);
			boolean justifiedAlignment = (StyleConstants.getAlignment(attr) == StyleConstants.ALIGN_JUSTIFIED);
			if (!justifiedAlignment && lineSpacing == 0) {
				return;
			}

			int cnt = fv.getViewCount();
			for (int i = 0; i < cnt - 1; i++) {
				AdvancedRow row = (AdvancedRow) fv.getView(i);
				if (lineSpacing != 0) {
					float height = row.getMinimumSpan(View.Y_AXIS) - row.getBottomInset();
					float addition = height * lineSpacing;
					System.out.println("lineSpacing=" + lineSpacing + ", height=" + height);
					row.setInsets(row.getTopInset(), row.getLeftInset(), (short) addition, row.getRightInset());
				}

				restructureRow(row, i);
				row.setRowNumber(i + 1);
			}
		}

		protected void restructureRow(View row, int rowNum) {
			int rowStartOffset = row.getStartOffset();
			int rowEndOffset = row.getEndOffset();
			String rowContent = "";
			try {
				rowContent = row.getDocument().getText(rowStartOffset, rowEndOffset - rowStartOffset);
				if (rowNum == 0) {
					while (rowContent.charAt(0) == ' ') {
						rowContent = rowContent.substring(1);
						if (rowContent.length() == 0) {
							break;
						}
					}
				}
			} catch (Exception exc) {
				exc.printStackTrace();
			}

			int rowSpaceCount = getSpaceCount(rowContent);
			if (rowSpaceCount < 1) {
				return;
			}

			int[] rowSpaceIndexes = getSpaceIndexes(rowContent, row.getStartOffset());
			int currentSpaceIndex = 0;

			for (int i = 0; i < row.getViewCount(); i++) {
				View child = row.getView(i);
				if ((child.getStartOffset() < rowSpaceIndexes[currentSpaceIndex]) && (child.getEndOffset() > rowSpaceIndexes[currentSpaceIndex])) {
					// split view
					View first = child.createFragment(child.getStartOffset(), rowSpaceIndexes[currentSpaceIndex]);
					View second = child.createFragment(rowSpaceIndexes[currentSpaceIndex], child.getEndOffset());
					View[] repl = new View[2];
					repl[0] = first;
					repl[1] = second;

					row.replace(i, 1, repl);
					currentSpaceIndex++;
					if (currentSpaceIndex >= rowSpaceIndexes.length) {
						break;
					}
				}
			}
		}
	}

	public class AdvancedRow extends BoxView {
		private int rowNumber = 0;

		AdvancedRow(Element elem) {
			super(elem, View.X_AXIS);
		}

		@Override
		protected void loadChildren(ViewFactory f) {
		}

		@Override
		public AttributeSet getAttributes() {
			View p = getParent();
			return (p != null) ? p.getAttributes() : null;
		}

		@Override
		public float getAlignment(int axis) {
			if (axis == View.X_AXIS) {
				AttributeSet attr = getAttributes();
				int justification = StyleConstants.getAlignment(attr);
				switch (justification) {
					case StyleConstants.ALIGN_LEFT:
					case StyleConstants.ALIGN_JUSTIFIED:
						return 0;
					case StyleConstants.ALIGN_RIGHT:
						return 1;
					case StyleConstants.ALIGN_CENTER:
						return 0.5f;
				}
			}
			return super.getAlignment(axis);
		}

		@Override
		public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
			Rectangle r = a.getBounds();
			View v = getViewAtPosition(pos, r);
			if ((v != null) && (!v.getElement().isLeaf())) {
				// Don't adjust the height if the view represents a branch.
				return super.modelToView(pos, a, b);
			}
			r = a.getBounds();
			int height = r.height;
			int y = r.y;
			Shape loc = super.modelToView(pos, a, b);
			r = loc.getBounds();
			r.height = height;
			r.y = y;
			return r;
		}

		@Override
		public int getStartOffset() {
			int offs = Integer.MAX_VALUE;
			int n = getViewCount();
			for (int i = 0; i < n; i++) {
				View v = getView(i);
				offs = Math.min(offs, v.getStartOffset());
			}
			return offs;
		}

		@Override
		public int getEndOffset() {
			int offs = 0;
			int n = getViewCount();
			for (int i = 0; i < n; i++) {
				View v = getView(i);
				offs = Math.max(offs, v.getEndOffset());
			}
			return offs;
		}

		@Override
		protected void layoutMinorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
			baselineLayout(targetSpan, axis, offsets, spans);
		}

		@Override
		protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) {
			return baselineRequirements(axis, r);
		}

		@Override
		protected int getViewIndexAtPosition(int pos) {
			// This is expensive, but are views are not necessarily layed
			// out in model order.
			if (pos < getStartOffset() || pos >= getEndOffset()) {
				return -1;
			}

			for (int counter = getViewCount() - 1; counter >= 0; counter--) {
				View v = getView(counter);
				if (pos >= v.getStartOffset() && pos < v.getEndOffset()) {
					return counter;
				}
			}
			return -1;
		}

		@Override
		public short getTopInset() {
			return super.getTopInset();
		}

		@Override
		public short getLeftInset() {
			return super.getLeftInset();
		}

		@Override
		public short getRightInset() {
			return super.getRightInset();
		}

		@Override
		public short getBottomInset() {
			return super.getBottomInset();
		}

		@Override
		public void setInsets(short topInset, short leftInset, short bottomInset, short rightInset) {
			super.setInsets(topInset, leftInset, bottomInset, rightInset);
		}

		@Override
		protected void layoutMajorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
			super.layoutMajorAxis(targetSpan, axis, offsets, spans);
			AttributeSet attr = getAttributes();
			if ((StyleConstants.getAlignment(attr) != StyleConstants.ALIGN_JUSTIFIED) && (axis != View.X_AXIS)) {
				return;
			}
			int cnt = offsets.length;

			int span = 0;
			for (int i = 0; i < cnt; i++) {
				span += spans[i];
			}

			if (getRowNumber() == 0) {
				return;
			}

			int startOffset = getStartOffset();
			int len = getEndOffset() - startOffset;
			String context = "";
			try {
				context = getElement().getDocument().getText(startOffset, len);
			} catch (Exception exc) {
				exc.printStackTrace();
			}

			int spaceCount = getSpaceCount(context) - 1;

			int pixelsToAdd = targetSpan - span;

			if (getRowNumber() == 1) {
				int firstLineIndent = (int) StyleConstants.getFirstLineIndent(getAttributes());
				pixelsToAdd -= firstLineIndent;
			}

			int[] spaces = getSpaces(pixelsToAdd, spaceCount);
			int j = 0;
			int shift = 0;
			for (int i = 1; i < cnt; i++) {
				GlyphView v = (GlyphView) getView(i);
				offsets[i] += shift;
				if ((isContainSpace(v)) && (i != cnt - 1)) {
					offsets[i] += spaces[j];
					spans[i - 1] += spaces[j];
					shift += spaces[j];
					j++;
				}
			}
		}

		protected int[] getSpaces(int space, int cnt) {
			if (cnt < 0) {
				cnt = 0;
			}

			int[] result = new int[cnt];
			if (cnt == 0) {
				return result;
			}

			int base = space / cnt;
			int rst = space % cnt;

			for (int i = 0; i < cnt; i++) {
				result[i] = base;
				if (rst > 0) {
					result[i]++;
					rst--;
				}
			}

			return result;
		}

		@Override
		public float getMinimumSpan(int axis) {
			if (axis == View.X_AXIS) {
				AttributeSet attr = getAttributes();
				if (StyleConstants.getAlignment(attr) != StyleConstants.ALIGN_JUSTIFIED) {
					return super.getMinimumSpan(axis);
				} else {
					return this.getParent().getMinimumSpan(axis);
				}
			} else {
				return super.getMinimumSpan(axis);
			}
		}

		@Override
		public float getMaximumSpan(int axis) {
			if (axis == View.X_AXIS) {
				AttributeSet attr = getAttributes();
				if (StyleConstants.getAlignment(attr) != StyleConstants.ALIGN_JUSTIFIED) {
					return super.getMaximumSpan(axis);
				} else {
					return this.getParent().getMaximumSpan(axis);
				}
			} else {
				return super.getMaximumSpan(axis);
			}
		}

		@Override
		public float getPreferredSpan(int axis) {
			if (axis == View.X_AXIS) {
				AttributeSet attr = getAttributes();
				if (StyleConstants.getAlignment(attr) != StyleConstants.ALIGN_JUSTIFIED) {
					return super.getPreferredSpan(axis);
				} else {
					return this.getParent().getPreferredSpan(axis);
				}
			} else {
				return super.getPreferredSpan(axis);
			}
		}

		public void setRowNumber(int value) {
			rowNumber = value;
		}

		public int getRowNumber() {
			return rowNumber;
		}
	}

	@Override
	public int getFlowSpan(int index) {
		int span = super.getFlowSpan(index);
		if (index == 0) {
			int firstLineIdent = (int) StyleConstants.getFirstLineIndent(this.getAttributes());
			span -= firstLineIdent;
		}
		return span;
	}

	@Override
	protected void layoutMinorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
		super.layoutMinorAxis(targetSpan, axis, offsets, spans);
		int firstLineIdent = (int) StyleConstants.getFirstLineIndent(this.getAttributes());
		offsets[0] += firstLineIdent;
	}

	protected static boolean isContainSpace(View v) {
		int startOffset = v.getStartOffset();
		int len = v.getEndOffset() - startOffset;
		try {
			String text = v.getDocument().getText(startOffset, len);
			if (text.indexOf(' ') >= 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception exc) {
			return false;
		}
	}
}
