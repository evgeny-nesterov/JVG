package ru.nest.jvg.shape;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.NavigationFilter;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.Utilities;
import javax.swing.text.View;

import ru.nest.fonts.Fonts;
import ru.nest.jvg.JVGComposeGraphics;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.actionarea.JVGActionArea;
import ru.nest.jvg.event.JVGKeyAdapter;
import ru.nest.jvg.event.JVGKeyEvent;
import ru.nest.jvg.event.JVGMouseEvent;
import ru.nest.jvg.event.JVGPeerEvent;
import ru.nest.jvg.event.JVGPeerListener;
import ru.nest.jvg.geom.MutableGeneralPath;
import ru.nest.jvg.resource.FontResource;
import ru.nest.jvg.resource.Resource;
import ru.nest.jvg.shape.paint.Painter;
import ru.nest.jvg.shape.text.JVGCaret;
import ru.nest.jvg.shape.text.JVGStyleConstants;
import ru.nest.jvg.shape.text.JVGTextEditorKit;
import sun.font.FontUtilities;

public class JVGStyledText extends JVGShape {
	private StyleContext styleContext = new StyleContext() {
		private transient FontKey fontSearch = new FontKey(null, 0, 0);

		private transient Hashtable<FontKey, Font> fontTable = new Hashtable<FontKey, Font>();

		@Override
		public Font getFont(String family, int style, int size) {
			fontSearch.setValue(family, style, size);
			Font f = fontTable.get(fontSearch);
			if (f == null) {
				// haven't seen this one yet.
				Style defaultStyle = getStyle(StyleContext.DEFAULT_STYLE);
				if (defaultStyle != null) {
					final String FONT_ATTRIBUTE_KEY = "FONT_ATTRIBUTE_KEY";
					Font defaultFont = (Font) defaultStyle.getAttribute(FONT_ATTRIBUTE_KEY);
					if (defaultFont != null && defaultFont.getFamily().equalsIgnoreCase(family)) {
						f = defaultFont.deriveFont(style, size);
					}
				}
				if (f == null) {
					f = Fonts.getFont(family, style, size);
				}
				if (!FontUtilities.fontSupportsDefaultEncoding(f)) {
					f = FontUtilities.getCompositeFontUIResource(f);
				}
				FontKey key = new FontKey(family, style, size);
				fontTable.put(key, f);
			}
			return f;
		}

		class FontKey {
			private String family;

			private int style;

			private int size;

			public FontKey(String family, int style, int size) {
				setValue(family, style, size);
			}

			public void setValue(String family, int style, int size) {
				this.family = (family != null) ? family.intern() : null;
				this.style = style;
				this.size = size;
			}

			@Override
			public int hashCode() {
				int fhash = (family != null) ? family.hashCode() : 0;
				return fhash ^ style ^ size;
			}

			@Override
			public boolean equals(Object obj) {
				if (obj instanceof FontKey) {
					FontKey font = (FontKey) obj;
					return (size == font.size) && (style == font.style) && (family == font.family);
				}
				return false;
			}
		}
	};

	private class JVGStyledDocument extends DefaultStyledDocument {
		public JVGStyledDocument(StyleContext styleContext) {
			super(styleContext);
		}

		public void update() {
			super.styleChanged(null);
		}

		@Override
		public Font getFont(AttributeSet attr) {
			StyleContext styles = (StyleContext) getAttributeContext();
			return styleContext.getFont(attr);
		}
	};

	private JVGStyledDocument document = new JVGStyledDocument(styleContext);

	private JTextPane editor = new JTextPane(document) {
		@Override
		public void setDocument(Document doc) {
			super.setDocument(document);
		}
	};

	private boolean fractionMetrics = true;

	public JVGStyledText() {
		this("");
	}

	public JVGStyledText(String text) {
		this(text, null);
	}

	public JVGStyledText(String text, Resource<Font> f) {
		if (f == null) {
			f = new FontResource(FontResource.DEFAULT_FONT);
		}

		setName("Text");
		setOriginalBounds(true);
		setTransferHandler(editor.getTransferHandler());
		setFont(f);
		setFill(true);

		editor.setEditorKit(new JVGTextEditorKit(this));
		editor.setEnabled(true);
		editor.setEditable(true);
		editor.setMargin(null);
		editor.setBorder(null);
		editor.setDoubleBuffered(false);

		caret = new JVGCaret(this);
		editor.setCaret(caret);
		caret.setVisible(false);

		setText(text);
		setWrap(true);

		editor.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				invalidate();
				repaint();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				invalidate();
				repaint();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				invalidate();
				repaint();
			}
		});

		addKeyListener(new JVGKeyAdapter() {
			@Override
			public void keyPressed(JVGKeyEvent e) {
				if (isEdited) {
					if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
						deletePrevChar();
					} else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
						deleteNextChar();
					} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
						if (e.isControlDown()) {
							movePrevWord(e.isShiftDown());
						} else {
							moveCaret(SwingConstants.WEST, e.isShiftDown());
						}
					} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
						if (e.isControlDown()) {
							moveNextWord(e.isShiftDown());
						} else {
							moveCaret(SwingConstants.EAST, e.isShiftDown());
						}
					} else if (e.getKeyCode() == KeyEvent.VK_UP) {
						moveCaret(SwingConstants.NORTH, e.isShiftDown());
					} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
						moveCaret(SwingConstants.SOUTH, e.isShiftDown());
					} else if (e.getKeyCode() == KeyEvent.VK_HOME) {
						moveBeginLine(e.isShiftDown());
					} else if (e.getKeyCode() == KeyEvent.VK_END) {
						moveEndLine(e.isShiftDown());
					} else if (e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
						moveStart(e.isShiftDown());
					} else if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
						moveEnd(e.isShiftDown());
					} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
						setEditMode(false);
						repaint();
					} else if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_A) {
						selectAll();
					} else if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_X) {
						cut();
					} else if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_C) {
						copy();
					} else if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_V) {
						paste();
					} else if (e.isControlDown() && e.isShiftDown() && e.getKeyCode() == KeyEvent.VK_Z) {
						getPane().getRedoAction().actionPerformed(null);
					} else if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Z) {
						getPane().getUndoAction().actionPerformed(null);
					} else {
						char c = e.getKeyChar();
						if (font.getResource().canDisplay(c)) {
							editor.replaceSelection(String.valueOf(c));
						}
					}

					e.consume();
				} else {
					if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_C) {
						if (isEditable()) {
							// Copy all if text is editable but not edited
							// when mouse draging is used for component
							// movement.
							editor.selectAll();
							copy();
							editor.moveCaretPosition(0);

							// Attension! Do not consume event, as ctr-c used to copy object too.
						} else {
							// Copy selected text if text is not editable.
							copy();
							e.consume();
						}
					}
				}
			}
		});

		addPeerListener(new JVGPeerListener() {
			@Override
			public void connectedToPeer(JVGPeerEvent e) {
				if (e.getOldPeer() != null) {
					editor.getDocument().removeUndoableEditListener(e.getOldPeer().getUndoableListener());
				}
				editor.getDocument().addUndoableEditListener(e.getNewPeer().getUndoableListener());
			}

			@Override
			public void disconnectedFromPeer(JVGPeerEvent e) {
			}
		});
		enableEvents();

		editor.putClientProperty("jvgtext", this);
	}

	private JVGCaret caret;

	public JVGCaret getCaret() {
		return caret;
	}

	public void cut() {
		editor.cut();
	}

	public void copy() {
		editor.copy();
	}

	public void paste() {
		editor.paste();
	}

	public void moveBeginLine(boolean select) {
		try {
			editor.setBounds(maxBounds);
			editor.setPreferredSize(maxBounds.getSize());

			int offs = editor.getCaretPosition();
			int begOffs = Utilities.getRowStart(editor, offs);
			if (select) {
				editor.moveCaretPosition(begOffs);
			} else {
				editor.setCaretPosition(begOffs);
			}
		} catch (BadLocationException exc) {
			UIManager.getLookAndFeel().provideErrorFeedback(editor);
		}
	}

	public void moveEndLine(boolean select) {
		try {
			editor.setBounds(maxBounds);
			editor.setPreferredSize(maxBounds.getSize());

			int offs = editor.getCaretPosition();
			int endOffs = Utilities.getRowEnd(editor, offs);
			if (select) {
				editor.moveCaretPosition(endOffs);
			} else {
				editor.setCaretPosition(endOffs);
			}
		} catch (BadLocationException exc) {
			UIManager.getLookAndFeel().provideErrorFeedback(editor);
		}
	}

	public void moveCaret(int direction, boolean select) {
		int dot = caret.getDot();
		Position.Bias[] bias = new Position.Bias[1];
		Point magicPosition = caret.getMagicCaretPosition();

		try {
			if (magicPosition == null && (direction == SwingConstants.NORTH || direction == SwingConstants.SOUTH)) {
				Rectangle r = modelToView(dot);
				magicPosition = new Point(r.x, r.y);
			}

			NavigationFilter filter = editor.getNavigationFilter();
			if (filter != null) {
				dot = filter.getNextVisualPositionFrom(editor, dot, Position.Bias.Forward, direction, bias);
			} else {
				dot = getNextVisualPositionFrom(editor, dot, Position.Bias.Forward, direction, bias);
			}

			if (bias[0] == null) {
				bias[0] = Position.Bias.Forward;
			}

			if (select) {
				caret.moveDot(dot);
			} else {
				caret.setDot(dot);
			}

			if (magicPosition != null && (direction == SwingConstants.NORTH || direction == SwingConstants.SOUTH)) {
				editor.getCaret().setMagicCaretPosition(magicPosition);
			}

			repaint();
		} catch (BadLocationException exc) {
		}
	}

	public int getNextVisualPositionFrom(JTextComponent t, int pos, Position.Bias b, int direction, Position.Bias[] biasRet) throws BadLocationException {
		Document doc = editor.getDocument();
		if (doc instanceof AbstractDocument) {
			((AbstractDocument) doc).readLock();
		}
		try {
			editor.setBounds(maxBounds);
			editor.setPreferredSize(maxBounds.getSize());
			return getRootView().getNextVisualPositionFrom(pos, b, maxBounds, direction, biasRet);
		} finally {
			if (doc instanceof AbstractDocument) {
				((AbstractDocument) doc).readUnlock();
			}
		}
	}

	public void moveNextWord(boolean select) {
		editor.setBounds(maxBounds);
		editor.setPreferredSize(maxBounds.getSize());

		int offs = editor.getCaretPosition();
		boolean failed = false;
		int oldOffs = offs;
		Element curPara = Utilities.getParagraphElement(editor, offs);
		try {
			offs = Utilities.getNextWord(editor, offs);
			if (offs >= curPara.getEndOffset() && oldOffs != curPara.getEndOffset() - 1) {
				// we should first move to the end of current
				// paragraph (bug #4278839)
				offs = curPara.getEndOffset() - 1;
			}
		} catch (BadLocationException bl) {
			int end = editor.getDocument().getLength();
			if (offs != end) {
				if (oldOffs != curPara.getEndOffset() - 1) {
					offs = curPara.getEndOffset() - 1;
				} else {
					offs = end;
				}
			} else {
				failed = true;
			}
		}

		if (!failed) {
			if (select) {
				editor.moveCaretPosition(offs);
			} else {
				editor.setCaretPosition(offs);
			}
		} else {
			UIManager.getLookAndFeel().provideErrorFeedback(editor);
		}
	}

	public void movePrevWord(boolean select) {
		editor.setBounds(maxBounds);
		editor.setPreferredSize(maxBounds.getSize());

		int offs = editor.getCaretPosition();
		boolean failed = false;
		try {
			Element curPara = Utilities.getParagraphElement(editor, offs);
			offs = Utilities.getPreviousWord(editor, offs);
			if (offs < curPara.getStartOffset()) {
				// we should first move to the end of the
				// previous paragraph (bug #4278839)
				offs = Utilities.getParagraphElement(editor, offs).getEndOffset() - 1;
			}
		} catch (BadLocationException bl) {
			if (offs != 0) {
				offs = 0;
			} else {
				failed = true;
			}
		}

		if (!failed) {
			if (select) {
				editor.moveCaretPosition(offs);
			} else {
				editor.setCaretPosition(offs);
			}
		} else {
			UIManager.getLookAndFeel().provideErrorFeedback(editor);
		}
	}

	public void moveStart(boolean select) {
		if (select) {
			editor.moveCaretPosition(0);
		} else {
			editor.setCaretPosition(0);
		}
	}

	public void moveEnd(boolean select) {
		if (select) {
			editor.moveCaretPosition(length());
		} else {
			editor.setCaretPosition(length());
		}
	}

	public void selectAll() {
		Document doc = getDocument();
		editor.setCaretPosition(0);
		editor.moveCaretPosition(doc.getLength());
		repaint();
	}

	public void deleteNextChar() {
		try {
			Document doc = getDocument();
			int dot = caret.getDot();
			int mark = caret.getMark();
			if (dot != mark) {
				doc.remove(Math.min(dot, mark), Math.abs(dot - mark));
			} else if (dot < doc.getLength()) {
				int delChars = 1;

				if (dot < doc.getLength() - 1) {
					String dotChars = doc.getText(dot, 2);
					char c0 = dotChars.charAt(0);
					char c1 = dotChars.charAt(1);

					if (c0 >= '\uD800' && c0 <= '\uDBFF' && c1 >= '\uDC00' && c1 <= '\uDFFF') {
						delChars = 2;
					}
				}

				doc.remove(dot, delChars);
			}
		} catch (BadLocationException exc) {
		}
	}

	public void deletePrevChar() {
		try {
			Document doc = getDocument();
			int dot = caret.getDot();
			int mark = caret.getMark();
			if (dot != mark) {
				doc.remove(Math.min(dot, mark), Math.abs(dot - mark));
			} else if (dot > 0) {
				int delChars = 1;

				if (dot > 1) {
					String dotChars = doc.getText(dot - 2, 2);
					char c0 = dotChars.charAt(0);
					char c1 = dotChars.charAt(1);

					if (c0 >= '\uD800' && c0 <= '\uDBFF' && c1 >= '\uDC00' && c1 <= '\uDFFF') {
						delChars = 2;
					}
				}

				doc.remove(dot - delChars, delChars);
			}
		} catch (BadLocationException exc) {
		}
	}

	public View getRootView() {
		return editor.getUI().getRootView(editor);
	}

	public JTextPane getEditor() {
		return editor;
	}

	private boolean allowSelection = false;

	private double mx, my;

	@Override
	public void processMouseEvent(JVGMouseEvent e) {
		allowSelection = (!allowSelection && e.getClickCount() == 1) || allowSelection;
		if (e.getID() == JVGMouseEvent.MOUSE_PRESSED && e.getButton() == JVGMouseEvent.BUTTON1 && e.getClickCount() == 2) {
			if (isEditable() && !isEdited) {
				setEditMode(true);
				int pos = viewToModel(e.getX(), e.getY());
				caret.setDot(pos);

				allowSelection = false;
				e.consume();
				return;
			}
		}

		boolean isEditable = isEditable();
		if (isEdited || !isEditable) {
			if (allowSelection || !isEditable) {
				switch (e.getID()) {
					case JVGMouseEvent.MOUSE_CLICKED:
						caret.mouseClicked(e);
						break;

					case JVGMouseEvent.MOUSE_DRAGGED:
						caret.mouseDragged(e);
						break;

					case JVGMouseEvent.MOUSE_ENTERED:
						caret.mouseEntered(e);
						break;

					case JVGMouseEvent.MOUSE_EXITED:
						caret.mouseExited(e);
						break;

					case JVGMouseEvent.MOUSE_MOVED:
						caret.mouseMoved(e);
						break;

					case JVGMouseEvent.MOUSE_PRESSED:
						caret.setSelectionVisible(true);
						caret.mousePressed(e);
						break;

					case JVGMouseEvent.MOUSE_RELEASED:
						caret.mouseReleased(e);
						break;
				}
			}
		}

		if (isEdited) {
			setSelected(true);
			e.consume();
		}

		if (!e.isConsumed()) {
			processMouseEventOrig(e);
		}
	}

	protected void processMouseEventOrig(JVGMouseEvent e) {
		super.processMouseEvent(e);
	}

	private boolean isEdited = false;

	public boolean isEdited() {
		return isEdited;
	}

	private Cursor cursor;

	public void setEditMode(boolean isEdited) {
		if (this.isEdited != isEdited) {
			this.isEdited = isEdited;

			if (isEdited) {
				caret.setVisible(true);
				JVGActionArea.setActionActive(true);
				cursor = getCursor();
				setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
			} else {
				JVGActionArea.setActionActive(false);
				caret.setVisible(false);
				editor.select(0, 0);
				isEdited = false;
				if (cursor != null) {
					setCursor(cursor);
				}
			}
		}
	}

	public String getText() {
		try {
			return getDocument().getText(0, getDocument().getLength());
		} catch (BadLocationException exc) {
			return null;
		}
	}

	public int length() {
		return editor.getDocument().getLength();
	}

	public StyledDocument getDocument() {
		return editor.getStyledDocument();
	}

	public void addCaretListener(CaretListener listener) {
		editor.addCaretListener(listener);
	}

	public void removeCaretListener(CaretListener listener) {
		editor.removeCaretListener(listener);
	}

	public int getCaretPosition() {
		return editor.getCaretPosition();
	}

	public void setText(String text) {
		if (text == null) {
			text = "";
		}

		try {
			getDocument().remove(0, getDocument().getLength());
			getDocument().insertString(0, text, null);
		} catch (BadLocationException exc) {
			exc.printStackTrace();
		}
	}

	// Do not use frational metrics for width calculation!
	private FontRenderContext frc = new FontRenderContext(null, false, false);

	public FontRenderContext getFRC() {
		return frc;
	}

	@Override
	public void setAntialias(boolean antialias) {
		super.setAntialias(antialias);
		if (isAntialias() != antialias) {
			frc = new FontRenderContext(null, antialias, false);
		}
	}

	private Resource<Font> font;

	public void setFont(Resource<Font> font) {
		if (font != null && !font.equals(this.font)) {
			this.font = font;
			editor.setFont(font.getResource());
			invalidate();
		}
	}

	public Resource<Font> getFont() {
		if (font == null) {
			setFont(new FontResource(FontResource.DEFAULT_FONT));
		}
		return font;
	}

	@Override
	public boolean containsShape(double x, double y) {
		return getBounds().contains(x, y);
	}

	@Override
	protected Shape computeInitialBounds() {
		if (length() == 0) {
			Graphics2D g = getGraphics();
			if (g != null) {
				double ascent = g.getFontMetrics(font.getResource()).getAscent();
				Rectangle2D bounds = font.getResource().getMaxCharBounds(frc);
				bounds.setRect(bounds.getX(), bounds.getY() + ascent, isWrap() ? (float) getWrapSize() : bounds.getWidth(), bounds.getHeight());
				return bounds;
			} else {
				return null;
			}
		} else {
			Document doc = editor.getDocument();
			View rootView = getRootView();

			if (doc instanceof AbstractDocument) {
				((AbstractDocument) doc).readLock();
			}

			double width, height;
			try {
				rootView.setSize(getMaxWidth(), Integer.MAX_VALUE);
				width = isWrap ? wrapSize : rootView.getPreferredSpan(View.X_AXIS);
				height = rootView.getPreferredSpan(View.Y_AXIS);
			} finally {
				if (doc instanceof AbstractDocument) {
					((AbstractDocument) doc).readUnlock();
				}
			}
			return new Rectangle2D.Double(0, 0, width, height);
		}
	}

	@Override
	protected Shape computeBounds() {
		if (isOriginalBounds) {
			if (originalBounds == null) {
				originalBounds = computeOriginalBounds();
			}
			return originalBounds;
		} else {
			if (initialBounds == null) {
				initialBounds = computeInitialBounds();
			}

			if (initialBounds != null) {
				AffineTransform transform = getTransform();
				if (transform.isIdentity()) {
					return initialBounds;
				} else {
					MutableGeneralPath path = new MutableGeneralPath(initialBounds);
					path.transform(transform);
					return path.getBounds2D();
				}
			}
		}

		return null;
	}

	private float getMaxWidth() {
		return isWrap ? (float) wrapSize : Float.MAX_VALUE;
	}

	public Rectangle modelToView(int pos) throws BadLocationException {
		return modelToView(pos, Position.Bias.Forward);
	}

	public Rectangle modelToView(int pos, Position.Bias b) throws BadLocationException {
		Shape s = getRootView().modelToView(pos, maxBounds, b);
		return s instanceof Rectangle ? (Rectangle) s : s.getBounds();
	}

	private static final Position.Bias[] discardBias = new Position.Bias[1];

	public int viewToModel(double x, double y) {
		return viewToModel(x, y, discardBias);
	}

	public int viewToModel(double x, double y, Position.Bias[] b) {
		double[] point = { x, y };
		getInverseTransform().transform(point, 0, point, 0, 1);
		x = point[0];
		y = point[1];
		return getRootView().viewToModel((float) x, (float) y, maxBounds, b);
	}

	@Override
	public final void paintShape(Graphics2D g) {
		g.transform(getTransform());

		Document doc = getDocument();
		if (doc instanceof AbstractDocument) {
			((AbstractDocument) doc).readLock();
		}

		try {
			paintSafely(g);
		} finally {
			if (doc instanceof AbstractDocument) {
				((AbstractDocument) doc).readUnlock();
			}
		}

		g.transform(getInverseTransform());
	}

	public void paintSafely(Graphics2D g) {
		// Draw highlighter
		if (isEdited) {
			Highlighter highlighter = editor.getHighlighter();
			if (highlighter != null) {
				highlighter.paint(g);
			}
		}

		// Draw text
		Paint composePaint = getDeepComposePaint();

		Graphics2D textGraphics = g;
		if (composePaint != null) {
			textGraphics = new JVGComposeGraphics(g, composePaint);
		}

		if (isAntialias()) {
			textGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		} else {
			textGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		}

		// TODO problem with caret when off
		if (fractionMetrics) {
			textGraphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		} else {
			textGraphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
		}

		View root = getRootView();
		try {
			Rectangle oldClip = textGraphics.getClipBounds();
			// clip is necessary for view drawing
			textGraphics.setClip(getInitialBounds());

			root.setSize(maxBounds.width, Integer.MAX_VALUE);
			root.paint(textGraphics, maxBounds);

			if (isAntialias()) {
				textGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
			}
			if (fractionMetrics) {
				textGraphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
			}

			// Draw caret
			if (isEdited) {
				if (caret != null) {
					caret.paint(g);
				}
			}
			textGraphics.setClip(oldClip);
		} catch (Throwable exc) {
			exc.printStackTrace();
		}
	}

	@Override
	public MutableGeneralPath getPath() {
		MutableGeneralPath path = new MutableGeneralPath();
		FontMetrics fontMetrics = getGraphics().getFontMetrics(font.getResource());

		try {
			StyledDocument d = getDocument();
			int pos = 0, end = d.getLength();
			while (pos < end) {
				Element e = d.getCharacterElement(pos);
				int next = e.getEndOffset();

				AttributeSet attr = e.getAttributes();
				Font font = Fonts.getFont(StyleConstants.getFontFamily(attr), (StyleConstants.isBold(attr) ? Font.BOLD : Font.PLAIN) | (StyleConstants.isItalic(attr) ? Font.ITALIC : Font.PLAIN), StyleConstants.getFontSize(attr));

				for (int i = pos; i < next; i++) {
					String text = d.getText(i, 1);
					Rectangle bounds = modelToView(i);
					GeneralPath shape = (GeneralPath) font.createGlyphVector(frc, text).getOutline();
					shape.transform(AffineTransform.getTranslateInstance(bounds.getX(), bounds.getY() + fontMetrics.getAscent()));

					path.append(shape, false);
				}

				pos = next;
			}
		} catch (BadLocationException exc) {
			exc.printStackTrace();
		}
		return path;
	}

	static ArrayList<Painter> defaultPainters = null;

	public static ArrayList<Painter> getDefaultPainters() {
		if (defaultPainters == null) {
			defaultPainters = new ArrayList<Painter>();
		}

		return defaultPainters;
	}

	public boolean isEditable() {
		Boolean isEditable = (Boolean) getClientProperty("editable");
		if (isEditable != null) {
			return isEditable;
		} else {
			JVGPane pane = getPane();
			if (pane != null) {
				return pane.isEditable();
			} else {
				return false;
			}
		}
	}

	public void setEditable(boolean isEditable) {
		setClientProperty("editable", isEditable);
	}

	private boolean isWrap = false;

	private Rectangle maxBounds = new Rectangle(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);

	public boolean isWrap() {
		return isWrap;
	}

	public void setWrap(boolean isWrap) {
		this.isWrap = isWrap;
		maxBounds.setRect(0, 0, isWrap ? wrapSize : Integer.MAX_VALUE, Integer.MAX_VALUE);
		invalidate();
		repaint();
	}

	private double wrapSize = 150;

	public double getWrapSize() {
		return wrapSize;
	}

	public void setWrapSize(double wrapSize) {
		Rectangle2D bounds = font.getResource().getMaxCharBounds(frc);
		if (wrapSize < bounds.getWidth()) {
			wrapSize = bounds.getWidth();
		}

		this.wrapSize = wrapSize;
		maxBounds.setRect(0, 0, isWrap ? wrapSize : Integer.MAX_VALUE, Integer.MAX_VALUE);
		invalidate();
	}

	public void setCurrentCharacterAttributes(AttributeSet attr, boolean replace) {
		int p0 = editor.getSelectionStart();
		int p1 = editor.getSelectionEnd();
		if (p0 != p1) {
			StyledDocument doc = getDocument();
			doc.setCharacterAttributes(p0, p1 - p0, attr, replace);
		}

		MutableAttributeSet inputAttributes = getInputAttributes();
		if (replace) {
			inputAttributes.removeAttributes(inputAttributes);
		}
		inputAttributes.addAttributes(attr);
	}

	public MutableAttributeSet getInputAttributes() {
		StyledEditorKit k = (StyledEditorKit) editor.getEditorKit();
		return k.getInputAttributes();
	}

	public final void setCurrentParagraphAttributes(AttributeSet attr, boolean replace) {
		int p0 = editor.getSelectionStart();
		int p1 = editor.getSelectionEnd();
		StyledDocument doc = getDocument();
		doc.setParagraphAttributes(p0, p1 - p0, attr, replace);

		MutableAttributeSet inputAttributes = getInputAttributes();
		if (replace) {
			inputAttributes.removeAttributes(inputAttributes);
		}
		inputAttributes.addAttributes(attr);
	}

	public List<Element> getSelectedParagraphs() {
		int p0 = editor.getSelectionStart();
		int p1 = editor.getSelectionEnd();

		List<Element> paragraphs = new ArrayList<Element>();
		for (int i = p0; i < p1;) {
			Element paragraph = editor.getStyledDocument().getParagraphElement(i);
			paragraphs.add(paragraph);
			i = paragraph.getEndOffset();
		}

		if (paragraphs.size() == 0) {
			Element currentParagraph = ((StyledDocument) editor.getDocument()).getParagraphElement(editor.getCaretPosition());
			paragraphs.add(currentParagraph);
		}
		return paragraphs;
	}

	// ============================================================================
	// === Transfer Data
	// ==========================================================
	// ============================================================================
	// class TextTransferHandler extends TransferHandler implements UIResource
	// {
	// private JVGPane exportComp;
	// private boolean shouldRemove;
	// private int p0;
	// private int p1;
	//
	// protected DataFlavor getImportFlavor(DataFlavor[] flavors)
	// {
	// for (int i = 0; i < flavors.length; i++)
	// {
	// String mime = flavors[i].getMimeType();
	// if (mime.startsWith("text/plain"))
	// {
	// return flavors[i];
	// }
	// else if (mime.startsWith("application/x-java-jvm-local-objectref")
	// && flavors[i].getRepresentationClass() == java.lang.String.class)
	// {
	// return flavors[i];
	// }
	// else if (flavors[i].equals(DataFlavor.stringFlavor))
	// {
	// return flavors[i];
	// }
	// }
	//
	// return null;
	// }
	//
	//
	// protected void handleReaderImport(Reader in, JVGText c) throws
	// BadLocationException, IOException
	// {
	// char[] buff = new char[1024];
	// int nch;
	// boolean lastWasCR = false;
	// int last;
	// StringBuffer sbuff = null;
	//
	// while ((nch = in.read(buff, 0, buff.length)) != -1)
	// {
	// if (sbuff == null)
	// {
	// sbuff = new StringBuffer(nch);
	// }
	// last = 0;
	//
	// for (int counter = 0; counter < nch; counter++)
	// {
	// switch (buff[counter])
	// {
	// case '\r':
	// if (lastWasCR)
	// {
	// if (counter == 0)
	// {
	// sbuff.append('\n');
	// }
	// else
	// {
	// buff[counter - 1] = '\n';
	// }
	// }
	// else
	// {
	// lastWasCR = true;
	// }
	// break;
	//
	// case '\n':
	// if (lastWasCR)
	// {
	// if (counter > (last + 1))
	// {
	// sbuff.append(buff, last, counter - last - 1);
	// }
	//
	// lastWasCR = false;
	// last = counter;
	// }
	// break;
	//
	// default:
	// if (lastWasCR)
	// {
	// if (counter == 0)
	// {
	// sbuff.append('\n');
	// }
	// else
	// {
	// buff[counter - 1] = '\n';
	// }
	// lastWasCR = false;
	// }
	// break;
	// }
	// }
	//
	// if (last < nch)
	// {
	// if (lastWasCR)
	// {
	// if (last < (nch - 1))
	// {
	// sbuff.append(buff, last, nch - last - 1);
	// }
	// }
	// else
	// {
	// sbuff.append(buff, last, nch - last);
	// }
	// }
	// }
	//
	// if (lastWasCR)
	// {
	// sbuff.append('\n');
	// }
	//
	// c.addRemoveUndoRedo(Math.min(c.getSelectionStart(), c.getSelectionEnd()),
	// Math.max(c.getSelectionStart(), c.getSelectionEnd()));
	// c.removeSelection();
	//
	// int pos = c.getSelectionStart();
	// String insert = sbuff != null ? sbuff.toString() : "";
	// c.insertText(pos, insert);
	// addInsertUndoRedo(pos, pos + insert.length());
	// pos += insert.length();
	// c.setSelection(pos, pos);
	// }
	//
	//
	// public int getSourceActions(JComponent c)
	// {
	// return COPY_OR_MOVE;
	// }
	//
	//
	// protected Transferable createTransferable(JComponent comp)
	// {
	// exportComp = (JVGPane) comp;
	// JVGComponent o = exportComp.getFocusOwner();
	// if (o instanceof JVGText)
	// {
	// JVGText text = (JVGText) o;
	// shouldRemove = true;
	// p0 = Math.min(text.getSelectionStart(), text.getSelectionEnd());
	// p1 = Math.max(text.getSelectionStart(), text.getSelectionEnd());
	// return (p0 != p1) ? (new TextTransferable(exportComp, p0, p1)) : null;
	// }
	// else
	// {
	// return null;
	// }
	// }
	//
	//
	// protected void exportDone(JComponent source, Transferable data, int
	// action)
	// {
	// if (shouldRemove && action == MOVE)
	// {
	// TextTransferable t = (TextTransferable) data;
	// t.removeText();
	// source.repaint();
	// }
	//
	// exportComp = null;
	// }
	//
	//
	// public boolean importData(JComponent comp, Transferable t)
	// {
	// JVGPane c = (JVGPane) comp;
	// JVGComponent o = c.getFocusOwner();
	// if (o instanceof JVGText)
	// {
	// JVGText text = (JVGText) o;
	//
	// if (c == exportComp && text.getSelectionEnd() >= p0 &&
	// text.getSelectionEnd() <= p1)
	// {
	// shouldRemove = false;
	// return true;
	// }
	//
	// boolean imported = false;
	// DataFlavor importFlavor = getImportFlavor(t.getTransferDataFlavors());
	// if (importFlavor != null)
	// {
	// try
	// {
	// InputContext ic = c.getInputContext();
	// if (ic != null)
	// {
	// ic.endComposition();
	// }
	// Reader r = importFlavor.getReaderForText(t);
	// handleReaderImport(r, text);
	// text.repaint();
	// imported = true;
	// }
	// catch (UnsupportedFlavorException ufe)
	// {
	// }
	// catch (BadLocationException ble)
	// {
	// }
	// catch (IOException ioe)
	// {
	// }
	// }
	// return imported;
	// }
	// else
	// {
	// return false;
	// }
	// }
	//
	//
	// public boolean canImport(JComponent comp, DataFlavor[] flavors)
	// {
	// JVGPane c = (JVGPane) comp;
	// if (!c.isEnabled())
	// {
	// return false;
	// }
	// return getImportFlavor(flavors) != null;
	// }
	//
	//
	// class TextTransferable implements Transferable, UIResource
	// {
	// String data;
	// int p0;
	// int p1;
	// JVGText t;
	//
	// TextTransferable(JVGPane c, int start, int end)
	// {
	// p0 = start;
	// p1 = end;
	//
	// JVGComponent o = c.getFocusOwner();
	// if (o instanceof JVGText)
	// {
	// t = (JVGText) o;
	// data = t.getSelectedText();
	// }
	// }
	//
	//
	// void removeText()
	// {
	// if (p0 != p1)
	// {
	// t.addRemoveUndoRedo(p0, p1);
	// t.removeText(p0, p1);
	// int pos = Math.min(p0, p1);
	// t.setSelection(pos, pos);
	// }
	// }
	//
	//
	// public Object getTransferData(DataFlavor flavor) throws
	// UnsupportedFlavorException, IOException
	// {
	// String data = (this.data == null) ? "" : this.data;
	// data = (data == null) ? "" : data;
	// if (String.class.equals(flavor.getRepresentationClass()))
	// {
	// return data;
	// }
	// else if (Reader.class.equals(flavor.getRepresentationClass()))
	// {
	// return new StringReader(data);
	// }
	// else if (InputStream.class.equals(flavor.getRepresentationClass()))
	// {
	// return new StringBufferInputStream(data);
	// }
	//
	// throw new UnsupportedFlavorException(flavor);
	// }
	//
	//
	// public boolean isDataFlavorSupported(DataFlavor flavor)
	// {
	// DataFlavor[] flavors = getTransferDataFlavors();
	// for (int i = 0; i < flavors.length; i++)
	// {
	// if (flavors[i].equals(flavor))
	// {
	// return true;
	// }
	// }
	// return false;
	// }
	//
	//
	// public DataFlavor[] getTransferDataFlavors()
	// {
	// return flavors;
	// }
	// }
	// }
	//
	//
	// final Action cutAction = new TransferAction("cut");
	// final Action copyAction = new TransferAction("copy");
	// final Action pasteAction = new TransferAction("paste");
	// class TransferAction extends AbstractAction implements UIResource
	// {
	// TransferAction(String name)
	// {
	// super(name);
	// }
	//
	//
	// public void actionPerformed(ActionEvent e)
	// {
	// JVGPane pane = getPane();
	// TransferHandler th = getTransferHandler();
	// Clipboard clipboard = getClipboard(getPane());
	// String name = (String) getValue(Action.NAME);
	// Transferable trans = null;
	// try
	// {
	// if ((clipboard != null) && (th != null) && (name != null))
	// {
	// if ("cut".equals(name))
	// {
	// th.exportToClipboard(pane, clipboard, TransferHandler.MOVE);
	// }
	// else if ("copy".equals(name))
	// {
	// th.exportToClipboard(pane, clipboard, TransferHandler.COPY);
	// }
	// else if ("paste".equals(name))
	// {
	// trans = clipboard.getContents(null);
	// }
	// }
	// }
	// catch (IllegalStateException ise)
	// {
	// UIManager.getLookAndFeel().provideErrorFeedback(pane);
	// return;
	// }
	//
	// if (trans != null)
	// {
	// th.importData(pane, trans);
	// }
	// }
	//
	//
	// private Clipboard getClipboard(JComponent c)
	// {
	// if (SwingUtilities2.canAccessSystemClipboard())
	// {
	// return c.getToolkit().getSystemClipboard();
	// }
	// Clipboard clipboard = (Clipboard)
	// sun.awt.AppContext.getAppContext().get(SandboxClipboardKey);
	// if (clipboard == null)
	// {
	// clipboard = new Clipboard("Sandboxed Component Clipboard");
	// sun.awt.AppContext.getAppContext().put(SandboxClipboardKey, clipboard);
	// }
	// return clipboard;
	// }
	// }
	//
	//
	// private static Object SandboxClipboardKey = new Object();
	// private static DataFlavor[] flavors;
	// static
	// {
	// try
	// {
	// flavors = new DataFlavor[3];
	// flavors[0] = new DataFlavor("text/plain;class=java.lang.String");
	// flavors[1] = new DataFlavor("text/plain;class=java.io.Reader");
	// flavors[2] = new
	// DataFlavor("text/plain;charset=unicode;class=java.io.InputStream");
	// }
	// catch (ClassNotFoundException cle)
	// {
	// System.err.println("Error initializing jvg.shape.JVGText.TextTransferHandler.TextTransferable");
	// }
	// }

	public void coptTo(JVGShape dst) {
		super.copyTo(dst);

		if (dst instanceof JVGStyledText) {
			JVGStyledText dstText = (JVGStyledText) dst;

			dstText.setEditable(isEditable());
			dstText.setFont(getFont());
			dstText.setText(getText());
			dstText.setWrap(isWrap());
			dstText.setWrapSize(getWrapSize());
		}
	}

	public boolean isFractionMetrics() {
		return fractionMetrics;
	}

	public void setFractionMetrics(boolean fractionMetrics) {
		this.fractionMetrics = fractionMetrics;
		invalidate();
	}

	@Override
	public void addPainter(int index, Painter painter) {
		super.addPainter(index, painter);
		if (painter.getType() == Painter.FILL) {
			MutableAttributeSet attr = new SimpleAttributeSet();
			JVGStyleConstants.setForeground(attr, painter.getPaint().getResource());
			document.setCharacterAttributes(0, document.getLength(), attr, true);
		}
		document.update();
	}

	@Override
	public Painter removePainter(int index) {
		Painter p = super.removePainter(index);
		if (p != null) {
			document.update();
		}
		return p;
	}
}
