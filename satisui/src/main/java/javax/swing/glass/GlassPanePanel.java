package javax.swing.glass;

import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

/**
 * Empty non-opaque panel to be used as a glass pane for event capture
 */
public class GlassPanePanel extends JPanel implements MouseInputListener {
	private static final long serialVersionUID = 1L;

	/** Creates a new instance of GlassPanePanel */
	public GlassPanePanel() {
		setVisible(false);
		setOpaque(false);
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	/**
	 * Override to return false.
	 */
	@Override
	public boolean isOpaque() {
		return false;
	}

	/**
	 * Invoked when a mouse button is pressed on a component and then dragged.
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
	}

	/**
	 * Invoked when the mouse cursor has been moved onto a component but no buttons have been pushed.
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
	}

	/**
	 * Invoked when the mouse button has been clicked (pressed and released) on a component.
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
	}

	/**
	 * Invoked when a mouse button has been pressed on a component.
	 */
	@Override
	public void mousePressed(MouseEvent e) {
	}

	/**
	 * Invoked when a mouse button has been released on a component.
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
	}

	/**
	 * Invoked when the mouse enters a component.
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * Invoked when the mouse exits a component.
	 */
	@Override
	public void mouseExited(MouseEvent e) {
	}
}
