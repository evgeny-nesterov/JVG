package satis.toi.factory;

import satis.toi.TOIFactory;
import satis.toi.TOIObject;
import satis.toi.objectcontrol.TOIRotateObjectControl;
import satis.toi.objectcontrol.TOIScaleObjectControl;

public class TOIEditorFactory implements TOIFactory {
	@Override
	public <O extends TOIObject> O create(Class<O> type) {
		O o = null;
		try {
			o = type.getConstructor().newInstance();
			o.addControl(new TOIRotateObjectControl(o));
			o.addControl(new TOIScaleObjectControl(o));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return o;
	}
}
