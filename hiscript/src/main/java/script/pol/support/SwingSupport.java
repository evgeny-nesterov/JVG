package script.pol.support;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import script.pol.model.ExecuteException;
import script.pol.model.Method;
import script.pol.model.Node;
import script.pol.model.RuntimeContext;
import script.pol.model.ScriptUtil;
import script.pol.model.Types;
import script.pol.model.ValueContainer;
import script.pol.model.Variable;

//==================================================
//	Methods:
//==================================================
//	long getRoot()
//	void add(long parent, long component)
//	void remove(long parent, long component)
//	int getChildsCount(long component)
//	long getChild(long component, int index)
//	void setBounds(long component, int x, int y, int w, int h)
//	void setLocation(long component, int x, int y)
//	void setSize(long component, int w, int h)
//	int getX(long component)
//	int getY(long component)
//	int getWidth(long component)
//	int getHeight(long component)
//	int getPreferredWidth(long component)
//	int getPreferredHeight(long component)
//	int getType(long component)
//	long createButton(string text)
//	long createLabel(string text)
//	long createTextField(string text)
//	long createTextArea(string text)
//	void mousePressed(long component, string script)
//	void mouseReleased(long component, string script)
//	void mouseEntered(long component, string script)
//	void mouseExited(long component, string script)
//	void mouseMoved(long component, string script)
//	void mouseDragged(long component, string script)
//	void keyPressed(long component, string script)
//	void keyReleased(long component, string script)

public class SwingSupport {
	public final static String NAMESPACE = "gui";

	private long rootID;

	public SwingSupport(JComponent root) {
		rootID = registerComponent(root);
		init();
	}

	public void export(RuntimeContext ctx) throws ExecuteException {
		ctx.addMethods(methods);
	}

	private static long id = 0;

	public static synchronized long nextID() {
		return id++;
	}

	private Map<Long, JComponent> id_components = new HashMap<Long, JComponent>();

	private Map<JComponent, Long> components_id = new HashMap<JComponent, Long>();

	public long registerComponent(JComponent component) {
		long id = nextID();
		id_components.put(id, component);
		components_id.put(component, id);
		return id;
	}

	private long getComponentID(JComponent c) {
		if (!components_id.containsKey(c)) {
			registerComponent(c);
		}
		return components_id.get(c);
	}

	private List<Method> methods = new ArrayList<Method>();

	private void init() {
		// Structure
		methods.add(new Method(NAMESPACE, "getRoot", new int[] {}, new int[] {}, Types.LONG) {
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				ctx.value.longNumber = rootID;
			}
		});

		methods.add(new Method(NAMESPACE, "add", new int[] { Types.LONG, Types.LONG }, new int[] { 0, 0 }, Types.VOID) {
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				Long parentID = (Long) arguments[0];
				Long childID = (Long) arguments[1];

				JComponent p = id_components.get(parentID);
				JComponent c = id_components.get(childID);
				if (p != null && c != null) {
					p.add(c);
					p.revalidate();
					p.repaint();
				}
			}
		});

		methods.add(new Method(NAMESPACE, "remove", new int[] { Types.LONG, Types.LONG }, new int[] { 0, 0 }, Types.VOID) {
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				Long parentID = (Long) arguments[0];
				Long childID = (Long) arguments[1];

				JComponent p = id_components.get(parentID);
				JComponent c = id_components.get(childID);
				if (p != null && c != null) {
					p.remove(c);
					p.revalidate();
					p.repaint();
				}
			}
		});

		methods.add(new Method(NAMESPACE, "getChildsCount", new int[] { Types.LONG }, new int[] { 0 }, Types.INT) {
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				Long id = (Long) arguments[0];
				JComponent c = id_components.get(id);
				ctx.value.intNumber = c != null ? c.getComponentCount() : 0;
			}
		});

		methods.add(new Method(NAMESPACE, "getChild", new int[] { Types.LONG, Types.INT }, new int[] { 0, 0 }, Types.LONG) {
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				Long id = (Long) arguments[0];
				int index = (Integer) arguments[1];

				JComponent p = id_components.get(id);
				if (p != null) {
					JComponent c = (JComponent) p.getComponent(index);
					ctx.value.longNumber = getComponentID(c);
				} else {
					ctx.value.longNumber = -1;
				}
			}
		});

		// Bounds
		methods.add(new Method(NAMESPACE, "setBounds", new int[] { Types.LONG, Types.INT, Types.INT, Types.INT, Types.INT }, new int[] { 0, 0, 0, 0, 0 }, Types.VOID) {
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				Long id = (Long) arguments[0];
				int x = (Integer) arguments[1];
				int y = (Integer) arguments[2];
				int w = (Integer) arguments[3];
				int h = (Integer) arguments[4];
				JComponent c = id_components.get(id);
				if (c != null) {
					c.setBounds(x, y, w, h);
				}
			}
		});

		methods.add(new Method(NAMESPACE, "setLocation", new int[] { Types.LONG, Types.INT, Types.INT }, new int[] { 0, 0, 0 }, Types.VOID) {
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				Long id = (Long) arguments[0];
				int x = (Integer) arguments[1];
				int y = (Integer) arguments[2];
				JComponent c = id_components.get(id);
				if (c != null) {
					c.setLocation(x, y);
				}
			}
		});

		methods.add(new Method(NAMESPACE, "setSize", new int[] { Types.LONG, Types.INT, Types.INT }, new int[] { 0, 0, 0 }, Types.VOID) {
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				Long id = (Long) arguments[0];
				int w = (Integer) arguments[1];
				int h = (Integer) arguments[2];
				JComponent c = id_components.get(id);
				if (c != null) {
					c.setSize(w, h);
				}
			}
		});

		methods.add(new Method(NAMESPACE, "getX", new int[] { Types.LONG }, new int[] { 0, 0, 0 }, Types.INT) {
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				Long id = (Long) arguments[0];
				JComponent c = id_components.get(id);
				ctx.value.intNumber = c != null ? c.getX() : 0;
			}
		});

		methods.add(new Method(NAMESPACE, "getY", new int[] { Types.LONG }, new int[] { 0, 0, 0 }, Types.INT) {
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				Long id = (Long) arguments[0];
				JComponent c = id_components.get(id);
				ctx.value.intNumber = c != null ? c.getY() : 0;
			}
		});

		methods.add(new Method(NAMESPACE, "getWidth", new int[] { Types.LONG }, new int[] { 0, 0, 0 }, Types.INT) {
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				Long id = (Long) arguments[0];
				JComponent c = id_components.get(id);
				ctx.value.intNumber = c != null ? c.getWidth() : 0;
			}
		});

		methods.add(new Method(NAMESPACE, "getHeight", new int[] { Types.LONG }, new int[] { 0, 0, 0 }, Types.INT) {
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				Long id = (Long) arguments[0];
				JComponent c = id_components.get(id);
				ctx.value.intNumber = c != null ? c.getHeight() : 0;
			}
		});

		methods.add(new Method(NAMESPACE, "getPreferredWidth", new int[] { Types.LONG }, new int[] { 0, 0, 0 }, Types.INT) {
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				Long id = (Long) arguments[0];
				JComponent c = id_components.get(id);
				ctx.value.intNumber = c != null ? c.getPreferredSize().width : 0;
			}
		});

		methods.add(new Method(NAMESPACE, "getPreferredHeight", new int[] { Types.LONG }, new int[] { 0, 0, 0 }, Types.INT) {
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				Long id = (Long) arguments[0];
				JComponent c = id_components.get(id);
				ctx.value.intNumber = c != null ? c.getPreferredSize().height : 0;
			}
		});

		// Create a new component
		methods.add(new Method(NAMESPACE, "getType", new int[] { Types.LONG }, new int[] { 0 }, Types.INT) {
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				String text = (String) arguments[0];
				JComponent c = new JButton(text);
				int type = -1;
				if (c instanceof JButton) {
					type = 0;
				} else if (c instanceof JLabel) {
					type = 1;
				} else if (c instanceof JTextField) {
					type = 2;
				} else if (c instanceof JTextArea) {
					type = 3;
				}
				ctx.value.intNumber = type;
			}
		});

		methods.add(new Method(NAMESPACE, "createButton", new int[] { Types.STRING }, new int[] { 0 }, Types.LONG) {
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				String text = (String) arguments[0];
				JComponent c = new JButton(text);
				ctx.value.longNumber = registerComponent(c);
			}
		});

		methods.add(new Method(NAMESPACE, "createLabel", new int[] { Types.STRING }, new int[] { 0 }, Types.LONG) {
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				String text = (String) arguments[0];
				JComponent c = new JLabel(text);
				ctx.value.longNumber = registerComponent(c);
			}
		});

		methods.add(new Method(NAMESPACE, "createTextField", new int[] { Types.STRING }, new int[] { 0 }, Types.LONG) {
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				String text = (String) arguments[0];
				JComponent c = new JTextField(text);
				ctx.value.longNumber = registerComponent(c);
			}
		});

		methods.add(new Method(NAMESPACE, "createTextArea", new int[] { Types.STRING }, new int[] { 0 }, Types.LONG) {
			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				String text = (String) arguments[0];
				JComponent c = new JTextArea(text);
				ctx.value.longNumber = registerComponent(c);
			}
		});

		// Events
		methods.add(new Method(NAMESPACE, "mousePressed", new int[] { Types.LONG, Types.STRING }, new int[] { 0, 0 }, Types.VOID) {
			MouseListener listener = null;

			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				Long id = (Long) arguments[0];
				JComponent c = id_components.get(id);
				if (c != null) {
					String script = (String) arguments[1];
					listener = setMouseEvent(c, ctx, parent, script, listener, MouseEvent.MOUSE_PRESSED);
				}
			}
		});

		methods.add(new Method(NAMESPACE, "mouseReleased", new int[] { Types.LONG, Types.STRING }, new int[] { 0, 0 }, Types.VOID) {
			MouseListener listener = null;

			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				Long id = (Long) arguments[0];
				JComponent c = id_components.get(id);
				if (c != null) {
					String script = (String) arguments[1];
					listener = setMouseEvent(c, ctx, parent, script, listener, MouseEvent.MOUSE_RELEASED);
				}
			}
		});

		methods.add(new Method(NAMESPACE, "mouseEntered", new int[] { Types.LONG, Types.STRING }, new int[] { 0, 0 }, Types.VOID) {
			MouseListener listener = null;

			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				Long id = (Long) arguments[0];
				JComponent c = id_components.get(id);
				if (c != null) {
					String script = (String) arguments[1];
					listener = setMouseEvent(c, ctx, parent, script, listener, MouseEvent.MOUSE_ENTERED);
				}
			}
		});

		methods.add(new Method(NAMESPACE, "mouseExited", new int[] { Types.LONG, Types.STRING }, new int[] { 0, 0 }, Types.VOID) {
			MouseListener listener = null;

			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				Long id = (Long) arguments[0];
				JComponent c = id_components.get(id);
				if (c != null) {
					String script = (String) arguments[1];
					listener = setMouseEvent(c, ctx, parent, script, listener, MouseEvent.MOUSE_EXITED);
				}
			}
		});

		methods.add(new Method(NAMESPACE, "mouseDragged", new int[] { Types.LONG, Types.STRING }, new int[] { 0, 0 }, Types.VOID) {
			MouseMotionListener listener = null;

			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				Long id = (Long) arguments[0];
				JComponent c = id_components.get(id);
				if (c != null) {
					String script = (String) arguments[1];
					listener = setMouseMotionEvent(c, ctx, parent, script, listener, MouseEvent.MOUSE_DRAGGED);
				}
			}
		});

		methods.add(new Method(NAMESPACE, "mouseMoved", new int[] { Types.LONG, Types.STRING }, new int[] { 0, 0 }, Types.VOID) {
			MouseMotionListener listener = null;

			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				Long id = (Long) arguments[0];
				JComponent c = id_components.get(id);
				if (c != null) {
					String script = (String) arguments[1];
					listener = setMouseMotionEvent(c, ctx, parent, script, listener, MouseEvent.MOUSE_MOVED);
				}
			}
		});

		methods.add(new Method(NAMESPACE, "keyPressed", new int[] { Types.LONG, Types.STRING }, new int[] { 0, 0 }, Types.VOID) {
			KeyListener listener = null;

			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				Long id = (Long) arguments[0];
				JComponent c = id_components.get(id);
				if (c != null) {
					String script = (String) arguments[1];
					listener = setKeyEvent(c, ctx, parent, script, listener, KeyEvent.KEY_PRESSED);
				}
			}
		});

		methods.add(new Method(NAMESPACE, "keyReleased", new int[] { Types.LONG, Types.STRING }, new int[] { 0, 0 }, Types.VOID) {
			KeyListener listener = null;

			public void invoke(RuntimeContext ctx, Node parent, Object... arguments) throws ExecuteException {
				super.invoke(ctx, parent, arguments);
				Long id = (Long) arguments[0];
				JComponent c = id_components.get(id);
				if (c != null) {
					String script = (String) arguments[1];
					listener = setKeyEvent(c, ctx, parent, script, listener, KeyEvent.KEY_RELEASED);
				}
			}
		});
	}

	private MouseListener setMouseEvent(JComponent c, final RuntimeContext ctx, final Node parent, final String script, MouseListener listener, final int type) {
		if (listener != null) {
			c.removeMouseListener(listener);
		}

		listener = new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (type == MouseEvent.MOUSE_PRESSED) {
					execute(e);
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (type == MouseEvent.MOUSE_RELEASED) {
					execute(e);
				}
			}

			public void mouseEntered(MouseEvent e) {
				if (type == MouseEvent.MOUSE_ENTERED) {
					execute(e);
				}
			}

			public void mouseExited(MouseEvent e) {
				if (type == MouseEvent.MOUSE_EXITED) {
					execute(e);
				}
			}

			private void execute(MouseEvent e) {
				try {
					Map<String, Variable> variables = new HashMap<String, Variable>();
					setMouseEventVariables(variables, e);

					parent.addVariables(variables);

					RuntimeContext.execute(ctx, parent, script, true);
				} catch (ExecuteException exc) {
					exc.printStackTrace();
				}
			}
		};
		c.addMouseListener(listener);
		return listener;
	}

	private MouseMotionListener setMouseMotionEvent(JComponent c, final RuntimeContext ctx, final Node parent, final String script, MouseMotionListener listener, final int type) {
		if (listener != null) {
			c.removeMouseMotionListener(listener);
		}

		listener = new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
				if (type == MouseEvent.MOUSE_DRAGGED) {
					execute(e);
				}
			}

			public void mouseMoved(MouseEvent e) {
				if (type == MouseEvent.MOUSE_MOVED) {
					execute(e);
				}
			}

			private void execute(MouseEvent e) {
				try {
					HashMap<String, Variable> variables = new HashMap<String, Variable>();
					setMouseEventVariables(variables, e);

					ScriptUtil.execute(ctx, parent, script, variables);
				} catch (ExecuteException exc) {
					exc.printStackTrace();
				}
			}
		};
		c.addMouseMotionListener(listener);
		return listener;
	}

	private KeyListener setKeyEvent(JComponent c, final RuntimeContext ctx, final Node parent, final String script, KeyListener listener, final int type) {
		if (listener != null) {
			c.removeKeyListener(listener);
		}

		listener = new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (type == KeyEvent.KEY_PRESSED) {
					execute(e);
				}
			}

			public void keyReleased(KeyEvent e) {
				if (type == KeyEvent.KEY_RELEASED) {
					execute(e);
				}
			}

			private void execute(KeyEvent e) {
				try {
					HashMap<String, Variable> variables = new HashMap<String, Variable>();
					setKeyEventVariables(variables, e);

					parent.addVariables(variables);

					ScriptUtil.execute(ctx, parent, script, variables);
				} catch (ExecuteException exc) {
					exc.printStackTrace();
				}
			}
		};
		c.addKeyListener(listener);
		return listener;
	}

	private void setMouseEventVariables(Map<String, Variable> variables, MouseEvent e) throws ExecuteException {
		addVariable(variables, NAMESPACE, "modifiers", Types.INT, e.getModifiers());
		addVariable(variables, NAMESPACE, "x", Types.INT, e.getX());
		addVariable(variables, NAMESPACE, "y", Types.INT, e.getY());
		addVariable(variables, NAMESPACE, "clickCount", Types.INT, e.getClickCount());
		addVariable(variables, NAMESPACE, "button", Types.INT, e.getButton());
		addVariable(variables, NAMESPACE, "when", Types.LONG, e.getWhen());
		addVariable(variables, NAMESPACE, "isAltDown", Types.BOOLEAN, e.isAltDown());
		addVariable(variables, NAMESPACE, "isControlDown", Types.BOOLEAN, e.isControlDown());
		addVariable(variables, NAMESPACE, "isShiftDown", Types.BOOLEAN, e.isShiftDown());
	}

	public void setKeyEventVariables(Map<String, Variable> variables, KeyEvent e) throws ExecuteException {
		addVariable(variables, NAMESPACE, "keyChar", Types.CHAR, e.getKeyChar());
		addVariable(variables, NAMESPACE, "keyCode", Types.INT, e.getKeyCode());
		addVariable(variables, NAMESPACE, "modifiers", Types.INT, e.getModifiers());
		addVariable(variables, NAMESPACE, "when", Types.LONG, e.getWhen());
		addVariable(variables, NAMESPACE, "isAltDown", Types.BOOLEAN, e.isAltDown());
		addVariable(variables, NAMESPACE, "isControlDown", Types.BOOLEAN, e.isControlDown());
		addVariable(variables, NAMESPACE, "isShiftDown", Types.BOOLEAN, e.isShiftDown());
	}

	public void exportConstants(Node node) throws ExecuteException {
		node.addVariable(new Variable(NAMESPACE, "BUTTON", Types.INT, 0)).getValue().setValue(0, Types.INT);
		node.addVariable(new Variable(NAMESPACE, "LABEL", Types.INT, 0)).getValue().setValue(1, Types.INT);
		node.addVariable(new Variable(NAMESPACE, "TEXTFIELD", Types.INT, 0)).getValue().setValue(2, Types.INT);
		node.addVariable(new Variable(NAMESPACE, "TEXTAREA", Types.INT, 0)).getValue().setValue(3, Types.INT);
	}

	private void addVariable(Map<String, Variable> variables, String namespace, String name, int type, Object value) throws ExecuteException {
		Variable v = new Variable(namespace, name, type, 0);
		ValueContainer vc = v.getValue();
		vc.setValue(value, type);
		variables.put(name, v);
	}
}
