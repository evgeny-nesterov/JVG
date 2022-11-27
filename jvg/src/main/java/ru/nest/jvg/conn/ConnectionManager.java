package ru.nest.jvg.conn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGPane;

public class ConnectionManager {
	private static HashMap<JVGPane, ConnectionContainer> containers = new HashMap<>();

	public static void register(Connection c) {
		if (c != null) {
			JVGComponent src = c.getSource();
			if (src != null) {
				JVGPane pane = src.getPane();
				if (pane != null) {
					synchronized (pane) {
						ConnectionContainer container = containers.get(pane);
						if (container == null) {
							container = new ConnectionContainer();
							containers.put(pane, container);
						}
						container.add(c);
					}
				}
			}
		}
	}

	public static boolean isRegistered(Connection c) {
		if (c != null) {
			JVGComponent src = c.getSource();
			if (src != null) {
				JVGPane pane = src.getPane();
				if (pane != null) {
					synchronized (pane) {
						ConnectionContainer container = containers.get(pane);
						if (container != null) {
							return container.isRegistered(c);
						}
					}
				}
			}
		}

		return false;
	}

	public static void unregister(Connection c) {
		if (c != null) {
			JVGComponent src = c.getSource();
			if (src != null) {
				JVGPane pane = src.getPane();
				if (pane != null) {
					synchronized (pane) {
						ConnectionContainer container = containers.get(pane);
						if (container != null) {
							container.remove(c);
						}
					}
				}
			}
		}
	}

	public static boolean accept(JVGPane pane, JVGComponent change) {
		if (pane != null) {
			ConnectionContainer container = null;
			synchronized (pane) {
				container = containers.get(pane);
			}

			if (container != null) {
				return container.accept(change);
			}
		}

		return false;
	}

	public static boolean postAdjust(JVGPane pane) {
		if (pane != null) {
			ConnectionContainer container = null;
			synchronized (pane) {
				container = containers.get(pane);
			}

			if (container != null) {
				return container.postAdjust();
			}
		}

		return false;
	}

	public static ArrayList<Relation> getRelations(JVGPane pane) {
		if (pane != null) {
			ConnectionContainer container = null;
			synchronized (pane) {
				container = containers.get(pane);
			}

			if (container != null) {
				return container.getRelations();
			}
		}

		return null;
	}

	public static void clear(JVGPane pane) {
		if (pane != null) {
			ConnectionContainer container = null;
			synchronized (pane) {
				container = containers.get(pane);
			}

			if (container != null) {
				container.clear();
			}
		}
	}

	static class ConnectionContainer {
		private HashSet<ConnectionClient> clients = new HashSet<>();

		private ArrayList<ConnectionClient> clients_list = new ArrayList<>();

		public ArrayList<ConnectionClient> getClients() {
			return clients_list;
		}

		private HashSet<ConnectionServer> servers = new HashSet<>();

		private ArrayList<ConnectionServer> servers_list = new ArrayList<>();

		public ArrayList<ConnectionServer> getServers() {
			return servers_list;
		}

		public void add(Connection c) {
			if (c instanceof ConnectionClient) {
				ConnectionClient cc = (ConnectionClient) c;
				if (clients.add(cc)) {
					clients_list.add(cc);
				}
			}

			if (c instanceof ConnectionServer) {
				ConnectionServer cs = (ConnectionServer) c;
				if (servers.add(cs)) {
					servers_list.add(cs);
				}
			}
		}

		public void remove(Connection c) {
			if (c instanceof ConnectionClient) {
				if (clients.remove(c)) {
					clients_list.remove(c);
				}
			}

			if (c instanceof ConnectionServer) {
				if (servers.remove(c)) {
					servers_list.remove(c);
				}
			}
		}

		public boolean isRegistered(Connection c) {
			if (c instanceof ConnectionClient) {
				return clients.contains(c);
			}

			if (c instanceof ConnectionServer) {
				return servers.contains(c);
			}

			return false;
		}

		private HashSet<Relation> relations = new HashSet<>();

		private ArrayList<Relation> relations_list = new ArrayList<>();

		public ArrayList<Relation> getRelations() {
			return relations_list;
		}

		/**
		 * Invoke then mouse is released
		 */
		public boolean postAdjust() {
			boolean isAdjusted = false;
			int size = relations_list.size();
			for (int i = 0; i < size; i++) {
				Relation relation = relations_list.get(i);
				if (!relation.isAdjusted()) {
					ConnectionClient client = relation.getClient();
					JVGComponent clientSource = client.getSource();
					if (!adjusted.contains(clientSource)) {
						adjusted.add(clientSource);
						client.adjust(relation.getServer());
						adjusted.remove(clientSource);

						isAdjusted = true;
					}
					relation.setAdjusted(true);
				}
			}

			return isAdjusted;
		}

		public void clear() {
			int size = relations_list.size();
			for (int i = size - 1; i >= 0; i--) {
				Relation relation = relations_list.get(i);
				ConnectionClient client = relation.getClient();
				ConnectionServer server = relation.getServer();

				client.disconnect(server);
				relations.remove(relation);
				relations_list.remove(i);
			}
		}

		private HashSet<JVGComponent> adjusted = new HashSet<>();

		public boolean accept(JVGComponent change) {
			int size = relations_list.size();
			for (int i = 0; i < size; i++) {
				Relation relation = relations_list.get(i);
				if (relation.getServer().getSource() == change && relation.isAdjusted()) {
					ConnectionClient client = relation.getClient();
					JVGComponent clientSource = client.getSource();

					if (!adjusted.contains(clientSource)) {
						adjusted.add(clientSource);
						client.adjust(relation.getServer());
						adjusted.remove(clientSource);
					}
				}
			}

			if (adjusted.contains(change)) {
				return false;
			}

			for (int i = size - 1; i >= 0; i--) {
				Relation relation = relations_list.get(i);
				ConnectionClient client = relation.getClient();
				ConnectionServer server = relation.getServer();
				JVGComponent clientSource = client.getSource();

				if (clientSource == change || clientSource.getParent() == null || server.getSource().getParent() == null) {
					client.disconnect(server);
					relations.remove(relation);
					relations_list.remove(i);
				}
			}

			int serversCount = servers_list.size();
			int clientsCount = clients_list.size();

			for (int i = 0; i < serversCount; i++) {
				ConnectionServer server = servers_list.get(i);
				for (int j = 0; j < clientsCount; j++) {
					ConnectionClient client = clients_list.get(j);
					if (client.getSource() == change) {
						if (client != server && !client.isConnected(server)) {
							Relation relation = new Relation(client, server);
							if (!relations.contains(relation)) {
								if (client.connect(server)) {
									relations.add(relation);
									relations_list.add(relation);
								}
							}
						}
					}
				}
			}

			// System.out.println((count++) + "> relations: " +
			// relations.size());
			return relations.size() > 0;
		}
	}
}
