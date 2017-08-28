package ru.nest.jvg.conn;

public interface ConnectionClient extends Connection {
	public boolean connect(ConnectionServer server);

	public boolean isConnected(ConnectionServer server);

	public void disconnect(ConnectionServer server);

	public void adjust(ConnectionServer server);

	// public void start();
	//
	//
	// public void stop(JVGUndoRedo edit);
}
