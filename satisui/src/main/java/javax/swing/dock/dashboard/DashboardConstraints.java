package javax.swing.dock.dashboard;

import java.awt.Component;

public class DashboardConstraints {
	public enum DashboardDirection {
		BACKWARD, FORWARD, FIRST_INSERT, NONE
	}

	public Component c;

	public DashboardDirection direction;

	public DashboardConstraints(Component c, DashboardDirection direction) {
		this.c = c;
		this.direction = direction;
	}
}
