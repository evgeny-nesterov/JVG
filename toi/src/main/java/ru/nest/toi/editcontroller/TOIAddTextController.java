package ru.nest.toi.editcontroller;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

import ru.nest.toi.TOIController;
import ru.nest.toi.TOIObject;
import ru.nest.toi.TOIPane;
import ru.nest.toi.objects.TOIText;

public class TOIAddTextController implements TOIController {
	private TOIPane pane;

	public TOIAddTextController() {
	}

	@Override
	public void processMouseEvent(MouseEvent e, double x, double y, double adjustX, double adjustY) {
		if (e.getID() == MouseEvent.MOUSE_PRESSED) {
			if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
				JTextField txt = new JTextField();
				int option = JOptionPane.showConfirmDialog(pane, txt, "Текст", JOptionPane.PLAIN_MESSAGE);
				if (option == JOptionPane.OK_OPTION && txt.getText().trim().length() > 0) {
					TOIText t = pane.getFactory().create(TOIText.class);
					t.setText(txt.getText().trim());
					t.translate(x, y);
					pane.addObject(t);
					pane.requestFocus();
				}
			}
		}
	}

	@Override
	public void processKeyEvent(KeyEvent e) {
	}

	@Override
	public void paint(Graphics2D g, Graphics2D gt, TOIObject o, int type) {
	}

	@Override
	public void install(TOIPane pane) {
		this.pane = pane;
		pane.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
	}

	@Override
	public void uninstall() {
	}
}
