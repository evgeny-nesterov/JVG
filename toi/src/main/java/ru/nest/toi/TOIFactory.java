package ru.nest.toi;

public interface TOIFactory {
	public <O extends TOIObject> O create(Class<O> type);
}
