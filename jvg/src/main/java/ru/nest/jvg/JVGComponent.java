package ru.nest.jvg;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.TransferHandler;
import javax.swing.event.UndoableEditEvent;

import ru.nest.jvg.event.JVGComponentEvent;
import ru.nest.jvg.event.JVGComponentListener;
import ru.nest.jvg.event.JVGEvent;
import ru.nest.jvg.event.JVGEventMulticaster;
import ru.nest.jvg.event.JVGFocusEvent;
import ru.nest.jvg.event.JVGFocusListener;
import ru.nest.jvg.event.JVGKeyEvent;
import ru.nest.jvg.event.JVGKeyListener;
import ru.nest.jvg.event.JVGMouseEvent;
import ru.nest.jvg.event.JVGMouseListener;
import ru.nest.jvg.event.JVGMouseWheelEvent;
import ru.nest.jvg.event.JVGMouseWheelListener;
import ru.nest.jvg.event.JVGPeerEvent;
import ru.nest.jvg.event.JVGPeerListener;
import ru.nest.jvg.event.JVGPropertyChangeEvent;
import ru.nest.jvg.event.JVGPropertyChangeListener;
import ru.nest.jvg.event.JVGSelectionEvent;
import ru.nest.jvg.event.JVGSelectionListener;
import ru.nest.jvg.geom.MutableGeneralPath;
import ru.nest.jvg.resource.Script;
import ru.nest.jvg.resource.ScriptResource;

public abstract class JVGComponent {
	public static int count = 0;

	public JVGComponent() {
		id = nextID(null);
		count++;
	}

	public void setID(Long id) {
		id = nextID(id);
	}

	private static Object lock = new Object();

	public final Object getLock() {
		return lock;
	}

	private static Set<Long> ids = new HashSet<>();

	private static Long nextID(Long id) {
		synchronized (ids) {
			if (id == null || ids.contains(id)) {
				id = 0L;
				while (ids.contains(id)) {
					id++;
				}
			}

			ids.add(id);
			return id;
		}
	}

	private Long id;

	protected JVGComponentType componentType = JVGComponentType.draw;

	protected JVGContainer parent;

	protected JVGPane pane;

	private boolean isScriptingEnabled = false;

	public Long getId() {
		return id;
	}

	public JVGContainer getParent() {
		return parent;
	}

	public JVGPane getPane() {
		return pane;
	}

	public JVGComponent getRoot() {
		JVGComponent c = this;
		while (c.parent != null) {
			c = c.parent;
		}
		return c;
	}

	public void addNotify() {
		synchronized (getLock()) {
			invalidate();
		}
	}

	public void removeNotify() {
		synchronized (getLock()) {
			JVGPane pane = getPane();
			if (pane != null) {
				if (isLastEntered()) {
					pane.setLastEnteredComponent(null);
					pane.clearCursor();
				}
			}
			clearFocusOwner();
			setSelected(false, false);
		}
	}

	public void setFocused(boolean isFocused) {
		if (isFocused) {
			requestFocus();
		} else {
			clearFocusOwner();
		}
	}

	private void clearFocusOwner() {
		JVGPane pane = getPane();
		if (pane != null) {
			JVGComponent focusOwner = pane.getFocusOwner();
			if (focusOwner == this) {
				pane.setFocusOwner(null);
			}
		}
	}

	public JVGComponent requestNextFocus() {
		JVGPane pane = getPane();
		if (pane != null) {
			JVGComponent focusOwner = pane.getFocusOwner();
			if (focusOwner != null) {
				JVGComponent nextOwner = getNextComponent(focusOwner);
				if (nextOwner != null && nextOwner != focusOwner) {
					nextOwner.requestFocus();
					return nextOwner;
				}
			}
		}
		return null;
	}

	private static JVGComponent getNextComponent(JVGComponent c) {
		if (c.parent != null) {
			int childs_count = c.parent.childrenCount;
			JVGComponent[] childs = c.parent.children;
			int index = -1;
			for (int i = 0; i < childs_count; i++) {
				if (childs[i] == c) {
					index = i;
					break;
				}
			}

			if (index >= 0) {
				index++;
				if (index == childs_count) {
					index = 0;
				}
				return childs[index];
			}
		}

		return null;
	}

	public JVGComponent requestPrevFocus() {
		JVGPane pane = getPane();
		if (pane != null) {
			JVGComponent focusOwner = pane.getFocusOwner();
			if (focusOwner != null) {
				JVGComponent prevOwner = getPrevComponent(focusOwner);
				if (prevOwner != null && prevOwner != focusOwner) {
					prevOwner.requestFocus();
					return prevOwner;
				}
			}
		}

		return null;
	}

	private static JVGComponent getPrevComponent(JVGComponent c) {
		if (c.parent != null) {
			int childs_count = c.parent.childrenCount;
			JVGComponent[] childs = c.parent.children;
			int index = -1;
			for (int i = childs_count - 1; i >= 0; i--) {
				if (childs[i] == c) {
					index = i;
					break;
				}
			}

			if (index >= 0) {
				index--;
				if (index == -1) {
					index = childs_count - 1;
				}
				return childs[index];
			}
		}

		return null;
	}

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		String oldName = this.name;
		this.name = name;
		dispatchEvent(new JVGPropertyChangeEvent(this, "name", oldName, name));
	}

	public boolean isSelected() {
		JVGPane pane = getPane();
		if (pane != null) {
			return pane.getSelectionManager().isSelected(this);
		} else {
			return false;
		}
	}

	private boolean selectable = true;

	protected boolean isSelectableInPane() {
		JVGPane pane = getPane();
		return selectable && pane != null && pane.isEditable();
	}

	public boolean isSelectable() {
		return selectable;
	}

	public void setSelectable(boolean selectable) {
		if (!selectable) {
			JVGPane pane = getPane();
			if (pane != null) {
				JVGSelectionModel selectionModel = pane.getSelectionManager();
				if (selectionModel != null) {
					if (selectionModel.isSelected(this)) {
						selectionModel.removeSelection(this);
					}
				}
			}
		}
		this.selectable = selectable;
	}

	public void setSelected(boolean selected, boolean clear) {
		if ((isSelectableInPane() && selected) || !selected) {
			JVGPane pane = getPane();
			if (pane != null) {
				JVGSelectionModel selectionModel = pane.getSelectionManager();
				if (selectionModel != null) {
					if (selected) {
						if (!selectionModel.isSelected(this)) {
							if (clear) {
								selectionModel.setSelection(this);
							} else {
								selectionModel.addSelection(this);
							}
						}
					} else {
						if (clear) {
							selectionModel.clearSelection();
						} else {
							if (selectionModel.isSelected(this)) {
								selectionModel.removeSelection(this);
							}
						}
					}
				}
			}
		}
	}

	public void setSelected(boolean selected) {
		setSelected(selected, true);
	}

	public void enableEvents() {
		newEventsOnly = true;
	}

	// --- Peer listeners ---
	protected JVGPeerListener peerListener = null;

	public void addPeerListener(JVGPeerListener listener) {
		if (listener != null) {
			peerListener = JVGEventMulticaster.add(peerListener, listener);
			newEventsOnly = true;
		}
	}

	public void removePeerListener(JVGPeerListener listener) {
		if (listener != null) {
			peerListener = JVGEventMulticaster.remove(peerListener, listener);
		}
	}

	protected void processPeerEvent(JVGPeerEvent e) {
		JVGPeerListener listener = peerListener;
		if (listener != null) {
			switch (e.getID()) {
				case JVGPeerEvent.CONNECTED_TO_PEER:
					listener.connectedToPeer(e);
					break;

				case JVGPeerEvent.DISCONNECTED_FROM_PEER:
					listener.disconnectedFromPeer(e);
					break;
			}
		}
	}

	// --- Component listeners ---
	protected JVGComponentListener componentListener = null;

	public void addComponentListener(JVGComponentListener listener) {
		if (listener != null) {
			componentListener = JVGEventMulticaster.add(componentListener, listener);
			newEventsOnly = true;
		}
	}

	public void removeComponentListener(JVGComponentListener listener) {
		if (listener != null) {
			componentListener = JVGEventMulticaster.remove(componentListener, listener);
		}
	}

	protected void processComponentEvent(JVGComponentEvent e) {
		JVGComponentListener listener = componentListener;
		if (listener != null) {
			switch (e.getID()) {
				case JVGComponentEvent.COMPONENT_TRANSFORMED:
					listener.componentTransformed(e);
					break;

				case JVGComponentEvent.COMPONENT_GEOMETRY_CHANGED:
					listener.componentGeometryChanged(e);
					break;

				case JVGComponentEvent.COMPONENT_SHOWN:
					listener.componentShown(e);
					break;

				case JVGComponentEvent.COMPONENT_HIDDEN:
					listener.componentHidden(e);
					break;
			}
		}
	}

	// --- Focus listeners ---
	JVGFocusListener focusListener = null;

	public void addFocusListener(JVGFocusListener l) {
		if (l == null) {
			return;
		}

		synchronized (this) {
			focusListener = JVGEventMulticaster.add(focusListener, l);
			newEventsOnly = true;
		}
	}

	public void removeFocusListener(JVGFocusListener l) {
		if (l == null) {
			return;
		}

		synchronized (this) {
			focusListener = JVGEventMulticaster.remove(focusListener, l);
		}
	}

	protected void processFocusEvent(JVGFocusEvent e) {
		JVGFocusListener listener = focusListener;
		if (listener != null) {
			int id = e.getID();
			switch (id) {
				case JVGFocusEvent.FOCUS_GAINED:
					listener.focusGained(e);
					break;

				case JVGFocusEvent.FOCUS_LOST:
					listener.focusLost(e);
					break;
			}
		}
	}

	// --- Selection listeners ---
	JVGSelectionListener selectionListener = null;

	public void addSelectionListener(JVGSelectionListener l) {
		if (l == null) {
			return;
		}

		synchronized (this) {
			selectionListener = JVGEventMulticaster.add(selectionListener, l);
			newEventsOnly = true;
		}
	}

	public void removeSelectionListener(JVGSelectionListener l) {
		if (l == null) {
			return;
		}

		synchronized (this) {
			selectionListener = JVGEventMulticaster.remove(selectionListener, l);
		}
	}

	protected void processSelectionEvent(JVGSelectionEvent e) {
		JVGSelectionListener listener = selectionListener;
		if (listener != null) {
			listener.selectionChanged(e);
		}
	}

	// --- Mouse listeners ---
	JVGMouseListener mouseListener = null;

	public void addMouseListener(JVGMouseListener l) {
		if (l == null) {
			return;
		}

		synchronized (this) {
			mouseListener = JVGEventMulticaster.add(mouseListener, l);
			newEventsOnly = true;
		}
	}

	public void removeMouseListener(JVGMouseListener l) {
		if (l == null) {
			return;
		}

		synchronized (this) {
			mouseListener = JVGEventMulticaster.remove(mouseListener, l);
		}
	}

	protected void processMouseEvent(JVGMouseEvent e) {
		processFocusAndSelect(e);

		if (e.isConsumed()) {
			return;
		}

		JVGMouseListener listener = mouseListener;
		if (listener != null) {
			int id = e.getID();
			switch (id) {
				case JVGMouseEvent.MOUSE_CLICKED:
					listener.mouseClicked(e);
					break;

				case JVGMouseEvent.MOUSE_DRAGGED:
					listener.mouseDragged(e);
					break;

				case JVGMouseEvent.MOUSE_ENTERED:
					listener.mouseEntered(e);
					break;

				case JVGMouseEvent.MOUSE_EXITED:
					listener.mouseExited(e);
					break;

				case JVGMouseEvent.MOUSE_MOVED:
					listener.mouseMoved(e);
					break;

				case JVGMouseEvent.MOUSE_PRESSED:
					listener.mousePressed(e);
					break;

				case JVGMouseEvent.MOUSE_RELEASED:
					listener.mouseReleased(e);
					break;
			}
		}
	}

	// --- Mouse Wheel listeners ---
	JVGMouseWheelListener mouseWheelListener = null;

	public void addMouseWheelListener(JVGMouseWheelListener l) {
		if (l == null) {
			return;
		}

		synchronized (this) {
			mouseWheelListener = JVGEventMulticaster.add(mouseWheelListener, l);
			newEventsOnly = true;
		}
	}

	public void removeMouseWheelListener(JVGMouseWheelListener l) {
		if (l == null) {
			return;
		}

		synchronized (this) {
			mouseWheelListener = JVGEventMulticaster.remove(mouseWheelListener, l);
		}
	}

	protected void processMouseWheelEvent(JVGMouseWheelEvent e) {
		JVGMouseWheelListener listener = mouseWheelListener;
		if (listener != null) {
			listener.mouseWheelMoved(e);
		}
	}

	// --- Key listeners ---
	private JVGKeyListener keyListener = null;

	public void addKeyListener(JVGKeyListener l) {
		if (l == null) {
			return;
		}

		synchronized (this) {
			keyListener = JVGEventMulticaster.add(keyListener, l);
			newEventsOnly = true;
		}
	}

	public void removeKeyListener(JVGKeyListener l) {
		if (l == null) {
			return;
		}

		synchronized (this) {
			keyListener = JVGEventMulticaster.remove(keyListener, l);
		}
	}

	protected void processKeyEvent(JVGKeyEvent e) {
		JVGKeyListener listener = keyListener;
		if (listener != null) {
			int id = e.getID();
			switch (id) {
				case JVGKeyEvent.KEY_PRESSED:
					listener.keyPressed(e);
					break;

				case JVGKeyEvent.KEY_RELEASED:
					listener.keyReleased(e);
					break;

				case JVGKeyEvent.KEY_TYPED:
					listener.keyTyped(e);
					break;
			}
		}
	}

	// --- Property change listeners ---
	JVGPropertyChangeListener propertyChangeListener = null;

	public void addPropertyChangeListener(JVGPropertyChangeListener l) {
		if (l == null) {
			return;
		}

		synchronized (this) {
			propertyChangeListener = JVGEventMulticaster.add(propertyChangeListener, l);
			newEventsOnly = true;
		}
	}

	public void removePropertyChangeListener(JVGPropertyChangeListener l) {
		if (l == null) {
			return;
		}

		synchronized (this) {
			propertyChangeListener = JVGEventMulticaster.remove(propertyChangeListener, l);
		}
	}

	protected void processPropertyChangeEvent(JVGPropertyChangeEvent e) {
		JVGPropertyChangeListener listener = propertyChangeListener;
		if (listener != null) {
			listener.propertyChange(e);
		}
	}

	// --- events ---
	public final void dispatchEvent(JVGEvent e) {
		dispatchEventImpl(e);
	}

	protected void dispatchEventImpl(JVGEvent e) {
		boolean isLastEntered = isLastEntered();
		if (e instanceof JVGPropertyChangeEvent) {
			JVGPropertyChangeEvent pe = (JVGPropertyChangeEvent) e;
			if (pe.getOldValue() != null && pe.getNewValue() != null && pe.getOldValue().equals(pe.getNewValue())) {
				return;
			}
			if (pe.getOldValue() == null && pe.getNewValue() == null) {
				return;
			}
		} else if (e instanceof JVGPeerEvent) {
			JVGPeerEvent pe = (JVGPeerEvent) e;
			if (pe.getID() == JVGPeerEvent.CONNECTED_TO_PEER) {
				pane = pe.getNewPeer();
			} else {
				pane = null;
			}
		}

		if (newEventsOnly) {
			processEvent(e);
		} else {
			// TODO check
			processFocusAndSelect(e);
		}

		// generate mouse enter / exit events on component add / remove 
		JVGPane pane = getPane();
		if (pane != null) {
			if (e instanceof JVGComponentEvent) {
				JVGComponentEvent ce = (JVGComponentEvent) e;
				switch (ce.getID()) {
					case JVGComponentEvent.COMPONENT_HIDDEN:
						if (isLastEntered) {
							double x = pane.getLastMouseX();
							double y = pane.getLastMouseY();
							JVGComponent root = getRoot();
							JVGMouseEvent mouseEnterEvent = new JVGMouseEvent(null, root, JVGMouseEvent.MOUSE_ENTERED, System.currentTimeMillis(), 0, x, y, x, x, 1, 0);
							processEnterEvent(mouseEnterEvent, root);
						}
						break;

					case JVGComponentEvent.COMPONENT_SHOWN:
						break;
				}
			}

			pane.dispatchEvent(e);
		}
	}

	protected void processEnterEvent(JVGMouseEvent e, JVGComponent c) {
	}

	protected void processExitEvent(JVGMouseEvent e) {
	}

	private void processFocusAndSelect(JVGEvent e) {
		if (e.getID() == JVGMouseEvent.MOUSE_PRESSED) {
			JVGMouseEvent me = (JVGMouseEvent) e;
			requestFocus();

			boolean selected = isSelected();
			if (!selected && !me.isControlDown()) {
				setSelected(true, true);
			} else if (me.isControlDown()) {
				setSelected(!selected, false);
			}
		}
	}

	private void invalidateSelection() {
		JVGPane pane = getPane();
		if (pane != null) {
			JVGSelectionModel selectionModel = pane.getSelectionManager();
			if (selectionModel != null && selectionModel.isValid() && selectionModel.isSelected(this)) {
				selectionModel.invalidate();
			}
		}
	}

	boolean newEventsOnly = false;

	protected void processEvent(JVGEvent e) {
		if (e instanceof JVGMouseEvent) {
			processMouseEvent((JVGMouseEvent) e);
			return;
		} else if (e instanceof JVGKeyEvent) {
			processKeyEvent((JVGKeyEvent) e);
			return;
		} else if (e instanceof JVGComponentEvent) {
			processComponentEvent((JVGComponentEvent) e);
			return;
		} else if (e instanceof JVGFocusEvent) {
			processFocusEvent((JVGFocusEvent) e);
			return;
		} else if (e instanceof JVGSelectionEvent) {
			processSelectionEvent((JVGSelectionEvent) e);
			return;
		} else if (e instanceof JVGPropertyChangeEvent) {
			processPropertyChangeEvent((JVGPropertyChangeEvent) e);
			return;
		} else if (e instanceof JVGPeerEvent) {
			processPeerEvent((JVGPeerEvent) e);
			return;
		}
	}

	protected boolean valid;

	public boolean isValid() {
		return valid;
	}

	public void executeScript(Script.Type type) {
		JVGPane pane = getPane();
		if (pane != null && (pane.isScriptingEnabled() || isScriptingEnabled())) {
			ScriptResource script = (ScriptResource) getClientProperty(type.getActionName());
			pane.getScriptManager().executeScript(script, getId());
		}
	}

	public void paintScript(Graphics2D g) {
		JVGPane pane = getPane();
		if (pane != null && (pane.isScriptingEnabled() || isScriptingEnabled())) {
			pane.getScriptManager().executeScript(this, g);
		}
	}

	public void validate() {
		if (!valid) {
			executeScript(Script.VALIDATION);

			// bounds
			bounds = computeBounds();
			if (bounds == null) {
				bounds = new Rectangle2D.Double(0, 0, 0, 0);
			}

			// rectangle bounds
			rectangleBounds = computeRectangleBounds();
			if (rectangleBounds == null) {
				rectangleBounds = new Rectangle2D.Double(0, 0, 0, 0);
			}

			screenBounds = computeScreenBounds();
			if (screenBounds == null) {
				screenBounds = new Rectangle(0, 0, 0, 0);
			}

			valid = true;
		}
	}

	public void invalidate() {
		if (valid) {
			valid = false;
			bounds = null;
			rectangleBounds = null;

			invalidateSelection();

			if (parent != null && parent.valid) {
				parent.invalidate();
			}
		}
	}

	private boolean visible = true;

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		if (visible) {
			show();
		} else {
			hide();
		}
	}

	private void show() {
		if (!visible) {
			synchronized (getLock()) {
				visible = true;

				if (componentListener != null) {
					JVGComponentEvent e = new JVGComponentEvent(this, JVGComponentEvent.COMPONENT_SHOWN);
					dispatchEvent(e);
				}
			}

			if (parent != null) {
				parent.invalidate();
			}
		}
	}

	protected void onMouseExit() {
		// TODO emulate mouse exit event
		JVGPane pane = getPane();
		if (pane != null) {
			if (isLastEntered()) {
				pane.setLastEnteredComponent(null);
				pane.clearCursor();
			}
		}
	}

	private void hide() {
		if (visible) {
			onMouseExit();
			synchronized (getLock()) {
				visible = false;
				if (isFocused()) {
					requestNextFocus();
				}

				if (componentListener != null) {
					JVGComponentEvent e = new JVGComponentEvent(this, JVGComponentEvent.COMPONENT_HIDDEN);
					dispatchEvent(e);
				}
			}

			if (parent != null) {
				parent.invalidate();
			}
		}
	}

	protected Shape computeBounds() {
		return null;
	}

	protected Shape bounds;

	public final Shape getBounds() {
		if (bounds == null) {
			validate();
		}
		return bounds;
	}

	private Rectangle2D rectangleBounds;

	public final Rectangle2D getRectangleBounds() {
		if (rectangleBounds == null) {
			validate();
		}
		return rectangleBounds;
	}

	protected Rectangle2D computeRectangleBounds() {
		Shape bounds = getBounds();
		if (bounds instanceof Rectangle2D) {
			return (Rectangle2D) bounds;
		} else {
			return bounds.getBounds2D();
		}
	}

	private Rectangle screenBounds;

	public final Rectangle getScreenBounds() {
		if (screenBounds == null) {
			validate();
		}
		return screenBounds;
	}

	protected final Rectangle computeScreenBounds() {
		JVGPane pane = getPane();
		if (pane != null && pane.isShowing()) {
			MutableGeneralPath bounds = new MutableGeneralPath(getBounds());
			AffineTransform paneTransform = pane.getTransform();
			if (paneTransform != null && !paneTransform.isIdentity()) {
				bounds.transform(paneTransform);
			}

			Point locationOnScreen = pane.getLocationOnScreen();

			Rectangle screenBounds = new Rectangle(bounds.getBounds());
			screenBounds.translate(locationOnScreen.x, locationOnScreen.y);
			return screenBounds;
		}
		return null;
	}

	public boolean isFocused() {
		JVGPane pane = getPane();
		if (pane != null) {
			return this == pane.getFocusOwner();
		} else {
			return false;
		}
	}

	public boolean isLastEntered() {
		JVGPane pane = getPane();
		if (pane != null) {
			return this == pane.getLastEnteredComponent();
		} else {
			return false;
		}
	}

	public void requestFocus() {
		if (isFocusable() && !isFocused() && isDisplable()) {
			JVGPane pane = getPane();
			if (pane != null) {
				pane.setFocusOwner(this);
			}
		}
	}

	private boolean focusable = true;

	public boolean isFocusable() {
		return focusable;
	}

	public void setFocusable(boolean focusable) {
		if (!focusable) {
			JVGPane pane = getPane();
			if (pane != null) {
				JVGComponent focusOwner = pane.getFocusOwner();
				if (this == focusOwner) {
					pane.setFocusOwner(null);
				}
			}
		}

		this.focusable = focusable;
	}

	public boolean isDisplable() {
		if (!visible || parent == null) {
			return false;
		} else {
			if (parent instanceof JVGRoot) {
				return true;
			} else {
				return parent.isDisplable();
			}
		}
	}

	public final void toFront() {
		if (parent != null) {
			parent.toFront(this, true);
		}
	}

	public final void toBack() {
		if (parent != null) {
			parent.toBack(this, true);
		}
	}

	public final int toUp() {
		if (parent != null) {
			return parent.toUp(this);
		} else {
			return -1;
		}
	}

	public final int toDown() {
		if (parent != null) {
			return parent.toDown(this);
		} else {
			return -1;
		}
	}

	public final void repaintComponent() {
		JVGPane pane = getPane();
		if (pane != null) {
			Rectangle2D bounds = getRectangleBounds();
			pane.repaint((int) bounds.getX() - 5, (int) bounds.getY() - 5, (int) bounds.getWidth() + 10, (int) bounds.getHeight() + 10);
		}
	}

	public final void repaint() {
		JVGPane pane = getPane();
		if (pane != null) {
			pane.repaint();
		}
	}

	public final void repaint(int x, int y, int w, int h) {
		JVGPane pane = getPane();
		if (pane != null) {
			pane.repaint(x, y, w, h);
		}
	}

	public void paint(Graphics2D g) {
		// paint component
		paintComponent(g);

		// paint scripted
		paintScript(g);
	}

	public void print(Graphics2D g) {
		// print component
		printComponent(g);
	}

	public void paintComponent(Graphics2D g) {
		// Transparent and empty on default
		if (!isValid()) {
			validate();
		}
	}

	public void printComponent(Graphics2D g) {
		// Transparent and empty on default
		if (!isValid()) {
			validate();
		}
	}

	private Color selectionColor = Color.darkGray;

	public void setSelectionColor(Color selectionColor) {
		Color oldValue = this.selectionColor;
		this.selectionColor = selectionColor;
		dispatchEvent(new JVGPropertyChangeEvent(this, "selection-color", oldValue, selectionColor));
	}

	public Color getSelectionColor() {
		return selectionColor;
	}

	private Stroke selectionStroke = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 3f, new float[] { 3f, 3f }, 0f);

	public void setSelectionStroke(Stroke selectionStroke) {
		Stroke oldValue = this.selectionStroke;
		this.selectionStroke = selectionStroke;
		dispatchEvent(new JVGPropertyChangeEvent(this, "selection-stroke", oldValue, selectionStroke));
	}

	public Stroke getSelectionStroke() {
		return selectionStroke;
	}

	protected Color focusColor = new Color(0, 180, 60, 100);

	protected Stroke focusStroke = new BasicStroke(4f);

	private Rectangle2D nullBounds = null;

	protected Shape getBoundsSafe() {
		Shape bounds = getBounds();
		if (bounds instanceof Rectangle2D) {
			Rectangle2D rectBounds = (Rectangle2D) bounds;
			double x = rectBounds.getX();
			double y = rectBounds.getY();
			double w = rectBounds.getWidth();
			double h = rectBounds.getHeight();

			if (w == 0) {
				if (nullBounds == null) {
					nullBounds = new Rectangle2D.Double();
				}
				x -= 1;
				w = 3;
			}

			if (h == 0) {
				if (nullBounds == null) {
					nullBounds = new Rectangle2D.Double();
				}
				y -= 1;
				h = 3;
			}

			if (nullBounds != null) {
				nullBounds.setRect(x, y, w, h);
				bounds = nullBounds;
			}
		}
		return bounds;
	}

	protected Shape getSelectionShape() {
		return getBoundsSafe();
	}

	protected void paintSelection(Graphics2D g) {
		Stroke oldStroke = g.getStroke();
		AffineTransform transform = g.getTransform();
		try {
			g.transform(transform.createInverse());
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}

		Object oldAntialiasing = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Shape shape = getSelectionShape();
		if (isFocused()) {
			JVGPane pane = getPane();
			if (pane != null && pane.getSelectionManager().getSelectionCount() > 1) {
				g.setColor(focusColor);
				g.setStroke(focusStroke);
				try {
					g.draw(transform.createTransformedShape(shape));
				} catch (Exception exc) {
				}
			}
		}

		g.setXORMode(Color.green);
		g.setColor(selectionColor);
		g.setStroke(selectionStroke);
		try {
			g.draw(transform.createTransformedShape(shape));
		} catch (Exception exc) {
			// TODO:
			// Exception in thread "AWT-EventQueue-0" java.lang.InternalError:
			// Unable to Stroke shape (setPenT4: invalid pen transformation
			// (singular))
		}

		g.setPaintMode();
		g.setStroke(oldStroke);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAntialiasing);
		g.transform(transform);
	}

	public boolean contains(double x, double y) {
		return false;
	}

	private Cursor cursor = Cursor.getDefaultCursor();

	public Cursor getCursor() {
		return cursor;
	}

	public void setCursor(Cursor cursor) {
		this.cursor = cursor;
		updateCursorImmediately();
	}

	private final void updateCursorImmediately() {
		JVGPane pane = getPane();
		if (pane != null) {
			if (pane.getLastEnteredComponent() == this) {
				pane.setCursor(getCursor());
			}
		}
	}

	// --- tooltip text ---
	public String getToolTipText() {
		return (String) getClientProperty("tooltip");
	}

	public void setToolTipText(String tooltip) {
		setClientProperty("tooltip", tooltip);
	}

	public String getToolTipText(int x, int y) {
		return getToolTipText();
	}

	public Point getToolTipLocation(int x, int y) {
		return null;
	}

	private Map<Object, Object> clientProperties = null;

	public Object getClientProperty(Object property) {
		if (clientProperties != null) {
			synchronized (clientProperties) {
				return clientProperties.get(property);
			}
		} else {
			return null;
		}
	}

	public void setClientProperty(Object property, Object value) {
		Object oldValue = null;
		if (value == null) {
			if (clientProperties != null) {
				synchronized (clientProperties) {
					oldValue = clientProperties.get(property);
					clientProperties.remove(property);
				}
			}
		} else {
			if (clientProperties == null) {
				clientProperties = new HashMap<>();
			}

			synchronized (clientProperties) {
				oldValue = clientProperties.get(property);
				clientProperties.put(property, value);
			}
		}

		if (oldValue != null && value != null && value.equals(oldValue)) {
			return;
		}
		if (oldValue == null && value == null) {
			return;
		}
		dispatchEvent(new JVGPropertyChangeEvent(this, property.toString(), oldValue, value));
	}

	public void setClientProperty(String type, String parameter, String value) {
		Map<String, String> hash = (Map<String, String>) getClientProperty(type);
		if (hash == null) {
			hash = new HashMap<>();
			hash.put(parameter, value);
			setClientProperty(type, hash);
		} else {
			hash.put(parameter, value);
		}
	}

	public String getClientProperty(String type, String parameter) {
		Map<String, String> hash = (Map<String, String>) getClientProperty(type);
		if (hash != null) {
			return hash.get(parameter);
		} else {
			return null;
		}
	}

	public Graphics2D getGraphics() {
		JVGPane pane = getPane();
		if (pane != null) {
			return (Graphics2D) pane.getGraphics();
		} else {
			return null;
		}
	}

	private static final StringBuffer TRANSFER_HANDLER_KEY = new StringBuffer("TransferHandler");

	public void setTransferHandler(TransferHandler newHandler) {
		setClientProperty(TRANSFER_HANDLER_KEY, newHandler);
	}

	public TransferHandler getTransferHandler() {
		return (TransferHandler) getClientProperty(TRANSFER_HANDLER_KEY);
	}

	private boolean isClipped = false;

	public boolean isClipped() {
		return isClipped;
	}

	public void setClipped(boolean isClipped) {
		boolean oldValue = this.isClipped;
		this.isClipped = isClipped;
		dispatchEvent(new JVGPropertyChangeEvent(this, "is-clipped", oldValue, isClipped));
	}

	public void fireUndoableEditUpdate(UndoableEditEvent e) {
		JVGPane pane = getPane();
		if (pane != null) {
			pane.fireUndoableEditUpdate(e);
		}
	}

	protected boolean isContainedIn(Shape shape) {
		Rectangle2D bounds = getRectangleBounds();
		if (bounds != null) {
			double x = bounds.getX();
			double y = bounds.getY();
			double w = bounds.getWidth();
			double h = bounds.getHeight();
			if (w <= 0) {
				w = 1;
			}

			if (h <= 0) {
				h = 1;
			}
			return shape.contains(x, y, w, h);
		}
		return false;
	}

	private Set<String> tags = null;

	public boolean addTag(String tag) {
		if (tags == null) {
			tags = new HashSet<>();
		}
		return tags.add(tag);
	}

	public boolean addTags(Collection<String> list) {
		if (tags == null) {
			tags = new HashSet<>();
		}
		return tags.addAll(list);
	}

	public boolean removeTag(String tag) {
		return tags != null ? tags.remove(tag) : false;
	}

	public boolean removeTags(Collection<String> list) {
		return tags != null ? tags.removeAll(list) : false;
	}

	public boolean hasTag(String tag) {
		return tags != null ? tags.contains(tags) : false;
	}

	public boolean hasTags(Collection<String> list) {
		return tags != null ? tags.containsAll(list) : false;
	}

	public boolean isScriptingEnabled() {
		return isScriptingEnabled;
	}

	public void setScriptingEnabled(boolean isScriptingEnabled) {
		this.isScriptingEnabled = isScriptingEnabled;
	}

	protected double getPaneScale() {
		JVGPane pane = getPane();
		if (pane != null) {
			AffineTransform transform = pane.getTransform();
			if (transform != null) {
				return transform.getScaleX();
			}
		}
		return 1;
	}

	public JVGComponentType getComponentType() {
		return componentType;
	}

	public boolean isClip() {
		return componentType == JVGComponentType.clip;
	}

	public void setComponentType(JVGComponentType componentType) {
		JVGComponentType oldValue = this.componentType;
		this.componentType = componentType;
		dispatchEvent(new JVGPropertyChangeEvent(this, "component-type", oldValue, componentType));
	}
}
