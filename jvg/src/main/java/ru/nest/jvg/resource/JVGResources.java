package ru.nest.jvg.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JVGResources {
	public JVGResources() {
	}

	public void append(JVGResources resources) {
		if (resources != null) {
			for (ClassResource<Resource<?>> cr : resources.classes.values()) {
				for (Resource<?> r : cr.list) {
					addResource(r);
				}
			}
		}
	}

	private Map<Class<?>, ClassResource<Resource<?>>> classes = new HashMap<>();

	public List<Class<?>> getClasses() {
		return new ArrayList<>(classes.keySet());
	}

	public void addResources(JVGResources resources) {
		if (resources != null) {
			for (Class resourceType : resources.classes.keySet()) {
				for (int i = 0; i < resources.getResourcesCount(resourceType); i++) {
					addResource(resources.getResource(resourceType, i));
				}
			}
		}
	}

	public <V extends Resource<?>> void addResource(V resource) {
		ClassResource<Resource<?>> resources = classes.get(resource.getClass());
		if (resources == null) {
			resources = new ClassResource<>();
			classes.put(resource.getClass(), resources);
		}
		resources.addResource(resource);
	}

	public <V extends Resource<?>> void addResource(int index, V resource) {
		ClassResource<Resource<?>> resources = classes.get(resource.getClass());
		if (resources == null) {
			resources = new ClassResource<>();
			classes.put(resource.getClass(), resources);
		}
		resources.addResource(index, resource);
	}

	public <V extends Resource<?>> V getResource(Class<?> clazz, String name) {
		ClassResource<V> resources = (ClassResource<V>) classes.get(clazz);
		if (resources != null) {
			return resources.getResource(name);
		} else {
			for (Class c : classes.keySet()) {
				if (clazz.isAssignableFrom(c)) {
					resources = (ClassResource<V>) classes.get(c);
					V resource = resources.getResource(name);
					if (resource != null) {
						return resource;
					}
				}
			}
			return null;
		}
	}

	public <V extends Resource<?>> V removeResource(Class<V> clazz, String name) {
		ClassResource<V> resources = (ClassResource<V>) classes.get(clazz);
		if (resources != null) {
			return resources.removeResource(name);
		} else {
			return null;
		}
	}

	public <V extends Resource<?>> V removeResource(V resource) {
		ClassResource<V> resources = (ClassResource<V>) classes.get(resource.getClass());
		if (resources != null) {
			return resources.removeResource(resource);
		} else {
			return null;
		}
	}

	public <V extends Resource<?>> int getResourcesCount(Class<V> clazz) {
		ClassResource<V> resources = (ClassResource<V>) classes.get(clazz);
		if (resources != null && resources.list != null) {
			return resources.list.size();
		} else {
			return 0;
		}
	}

	public <V extends Resource<?>> V getResource(Class<V> clazz, int index) {
		ClassResource<V> resources = (ClassResource<V>) classes.get(clazz);
		if (resources != null && resources.list != null && index >= 0 && index < resources.list.size()) {
			return resources.list.get(index);
		} else {
			return null;
		}
	}

	public <V extends Resource<?>> boolean contains(Class<V> clazz, String name) {
		ClassResource<V> resources = (ClassResource<V>) classes.get(clazz);
		if (resources != null) {
			return resources.contains(name);
		} else {
			return false;
		}
	}

	class ClassResource<V extends Resource<?>> {
		Map<String, V> resources = new HashMap<>();

		List<V> list = new ArrayList<>();

		public void addResource(V resource) {
			addResource(-1, resource);
		}

		public void addResource(int index, V resource) {
			if (index == -1 || (index >= 0 && index < list.size())) {
				V exist = getResource(resource.getName());
				if (exist != null) {
					if (exist == resource) {
						return;
					}
					removeResource(resource.getName());
				}

				if (index != -1) {
					list.add(index, resource);
				} else {
					list.add(resource);
				}
				resources.put(resource.getName(), resource);
				fireResourceAdded(resource);
			}
		}

		public V getResource(String name) {
			return resources.get(name);
		}

		public boolean contains(String name) {
			return resources.containsKey(name);
		}

		public V removeResource(String name) {
			if (resources.containsKey(name)) {
				return removeResource(resources.get(name));
			} else {
				return null;
			}
		}

		@SuppressWarnings("hiding")
		public <V extends Resource<?>> V removeResource(V resource) {
			if (list.contains(resource)) {
				fireResourceRemoved(resource);
				list.remove(resource);
				resources.remove(resource.getName());
				return resource;
			}
			return null;
		}
	}

	// listeners
	public static interface Listener {
		public void resourceAdded(Resource<?> resource);

		public void resourceRemoved(Resource<?> resource);
	}

	private List<Listener> listeners = new ArrayList<>();

	public void addListener(Listener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	public void removeListener(Listener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	protected void fireResourceAdded(Resource<?> resource) {
		synchronized (listeners) {
			for (Listener listener : listeners) {
				listener.resourceAdded(resource);
			}
		}
	}

	protected void fireResourceRemoved(Resource<?> resource) {
		synchronized (listeners) {
			for (Listener listener : listeners) {
				listener.resourceRemoved(resource);
			}
		}
	}
}
