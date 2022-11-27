package ru.nest.jvg.actionarea;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.conn.Connection;
import ru.nest.jvg.conn.ConnectionManager;
import ru.nest.jvg.conn.Position;
import ru.nest.jvg.event.JVGEvent;
import ru.nest.jvg.event.JVGListener;
import ru.nest.jvg.event.JVGMouseEvent;
import ru.nest.jvg.event.JVGPeerEvent;
import ru.nest.jvg.event.JVGPeerListener;

public abstract class JVGAbstractConnectionActionArea extends JVGActionArea implements Connection, JVGPeerListener, JVGListener {
	public final static int CLIENT = 1;

	public final static int SERVER = 2;

	private final static Color connectionColor = new Color(100, 0, 0, 100);

	private int type;

	public JVGAbstractConnectionActionArea(int type) {
		super(VISIBILITY_TYPE_ALWAYS);
		this.type = type;
		setName("connection");
		addPeerListener(this);
	}

	public boolean isClient() {
		return (type & CLIENT) != 0;
	}

	public boolean isServer() {
		return (type & SERVER) != 0;
	}

	@Override
	protected boolean isDrawAction() {
		return isDrawConnection;
	}

	@Override
	public void paintAction(Graphics2D g, AffineTransform transform) {
		int count = getPositionsCount();
		if (count > 0) {
			double sizex = 8 / transform.getScaleX();
			double sizey = 8 / transform.getScaleY();

			Stroke oldStroke = g.getStroke();
			Stroke connectionStroke = new BasicStroke(2f);
			g.setStroke(connectionStroke);
			g.setColor(connectionColor);
			for (int i = 0; i < count; i++) {
				Position pos = getPosition(i);
				if (pos != null) {
					double x = pos.getServerX();
					double y = pos.getServerY();
					g.draw(transform.createTransformedShape(new Rectangle2D.Double(x - sizex / 2, y - sizey / 2, sizex, sizey)));
				}
			}
			g.setStroke(oldStroke);
		}
	}

	public abstract int getPositionsCount();

	public abstract Position getPosition(int index);

	@Override
	public void connectedToPeer(JVGPeerEvent e) {
		if (!ConnectionManager.isRegistered(this)) {
			ConnectionManager.register(this);
		}

		if (e.getOldPeer() != null) {
			e.getOldPeer().removeJVGListener(this);
		}
		e.getNewPeer().addJVGListener(this);
	}

	@Override
	public void addNotify() {
		super.addNotify();
	}

	@Override
	public void disconnectedFromPeer(JVGPeerEvent e) {
	}

	protected boolean isDrawConnection = false;

	@Override
	public void eventOccurred(JVGEvent e) {
		if (e.getID() == JVGMouseEvent.MOUSE_PRESSED) {
			isDrawConnection = e.getSource().getPane().isConnectionsEnabled();
		} else if (e.getID() == JVGMouseEvent.MOUSE_RELEASED) {
			isDrawConnection = false;
		}
	}

	@Override
	public void removeNotify() {
		ConnectionManager.unregister(this);
		if (getPane() != null) {
			getPane().removeJVGListener(this);
		}
		super.removeNotify();
	}

	@Override
	public JVGComponent getSource() {
		return getParent();
	}
}
