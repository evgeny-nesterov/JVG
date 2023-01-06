package ru.nest.jvg.conn;

public interface ConnectionClient extends Connection {
	boolean connect(ConnectionServer server);

	boolean isConnected(ConnectionServer server);

	void disconnect(ConnectionServer server);

	void adjust(ConnectionServer server);

	// public void start();
	//
	//
	// public void stop(JVGUndoRedo edit);
}
