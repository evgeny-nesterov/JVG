package ru.nest.jvg;

import java.awt.geom.Rectangle2D;

import ru.nest.jvg.event.JVGSelectionListener;

public interface JVGSelectionModel {
	public void setSelection(JVGComponent o);

	public void setSelection(JVGComponent[] o);

	public void addSelection(JVGComponent o);

	public void addSelection(JVGComponent[] o);

	public void removeSelection(JVGComponent o);

	public void removeSelection(JVGComponent[] o);

	public void clearSelection();

	public JVGComponent[] getSelection();

	public int getSelectionCount();

	public boolean isSelected(JVGComponent o);

	public void addSelectionListener(JVGSelectionListener listener);

	public void removeSelectionListener(JVGSelectionListener listener);

	public Rectangle2D getSelectionBounds();

	public void invalidate();

	public boolean isValid();
}
