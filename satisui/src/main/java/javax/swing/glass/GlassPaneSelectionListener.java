package javax.swing.glass;

import java.awt.event.MouseEvent;

public interface GlassPaneSelectionListener {
	/**
	 * Indicates that the pane has been acted upon in some way - usually any type of mouse event.
	 */
	public void glassPaneSelected(MouseEvent e);
}
