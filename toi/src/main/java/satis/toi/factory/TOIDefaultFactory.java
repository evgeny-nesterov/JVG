package satis.toi.factory;

import satis.toi.TOIFactory;
import satis.toi.TOIObject;

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
