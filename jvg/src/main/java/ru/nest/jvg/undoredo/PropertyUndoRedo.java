package ru.nest.jvg.undoredo;

import java.lang.reflect.Method;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import ru.nest.jvg.JVGComponent;
import ru.nest.jvg.JVGPane;

public class PropertyUndoRedo extends JVGUndoRedo {
	private Object object;

	private String undoMethodName;

	private Class<?>[] undoParameterTypes;

	private Object[] undoArg;

	private String redoMethodName;

	private Class<?>[] redoParameterTypes;

	private Object[] redoArg;

	public PropertyUndoRedo(String name, JVGPane pane, Object object, String methodName, boolean oldValue, boolean newValue) {
		this(name, pane, object, methodName, new Class[] { boolean.class }, new Object[] { oldValue }, new Object[] { newValue });
	}

	public PropertyUndoRedo(String name, JVGPane pane, Object object, String methodName, int oldValue, int newValue) {
		this(name, pane, object, methodName, new Class[] { int.class }, new Object[] { oldValue }, new Object[] { newValue });
	}

	public PropertyUndoRedo(String name, JVGPane pane, Object object, String methodName, double oldValue, double newValue) {
		this(name, pane, object, methodName, new Class[] { double.class }, new Object[] { oldValue }, new Object[] { newValue });
	}

	public PropertyUndoRedo(String name, JVGPane pane, Object object, String methodName, float oldValue, float newValue) {
		this(name, pane, object, methodName, new Class[] { float.class }, new Object[] { oldValue }, new Object[] { newValue });
	}

	public <V> PropertyUndoRedo(String name, JVGPane pane, Object object, String methodName, V oldValue, V newValue) {
		this(name, pane, object, methodName, oldValue != null ? oldValue.getClass() : newValue.getClass(), oldValue, newValue);
	}

	public <V> PropertyUndoRedo(String name, JVGPane pane, Object object, String methodName, Class<?> clazz, V oldValue, V newValue) {
		this(name, pane, object, methodName, new Class[] { clazz }, new Object[] { oldValue }, new Object[] { newValue });
	}

	public PropertyUndoRedo(String name, JVGPane pane, Object object, String methodName, Class<?>[] parameterTypes, Object[] undoArg, Object[] redoArg) {
		this(name, pane, object, methodName, parameterTypes, undoArg, methodName, parameterTypes, redoArg);
	}

	public PropertyUndoRedo(String name, JVGPane pane, Object object, String undoMethodName, Class<?>[] undoParameterTypes, Object[] undoArg, String redoMethodName, Class<?>[] redoParameterTypes, Object[] redoArg) {
		super(name, pane);
		this.object = object;
		this.undoMethodName = undoMethodName;
		this.undoParameterTypes = undoParameterTypes;
		this.undoArg = undoArg;
		this.redoMethodName = redoMethodName;
		this.redoParameterTypes = redoParameterTypes;
		this.redoArg = redoArg;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		invoke(undoMethodName, undoParameterTypes, undoArg);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		invoke(redoMethodName, redoParameterTypes, redoArg);
	}

	private void invoke(String methodName, Class<?>[] parameterTypes, Object[] arg) {
		try {
			Method method = object.getClass().getMethod(methodName, parameterTypes);
			method.invoke(object, arg);

			if (object instanceof JVGComponent) {
				JVGComponent component = (JVGComponent) object;
				component.invalidate();
				component.repaint();
			} else {
				getPane().repaint();
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
}
