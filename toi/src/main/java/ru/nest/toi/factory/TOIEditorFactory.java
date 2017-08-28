package ru.nest.toi.factory;

import ru.nest.toi.TOIFactory;
import ru.nest.toi.TOIObject;
import ru.nest.toi.objectcontrol.TOIRotateObjectControl;
import ru.nest.toi.objectcontrol.TOIScaleObjectControl;

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
