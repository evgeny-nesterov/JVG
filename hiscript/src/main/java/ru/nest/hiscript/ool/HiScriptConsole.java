package ru.nest.hiscript.ool;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.model.validation.HiScriptValidationException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.OutputStream;
import java.io.PrintStream;

public class HiScriptConsole extends JFrame {
	HiScript script = HiScript.create();

	public HiScriptConsole() {
		JTextArea textArea = new JTextArea();
		textArea.setFont(new Font("monospaced", Font.PLAIN, 10));
		textArea.setEditable(false);
		textArea.setBorder(null);

		JTextArea enterText = new JTextArea();
		enterText.setFont(new Font("monospaced", Font.PLAIN, 12));
		enterText.setBorder(null);

		JTextArea logsText = new JTextArea();
		logsText.setFont(new Font("monospaced", Font.PLAIN, 10));
		logsText.setEditable(false);
		logsText.setBorder(null);

		enterText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.isShiftDown() && e.getKeyCode() == KeyEvent.VK_ENTER) {
					try {
						String text = enterText.getText();
						script.compile(text).execute();
						if (!script.hasValidationException() && !script.hasRuntimeException()) {
							textArea.append(text + "\n");
							enterText.setText("");
						} else if (script.hasRuntimeException()) {
							System.err.print("[runtime] ");
							script.printError();
						}
					} catch (HiScriptParseException exc) {
					} catch (HiScriptRuntimeException exc) {
					} catch (HiScriptValidationException exc) {
					} catch (Throwable exc) {
						exc.printStackTrace();
					}
				}
			}
		});

		JScrollPane textAreaScroll = new JScrollPane(textArea);
		textAreaScroll.setBorder(null);

		JScrollPane enterTextScroll = new JScrollPane(enterText);
		enterTextScroll.setBorder(null);

		JScrollPane logsTextScroll = new JScrollPane(logsText);
		logsTextScroll.setBorder(null);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, textAreaScroll, enterTextScroll);
		splitPane.setBorder(null);

		JSplitPane rightSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, splitPane, logsTextScroll);
		rightSplitPane.setBorder(null);
		getContentPane().add(rightSplitPane, BorderLayout.CENTER);

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				splitPane.setDividerLocation(0.7);
				rightSplitPane.setDividerLocation(0.6);

				PrintStream printStream = new PrintStream(new OutputStream() {
					@Override
					public void write(int b) {
						logsText.append(String.valueOf((char) b));
						logsText.setCaretPosition(logsText.getDocument().getLength());
					}
				});
				System.setOut(printStream);
				System.setErr(printStream);
			}
		});
	}

	public static void main(String[] args) {
		HiScriptConsole frame = new HiScriptConsole();
		frame.setTitle("HiScript Console");
		frame.setBounds(20, 20, 1400, 830);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);
	}
}
