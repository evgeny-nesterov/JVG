package ru.nest.jvg.event;

public interface JVGPeerListener extends JVGEventListener {
	void connectedToPeer(JVGPeerEvent e);

	void disconnectedFromPeer(JVGPeerEvent e);
}
