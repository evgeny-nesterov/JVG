package ru.nest.jvg.actionarea;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.conn.ConnectionClient;
import ru.nest.jvg.conn.ConnectionServer;
import ru.nest.jvg.conn.Position;
import ru.nest.jvg.geom.CoordinablePathIterator;
import ru.nest.jvg.geom.MutableGeneralPath;
import ru.nest.jvg.shape.JVGPath;
import ru.nest.jvg.shape.JVGShape;

public class JVGPathConnectionActionArea extends JVGAbstractConnectionActionArea implements ConnectionClient, ConnectionServer {
	public JVGPathConnectionActionArea(int type) {
		super(type);
		setName("path-connection");
	}

	@Override
	protected Shape computeActionBounds() {
		return null;
	}

	@Override
	public boolean contains(double x, double y) {
		return false;
	}

	@Override
	protected boolean isDrawAction() {
		return isDrawConnection;
	}

	@Override
	public void paintAction(Graphics2D g, AffineTransform transform) {
		super.paintAction(g, transform);
		// Do not paint client as if shape has a lot of points when it will have a bad appearance.

		if (isServer()) {
			JVGComponent parent = getParent();
			if (parent instanceof JVGShape) {
				JVGShape shape = (JVGShape) parent;
				MutableGeneralPath path = shape.getTransformedShape();

				double scale = getPaneScale();
				double delta = 3 / scale;

				int coord = 0;
				for (int t = 0; t < path.numTypes; t++) {
					int type = path.pointTypes[t];
					int numcoords = CoordinablePathIterator.curvesize[type];
					if (numcoords > 0) {
						coord += numcoords - 2;

						double x = path.pointCoords[coord];
						double y = path.pointCoords[coord + 1];

						g.setColor(Color.red);
						g.draw(transform.createTransformedShape(new Line2D.Double(x - delta, y - delta, x + delta, y + delta)));
						g.draw(transform.createTransformedShape(new Line2D.Double(x - delta, y + delta, x + delta, y - delta)));

						coord += 2;
					}
				}
			}
		}
	}

	/**
	 * Connection implementation. Any shape may be a server.
	 */
	@Override
	public Position accept(ConnectionClient client, double clientX, double clientY) {
		if (!isServer() || getSource() == client.getSource()) {
			return null;
		}

		JVGComponent parent = getParent();
		if (parent instanceof JVGShape) {
			JVGShape shape = (JVGShape) parent;
			MutableGeneralPath path = shape.getTransformedShape();

			int coord = 0;
			for (int t = 0; t < path.numTypes; t++) {
				int type = path.pointTypes[t];
				int numcoords = CoordinablePathIterator.curvesize[type];
				if (numcoords > 0) {
					coord += numcoords - 2;

					double x = path.pointCoords[coord];
					double y = path.pointCoords[coord + 1];
					if (clientX >= x - 2 && clientX <= x + 2 && clientY >= y - 2 && clientY <= y + 2) {
						final int index = coord;
						return new Position() {
							@Override
							public double getServerX() {
								JVGShape shape = (JVGShape) getParent();
								MutableGeneralPath path = shape.getTransformedShape();
								return path.pointCoords[index];
							}

							@Override
							public double getServerY() {
								JVGShape shape = (JVGShape) getParent();
								MutableGeneralPath path = shape.getTransformedShape();
								return path.pointCoords[index + 1];
							}
						};
					}

					coord += 2;
				}
			}
		}

		return null;
	}

	/**
	 * Only JVGPath can be client by default
	 */
	@Override
	public boolean connect(ConnectionServer server) {
		if (!isClient()) {
			return false;
		}

		List<ConnectionContext> list = positions.get(server);
		if (list != null) {
			positionsCount -= list.size();
			list.clear();
		}

		JVGComponent parent = getParent();
		if (parent instanceof JVGShape) {
			JVGShape shape = (JVGShape) parent;
			MutableGeneralPath path = shape.getTransformedShape();

			int coord = 0;
			for (int t = 0; t < path.numTypes; t++) {
				int type = path.pointTypes[t];
				int numcoords = CoordinablePathIterator.curvesize[type];

				if (numcoords > 0) {
					coord += numcoords - 2; // move to last curve point
					double x = path.pointCoords[coord];
					double y = path.pointCoords[coord + 1];

					Position pos = server.accept(this, x, y);
					if (pos != null) {
						if (list == null) {
							list = new ArrayList<>();
							positions.put(server, list);
						}

						list.add(new ConnectionContext(coord, pos));
						positionsCount++;
					}

					coord += 2;
				}
			}
		}

		return list != null && list.size() > 0;
	}

	private Map<ConnectionServer, List<ConnectionContext>> positions = new HashMap<>();

	private int positionsCount = 0;

	@Override
	public boolean isConnected(ConnectionServer server) {
		List<ConnectionContext> list = positions.get(server);
		return list != null && list.size() > 0;
	}

	@Override
	public void disconnect(ConnectionServer server) {
		List<ConnectionContext> list = positions.get(server);
		if (list != null) {
			positionsCount -= list.size();
			list.clear();
		}
	}

	/**
	 * Implementation for JVGPath. For other shapes it's necassary to override this method according to shape changing policy.
	 */
	@Override
	public void adjust(ConnectionServer server) {
		List<ConnectionContext> list = positions.get(server);
		if (list != null && list.size() > 0) {
			JVGComponent parent = getParent();
			if (parent instanceof JVGPath) {
				JVGPath path = (JVGPath) parent;
				for (ConnectionContext ctx : list) {
					double[] point = { ctx.getPosition().getServerX(), ctx.getPosition().getServerY() };
					path.getInverseTransform().transform(point, 0, point, 0, 1);
					path.setPoint(ctx.getIndex(), point[0], point[1]);
				}
			}
		}
	}

	class ConnectionContext {
		public ConnectionContext(int index, Position pos) {
			this.index = index;
			this.pos = pos;
		}

		private int index;

		public int getIndex() {
			return index;
		}

		private Position pos;

		public Position getPosition() {
			return pos;
		}
	}

	@Override
	public int getPositionsCount() {
		return positionsCount;
	}

	@Override
	public Position getPosition(int index) {
		int i = 0;
		for (List<ConnectionContext> list : positions.values()) {
			if (index >= i && index < i + list.size()) {
				return list.get(index - i).getPosition();
			}
			i += list.size();
		}
		return null;
	}
}
