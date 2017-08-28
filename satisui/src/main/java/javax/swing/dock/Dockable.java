package javax.swing.dock;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;

import javax.swing.dock.selection.SelectionModel;

public interface Dockable<C> {
	public C getDockConstraints(DockFlow flow, int x, int y);

	public void drawDockPlace(Graphics g, DockFlow flow, C constraints);

	// 1. get undock component constraints (not required)
	// 2. remove undock component (not required)
	// 3. compute dock constraints dependent on flow and undock component
	// constraints (not required)
	// 4. create dock content panel (required)
	// 5. add content panel to this panel (required)
	// 6. revalidate (required)
	public void dock(DockFlow flow, C constraints);

	// Clear data (not required for implementation).
	public void undock(DockFlow flow);

	public Cursor getDockCursor(DockFlow flow, C constraints);

	// title component is created in dock content panel or used for custom
	// content panel build
	public Component createHeader(DockFlow flow);

	public boolean acceptUndock(DockFlow flow, Dockable<?> dst);

	public boolean acceptDock(DockFlow flow);

	public ResizeSupport getResizeSupport();

	public SelectionModel getSelectionModel();
}
