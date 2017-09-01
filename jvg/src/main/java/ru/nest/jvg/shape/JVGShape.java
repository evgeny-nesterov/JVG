package ru.nest.jvg.shape;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AlfaPaint;

import ru.nest.jvg.Filter;
import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGContainer;
import ru.nest.jvg.JVGUtil;
import ru.nest.jvg.event.JVGComponentEvent;
import ru.nest.jvg.event.JVGPropertyChangeEvent;
import ru.nest.jvg.geom.CoordinablePath;
import ru.nest.jvg.geom.MutableGeneralPath;
import ru.nest.jvg.geom.Pathable;
import ru.nest.jvg.shape.paint.FillPainter;
import ru.nest.jvg.shape.paint.Painter;
import ru.nest.jvg.shape.paint.PainterFilter;

/**
 * Transparency is supported in Color paint.
 */
public abstract class JVGShape extends JVGContainer {
	public final static String PROPERTY_ORIGINAL_BOUNDS = "original-bounds";

	public final static String PROPERTY_ANTIALIAS = "antialias";

	public final static String PROPERTY_ISFILL = "isfill";

	public final static String PROPERTY_LOCK_STATE = "lock-state";

	public final static String PROPERTY_ALFA = "alfa";

	public final static String PROPERTY_COMPOSE_PAINT = "compose-paint";

	public final static String PROPERTY_PAINTERS = "painters";

	public final static int LOCK_NONE = 0;

	public final static int LOCK_TRANSFORM = 1;

	public final static int LOCK_SHAPE = 2;

	protected static Robot robot;
	static {
		try {
			robot = new Robot();
		} catch (AWTException exc) {
			exc.printStackTrace();
		}
	}

	// has to be set to null after paintShape
	private PainterFilter painterFilter = null;

	protected static StringBuffer shapeId = new StringBuffer("shape");

	protected MutableGeneralPath transformedShape = null;

	protected boolean clipComputed = false;

	protected Shape clipShape = null;

	private boolean isClip = false;

	public JVGShape() {
		setName("Shape");
	}

	public Shape getShape() {
		return getShape(shapeId);
	}

	public Shape getShape(Object shapeId) {
		return (Shape) getClientProperty(shapeId);
	}

	public MutableGeneralPath getTransformedShape() {
		if (transformedShape == null) {
			validate();
		}
		return transformedShape;
	}

	public MutableGeneralPath computeTransformedShape() {
		Shape shape = getShape();
		if (shape != null) {
			MutableGeneralPath transformedShape = new MutableGeneralPath();
			transformedShape.append(shape, false);
			transformedShape.transform(getTransform());
			return transformedShape;
		} else {
			return null;
		}
	}

	public void setShape(Shape shape) {
		setShape(shape, true);
	}

	public void setShape(Shape shape, boolean coordinable) {
		setShape(shape, coordinable, shapeId);
	}

	public void setShape(Shape shape, boolean coordinable, Object... shapeIds) {
		if (isLockShape()) {
			return;
		}

		if (shape == null) {
			throw new RuntimeException("Null shape was passed");
		}

		Pathable<?> path;
		if (coordinable) {
			if (shape instanceof CoordinablePath) {
				path = (CoordinablePath) shape;
			} else {
				path = new CoordinablePath(shape);
			}
		} else {
			if (shape instanceof MutableGeneralPath) {
				path = (MutableGeneralPath) shape;
			} else {
				path = new MutableGeneralPath(shape);
			}
		}

		for (Object shapeId : shapeIds) {
			setClientProperty(shapeId, path);
		}
		invalidate();

		JVGComponentEvent event = new JVGComponentEvent(this, JVGComponentEvent.COMPONENT_GEOMETRY_CHANGED);
		dispatchEvent(event);
	}

	protected Shape initialBounds = null;

	public final Shape getInitialBounds() {
		if (initialBounds == null) {
			validate();
		}
		return initialBounds;
	}

	protected Shape computeInitialBounds() {
		Shape shape = getShape();
		if (shape != null) {
			return shape.getBounds2D();
		} else {
			return null;
		}
	}

	protected boolean isOriginalBounds = false;

	public void setOriginalBounds(boolean isOriginalBounds) {
		boolean oldValue = this.isOriginalBounds;
		this.isOriginalBounds = isOriginalBounds;
		dispatchEvent(new JVGPropertyChangeEvent(this, PROPERTY_ORIGINAL_BOUNDS, oldValue, isOriginalBounds));
	}

	public boolean isOriginalBounds() {
		return isOriginalBounds;
	}

	protected Shape originalBounds = null;

	public final Shape getOriginalBounds() {
		if (originalBounds == null) {
			validate();
		}
		return originalBounds;
	}

	protected Shape computeOriginalBounds() {
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
				return path;
			}
		}
		return null;
	}

	@Override
	protected Shape computeBounds() {
		if (isOriginalBounds) {
			if (originalBounds == null) {
				originalBounds = computeOriginalBounds();
			}
			return originalBounds;
		} else {
			if (transformedShape == null) {
				transformedShape = computeTransformedShape();
			}

			if (transformedShape != null) {
				return transformedShape.getBounds2D();
			} else {
				return null;
			}
		}
	}

	@Override
	public boolean contains(double x, double y, Filter filter) {
		return containsShape(x, y) || super.contains(x, y, filter);
	}

	// default implementation may be overloaded and optimized for every shape
	private double[] tmpPoint = new double[2];

	public boolean containsShape(double x, double y) {
		/**
		 * ERROR: Если bounds меньше чем shape, то при клике на часть фигуры, которая находится вне bounds, объект не выделяется. 
		 * Данную оптимизацию необходимо применять выборочно.
		 */
		// optimization
		// Rectangle2D shapeBounds = getRectangleBounds(); // is probably
		// computed
		// if (!shapeBounds.contains(x, y)) // is not heavy operation to
		// determine shape doesn't contain a point
		// {
		// return false;
		// }

		tmpPoint[0] = x;
		tmpPoint[1] = y;
		getInverseTransform().transform(tmpPoint, 0, tmpPoint, 0, 1);
		x = tmpPoint[0];
		y = tmpPoint[1];

		Shape shape = getShape();
		if (shape != null) {
			if (checkFill()) {
				if (shape.contains(x, y)) {
					return true;
				}

				if (shape.intersects(x - 2, y - 2, 5, 5)) {
					return true;
				}
			} else {
				// boolean b11 = shape.contains(x - 2, y - 2);
				// boolean b12 = shape.contains(x + 2, y + 2);
				// if ((b11 | b12) &&
				// !(b11 & b12))
				// {
				// return true;
				// }
				//
				// boolean b21 = shape.contains(x + 2, y - 2);
				// if ((b11 | b12 | b21) &&
				// !(b11 & b12 & b21))
				// {
				// return true;
				// }
				//
				// boolean b22 = shape.contains(x - 2, y + 2);
				// if ((b11 | b12 | b21 | b22) &&
				// !(b11 & b12 & b21 & b22))
				// {
				// return true;
				// }

				// if(!b11 & !b12 & !b21 & !b22 &&
				// Crossings c =
				// Crossings.findCrossings(shape.getPathIterator(null), x - 2, y - 2, x + 2, y + 2);
				// if (c != null)
				// {
				// System.out.println("------- " + c + ", " + c.getXHi() + ", "
				// + c.getXLo() + ", " + c.getYHi() + ", " + c.getYLo());
				// c.print();
				// }
				// else
				// {
				// System.out.println();
				// }

				if (shape.intersects(x - 2, y - 2, 4, 4)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void paintComponent(Graphics2D g) {
		super.paintComponent(g);

		// draw shape
		paintShape(g);
	}

	@Override
	public void printComponent(Graphics2D g) {
		super.printComponent(g);

		// print shape
		paintShape(g);
	}

	private boolean antialias = false;

	public boolean isAntialias() {
		return antialias;
	}

	public void setAntialias(boolean antialias) {
		boolean oldValue = this.antialias;
		this.antialias = antialias;
		dispatchEvent(new JVGPropertyChangeEvent(this, PROPERTY_ANTIALIAS, oldValue, antialias));
	}

	private boolean fill = true;

	public boolean isFill() {
		return fill;
	}

	// TODO cache
	protected boolean checkFill() {
		if (!fill) {
			return false;
		} else if (painters != null) {
			int count = painters.size();
			for (int i = 0; i < count; i++) {
				Painter p = painters.get(i);
				if (p.getType() == Painter.FILL && p.isDraw() && p.getPaint() != null) {
					return true;
				}
			}
		}
		return false;
	}

	public void setFill(boolean fill) {
		boolean oldValue = this.fill;
		this.fill = fill;
		dispatchEvent(new JVGPropertyChangeEvent(this, PROPERTY_ISFILL, oldValue, fill));
	}

	// TODO use instead new Color(r, g, b, a) for common case interface Filter 
	//	public interface PaintFilter {
	//		public String getName();
	//
	//		public Paint getPaint();
	//
	//		public boolean requireAlfa();
	//
	//		public PaintFilter next();
	//	}

	public void setAlfa(int alfa) {
		if (alfa < 0) {
			alfa = 0;
		} else if (alfa > 255) {
			alfa = 255;
		}

		int oldValue = getAlfa();
		if (alfa != oldValue) {
			if (alfa != 255) {
				setComposePaint(new Color(255, 255, 255, alfa));
			} else {
				setComposePaint(null);
			}
			dispatchEvent(new JVGPropertyChangeEvent(this, PROPERTY_ALFA, oldValue, alfa));
		}
	}

	public int getAlfa() {
		if (composePaint instanceof Color) {
			Color alfaColor = (Color) composePaint;
			return alfaColor.getAlpha();
		}
		return 255;
	}

	private Paint composePaint = null;

	public Paint getComposePaint() {
		return composePaint;
	}

	// TODO: cache
	public Paint getDeepComposePaint() {
		Paint paint = composePaint;
		JVGContainer parent = this.parent;
		if (parent instanceof JVGShape) {
			JVGShape parentShape = (JVGShape) parent;
			Paint parentPaint = parentShape.getDeepComposePaint();
			if (parentPaint != null) {
				if (paint != null) {
					paint = new AlfaPaint(paint, parentPaint);
				} else {
					paint = parentPaint;
				}
			}
		}
		return paint;
	}

	public void setComposePaint(Paint composePaint) {
		Paint oldValue = this.composePaint;
		this.composePaint = composePaint;
		dispatchEvent(new JVGPropertyChangeEvent(this, PROPERTY_COMPOSE_PAINT, oldValue, composePaint));
	}

	protected List<Painter> painters = null;

	public void setPainterIndex(Painter painter, int index) {
		int oldIndex = getPainterIndex(painter);
		if (oldIndex != -1 && index >= 0 && index <= painters.size() && oldIndex != index) {
			painters.remove(oldIndex);
			if (index > oldIndex) {
				index--;
			}
			painters.add(index, painter);
			dispatchEvent(new JVGPropertyChangeEvent(this, PROPERTY_PAINTERS, 0, 1));
		}
	}

	public void addPainter(Painter painter) {
		addPainter(-1, painter);
	}

	public void addPainter(int index, Painter painter) {
		if (painter != null) {
			if (painters == null) {
				painters = new ArrayList<Painter>();
			}

			if (index != -1) {
				painters.add(index, painter);
			} else {
				painters.add(painter);
			}
			dispatchEvent(new JVGPropertyChangeEvent(this, PROPERTY_PAINTERS, 0, 1));
		}
	}

	public void setPainter(Painter painter) {
		if (painter != null) {
			List<Painter> painters = new ArrayList<>(1);
			painters.add(painter);
			setPainters(painters);
		}
	}

	public void setPainters(List<Painter> painters) {
		List<Painter> oldValue = this.painters;
		this.painters = painters;
		dispatchEvent(new JVGPropertyChangeEvent(this, PROPERTY_PAINTERS, oldValue, composePaint));
	}

	public int getPaintersCount() {
		if (painters != null) {
			return painters.size();
		} else {
			return 0;
		}
	}

	public Painter getPainter(int index) {
		if (painters != null && index >= 0 && index < painters.size()) {
			return painters.get(index);
		} else {
			return null;
		}
	}

	public <P extends Painter> P getPainter(Class<P> clazz) {
		if (painters != null) {
			int count = painters.size();
			for (int i = 0; i < count; i++) {
				Painter p = painters.get(i);
				if (clazz.isAssignableFrom(p.getClass())) {
					return (P) p;
				}
			}
		}
		return null;
	}

	public Painter removePainter(int index) {
		if (painters != null && index >= 0 && index < painters.size()) {
			Painter p = painters.remove(index);
			if (p != null) {
				dispatchEvent(new JVGPropertyChangeEvent(this, PROPERTY_PAINTERS, 0, 2));
				return p;
			}
		}
		return null;
	}

	public void removePainter(Painter painter) {
		if (painters != null) {
			int index = painters.indexOf(painter);
			removePainter(index);
		}
	}

	public void removeAllPainter(int type) {
		if (painters != null) {
			boolean removed = false;
			for (int i = painters.size() - 1; i >= 0; i--) {
				Painter p = painters.get(i);
				if (p.getType() == type) {
					removed |= painters.remove(i) != null;
				}
			}
			if (removed) {
				dispatchEvent(new JVGPropertyChangeEvent(this, PROPERTY_PAINTERS, 0, 2));
			}
		}
	}

	public int getPainterIndex(Painter painter) {
		if (painters != null) {
			return painters.indexOf(painter);
		} else {
			return -1;
		}
	}

	public void removeAllPainters() {
		if (painters != null && painters.size() > 0) {
			painters.clear();
			dispatchEvent(new JVGPropertyChangeEvent(this, PROPERTY_PAINTERS, 0, 2));
		}
	}

	public boolean isPaint(int type) {
		if (painters != null) {
			for (int i = 0; i < painters.size(); i++) {
				if (painters.get(i).getType() == type) {
					return true;
				}
			}
		}
		return false;
	}

	static List<Painter> defaultPainters = null;

	public static List<Painter> getDefaultPainters() {
		if (defaultPainters == null) {
			defaultPainters = new ArrayList<Painter>();
			defaultPainters.add(new FillPainter(FillPainter.DEFAULT_COLOR));
		}
		return defaultPainters;
	}

	public void paintShape(Graphics2D g) {
		if (isAntialias()) {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}

		List<Painter> painters = null;
		if (this.painters != null && this.painters.size() > 0) {
			painters = this.painters;
		} else {
			try {
				Method method = getClass().getMethod("getDefaultPainters");
				painters = (List<Painter>) method.invoke(null);
			} catch (Exception exc) {
				painters = new ArrayList<Painter>();
				setPainters(painters);
				System.err.println("Can't get default painters");
			}
		}

		if (painters != null) {
			int len = painters.size();
			if (len > 0) {
				g.transform(getTransform());

				Shape oldClip = null;
				Shape clip = getClipShape();
				if (clip != null) {
					oldClip = g.getClip();
					g.setClip(clip);
				}

				for (int i = 0; i < len; i++) {
					Painter painter = painters.get(i);
					if (painterFilter == null || painterFilter.pass(this, painter)) {
						painter.paint(g, this);
					}
				}

				if (oldClip != null) {
					g.setClip(oldClip);
				}
				g.transform(getInverseTransform());
			}
		}

		if (isAntialias()) {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		}
		setPainterFilter(null);
	}

	protected AffineTransform transform = new AffineTransform(); // Identity transformation by default

	public AffineTransform getTransform() {
		return transform;
	}

	protected AffineTransform inverseTransform; // Identity transformation by default

	public AffineTransform getInverseTransform() {
		if (inverseTransform == null) {
			try {
				inverseTransform = transform.createInverse();
			} catch (NoninvertibleTransformException exc) {
				exc.printStackTrace();
				inverseTransform = new AffineTransform();
			}
		}
		return inverseTransform;
	}

	private double[] matrix_cache = new double[6];

	public void transformComponent(AffineTransform transform, Set<JVGShape> locked) {
		if (isLockTransform() || transform == null || transform.isIdentity()) {
			return;
		}

		this.transform.getMatrix(matrix_cache);
		this.transform.preConcatenate(transform);
		if (this.transform.getDeterminant() == 0) {
			this.transform.setTransform(matrix_cache[0], matrix_cache[1], matrix_cache[2], matrix_cache[3], matrix_cache[4], matrix_cache[5]);
			return;
		}

		locked.add(this);

		invalidate();

		JVGComponentEvent e = new JVGComponentEvent(this, JVGComponentEvent.COMPONENT_TRANSFORMED);
		dispatchEvent(e);
	}

	public void unlock() {
		Shape shape = getShape();
		if (shape instanceof CoordinablePath) {
			CoordinablePath path = (CoordinablePath) shape;
			path.unlock();
		}
	}

	public void transformChildren(AffineTransform transform, Set<JVGShape> locked) {
		if (isLockTransform() || transform == null || transform.isIdentity()) {
			return;
		}

		int childs_count = this.childrenCount;
		JVGComponent[] childs = this.children;
		for (int i = 0; i < childs_count; i++) {
			JVGComponent c = childs[i];
			if (c instanceof JVGShape) {
				JVGShape shapeComponent = (JVGShape) c;
				shapeComponent.transform(transform, locked);
			}
		}
	}

	public void transform(AffineTransform transform) {
		if (isLockTransform() || transform == null || transform.isIdentity()) {
			return;
		}

		Set<JVGShape> locked = new HashSet<JVGShape>();
		transform(transform, locked);
		JVGUtil.unlock(locked);
	}

	public void transform(AffineTransform transform, Set<JVGShape> locked) {
		if (isLockTransform() || transform == null || transform.isIdentity()) {
			return;
		}

		transformComponent(transform, locked);
		transformChildren(transform, locked);
	}

	private int lock_state = LOCK_NONE;

	public int getLockState() {
		return lock_state;
	}

	public boolean isLockShape() {
		return (lock_state & LOCK_SHAPE) != 0;
	}

	public boolean isLockTransform() {
		return (lock_state & LOCK_TRANSFORM) != 0;
	}

	public void setLockState(int lock_state) {
		int oldValue = this.lock_state;
		this.lock_state = lock_state;
		dispatchEvent(new JVGPropertyChangeEvent(this, PROPERTY_LOCK_STATE, oldValue, lock_state));
	}

	@Override
	public void validate() {
		if (!isValid()) {
			// transformed shape
			transformedShape = computeTransformedShape();
			if (transformedShape == null) {
				Shape shape = getShape();
				if (shape != null) {
					transformedShape = shape instanceof MutableGeneralPath ? (MutableGeneralPath) shape : new MutableGeneralPath(shape);
				}
			}

			// initial bounds
			if (initialBounds == null) {
				initialBounds = computeInitialBounds();
				if (initialBounds == null) {
					initialBounds = new Rectangle2D.Double(0, 0, 0, 0);
				}
			}

			// original bounds
			if (originalBounds == null) {
				originalBounds = computeOriginalBounds();
				if (originalBounds == null) {
					originalBounds = new Rectangle2D.Double(0, 0, 0, 0);
				}
			}

			validateContainer();

			// try to find value for transformed shape
			if (transformedShape == null) {
				transformedShape = new MutableGeneralPath(initialBounds);
			}

			if (!isClip && !clipComputed) {
				clipShape = computeClip();
				clipComputed = true;
			}
		}
	}

	protected Shape computeClip() {
		Area area = null;
		Shape firstShape = null;
		int childs_count = this.childrenCount;
		JVGComponent[] childs = this.children;
		for (int i = 0; i < childs_count; i++) {
			JVGComponent c = childs[i];
			if (c instanceof JVGShape) {
				JVGShape child = (JVGShape) c;
				if (child.isClip()) {
					if (firstShape == null) {
						firstShape = child.getShape();
					} else if (area == null) {
						area = new Area(firstShape);
						area.add(new Area(child.getShape()));
						firstShape = null;
					} else {
						area.add(new Area(child.getShape()));
					}
				}
			}
		}
		if (isClipped()) {
			JVGShape parent = getParentShape();
			if (parent != null) {
				Shape parentBounds = parent.getBounds();
				if (firstShape == null) {
					return parentBounds;
				} else if (area == null) {
					area = new Area(firstShape);
					area.subtract(new Area(parentBounds));
					firstShape = null;
				} else {
					area.subtract(new Area(parentBounds));
				}
			}
		}
		return firstShape != null ? firstShape : area;
	}

	public JVGShape getParentShape() {
		JVGContainer parent = getParent();
		if (parent instanceof JVGShape) {
			return (JVGShape) parent;
		} else {
			return null;
		}
	}

	protected void validateContainer() {
		super.validate();
	}

	@Override
	public void invalidate() {
		if (isValid()) {
			super.invalidate();
			inverseTransform = null;
			initialBounds = null;
			originalBounds = null;
			transformedShape = null;
			clipComputed = false;
			clipShape = null;
		}
	}

	public MutableGeneralPath getPath() {
		return getPath(shapeId);
	}

	public MutableGeneralPath getPath(Object shapeId) {
		Shape shape = getShape(shapeId);
		MutableGeneralPath path;
		if (shape instanceof MutableGeneralPath) {
			path = (MutableGeneralPath) shape;
		} else {
			path = new MutableGeneralPath(shape);
		}
		return path;
	}

	@Override
	protected boolean isContainedIn(Shape shape) {
		if (shape instanceof Rectangle2D) {
			return super.isContainedIn(shape);
		} else {
			// first check on bounds
			Rectangle2D outerBounds = shape.getBounds2D();
			Rectangle2D bounds = getRectangleBounds();
			if (!outerBounds.contains(bounds)) {
				return false;
			}

			Area a1 = new Area(shape);
			Area a2 = new Area(getTransformedShape());
			a2.subtract(a1);
			return a2.isEmpty();
		}
	}

	/**
	 * Copy only appearence. Do not copy shape, image or text. Do not copy transformation.
	 */
	public void copyTo(JVGShape dst) {
		dst.setClipped(isClipped());
		dst.setClip(isClip());
		dst.setFocusable(isFocusable());
		dst.setOriginalBounds(isOriginalBounds());
		dst.setFill(isFill());
		dst.setAntialias(isAntialias());
		dst.setToolTipText(getToolTipText());
		dst.setComposePaint(getComposePaint());
		dst.setSelectable(isSelectable());
		dst.setVisible(isVisible());
		dst.setCursor(getCursor());

		// copy painters
		List<Painter> painters = null;
		if (this.painters != null) {
			painters = new ArrayList<Painter>(this.painters.size());
			for (Painter painter : this.painters) {
				painters.add((Painter) painter.clone());
			}
		}
		dst.setPainters(painters);
	}

	public PainterFilter getPainterFilter() {
		return painterFilter;
	}

	public void setPainterFilter(PainterFilter painterFilter) {
		this.painterFilter = painterFilter;
	}

	public boolean isClip() {
		return isClip;
	}

	public void setClip(boolean isClip) {
		boolean oldValue = this.isClip;
		this.isClip = isClip;
		dispatchEvent(new JVGPropertyChangeEvent(this, "isclip", oldValue, isClip));
	}

	public Shape getClipShape() {
		if (!clipComputed) {
			validate();
		}
		return clipShape;
	}
}
