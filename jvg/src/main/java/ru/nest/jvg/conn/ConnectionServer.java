package ru.nest.jvg.conn;

public interface ConnectionServer extends Connection {
	public Position accept(ConnectionClient client, double clientX, double clientY);
}
