package ru.nest.jvg.resource;

import ru.nest.jvg.editor.resources.JVGLocaleManager;

public class Script {
	// pane
	public final static Type START = new Type("start", 0, "button.scripts.start", "Start");

	// mouse
	public final static Type MOUSE_CLICKED = new Type("mouse-clicked", 1, "button.scripts.mouse.clicked", "Mouse clicked");

	public final static Type MOUSE_PRESSED = new Type("mouse-pressed", 2, "button.scripts.mouse.pressed", "Mouse pressed");

	public final static Type MOUSE_RELEASED = new Type("mouse-released", 3, "button.scripts.mouse.released", "Mouse released");

	public final static Type MOUSE_ENTERED = new Type("mouse-entered", 4, "button.scripts.mouse.entered", "Mouse entered");

	public final static Type MOUSE_EXITED = new Type("mouse-exited", 5, "button.scripts.mouse.exited", "Mouse exited");

	public final static Type MOUSE_MOVED = new Type("mouse-moved", 6, "button.scripts.mouse.moved", "Mouse moved");

	public final static Type MOUSE_DRAGGED = new Type("mouse-dragged", 7, "button.scripts.mouse.dragged", "Mouse dragged");

	public final static Type MOUSE_WHEEL = new Type("mouse-wheel", 8, "button.scripts.mouse.wheel", "Mouse wheel");

	// key
	public final static Type KEY_PRESSED = new Type("key-pressed", 9, "button.scripts.key.pressed", "Key pressed");

	public final static Type KEY_RELEASED = new Type("key-released", 10, "button.scripts.key.released", "Key released");

	public final static Type KEY_TYPED = new Type("key-typed", 11, "button.scripts.key.typed", "Key typed");

	// component
	public final static Type COMPONENT_SHOWN = new Type("component-shown", 12, "button.scripts.component-shown", "Component shown");

	public final static Type COMPONENT_HIDDEN = new Type("component-hidden", 13, "button.scripts.component-hidden", "Component hidden");

	public final static Type COMPONENT_GEOMETRY_CHANGED = new Type("component-geometry-changed", 14, "button.scripts.component-geometry-changed", "Component geometry changed");

	public final static Type COMPONENT_TRANSFORMED = new Type("component-transformed", 15, "button.scripts.component-transformed", "Component transformed");

	// focus
	public final static Type FOCUS_LOST = new Type("focus-lost", 16, "button.scripts.focus-lost", "Focus lost");

	public final static Type FOCUS_GAINED = new Type("focus-gained", 17, "button.scripts.focus-gained", "Focus gained");

	// container
	public final static Type COMPONENT_ADDED = new Type("component-added", 18, "button.scripts.component-added", "Component added");

	public final static Type COMPONENT_REMOVED = new Type("component-removed", 19, "button.scripts.component-removed", "Component removed");

	public final static Type COMPONENT_ORDER_CHANGED = new Type("component-order-changed", 20, "button.scripts.component-order-changed", "Component order changed");

	// peer
	public final static Type COMPONENT_CONNECTED_TO_PEER = new Type("component-connected-to-peer", 21, "button.scripts.component-connected-to-peer", "Component connected to peer");

	public final static Type COMPONENT_DISCONNECTED_FROM_PEER = new Type("component-disconnected-from-peer", 22, "button.scripts.component-disconnected-from-peer", "Component disconnected from peer");

	public final static Type PROPERTY_CHANGED = new Type("property-changed", 23, "button.scripts.property-changed", "Property changed");

	public final static Type VALIDATION = new Type("validation", 24, "button.scripts.validation", "Validation");

	public final static Type PAINT = new Type("paint", 25, "button.scripts.paint", "Paint");

	public final static Type[] types = { START, MOUSE_CLICKED, MOUSE_PRESSED, MOUSE_RELEASED, MOUSE_ENTERED, MOUSE_EXITED, MOUSE_MOVED, MOUSE_DRAGGED, MOUSE_WHEEL, KEY_PRESSED, KEY_RELEASED, KEY_TYPED, COMPONENT_SHOWN, COMPONENT_HIDDEN, COMPONENT_GEOMETRY_CHANGED, COMPONENT_TRANSFORMED, FOCUS_LOST, FOCUS_GAINED, COMPONENT_ADDED, COMPONENT_REMOVED, COMPONENT_ORDER_CHANGED, COMPONENT_CONNECTED_TO_PEER, COMPONENT_DISCONNECTED_FROM_PEER, PROPERTY_CHANGED, VALIDATION, PAINT };

	public Script(ScriptResource data, Type type) {
		this.data = data;
		this.type = type;
	}

	public static Type getType(String name) {
		for (Type t : types) {
			if (t.name.equals(name)) {
				return t;
			}
		}
		return null;
	}

	private ScriptResource data;

	public ScriptResource getData() {
		return data;
	}

	public void setData(ScriptResource data) {
		this.data = data;
	}

	private Type type;

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public final static class Type {
		public final static String ACTION_PREFIX = "script-";

		private Type(String name, int index, String localeName, String descr) {
			this.name = name;
			this.index = index;
			this.localeName = localeName;
			this.descr = descr;

			actionName = ACTION_PREFIX + name;
		}

		private String name;

		public String getName() {
			return name;
		}

		private int index;

		public int getIndex() {
			return index;
		}

		private String actionName;

		public String getActionName() {
			return actionName;
		}

		private String localeName;

		private String descr;

		public String getDescr() {
			return JVGLocaleManager.getInstance().getValue(localeName, descr);
		}

		@Override
		public String toString() {
			return getDescr();
		}
	}
}
