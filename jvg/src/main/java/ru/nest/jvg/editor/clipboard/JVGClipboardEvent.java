package ru.nest.jvg.editor.clipboard;

public class JVGClipboardEvent {
	public final static int DATA_ADDED = 0;

	public final static int DATA_REMOVED = 1;

	public final static int DATA_SAVED = 2;

	public JVGClipboardEvent(JVGClipboardContext ctx, int id) {
		this.ctx = ctx;
		this.id = id;
	}

	private JVGClipboardContext ctx;

	public JVGClipboardContext getContext() {
		return ctx;
	}

	private int id;

	public int getID() {
		return id;
	}
}
