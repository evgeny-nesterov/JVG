package javax.swing.outlook;

import java.awt.Component;

public class OutlookTab {
	protected Component header;

	protected Component comp;

	protected double weight = 1;

	protected boolean isMaximized;

	protected boolean isVisible = true;

	public OutlookTab(Component header, Component comp) {
		this.header = header;
		this.comp = comp;
	}

	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
		comp.setVisible(isVisible);
	}

	public boolean isVisible() {
		return isVisible;
	}

	public Component getHeader() {
		return header;
	}

	public Component getComponent() {
		return comp;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public void addWeight(double weight) {
		this.weight += weight;
	}

	public boolean isMaximized() {
		return isMaximized;
	}

	public void setMaximized(boolean isMaximized) {
		this.isMaximized = isMaximized;
		comp.setVisible(isMaximized);
	}
}
