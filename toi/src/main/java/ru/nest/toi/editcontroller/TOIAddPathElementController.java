package ru.nest.toi.editcontroller;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import ru.nest.toi.PathElementPoint;
import ru.nest.toi.TOIController;
import ru.nest.toi.TOIEditor.TOIEditPane;
import ru.nest.toi.TOIObject;
import ru.nest.toi.TOIPane;
import ru.nest.toi.objects.TOIArrowPathElement;

public class TOIAddPathElementController implements TOIController {
	private TOIPane pane;

	private boolean pressed;

	private PathElementPoint closestPoint;

	private TOIArrowPathElement overEndingElement;

	@Override
	public void processMouseEvent(MouseEvent e, double x, double y, double adjustX, double adjustY) {
		if (e.getID() == MouseEvent.MOUSE_PRESSED) {
			if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
				pressed = true;
				if (overEndingElement != null) {
					pane.repaint();
				} else if (closestPoint != null) {
					TOIArrowPathElement closestEndingElement = pane.getPathElementEndingAt(x, y);
					if (closestEndingElement == null) {
						overEndingElement = closestPoint.element.getParent().addElement(closestPoint.percentPos);
						closestPoint = null;
						pane.repaint();
					}
				}
				pane.requestFocus();
			}
		} else if (e.getID() == MouseEvent.MOUSE_RELEASED) {
			if (e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() == 1) {
				showPopupMenu((int) x, (int) y);
			}

			if (pressed) {
				pressed = false;
				pane.repaint();
			}
		} else if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
			if (overEndingElement != null && pressed) {
				if (!overEndingElement.isLast()) {
					PathElementPoint closestPoint = pane.findClosestElementPoint(x, y, Double.MAX_VALUE);
					overEndingElement.setPercentPos(closestPoint.percentPos);
					pane.repaint();
				}
			}
		} else if (e.getID() == MouseEvent.MOUSE_MOVED) {
			overEndingElement = pane.getPathElementEndingAt(x, y);
			if (overEndingElement != null) {
				pane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			} else {
				closestPoint = pane.findClosestElementPoint(x, y, 6);
				if (closestPoint != null) {
					pane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				} else {
					pane.setCursor(Cursor.getDefaultCursor());
				}
			}
			pane.repaint();
		}
	}

	private void showPopupMenu(int x, int y) {
		final PathElementPoint point = pane.findClosestElementPoint(x, y, 6);
		if (point != null) {
			JPopupMenu popup = new JPopupMenu();

			JMenuItem menuDeletePathElement = new JMenuItem("Удалить");
			menuDeletePathElement.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					point.element.getParent().removeElement(point.element);
					pane.repaint();
				}
			});
			menuDeletePathElement.setEnabled(!point.element.isLast());
			popup.add(menuDeletePathElement);

			if (pane instanceof TOIEditPane) {
				TOIEditPane editPane = (TOIEditPane) pane;
				Point2D documentLocation = editPane.getDocumentLocation();
				x += documentLocation.getX();
				y += documentLocation.getY();
			}
			popup.show(pane, x, y);
		}
	}

	@Override
	public void processKeyEvent(KeyEvent e) {
		if (e.getID() == KeyEvent.KEY_PRESSED) {
		}
	}

	@Override
	public void paint(Graphics2D g, Graphics2D gt, TOIObject o, int type) {
		if (type == PAINT_TOP) {
			if (overEndingElement != null) {
			} else if (closestPoint != null) {
				int x = (int) closestPoint.point.getX();
				int y = (int) closestPoint.point.getY();
				gt.setXORMode(Color.black);
				gt.setColor(Color.white);
				gt.drawLine(x - 5, y - 5, x + 5, y + 5);
				gt.drawLine(x + 5, y - 5, x - 5, y + 5);
				gt.setPaintMode();
			}
		}
	}

	@Override
	public void install(TOIPane pane) {
		this.pane = pane;
	}

	@Override
	public void uninstall() {
	}
}
