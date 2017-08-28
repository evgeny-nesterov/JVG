package javax.swing.dock;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

public class DockIcons {
	private static Color color = new Color(110, 110, 110);

	private static int w = 14;

	private static int h = 14;

	private static int w0 = 10;

	private static int h0 = 10;

	public static Icon closeIcon = new Icon() {
		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			int dx = x + (w - w0) / 2;
			int dy = y + (h - h0) / 2;
			g.setColor(color);
			g.translate(dx, dy);

			g.drawLine(1, 0, w0 - 1, h0 - 2);
			g.drawLine(1, 1, w0 - 2, h0 - 2);
			g.drawLine(0, 1, w0 - 2, h0 - 1);

			g.drawLine(1, h0 - 1, w0 - 1, 1);
			g.drawLine(1, h0 - 2, w0 - 2, 1);
			g.drawLine(0, h0 - 2, w0 - 2, 0);

			g.translate(-dx, -dy);
		}

		@Override
		public int getIconWidth() {
			return w;
		}

		@Override
		public int getIconHeight() {
			return h;
		}
	};

	public static Icon maximizeIcon = new Icon() {
		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			int dx = x + (w - w0) / 2;
			int dy = y + (h - h0) / 2;
			g.setColor(color);
			g.translate(dx, dy);

			g.drawRect(0, 0, w0 - 1, h0 - 1);
			g.drawLine(0, 1, w0 - 1, 1);

			g.translate(-dx, -dy);
		}

		@Override
		public int getIconWidth() {
			return w;
		}

		@Override
		public int getIconHeight() {
			return h;
		}
	};

	public static Icon normalizeIcon = new Icon() {
		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			int dx = x + (w - w0) / 2;
			int dy = y + (h - h0) / 2;
			g.setColor(color);
			g.translate(dx, dy);

			g.drawLine(2, 0, w0 - 1, 0);
			g.drawLine(2, 0, 2, 2);
			g.drawLine(w0 - 1, 0, w0 - 1, h0 - 3);
			g.drawLine(w0 - 3, h0 - 3, w0 - 1, h0 - 3);
			g.drawLine(2, 1, w0 - 1, 1);

			g.drawRect(0, 3, w0 - 3, h0 - 4);
			g.drawLine(0, 4, w0 - 3, 4);

			g.translate(-dx, -dy);
		}

		@Override
		public int getIconWidth() {
			return w;
		}

		@Override
		public int getIconHeight() {
			return h;
		}
	};

	public static Icon minimizeIcon = new Icon() {
		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			int dx = x + (w - w0) / 2;
			int dy = y + (h - h0) / 2;
			g.setColor(color);
			g.translate(dx, dy);

			g.drawLine(0, h0 - 2, w0 - 1, h0 - 2);
			g.drawLine(0, h0 - 1, w0 - 1, h0 - 1);

			g.translate(-dx, -dy);
		}

		@Override
		public int getIconWidth() {
			return w;
		}

		@Override
		public int getIconHeight() {
			return h;
		}
	};
}
