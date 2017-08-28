package ru.nest.jvg.actionarea;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGPane;
import ru.nest.jvg.editor.resources.JVGLocaleManager;
import ru.nest.jvg.event.JVGFocusEvent;
import ru.nest.jvg.event.JVGFocusListener;
import ru.nest.jvg.event.JVGMouseEvent;
import ru.nest.jvg.event.JVGMouseListener;
import ru.nest.jvg.event.JVGSelectionEvent;
import ru.nest.jvg.event.JVGSelectionListener;

public abstract class JVGActionArea extends JVGComponent implements JVGSelectionListener, JVGFocusListener, JVGMouseListener {
	public final static int VISIBILITY_TYPE_CUSTOM = -1;

	public final static int VISIBILITY_TYPE_ALWAYS = 0;

	public final static int VISIBILITY_TYPE_SELECTED = 1;

	public final static int VISIBILITY_TYPE_FOCUSED = 2;

	protected static JVGLocaleManager lm = JVGLocaleManager.getInstance();

	public JVGActionArea() {
		this(VISIBILITY_TYPE_SELECTED);
	}

	protected JVGActionArea(int visibilityType) {
		this.visibilityType = visibilityType;

		setSelectable(false);
		setFocusable(false);
		setActive(this.visibilityType == VISIBILITY_TYPE_ALWAYS);

		addMouseListener(this);
	}

	@Override
	public void mouseClicked(JVGMouseEvent e) {
	}

	@Override
	public void mouseDragged(JVGMouseEvent e) {
	}

	@Override
	public void mouseEntered(JVGMouseEvent e) {
	}

	@Override
	public void mouseExited(JVGMouseEvent e) {
	}

	@Override
	public void mouseMoved(JVGMouseEvent e) {
	}

	@Override
	public void mousePressed(JVGMouseEvent e) {
		currentAction = this;
		setActionActive(true);
		repaint();
	}

	@Override
	public void mouseReleased(JVGMouseEvent e) {
		reset();
		repaint();
	}

	private int visibilityType;

	public int getVisibilityType() {
		return visibilityType;
	}

	@Override
	public boolean isSelectable() {
		return false;
	}

	public static void reset() {
		actionActive = false;
		currentAction = null;
	}

	private static boolean actionActive = false;

	public static void setActionActive(boolean actionActive) {
		JVGActionArea.actionActive = actionActive;
	}

	public static boolean isActionActive() {
		return actionActive;
	}

	private static ActionsFilter filter;

	public static void setActionsFilter(ActionsFilter filter) {
		JVGActionArea.filter = filter;
	}

	public static ActionsFilter getActionsFilter() {
		return filter;
	}

	private static JVGActionArea currentAction = null;

	public static JVGActionArea getCurrentAction() {
		return currentAction;
	}

	protected boolean listenParentSelection = false;

	protected boolean listenParentFocus = false;

	protected boolean firstAdd = true;

	@Override
	public void addNotify() {
		super.addNotify();
		registerListener();
	}

	protected void registerListener() {
		if (firstAdd) {
			firstAdd = false;
			if (visibilityType == VISIBILITY_TYPE_SELECTED) {
				listenParentSelection = true;
				parent.addSelectionListener(this);
			} else if (visibilityType == VISIBILITY_TYPE_FOCUSED) {
				listenParentFocus = true;
				parent.addFocusListener(this);
			}
		}
	}

	@Override
	public void removeNotify() {
		unregisterListener();
		super.removeNotify();
	}

	protected void unregisterListener() {
		if (listenParentSelection) {
			listenParentSelection = false;
			parent.removeSelectionListener(this);
		} else if (listenParentFocus) {
			listenParentFocus = false;
			parent.removeFocusListener(this);
		}
		firstAdd = true;
	}

	@Override
	public void selectionChanged(JVGSelectionEvent event) {
		if (visibilityType == VISIBILITY_TYPE_SELECTED) {
			if (parent.isSelected()) {
				setActive(true);

				// TODO: this line changes order of the action areas
				// Aim of this line was ...
				// getParent().toFront(this, false);
			} else {
				setActive(false);
			}
		}
	}

	protected boolean isDrawAction() {
		return isActive() && isVisible() && (currentAction == this || (currentAction == null && !actionActive)) && (filter == null || filter.pass(this));
	}

	@Override
	public void paint(Graphics2D g) {
		// Can't be selected => its not necessary to draw super
		// super.paint(g);

		if (isDrawAction()) {
			AffineTransform transform = getPane().getTransform();
			try {
				g.transform(transform.createInverse());
			} catch (NoninvertibleTransformException e) {
				e.printStackTrace();
			}
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			paintAction(g, transform);

			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			g.transform(transform);
		}
	}

	protected Color getColor() {
		return Color.white;
	}

	protected Color getBorderColor() {
		return Color.white;
	}

	private Shape transformedBounds = null;

	public Shape getTransformedBounds() {
		return transformedBounds;
	}

	private Shape scaledBounds = null;

	public Shape getScaledBounds() {
		return scaledBounds;
	}

	@Override
	protected final Shape computeBounds() {
		Shape bounds = computeActionBounds();
		if (bounds != null) {
			JVGPane pane = getPane();
			if (pane != null) {
				AffineTransform transform = pane.getTransform();
				if (transform != null && !transform.isIdentity()) {
					Rectangle2D rectBounds = bounds instanceof Rectangle2D ? (Rectangle2D) bounds : bounds.getBounds2D();
					double cx = rectBounds.getX() + rectBounds.getWidth() / 2;
					double cy = rectBounds.getY() + rectBounds.getHeight() / 2;

					AffineTransform scale = AffineTransform.getTranslateInstance(-cx, -cy);
					scale.preConcatenate(AffineTransform.getScaleInstance(1 / transform.getScaleX(), 1 / transform.getScaleY()));
					scale.preConcatenate(AffineTransform.getTranslateInstance(cx, cy));

					scaledBounds = scale.createTransformedShape(bounds);
					transformedBounds = transform.createTransformedShape(scaledBounds);
				} else {
					scaledBounds = bounds;
					transformedBounds = bounds;
				}
			} else {
				scaledBounds = bounds;
				transformedBounds = bounds;
			}
		} else {
			scaledBounds = null;
			transformedBounds = null;
		}
		return bounds;
	}

	protected abstract Shape computeActionBounds();

	public void paintAction(Graphics2D g, AffineTransform transform) {
		if (transformedBounds != null) {
			Color color = getColor();
			if (color != null) {
				g.setColor(color);
				g.fill(transformedBounds);
			}

			Color borderColor = getBorderColor();
			if (borderColor != null) {
				g.setColor(borderColor);
				g.draw(transformedBounds);
			}
		}
	}

	protected boolean isActive;

	public void setActive(boolean isActive) {
		if (this.isActive != isActive) {
			this.isActive = isActive;
			onMouseExit();
			invalidate();
		}
	}

	public boolean isActive() {
		JVGPane pane = getPane();
		if (pane != null) {
			return pane.isEditable() && isActive && isVisible();
		} else {
			return false;
		}
	}

	@Override
	public boolean contains(double x, double y) {
		return isActive() && (filter == null || filter.pass(this)) && (scaledBounds == null || scaledBounds.contains(x, y));
	}

	@Override
	public void focusLost(JVGFocusEvent e) {
		if (visibilityType == VISIBILITY_TYPE_FOCUSED) {
			setActive(false);
		}
	}

	@Override
	public void focusGained(JVGFocusEvent e) {
		if (visibilityType == VISIBILITY_TYPE_FOCUSED) {
			setActive(true);
			getParent().toFront(this, false);
		}
	}
}
