package ru.nest.jvg.event;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGPane;

public class JVGPeerEvent extends JVGEvent {
	public final static int CONNECTED_TO_PEER = 0;

	public final static int DISCONNECTED_FROM_PEER = 1;

	public JVGPeerEvent(JVGComponent source, int id, JVGPane oldPeer, JVGPane newPeer) {
		super(source, id);
		this.oldPeer = oldPeer;
		this.newPeer = newPeer;
	}

	JVGPane oldPeer;

	public JVGPane getOldPeer() {
		return oldPeer;
	}

	JVGPane newPeer;

	public JVGPane getNewPeer() {
		return newPeer;
	}

	@Override
	public String paramString() {
		if (id == CONNECTED_TO_PEER) {
			return "CONNECTED_TO_PEER";
		} else {
			return "DISCONNECTED_FROM_PEER";
		}
	}
}
