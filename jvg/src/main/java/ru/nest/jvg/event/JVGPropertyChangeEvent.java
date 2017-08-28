package ru.nest.jvg.event;

import ru.nest.jvg.JVGComponent;

public class JVGPropertyChangeEvent extends JVGEvent {
	public JVGPropertyChangeEvent(JVGComponent source, String propertyName, Object oldValue, Object newValue) {
		super(source, -1);
		this.propertyName = propertyName;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	String propertyName;

	public String getPropertyName() {
		return propertyName;
	}

	Object oldValue;

	public Object getOldValue() {
		return oldValue;
	}

	Object newValue;

	public Object getNewValue() {
		return newValue;
	}

	@Override
	public String toString() {
		return propertyName + "=" + newValue;
	}
}
