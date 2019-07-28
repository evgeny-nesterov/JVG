package ru.nest.jvg;

import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.ImageCapabilities;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.VolatileImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.event.EventListenerList;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import ru.nest.jvg.editor.JVGPaneInternalFrame;
import ru.nest.jvg.editor.editoraction.InsertEditorAction;
import ru.nest.jvg.event.JVGComponentEvent;
import ru.nest.jvg.event.JVGContainerEvent;
import ru.nest.jvg.event.JVGEvent;
import ru.nest.jvg.event.JVGFocusEvent;
import ru.nest.jvg.event.JVGKeyEvent;
import ru.nest.jvg.event.JVGListener;
import ru.nest.jvg.event.JVGMouseEvent;
import ru.nest.jvg.event.JVGMouseWheelEvent;
import ru.nest.jvg.event.JVGPeerEvent;
import ru.nest.jvg.event.JVGPropertyChangeEvent;
import ru.nest.jvg.event.JVGSelectionEvent;
import ru.nest.jvg.event.JVGSelectionListener;
import ru.nest.jvg.macros.JVGMacros;
import ru.nest.jvg.macros.JVGMacrosCode;
import ru.nest.jvg.parser.JVGParseException;
import ru.nest.jvg.parser.JVGParser;
import ru.nest.jvg.resource.Script;
import ru.nest.jvg.resource.ScriptResource;
import ru.nest.jvg.undoredo.CompoundUndoRedo;
import sun.awt.AppContext;
import sun.print.ProxyPrintGraphics;
import sun.swing.SwingUtilities2;

public class JVGPane extends JComponent implements JVGSelectionListener, DropTargetListener, DragSourceListener {
	public final static double MIN_ZOOM = 0.1;

	public final static double MAX_ZOOM = 10.0;

	private DragSource dragSource;

	private DropTarget dropTarget;

	private JVGPaneDrop drop;

	public JVGPane() {
		setRequestFocusEnabled(true);
		setFocusable(true);
		setDoubleBuffered(true);
		setOpaque(true);
		setBackground(Color.white);
		setFocusCycleRoot(true);
		updateDocumentSize();
		setFocusTraversalKeysEnabled(false);

		addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				AppContext.getAppContext().put(FOCUSED_PANE, e.getSource());
			}
		});

		enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK | AWTEvent.KEY_EVENT_MASK | AWTEvent.COMPONENT_EVENT_MASK);

		selectionManager.addSelectionListener(this);
		ToolTipManager.sharedInstance().setReshowDelay(0);
		ToolTipManager.sharedInstance().setInitialDelay(0);

		addUndoableEditListener(new JVGUndoableListener());
		undoManager.setLimit(2048);

		setTransferHandler(transferHandler);

		dragSource = DragSource.getDefaultDragSource();
		dropTarget = new DropTarget(this, this);
	}

	private Dimension defaultDocumentSize = new Dimension(700, 500);

	private Dimension documentSize;

	private boolean isDocumentSizeSet;

	public boolean isDocumentSizeSet() {
		return isDocumentSizeSet;
	}

	public Dimension getDocumentSize() {
		if (isDocumentSizeSet && documentSize != null && documentSize.width > 0 && documentSize.height > 0) {
			return documentSize;
		} else {
			return defaultDocumentSize;
		}
	}

	public void setDocumentSize(Dimension documentSize) {
		if (documentSize != null && (documentSize.width == 0 || documentSize.height == 0)) {
			documentSize = null;
		}

		Dimension oldDocumentSize = this.documentSize;
		this.documentSize = documentSize;
		isDocumentSizeSet = documentSize != null;

		if ((oldDocumentSize == null && documentSize != null) || (oldDocumentSize != null && documentSize == null) || (oldDocumentSize != null && documentSize != null && !oldDocumentSize.equals(documentSize))) {
			firePropertyChange("document-size", oldDocumentSize, documentSize);
			updateDocumentSize();
			revalidate();
			repaint();
		}
	}

	public void update() {
		updateDocumentSize();
		updateTransform();
		repaint();
	}

	public void updateDocumentSize() {
		Dimension documentSize = getDocumentSize();
		Insets documentInsets = getDocumentInsets();
		double zoom = getZoom();
		setPreferredSize(new Dimension((int) (zoom * (documentSize.width + documentInsets.left + documentInsets.right)), (int) (zoom * (documentSize.height + documentInsets.top + documentInsets.bottom))));
	}

	public double getZoom() {
		Double zoom = (Double) getClientProperty("zoom");
		if (zoom == null) {
			return 1;
		} else {
			return zoom;
		}
	}

	public void setZoom(double zoom) {
		double oldValue = getZoom();
		if (zoom < MIN_ZOOM) {
			zoom = MIN_ZOOM;
		} else if (zoom > MAX_ZOOM) {
			zoom = MAX_ZOOM;
		}

		if (zoom != oldValue) {
			putClientProperty("zoom", zoom);
			updateDocumentSize();
			updateTransform();
		}
	}

	private Insets defaultDocumentInsets = new Insets(0, 0, 0, 0);

	public Insets getDocumentInsets() {
		Insets insets = (Insets) getClientProperty("document-insets");
		if (insets == null) {
			insets = defaultDocumentInsets;
		}
		return insets;
	}

	public void setDocumentInsets(Insets documentInsets) {
		putClientProperty("document-insets", documentInsets);
		updateTransform();
	}

	protected void updateTransform() {
		double zoom = getZoom();
		Insets documentInsets = getDocumentInsets();
		AffineTransform transform = AffineTransform.getTranslateInstance(documentInsets.left, documentInsets.top);
		transform.preConcatenate(AffineTransform.getScaleInstance(zoom, zoom));
		setTransform(transform);
		updateGridImage();
	}

	@Override
	public void addNotify() {
		super.addNotify();
		ToolTipManager.sharedInstance().registerComponent(this);
	}

	@Override
	public void removeNotify() {
		ToolTipManager.sharedInstance().unregisterComponent(this);
		if (getFocusedPane() == this) {
			AppContext.getAppContext().remove(FOCUSED_PANE);
		}
		super.removeNotify();
	}

	private JVGRoot root;

	public JVGRoot getRoot() {
		if (root == null) {
			JVGRoot root = getEditorKit().getFactory().createComponent(JVGRoot.class, new Object[] { JVGPane.this });
			setRoot(root);
		}
		return root;
	}

	public void setRoot(JVGRoot root) {
		if (root != null) {
			this.root = root;
			root.setPane(this);

			if (isScriptingEnabled()) {
				ScriptResource script = (ScriptResource) getClientProperty(Script.START.getActionName());
				getScriptManager().executeScript(script);
			}
		}
	}

	@Override
	public void processMouseEvent(MouseEvent e) {
		super.processMouseEvent(e);
		processME(e);
	}

	@Override
	public void processMouseMotionEvent(MouseEvent e) {
		super.processMouseMotionEvent(e);
		processME(e);
	}

	private void processME(MouseEvent e) {
		Point2D point = new Point2D.Double(e.getX(), e.getY());
		if (transform != null) {
			try {
				transform.inverseTransform(point, point);
			} catch (NoninvertibleTransformException exc) {
			}
		}

		Point2D adjustedPoint = new Point2D.Double(point.getX(), point.getY());
		if (!e.isControlDown()) {
			adjustPoint(adjustedPoint);
		}

		processMouseEvent(e, point.getX(), point.getY(), adjustedPoint.getX(), adjustedPoint.getY());
	}

	private double lastMouseX;

	private double lastMouseY;

	protected double getLastMouseX() {
		return lastMouseX;
	}

	protected double getLastMouseY() {
		return lastMouseY;
	}

	public void processMouseEvent(MouseEvent e, double x, double y, double adjustX, double adjustY) {
		switch (e.getID()) {
			case MouseEvent.MOUSE_MOVED:
			case MouseEvent.MOUSE_PRESSED:
			case MouseEvent.MOUSE_CLICKED:
			case MouseEvent.MOUSE_DRAGGED:
				lastMouseX = e.getX();
				lastMouseY = e.getY();
		}

		getRoot().dispatchEvent(new JVGMouseEvent(e, root, e.getID(), e.getWhen(), e.getModifiers(), x, y, adjustX, adjustY, e.getClickCount(), e.getButton()));
	}

	@Override
	public void processMouseWheelEvent(MouseWheelEvent e) {
		super.processMouseWheelEvent(e);

		Point2D point = new Point2D.Double(e.getX(), e.getY());
		if (transform != null) {
			try {
				transform.inverseTransform(point, point);
			} catch (NoninvertibleTransformException exc) {
			}
		}

		Point2D adjustedPoint = new Point2D.Double(point.getX(), point.getY());
		adjustPoint(adjustedPoint);

		getRoot().dispatchEvent(new JVGMouseWheelEvent(e, getRoot(), e.getID(), e.getWhen(), e.getModifiers(), point.getX(), point.getY(), adjustedPoint.getX(), adjustedPoint.getY(), e.getClickCount(), e.getButton(), e.getScrollType(), e.getScrollAmount(), e.getWheelRotation()));
	}

	public boolean adjustPoint(Point2D p) {
		if (isGridAlign && increment > 1) {
			double x = p.getX();
			double y = p.getY();

			double dx = x % increment;
			double dy = y % increment;

			double half = increment / 2;
			if (dx <= half) {
				dx = -dx;
			} else {
				dx = increment - dx;
			}

			if (dy <= half) {
				dy = -dy;
			} else {
				dy = increment - dy;
			}

			if (dx == 0 && dy == 0) {
				return false;
			}

			p.setLocation(x + dx, y + dy);
		}
		return true;
	}

	@Override
	public void processKeyEvent(KeyEvent e) {
		JVGComponent focueOwner = getFocusOwner();
		if (focueOwner != null) {
			JVGKeyEvent event = new JVGKeyEvent(e, focueOwner, e.getID(), e.getWhen(), e.getModifiers(), e.getKeyCode(), e.getKeyChar());
			focueOwner.dispatchEvent(event);
			if (event.isConsumed()) {
				return;
			}
		}
		super.processKeyEvent(e);
	}

	@Override
	public void processComponentEvent(ComponentEvent e) {
		if (e.getID() == ComponentEvent.COMPONENT_RESIZED) {
			getRoot().invalidate();
		}

		super.processComponentEvent(e);
	}

	private JVGEditorKit editorKit = new JVGEditorKit();

	public JVGEditorKit getEditorKit() {
		return editorKit;
	}

	private ImageIcon gridImage;

	private Color gridColor = new Color(230, 230, 230);

	private BasicStroke gridStrokeEven = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 3f, new float[] { 3f, 3f }, 0f);

	private BasicStroke gridStrokeOdd = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 3f, new float[] { 1f, 1f }, 0f);

	protected void paintBackground(Graphics2D g) {
		if (isOpaque()) {
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
		}
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2d = getGraphics2D(g);
		if (!isOpaque()) {
			super.paint(g);
		}
		paintBackground(g2d);

		// paint grid on background
		if (isEditable() && !isDrawGridAbove()) {
			paintGrid(g2d);
		}

		paintBeforeTransformed(g2d);

		// paint JVG
		// JVGGraphics jvgGraphics = new JVGGraphics(g2d);
		if (transform != null) {
			g2d.transform(transform);
		}

		paintTransformed(g2d);

		if (transform != null) {
			try {
				g2d.transform(transform.createInverse());
			} catch (NoninvertibleTransformException e) {
				e.printStackTrace();
			}
		}

		// paint grid above all
		if (isEditable() && isDrawGridAbove()) {
			paintGrid(g2d);
		}

		// Draw child components
		paintComponents(g2d);
	}

	public void paintTransformed(Graphics2D g) {
		getRoot().paint(g);
	}

	public void paintBeforeTransformed(Graphics2D g) {
	}

	@Override
	public void print(Graphics g) {
		print(g, true);
	}

	public void print(Graphics g, boolean isTransform) {
		Graphics2D g2d = getGraphics2D(g);

		if (isTransform && transform != null) {
			g2d.transform(transform);
		}

		// paint background
		g.setColor(getBackground());
		g.fillRect(0, 0, getDocumentSize().width, getDocumentSize().height);

		// print JVG
		getRoot().print(g2d);

		// print child components
		printComponents(g2d);
	}

	public static Graphics2D getGraphics2D(Graphics g) {
		if (g instanceof Graphics2D) {
			return (Graphics2D) g;
		} else if (g instanceof ProxyPrintGraphics) {
			return (Graphics2D) (((ProxyPrintGraphics) g).getGraphics());
		} else {
			return null;
		}
	}

	private boolean drawGridAbove = false;

	public boolean isDrawGridAbove() {
		return drawGridAbove;
	}

	public void setDrawGridAbove(boolean drawGridAbove) {
		if (this.drawGridAbove != drawGridAbove) {
			boolean oldValue = this.drawGridAbove;
			this.drawGridAbove = drawGridAbove;
			firePropertyChange("draw-grid-above", oldValue, drawGridAbove);
			repaint();
		}
	}

	private boolean drawGrid = true;

	public boolean isDrawGrid() {
		return drawGrid;
	}

	public void setDrawGrid(boolean drawGrid) {
		if (this.drawGrid != drawGrid) {
			boolean oldValue = this.drawGrid;
			this.drawGrid = drawGrid;
			firePropertyChange("draw-grid", oldValue, drawGrid);
			repaint();
		}
	}

	public void paintGrid(Graphics2D g) {
		if (gridImage != null) {
			if (isDrawGrid()) {
				AffineTransform t = getTransform();
				Dimension documentSize = getDocumentSize();
				Rectangle2D r = t.createTransformedShape(new Rectangle2D.Double(0, 0, documentSize.width, documentSize.height)).getBounds2D();
				Image img = gridImage.getImage();
				Shape oldClip = g.getClip();
				g.clip(r);

				float[] dash = gridStrokeEven.getDashArray();
				int dashlen = 0;
				for (int i = 0; i < dash.length; i++) {
					dashlen += dash[i];
				}
				double size = 2 * getIncrement() * t.getScaleX() * dashlen;

				for (double x = (int) r.getX(); x < r.getX() + r.getWidth(); x += size) {
					for (double y = (int) r.getY(); y < r.getY() + r.getHeight(); y += size) {
						g.drawImage(img, (int) x, (int) y, null);
					}
				}
				g.setClip(oldClip);
			}
		}
	}

	@Override
	public Dimension getPreferredSize() {
		if (!isPreferredSizeSet()) {
			Rectangle2D r = getRoot().getRectangleBounds();
			return new Dimension((int) (r.getX() + r.getWidth()), (int) (r.getY() + r.getHeight()));
		} else {
			return super.getPreferredSize();
		}
	}

	private JVGComponent focusOwner = null;

	public JVGComponent getFocusOwner() {
		return focusOwner;
	}

	private TransferHandler transferHandler = new JVGTransferHandler();

	public void setFocusOwner(JVGComponent focusOwner) {
		if (this.focusOwner != focusOwner) {
			JVGComponent old = this.focusOwner;
			this.focusOwner = focusOwner;

			if (old != null) {
				JVGFocusEvent e = new JVGFocusEvent(old, JVGFocusEvent.FOCUS_LOST);
				old.dispatchEvent(e);
			}

			if (focusOwner != null) {
				JVGFocusEvent e = new JVGFocusEvent(focusOwner, JVGFocusEvent.FOCUS_GAINED);
				focusOwner.dispatchEvent(e);
			}
			repaint();
		}
	}

	private JVGComponent lastEnteredComponent = null;

	public JVGComponent getLastEnteredComponent() {
		return lastEnteredComponent;
	}

	public void setLastEnteredComponent(JVGComponent lastEnteredComponent) {
		this.lastEnteredComponent = lastEnteredComponent;

		// Cursor
		if (lastEnteredComponent != null) {
			Cursor cursor = lastEnteredComponent.getCursor();
			if (cursor != getCursor()) {
				setCursor(cursor);
			}
		}
	}

	protected void clearCursor() {
		setCursor(Cursor.getDefaultCursor());
	}

	private JVGSelectionModel selectionManager = new JVGDefaultSelectionModel();

	public JVGSelectionModel getSelectionManager() {
		return selectionManager;
	}

	@Override
	public void selectionChanged(JVGSelectionEvent event) {
		repaint();
	}

	private boolean autoResize = false;

	public void setAutoResize(boolean autoResize) {
		this.autoResize = autoResize;
	}

	protected final void dispatchEvent(JVGEvent e) {
		if (e.getID() == JVGComponentEvent.COMPONENT_TRANSFORMED) {
			if (autoResize) {
				getRoot().invalidate();
				((JComponent) getParent()).revalidate();
			}
		}
		processJVGEvent(e);
	}

	private List<JVGListener> listeners;

	public void addJVGListener(JVGListener listener) {
		if (listener != null) {
			if (listeners == null) {
				listeners = new ArrayList<JVGListener>();
			}
			listeners.add(listener);
		}
	}

	public void removeJVGListener(JVGListener listener) {
		if (listeners != null && listener != null) {
			listeners.remove(listener);
		}
	}

	public void processJVGEvent(JVGEvent e) {
		if (listeners != null) {
			for (JVGListener listener : listeners) {
				listener.eventOccured(e);
			}
		}

		if (isScriptingEnabled() || e.getSource().isScriptingEnabled()) {
			if (e instanceof JVGKeyEvent) {
				switch (e.getID()) {
					case JVGKeyEvent.KEY_PRESSED:
						getScriptManager().executeScript(Script.KEY_PRESSED, (JVGKeyEvent) e);
						break;

					case JVGKeyEvent.KEY_RELEASED:
						getScriptManager().executeScript(Script.KEY_RELEASED, (JVGKeyEvent) e);
						break;

					case JVGKeyEvent.KEY_TYPED:
						getScriptManager().executeScript(Script.KEY_TYPED, (JVGKeyEvent) e);
						break;
				}
			} else if (e instanceof JVGMouseEvent) {
				switch (e.getID()) {
					case JVGMouseEvent.MOUSE_CLICKED:
						getScriptManager().executeScript(Script.MOUSE_CLICKED, (JVGMouseEvent) e);
						break;

					case JVGMouseEvent.MOUSE_PRESSED:
						getScriptManager().executeScript(Script.MOUSE_PRESSED, (JVGMouseEvent) e);
						break;

					case JVGMouseEvent.MOUSE_RELEASED:
						getScriptManager().executeScript(Script.MOUSE_RELEASED, (JVGMouseEvent) e);
						break;

					case JVGMouseEvent.MOUSE_ENTERED:
						getScriptManager().executeScript(Script.MOUSE_ENTERED, (JVGMouseEvent) e);
						break;

					case JVGMouseEvent.MOUSE_EXITED:
						getScriptManager().executeScript(Script.MOUSE_EXITED, (JVGMouseEvent) e);
						break;

					case JVGMouseEvent.MOUSE_MOVED:
						getScriptManager().executeScript(Script.MOUSE_MOVED, (JVGMouseEvent) e);
						break;

					case JVGMouseEvent.MOUSE_DRAGGED:
						getScriptManager().executeScript(Script.MOUSE_DRAGGED, (JVGMouseEvent) e);
						break;

					case JVGMouseEvent.MOUSE_WHEEL:
						getScriptManager().executeScript(Script.MOUSE_WHEEL, (JVGMouseWheelEvent) e);
						break;
				}
			} else if (e instanceof JVGComponentEvent) {
				switch (e.getID()) {
					case JVGComponentEvent.COMPONENT_SHOWN:
						getScriptManager().executeScript(Script.COMPONENT_SHOWN, (JVGComponentEvent) e);
						break;

					case JVGComponentEvent.COMPONENT_HIDDEN:
						getScriptManager().executeScript(Script.COMPONENT_HIDDEN, (JVGComponentEvent) e);
						break;

					case JVGComponentEvent.COMPONENT_GEOMETRY_CHANGED:
						getScriptManager().executeScript(Script.COMPONENT_GEOMETRY_CHANGED, (JVGComponentEvent) e);
						break;

					case JVGComponentEvent.COMPONENT_TRANSFORMED:
						getScriptManager().executeScript(Script.COMPONENT_TRANSFORMED, (JVGComponentEvent) e);
						break;

					case JVGContainerEvent.COMPONENT_ADDED:
						getScriptManager().executeScript(Script.COMPONENT_ADDED, (JVGContainerEvent) e);
						break;

					case JVGContainerEvent.COMPONENT_REMOVED:
						getScriptManager().executeScript(Script.COMPONENT_REMOVED, (JVGContainerEvent) e);
						break;

					case JVGContainerEvent.COMPONENT_ORDER_CHANGED:
						getScriptManager().executeScript(Script.COMPONENT_ORDER_CHANGED, (JVGContainerEvent) e);
						break;
				}
			} else if (e instanceof JVGPeerEvent) {
				switch (e.getID()) {
					case JVGPeerEvent.CONNECTED_TO_PEER:
						getScriptManager().executeScript(Script.COMPONENT_CONNECTED_TO_PEER, (JVGPeerEvent) e);
						break;

					case JVGPeerEvent.DISCONNECTED_FROM_PEER:
						getScriptManager().executeScript(Script.COMPONENT_DISCONNECTED_FROM_PEER, (JVGPeerEvent) e);
						break;
				}
			} else if (e instanceof JVGPropertyChangeEvent) {
				getScriptManager().executeScript(Script.PROPERTY_CHANGED, (JVGPropertyChangeEvent) e);
			}
		}
	}

	// script support
	private JVGScriptSupport scriptSupport = null;

	public JVGScriptSupport getScriptManager() {
		if (scriptSupport == null) {
			scriptSupport = new JVGScriptSupport(this);
		}
		return scriptSupport;
	}

	private boolean scriptingEnabled = false;

	public boolean isScriptingEnabled() {
		return scriptingEnabled;
	}

	public void setScriptingEnabled(boolean scriptingEnabled) {
		if (this.scriptingEnabled != scriptingEnabled) {
			boolean oldValue = this.scriptingEnabled;
			this.scriptingEnabled = scriptingEnabled;
			firePropertyChange("scriptingEnabled", oldValue, scriptingEnabled);
		}
	}

	@Override
	public String getToolTipText(MouseEvent e) {
		if (lastEnteredComponent != null) {
			return lastEnteredComponent.getToolTipText(e.getX(), e.getY());
		} else {
			return super.getToolTipText(e);
		}
	}

	@Override
	public Point getToolTipLocation(MouseEvent e) {
		if (lastEnteredComponent != null) {
			return lastEnteredComponent.getToolTipLocation(e.getX(), e.getY());
		} else {
			return super.getToolTipLocation(e);
		}
	}

	private static final Object FOCUSED_PANE = new Object();

	public static final JVGPane getFocusedPane() {
		return (JVGPane) AppContext.getAppContext().get(FOCUSED_PANE);
	}

	public boolean selectAll() {
		return selectAll(false);
	}

	public boolean selectAll(boolean deep) {
		if (selectionManager != null) {
			if (deep) {
				return selectRecursively(getRoot());
			} else {
				int childs_count = getRoot().childrenCount;
				JVGComponent[] childs = getRoot().children;
				boolean selectionChanges = false;
				for (int i = 0; i < childs_count; i++) {
					selectionChanges |= !childs[i].isSelected();
					childs[i].setSelected(true, false);
				}
				return selectionChanges;
			}
		} else {
			return false;
		}
	}

	private boolean selectRecursively(JVGContainer p) {
		int childs_count = p.childrenCount;
		JVGComponent[] childs = p.children;
		boolean selectionChanges = false;
		for (int i = 0; i < childs_count; i++) {
			selectionChanges |= !childs[i].isSelected();

			childs[i].setSelected(true, false);
			if (childs[i] instanceof JVGContainer) {
				selectRecursively((JVGContainer) childs[i]);
			}
		}
		return selectionChanges;
	}

	public void unselectAll() {
		if (selectionManager != null) {
			selectionManager.clearSelection();
		}
	}

	// drag
	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
	}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {
	}

	@Override
	public void dragExit(DropTargetEvent dte) {
	}

	@Override
	public void drop(DropTargetDropEvent dtde) {
		try {
			if (transform != null) {
				try {
					transform.inverseTransform(dtde.getLocation(), dtde.getLocation());
				} catch (NoninvertibleTransformException exc) {
				}
			}

			dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
			Transferable transferable = dtde.getTransferable();

			if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
				if (files != null) {
					InsertEditorAction.insert(this, files, dtde.getLocation().x, dtde.getLocation().y);
					requestFocus();
					return;
				}
			}

			if (transferable.isDataFlavorSupported(JVGTransferable.JVGFLAVOR)) {
				JVGCopyContext ctx = (JVGCopyContext) transferable.getTransferData(JVGTransferable.JVGFLAVOR);
				if (ctx != null) {
					InsertEditorAction.insert(this, ctx, dtde.getLocation().x, dtde.getLocation().y);
					requestFocus();
					return;
				}
			}

			if (drop != null) {
				drop.drop(dtde);
			}
		} catch (UnsupportedFlavorException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
	}

	@Override
	public void dragEnter(DragSourceDragEvent dsde) {
		System.out.println("2 dragEnter(DragSourceDragEvent dsde)");
	}

	@Override
	public void dragOver(DragSourceDragEvent dsde) {
		System.out.println("2 dragOver(DragSourceDragEvent dsde)");
	}

	@Override
	public void dropActionChanged(DragSourceDragEvent dsde) {
		System.out.println("2 dropActionChanged(DragSourceDragEvent dsde)");
	}

	@Override
	public void dragExit(DragSourceEvent dse) {
		System.out.println("2 dragExit(DragSourceDragEvent dsde)");
	}

	@Override
	public void dragDropEnd(DragSourceDropEvent dsde) {
		System.out.println("2 dragDropEnd(DragSourceDragEvent dsde)");
	}

	// ============================================================================
	// === Undo / Redo
	// ============================================================================
	protected EventListenerList undoableListenerList = new EventListenerList();

	protected UndoableEditListener lastUndoableListener;

	private CompoundUndoRedo collectedEdit;

	public void startCollectUndo(String name) {
		if (collectedEdit != null) {
			stopCollectUndo();
		}
		collectedEdit = new CompoundUndoRedo(name, this);
	}

	public void stopCollectUndo() {
		if (collectedEdit != null) {
			if (!collectedEdit.isEmpty()) {
				UndoableEditEvent e = new UndoableEditEvent(this, collectedEdit);
				collectedEdit = null;
				fireUndoableEditUpdate(e);
			} else {
				collectedEdit = null;
			}
		}
	}

	public void fireUndoableEditUpdate(UndoableEditEvent e) {
		if (collectedEdit != null) {
			collectedEdit.add(e.getEdit());
			return;
		}

		Object[] listeners = undoableListenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == UndoableEditListener.class) {
				((UndoableEditListener) listeners[i + 1]).undoableEditHappened(e);
			}
		}

		if (lastUndoableListener != null) {
			lastUndoableListener.undoableEditHappened(e);
		}
	}

	public void addUndoableEditListener(UndoableEditListener listener) {
		undoableListenerList.add(UndoableEditListener.class, listener);
	}

	public void setLastUndoableEditListener(UndoableEditListener lastUndoableListener) {
		this.lastUndoableListener = lastUndoableListener;
	}

	public void removeUndoableEditListener(UndoableEditListener listener) {
		undoableListenerList.remove(UndoableEditListener.class, listener);
	}

	public UndoableEditListener[] getUndoableEditListeners() {
		return undoableListenerList.getListeners(UndoableEditListener.class);
	}

	private UndoManager undoManager = new UndoManager();

	public UndoManager getUndoManager() {
		return undoManager;
	}

	private JVGUndoableListener undoableListener = new JVGUndoableListener();

	public JVGUndoableListener getUndoableListener() {
		return undoableListener;
	}

	class JVGUndoableListener implements UndoableEditListener {
		@Override
		public void undoableEditHappened(UndoableEditEvent e) {
			undoManager.addEdit(e.getEdit());
			undoAction.updateUndoState();
			redoAction.updateRedoState();
		}
	}

	private UndoAction undoAction = new UndoAction();

	public UndoAction getUndoAction() {
		return undoAction;
	}

	public class UndoAction extends AbstractAction {
		public UndoAction() {
			super("Undo");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				undoManager.undo();
			} catch (CannotUndoException Exc) {
			}
			updateUndoState();
			redoAction.updateRedoState();
		}

		protected void updateUndoState() {
			if (undoManager.canUndo()) {
				putValue(Action.NAME, undoManager.getUndoPresentationName());
			} else {
				putValue(Action.NAME, "Undo");
			}
		}
	}

	private RedoAction redoAction = new RedoAction();

	public RedoAction getRedoAction() {
		return redoAction;
	}

	public class RedoAction extends AbstractAction {
		public RedoAction() {
			super("Redo");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				undoManager.redo();
			} catch (CannotRedoException Exc) {
			}
			updateRedoState();
			undoAction.updateUndoState();
		}

		protected void updateRedoState() {
			if (undoManager.canRedo()) {
				putValue(Action.NAME, undoManager.getRedoPresentationName());
			} else {
				putValue(Action.NAME, "Redo");
			}
		}
	}

	private boolean isEditable = false;

	public boolean isEditable() {
		return isEditable;
	}

	public void setEditable(boolean isEditable) {
		if (this.isEditable != isEditable) {
			boolean oldValue = this.isEditable;
			this.isEditable = isEditable;
			firePropertyChange("editable", oldValue, isEditable);
		}
	}

	private boolean isGridAlign = false;

	public boolean isGridAlign() {
		return isGridAlign;
	}

	public void setGridAlign(boolean isGridAlign) {
		if (this.isGridAlign != isGridAlign) {
			boolean oldValue = this.isGridAlign;
			this.isGridAlign = isGridAlign;
			firePropertyChange("grid-align", oldValue, isGridAlign);
		}
	}

	public double getScaleX() {
		return transform != null ? transform.getScaleX() : 1;
	}

	public double getScaleY() {
		return transform != null ? transform.getScaleY() : 1;
	}

	private double increment = 1;

	public double getIncrement() {
		return increment;
	}

	public void setIncrement(double increment) {
		if (increment < 1) {
			increment = 1;
		} else if (increment > 100) {
			increment = 100;
		}

		if (this.increment != increment) {
			double oldValue = this.increment;
			this.increment = increment;
			updateGridImage();
			firePropertyChange("increment", oldValue, increment);
		}
	}

	private void updateGridImage() {
		if (increment >= 5) {
			double size = 2 * getIncrement() * getZoom();
			gridImage = new ImageIcon(JVGUtil.getGridImage(gridColor, gridStrokeEven, gridStrokeOdd, (int) size));
		} else {
			gridImage = null;
		}
	}

	private AffineTransform transform;

	public void setTransform(AffineTransform transform) {
		this.transform = transform;
		repaint();
	}

	public AffineTransform getTransform() {
		return transform;
	}

	public void cut() {
		getEditorKit();
		invokeAction(JVGEditorKit.CUT_ACTION, JVGEditorKit.getAction(JVGEditorKit.CUT_ACTION));
	}

	public void copy() {
		getEditorKit();
		invokeAction(JVGEditorKit.COPY_ACTION, JVGEditorKit.getAction(JVGEditorKit.COPY_ACTION));
	}

	public void paste() {
		getEditorKit();
		invokeAction(JVGEditorKit.PASTE_ACTION, JVGEditorKit.getAction(JVGEditorKit.PASTE_ACTION));
	}

	private void invokeAction(String name, Action action) {
		action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, (String) action.getValue(Action.NAME), EventQueue.getMostRecentEventTime(), getCurrentEventModifiers()));
	}

	private int getCurrentEventModifiers() {
		int modifiers = 0;
		AWTEvent currentEvent = EventQueue.getCurrentEvent();
		if (currentEvent instanceof InputEvent) {
			modifiers = ((InputEvent) currentEvent).getModifiers();
		} else if (currentEvent instanceof ActionEvent) {
			modifiers = ((ActionEvent) currentEvent).getModifiers();
		}
		return modifiers;
	}

	private static Object SandboxClipboardKey = new Object();

	public Clipboard getClipboard() {
		if (SwingUtilities2.canAccessSystemClipboard()) {
			return Toolkit.getDefaultToolkit().getSystemClipboard();
		}

		Clipboard clipboard = (Clipboard) sun.awt.AppContext.getAppContext().get(SandboxClipboardKey);
		if (clipboard == null) {
			clipboard = new Clipboard("Sandboxed Component Clipboard");
			sun.awt.AppContext.getAppContext().put(SandboxClipboardKey, clipboard);
		}
		return clipboard;
	}

	public JVGComponent getComponent(Long id) {
		JVGRoot root = getRoot();
		return root.getComponent(id);
	}

	public long[] getAllComponents() {
		JVGRoot root = getRoot();
		return root.getAllComponents();
	}

	public boolean isConnectionsEnabled() {
		return false;
	}

	public JVGPaneInternalFrame getWindow() {
		Container w = this;
		while (w != null && !(w instanceof JVGPaneInternalFrame)) {
			w = w.getParent();
		}
		return w instanceof JVGPaneInternalFrame ? (JVGPaneInternalFrame) w : null;
	}

	// =======================================================
	// === Macros
	// =======================================================
	private JVGMacros macros;

	public void appendMacrosCode(JVGMacrosCode code) {
		if (macros != null) {
			macros.appendCode(code);
		}
	}

	public void removeMacrosCode(JVGMacrosCode code) {
		if (macros != null) {
			macros.removeCode(code);
		}
	}

	public void startMacros() {
		macros = new JVGMacros();
	}

	public void pauseMacros() {
		if (macros != null) {
			macros.setActive(false);
		}
	}

	public void resumeMacros() {
		if (macros != null) {
			macros.setActive(true);
		}
	}

	public void stopMacros() {
		macros = null;
	}

	public boolean isMacrosActive() {
		return macros != null && macros.isActive();
	}

	public JVGMacrosCode getMacros() {
		if (macros != null) {
			return macros.getCode();
		} else {
			return null;
		}
	}

	private ImageCapabilities capabilities = new ImageCapabilities(true);

	private Map<Integer, VolatileImage> drawBuffers = new HashMap<Integer, VolatileImage>();

	// not thread safe
	protected VolatileImage createDrawBuffer(int level) throws Exception {
		VolatileImage img = drawBuffers.get(level);
		try {
			if (img != null) {
				img.flush();
			}
			img = createVolatileImage(getWidth(), getHeight(), capabilities);
			if (img != null) {
				drawBuffers.put(level, img);
			}
		} catch (Exception exc) {
		}
		return img;
	}

	public static void main(String[] args) {
		JVGPane p = null;
		try {
			p = new JVGPane();
			p.setEditable(false);
			JVGParser parser = new JVGParser(p.getEditorKit().getFactory());
			JVGRoot root = parser.parse(new File("C:/Documents and Settings/john/Мои документы/test2.xml"));
			p.setRoot(root);

			JFrame f = new JFrame();
			f.setContentPane(new JScrollPane(p));
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			f.setBounds(200, 200, 600, 600);
			f.setVisible(true);
		} catch (JVGParseException exc) {
			exc.printStackTrace();
		}
	}

	public JVGPaneDrop getDrop() {
		return drop;
	}

	public void setDrop(JVGPaneDrop drop) {
		this.drop = drop;
	}
}
