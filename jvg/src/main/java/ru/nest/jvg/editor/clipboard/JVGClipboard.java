package ru.nest.jvg.editor.clipboard;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JVGClipboard {
	private File dir;

	public JVGClipboard(File configDir) {
		dir = new File(configDir, "clipboard");
		dir.mkdirs();
	}

	public void load() {
		for (File file : dir.listFiles()) {
			if (file.getName().endsWith(".xml")) {
				try {
					JVGClipboardContext ctx = new JVGClipboardContext(file.getAbsolutePath());
					add(ctx);
				} catch (Exception exc) {
					exc.printStackTrace();
					file.renameTo(new File(file.getParentFile(), file.getName() + ".bad"));
				}
			}
		}
	}

	private Set<JVGClipboardContext> data = new HashSet<JVGClipboardContext>();

	public boolean add(final JVGClipboardContext ctx) {
		synchronized (data) {
			if (data.contains(ctx)) {
				return false;
			}
			data.add(ctx);
		}

		fireClipboardEvent(new JVGClipboardEvent(ctx, JVGClipboardEvent.DATA_ADDED));

		if (ctx.getFile() == null) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						File file = null;
						while (file == null || file.exists()) {
							file = new File(dir, "clip" + System.currentTimeMillis() + ".xml");
						}

						Writer w = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
						w.write(ctx.getData());
						w.flush();
						w.close();

						ctx.setFile(file.getAbsolutePath());
						fireClipboardEvent(new JVGClipboardEvent(ctx, JVGClipboardEvent.DATA_SAVED));
					} catch (Exception exc) {
						exc.printStackTrace();
					}
				}
			}, "save-clipboard").start();
		}
		return true;
	}

	public void remove(JVGClipboardContext ctx) {
		synchronized (data) {
			data.remove(ctx);
		}
		fireClipboardEvent(new JVGClipboardEvent(ctx, JVGClipboardEvent.DATA_REMOVED));
	}

	private List<JVGClipboardListener> listeners = new ArrayList<JVGClipboardListener>();

	public void addListener(JVGClipboardListener listener) {
		if (listener != null) {
			synchronized (listeners) {
				listeners.add(listener);
			}
		}
	}

	public void removeListener(JVGClipboardListener listener) {
		if (listener != null) {
			synchronized (listeners) {
				listeners.remove(listener);
			}
		}
	}

	protected void fireClipboardEvent(JVGClipboardEvent event) {
		synchronized (listeners) {
			for (JVGClipboardListener listener : listeners) {
				switch (event.getID()) {
					case JVGClipboardEvent.DATA_ADDED:
						listener.dataAddedToClipboard(event);
						break;

					case JVGClipboardEvent.DATA_REMOVED:
						listener.dataRemovedFromClipboard(event);
						break;

					case JVGClipboardEvent.DATA_SAVED:
						listener.dataSaved(event);
						break;
				}
			}
		}
	}
}
