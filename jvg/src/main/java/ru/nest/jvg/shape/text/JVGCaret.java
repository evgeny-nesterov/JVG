package ru.nest.jvg.shape.text;

import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.EventListener;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;
import javax.swing.plaf.TextUI;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.NavigationFilter;
import javax.swing.text.Position;
import javax.swing.text.Segment;

import ru.nest.jvg.event.JVGFocusEvent;
import ru.nest.jvg.event.JVGFocusListener;
import ru.nest.jvg.event.JVGMouseEvent;
import ru.nest.jvg.shape.JVGStyledText;
import sun.swing.SwingUtilities2;

public class JVGCaret extends Rectangle implements Caret, JVGFocusListener {
	public static final int UPDATE_WHEN_ON_EDT = 0;

	public static final int NEVER_UPDATE = 1;

	public static final int ALWAYS_UPDATE = 2;

	private JVGStyledText text;

	public JVGCaret(JVGStyledText text) {
		this.text = text;
	}

	public void setUpdatePolicy(int policy) {
		updatePolicy = policy;
	}

	public int getUpdatePolicy() {
		return updatePolicy;
	}

	protected final JTextComponent getComponent() {
		return component;
	}

	protected final synchronized void repaint() {
		// if (component != null) {
		// component.repaint(x, y, width, height);
		// }
		text.repaint();
	}

	protected synchronized void damage(Rectangle r) {
		if (r != null) {
			int damageWidth = getCaretWidth(r.height);
			x = r.x - 4 - (damageWidth >> 1);
			y = r.y;
			width = 9 + damageWidth;
			height = r.height;
			repaint();
		}
	}

	/**
	 * Scrolls the associated view (if necessary) to make the caret visible. Since how this should be done is somewhat of a policy, this method
	 * can be reimplemented to change the behavior. By default the scrollRectToVisible method is called on the associated component.
	 * 
	 * @param nloc
	 *            the new position to scroll to
	 */
	// protected void adjustVisibility(Rectangle nloc) {
	// if(component == null) {
	// return;
	// }
	// if (SwingUtilities.isEventDispatchThread()) {
	// component.scrollRectToVisible(nloc);
	// } else {
	// SwingUtilities.invokeLater(new SafeScroller(nloc));
	// }
	// }

	protected Highlighter.HighlightPainter getSelectionPainter() {
		return DefaultHighlighter.DefaultPainter;
	}

	protected void positionCaret(JVGMouseEvent e) {
		Position.Bias[] biasRet = new Position.Bias[1];
		int pos = text.viewToModel(e.getX(), e.getY(), biasRet);
		if (biasRet[0] == null) {
			biasRet[0] = Position.Bias.Forward;
		}
		if (pos >= 0) {
			setDot(pos, biasRet[0]);
		}
	}

	protected void moveCaret(JVGMouseEvent e) {
		Position.Bias[] biasRet = new Position.Bias[1];
		int pos = text.viewToModel(e.getX(), e.getY(), biasRet);
		if (biasRet[0] == null) {
			biasRet[0] = Position.Bias.Forward;
		}
		if (pos >= 0) {
			moveDot(pos, biasRet[0]);
		}
	}

	@Override
	public void focusGained(JVGFocusEvent e) {
		if (text.isEditable() && text.isVisible()) {
			setSelectionVisible(true);
		}
	}

	@Override
	public void focusLost(JVGFocusEvent e) {
		setVisible(false);
		setSelectionVisible(ownsSelection); // || e.isTemporary());
		text.setEditMode(false);
	}

	private void selectWord(JVGMouseEvent e) {
		if (selectedWordEvent != null && selectedWordEvent.getX() == e.getX() && selectedWordEvent.getY() == e.getY()) {
			// we already done selection for this
			return;
		}
		Action a = null;
		ActionMap map = getComponent().getActionMap();
		if (map != null) {
			a = map.get(DefaultEditorKit.selectWordAction);
		}
		// if (a == null) {
		// if (selectWord == null) {
		// selectWord = new DefaultEditorKit.SelectWordAction();
		// }
		// a = selectWord;
		// }
		a.actionPerformed(new ActionEvent(getComponent(), ActionEvent.ACTION_PERFORMED, null, e.getWhen(), e.getModifiers()));
		selectedWordEvent = e;
	}

	public void mouseClicked(JVGMouseEvent e) {
		if (!e.isConsumed()) {
			int nclicks = e.getClickCount();
			if (e.getButton() == JVGMouseEvent.BUTTON1) {
				// mouse 1 behavior
				if (nclicks == 1) {
					selectedWordEvent = null;
				} else if (nclicks == 2) {
					selectWord(e);
					selectedWordEvent = null;
				} else if (nclicks == 3) {
					text.moveBeginLine(false);
					text.moveEndLine(true);
				}
			} else if (e.getButton() == JVGMouseEvent.BUTTON2) {
				// mouse 2 behavior
				if (nclicks == 1 && component.isEditable() && component.isEnabled()) {
					// paste system selection, if it exists
					JVGStyledText c = (JVGStyledText) e.getSource();
					if (c != null) {
						try {
							Toolkit tk = Toolkit.getDefaultToolkit();
							Clipboard buffer = tk.getSystemSelection();
							if (buffer != null) {
								// platform supports system selections, update
								// it.
								adjustCaret(e);
								TransferHandler th = c.getTransferHandler();
								if (th != null) {
									Transferable trans = null;

									try {
										trans = buffer.getContents(null);
									} catch (IllegalStateException exc) {
										// clipboard was unavailable
										UIManager.getLookAndFeel().provideErrorFeedback(getComponent());
									}

									if (trans != null) {
										th.importData(c.getPane(), trans);
									}
								}
								// adjustFocus(true);
							}
						} catch (HeadlessException he) {
							// do nothing... there is no system clipboard
						}
					}
				}
			}
		}
	}

	public void mousePressed(JVGMouseEvent e) {
		if (e.getButton() == JVGMouseEvent.BUTTON1) {
			if (e.isConsumed()) {
				shouldHandleRelease = true;
			} else {
				shouldHandleRelease = false;
				adjustCaretAndFocus(e);
				if (e.getClickCount() == 2) {
					// && SwingUtilities2.canEventAccessSystemClipboard(e)) {
					selectWord(e);
				}
			}
		}
	}

	void adjustCaretAndFocus(JVGMouseEvent e) {
		adjustCaret(e);
		// adjustFocus(false);
	}

	private void adjustCaret(JVGMouseEvent e) {
		if ((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0 && getDot() != -1) {
			moveCaret(e);
		} else {
			positionCaret(e);
		}
	}

	// private void adjustFocus(boolean inWindow)
	// {
	// if ((component != null) && component.isEnabled() &&
	// component.isRequestFocusEnabled())
	// {
	// if (inWindow)
	// {
	// component.requestFocusInWindow();
	// }
	// else
	// {
	// component.requestFocus();
	// }
	// }
	// }

	public void mouseReleased(JVGMouseEvent e) {
		if (!e.isConsumed() && shouldHandleRelease && e.getButton() == JVGMouseEvent.BUTTON1) {
			adjustCaretAndFocus(e);
		}
	}

	public void mouseEntered(JVGMouseEvent e) {
	}

	public void mouseExited(JVGMouseEvent e) {
	}

	public void mouseDragged(JVGMouseEvent e) {
		if (!e.isConsumed()) {
			moveCaret(e);
		}
	}

	public void mouseMoved(JVGMouseEvent e) {
	}

	@Override
	public void paint(Graphics g) {
		if (isVisible()) {
			try {
				Rectangle r = text.modelToView(dot, dotBias);

				if ((r == null) || ((r.width == 0) && (r.height == 0))) {
					return;
				}
				if (width > 0 && height > 0 && !this._contains(r.x, r.y, r.width, r.height)) {
					// We seem to have gotten out of sync and no longer
					// contain the right location, adjust accordingly.
					Rectangle clip = g.getClipBounds();

					if (clip != null && !clip.contains(this)) {
						// Clip doesn't contain the old location, force it
						// to be repainted lest we leave a caret around.
						repaint();
					}
					// This will potentially cause a repaint of something
					// we're already repainting, but without changing the
					// semantics of damage we can't really get around this.
					damage(r);
				}
				g.setColor(component.getCaretColor());
				int paintWidth = getCaretWidth(r.height);
				r.x -= paintWidth >> 1;
				g.fillRect(r.x, r.y, paintWidth, r.height - 1);

				// see if we should paint a flag to indicate the bias
				// of the caret.
				// PENDING(prinz) this should be done through
				// protected methods so that alternative LAF
				// will show bidi information.
				Document doc = component.getDocument();
				if (doc instanceof AbstractDocument) {
					Element bidi = ((AbstractDocument) doc).getBidiRootElement();
					if ((bidi != null) && (bidi.getElementCount() > 1)) {
						// there are multiple directions present.
						flagXPoints[0] = r.x + ((dotLTR) ? paintWidth : 0);
						flagYPoints[0] = r.y;
						flagXPoints[1] = flagXPoints[0];
						flagYPoints[1] = flagYPoints[0] + 4;
						flagXPoints[2] = flagXPoints[0] + ((dotLTR) ? 4 : -4);
						flagYPoints[2] = flagYPoints[0];
						g.fillPolygon(flagXPoints, flagYPoints, 3);
					}
				}
			} catch (BadLocationException exc) {
				// can't render I guess
				// System.err.println("Can't render cursor");
			}
		}
	}

	@Override
	public void install(JTextComponent c) {
		component = c;
		Document doc = c.getDocument();
		dot = mark = 0;
		dotLTR = markLTR = true;
		dotBias = markBias = Position.Bias.Forward;
		if (doc != null) {
			doc.addDocumentListener(handler);
		}
		c.addPropertyChangeListener(handler);
		text.addFocusListener(this);

		// if the component already has focus, it won't
		// be notified.
		if (component.hasFocus()) {
			focusGained(null);
		}

		aspectRatio = -1;
		caretWidth = 1;
		setBlinkRate(300);
		setSelectionVisible(true);
	}

	@Override
	public void deinstall(JTextComponent c) {
		text.removeFocusListener(this);
		c.removePropertyChangeListener(handler);
		Document doc = c.getDocument();
		if (doc != null) {
			doc.removeDocumentListener(handler);
		}
		synchronized (this) {
			component = null;
		}
		if (flasher != null) {
			flasher.stop();
		}
	}

	@Override
	public void addChangeListener(ChangeListener l) {
		listenerList.add(ChangeListener.class, l);
	}

	@Override
	public void removeChangeListener(ChangeListener l) {
		listenerList.remove(ChangeListener.class, l);
	}

	public ChangeListener[] getChangeListeners() {
		return listenerList.getListeners(ChangeListener.class);
	}

	protected void fireStateChanged() {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ChangeListener.class) {
				// Lazily create the event:
				if (changeEvent == null)
					changeEvent = new ChangeEvent(this);
				((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
			}
		}
	}

	public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
		return listenerList.getListeners(listenerType);
	}

	/**
	 * Changes the selection visibility.
	 * 
	 * @param vis
	 *            the new visibility
	 */
	@Override
	public void setSelectionVisible(boolean vis) {
		if (vis != selectionVisible) {
			selectionVisible = vis;
			if (selectionVisible) {
				// show
				Highlighter h = component.getHighlighter();
				if ((dot != mark) && (h != null) && (selectionTag == null)) {
					int p0 = Math.min(dot, mark);
					int p1 = Math.max(dot, mark);
					Highlighter.HighlightPainter p = getSelectionPainter();
					try {
						selectionTag = h.addHighlight(p0, p1, p);
					} catch (BadLocationException bl) {
						selectionTag = null;
					}
				}
			} else {
				// hide
				if (selectionTag != null) {
					Highlighter h = component.getHighlighter();
					h.removeHighlight(selectionTag);
					selectionTag = null;
				}
			}
		}
	}

	@Override
	public boolean isSelectionVisible() {
		return selectionVisible;
	}

	public boolean isActive() {
		return active;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	@Override
	public void setVisible(boolean e) {
		// focus lost notification can come in later after the
		// caret has been deinstalled, in which case the component
		// will be null.
		if (component != null) {
			active = e;
			TextUI mapper = component.getUI();
			if (visible != e) {
				visible = e;
				// repaint the caret
				try {
					Rectangle loc = text.modelToView(dot, dotBias);
					damage(loc);
				} catch (BadLocationException exc) {
					// hmm... not legally positioned
				}
			}
		}
		if (flasher != null) {
			if (visible) {
				flasher.start();
			} else {
				flasher.stop();
			}
		}
	}

	/**
	 * Sets the caret blink rate.
	 * 
	 * @param rate
	 *            the rate in milliseconds, 0 to stop blinking
	 * @see Caret#setBlinkRate
	 */
	@Override
	public void setBlinkRate(int rate) {
		if (rate != 0) {
			if (flasher == null) {
				flasher = new Timer(rate, handler);
			}
			flasher.setDelay(rate);
		} else {
			if (flasher != null) {
				flasher.stop();
				flasher.removeActionListener(handler);
				flasher = null;
			}
		}
	}

	@Override
	public int getBlinkRate() {
		return (flasher == null) ? 0 : flasher.getDelay();
	}

	@Override
	public int getDot() {
		return dot;
	}

	@Override
	public int getMark() {
		return mark;
	}

	@Override
	public void setDot(int dot) {
		setDot(dot, Position.Bias.Forward);
	}

	@Override
	public void moveDot(int dot) {
		moveDot(dot, Position.Bias.Forward);
	}

	void moveDot(int dot, Position.Bias dotBias) {
		if (!component.isEnabled()) {
			// don't allow selection on disabled components.
			setDot(dot, dotBias);
			return;
		}
		if (dot != this.dot) {
			NavigationFilter filter = component.getNavigationFilter();

			if (filter != null) {
				filter.moveDot(getFilterBypass(), dot, dotBias);
			} else {
				handleMoveDot(dot, dotBias);
			}
		}
	}

	void handleMoveDot(int dot, Position.Bias dotBias) {
		changeCaretPosition(dot, dotBias);

		if (selectionVisible) {
			Highlighter h = component.getHighlighter();
			if (h != null) {
				int p0 = Math.min(dot, mark);
				int p1 = Math.max(dot, mark);

				// if p0 == p1 then there should be no highlight, remove it if
				// necessary
				if (p0 == p1) {
					if (selectionTag != null) {
						h.removeHighlight(selectionTag);
						selectionTag = null;
					}
					// otherwise, change or add the highlight
				} else {
					try {
						if (selectionTag != null) {
							h.changeHighlight(selectionTag, p0, p1);
						} else {
							Highlighter.HighlightPainter p = getSelectionPainter();
							selectionTag = h.addHighlight(p0, p1, p);
						}
					} catch (BadLocationException e) {
						throw new RuntimeException("Bad caret position");
					}
				}
			}
		}
	}

	void setDot(int dot, Position.Bias dotBias) {
		NavigationFilter filter = component.getNavigationFilter();
		if (filter != null) {
			filter.setDot(getFilterBypass(), dot, dotBias);
		} else {
			handleSetDot(dot, dotBias);
		}
	}

	void handleSetDot(int dot, Position.Bias dotBias) {
		// move dot, if it changed
		Document doc = component.getDocument();
		if (doc != null) {
			dot = Math.min(dot, doc.getLength());
		}
		dot = Math.max(dot, 0);

		// The position (0,Backward) is out of range so disallow it.
		if (dot == 0)
			dotBias = Position.Bias.Forward;

		mark = dot;
		if (this.dot != dot || this.dotBias != dotBias || selectionTag != null || forceCaretPositionChange) {
			changeCaretPosition(dot, dotBias);
		}
		this.markBias = this.dotBias;
		this.markLTR = dotLTR;
		Highlighter h = component.getHighlighter();
		if ((h != null) && (selectionTag != null)) {
			h.removeHighlight(selectionTag);
			selectionTag = null;
		}
	}

	Position.Bias getDotBias() {
		return dotBias;
	}

	Position.Bias getMarkBias() {
		return markBias;
	}

	boolean isDotLeftToRight() {
		return dotLTR;
	}

	boolean isMarkLeftToRight() {
		return markLTR;
	}

	boolean isPositionLTR(int position, Position.Bias bias) {
		// Document doc = component.getDocument();
		// if(doc instanceof AbstractDocument ) {
		// if(bias == Position.Bias.Backward && --position < 0)
		// position = 0;
		// return ((AbstractDocument)doc).isLeftToRight(position, position);
		// }
		return true;
	}

	Position.Bias guessBiasForOffset(int offset, Position.Bias lastBias, boolean lastLTR) {
		// There is an abiguous case here. That if your model looks like:
		// abAB with the cursor at abB]A (visual representation of
		// 3 forward) deleting could either become abB] or
		// ab[B. I'ld actually prefer abB]. But, if I implement that
		// a delete at abBA] would result in aBA] vs a[BA which I
		// think is totally wrong. To get this right we need to know what
		// was deleted. And we could get this from the bidi structure
		// in the change event. So:
		// PENDING: base this off what was deleted.
		if (lastLTR != isPositionLTR(offset, lastBias)) {
			lastBias = Position.Bias.Backward;
		} else if (lastBias != Position.Bias.Backward && lastLTR != isPositionLTR(offset, Position.Bias.Backward)) {
			lastBias = Position.Bias.Backward;
		}
		if (lastBias == Position.Bias.Backward && offset > 0) {
			try {
				Segment s = new Segment();
				component.getDocument().getText(offset - 1, 1, s);
				if (s.count > 0 && s.array[s.offset] == '\n') {
					lastBias = Position.Bias.Forward;
				}
			} catch (BadLocationException ble) {
			}
		}
		return lastBias;
	}

	void changeCaretPosition(int dot, Position.Bias dotBias) {
		// repaint the old position and set the new value of
		// the dot.
		repaint();

		// Make sure the caret is visible if this window has the focus.
		if (flasher != null && flasher.isRunning()) {
			visible = true;
			flasher.restart();
		}

		// notify listeners at the caret moved
		this.dot = dot;
		this.dotBias = dotBias;
		dotLTR = isPositionLTR(dot, dotBias);
		fireStateChanged();

		updateSystemSelection();

		setMagicCaretPosition(null);

		// We try to repaint the caret later, since things
		// may be unstable at the time this is called
		// (i.e. we don't want to depend upon notification
		// order or the fact that this might happen on
		// an unsafe thread).
		Runnable callRepaintNewCaret = new Runnable() {
			@Override
			public void run() {
				repaintNewCaret();
			}
		};
		SwingUtilities.invokeLater(callRepaintNewCaret);
	}

	/**
	 * Repaints the new caret position, with the assumption that this is happening on the event thread so that calling <code>modelToView</code>
	 * is safe.
	 */
	void repaintNewCaret() {
		if (component != null) {
			TextUI mapper = component.getUI();
			Document doc = component.getDocument();
			if ((mapper != null) && (doc != null)) {
				// determine the new location and scroll if
				// not visible.
				Rectangle newLoc;
				try {
					newLoc = text.modelToView(this.dot, dotBias);
				} catch (BadLocationException e) {
					newLoc = null;
				}
				if (newLoc != null) {
					// adjustVisibility(newLoc);
					// If there is no magic caret position, make one
					if (getMagicCaretPosition() == null) {
						setMagicCaretPosition(new Point(newLoc.x, newLoc.y));
					}
				}

				// repaint the new position
				damage(newLoc);
			}
		}
	}

	private void updateSystemSelection() {
		if (!SwingUtilities2.canCurrentEventAccessSystemClipboard()) {
			return;
		}
		if (this.dot != this.mark && component != null) {
			Clipboard clip = getSystemSelection();
			if (clip != null) {
				String selectedText = null;
				if (component instanceof JPasswordField && component.getClientProperty("JPasswordField.cutCopyAllowed") != Boolean.TRUE) {
					// fix for 4793761
					StringBuffer txt = null;
					char echoChar = ((JPasswordField) component).getEchoChar();
					int p0 = Math.min(getDot(), getMark());
					int p1 = Math.max(getDot(), getMark());
					for (int i = p0; i < p1; i++) {
						if (txt == null) {
							txt = new StringBuffer();
						}
						txt.append(echoChar);
					}
					selectedText = (txt != null) ? txt.toString() : null;
				} else {
					selectedText = component.getSelectedText();
				}
				try {
					clip.setContents(new StringSelection(selectedText), getClipboardOwner());

					ownsSelection = true;
				} catch (IllegalStateException ise) {
					// clipboard was unavailable
					// no need to provide error feedback to user since updating
					// the system selection is not a user invoked action
				}
			}
		}
	}

	private Clipboard getSystemSelection() {
		try {
			return component.getToolkit().getSystemSelection();
		} catch (HeadlessException he) {
			// do nothing... there is no system clipboard
		} catch (SecurityException se) {
			// do nothing... there is no allowed system clipboard
		}
		return null;
	}

	private ClipboardOwner getClipboardOwner() {
		return handler;
	}

	private void ensureValidPosition() {
		int length = component.getDocument().getLength();
		if (dot > length || mark > length) {
			// Current location is bogus and filter likely vetoed the
			// change, force the reset without giving the filter a
			// chance at changing it.
			handleSetDot(length, Position.Bias.Forward);
		}
	}

	@Override
	public void setMagicCaretPosition(Point p) {
		magicCaretPosition = p;
	}

	/**
	 * Gets the saved caret position.
	 * 
	 * @return the position see #setMagicCaretPosition
	 */
	@Override
	public Point getMagicCaretPosition() {
		return magicCaretPosition;
	}

	@Override
	public boolean equals(Object obj) {
		return (this == obj);
	}

	@Override
	public String toString() {
		String s = "Dot=(" + dot + ", " + dotBias + ")";
		s += " Mark=(" + mark + ", " + markBias + ")";
		return s;
	}

	private NavigationFilter.FilterBypass getFilterBypass() {
		if (filterBypass == null) {
			filterBypass = new DefaultFilterBypass();
		}
		return filterBypass;
	}

	// Rectangle.contains returns false if passed a rect with a w or h == 0,
	// this won't (assuming X,Y are contained with this rectangle).
	private boolean _contains(int X, int Y, int W, int H) {
		int w = this.width;
		int h = this.height;
		if ((w | h | W | H) < 0) {
			// At least one of the dimensions is negative...
			return false;
		}
		// Note: if any dimension is zero, tests below must return false...
		int x = this.x;
		int y = this.y;
		if (X < x || Y < y) {
			return false;
		}
		if (W > 0) {
			w += x;
			W += X;
			if (W <= X) {
				// X+W overflowed or W was zero, return false if...
				// either original w or W was zero or
				// x+w did not overflow or
				// the overflowed x+w is smaller than the overflowed X+W
				if (w >= x || W > w)
					return false;
			} else {
				// X+W did not overflow and W was not zero, return false if...
				// original w was zero or
				// x+w did not overflow and x+w is smaller than X+W
				if (w >= x && W > w)
					return false;
			}
		} else if ((x + w) < X) {
			return false;
		}
		if (H > 0) {
			h += y;
			H += Y;
			if (H <= Y) {
				if (h >= y || H > h)
					return false;
			} else {
				if (h >= y && H > h)
					return false;
			}
		} else if ((y + h) < Y) {
			return false;
		}
		return true;
	}

	int getCaretWidth(int height) {
		if (aspectRatio > -1) {
			return (int) (aspectRatio * height) + 1;
		}

		if (caretWidth > -1) {
			return caretWidth;
		}
		return 1;
	}

	// --- serialization ---------------------------------------------

	private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
		s.defaultReadObject();
		handler = new Handler();
		if (!s.readBoolean()) {
			dotBias = Position.Bias.Forward;
		} else {
			dotBias = Position.Bias.Backward;
		}
		if (!s.readBoolean()) {
			markBias = Position.Bias.Forward;
		} else {
			markBias = Position.Bias.Backward;
		}
	}

	private void writeObject(ObjectOutputStream s) throws IOException {
		s.defaultWriteObject();
		s.writeBoolean((dotBias == Position.Bias.Backward));
		s.writeBoolean((markBias == Position.Bias.Backward));
	}

	protected EventListenerList listenerList = new EventListenerList();

	protected transient ChangeEvent changeEvent = null;

	JTextComponent component;

	int updatePolicy = UPDATE_WHEN_ON_EDT;

	boolean visible;

	boolean active;

	int dot;

	int mark;

	Object selectionTag;

	boolean selectionVisible;

	Timer flasher;

	Point magicCaretPosition;

	transient Position.Bias dotBias;

	transient Position.Bias markBias;

	boolean dotLTR;

	boolean markLTR;

	transient Handler handler = new Handler();

	transient private int[] flagXPoints = new int[3];

	transient private int[] flagYPoints = new int[3];

	private transient NavigationFilter.FilterBypass filterBypass;

	static private transient Action selectWord = null;

	static private transient Action selectLine = null;

	private boolean ownsSelection;

	private boolean forceCaretPositionChange;

	private transient boolean shouldHandleRelease;

	private transient JVGMouseEvent selectedWordEvent = null;

	private int caretWidth = -1;

	private float aspectRatio = -1;

	class Handler implements PropertyChangeListener, DocumentListener, ActionListener, ClipboardOwner {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (width == 0 || height == 0) {
				// setVisible(true) will cause a scroll, only do this if the
				// new location is really valid.
				if (component != null) {
					try {
						Rectangle r = text.modelToView(dot, dotBias);
						if (r != null && r.width != 0 && r.height != 0) {
							damage(r);
						}
					} catch (BadLocationException ble) {
					}
				}
			}
			visible = !visible;
			repaint();
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			if (getUpdatePolicy() == NEVER_UPDATE || (getUpdatePolicy() == UPDATE_WHEN_ON_EDT && !SwingUtilities.isEventDispatchThread())) {

				if ((e.getOffset() <= dot || e.getOffset() <= mark) && selectionTag != null) {
					try {
						component.getHighlighter().changeHighlight(selectionTag, Math.min(dot, mark), Math.max(dot, mark));
					} catch (BadLocationException exc) {
						exc.printStackTrace();
					}
				}
				return;
			}

			int offset = e.getOffset();
			int length = e.getLength();
			int newDot = dot;
			short changed = 0;

			if (newDot >= offset) {
				newDot += length;
				changed |= 1;
			}
			int newMark = mark;
			if (newMark >= offset) {
				newMark += length;
				changed |= 2;
			}

			if (changed != 0) {
				Position.Bias dotBias = JVGCaret.this.dotBias;
				if (dot == offset) {
					Document doc = component.getDocument();
					boolean isNewline;
					try {
						Segment s = new Segment();
						doc.getText(newDot - 1, 1, s);
						isNewline = (s.count > 0 && s.array[s.offset] == '\n');
					} catch (BadLocationException exc) {
						isNewline = false;
					}
					if (isNewline) {
						dotBias = Position.Bias.Forward;
					} else {
						dotBias = Position.Bias.Backward;
					}
				}
				if (newMark == newDot) {
					setDot(newDot, dotBias);
					ensureValidPosition();
				} else {
					setDot(newMark, markBias);
					if (getDot() == newMark) {
						// Due this test in case the filter vetoed the
						// change in which case this probably won't be
						// valid either.
						moveDot(newDot, dotBias);
					}
					ensureValidPosition();
				}
			}
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			if (getUpdatePolicy() == NEVER_UPDATE || (getUpdatePolicy() == UPDATE_WHEN_ON_EDT && !SwingUtilities.isEventDispatchThread())) {

				int length = component.getDocument().getLength();
				dot = Math.min(dot, length);
				mark = Math.min(mark, length);
				if ((e.getOffset() < dot || e.getOffset() < mark) && selectionTag != null) {
					try {
						component.getHighlighter().changeHighlight(selectionTag, Math.min(dot, mark), Math.max(dot, mark));
					} catch (BadLocationException e1) {
						e1.printStackTrace();
					}
				}
				return;
			}
			int offs0 = e.getOffset();
			int offs1 = offs0 + e.getLength();
			int newDot = dot;
			boolean adjustDotBias = false;
			int newMark = mark;
			boolean adjustMarkBias = false;

			if (newDot >= offs1) {
				newDot -= (offs1 - offs0);
				if (newDot == offs1) {
					adjustDotBias = true;
				}
			} else if (newDot >= offs0) {
				newDot = offs0;
				adjustDotBias = true;
			}
			if (newMark >= offs1) {
				newMark -= (offs1 - offs0);
				if (newMark == offs1) {
					adjustMarkBias = true;
				}
			} else if (newMark >= offs0) {
				newMark = offs0;
				adjustMarkBias = true;
			}
			if (newMark == newDot) {
				forceCaretPositionChange = true;
				try {
					setDot(newDot, guessBiasForOffset(newDot, dotBias, dotLTR));
				} finally {
					forceCaretPositionChange = false;
				}
				ensureValidPosition();
			} else {
				Position.Bias dotBias = JVGCaret.this.dotBias;
				Position.Bias markBias = JVGCaret.this.markBias;
				if (adjustDotBias) {
					dotBias = guessBiasForOffset(newDot, dotBias, dotLTR);
				}
				if (adjustMarkBias) {
					markBias = guessBiasForOffset(mark, markBias, markLTR);
				}
				setDot(newMark, markBias);
				if (getDot() == newMark) {
					// Due this test in case the filter vetoed the change
					// in which case this probably won't be valid either.
					moveDot(newDot, dotBias);
				}
				ensureValidPosition();
			}
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			if (getUpdatePolicy() == NEVER_UPDATE || (getUpdatePolicy() == UPDATE_WHEN_ON_EDT && !SwingUtilities.isEventDispatchThread())) {
				return;
			}
			// if(e instanceof AbstractDocument.UndoRedoDocumentEvent) {
			// setDot(e.getOffset() + e.getLength());
			// }
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			Object oldValue = evt.getOldValue();
			Object newValue = evt.getNewValue();
			if ((oldValue instanceof Document) || (newValue instanceof Document)) {
				setDot(0);
				if (oldValue != null) {
					((Document) oldValue).removeDocumentListener(this);
				}
				if (newValue != null) {
					((Document) newValue).addDocumentListener(this);
				}
			} else if ("enabled".equals(evt.getPropertyName())) {
				Boolean enabled = (Boolean) evt.getNewValue();
				if (component.isFocusOwner()) {
					if (enabled == Boolean.TRUE) {
						if (component.isEditable()) {
							setVisible(true);
						}
						setSelectionVisible(true);
					} else {
						setVisible(false);
						setSelectionVisible(false);
					}
				}
			} else if ("caretWidth".equals(evt.getPropertyName())) {
				Integer newWidth = (Integer) evt.getNewValue();
				if (newWidth != null) {
					caretWidth = newWidth.intValue();
				} else {
					caretWidth = -1;
				}
				repaint();
			} else if ("caretAspectRatio".equals(evt.getPropertyName())) {
				Number newRatio = (Number) evt.getNewValue();
				if (newRatio != null) {
					aspectRatio = newRatio.floatValue();
				} else {
					aspectRatio = -1;
				}
				repaint();
			}
		}

		@Override
		public void lostOwnership(Clipboard clipboard, Transferable contents) {
			if (ownsSelection) {
				ownsSelection = false;
				if (component != null && !component.hasFocus()) {
					setSelectionVisible(false);
				}
			}
		}
	}

	private class DefaultFilterBypass extends NavigationFilter.FilterBypass {
		@Override
		public Caret getCaret() {
			return JVGCaret.this;
		}

		@Override
		public void setDot(int dot, Position.Bias bias) {
			handleSetDot(dot, bias);
		}

		@Override
		public void moveDot(int dot, Position.Bias bias) {
			handleMoveDot(dot, bias);
		}
	}
}
