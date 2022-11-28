package ru.nest.toi.editcontroller;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

import ru.nest.toi.TOIController;
import ru.nest.toi.TOIObject;
import ru.nest.toi.TOIPane;
import ru.nest.toi.objects.TOIArrowPathElement;
import ru.nest.toi.objects.TOIMultiArrowPath;
import ru.nest.toi.objects.TOIPath;
import ru.nest.toi.objects.TOITextPath;

public class TOIAddPathController implements TOIController {
	public final static int LINE = 0;

	public final static int QUAD = 1;

	public final static int CUBIC = 2;

	private TOIPane pane;

	private double x1, y1, x2, y2;

	private boolean pressed;

	private int curveType = LINE;

	private Stroke stroke = new BasicStroke(14, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);

	private Class<? extends TOIPath> pathClass;

	public <T extends TOIPath> TOIAddPathController(Class<T> pathClass, int curveType) {
		this.pathClass = pathClass;
		this.curveType = curveType;
	}

	@Override
	public void processMouseEvent(MouseEvent e, double x, double y, double adjustX, double adjustY) {
		if (e.getID() == MouseEvent.MOUSE_PRESSED) {
			if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
				if (pane.getFocusedObject() instanceof TOIPath && pane.getFocusedObject().getClass() == pathClass) {
					TOIPath path = (TOIPath) pane.getFocusedObject();
					x1 = path.getPath().doubleCoords[path.getPath().numCoords - 2];
					y1 = path.getPath().doubleCoords[path.getPath().numCoords - 1];
					x2 = x;
					y2 = y;
				} else {
					x1 = x2 = x;
					y1 = y2 = y;
				}
				pressed = true;
				pane.repaint();
				pane.requestFocus();
			}
		} else if (e.getID() == MouseEvent.MOUSE_RELEASED) {
			if (pressed) {
				TOIPath path = null;
				if (pane.getFocusedObject() == null || pane.getFocusedObject().getClass() != pathClass) {
					path = pane.getFactory().create(pathClass);
					path.getPath().moveTo(x1, y1);

					if (path instanceof TOIMultiArrowPath) {
						TOIMultiArrowPath arr = (TOIMultiArrowPath) path;
						arr.addElement(1);
					} else if (path instanceof TOIArrowPathElement) {
						// Do nothing
					} else if (path instanceof TOITextPath) {
						JTextField txt = new JTextField();
						int option = JOptionPane.showConfirmDialog(pane, txt, "Текст", JOptionPane.PLAIN_MESSAGE);
						if (option == JOptionPane.OK_OPTION && txt.getText().trim().length() > 0) {
							((TOITextPath) path).setText(txt.getText().trim());
						} else {
							pressed = false;
							pane.repaint();
							return;
						}
					}

					pane.addObject(path);
					pane.setFocusedObject(path);
				} else {
					path = (TOIPath) pane.getFocusedObject();
				}

				if (!(x1 == x2 && y1 == y2)) {
					if (curveType == LINE) {
						path.getPath().lineTo(x2, y2);
						path.invalidate();
					} else if (curveType == QUAD) {
						path.getPath().quadTo((x1 + x2) / 2, (y1 + y2) / 2, x2, y2);
						path.invalidate();
					} else if (curveType == CUBIC) {
						path.getPath().curveTo(x1 + (x2 - x1) / 3, y1 + (y2 - y1) / 3, x1 + (x2 - x1) * 2 / 3, y1 + (y2 - y1) * 2 / 3, x2, y2);
						path.invalidate();
					}
				}

				pressed = false;
				pane.repaint();
			}
		} else if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
			if (pressed) {
				x2 = x;
				y2 = y;
				pane.repaint();
			}
		} else if (e.getID() == MouseEvent.MOUSE_MOVED) {
		}
	}

	@Override
	public void processKeyEvent(KeyEvent e) {
		if (e.getID() == KeyEvent.KEY_PRESSED) {
			if (e.getKeyCode() == KeyEvent.VK_Z) {
				if (e.isControlDown()) {
					if (pane.getFocusedObject() instanceof TOIPath) {
						TOIPath path = (TOIPath) pane.getFocusedObject();
						path.deleteLastSegment();
						pane.repaint();
					}
				}
			}
		}
	}

	@Override
	public void paint(Graphics2D g, Graphics2D gt, TOIObject o, int type) {
		if (type == PAINT_TOP) {
			if (pressed) {
				Stroke oldStroke = gt.getStroke();
				gt.setStroke(stroke);
				gt.setColor(new Color(200, 200, 200, 200));
				gt.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
				gt.setStroke(oldStroke);
			}
		}
	}

	@Override
	public void install(TOIPane pane) {
		this.pane = pane;
		pane.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
	}

	@Override
	public void uninstall() {
	}

	public int getCurveType() {
		return curveType;
	}

	public void setCurveType(int curveType) {
		this.curveType = curveType;
	}
}
