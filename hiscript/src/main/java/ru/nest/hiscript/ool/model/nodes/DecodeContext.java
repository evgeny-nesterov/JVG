package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.HiScriptRuntimeException;
import ru.nest.hiscript.ool.model.ClassLoadListener;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiClassLoader;
import ru.nest.hiscript.ool.model.HiNoClassException;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.TypeArgumentIF;
import ru.nest.hiscript.ool.model.TypeVarargs;
import ru.nest.hiscript.tokenizer.Token;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DecodeContext {
	private final DataInputStream is;

	private HiClass clazz;

	public void setHiClass(HiClass clazz) {
		this.clazz = clazz;
	}

	public HiClass getHiClass() {
		return clazz;
	}

	public DecodeContext(HiClassLoader classLoader, byte[] data) {
		this(classLoader, new DataInputStream(new ByteArrayInputStream(data)));
	}

	public DecodeContext(HiClassLoader classLoader, DataInputStream is) {
		this(classLoader, is, null);
	}

	public DecodeContext(HiClassLoader classLoader, DataInputStream is, DecodeContext parent) {
		this.classLoader = classLoader;
		this.is = is;
		this.parent = parent;
	}

	private final DecodeContext parent;

	private final HiClassLoader classLoader;

	public HiClassLoader getClassLoader() {
		return classLoader;
	}

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
				throw new HiScriptRuntimeException("invalid string index " + index + " (max " + (strings != null ? (strings.length - 1) : 0) + ")");
			}
		}
	}

	public Token readToken() throws IOException {
		// TODO write tokens optionally
		return Token.decode(this);
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

	public TypeArgumentIF readTypeArgument() throws IOException {
		Type type = getType(is.readShort());
		if (type.isArray() && readBoolean()) {
			return new TypeVarargs(type);
		} else {
			return type;
		}
	}

	public Type[] readTypes() throws IOException {
		int count = is.readByte();
		if (count > 0) {
			Type[] types = new Type[count];
			for (int i = 0; i < count; i++) {
				types[i] = readType();
			}
			return types;
		} else {
			return null;
		}
	}

	public Type getType(int index) {
		DecodeContext ctx = getRoot();
		if (ctx != this) {
			return ctx.getType(index);
		} else {
			if (index >= 0 && types != null && index < types.length) {
				return types[index];
			} else {
				throw new HiScriptRuntimeException("invalid type index " + index + " (max " + (types != null ? (types.length - 1) : 0) + ")");
			}
		}
	}

	private final Map<Integer, List<ClassLoadListener>> classLoadListeners = new HashMap<>();

	public void addClassLoadListener(ClassLoadListener listener, int index) {
		DecodeContext ctx = getRoot();
		List<ClassLoadListener> list = ctx.classLoadListeners.computeIfAbsent(index, k -> new ArrayList<>());
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
			DecodeContext ctx = new DecodeContext(classLoader, is, this);
			classes[index] = HiClass.decode(ctx);

			// DEBUG
			// System.out.println("class loaded: " + classes[index].getNameDescr());

			// fire event
			fireClassLoaded(classes[index], index);
		}
	}

	public HiClass readClass() throws IOException, HiNoClassException {
		boolean isHasIndex = is.readBoolean();
		HiClass clazz;
		if (isHasIndex) {
			int clazzIndex = is.readShort();

			// DEBUG
			// System.out.println("read class: " + clazzIndex);

			clazz = getClass(clazzIndex);
		} else {
			String classFullName = is.readUTF();
			clazz = HiClass.forName(/*no context*/ null, classFullName);
		}
		return clazz;
	}

	public HiClass getClass(int index) throws HiNoClassException {
		DecodeContext ctx = getRoot();
		if (ctx != this) {
			return ctx.getClass(index);
		} else {
			if (index >= 0 && classes != null && index < classes.length) {
				if (classes[index] == null) {
					throw new HiNoClassException(index);
				}
				return classes[index];
			} else {
				throw new HiScriptRuntimeException("invalid class index " + index + " (max is " + (classes != null ? (classes.length - 1) : 0) + ")");
			}
		}
	}

	public String[] readStringArray() throws IOException {
		int length = readInt();
		if (length > 0) {
			String[] array = new String[length];
			for (int i = 0; i < length; i++) {
				array[i] = readUTF();
			}
			return array;
		}
		return null;
	}

	public <N> List<N> readList(Class<N> type, int size) throws IOException {
		List<N> list = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			list.add(read(type));
		}
		return list;
	}

	public <N extends HiNode> List<N> readNodesList(Class<N> type, int size) throws IOException {
		List<N> list = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			list.add((N) read(HiNode.class));
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

	public <N> N[] readShortArray(Class<N> type) throws IOException {
		int size = readShort();
		if (size > 0) {
			return readArray(type, size);
		} else {
			return null;
		}
	}

	public <N> N[] readNodeArray(Class<N> type, int size) throws IOException {
		N[] nodes = (N[]) Array.newInstance(type, size);
		for (int i = 0; i < size; i++) {
			nodes[i] = (N) read(HiNode.class);
		}
		return nodes;
	}

	public <N> N[] readShortNodeArray(Class<N> type) throws IOException {
		int size = readShort();
		if (size > 0) {
			return readNodeArray(type, size);
		} else {
			return null;
		}
	}

	public <N> List<N> readNullableList(Class<N> type, int size) throws IOException {
		List<N> list = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			list.add(readNullable(type));
		}
		return list;
	}

	public <N extends HiNode> List<N[]> readNullableListArray(Class<N> type, int size) throws IOException {
		List<N[]> list = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			int arraySize = readShort();
			list.add(readNullableArray(type, arraySize));
		}
		return list;
	}

	public <N> N[] readNullableArray(Class<N> type, int size) throws IOException {
		if (size == 0) {
			return null;
		}
		N[] nodes = (N[]) Array.newInstance(type, size);
		for (int i = 0; i < size; i++) {
			nodes[i] = (N) readNullable(type);
		}
		return nodes;
	}

	public <N extends HiNode> N[] readNullableNodeArray(Class<N> type, int size) throws IOException {
		if (size == 0) {
			return null;
		}
		N[] nodes = (N[]) Array.newInstance(type, size);
		for (int i = 0; i < size; i++) {
			nodes[i] = (N) readNullable(HiNode.class);
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

	public <N> N read(Class<N> type) {
		try {
			Method m = type.getMethod("decode", DecodeContext.class);
			return (N) m.invoke(type, this);
		} catch (Exception exc) {
			throw new HiScriptRuntimeException("cannot decode for " + type, exc);
		}
	}

	public HiNode load() throws IOException {
		loadUTF();
		loadTypes();
		loadClasses();
		return read(HiNode.class);
	}
}
