package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.model.ClassLoadListener;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiClassLoader;
import ru.nest.hiscript.ool.model.HiConstructor;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.HiNoClassException;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.HiNodeIF;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.TypeArgumentIF;
import ru.nest.hiscript.ool.model.TypeVarargs;
import ru.nest.hiscript.ool.runtime.HiRuntimeEnvironment;
import ru.nest.hiscript.ool.runtime.HiScriptRuntimeException;
import ru.nest.hiscript.tokenizer.Token;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DecodeContext {
	private final DecodeContext parent;

	private final HiClassLoader classLoader;

	private final DataInputStream is;

	private HiClass clazz;

	public void setHiClass(HiClass clazz) {
		this.clazz = clazz;
	}

	public HiClass getHiClass() {
		return clazz;
	}

	public DecodeContext(HiClassLoader classLoader, byte[] data) throws IOException {
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

	public HiRuntimeEnvironment getEnv() {
		return classLoader.getEnv();
	}

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

	public <E extends Enum> E readEnum(Class<E> enumClass) throws IOException {
		len_byte += 1;
		int ordinal = is.readByte();
		if (ordinal != -1) {
			return (E) enumClass.getEnumConstants()[ordinal];
		} else {
			return null;
		}
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

	private void loadStrings() throws IOException {
		int count = is.readShort();
		strings = new String[count];
		for (int index = 0; index < count; index++) {
			strings[index] = is.readUTF();
		}
	}

	public String readUTF() throws IOException {
		return getUTF(readShort());
	}

	public Class<?> readJavaClass() throws IOException {
		try {
			String className = readUTF();
			switch (className) {
				case "byte":
					return byte.class;
				case "short":
					return short.class;
				case "char":
					return char.class;
				case "int":
					return int.class;
				case "long":
					return long.class;
				case "float":
					return float.class;
				case "double":
					return double.class;
				case "boolean":
					return boolean.class;
				default:
					return Class.forName(className);
			}
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
	}

	public String[] readUTFArray(int size) throws IOException {
		String[] array = new String[size];
		for (int i = 0; i < size; i++) {
			array[i] = readUTF();
		}
		return array;
	}

	public String readNullableUTF() throws IOException {
		return readBoolean() ? getUTF(readShort()) : null;
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
		int count = readShort();
		types = new Type[count];
		int[] indexes = new int[count];
		for (int i = 0; i < count; i++) {
			int index = readShort();
			Type type = Type.decode(this);
			types[index] = type;
			indexes[i] = index;
		}
	}

	public Type readType() throws IOException {
		return getType(readShort());
	}

	public TypeArgumentIF readTypeArgument() throws IOException {
		Type type = getType(readShort());
		if (type.isArray() && readBoolean()) {
			return new TypeVarargs(type);
		} else {
			return type;
		}
	}

	public Type[] readTypes() throws IOException {
		int count = readByte();
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
		if (index == -1) {
			return null;
		}
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

	private final Map<String, List<ClassLoadListener>> classByNameLoadListeners = new HashMap<>();

	public void addClassLoadListener(ClassLoadListener<HiClass> listener, int index) {
		DecodeContext ctx = getRoot();
		List<ClassLoadListener> list = ctx.classLoadListeners.computeIfAbsent(index, k -> new ArrayList<>());
		list.add(listener);
	}

	public void addClassLoadListener(ClassLoadListener<HiClass> listener, String name) {
		DecodeContext ctx = getRoot();
		List<ClassLoadListener> list = ctx.classByNameLoadListeners.computeIfAbsent(name, k -> new ArrayList<>());
		list.add(listener);
	}

	private HiClass[] classes;

	public void loadClasses() throws IOException {
		int count = is.readShort();
		classes = new HiClass[count];
		for (int index = 0; index < count; index++) {
			DecodeContext ctx = new DecodeContext(classLoader, is, this);
			HiClass clazz = HiClass.decode(ctx);
			classes[index] = clazz;
			fireClassLoaded(clazz, index);
		}
		while (!classLoadListeners.isEmpty()) {
			Map<Integer, List<ClassLoadListener>> classLoadListeners = new HashMap<>(this.classLoadListeners);
			this.classLoadListeners.clear();
			for (int index : classLoadListeners.keySet()) {
				List<ClassLoadListener> list = classLoadListeners.get(index);
				HiClass clazz = classes[index];
				for (ClassLoadListener listener : list) {
					listener.classLoaded(clazz);
				}
			}
		}
	}

	public void fireClassLoaded(HiClass clazz, int index) {
		// Attention! Class may be yet not initialized fully.
		List<ClassLoadListener> listeners = classLoadListeners.get(index);
		if (listeners != null) {
			for (ClassLoadListener l : listeners) {
				l.classLoaded(clazz);
			}
		}
		listeners = classByNameLoadListeners.get(clazz.getClassName());
		if (listeners != null) {
			for (ClassLoadListener l : listeners) {
				l.classLoaded(clazz);
			}
		}
	}

	/**
	 * Read class or receive HiNoClassException.
	 * If HiNoClassException occur listen class loaded and update class value on event:
	 * try {
	 * HiClass clazz = os.readClass();
	 * ... use clazz ...
	 * } catch (HiNoClassException exc) {
	 * os.addClassLoadListener(clazz -> ... use clazz ..., exc.getIndex());
	 * }
	 */
	public HiClass readClass() throws IOException, HiNoClassException {
		int classIndex = readShort();
		if (classIndex != -1) {
			return getClass(classIndex);
		} else {
			return null;
		}
	}

	public void readClass(ClassLoadListener<HiClass> callback) throws IOException, HiNoClassException {
		try {
			HiClass clazz = readClass();
			if (clazz != null) {
				callback.classLoaded(clazz);
			}
		} catch (HiNoClassException exc) {
			addClassLoadListener(callback, exc.getIndex());
		}
	}

	public HiClass[] readClasses() throws IOException, HiNoClassException {
		int size = readShort();
		HiClass[] classes = new HiClass[size];
		for (int i = 0; i < size; i++) {
			try {
				classes[i] = readClass();
			} catch (HiNoClassException exc) {
				final int index = i;
				addClassLoadListener(clazz -> classes[index] = clazz, exc.getIndex());
			}
		}
		return classes;
	}

	public HiClass getClass(int index) throws HiNoClassException {
		DecodeContext ctx = getRoot();
		if (ctx != this) {
			return ctx.getClass(index);
		} else {
			if (index >= 0 && classes != null && index < classes.length) {
				HiClass clazz = classes[index];
				if (clazz == null || clazz.fullName == null) {
					throw new HiNoClassException(index);
				}
				return clazz;
			} else {
				throw new HiScriptRuntimeException("invalid class index " + index + " (max is " + (classes != null ? (classes.length - 1) : 0) + ")");
			}
		}
	}

	public void readConstructor(ClassLoadListener<HiConstructor> callback) throws IOException {
		HiConstructor.decodeLink(this, callback);
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
			nodes[i] = (N) read(type.isInterface() ? (Class<N>) HiNode.class : type);
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

	public <N> N read(Class<N> type) throws IOException {
		try {
			if (type == HiNodeIF.class || HiNode.class.isAssignableFrom(type) || HiMethod.class == type) {
				return (N) HiNode.decode(this);
			} else {
				Method m = type.getMethod("decode", DecodeContext.class);
				return (N) m.invoke(type, this);
			}
		} catch (IOException exc) {
			throw exc;
		} catch (Exception exc) {
			throw new IOException("cannot decode for " + type, exc);
		}
	}

	public Object readObject() throws IOException {
		int type = readByte();
		switch (type) {
			case -1:
				return null;
			case 1:
				return readUTF();
			case 2:
				return readBoolean();
			case 3:
				return readInt();
			case 4:
				return readByte();
			case 5:
				return readShort();
			case 6:
				return readChar();
			case 7:
				return readLong();
			case 8:
				return readDouble();
			case 9:
				return readFloat();
			case 10:
				return HiNode.decode(this);
			case 11:
				int size = readInt();
				byte[] bytes = new byte[size];
				is.read(bytes);
				ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(bytes));
				try {
					return is.readObject();
				} catch (ClassNotFoundException e) {
					throw new IOException(e);
				}
		}
		throw new IOException("undefined object type: " + type);
	}

	public void prepare() throws IOException {
		loadStrings();
		loadTypes();
		loadClasses();
	}

	public HiNode loadNode() throws IOException {
		prepare();
		return read(HiNode.class);
	}
}
