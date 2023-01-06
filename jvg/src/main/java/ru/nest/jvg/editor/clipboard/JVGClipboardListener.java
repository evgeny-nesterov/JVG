package ru.nest.jvg.editor.clipboard;

public interface JVGClipboardListener {
	void dataAddedToClipboard(JVGClipboardEvent event);

	void dataRemovedFromClipboard(JVGClipboardEvent event);

	void dataSaved(JVGClipboardEvent event);
}
