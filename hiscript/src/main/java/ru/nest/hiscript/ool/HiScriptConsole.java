package ru.nest.hiscript.ool;

import javax.swing.*;
import java.awt.*;

public class HiScriptConsole extends JFrame {
	public HiScriptConsole() {
		JTextArea textArea = new JTextArea();
		JTextArea enterText = new JTextArea();

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, textArea, enterText);
		splitPane.setDividerLocation(200);
		getContentPane().add(splitPane, BorderLayout.CENTER);
	}

	public static void main(String[] args) {
		HiScriptConsole frame = new HiScriptConsole();
		frame.setTitle("HiScript Console");
		frame.setBounds(100, 100, 600, 400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
