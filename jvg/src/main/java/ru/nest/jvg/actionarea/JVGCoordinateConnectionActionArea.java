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
import ru.nest.jvg.event.JVGComponentEvent;
import ru.nest.jvg.geom.coord.Coordinable;
import ru.nest.jvg.shape.JVGShape;

public class JVGCoordinateConnectionActionArea extends JVGAbstractConnectionActionArea implements ConnectionServer, ConnectionClient, Position {
	private Coordinable x;

	private Coordinable y;

	public JVGCoordinateConnectionActionArea(int type, Coordinable x, Coordinable y) {
		super(type);
		setName("coordinate-connection");
		this.x = x;
		this.y = y;
	}

	@Override
	public Shape computeActionBounds() {
		double[] point = getPoint();
		return new Rectangle2D.Double(point[0] - 2, point[1] - 2, 4, 4);
	}

	@Override
	protected boolean isDrawAction() {
		return isDrawConnection && (isServer() || (isClient() && getParent().isSelected()));
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

			Line2D line = new Line2D.Double();
			if (isClient) {
				g.setColor(Color.green);

				line.setLine(cx - dx, cy, cx + dx, cy);
				g.draw(line);

				line.setLine(cx, cy - dy, cx, cy + dy);
				g.draw(line);
			}
			if (isServer) {
				g.setColor(Color.blue);

				line.setLine(cx - dx, cy - dy, cx + dx, cy + dy);
				g.draw(line);

				line.setLine(cx + dx, cy - dy, cx - dx, cy + dy);
				g.draw(line);
			}
			g.setPaintMode();
		}
	}

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

	private double[] getPoint() {
		double[] point = { x.getCoord(), y.getCoord() };
		JVGShape parent = (JVGShape) getParent();
		parent.getTransform().transform(point, 0, point, 0, 1);
		return point;
	}

	@Override
	public boolean connect(ConnectionServer server) {
		pos = null;
		if (isClient() && server.getSource() != getParent()) {
			double[] point = getPoint();
			pos = server.accept(this, point[0], point[1]);
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
				double sx = pos.getServerX();
				double sy = pos.getServerY();
				double[] point = getPoint();
				if (point[0] != sx || point[1] != sy) {
					setCoord((JVGShape) parent, sx, sy);
					parent.invalidate();
					parent.repaint();
				}
			}
		}
	}

	private void setCoord(JVGShape parent, double sx, double sy) {
		double[] point = { sx, sy };
		parent.getInverseTransform().transform(point, 0, point, 0, 1);
		x.setCoord(point[0]);
		y.setCoord(point[1]);

		JVGComponentEvent event = new JVGComponentEvent(parent, JVGComponentEvent.COMPONENT_GEOMETRY_CHANGED);
		parent.dispatchEvent(event);
	}

	@Override
	public double getServerX() {
		double[] point = getPoint();
		return point[0];
	}

	@Override
	public double getServerY() {
		double[] point = getPoint();
		return point[1];
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
