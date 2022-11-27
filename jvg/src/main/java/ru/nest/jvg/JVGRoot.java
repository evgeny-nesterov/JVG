package ru.nest.jvg;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ru.nest.jvg.actionarea.JVGActionArea;
import ru.nest.jvg.event.JVGMouseEvent;
import ru.nest.jvg.event.JVGPropertyChangeEvent;
import ru.nest.jvg.geom.MutableGeneralPath;

public class JVGRoot extends JVGContainer implements Repainter {
	public final static int SELECTION_NONE = 0;

	public final static int SELECTION_AREA = 1;

	public final static int SELECTION_LASSO = 2;

	public JVGRoot(JVGPane pane) {
		setPane(pane);
		setSelectable(false);
		setSelectionType(SELECTION_NONE);
		setName("Root");
		newEventsOnly = true;
		register(this);
	}

	private JVGPane pane;

	@Override
	public JVGPane getPane() {
		return pane;
	}

	public void setPane(JVGPane pane) {
		this.pane = pane;
		populateConnectEvent(this);
	}

	private int selectionType = SELECTION_NONE;

	public int getSelectionType() {
		return selectionType;
	}

	public void setSelectionType(int selectionType) {
		if (this.selectionType != selectionType) {
			int oldValue = this.selectionType;
			this.selectionType = selectionType;

			switch (selectionType) {
				case SELECTION_AREA:
					selection = new AreaSelection();
					break;

				case SELECTION_LASSO:
					selection = new LassoSelection();
					break;

				default:
					selection = null;
					break;
			}

			dispatchEvent(new JVGPropertyChangeEvent(this, "select-type", oldValue, selectionType));
		}
	}

	@Override
	public void setSelected(boolean selected, boolean clear) {
		if (selected && clear) {
			JVGPane pane = getPane();
			if (pane != null) {
				JVGSelectionModel selectionModel = pane.getSelectionManager();
				if (selectionModel != null) {
					selectionModel.clearSelection();
					pane.setFocusOwner(null);
				}
			}
		}
	}

	@Override
	public boolean isSelected() {
		return false;
	}

	public boolean contains(double x, double y, boolean ignoreVisibility) {
		return x >= pane.getX() && x <= pane.getX() + pane.getWidth() && y >= pane.getY() && y <= pane.getY() + pane.getHeight();
	}

	@Override
	public JVGComponent getComponent(double x, double y) {
		JVGComponent child = getChild(x, y);
		if (child != null) {
			return child;
		} else {
			return this;
		}
	}

	@Override
	public void processMouseEvent(JVGMouseEvent e) {
		if (getPane().isEditable() && selection != null) {
			if (e.getID() == JVGMouseEvent.MOUSE_PRESSED) {
				if (e.getButton() == JVGMouseEvent.BUTTON1) {
					if (selectionRotater != null) {
						selectionRotater.stopRotate();
					}
					selectionRotater = new SelectionRotater(this, this);
					selectionRotater.start();

					drawSelection = true;
					selection.mousePressed(e);
					repaint();
				}
			} else if (e.getID() == JVGMouseEvent.MOUSE_RELEASED) {
				if (drawSelection) {
					if (selectionRotater != null) {
						selectionRotater.stopRotate();
						selectionRotater = null;
					}

					drawSelection = false;
					selection.mouseReleases(e);
					repaint();
				}
			} else if (e.getID() == JVGMouseEvent.MOUSE_DRAGGED) {
				selection.mouseDragged(e);
				repaint();
			}

			super.processMouseEvent(e);
		}
	}

	private SelectionRotater selectionRotater = null;

	private Set<JVGComponent> selectedComponentsHash = new HashSet<>();

	public void selectChilds(Shape selectionShape) {
		JVGPane pane = getPane();
		if (pane != null) {
			selectedComponentsHash.clear();
			selectChilds(selectionShape, selectedComponentsHash);
			JVGComponent[] selectedComponents = new JVGComponent[selectedComponentsHash.size()];
			selectedComponentsHash.toArray(selectedComponents);

			pane.getSelectionManager().setSelection(selectedComponents);
		}
	}

	// Don't use this optimization, this causes an errors
	// public void invalidateTree()
	// {
	// // don't invalidate childs
	// }

	@Override
	public void paint(Graphics2D g) {
		if (drawCommonSelection) {
			paintMultipleShapesSelection(g);
		}

		super.paint(g);

		if (drawSelection) {
			paintSelection(g);
		}
	}

	private Stroke commonSelectionStroke = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 3f, new float[] { 1f, 2f }, 0f);

	public void paintMultipleShapesSelection(Graphics2D g) {
		JVGSelectionModel selectionManager = getPane().getSelectionManager();
		int size = selectionManager.getSelectionCount();
		if (size > 1) {
			AffineTransform transform = g.getTransform();
			try {
				g.transform(transform.createInverse());
			} catch (NoninvertibleTransformException e) {
				e.printStackTrace();
			}

			Stroke oldStroke = g.getStroke();
			Rectangle2D bounds = selectionManager.getSelectionBounds();

			g.setStroke(commonSelectionStroke);
			g.setColor(Color.lightGray);
			g.draw(transform.createTransformedShape(bounds));

			g.setStroke(oldStroke);
			g.transform(transform);
		}
	}

	@Override
	public void paintSelection(Graphics2D g) {
		if (selection != null && selectionRotater.getStroke() != null) {
			AffineTransform transform = g.getTransform();
			try {
				g.transform(transform.createInverse());
			} catch (NoninvertibleTransformException e) {
				e.printStackTrace();
			}

			Stroke oldStroke = g.getStroke();
			g.setStroke(selectionRotater.getStroke());
			g.setXORMode(Color.white);
			g.setColor(Color.black);
			g.draw(transform.createTransformedShape(selection.getShape()));
			g.setPaintMode();
			g.setStroke(oldStroke);
			g.transform(transform);
		}
	}

	@Override
	public void repaint(JVGComponent c) {
		if (selection != null) {
			selection.repaint(c);
		}
	}

	private JVGSelection<?> selection;

	public JVGSelection<?> getSelection() {
		return selection;
	}

	public Rectangle2D getSelectionBounds() {
		if (selection != null) {
			Shape shape = selection.getShape();
			return shape instanceof Rectangle2D ? (Rectangle2D) shape : shape.getBounds2D();
		} else {
			return null;
		}
	}

	private boolean drawCommonSelection = true;

	public boolean isDrawCommonSelection() {
		return drawCommonSelection;
	}

	public void setDrawCommonSelection(boolean drawCommonSelection) {
		this.drawCommonSelection = drawCommonSelection;
	}

	private boolean drawSelection = false;

	class AreaSelection implements JVGSelection<Rectangle2D> {
		private double x, y;

		private Rectangle2D shape = new Rectangle2D.Double();

		@Override
		public Rectangle2D getShape() {
			return shape;
		}

		@Override
		public void mousePressed(JVGMouseEvent e) {
			if (e.getButton() == JVGMouseEvent.BUTTON1) {
				x = e.getX();
				y = e.getY();
				shape.setRect(x, y, 0, 0);
			}
		}

		@Override
		public void mouseReleases(JVGMouseEvent e) {
			// do nothing
		}

		@Override
		public void mouseDragged(JVGMouseEvent e) {
			shape.setRect(Math.min(x, e.getX()), Math.min(y, e.getY()), Math.abs(x - e.getX()), Math.abs(y - e.getY()));
			selectChilds(shape);
		}

		@Override
		public void repaint(JVGComponent c) {
			// c.repaint((int)shape.getX(), (int)shape.getY(),
			// (int)shape.getWidth(), 1);
			// c.repaint((int)shape.getX(), (int)(shape.getY() +
			// shape.getHeight() - 1), (int)shape.getWidth(), 1);
			// c.repaint((int)shape.getX(), (int)shape.getY(), 1,
			// (int)shape.getHeight());
			// c.repaint((int)(shape.getX() + shape.getWidth()),
			// (int)shape.getY(), 1, (int)shape.getHeight());

			c.repaint();
		}
	}

	class LassoSelection implements JVGSelection<MutableGeneralPath> {
		private double x, y;

		private MutableGeneralPath shape = new MutableGeneralPath();

		@Override
		public MutableGeneralPath getShape() {
			return shape;
		}

		@Override
		public void mousePressed(JVGMouseEvent e) {
			if (e.getButton() == JVGMouseEvent.BUTTON1) {
				x = e.getX();
				y = e.getY();
				shape.reset();
				shape.moveTo(e.getX(), e.getY());
			}
		}

		@Override
		public void mouseReleases(JVGMouseEvent e) {
			shape.closePath();
			selectChilds(shape);
		}

		@Override
		public void mouseDragged(JVGMouseEvent e) {
			shape.lineTo(e.getX(), e.getY());
		}

		@Override
		public void repaint(JVGComponent c) {
			c.repaint();
		}
	}

	// hash of all components
	private Map<Long, JVGComponent> hash = new HashMap<>(1);

	public JVGComponent getComponent(Long id) {
		return hash.get(id);
	}

	public long[] getAllComponents() {
		long[] ids = new long[hash.size()];
		int index = 0;
		for (Long id : hash.keySet()) {
			ids[index++] = id;
		}
		return ids;
	}

	protected void register(JVGComponent component) {
		Long id = component.getId();
		if (id != null) {
			hash.put(id, component);
		}

		if (component instanceof JVGContainer) {
			JVGContainer container = (JVGContainer) component;
			int childsCount = container.childrenCount;
			JVGComponent[] childs = container.children;
			for (int i = 0; i < childsCount; i++) {
				register(childs[i]);
			}
		}
	}

	protected void unregister(JVGComponent component) {
		Long id = component.getId();
		if (id != null) {
			hash.remove(id);
		}

		if (component instanceof JVGContainer) {
			JVGContainer container = (JVGContainer) component;
			int childsCount = container.childrenCount;
			JVGComponent[] childs = container.children;
			for (int i = 0; i < childsCount; i++) {
				unregister(childs[i]);
			}
		}
	}

	@Override
	public void add(JVGComponent c) {
		// action areas for root is always on top
		int index = -1;
		if (!(c instanceof JVGActionArea)) {
			JVGComponent[] childs = this.children;
			for (int i = 0; i < childrenCount; i++) {
				if (childs[i] instanceof JVGActionArea) {
					index = i;
					break;
				}
			}
		}
		add(c, index);
	}
}
