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
		JTextArea enterText = new JTextArea();
		JTextArea logsText = new JTextArea();
		enterText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.isShiftDown() && e.getKeyCode() == KeyEvent.VK_ENTER) {
					try {
						String text = enterText.getText();
						script.compile(text).execute();
						if (!script.hasRuntimeException()) {
							textArea.append(text + "\n\n");
							enterText.setText("");
						} else {
							System.err.print("[runtime] ");
							script.printError();
						}
					} catch (HiScriptParseException exc) {
						System.err.println("[parse] " + exc.getMessage());
					} catch (HiScriptRuntimeException exc) {
					} catch (HiScriptValidationException exc) {
					} catch (Throwable exc) {
						exc.printStackTrace();
					}
				}
			}
		});

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(textArea), new JScrollPane(enterText));

		JSplitPane rightSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, splitPane, new JScrollPane(logsText));
		getContentPane().add(rightSplitPane, BorderLayout.CENTER);

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				splitPane.setDividerLocation(getHeight() - 200);
				rightSplitPane.setDividerLocation(0.5);

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
