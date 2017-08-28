package ru.nest.jvg.actionarea;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.conn.ConnectionClient;
import ru.nest.jvg.conn.ConnectionServer;
import ru.nest.jvg.conn.Position;
import ru.nest.jvg.shape.JVGShape;

public class JVGFreeConnectionActionArea extends JVGAbstractConnectionActionArea implements ConnectionServer, ConnectionClient, Position {
	public JVGFreeConnectionActionArea(int type, float koefX, float koefY) {
		super(type);
		setName("free-connection");
		this.koefX = koefX;
		this.koefY = koefY;
	}

	@Override
	public Shape computeActionBounds() {
		Rectangle2D b = getParent().getRectangleBounds();
		double x = b.getX() + koefX * b.getWidth();
		double y = b.getY() + koefY * b.getHeight();
		return new Rectangle2D.Double(x - 2, y - 2, 4, 4);
	}

	@Override
	protected boolean isDrawAction() {
		return isDrawConnection && (isServer() || getParent().isSelected());
	}

	@Override
	public void paintAction(Graphics2D g, AffineTransform transform) {
		super.paintAction(g, transform);

		boolean isClient = isClient();
		boolean isServer = isServer();
		if (isClient || isServer) {
			Rectangle2D b = getTransformedBounds().getBounds2D();
			double dx = b.getWidth() / 2;
			double dy = b.getHeight() / 2;
			double cx = b.getX() + dx;
			double cy = b.getY() + dy;

			g.setXORMode(Color.white);
			g.setColor(Color.blue);

			Line2D line = new Line2D.Double();
			if (isClient) {
				line.setLine(cx - dx, cy, cx + dx, cy);
				g.draw(line);

				line.setLine(cx, cy - dy, cx, cy + dy);
				g.draw(line);
			}
			if (isServer) {
				line.setLine(cx - dx, cy - dy, cx + dx, cy + dy);
				g.draw(line);

				line.setLine(cx + dx, cy - dy, cx - dx, cy + dy);
				g.draw(line);
			}
			g.setPaintMode();
		}
	}

	private float koefY;

	private float koefX;

	@Override
	public Position accept(ConnectionClient client, double clientX, double clientY) {
		if (getSource() == client.getSource()) {
			return null;
		}

		if (isServer()) {
			Shape b = getBounds();
			if (b.contains(clientX, clientY)) {
				return this;
			}
		}
		return null;
	}

	@Override
	public boolean connect(ConnectionServer server) {
		if (isClient() && server.getSource() != getParent()) {
			Rectangle2D b = getParent().getRectangleBounds();
			double x = b.getX() + koefX * b.getWidth();
			double y = b.getY() + koefY * b.getHeight();
			pos = server.accept(this, x, y);
		}

		return pos != null;
	}

	@Override
	public void disconnect(ConnectionServer server) {
		pos = null;
	}

	@Override
	public boolean isConnected(ConnectionServer server) {
		return pos != null;
	}

	private Position pos;

	@Override
	public void adjust(ConnectionServer server) {
		if (pos != null) {
			JVGComponent parent = getParent();
			if (parent instanceof JVGShape) {
				JVGShape shape = (JVGShape) parent;

				Rectangle2D b = shape.getRectangleBounds();
				double x = b.getX() + koefX * b.getWidth();
				double y = b.getY() + koefY * b.getHeight();
				double dx = pos.getServerX() - x;
				double dy = pos.getServerY() - y;

				if (dx != 0 || dy != 0) {
					shape.transform(AffineTransform.getTranslateInstance(dx, dy));
				}
			}
		}
	}

	@Override
	public double getServerX() {
		Rectangle2D b = getParent().getRectangleBounds();
		return b.getX() + koefX * b.getWidth();
	}

	@Override
	public double getServerY() {
		Rectangle2D b = getParent().getRectangleBounds();
		return b.getY() + koefY * b.getHeight();
	}

	@Override
	public int getPositionsCount() {
		return pos != null ? 1 : 0;
	}

	@Override
	public Position getPosition(int index) {
		return pos;
	}
}
