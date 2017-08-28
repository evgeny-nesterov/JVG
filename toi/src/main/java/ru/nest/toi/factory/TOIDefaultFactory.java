package ru.nest.toi.factory;

import ru.nest.toi.TOIFactory;
import ru.nest.toi.TOIObject;

public class TOIDefaultFactory implements TOIFactory {
	@Override
	public <O extends TOIObject> O create(Class<O> type) {
		O o = null;
		try {
			o = type.getConstructor().newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return o;
	}
}
