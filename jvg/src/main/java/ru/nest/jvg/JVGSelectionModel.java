package ru.nest.jvg;

import java.awt.geom.Rectangle2D;

import ru.nest.jvg.event.JVGSelectionListener;

public interface JVGSelectionModel {
	void setSelection(JVGComponent o);

	void setSelection(JVGComponent[] o);

	void addSelection(JVGComponent o);

	void addSelection(JVGComponent[] o);

	void removeSelection(JVGComponent o);

	void removeSelection(JVGComponent[] o);

	void clearSelection();

	JVGComponent[] getSelection();

	int getSelectionCount();

	boolean isSelected(JVGComponent o);

	void addSelectionListener(JVGSelectionListener listener);

	void removeSelectionListener(JVGSelectionListener listener);

	Rectangle2D getSelectionBounds();

	void invalidate();

	boolean isValid();
}
