package ru.nest.jvg.shape.text;

import ru.nest.jvg.resource.Resource;
import ru.nest.swing.text.AdvancedParagraphView;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import java.awt.*;
import java.awt.font.FontRenderContext;

public class JVGParagraphView extends AdvancedParagraphView {
	protected static final Border smallBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);

	protected final static FontRenderContext frc = new FontRenderContext(null, false, false);

	protected Resource<Color> bgColor; // Background color, or null for

	// transparent.
	protected Border border; // Border, or null for no border

	protected Insets paraInsets; // Original paragraph insets

	protected int bulletsType;

	protected int bulletsIndentSize;

	protected int bulletsIndentCount;

	protected Resource<Font> bulletsFont;

	protected Resource<Color> bulletsColor;

	public JVGParagraphView(Element e) {
		super(e);
	}

	@Override
	protected void setPropertiesFromAttributes() {
		AttributeSet attr = getAttributes();
		if (attr != null) {
			super.setPropertiesFromAttributes();

			float spaceAbove = StyleConstants.getSpaceAbove(attr);
			float spaceBelow = StyleConstants.getSpaceBelow(attr);
			paraInsets = new Insets((int) spaceAbove, getLeftInset(), (int) spaceBelow, getRightInset());

			bulletsSize = 0;
			bulletsType = JVGStyleConstants.getParagraphBullets(attr);
			if (bulletsType != JVGStyleConstants.BULLETS_NONE) {
				bulletsFont = JVGStyleConstants.getParagraphBulletsFont(attr);
				bulletsColor = JVGStyleConstants.getParagraphBulletsColor(attr);
				bulletsIndentSize = JVGStyleConstants.getParagraphBulletsIndentSize(attr);
				bulletsIndentCount = JVGStyleConstants.getParagraphBulletsIndentCount(attr);

				paraInsets.left += bulletsIndentSize * bulletsIndentCount;
				switch (bulletsType) {
					case JVGStyleConstants.BULLETS_CIRCLE:
					case JVGStyleConstants.BULLETS_SQURE:
					case JVGStyleConstants.BULLETS_MINUS:
						bulletsSize = 20;
						paraInsets.left += bulletsSize;
						break;

					case JVGStyleConstants.BULLETS_NUMBER:
					case JVGStyleConstants.BULLETS_LETTER:
						int size = getBulletsSize();
						String text = size + ".";

						bulletsSize = (int) bulletsFont.getResource().getStringBounds(text, frc).getWidth() + 5;
						if (bulletsSize < 20) {
							bulletsSize = 20;
						}

						paraInsets.left += bulletsSize;
						break;
				}
			}

			bgColor = JVGStyleConstants.getParagraphBackground(attr);
			if (bgColor != null && border == null) {
				border = smallBorder;
			}

			border = JVGStyleConstants.getParagraphBorder(attr);
			if (border != null) {
				Insets borderInsets = border.getBorderInsets(getContainer());
				paraInsets.top += borderInsets.top;
				paraInsets.left += borderInsets.left;
				paraInsets.bottom += borderInsets.bottom;
				paraInsets.right += borderInsets.right;
			}

			setInsets((short) paraInsets.top, (short) paraInsets.left, (short) paraInsets.bottom, (short) paraInsets.right);
		}
	}

	int bulletsSize;

	@Override
	public void paint(Graphics g, Shape a) {
		Rectangle alloc = null;
		if (border != null || bulletsType != JVGStyleConstants.BULLETS_NONE) {
			alloc = a.getBounds();
			alloc.x += paraInsets.left;
			alloc.y += paraInsets.top;
			alloc.width -= paraInsets.left + paraInsets.right;
			alloc.height -= paraInsets.top + paraInsets.bottom;
		}

		if (border != null) {
			Insets bi = border.getBorderInsets(null);
			alloc.x -= bi.left;
			alloc.y -= bi.top;
			alloc.width += bi.left + bi.right;
			alloc.height += bi.top + bi.bottom;

			Container comp = getContainer();
			if (bgColor != null) {
				Color origColor = g.getColor();
				g.setColor(bgColor.getResource());
				g.fillRect(alloc.x, alloc.y, alloc.width, alloc.height);
				g.setColor(origColor);
			}
			border.paintBorder(comp, g, alloc.x, alloc.y, alloc.width, alloc.height);
		}

		if (bulletsType != JVGStyleConstants.BULLETS_NONE) {
			alloc.x -= bulletsSize;
			alloc.width += bulletsSize;
		}

		switch (bulletsType) {
			case JVGStyleConstants.BULLETS_CIRCLE:
			case JVGStyleConstants.BULLETS_SQURE:
			case JVGStyleConstants.BULLETS_MINUS:
				drawFigure(g, alloc, bulletsType);
				break;

			case JVGStyleConstants.BULLETS_NUMBER:
			case JVGStyleConstants.BULLETS_LETTER:
				int index = getBulletsIndex();
				drawNumeration(g, alloc, bulletsType, index);
				break;
		}

		super.paint(g, a);
	}

	private void drawFigure(Graphics g, Rectangle alloc, int bulletsType) {
		try {
			int x = alloc.x + getOffset(X_AXIS, 0);
			int y = alloc.y + getOffset(Y_AXIS, 0) + getSpan(Y_AXIS, 0) / 2;

			g.setColor(bulletsColor.getResource());
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			switch (bulletsType) {
				case JVGStyleConstants.BULLETS_CIRCLE:
					g.fillArc(x, y - 2, 6, 6, 0, 360);
					break;

				case JVGStyleConstants.BULLETS_SQURE:
					g.fillRect(x, y - 2, 6, 6);
					break;

				case JVGStyleConstants.BULLETS_MINUS:
					g.drawLine(x, y + 1, x + 10, y + 1);
					break;
			}
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	private int getBulletsIndex() {
		int index = -1;
		View parent = getParent();
		int childsCount = parent.getViewCount();
		for (int i = 0; i < childsCount; i++) {
			View view = parent.getView(i);
			if (view == this) {
				index = i;
				break;
			}
		}

		if (index == -1) {
			return -1;
		}

		int numerationIndex = 1;
		for (int i = index - 1; i >= 0; i--) {
			View view = parent.getView(i);
			AttributeSet attr = view.getAttributes();
			if (attr != null) {
				int bt = JVGStyleConstants.getParagraphBullets(attr);
				int bic = JVGStyleConstants.getParagraphBulletsIndentCount(attr);
				if (bt == bulletsType && bic >= bulletsIndentCount) {
					if (bic == bulletsIndentCount) {
						numerationIndex++;
					}
				} else {
					break;
				}
			}
		}
		return numerationIndex;
	}

	private void drawNumeration(Graphics g, Rectangle alloc, int bulletsType, int numerationIndex) {
		try {
			int x = alloc.x + getOffset(X_AXIS, 0);
			int y = alloc.y + getOffset(Y_AXIS, 0) + getSpan(Y_AXIS, 0);

			Font oldFont = g.getFont();

			g.setColor(bulletsColor.getResource());
			g.setFont(bulletsFont.getResource());

			switch (bulletsType) {
				case JVGStyleConstants.BULLETS_NUMBER:
					g.drawString(numerationIndex + ".", x, y);
					break;

				case JVGStyleConstants.BULLETS_LETTER:
					int letter = 'a' + numerationIndex - 1;
					letter %= 'z';

					g.drawString((char) letter + ".", x, y);
					break;
			}

			g.setFont(oldFont);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	private int getBulletsSize() {
		int index = -1;
		View parent = getParent();
		int childsCount = parent != null ? parent.getViewCount() : 0;
		for (int i = 0; i < childsCount; i++) {
			View view = parent.getView(i);
			if (view == this) {
				index = i;
				break;
			}
		}

		if (index == -1) {
			return 0;
		}

		int size = 1;
		for (int i = index - 1; i >= 0; i--) {
			View view = parent.getView(i);
			AttributeSet attr = view.getAttributes();
			if (attr != null) {
				int bt = JVGStyleConstants.getParagraphBullets(attr);
				int bic = JVGStyleConstants.getParagraphBulletsIndentCount(attr);
				if (bt == bulletsType && bic >= bulletsIndentCount) {
					if (bic == bulletsIndentCount) {
						size++;
					}
				} else {
					break;
				}
			}
		}

		for (int i = index + 1; i < childsCount; i++) {
			View view = parent.getView(i);
			AttributeSet attr = view.getAttributes();
			if (attr != null) {
				int bt = JVGStyleConstants.getParagraphBullets(attr);
				int bic = JVGStyleConstants.getParagraphBulletsIndentCount(attr);
				if (bt == bulletsType && bic >= bulletsIndentCount) {
					if (bic == bulletsIndentCount) {
						size++;
					}
				} else {
					break;
				}
			}
		}
		return size;
	}
}
