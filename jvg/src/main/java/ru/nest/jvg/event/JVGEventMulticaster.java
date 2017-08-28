package ru.nest.jvg.event;

import java.lang.reflect.Array;

public class JVGEventMulticaster implements JVGComponentListener, JVGContainerListener, JVGFocusListener, JVGSelectionListener, JVGMouseListener, JVGKeyListener, JVGMouseWheelListener, JVGPropertyChangeListener, JVGPeerListener {
	protected final JVGEventListener a, b;

	public JVGEventMulticaster(JVGEventListener a, JVGEventListener b) {
		this.a = a;
		this.b = b;
	}

	// --- add ---
	protected static JVGEventListener addInternal(JVGEventListener a, JVGEventListener b) {
		if (a == null)
			return b;
		if (b == null)
			return a;
		return new JVGEventMulticaster(a, b);
	}

	public static JVGContainerListener add(JVGContainerListener a, JVGContainerListener b) {
		return (JVGContainerListener) addInternal(a, b);
	}

	public static JVGComponentListener add(JVGComponentListener a, JVGComponentListener b) {
		return (JVGComponentListener) addInternal(a, b);
	}

	public static JVGFocusListener add(JVGFocusListener a, JVGFocusListener b) {
		return (JVGFocusListener) addInternal(a, b);
	}

	public static JVGSelectionListener add(JVGSelectionListener a, JVGSelectionListener b) {
		return (JVGSelectionListener) addInternal(a, b);
	}

	public static JVGKeyListener add(JVGKeyListener a, JVGKeyListener b) {
		return (JVGKeyListener) addInternal(a, b);
	}

	public static JVGMouseListener add(JVGMouseListener a, JVGMouseListener b) {
		return (JVGMouseListener) addInternal(a, b);
	}

	public static JVGMouseWheelListener add(JVGMouseWheelListener a, JVGMouseWheelListener b) {
		return (JVGMouseWheelListener) addInternal(a, b);
	}

	public static JVGPropertyChangeListener add(JVGPropertyChangeListener a, JVGPropertyChangeListener b) {
		return (JVGPropertyChangeListener) addInternal(a, b);
	}

	public static JVGPeerListener add(JVGPeerListener a, JVGPeerListener b) {
		return (JVGPeerListener) addInternal(a, b);
	}

	// --- remove ---
	protected JVGEventListener remove(JVGEventListener oldl) {
		if (oldl == a)
			return b;
		if (oldl == b)
			return a;
		JVGEventListener a2 = removeInternal(a, oldl);
		JVGEventListener b2 = removeInternal(b, oldl);
		if (a2 == a && b2 == b) {
			return this;
		}
		return addInternal(a2, b2);
	}

	protected static JVGEventListener removeInternal(JVGEventListener l, JVGEventListener oldl) {
		if (l == oldl || l == null) {
			return null;
		} else if (l instanceof JVGEventMulticaster) {
			return ((JVGEventMulticaster) l).remove(oldl);
		} else {
			return l;
		}
	}

	public static JVGContainerListener remove(JVGContainerListener a, JVGContainerListener b) {
		return (JVGContainerListener) removeInternal(a, b);
	}

	public static JVGComponentListener remove(JVGComponentListener a, JVGComponentListener b) {
		return (JVGComponentListener) removeInternal(a, b);
	}

	public static JVGFocusListener remove(JVGFocusListener a, JVGFocusListener b) {
		return (JVGFocusListener) removeInternal(a, b);
	}

	public static JVGSelectionListener remove(JVGSelectionListener a, JVGSelectionListener b) {
		return (JVGSelectionListener) removeInternal(a, b);
	}

	public static JVGMouseListener remove(JVGMouseListener a, JVGMouseListener b) {
		return (JVGMouseListener) removeInternal(a, b);
	}

	public static JVGKeyListener remove(JVGKeyListener a, JVGKeyListener b) {
		return (JVGKeyListener) removeInternal(a, b);
	}

	public static JVGMouseWheelListener remove(JVGMouseWheelListener a, JVGMouseWheelListener b) {
		return (JVGMouseWheelListener) removeInternal(a, b);
	}

	public static JVGPropertyChangeListener remove(JVGPropertyChangeListener a, JVGPropertyChangeListener b) {
		return (JVGPropertyChangeListener) removeInternal(a, b);
	}

	public static JVGPeerListener remove(JVGPeerListener a, JVGPeerListener b) {
		return (JVGPeerListener) removeInternal(a, b);
	}

	public static <T extends JVGComponentListener> T[] getListeners(JVGComponentListener l, Class<T> listenerType) {
		int n = getListenerCount(l, listenerType);
		T[] result = (T[]) Array.newInstance(listenerType, n);
		populateListenerArray(result, l, 0);
		return result;
	}

	private static <T extends JVGComponentListener> int getListenerCount(JVGEventListener l, Class<T> listenerType) {
		if (l instanceof JVGEventMulticaster) {
			JVGEventMulticaster mc = (JVGEventMulticaster) l;
			return getListenerCount(mc.a, listenerType) + getListenerCount(mc.b, listenerType);
		} else {
			return listenerType.isInstance(l) ? 1 : 0;
		}
	}

	private static int populateListenerArray(JVGEventListener[] a, JVGEventListener l, int index) {
		if (l instanceof JVGEventMulticaster) {
			JVGEventMulticaster mc = (JVGEventMulticaster) l;
			int lhs = populateListenerArray(a, mc.a, index);
			return populateListenerArray(a, mc.b, lhs);
		} else if (a.getClass().getComponentType().isInstance(l)) {
			a[index] = l;
			return index + 1;
		} else {
			return index;
		}
	}

	// --- implementations ---
	@Override
	public void componentTransformed(JVGComponentEvent e) {
		((JVGComponentListener) a).componentTransformed(e);
		((JVGComponentListener) b).componentTransformed(e);
	}

	@Override
	public void componentGeometryChanged(JVGComponentEvent e) {
		((JVGComponentListener) a).componentGeometryChanged(e);
		((JVGComponentListener) b).componentGeometryChanged(e);
	}

	@Override
	public void componentShown(JVGComponentEvent e) {
		((JVGComponentListener) a).componentShown(e);
		((JVGComponentListener) b).componentShown(e);
	}

	@Override
	public void componentHidden(JVGComponentEvent e) {
		((JVGComponentListener) a).componentHidden(e);
		((JVGComponentListener) b).componentHidden(e);
	}

	@Override
	public void componentAdded(JVGContainerEvent e) {
		((JVGContainerListener) a).componentAdded(e);
		((JVGContainerListener) b).componentAdded(e);
	}

	@Override
	public void componentRemoved(JVGContainerEvent e) {
		((JVGContainerListener) a).componentRemoved(e);
		((JVGContainerListener) b).componentRemoved(e);
	}

	@Override
	public void componentOrderChanged(JVGContainerEvent e) {
		((JVGContainerListener) a).componentOrderChanged(e);
		((JVGContainerListener) b).componentOrderChanged(e);
	}

	@Override
	public void connectedToPeer(JVGPeerEvent e) {
		((JVGPeerListener) a).connectedToPeer(e);
		((JVGPeerListener) b).connectedToPeer(e);
	}

	@Override
	public void disconnectedFromPeer(JVGPeerEvent e) {
		((JVGPeerListener) a).disconnectedFromPeer(e);
		((JVGPeerListener) b).disconnectedFromPeer(e);
	}

	@Override
	public void focusLost(JVGFocusEvent e) {
		((JVGFocusListener) a).focusLost(e);
		((JVGFocusListener) b).focusLost(e);
	}

	@Override
	public void focusGained(JVGFocusEvent e) {
		((JVGFocusListener) a).focusGained(e);
		((JVGFocusListener) b).focusGained(e);
	}

	@Override
	public void selectionChanged(JVGSelectionEvent e) {
		((JVGSelectionListener) a).selectionChanged(e);
		((JVGSelectionListener) b).selectionChanged(e);
	}

	@Override
	public void mouseClicked(JVGMouseEvent e) {
		((JVGMouseListener) a).mouseClicked(e);
		((JVGMouseListener) b).mouseClicked(e);
	}

	@Override
	public void mousePressed(JVGMouseEvent e) {
		((JVGMouseListener) a).mousePressed(e);
		((JVGMouseListener) b).mousePressed(e);
	}

	@Override
	public void mouseReleased(JVGMouseEvent e) {
		((JVGMouseListener) a).mouseReleased(e);
		((JVGMouseListener) b).mouseReleased(e);
	}

	@Override
	public void mouseEntered(JVGMouseEvent e) {
		((JVGMouseListener) a).mouseEntered(e);
		((JVGMouseListener) b).mouseEntered(e);
	}

	@Override
	public void mouseExited(JVGMouseEvent e) {
		((JVGMouseListener) a).mouseExited(e);
		((JVGMouseListener) b).mouseExited(e);
	}

	@Override
	public void mouseDragged(JVGMouseEvent e) {
		((JVGMouseListener) a).mouseDragged(e);
		((JVGMouseListener) b).mouseDragged(e);
	}

	@Override
	public void mouseMoved(JVGMouseEvent e) {
		((JVGMouseListener) a).mouseMoved(e);
		((JVGMouseListener) b).mouseMoved(e);
	}

	@Override
	public void keyTyped(JVGKeyEvent e) {
		((JVGKeyListener) a).keyTyped(e);
		((JVGKeyListener) b).keyTyped(e);
	}

	@Override
	public void keyPressed(JVGKeyEvent e) {
		((JVGKeyListener) a).keyPressed(e);
		((JVGKeyListener) b).keyPressed(e);
	}

	@Override
	public void keyReleased(JVGKeyEvent e) {
		((JVGKeyListener) a).keyReleased(e);
		((JVGKeyListener) b).keyReleased(e);
	}

	@Override
	public void mouseWheelMoved(JVGMouseWheelEvent e) {
		((JVGMouseWheelListener) a).mouseWheelMoved(e);
		((JVGMouseWheelListener) b).mouseWheelMoved(e);
	}

	@Override
	public void propertyChange(JVGPropertyChangeEvent e) {
		((JVGPropertyChangeListener) a).propertyChange(e);
		((JVGPropertyChangeListener) b).propertyChange(e);
	}
}
