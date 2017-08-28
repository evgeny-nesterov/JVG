package javax.swing.dock;

import java.awt.Component;

public interface Resizable {
	public enum ResizeDirection {
		TOP, LEFT, BOTTOM, RIGHT
	}

	public boolean doResize(Component c, int width, int height, int dw, int dh);

	public Component getComponentToResize(Component c, ResizeDirection direction);
}
