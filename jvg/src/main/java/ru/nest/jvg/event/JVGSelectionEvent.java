package ru.nest.jvg.event;

import ru.nest.jvg.JVGComponent;

public class JVGSelectionEvent extends JVGEvent {
	public JVGSelectionEvent(JVGComponent[] o, boolean[] added) {
		super(o.length > 0 ? o[0] : null, -1);
		this.o = o;
		this.added = added;
	}

	public JVGSelectionEvent(JVGComponent o, boolean added) {
		super(o, -1);
		this.o = new JVGComponent[] { o };
		this.added = new boolean[] { added };
	}

	private JVGComponent[] o;

	public JVGComponent[] getChange() {
		return o;
	}

	private boolean[] added;

	public boolean[] getAdded() {
		return added;
	}
}
