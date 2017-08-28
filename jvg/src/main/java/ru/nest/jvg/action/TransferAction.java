package ru.nest.jvg.action;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JTextField;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.plaf.UIResource;

import ru.nest.jvg.JVGPane;
import ru.nest.jvg.macros.JVGMacrosCode;

public class TransferAction extends JVGAction implements UIResource {
	public TransferAction(String name) {
		super(name);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JVGPane pane = getPane(e);
		String name = (String) getValue(Action.NAME);
		transfer(pane, name);

		if ("cut".equals(name)) {
			appendMacrosCode(pane, "cut();", JVGMacrosCode.ARG_NONE);
		} else if ("copy".equals(name)) {
			appendMacrosCode(pane, "copy();", JVGMacrosCode.ARG_NONE);
		} else if ("paste".equals(name)) {
			appendMacrosCode(pane, "paste();", JVGMacrosCode.ARG_NONE);
		}
	}

	public static void transfer(JVGPane pane, String name) {
		TransferHandler th = pane.getTransferHandler();
		Clipboard clipboard = pane.getClipboard();
		Transferable trans = null;
		try {
			if ((clipboard != null) && (th != null) && (name != null)) {
				if ("cut".equals(name)) {
					JTextField txt = new JTextField("this is necessary to fire clipboard event");
					txt.selectAll();
					txt.copy();

					th.exportToClipboard(pane, clipboard, TransferHandler.MOVE);
				} else if ("copy".equals(name)) {
					JTextField txt = new JTextField("this is necessary to fire clipboard event");
					txt.selectAll();
					txt.copy();

					th.exportToClipboard(pane, clipboard, TransferHandler.COPY);
				} else if ("paste".equals(name)) {
					trans = clipboard.getContents(null);
				}
			}
		} catch (IllegalStateException exc) {
			UIManager.getLookAndFeel().provideErrorFeedback(pane);
			return;
		}

		if (trans != null) {
			th.importData(pane, trans);
		}
	}
}
