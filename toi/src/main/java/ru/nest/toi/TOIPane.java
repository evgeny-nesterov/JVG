package ru.nest.toi;

import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

import ru.nest.imagechooser.ImageChooser;
import ru.nest.toi.factory.TOIDefaultFactory;
import ru.nest.toi.objects.TOIArrowPathElement;
import ru.nest.toi.objects.TOIMultiArrowPath;
import ru.nest.toi.objects.TOIPath;

public class TOIPane extends JPanel {
	private static final long serialVersionUID = 8486853362488889589L;

	public static Stroke selectedStroke1 = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);

	public static Stroke selectedStroke2 = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[] { 3f, 3f }, 0f);

	private List<TOIObject> objects = new ArrayList<>();

	private boolean editable = false;

	private TOIObject focusedObject;

	private Set<TOIObject> selectedObjects = new HashSet<>();

	private TOIController controller;

	private TOIColor colorRenderer = new TOIDefaultColor();

	private AffineTransform transform = new AffineTransform();

	private boolean isGridAlign = false;

	private double increment = 10;

	private ControlFilter controlFilter;

	private TOIFactory factory;

	private double minZoom = 1;

	private double maxZoom = 8;

	private boolean selectEnable = true;

	public TOIPane() {
		setOpaque(false);
		setBackground(Color.black);
		setRequestFocusEnabled(true);
		setFocusable(true);
		setFocusCycleRoot(true);
		//		setFocusTraversalKeysEnabled(false);

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				requestFocus();
			}
		});
		enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
	}

	public void selectObjectsInRect(Rectangle2D rect) {
		clearSelection();
		synchronized (objects) {
			for (TOIObject o : objects) {
				if (o.getBounds() != null) {
					Rectangle2D b = o.getBounds().getBounds2D();
					if (rect.contains(b)) {
						select(o, true);
					}
				}
			}
		}
	}

	public List<TOIObject> getObjects() {
		synchronized (objects) {
			return new ArrayList<>(objects);
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

	@Override
	public void processMouseWheelEvent(MouseWheelEvent e) {
		super.processMouseWheelEvent(e);
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
		adjustPoint(adjustedPoint);

		processMouseEvent(e, point.getX(), point.getY(), adjustedPoint.getX(), adjustedPoint.getY());
	}

	public void processMouseEvent(MouseEvent e, double x, double y, double adjustX, double adjustY) {
		if (controller != null) {
			controller.processMouseEvent(e, x, y, adjustX, adjustY);
		}
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
		if (controller != null) {
			controller.processKeyEvent(e);
		}

		if (!e.isConsumed()) {
			super.processKeyEvent(e);
		}

		if (!e.isConsumed()) {
			if (e.getKeyCode() == KeyEvent.VK_PLUS) {
				setZoom(getZoom() + 1);
			} else if (e.getKeyCode() == KeyEvent.VK_MINUS) {
				setZoom(getZoom() - 1);
			}
		}
	}

	public TOIObject getFocusedObject() {
		return focusedObject;
	}

	public void setFocusedObject(TOIObject focusedObject) {
		if (selectEnable && this.focusedObject != focusedObject) {
			if (this.focusedObject != null) {
				this.focusedObject.setFocused(false);
			}
			this.focusedObject = focusedObject;
			if (focusedObject != null) {
				focusedObject.setFocused(true);
			}
			focusChanged(focusedObject);
			repaint();
		}
	}

	protected void focusChanged(TOIObject focusedObject) {
	}

	public TOIObject getObject(double x, double y) {
		synchronized (objects) {
			for (int i = objects.size() - 1; i >= 0; i--) {
				TOIObject o = objects.get(i);
				if (o.contains(x, y)) {
					return o;
				}
			}
		}
		return null;
	}

	public TOIObjectControl getObjectControl(double x, double y) {
		return getObjectControl(x, y, controlFilter);
	}

	public interface ControlFilter {
		boolean pass(TOIObjectControl c);
	}

	public TOIObjectControl getObjectControl(double x, double y, ControlFilter filter) {
		if (!isEditable()) {
			return null;
		}

		synchronized (objects) {
			for (int i = objects.size() - 1; i >= 0; i--) {
				TOIObject o = objects.get(i);
				TOIObjectControl c = o.getControlAt(this, x, y);
				if (c != null && (filter == null || filter.pass(c))) {
					return c;
				}
			}
		}
		return null;
	}

	public static class PathCoord {
		private TOIPath path;

		private int coordIndex;

		public TOIPath getPath() {
			return path;
		}

		public void setPath(TOIPath path) {
			this.path = path;
		}

		public int getCoordIndex() {
			return coordIndex;
		}

		public void setCoordIndex(int coordIndex) {
			this.coordIndex = coordIndex;
		}
	}

	public PathCoord getPathCoord(double x, double y) {
		synchronized (objects) {
			for (TOIObject o : objects) {
				if (o instanceof TOIPath) {
					TOIPath path = (TOIPath) o;
					int index = path.getCoordIndex(x, y);
					if (index != -1) {
						PathCoord p = new PathCoord();
						p.setPath(path);
						p.setCoordIndex(index);
						return p;
					}
				}
			}
		}
		return null;
	}

	protected void drawBackground(Graphics2D g2d) {
		g2d.setColor(getBackground());
		g2d.fillRect(0, 0, getWidth(), getHeight());
	}

	private Stroke focusStroke = new BasicStroke(3f);

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		drawBackground(g2d);

		Graphics2D g2dt;
		if (transform != null) {
			g2dt = (Graphics2D) g.create();
			g2dt.transform(transform);
		} else {
			g2dt = g2d;
		}

		for (int layer = TOIPaintContext.MIN_LAYER; layer <= TOIPaintContext.MAX_LAYER; layer++) {
			TOIPaintContext ctx = new TOIPaintContext();
			ctx.setColorRenderer(getColorRenderer());
			ctx.setController(getController());
			ctx.setTransform(getTransform());
			ctx.setControlFilter(getControlFilter());
			ctx.setPane(this);
			ctx.setLayer(layer);

			// bottom layer
			if (layer == TOIPaintContext.BOTTOM_LAYER) {
				if (controller != null) {
					controller.paint(g2d, g2dt, null, TOIController.PAINT_BOTTOM);
				}

				if (selectedObjects.size() > 1) {
					Rectangle2D selectionBounds = getSelectionBounds();
					if (selectionBounds != null) {
						Shape transformedSelectionBounds = ctx.getTransform().createTransformedShape(selectionBounds);
						Stroke oldStroke = g2d.getStroke();
						g2d.setStroke(selectedStroke1);
						g2d.setColor(new Color(220, 220, 220));
						g2d.draw(transformedSelectionBounds);
						g2d.setStroke(selectedStroke2);
						g2d.setColor(Color.black);
						g2d.draw(transformedSelectionBounds);
						g2d.setStroke(oldStroke);
					}
				}
			}

			// all layers
			List<TOIObject> objects = getObjects();
			for (TOIObject o : objects) {
				if (o.isFocused() && selectedObjects.size() > 1) {
					Stroke oldStroke = g2d.getStroke();
					g2d.setColor(new Color(50, 200, 50, 240));
					g2d.setStroke(focusStroke);
					try {
						g2d.draw(transform.createTransformedShape(o.getBounds()));
					} catch (Exception exc) {
					}
					g2d.setStroke(oldStroke);
				}

				if (layer == TOIPaintContext.MAIN_LAYER) {
					if (ctx.getController() != null) {
						ctx.getController().paint(g2d, g2dt, o, TOIController.PAINT_UNDER_OBJECT);
					}
				}

				if (ctx.getLayer() == TOIPaintContext.MAIN_LAYER) {
					o.paint(g2d, g2dt, ctx);
				}

				if (layer == TOIPaintContext.MAIN_LAYER) {
					if (ctx.getController() != null) {
						ctx.getController().paint(g2d, g2dt, o, TOIController.PAINT_OVER_OBJECT);
					}
				}

				if (layer == TOIPaintContext.MAIN_LAYER) {
					if (o.isSelected()) {
						if (ctx.getController() != null) {
							ctx.getController().paint(g2d, g2dt, o, TOIController.PAINT_UNDER_SELECTION);
						}

						o.paintSelection(g2d, g2dt, ctx);

						if (ctx.getController() != null) {
							ctx.getController().paint(g2d, g2dt, o, TOIController.PAINT_OVER_SELECTION);
						}
					}
				}
				o.paintControls(g2d, g2dt, ctx);
			}

			// top layer
			if (layer == TOIPaintContext.TOP_LAYER) {
				if (controller != null) {
					controller.paint(g2d, g2dt, null, TOIController.PAINT_TOP);
				}
			}
		}
	}

	public Rectangle2D getSelectionBounds() {
		if (selectedObjects.size() > 0) {
			Rectangle2D b = null;
			for (TOIObject o : selectedObjects) {
				Rectangle2D r = o.getBounds().getBounds2D();
				if (b != null) {
					b.add(r);
				} else {
					b = new Rectangle2D.Double(r.getX(), r.getY(), r.getWidth(), r.getHeight());
				}
			}
			return b;
		}
		return null;
	}

	public void rotateSelection(double angle) {
		Rectangle2D r = getSelectionBounds();
		if (r != null) {
			AffineTransform t = AffineTransform.getRotateInstance(angle, r.getCenterX(), r.getCenterY());
			transformSelection(t);
		}
	}

	public void scaleSelection(double scalex, double scaley) {
		Rectangle2D r = getSelectionBounds();
		if (r != null) {
			AffineTransform t = AffineTransform.getTranslateInstance(r.getCenterX(), r.getCenterY());
			t.scale(scalex, scaley);
			t.translate(-r.getCenterX(), -r.getCenterY());
			transformSelection(t);
		}
	}

	public void transformSelection(AffineTransform transform) {
		for (TOIObject o : selectedObjects) {
			o.transform(transform);
		}
		repaint();
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public List<TOIObject> getSelectedObjects() {
		return new ArrayList<>(selectedObjects);
	}

	public int getSelectionSize() {
		return selectedObjects.size();
	}

	public void clearSelection() {
		for (TOIObject o : selectedObjects) {
			o.setSelected(false);
		}
		selectedObjects.clear();
		selectionChanged();
		repaint();
	}

	public void selectAll() {
		synchronized (objects) {
			for (TOIObject o : objects) {
				o.setSelected(true);
			}
		}
		selectedObjects.addAll(objects);
		selectionChanged();
		repaint();
	}

	public void select(TOIObject o, boolean selected) {
		if (selectEnable && o.isSelected() != selected) {
			o.setSelected(selected);
			if (selected) {
				selectedObjects.add(o);
			} else {
				selectedObjects.remove(o);
			}
			selectionChanged();
			repaint();
		}
	}

	protected void selectionChanged() {
	}

	public void addObject(TOIObject o) {
		insertObject(o, -1);
	}

	public void insertObject(TOIObject o, int index) {
		if (o != null) {
			synchronized (this.objects) {
				if (index == -1) {
					objects.add(o);
				} else {
					objects.add(index, o);
				}
			}
			repaint();
		}
	}

	public void setObjects(List<TOIObject> objects) {
		if (objects != null) {
			synchronized (this.objects) {
				this.objects.clear();
				this.objects.addAll(objects);
			}
			repaint();
		}
	}

	public void removeObject(TOIObject o) {
		if (o == focusedObject) {
			setFocusedObject(null);
		}
		select(o, false);
		synchronized (objects) {
			objects.remove(o);
		}
		repaint();
	}

	public void removeObjects(List<TOIObject> objects) {
		if (objects.contains(focusedObject)) {
			setFocusedObject(null);
		}
		for (TOIObject o : objects) {
			select(o, false);
		}
		synchronized (this.objects) {
			this.objects.removeAll(objects);
		}
		repaint();
	}

	public void removeAllObjects() {
		setFocusedObject(null);
		clearSelection();
		synchronized (objects) {
			objects.clear();
		}
		repaint();
	}

	public TOIController getController() {
		return controller;
	}

	public void setController(TOIController controller) {
		if (this.controller != controller) {
			if (this.controller != null) {
				this.controller.uninstall();
				setCursor(Cursor.getDefaultCursor());
			}
			this.controller = controller;
			if (controller != null) {
				controller.install(this);
			} else {
				clearSelection();
				setFocusedObject(null);
			}
			repaint();
		}
	}

	public void toFront(TOIObject o) {
		if (o != null) {
			synchronized (objects) {
				int index = objects.indexOf(o);
				if (index != -1 && index < objects.size() - 1) {
					objects.add(o);
					repaint();
				}
			}
		}
	}

	public void toBack(TOIObject o) {
		if (o != null) {
			synchronized (objects) {
				int index = objects.indexOf(o);
				if (index != -1 && index > 0) {
					objects.remove(o);
					objects.add(0, o);
					repaint();
				}
			}
		}
	}

	public void toUp(TOIObject o) {
		if (o != null) {
			synchronized (objects) {
				int index = objects.indexOf(o);
				if (index != -1 && index < objects.size() - 1) {
					objects.remove(index);
					objects.add(index + 1, o);
					repaint();
				}
			}
		}
	}

	public void toDown(TOIObject o) {
		if (o != null) {
			synchronized (objects) {
				int index = objects.indexOf(o);
				if (index != -1 && index > 0) {
					objects.remove(index);
					objects.add(index - 1, o);
					repaint();
				}
			}
		}
	}

	public PathElementPoint findClosestElementPoint(double x, double y, double threshold) {
		double minDistanceSq = Double.MAX_VALUE;
		PathElementPoint closestPoint = null;
		for (TOIObject o : getObjects()) {
			if (o instanceof TOIMultiArrowPath) {
				TOIMultiArrowPath path = (TOIMultiArrowPath) o;
				PathElementPoint point = path.getClosestPoint(x, y);
				if (point != null) {
					if (closestPoint == null) {
						closestPoint = point;
						double dx = x - point.point.getX();
						double dy = y - point.point.getY();
						minDistanceSq = dx * dx + dy * dy;
					} else {
						double dx = x - point.point.getX();
						double dy = y - point.point.getY();
						double distSq = dx * dx + dy * dy;
						if (distSq < minDistanceSq) {
							minDistanceSq = distSq;
							closestPoint = point;
						}
					}
				}
			}
		}

		if (minDistanceSq < threshold * threshold) {
			return closestPoint;
		} else {
			return null;
		}
	}

	public TOIArrowPathElement getPathElementEndingAt(double x, double y) {
		synchronized (objects) {
			for (TOIObject o : objects) {
				if (o instanceof TOIMultiArrowPath) {
					TOIMultiArrowPath path = (TOIMultiArrowPath) o;
					TOIArrowPathElement e = path.getPathElementEndingAt(x, y);
					if (e != null) {
						return e;
					}
				}
			}
		}
		return null;
	}

	public TOIColor getColorRenderer() {
		return colorRenderer;
	}

	public void setColorRenderer(TOIColor colorRenderer) {
		this.colorRenderer = colorRenderer;
	}

	public File chooseImage() {
		ImageChooser chooser = new ImageChooser();
		return (File) ImageChooser.choose(this, JFileChooser.OPEN_DIALOG, chooser);
	}

	public String export() throws Exception {
		StringWriter w = new StringWriter();
		new TOIBuilder(getFactory()).export(w, getObjects());
		return w.toString();
	}

	public void export(Writer w) throws Exception {
		new TOIBuilder(getFactory()).export(w, getObjects());
	}

	public void load(String doc) throws Exception {
		List<TOIObject> objects = new TOIBuilder(getFactory()).load(new StringReader(doc));
		setObjects(objects);
	}

	public void append(String doc) throws Exception {
		List<TOIObject> objects = new TOIBuilder(getFactory()).load(new StringReader(doc));
		for (TOIObject o : objects) {
			addObject(o);
		}
	}

	public void load(Reader r) throws Exception {
		List<TOIObject> objects = new TOIBuilder(getFactory()).load(r);
		setObjects(objects);
	}

	private Dimension defaultDocumentSize = new Dimension(400, 300);

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

	public void setDocumentSize(int w, int h) {
		setDocumentSize(new Dimension(w, h));
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
		if (zoom < getMinZoom()) {
			zoom = getMinZoom();
		} else if (zoom > getMaxZoom()) {
			zoom = getMaxZoom();
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

	public Point2D getDocumentLocation() {
		Insets i = getDocumentInsets();
		return new Point2D.Double(i.left, i.top);
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
	}

	public void setTransform(AffineTransform transform) {
		this.transform = transform;
		repaint();
	}

	public AffineTransform getTransform() {
		return transform;
	}

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
			firePropertyChange("increment", oldValue, increment);
		}
	}

	public static void main(String[] args) {
		TOIPane pane = new TOIPane();
		try {
			pane.load(new FileReader("C:/Users/john/Documents/toi3.xml"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		JFrame f = new JFrame();
		f.setContentPane(pane);
		f.setBounds(300, 200, 600, 400);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}

	public ControlFilter getControlFilter() {
		return controlFilter;
	}

	public void setControlFilter(ControlFilter controlFilter) {
		this.controlFilter = controlFilter;
	}

	public TOIFactory getFactory() {
		if (factory == null) {
			factory = new TOIDefaultFactory();
		}
		return factory;
	}

	public void setFactory(TOIFactory factory) {
		this.factory = factory;
	}

	public double getMinZoom() {
		return minZoom;
	}

	public void setMinZoom(double minZoom) {
		this.minZoom = minZoom;
	}

	public double getMaxZoom() {
		return maxZoom;
	}

	public void setMaxZoom(double maxZoom) {
		this.maxZoom = maxZoom;
	}

	public boolean isSelectEnable() {
		return selectEnable;
	}

	public void setSelectEnable(boolean selectEnable) {
		this.selectEnable = selectEnable;
	}
}
