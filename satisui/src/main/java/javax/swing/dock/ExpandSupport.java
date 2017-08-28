package javax.swing.dock;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;

public class ExpandSupport {
	public final static String COMPONENT_PROPERTY = "/expand-parent/";

	private boolean isExpandable = true;

	private boolean isMinimizable = true;

	private Component outerControl;

	private Component innerControl;

	private boolean isExpanded = false;

	private Object collapsedContraints;

	private Container collapsedParent;

	private Container expandedParent;

	private LayoutManager tempLayout;

	private List<Component> tempComponents;

	private List<Object> tempComponentsConstr;

	private String expandParentProperty;

	private boolean isMinimized = false;

	private Dimension sizeBeforeMinimize = null;

	public ExpandSupport(Component expandControl, Component minimizeControl) {
		this(expandControl, COMPONENT_PROPERTY, minimizeControl);
	}

	public ExpandSupport(Component expandControl, String expandParentProperty, Component minimizeControl) {
		this.expandParentProperty = expandParentProperty;
		setExpandControl(expandControl);
		setMinimizeControl(minimizeControl);
	}

	private MouseListener mouseListener;

	public void expandOnDoubleClick(Component clickControl) {
		MouseAdapter mouseListener = new MouseAdapter() {
			long lastClickTime;

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					double dt = System.currentTimeMillis() - lastClickTime;
					boolean doubleClick = false;
					if (dt < 300) {
						doubleClick = true;
					}
					lastClickTime = System.currentTimeMillis();

					if (doubleClick) {
						setExpanded(!isExpanded());
					}
				}
			}
		};
		setMouseListener(clickControl, mouseListener);
	}

	public void minimizeOnDblCLick(Component clickControl) {
		MouseAdapter mouseListener = new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					if (e.getClickCount() == 2 && !isExpanded()) {
						if (!isMinimized) {
							minimize();
						} else {
							minimizeRestore();
						}
					}
				}
			}
		};
		setMouseListener(clickControl, mouseListener);
	}

	public void setMouseListener(Component control, MouseListener mouseListener) {
		if (this.mouseListener != null) {
			control.removeMouseListener(this.mouseListener);
		}
		this.mouseListener = mouseListener;
		if (mouseListener != null) {
			control.addMouseListener(mouseListener);
		}
	}

	public void setExpandControl(Component expandControl) {
		this.outerControl = expandControl;
	}

	public void setExpandable(boolean isExpandable) {
		this.isExpandable = isExpandable;
	}

	public boolean isExpandable() {
		return isExpandable;
	}

	public boolean isExpanded() {
		return isExpanded;
	}

	public void setExpanded(boolean isExpanded) {
		if (isExpanded) {
			expand();
		} else {
			expandRestore();
		}
	}

	public boolean expand() {
		if (isExpanded || !isExpandable) {
			return false;
		}

		collapsedParent = outerControl.getParent();

		expandedParent = null;
		Component p = collapsedParent;
		while (p != null) {
			if (p instanceof JComponent && ((JComponent) p).getClientProperty(expandParentProperty) != null) {
				expandedParent = (Container) p;
				break;
			}
			p = p.getParent();
		}

		if (expandedParent != null) {
			isExpanded = true;

			collapsedContraints = DockUtils.getConstraints(collapsedParent, outerControl);
			if (collapsedParent instanceof JTabbedPane) {
				JTabbedPane tab = (JTabbedPane) collapsedParent;
				int index = tab.indexOfComponent(outerControl);
				String title = tab.getTitleAt(index);
				Icon icon = tab.getIconAt(index);
				Component header = tab.getTabComponentAt(index);
				collapsedContraints = new Object[] { title, icon, header };
			}
			if (collapsedContraints == null) {
				collapsedContraints = collapsedParent.getComponentZOrder(outerControl);
			}

			removeAllTemp(expandedParent);

			expandedParent.setLayout(new BorderLayout());
			expandedParent.add(outerControl, BorderLayout.CENTER);
			expandedParent.invalidate();
			expandedParent.validate();
			expandedParent.repaint();

			// restore if minimized
			if (isMinimized) {
				innerControl.setVisible(true);
				outerControl.setPreferredSize(sizeBeforeMinimize);
				outerControl.invalidate();
				outerControl.validate();
				outerControl.repaint();
			}

			fireExpandStateChanged(isExpanded, isMinimized);
			return true;
		}
		return false;
	}

	public void setMinimized(boolean isMinimized) {
		if (isMinimized) {
			if (isExpanded) {
				setExpanded(false);
			}
			minimize();
		} else {
			minimizeRestore();
		}
	}

	public void minimize() {
		if (isExpanded() || !isMinimizable) {
			return;
		}

		if (!isMinimized) {
			sizeBeforeMinimize = outerControl.getPreferredSize();
		}

		outerControl.setPreferredSize(null);
		innerControl.setPreferredSize(null);
		innerControl.setVisible(false);

		outerControl.invalidate();
		outerControl.validate();
		outerControl.repaint();

		if (!isMinimized) {
			isMinimized = true;
			fireExpandStateChanged(isExpanded, isMinimized);
		}
	}

	public void minimizeRestore() {
		if (isExpanded()) {
			setExpanded(false);
		}

		if (isMinimized) {
			isMinimized = false;
			innerControl.setVisible(true);
			outerControl.setPreferredSize(sizeBeforeMinimize);

			fireExpandStateChanged(isExpanded, isMinimized);
		}
	}

	public void expandRestore() {
		if (!isExpanded || !isExpandable) {
			return;
		}

		restoreAllTemp(expandedParent);

		if (collapsedParent != expandedParent) {
			if (collapsedParent instanceof JTabbedPane) {
				JTabbedPane tab = (JTabbedPane) collapsedParent;
				Object[] data = (Object[]) collapsedContraints;
				tab.addTab((String) data[0], (Icon) data[1], outerControl);
				tab.setTabComponentAt(tab.indexOfComponent(outerControl), (Component) data[2]);
				tab.setSelectedComponent(outerControl);
			} else if (collapsedContraints instanceof Integer) {
				collapsedParent.add(outerControl, ((Integer) collapsedContraints).intValue());
			} else {
				collapsedParent.add(outerControl, collapsedContraints);
			}
			collapsedParent.invalidate();
			collapsedParent.validate();
			collapsedParent.repaint();
		}

		isExpanded = false;

		// restore minimize after expand restore
		if (isMinimized) {
			minimize();
		}

		fireExpandStateChanged(isExpanded, isMinimized);
	}

	private void removeAllTemp(Container container) {
		if (tempComponents != null && tempComponents.size() != 0) {
			return;
		}

		tempComponents = new ArrayList<Component>();
		tempComponentsConstr = new ArrayList<Object>();
		tempLayout = container.getLayout();

		for (int i = 0; i < container.getComponentCount(); i++) {
			Component c = container.getComponent(i);
			tempComponents.add(c);

			Object constraints = DockUtils.getConstraints(container, c);
			if (container instanceof JTabbedPane) {
				JTabbedPane tab = (JTabbedPane) container;
				int index = tab.indexOfComponent(c);
				String title = tab.getTitleAt(index);
				Icon icon = tab.getIconAt(index);
				Component header = tab.getTabComponentAt(index);
				constraints = new Object[] { title, icon, header };
			}
			// if (constraints == null) {
			// constraints = i;
			// }
			tempComponentsConstr.add(constraints);
		}

		container.removeAll();
		container.invalidate();
		container.validate();
		container.repaint();
	}

	private void restoreAllTemp(Container container) {
		if (tempComponents != null && tempComponents.size() > 0) {
			container.removeAll();

			container.setLayout(tempLayout);
			int index = 0;
			for (Component c : tempComponents) {
				Object constraints = tempComponentsConstr.get(index);
				if (constraints instanceof Integer) {
					container.add(outerControl, ((Integer) constraints).intValue());
				} else if (container instanceof JTabbedPane) {
					JTabbedPane tab = (JTabbedPane) container;
					Object[] data = (Object[]) constraints;
					tab.addTab((String) data[0], (Icon) data[1], c);
					tab.setTabComponentAt(tab.indexOfComponent(c), (Component) data[2]);
				} else {
					container.add(c, constraints);
				}

				index++;
			}
			container.invalidate();
			container.validate();
			container.repaint();
		}
		tempComponents = null;
		tempComponentsConstr = null;
		tempLayout = null;
	}

	public static interface ExpandListener {
		public void expandStateChanged(boolean isExpanded, boolean isMinimized);
	}

	private List<ExpandListener> listeners = new ArrayList<ExpandListener>(0);

	public void addListener(ExpandListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	public void removeListener(ExpandListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	private void fireExpandStateChanged(boolean isExpanded, boolean isMinimized) {
		synchronized (listeners) {
			for (ExpandListener listener : listeners) {
				listener.expandStateChanged(isExpanded, isMinimized);
			}
		}
	}

	public Component getMinimizeControl() {
		return innerControl;
	}

	public void setMinimizeControl(Component minimizeControl) {
		this.innerControl = minimizeControl;
	}

	public boolean isMinimized() {
		return isMinimized;
	}

	public boolean isMinimizable() {
		return isMinimizable;
	}

	public void setMinimizable(boolean isMinimizable) {
		this.isMinimizable = isMinimizable;
	}
}
