package ru.nest.jvg.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JViewport;

import ru.nest.jvg.JVGPane;

public class ZoomPreviewPane extends JPanel {
	private JVGEditPane pane;

	private PreviewPane previewPane;

	private Point2D viewPosition = null;

	public ZoomPreviewPane() {
		previewPane = new PreviewPane();
		setLayout(new BorderLayout());
		add(previewPane, BorderLayout.CENTER);
	}

	public void setPane(JVGEditPane pane) {
		if (this.pane != pane) {
			this.pane = pane;
			viewPosition = null;
			previewPane.repaint();
		}
	}

	public JVGPane getPane() {
		return pane;
	}

	private boolean updateViewPosition = false;

	public void update() {
		previewPane.repaint();
		updateViewPosition = true;
	}

	class PreviewPane extends JLabel {
		public final static int NONE = 0;

		public final static int LEFT = 1;

		public final static int RIGHT = 2;

		public final static int TOP = 4;

		public final static int BOTTOM = 8;

		public final static int TOP_LEFT = TOP | LEFT;

		public final static int LEFT_BOTTOM = LEFT | BOTTOM;

		public final static int BOTTOM_RIGHT = BOTTOM | RIGHT;

		public final static int RIGHT_TOP = RIGHT | TOP;

		public final static int CENTER = 16;

		private double mx;

		private double my;

		private boolean pressed = false;

		private int dragType = NONE;

		public PreviewPane() {
			setOpaque(false);
			addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					if (pane == null) {
						return;
					}

					if (e.getButton() == MouseEvent.BUTTON1) {
						pressed = true;
						mx = e.getX();
						my = e.getY();
						if (updateViewPosition) {
							viewPosition = null;
						}
						dragType = getDragType(mx, my);

						if (dragType == NONE) {
							Rectangle2D.Double r = getCenter(mx, my);
							if (r != null) {
								setVisibleBounds(r);
								repaint();

								setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
								dragType = CENTER;
							}
						}
					}
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					pressed = false;
				}
			});

			addMouseMotionListener(new MouseMotionAdapter() {
				@Override
				public void mouseMoved(MouseEvent e) {
					if (pane == null) {
						return;
					}

					int dragType = getDragType(e.getX(), e.getY());
					switch (dragType) {
						case NONE:
							setCursor(Cursor.getDefaultCursor());
							break;

						case CENTER:
							setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
							break;

						case LEFT:
						case RIGHT:
							setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
							break;

						case TOP:
						case BOTTOM:
							setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
							break;

						case TOP_LEFT:
						case BOTTOM_RIGHT:
							setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
							break;

						case RIGHT_TOP:
						case LEFT_BOTTOM:
							setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
							break;
					}
				}

				@Override
				public void mouseDragged(MouseEvent e) {
					if (pane == null) {
						return;
					}

					if (pressed) {
						double dx = e.getX() - mx;
						double dy = e.getY() - my;
						if (dx != 0 || dy != 0) {
							Rectangle2D.Double r = getVisibleBounds(e.getX(), e.getY(), dx, dy, dragType);
							if (r != null) {
								setVisibleBounds(r);
								repaint();
							}
						}
					}
				}
			});
		}

		public void setVisibleBounds(Rectangle2D.Double r) {
			double w = getWidth();
			double h = getHeight();
			Rectangle vr = pane.getVisibleRect();

			Dimension documentSize = pane.getDocumentSize();
			double dx = 0.2 * documentSize.width;
			double dy = 0.2 * documentSize.height;
			double dw = documentSize.width + 2 * dx;
			double dh = documentSize.height + 2 * dy;

			double scale = Math.min(w / dw, h / dh);

			Insets di = pane.getDocumentInsets();

			double zoom = scale * vr.width / r.width;
			double paneX = zoom * ((r.x - (w - scale * documentSize.width) / 2.0) / scale + di.left);
			double paneY = zoom * ((r.y - (h - scale * documentSize.height) / 2.0) / scale + di.top);

			pane.setZoom(zoom);

			viewPosition = new Point2D.Double(paneX, paneY);
			((JViewport) pane.getParent()).setViewPosition(new Point((int) paneX, (int) paneY));
		}

		private double checkZoom(double zoom) {
			if (zoom > 0 && zoom < JVGPane.MIN_ZOOM) {
				zoom = JVGPane.MIN_ZOOM;
			} else if (zoom < 0 || zoom > JVGPane.MAX_ZOOM) {
				zoom = JVGPane.MAX_ZOOM;
			}
			return zoom;
		}

		private Rectangle2D.Double getCenter(double mx, double my) {
			Rectangle2D.Double r = getVisibleBounds(false);
			r.x = mx - r.width / 2.0;
			r.y = my - r.height / 2.0;
			checkBounds(r);
			return r;
		}

		private void checkBounds(Rectangle2D.Double r) {
			if (r.x < 0) {
				r.x = 0;
			}
			if (r.x + r.width > getWidth()) {
				if (r.width > getWidth()) {
					r.width = getWidth();
				}
				r.x = getWidth() - r.width;
			}

			if (r.y < 0) {
				r.y = 0;
			}
			if (r.y + r.height > getHeight()) {
				if (r.height > getHeight()) {
					r.height = getHeight();
				}
				r.y = getHeight() - r.height;
			}
		}

		private Rectangle2D.Double getVisibleBounds(double mx, double my, double dmx, double dmy, int dragType) {
			Rectangle2D.Double r = getVisibleBounds(false);
			Rectangle vr = pane.getVisibleRect();

			double odx = dmx;
			double ody = dmy;
			double dx = dmx;
			double dy = dmy;

			double x = r.x;
			double y = r.y;
			double w = r.width;
			double h = r.height;
			double x2 = x + w;
			double y2 = y + h;

			boolean updated = false;
			double scale = getScale();
			double oldZoom = pane.getZoom();

			switch (dragType) {
				case CENTER:
					x += dmx;
					y += dmy;
					updated = true;
					break;

				case LEFT:
					dx = mx - x;
					double zoom = checkZoom(scale * vr.width / (w - dx));
					if (zoom != oldZoom) {
						dx = w - scale * vr.width / zoom;
						h *= (w - dmx) / w;
						x += dx;
						w -= dx;
						updated = true;
					}
					break;

				case RIGHT:
					dx = mx - x2;
					zoom = checkZoom(scale * vr.width / (w + dx));
					if (zoom != oldZoom) {
						dx = scale * vr.width / zoom - w;
						h *= (w + dx) / w;
						w += dx;
						updated = true;
					}
					break;

				case TOP:
					dy = my - y;
					zoom = checkZoom(scale * vr.height / (h - dy));
					if (zoom != oldZoom) {
						dy = h - scale * vr.height / zoom;
						w *= (h - dy) / h;
						y += dy;
						h -= dy;
						updated = true;
					}
					break;

				case BOTTOM:
					dy = my - y2;
					zoom = checkZoom(scale * vr.height / (h + dy));
					if (zoom != oldZoom) {
						dy = scale * vr.height / zoom - h;
						w *= (h + dy) / h;
						h += dy;
						updated = true;
					}
					break;

				case TOP_LEFT:
					dx = mx - x;
					dy = my - y;
					if ((w - dx) / w > (h - dy) / h) {
						zoom = checkZoom(scale * vr.width / (w - dx));
						if (zoom != oldZoom) {
							dx = w - scale * vr.width / zoom;
							h *= (w - dx) / w;
							y = y2 - h;
							w -= dx;
							x = x2 - w;
							updated = true;
						}
					} else {
						zoom = checkZoom(scale * vr.height / (h - dy));
						if (zoom != oldZoom) {
							dy = h - scale * vr.height / zoom;
							w *= (h - dy) / h;
							x = x2 - w;
							h -= dy;
							y = y2 - h;
							updated = true;
						}
					}
					break;

				case LEFT_BOTTOM:
					dx = mx - x;
					dy = my - y2;
					if ((w - dx) / w > (h + dy) / h) {
						zoom = checkZoom(scale * vr.width / (w - dx));
						if (zoom != oldZoom) {
							dx = w - scale * vr.width / zoom;
							h *= (w - dx) / w;
							w -= dx;
							x = x2 - w;
							updated = true;
						}
					} else {
						zoom = checkZoom(scale * vr.height / (h + dy));
						if (zoom != oldZoom) {
							dy = scale * vr.height / zoom - h;
							w *= (h + dy) / h;
							x = x2 - w;
							h += dy;
							updated = true;
						}
					}
					break;

				case BOTTOM_RIGHT:
					dx = mx - x2;
					dy = my - y2;
					if ((w + dx) / w > (h + dy) / h) {
						zoom = checkZoom(scale * vr.width / (w + dx));
						if (zoom != oldZoom) {
							dx = scale * vr.width / zoom - w;
							h *= (w + dx) / w;
							w += dx;
							updated = true;
						}
					} else {
						zoom = checkZoom(scale * vr.height / (h + dy));
						if (zoom != oldZoom) {
							dy = scale * vr.height / zoom - h;
							w *= (h + dy) / h;
							h += dy;
							updated = true;
						}
					}
					break;

				case RIGHT_TOP:
					dx = mx - x2;
					dy = my - y;
					if ((w + dx) / w > (h - dy) / h) {
						zoom = checkZoom(scale * vr.width / (w + dx));
						if (zoom != oldZoom) {
							dx = scale * vr.width / zoom - w;
							h *= (w + dx) / w;
							y = y2 - h;
							w += dx;
							updated = true;
						}
					} else {
						zoom = checkZoom(scale * vr.height / (h - dy));
						if (zoom != oldZoom) {
							dy = h - scale * vr.height / zoom;
							w *= (h - dy) / h;
							h -= dy;
							y = y2 - h;
							updated = true;
						}
					}
					break;
			}

			if (updated) {
				Rectangle2D.Double newVisibleBounds = new Rectangle2D.Double(x, y, w, h);
				//checkBounds(newVisibleBounds);
				this.mx = mx + odx - dx;
				this.my = my + ody - dy;

				if (newVisibleBounds.x != r.x || newVisibleBounds.y != r.y || newVisibleBounds.width != r.width || newVisibleBounds.height != r.height) {
					return newVisibleBounds;
				}
			}
			return null;
		}

		private Rectangle2D.Double getVisibleBounds(boolean calcPos) {
			double w = getWidth();
			double h = getHeight();

			Dimension documentSize = pane.getDocumentSize();
			double dx = 0.2 * documentSize.width;
			double dy = 0.2 * documentSize.height;
			double dw = documentSize.width + 2 * dx;
			double dh = documentSize.height + 2 * dy;

			double scale = Math.min(w / dw, h / dh);

			Rectangle r = pane.getVisibleRect();
			double paneX = !calcPos && viewPosition != null ? viewPosition.getX() : -pane.getX();
			double paneY = !calcPos && viewPosition != null ? viewPosition.getY() : -pane.getY();

			Insets di = pane.getDocumentInsets();
			double zoom = pane.getZoom();

			double rx = (paneX / zoom - di.left) * scale + (w - scale * documentSize.width) / 2.0;
			double ry = (paneY / zoom - di.top) * scale + (h - scale * documentSize.height) / 2.0;
			double rw = scale * r.width / zoom;
			double rh = scale * r.height / zoom;
			return new Rectangle2D.Double(rx, ry, rw, rh);
		}

		private int getDragType(double x, double y) {
			int dragType = NONE;

			Rectangle2D.Double r = getVisibleBounds(true);
			int size = 2;

			if (y > r.y - size && y < r.y + r.height + size) {
				if (x >= r.x - size && x <= r.x + size) {
					dragType |= LEFT;
				} else if (x >= r.x + r.width - size && x <= r.x + r.width + size) {
					dragType |= RIGHT;
				}
			}

			if (x > r.x - size && x < r.x + r.width + size) {
				if (y >= r.y - size && y <= r.y + size) {
					dragType |= TOP;
				} else if (y >= r.y + r.height - size && y <= r.y + r.height + size) {
					dragType |= BOTTOM;
				}
			}

			if (dragType == NONE) {
				if (x >= r.x && x <= r.x + r.width && y >= r.y && y <= r.y + r.height) {
					dragType = CENTER;
				}
			}
			return dragType;
		}

		private double getScale() {
			int w = getWidth();
			int h = getHeight();

			Dimension documentSize = pane.getDocumentSize();
			double dx = 0.2 * documentSize.width;
			double dy = 0.2 * documentSize.height;
			double dw = documentSize.width + 2 * dx;
			double dh = documentSize.height + 2 * dy;

			double scale = Math.min(w / dw, h / dh);
			return scale;
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);

			Graphics2D g2d = (Graphics2D) g;
			int w = getWidth();
			int h = getHeight();

			g2d.setColor(Color.gray);
			g2d.fillRect(0, 0, w, h);

			if (pane != null) {
				Dimension documentSize = pane.getDocumentSize();
				double scale = getScale();

				g2d.scale(scale, scale);
				g2d.translate((w / scale - documentSize.width) / 2.0, (h / scale - documentSize.height) / 2.0);

				g.setColor(Color.white);
				g.fillRect(0, 0, documentSize.width, documentSize.height);

				pane.print(g2d, false);

				g2d.translate(-(w / scale - documentSize.width) / 2.0, -(h / scale - documentSize.height) / 2.0);
				g2d.scale(1 / scale, 1 / scale);

				// draw visible area
				Rectangle2D.Double r = getVisibleBounds(true);
				int rx = (int) r.x;
				int ry = (int) r.y;
				int rw = (int) r.width;
				int rh = (int) r.height;
				int rx2 = (rx + rw);
				int ry2 = (ry + rh);

				g2d.setColor(Color.red);
				int delta = 2;
				int size = 2 * delta + 1;
				int lineDelta = 3 + delta;
				int delta2 = delta - 1;
				int size2 = 2 * delta2 + 1;

				g2d.fillRect(rx - delta, ry - delta, size, size);
				g2d.fillRect(rx - delta, ry2 - delta, size, size);
				g2d.fillRect(rx2 - delta, ry - delta, size, size);
				g2d.fillRect(rx2 - delta, ry2 - delta, size, size);

				g2d.fillRect(rx + lineDelta, ry - delta2, rw - 2 * lineDelta + 1, size2);
				g2d.fillRect(rx + lineDelta, ry2 - delta2, rw - 2 * lineDelta + 1, size2);
				g2d.fillRect(rx - delta2, ry + lineDelta, size2, rh - 2 * lineDelta + 1);
				g2d.fillRect(rx2 - delta2, ry + lineDelta, size2, rh - 2 * lineDelta + 1);
			}
		}
	}
}
