package javax.swing.dock.selection;

import java.util.EventListener;

public interface SelectionChangeListener extends EventListener {
	void selectionChanged(SelectionChangeEvent e);
}
