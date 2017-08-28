package satis.toi.editcontroller;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import satis.toi.TOIController;
import satis.toi.TOIObject;
import satis.toi.TOIPane;
import satis.toi.objects.TOIImage;

public class TOIAddImageController implements TOIController {
	private TOIPane pane;

	public TOIAddImageController() {
	}

	@Override
	public void processMouseEvent(MouseEvent e, double x, double y, double adjustX, double adjustY) {
		if (e.getID() == MouseEvent.MOUSE_PRESSED) {
			if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
				File file = pane.chooseImage();
				if (file != null) {
					try {
						ByteArrayOutputStream bos = new ByteArrayOutputStream();

						FileInputStream in = new FileInputStream(file);
						byte buffer[] = new byte[1024];
						int count;
						while ((count = in.read(buffer)) != -1) {
							if (count > 0) {
								bos.write(buffer, 0, count);
							}
						}
						in.close();

						byte[] data = bos.toByteArray();
						TOIImage image = pane.getFactory().create(TOIImage.class);
						image.setData(data, file.getName());
						image.translate(x - image.getIcon().getIconWidth() / 2, y - image.getIcon().getIconHeight() / 2);
						pane.addObject(image);
						pane.requestFocus();
					} catch (Exception exc) {
						exc.printStackTrace();
					}
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
