package ru.nest.toi;

public interface TOIFactory {
	<O extends TOIObject> O create(Class<O> type);
}
