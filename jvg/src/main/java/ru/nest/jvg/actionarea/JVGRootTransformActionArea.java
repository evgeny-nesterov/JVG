package ru.nest.jvg.actionarea;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import ru.nest.jvg.JVGPane;
import ru.nest.jvg.JVGSelectionModel;
import ru.nest.jvg.event.JVGFocusEvent;
import ru.nest.jvg.event.JVGMouseEvent;
import ru.nest.jvg.event.JVGPeerEvent;
import ru.nest.jvg.event.JVGPeerListener;
import ru.nest.jvg.event.JVGSelectionEvent;

public abstract class JVGRootTransformActionArea extends JVGTransformActionArea implements JVGPeerListener {
	public JVGRootTransformActionArea() {
		super(VISIBILITY_TYPE_CUSTOM, true);
		addPeerListener(this);
	}

	public void checkActive() {
		boolean isActive = false;
		JVGPane pane = getPane();
		if (pane != null && pane.isEditable()) {
			JVGSelectionModel selectionModel = pane.getSelectionManager();
			if (selectionModel.getSelectionCount() > 1) {
				isActive = true;
			}
		}
		setActive(isActive);
	}

	@Override
	public void paintAction(Graphics2D g, AffineTransform transform) {
		// revalidate root action as root child may be changed and only this child will be revalidated
		invalidate();
		validate();
		super.paintAction(g, transform);
	}

	@Override
	public void start(JVGMouseEvent e) {
		JVGPane pane = getPane();
		if (pane != null) {
			pane.setFocusOwner(null);
		}
	}

	@Override
	public boolean contains(double x, double y) {
		return isActive() && (getScaledBounds() == null || getScaledBounds().contains(x, y));
	}

	@Override
	public void selectionChanged(JVGSelectionEvent event) {
		checkActive();
		invalidate();
	}

	private boolean firstConnectedToPeer = true;

	@Override
	public void connectedToPeer(JVGPeerEvent e) {
		JVGPane pane = e.getNewPeer();
		if (pane != null && pane.isEditable()) {
			JVGSelectionModel selectionModel = pane.getSelectionManager();
			if (selectionModel != null) {
				if (firstConnectedToPeer) {
					firstConnectedToPeer = false;
					selectionModel.addSelectionListener(this);
				}
			}
		} else {
			JVGSelectionModel selectionModel = pane.getSelectionManager();
			if (selectionModel != null) {
				selectionModel.removeSelectionListener(this);
				firstConnectedToPeer = true;
			}
		}
	}

	@Override
	public void disconnectedFromPeer(JVGPeerEvent e) {
	}

	@Override
	public boolean isDrawAction() {
		return isActive() && ((getCurrentAction() == null && !isActionActive()) || getCurrentAction() == this);
	}

	@Override
	public void focusLost(JVGFocusEvent e) {
	}

	@Override
	public void focusGained(JVGFocusEvent e) {
	}
}
