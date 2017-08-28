package satis.iface.cache;

import java.util.ArrayList;
import java.util.List;

public abstract class Cacher<V> {
	public Cacher() {
	}

	private List<V> cache = new ArrayList<V>(10);

	public V next() {
		synchronized (cache) {
			if (cache.size() > 0) {
				return cache.remove(0);
			} else {
				return createObject();
			}
		}
	}

	public void putBack(V object) {
		synchronized (cache) {
			cache.add(object);
		}
	}

	public void putBack(ArrayList<V> objects) {
		synchronized (cache) {
			cache.addAll(objects);
		}
	}

	public void clear() {
		synchronized (cache) {
			cache.clear();
		}
	}

	public abstract V createObject();
}
