package ru.nest.swing.menu;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class Test extends JFrame {
	public Test() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 600);

		JMenuBar bar = new JMenuBar();
		setJMenuBar(bar);

		WBarMenu menu = new WBarMenu("File");
		bar.add(menu);

		WMenuItem mi = new WMenuItem("Open");
		mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.ALT_MASK));
		menu.add(mi);

		WMenu m = new WMenu("Save"); // , new
		// ImageIcon("C:/7. Progs/Icons/ICON 2/Help16.gif"));
		m.add(new WMenuItem("Data")); // , new
		// ImageIcon("C:/7. Progs/Icons/ICON 2/AllTopics[1].gif")));
		m.add(new WMenuItem("Image")); // , new
		// ImageIcon("C:/7. Progs/Icons/ICON 2/attachment[1].gif")));
		menu.add(m);

		menu.add(new WSeparator());

		mi = new WMenuItem("Exit");
		mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.ALT_MASK));
		mi.setEnabled(false);
		menu.add(mi);

		menu = new WBarMenu("Options");
		bar.add(menu);

		menu = new WBarMenu("Help");
		bar.add(menu);
	}

	public static void main(String[] args) {
		new Test().setVisible(true);
	}
}
