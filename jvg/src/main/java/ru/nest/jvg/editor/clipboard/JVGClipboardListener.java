package ru.nest.jvg.editor.clipboard;

public interface JVGClipboardListener {
	public void dataAddedToClipboard(JVGClipboardEvent event);

	public void dataRemovedFromClipboard(JVGClipboardEvent event);

	public void dataSaved(JVGClipboardEvent event);
}
