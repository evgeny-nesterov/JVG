package ru.nest.jvg.event;

public interface JVGPeerListener extends JVGEventListener {
	public void connectedToPeer(JVGPeerEvent e);

	public void disconnectedFromPeer(JVGPeerEvent e);
}
