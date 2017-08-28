package javax.swing.dock.accordeon;

import java.awt.Component;

public class AccordeonConstraints {
	public enum AccordeonDirection {
		BACKWARD, FORWARD, FIRST_INSERT, NONE
	}

	public Component c;

	public AccordeonDirection direction;

	public AccordeonConstraints(Component c, AccordeonDirection direction) {
		this.c = c;
		this.direction = direction;
	}
}
