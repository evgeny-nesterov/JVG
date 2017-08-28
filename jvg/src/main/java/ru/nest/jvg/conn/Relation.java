package ru.nest.jvg.conn;

public class Relation {
	public Relation(ConnectionClient client, ConnectionServer server) {
		this.client = client;
		this.server = server;
	}

	private ConnectionClient client;

	public ConnectionClient getClient() {
		return client;
	}

	private ConnectionServer server;

	public ConnectionServer getServer() {
		return server;
	}

	@Override
	public int hashCode() {
		return client.hashCode() + server.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Relation)) {
			return false;
		}

		Relation r = (Relation) o;
		return (r.client == client && r.server == server) || (r.client == server && r.server == client);
	}

	private boolean adjusted = false;

	public boolean isAdjusted() {
		return adjusted;
	}

	public void setAdjusted(boolean adjusted) {
		this.adjusted = adjusted;
	}
}
