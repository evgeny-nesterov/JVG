package ru.nest.hiscript.ool.model.nodes;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.nest.hiscript.ool.model.ClassLoadListener;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.NoClassException;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.Type;

public class DecodeContext {
	private DataInputStream is;

	private HiClass clazz;

	public void setHiClass(HiClass clazz) {
		this.clazz = clazz;
	}

	public HiClass getHiClass() {
		return clazz;
	}

	public DecodeContext(byte[] data) {
		this(new DataInputStream(new ByteArrayInputStream(data)));
	}

	public DecodeContext(DataInputStream is) {
		this(is, null);
	}

	public DecodeContext(DataInputStream is, DecodeContext parent) {
		this.is = is;
		this.parent = parent;
	}

	private DecodeContext parent;

	public DecodeContext getRoot() {
		DecodeContext ctx = this;
		while (ctx.parent != null) {
			ctx = ctx.parent;
		}
		return ctx;
	}

	private int len_boolean = 0;

	public boolean readBoolean() throws IOException {
		len_boolean += 1;
		return is.readBoolean();
	}

	private int len_byte = 0;

	public byte readByte() throws IOException {
		len_byte += 1;
		return is.readByte();
	}

	private int len_char = 0;

	public char readChar() throws IOException {
		len_char += 2;
		return is.readChar();
	}

	private int len_double = 0;

	public double readDouble() throws IOException {
		len_double += 8;
		return is.readDouble();
	}

	private int len_float = 0;

	public float readFloat() throws IOException {
		len_float += 4;
		return is.readFloat();
	}

	private int len_int = 0;

	public int readInt() throws IOException {
		len_int += 4;
		return is.readInt();
	}

	private int len_long = 0;

	public long readLong() throws IOException {
		len_long += 8;
		return is.readLong();
	}

	private int len_short = 0;

	public short readShort() throws IOException {
		len_short += 2;
		return is.readShort();
	}

	private String[] strings;

	private void loadUTF() throws IOException {
		int count = is.readShort();
		strings = new String[count];
		for (int index = 0; index < count; index++) {
			strings[index] = is.readUTF();
		}
	}

	public String readUTF() throws IOException {
		return getUTF(is.readShort());
	}

	public String[] readUTFArray(int size) throws IOException {
		String[] array = new String[size];
		for (int i = 0; i < size; i++) {
			array[i] = readUTF();
		}
		return array;
	}

	public String readNullableUTF() throws IOException {
		return readBoolean() ? getUTF(is.readShort()) : null;
	}

	public String getUTF(int index) {
		DecodeContext ctx = getRoot();
		if (ctx != this) {
			return ctx.getUTF(index);
		} else {
			if (index >= 0 && strings != null && index < strings.length) {
				return strings[index];
			} else {
				throw new RuntimeException("invalid string index " + index + " (max " + (strings != null ? (strings.length - 1) : 0) + ")");
			}
		}
	}

	private Type[] types;

	public void loadTypes() throws IOException {
		int count = is.readShort();
		types = new Type[count];
		for (int i = 0; i < count; i++) {
			int index = readShort();
			types[index] = Type.decode(this);

			// DEBUG
			// System.out.println(index + ": " + types[index].name);
		}
	}

	public Type readType() throws IOException {
		return getType(is.readShort());
	}

	public Type getType(int index) {
		DecodeContext ctx = getRoot();
		if (ctx != this) {
			return ctx.getType(index);
		} else {
			if (index >= 0 && types != null && index < types.length) {
				return types[index];
			} else {
				throw new RuntimeException("invalid type index " + index + " (max " + (types != null ? (types.length - 1) : 0) + ")");
			}
		}
	}

	private Map<Integer, List<ClassLoadListener>> classLoadListeners = new HashMap<Integer, List<ClassLoadListener>>();

	public void addClassLoadListener(ClassLoadListener listener, int index) {
		DecodeContext ctx = getRoot();
		List<ClassLoadListener> list = ctx.classLoadListeners.get(index);
		if (list == null) {
			list = new ArrayList<ClassLoadListener>();
			ctx.classLoadListeners.put(index, list);
		}
		list.add(listener);
	}

	protected void fireClassLoaded(HiClass clazz, int index) {
		DecodeContext ctx = getRoot();
		List<ClassLoadListener> list = ctx.classLoadListeners.get(index);
		if (list != null) {
			for (ClassLoadListener listener : list) {
				listener.classLoaded(clazz);
			}
			ctx.classLoadListeners.remove(index);
		}
	}

	private HiClass[] classes;

	public void loadClasses() throws IOException {
		int count = is.readShort();
		classes = new HiClass[count];
		for (int index = 0; index < count; index++) {
			DecodeContext ctx = new DecodeContext(is, this);
			classes[index] = HiClass.decode(ctx);

			// DEBUG
			// System.out.println("class loaded: " + classes[index].fullName);

			// fire event
			fireClassLoaded(classes[index], index);
		}
	}

	public HiClass readClass() throws IOException, NoClassException {
		boolean isHasIndex = is.readBoolean();
		HiClass clazz = null;
		if (isHasIndex) {
			int clazzIndex = is.readShort();

			// DEBUG
			// System.out.println("read class: " + clazzIndex);

			clazz = getClass(clazzIndex);
		} else {
			String classFullName = is.readUTF();
			clazz = HiClass.forName(null, classFullName);
		}

		return clazz;
	}

	public HiClass getClass(int index) throws NoClassException {
		DecodeContext ctx = getRoot();
		if (ctx != this) {
			return ctx.getClass(index);
		} else {
			if (index >= 0 && classes != null && index < classes.length) {
				if (classes[index] == null) {
					throw new NoClassException(index);
				}
				return classes[index];
			} else {
				throw new RuntimeException("invalid class index " + index + " (max is " + (classes != null ? (classes.length - 1) : 0) + ")");
			}
		}
	}

	public <N> List<N> readList(Class<N> type, int size) throws IOException {
		List<N> list = new ArrayList<N>(size);
		for (int i = 0; i < size; i++) {
			list.add(read(type));
		}
		return list;
	}

	public <N> N[] readArray(Class<N> type, int size) throws IOException {
		N[] nodes = (N[]) Array.newInstance(type, size);
		for (int i = 0; i < size; i++) {
			nodes[i] = read(type);
		}
		return nodes;
	}

	public <N> N[] readNodeArray(Class<N> type, int size) throws IOException {
		N[] nodes = (N[]) Array.newInstance(type, size);
		for (int i = 0; i < size; i++) {
			nodes[i] = (N) read(Node.class);
		}
		return nodes;
	}

	public <N> List<N> readNullableList(Class<N> type, int size) throws IOException {
		List<N> list = new ArrayList<N>(size);
		for (int i = 0; i < size; i++) {
			list.add(readNullable(type));
		}
		return list;
	}

	public <N> N[] readNullableArray(Class<N> type, int size) throws IOException {
		N[] nodes = (N[]) Array.newInstance(type, size);
		for (int i = 0; i < size; i++) {
			nodes[i] = readNullable(type);
		}
		return nodes;
	}

	public <N> N[] readNullableNodeArray(Class<N> type, int size) throws IOException {
		N[] nodes = (N[]) Array.newInstance(type, size);
		for (int i = 0; i < size; i++) {
			nodes[i] = (N) readNullable(Node.class);
		}
		return nodes;
	}

	public <N> N readNullable(Class<N> type) throws IOException {
		boolean isNotNull = readBoolean();
		if (isNotNull) {
			return read(type);
		} else {
			return null;
		}
	}

	public <N> N read(Class<N> type) throws IOException {
		try {
			Method m = type.getMethod("decode", DecodeContext.class);
			return (N) m.invoke(type, this);
		} catch (Exception exc) {
			throw new RuntimeException("Can't decode for " + type, exc);
		}
	}

	public Node load() throws IOException {
		loadUTF();
		loadTypes();
		loadClasses();
		return read(Node.class);
	}
}
