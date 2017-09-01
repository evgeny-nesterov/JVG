package ru.nest.jvg;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import ru.nest.jvg.actionarea.JVGActionArea;
import ru.nest.jvg.event.JVGContainerEvent;
import ru.nest.jvg.event.JVGContainerListener;
import ru.nest.jvg.event.JVGEvent;
import ru.nest.jvg.event.JVGEventMulticaster;
import ru.nest.jvg.event.JVGMouseEvent;
import ru.nest.jvg.event.JVGPeerEvent;
import ru.nest.jvg.resource.Script;

public class JVGContainer extends JVGComponent {
	public JVGContainer() {
	}

	protected JVGComponent[] children = new JVGComponent[0];

	protected int childrenCount = 0;

	public int getChildCount() {
		return childrenCount;
	}

	public JVGComponent getChild(int index) {
		synchronized (getLock()) {
			if ((index < 0) || (index >= childrenCount)) {
				throw new ArrayIndexOutOfBoundsException("No such child: " + index);
			}
			return children[index];
		}
	}

	public JVGComponent getChild(String name) {
		if (name != null && name.length() > 0) {
			for (int i = childrenCount; --i >= 0;) {
				if (name.equals(children[i].getName())) {
					return children[i];
				}
			}
		}
		return null;
	}

	public int getChildIndex(JVGComponent c) {
		synchronized (getLock()) {
			if (c.parent == this) {
				for (int i = childrenCount; --i >= 0;) {
					if (children[i] == c) {
						return i;
					}
				}
			}
		}
		return -1;
	}

	public final JVGComponent[] getChildren() {
		synchronized (getLock()) {
			JVGComponent list[] = new JVGComponent[childrenCount];
			System.arraycopy(children, 0, list, 0, childrenCount);
			return list;
		}
	}

	public final boolean hasSelectedChild() {
		int childs_count = this.childrenCount;
		JVGComponent[] childs = this.children;
		for (int i = 0; i < childs_count; i++) {
			if (childs[i].isSelected()) {
				return true;
			}
		}
		return false;
	}

	public final boolean hasFocusedChild() {
		int childs_count = this.childrenCount;
		JVGComponent[] childs = this.children;
		for (int i = 0; i < childs_count; i++) {
			if (childs[i].isFocused()) {
				return true;
			}
		}
		return false;
	}

	public <C extends JVGComponent> boolean containsChild(Class<C>... types) {
		int childs_count = this.childrenCount;
		JVGComponent[] childs = this.children;
		for (Class<C> type : types) {
			for (int i = 0; i < childs_count; i++) {
				if (type.isInstance(childs[i])) {
					return true;
				}
			}
		}
		return false;
	}

	public <C extends JVGComponent> boolean isVisibleAnyChild(Class<C>... types) {
		int childs_count = this.childrenCount;
		JVGComponent[] childs = this.children;
		for (Class<C> type : types) {
			for (int i = 0; i < childs_count; i++) {
				if (childs[i].isVisible() && type.isInstance(childs[i])) {
					return true;
				}
			}
		}
		return false;
	}

	public <C extends JVGComponent> C[] getChilds(Class<?>... types) {
		List<C> list = new ArrayList<C>(0);
		getChilds(false, list, types);
		return (C[]) list.toArray(new JVGComponent[0]);
	}

	public <C extends JVGComponent> C[] getChilds(boolean deep, Class<?>... types) {
		List<C> list = new ArrayList<C>(0);
		getChilds(deep, list, types);
		return (C[]) list.toArray(new JVGComponent[0]);
	}

	protected <C extends JVGComponent> void getChilds(boolean deep, List<C> list, Class<?>... types) {
		int childs_count = this.childrenCount;
		JVGComponent[] childs = this.children;

		for (Class<?> type : types) {
			for (int i = 0; i < childs_count; i++) {
				if (type.isInstance(childs[i])) {
					list.add((C) childs[i]);
				}
				if (deep && childs[i] instanceof JVGContainer) {
					JVGContainer c = (JVGContainer) childs[i];
					c.getChilds(true, list, types);
				}
			}
		}
	}

	public void add(JVGComponent[] c) {
		if (c != null) {
			add(c, 0, c.length, childrenCount);
		}
	}

	public void add(JVGComponent[] c, int index) {
		if (c != null) {
			add(c, 0, c.length, index);
		}
	}

	public void add(JVGComponent[] c, int start, int end, int index) {
		if (c != null && c.length > 0) {
			synchronized (getLock()) {
				for (int i = start; i < end; i++) {
					add(c[i], index++);
				}
			}
		}
	}

	public void add(JVGComponent c) {
		add(c, -1);
	}

	public void add(JVGComponent c, int index) {
		synchronized (getLock()) {
			if (index > childrenCount || (index < 0 && index != -1)) {
				throw new IllegalArgumentException("illegal component position");
			}

			if (c instanceof JVGContainer) {
				for (JVGContainer cn = this; cn != null; cn = cn.parent) {
					if (cn == c) {
						throw new IllegalArgumentException("adding container's parent to itself");
					}
				}

				if (c instanceof JVGRoot) {
					throw new IllegalArgumentException("adding a root to a container");
				}
			}

			if (c.parent != null) {
				c.parent.remove(c);
				if (index > childrenCount) {
					throw new IllegalArgumentException("illegal component position");
				}
			}

			if (childrenCount == children.length) {
				JVGComponent newchilds[] = new JVGComponent[childrenCount * 2 + 1];
				System.arraycopy(children, 0, newchilds, 0, childrenCount);
				children = newchilds;
			}

			if (index == -1 || index == childrenCount) {
				children[childrenCount++] = c;
			} else {
				System.arraycopy(children, index, children, index + 1, childrenCount - index);
				children[index] = c;
				childrenCount++;
			}
			c.parent = this;

			if (valid) {
				invalidate();
			}

			// add component and its childs to hash
			JVGComponent topAncestor = getRoot();
			if (topAncestor instanceof JVGRoot) {
				JVGRoot root = (JVGRoot) topAncestor;
				root.register(c);
			}

			if (getPane() != null) {
				populateConnectEvent(c);
			}

			c.addNotify();

			if (containerListener != null) {
				JVGContainerEvent e = new JVGContainerEvent(this, JVGContainerEvent.COMPONENT_ADDED, c);
				dispatchEvent(e);
			}
		}
	}

	protected void populateConnectEvent(JVGComponent c) {
		// if(c.getPane() != getPane())
		// {
		c.dispatchEvent(new JVGPeerEvent(c, JVGPeerEvent.CONNECTED_TO_PEER, c.getPane(), getPane()));
		// }

		if (c instanceof JVGContainer) {
			JVGContainer container = (JVGContainer) c;
			int components_count = container.childrenCount;
			JVGComponent[] childs = container.children;
			for (int i = 0; i < components_count; i++) {
				populateConnectEvent(childs[i]);
			}
		}
	}

	@Override
	public void validate() {
		if (!valid) {
			super.validate();
			validateTree();
		}
	}

	@Override
	public void invalidate() {
		if (valid) {
			super.invalidate();
			invalidateTree();
		}
	}

	/**
	 * Invalidate all childs
	 */
	public void invalidateTree() {
		int childs_count = this.childrenCount;
		JVGComponent[] childs = this.children;
		for (int i = 0; i < childs_count; i++) {
			JVGComponent c = childs[i];
			c.invalidate();
		}
	}

	/**
	 * Validate all childs
	 */
	protected void validateTree() {
		int childs_count = this.childrenCount;
		JVGComponent[] childs = this.children;
		for (int i = 0; i < childs_count; i++) {
			JVGComponent c = childs[i];
			c.validate();
		}
	}

	public void remove(JVGComponent c) {
		synchronized (getLock()) {
			if (c.parent == this) {
				for (int i = childrenCount; --i >= 0;) {
					if (children[i] == c) {
						remove(i);
					}
				}
			}
		}
	}

	public void remove(int index) {
		synchronized (getLock()) {
			if (index < 0 || index >= childrenCount) {
				throw new ArrayIndexOutOfBoundsException(index);
			}
			JVGComponent c = children[index];
			c.removeNotify();
			c.parent = null;
			System.arraycopy(children, index + 1, children, index, childrenCount - index - 1);
			children[--childrenCount] = null;

			if (valid) {
				invalidate();
			}

			if (containerListener != null) {
				JVGContainerEvent e = new JVGContainerEvent(this, JVGContainerEvent.COMPONENT_REMOVED, c);
				dispatchEvent(e);
			}

			// remove component and its childs from hash
			JVGComponent topAncestor = getRoot();
			if (topAncestor instanceof JVGRoot) {
				JVGRoot root = (JVGRoot) topAncestor;
				root.unregister(c);
			}
		}
	}

	public void removeAll() {
		synchronized (getLock()) {
			while (childrenCount > 0) {
				JVGComponent c = children[--childrenCount];
				children[childrenCount] = null;
				c.removeNotify();
				c.parent = null;

				if (containerListener != null) {
					JVGContainerEvent e = new JVGContainerEvent(this, JVGContainerEvent.COMPONENT_REMOVED, c);
					dispatchEvent(e);
				}
			}

			if (valid) {
				invalidate();
			}
		}
	}

	public void setComponentIndex(JVGComponent c, int index) {
		synchronized (getLock()) {
			if (c.parent == this) {
				int childs_count = this.childrenCount;
				if (index == -1) {
					index = childs_count - 1;
				}

				if (index >= 0 && index <= childs_count) {
					for (int i = childs_count; --i >= 0;) {
						if (children[i] == c) {
							if (i == index) {
								return;
							}

							if (index > i) {
								System.arraycopy(children, i + 1, children, i, index - i);
							} else if (index < i) {
								System.arraycopy(children, index, children, index + 1, i - index);
							}
							children[index] = c;

							if (containerListener != null) {
								JVGContainerEvent e = new JVGContainerEvent(this, JVGContainerEvent.COMPONENT_ORDER_CHANGED, c);
								dispatchEvent(e);
							}
							break;
						}
					}
				}
			}
		}
	}

	public void toFront(JVGComponent c, boolean withParent) {
		synchronized (getLock()) {
			setComponentIndex(c, -1);
			if (withParent) {
				toFront();
			}
		}
	}

	public void toBack(JVGComponent c, boolean withParent) {
		synchronized (getLock()) {
			setComponentIndex(c, 0);
			if (withParent) {
				toBack();
			}
		}
	}

	public int toUp(JVGComponent c) {
		synchronized (getLock()) {
			if (c.parent == this) {
				int index = getChildIndex(c);
				if (index == childrenCount - 1) {
					index = 0;
				} else {
					index++;
				}
				setComponentIndex(c, index);
				return index;
			}
		}

		return -1;
	}

	public int toDown(JVGComponent c) {
		synchronized (getLock()) {
			if (c.parent == this) {
				int index = getChildIndex(c);
				if (index == 0) {
					index = childrenCount - 1;
				} else {
					index--;
				}
				setComponentIndex(c, index);
				return index;
			}
		}

		return -1;
	}

	// --- Container listeners ---
	private JVGContainerListener containerListener = null;

	public void addContainerListener(JVGContainerListener listener) {
		if (listener != null) {
			containerListener = JVGEventMulticaster.add(containerListener, listener);
			newEventsOnly = true;
		}
	}

	public void removeContainerListener(JVGContainerListener listener) {
		if (listener != null) {
			containerListener = JVGEventMulticaster.remove(containerListener, listener);
		}
	}

	protected void processContainerEvent(JVGContainerEvent e) {
		JVGContainerListener listener = containerListener;
		if (listener != null) {
			switch (e.getID()) {
				case JVGContainerEvent.COMPONENT_ADDED:
					listener.componentAdded(e);
					break;

				case JVGContainerEvent.COMPONENT_REMOVED:
					listener.componentRemoved(e);
					break;

				case JVGContainerEvent.COMPONENT_ORDER_CHANGED:
					listener.componentOrderChanged(e);
					break;
			}
		}
	}

	@Override
	protected void processEvent(JVGEvent e) {
		if (e instanceof JVGContainerEvent) {
			processContainerEvent((JVGContainerEvent) e);
			return;
		}
		super.processEvent(e);
	}

	@Override
	protected void dispatchEventImpl(JVGEvent e) {
		if (e instanceof JVGMouseEvent) {
			JVGMouseEvent me = (JVGMouseEvent) e;
			retargetMouseEvent(me);
			me.consume();
			return;
		}
		super.dispatchEventImpl(e);
	}

	void dispatchEventToItself(JVGEvent e) {
		super.dispatchEventImpl(e);
	}

	private JVGComponent targetLastEntered = null;

	private JVGComponent targetLastPressed = null;

	private void retargetMouseEvent(JVGMouseEvent e) {
		switch (e.getID()) {
			case JVGMouseEvent.MOUSE_ENTERED:
				JVGComponent c = getComponent(e.getX(), e.getY());
				processEnterEvent(e, c);
				break;

			case JVGMouseEvent.MOUSE_RELEASED:
				retargetMouseEvent(e, targetLastPressed);

				c = getComponent(e.getX(), e.getY());
				if (c != targetLastEntered) {
					targetLastPressed = null;
					processExitEvent(e);
					processEnterEvent(e, c);
				} else {
					targetLastPressed = null;
				}
				break;

			case JVGMouseEvent.MOUSE_CLICKED:
				c = getComponent(e.getX(), e.getY());
				retargetMouseEvent(e, c);
				break;

			case JVGMouseEvent.MOUSE_MOVED:
			case JVGMouseEvent.MOUSE_PRESSED:
			case JVGMouseEvent.MOUSE_WHEEL:
				c = getComponent(e.getX(), e.getY());

				// enter - exit
				if (c != targetLastEntered) {
					processExitEvent(e);
					processEnterEvent(e, c);
				}

				// need for dragging
				if (e.getID() == JVGMouseEvent.MOUSE_PRESSED) {
					targetLastPressed = c;
				}

				retargetMouseEvent(e, c);
				break;

			case JVGMouseEvent.MOUSE_EXITED:
				processExitEvent(e);
				break;

			case JVGMouseEvent.MOUSE_DRAGGED:
				processMouseDraggedEvent(e);
				break;
		}
	}

	private void retargetMouseEvent(JVGMouseEvent e, JVGComponent c) {
		if (c == this) {
			dispatchEventToItself(e);
		} else {
			if (c != null) {
				JVGMouseEvent retargetEvent = new JVGMouseEvent(e.getOriginEvent(), c, e.getID(), e.getWhen(), e.getModifiers(), e.getX(), e.getY(), e.getAdjustedX(), e.getAdjustedY(), e.getClickCount(), e.getButton());
				c.dispatchEvent(retargetEvent);
			}
		}
	}

	private void processMouseDraggedEvent(JVGMouseEvent e) {
		if (targetLastPressed != null) {
			if (targetLastPressed == this) {
				dispatchEventToItself(e);
			} else {
				JVGMouseEvent draggedEvent = new JVGMouseEvent(e.getOriginEvent(), targetLastPressed, e.getID(), e.getWhen(), e.getModifiers(), e.getX(), e.getY(), e.getAdjustedX(), e.getAdjustedY(), e.getClickCount(), e.getButton());
				targetLastPressed.dispatchEvent(draggedEvent);
			}
		}
	}

	@Override
	protected void processEnterEvent(JVGMouseEvent e, JVGComponent c) {
		super.processEnterEvent(e, c);
		if (c == this || (c != null && !(c instanceof JVGContainer))) {
			JVGMouseEvent enterEvent = new JVGMouseEvent(e.getOriginEvent(), c, JVGMouseEvent.MOUSE_ENTERED, e.getWhen(), e.getModifiers(), e.getX(), e.getY(), e.getAdjustedX(), e.getAdjustedY(), e.getClickCount(), e.getButton());
			if (c == this) {
				dispatchEventToItself(enterEvent);
			} else {
				c.dispatchEvent(enterEvent);
			}

			JVGPane pane = getPane();
			if (pane != null) {
				pane.setLastEnteredComponent(c);
			}
		} else {
			if (c != null) {
				retargetMouseEvent(e, c);
			}
		}
		targetLastEntered = c;
	}

	@Override
	protected void processExitEvent(JVGMouseEvent e) {
		super.processExitEvent(e);
		if (targetLastEntered != null && targetLastPressed == null) {
			JVGMouseEvent exitEvent = new JVGMouseEvent(e.getOriginEvent(), targetLastEntered, JVGMouseEvent.MOUSE_EXITED, e.getWhen(), e.getModifiers(), e.getX(), e.getY(), e.getAdjustedX(), e.getAdjustedY(), e.getClickCount(), e.getButton());
			if (targetLastEntered == this) {
				dispatchEventToItself(exitEvent);
			} else {
				targetLastEntered.dispatchEvent(exitEvent);
			}

			targetLastEntered = null;
		}
	}

	@Override
	public void addNotify() {
		synchronized (getLock()) {
			super.addNotify();
			int childs_count = this.childrenCount;
			JVGComponent childs[] = this.children;
			for (int i = 0; i < childs_count; i++) {
				childs[i].addNotify();
			}
		}
	}

	@Override
	public void removeNotify() {
		synchronized (getLock()) {
			int childs_count = this.childrenCount;
			JVGComponent childs[] = this.children;
			for (int i = childs_count - 1; i >= 0; i--) {
				if (childs[i] != null) {
					childs[i].removeNotify();
				}
			}
			super.removeNotify();
		}
	}

	private final static Filter defaultFilter = new DefaultFilter();

	public JVGComponent getComponent(double x, double y) {
		return getComponent(x, y, defaultFilter);
	}

	public JVGComponent getComponent(double x, double y, Filter filter) {
		JVGComponent child = getChild(x, y, filter);
		if (child != null) {
			return child;
		} else if ((filter == null || filter.pass(this))) { // && getBounds().contains(x, y))
			return this;
		} else {
			return null;
		}
	}

	public JVGComponent getChild(double x, double y) {
		return getChild(x, y, defaultFilter);
	}

	// private Rectangle visibleRectangle = new Rectangle();
	public JVGComponent getChild(double x, double y, Filter filter) {
		// JVGPane pane = getPane();
		// if (pane != null)
		// {
		// pane.computeVisibleRect(visibleRectangle);
		// if (x < visibleRectangle.x || y < visibleRectangle.y ||
		// x > visibleRectangle.x + visibleRectangle.width || y >
		// visibleRectangle.y + visibleRectangle.height)
		// {
		// return null;
		// }
		// }

		int childs_count = this.childrenCount;
		JVGComponent childs[] = this.children;
		for (int i = childs_count - 1; i >= 0; i--) {
			JVGComponent c = childs[i];
			boolean isContainer = c instanceof JVGContainer;
			if ((filter == null || filter.pass(c)) && ((isContainer && ((JVGContainer) c).contains(x, y, filter)) || (!isContainer && c.contains(x, y)))) {
				return c;
			}
		}
		return null;
	}

	public JVGComponent getDeepestComponent(double x, double y) {
		return getDeepestComponent(x, y, defaultFilter);
	}

	public JVGComponent getDeepestComponent(double x, double y, Filter filter) {
		int childs_count = this.childrenCount;
		JVGComponent childs[] = this.children;
		for (int i = childs_count - 1; i >= 0; i--) {
			JVGComponent c = childs[i];
			if (c instanceof JVGContainer) {
				JVGContainer container = (JVGContainer) c;
				c = container.getDeepestComponent(x, y, filter);
				if (c != null) {
					return c;
				}
			} else if (filter.pass(c) && c.contains(x, y)) {
				return c;
			}
		}

		if (filter.pass(this) && contains(x, y)) {
			return this;
		} else {
			return null;
		}
	}

	// --- default implementations ---
	@Override
	public void paint(Graphics2D g) {
		// paint component
		paintComponent(g);

		// paint children
		paintChildren(g);

		// paint from script
		executeScript(Script.PAINT);

		// paint selection
		if (isSelected()) {
			paintSelection(g);
		}
	}

	@Override
	public void print(Graphics2D g) {
		// print component
		printComponent(g);

		// paint children
		printChildren(g);
	}

	public void paintChildren(Graphics2D g) {
		int childs_count = this.childrenCount;
		if (childs_count > 0) {
			JVGComponent[] childs = this.children;
			for (int i = 0; i < childs_count; i++) {
				JVGComponent c = childs[i];
				if (c.isVisible()) {
					try {
						paintChild(g, c);
					} catch (Exception exc) {
						// System.err.println("JVGContainer.paintChildren: " + exc.toString());
						exc.printStackTrace();
					}
				}
			}
		}
	}

	public void paintChild(Graphics2D g, JVGComponent child) {
		child.paint(g);
	}

	public void printChildren(Graphics2D g) {
		int childs_count = this.childrenCount;
		if (childs_count > 0) {
			JVGComponent[] childs = this.children;
			for (int i = 0; i < childs_count; i++) {
				JVGComponent c = childs[i];
				if (c.isVisible() && !(c instanceof JVGActionArea)) {
					try {
						printChild(g, c);
					} catch (Exception exc) {
						exc.printStackTrace();
					}
				}
			}
		}
	}

	public void printChild(Graphics2D g, JVGComponent child) {
		child.print(g);
	}

	@Override
	public boolean contains(double x, double y) {
		return contains(x, y, defaultFilter);
	}

	public boolean contains(double x, double y, Filter filter) {
		return containsChilds(x, y, filter);
	}

	public boolean containsChilds(double x, double y) {
		return containsChilds(x, y, defaultFilter);
	}

	public boolean containsChilds(double x, double y, Filter filter) {
		int childs_count = this.childrenCount;
		JVGComponent[] childs = this.children;
		for (int i = 0; i < childs_count; i++) {
			JVGComponent c = childs[i];
			if (!c.isClipped() && (filter == null || filter.pass(c)) && c.contains(x, y)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected Shape computeBounds() {
		return computeChildsBounds();
	}

	protected Rectangle2D computeChildsBounds() {
		int childs_count = this.childrenCount;
		JVGComponent[] childs = this.children;

		Rectangle2D bounds = null;
		double minX = 0, minY = 0, maxX = 0, maxY = 0;
		for (int i = 0; i < childs_count; i++) {
			JVGComponent c = childs[i];
			if (!(c instanceof JVGActionArea) && c.isVisible()) {
				Rectangle2D r = c.getRectangleBounds();
				if (r != null) {
					if (bounds == null) {
						bounds = new Rectangle2D.Double();
						minX = r.getX();
						minY = r.getY();
						maxX = r.getX() + r.getWidth();
						maxY = r.getY() + r.getHeight();
					} else {
						if (minX > r.getX()) {
							minX = r.getX();
						}
						if (maxX < r.getX() + r.getWidth()) {
							maxX = r.getX() + r.getWidth();
						}

						if (minY > r.getY()) {
							minY = r.getY();
						}
						if (maxY < r.getY() + r.getHeight()) {
							maxY = r.getY() + r.getHeight();
						}
					}
				} else {
					// can't compute rect bounds!
				}
			}
		}

		if (bounds != null) {
			bounds.setRect(minX, minY, maxX - minX, maxY - minY);
		}
		return bounds;
	}

	/**
	 * Select childs
	 */
	protected void selectChilds(Shape selectionShape, Set<JVGComponent> selection) {
		if (selectionShape != null) {
			int childs_count = this.childrenCount;
			JVGComponent childs[] = this.children;
			for (int i = childs_count - 1; i >= 0; i--) {
				JVGComponent c = childs[i];
				if (c.isVisible() && c.isSelectableInPane() && c.isContainedIn(selectionShape)) {
					selection.add(c);
				}

				if (c instanceof JVGContainer) {
					((JVGContainer) c).selectChilds(selectionShape, selection);
				}
			}
		}
	}

	public JVGComponent[] find(String pattern, boolean matchCase, boolean wholeWord, boolean regexp) {
		return JVGUtil.find(this, pattern, matchCase, wholeWord, regexp);
	}

	public JVGComponent findByID(Long id) {
		return JVGUtil.findByID(this, id);
	}

	public void getChildsByTags(Collection<String> tags, Set<JVGComponent> components) {
		for (int i = 0; i < childrenCount; i++) {
			JVGComponent c = children[i];
			if (c.hasTags(tags)) {
				components.add(c);
			}

			if (c instanceof JVGContainer) {
				JVGContainer container = (JVGContainer) c;
				container.getChildsByTags(tags, components);
			}
		}
	}

	public void setChildrenSelected(boolean selected) {
		for (int i = 0; i < childrenCount; i++) {
			children[i].setSelected(selected);
		}
	}
}
