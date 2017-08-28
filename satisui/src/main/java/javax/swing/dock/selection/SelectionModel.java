package javax.swing.dock.selection;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import javax.swing.JComponent;

public class SelectionModel {
	private JComponent target;

	private Component selectedComponent = null;

	private boolean enabled = true;

	public SelectionModel(JComponent target) {
		this.target = target;
	}

	public void setSelected(int index) {
		if (index >= 0 && index < target.getComponentCount()) {
			setSelected(target.getComponent(index));
		}
	}

	public void setSelected(Component selectedComponent) {
		if (enabled && this.selectedComponent != selectedComponent) {
			this.selectedComponent = selectedComponent;
			SelectionChangeListener[] listeners = getSelectionChangeListeners();
			SelectionChangeEvent event = new SelectionChangeEvent(this);
			for (SelectionChangeListener listener : listeners) {
				listener.selectionChanged(event);
			}
			target.repaint();
		}
	}

	public Component getSelectedComponent() {
		return selectedComponent;
	}

	public int getSelectedIndex() {
		return selectedComponent != null ? target.getComponentZOrder(selectedComponent) : -1;
	}

	public SelectionChangeListener[] getSelectionChangeListeners() {
		return target.getListeners(SelectionChangeListener.class);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		if (this.enabled != enabled) {
			if (!enabled) {
				clear();
			}
			this.enabled = enabled;
		}
	}

	public void clear() {
		setSelected(null);
	}

	private static Stroke selectionStroke = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f, new float[] { 1f, 1f }, 0f);

	public void drawSelection(Graphics g) {
		if (selectedComponent != null && selectedComponent.isShowing()) {
			int x = selectedComponent.getX() - 1;
			int y = selectedComponent.getY() - 1;
			int w = selectedComponent.getWidth() + 2;
			int h = selectedComponent.getHeight() + 2;

			Graphics2D g2d = (Graphics2D) g;
			g2d.setColor(Color.lightGray);
			g2d.drawRect(x, y, w, h);
			g2d.setStroke(selectionStroke);
			g2d.setColor(Color.darkGray);
			g2d.drawRect(x, y, w, h);
		}
	}
}
