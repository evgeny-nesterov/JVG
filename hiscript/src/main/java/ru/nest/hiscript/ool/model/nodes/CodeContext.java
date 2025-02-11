package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.model.Codeable;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiNodeIF;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.TypeArgumentIF;
import ru.nest.hiscript.tokenizer.Token;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeContext {
	public CodeContext() throws IOException {
		this(null, null);
	}

	public CodeContext(CodeContext parent, HiClass clazz) throws IOException {
		this.parent = parent;
		this.clazz = clazz;
		this.bos = new ByteArrayOutputStream();
		this.dos = new DataOutputStream(bos);
	}

	private final HiClass clazz;

	private final CodeContext parent;

	public CodeContext getRoot() {
		CodeContext ctx = this;
		while (ctx.parent != null) {
			ctx = ctx.parent;
		}
		return ctx;
	}

	private final ByteArrayOutputStream bos;

	private DataOutputStream dos;

	private int len_boolean = 0;

	public void writeBoolean(boolean value) throws IOException {
		dos.writeBoolean(value);
		len_boolean += 1;
	}

	private int len_byte = 0;

	public void writeByte(int value) throws IOException {
		dos.writeByte(value);
		len_byte += 1;
	}

	private int len_char = 0;

	public void writeChar(int value) throws IOException {
		dos.writeChar(value);
		len_char += 2;
	}

	private int len_double = 0;

	public void writeDouble(double value) throws IOException {
		dos.writeDouble(value);
		len_double += 8;
	}

	private int len_float = 0;

	public void writeFloat(float value) throws IOException {
		dos.writeFloat(value);
		len_float += 4;
	}

	private int len_int = 0;

	public void writeInt(int value) throws IOException {
		dos.writeInt(value);
		len_int += 4;
	}

	private int len_long = 0;

	public void writeLong(long value) throws IOException {
		dos.writeLong(value);
		len_long += 8;
	}

	private int len_short = 0;

	public void writeShort(int value) throws IOException {
		dos.writeShort(value);
		len_short += 2;
	}

	// ============================================================================
	public void writeToken(Token token) throws IOException {
		// TODO write tokens optionally
		if (token != null) {
			token.code(this);
		} else {
			writeInt(-1); // line
		}
	}

	public void writeNullable(Codeable object) throws IOException {
		writeBoolean(object != null);
		if (object != null) {
			object.code(this);
		}
	}

	public <N extends Codeable> void writeArray(N[] objects) throws IOException {
		if (objects != null) {
			for (int i = 0; i < objects.length; i++) {
				objects[i].code(this);
			}
		}
	}

	public <N extends Codeable> void writeShortArray(N[] objects) throws IOException {
		writeShort(objects != null ? objects.length : 0);
		writeArray(objects);
	}

	public <N extends Codeable> void write(List<N> objects) throws IOException {
		if (objects != null) {
			for (int i = 0; i < objects.size(); i++) {
				objects.get(i).code(this);
			}
		}
	}

	public <N extends Codeable> void write(N object) throws IOException {
		object.code(this);
	}

	public <N extends Codeable> void writeNullable(List<N> objects) throws IOException {
		if (objects != null) {
			for (int i = 0; i < objects.size(); i++) {
				writeNullable(objects.get(i));
			}
		}
	}

	public <N extends Codeable> void writeArraysNullable(List<N[]> objectsArrays) throws IOException {
		if (objectsArrays != null) {
			for (int i = 0; i < objectsArrays.size(); i++) {
				N[] objects = objectsArrays.get(i);
				writeShort(objects != null ? objects.length : 0);
				writeNullable(objects);
			}
		}
	}

	/**
	 * Write size (byte, short or int) and only then write array
	 */
	public <N extends Codeable> void writeNullable(N[] objects) throws IOException {
		if (objects != null) {
			for (int i = 0; i < objects.length; i++) {
				writeNullable(objects[i]);
			}
		}
	}

	// ============================================================================
	private int len_utf = 0;

	private final Map<String, Integer> stringsHash = new HashMap<>();

	private final List<String> strings = new ArrayList<>();

	private int getUTFIndex(String value) {
		CodeContext ctx = getRoot();
		int index;
		if (ctx.stringsHash.containsKey(value)) {
			index = ctx.stringsHash.get(value);
		} else {
			index = ctx.stringsHash.size();
			ctx.stringsHash.put(value, index);
			ctx.strings.add(value);
		}
		return index;
	}

	public byte[] getStringsCode() throws IOException {
		ByteArrayOutputStream bos_string = new ByteArrayOutputStream();
		try (DataOutputStream dos_string = new DataOutputStream(bos_string)) {
			dos_string.writeShort(stringsHash.size());
			for (String s : strings) {
				dos_string.writeUTF(s);
				len_utf += s.length();
			}
		}
		return bos_string.toByteArray();
	}

	public void writeUTF(String value) throws IOException {
		int index = getUTFIndex(value);
		writeShort(index);
	}

	public void writeJavaClass(Class<?> javaClass) throws IOException {
		writeUTF(javaClass.getName());
	}

	public void writeNullableUTF(String value) throws IOException {
		writeBoolean(value != null);
		if (value != null) {
			writeUTF(value);
		}
	}

	public void writeUTFArray(String[] array) throws IOException {
		if (array != null) {
			for (int i = 0; i < array.length; i++) {
				writeUTF(array[i]);
			}
		}
	}

	private int len_object = 0;

	public void writeObject(Object value) throws IOException {
		int size = bos.size();
		if (value == null) {
			writeByte(-1);
		} else if (value instanceof String) {
			int index = getUTFIndex((String) value);
			writeByte(1);
			writeShort(index);
		} else if (value instanceof Boolean) {
			writeByte(2);
			writeBoolean((Boolean) value);
		} else if (value instanceof Integer) {
			writeByte(3);
			writeInt((Integer) value);
		} else if (value instanceof Byte) {
			writeByte(4);
			writeByte((Byte) value);
		} else if (value instanceof Short) {
			writeByte(5);
			writeShort((Short) value);
		} else if (value instanceof Character) {
			writeByte(6);
			writeChar((Character) value);
		} else if (value instanceof Long) {
			writeByte(7);
			writeLong((Long) value);
		} else if (value instanceof Double) {
			writeByte(8);
			writeDouble((Double) value);
		} else if (value instanceof Float) {
			writeByte(9);
			writeFloat((Float) value);
		} else if (value instanceof HiNodeIF) {
			HiNodeIF node = (HiNodeIF) value;
			writeByte(10);
			node.code(this);
		} else if (value instanceof Serializable) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(value);
			oos.close();
			byte[] bytes = bos.toByteArray();
			dos.write(11);
			dos.writeInt(bytes.length);
			dos.write(bytes);
		} else {
			throw new IOException("not serializable class: " + value);
		}
		len_object += bos.size() - size;
	}

	// ============================================================================
	private final Map<Type, Integer> typesHash = new HashMap<>();

	private final List<Type> types = new ArrayList<>();

	public void writeType(Type type) throws IOException {
		if (type == null) {
			writeShort(-1);
			return;
		}
		CodeContext ctx = getRoot();
		int index;
		if (ctx.typesHash.containsKey(type)) {
			index = ctx.typesHash.get(type);
		} else {
			index = ctx.typesHash.size();
			ctx.typesHash.put(type, index);
			ctx.types.add(type);
		}
		writeShort(index);
	}

	public void writeTypeArgument(TypeArgumentIF typeArgument) throws IOException {
		writeType(typeArgument.getType());
		if (typeArgument.isArray()) {
			writeBoolean(typeArgument.isVarargs());
		}
	}

	public void writeTypes(Type[] types) throws IOException {
		if (types != null) {
			writeByte(types.length);
			for (int i = 0; i < types.length; i++) {
				writeType(types[i]);
			}
		} else {
			writeByte(0);
		}
	}

	public byte[] getTypesCode() throws IOException {
		DataOutputStream oldDos = dos;
		ByteArrayOutputStream bos_type = new ByteArrayOutputStream();
		try (DataOutputStream dos_type = new DataOutputStream(bos_type)) {
			dos = dos_type;
			for (int i = 0; i < types.size(); i++) {
				Type type = types.get(i);
				if (type.parameters != null && type.parameters.length > 0) {
					for (Type p : type.parameters) {
						if (!typesHash.containsKey(p)) {
							int index = typesHash.size();
							typesHash.put(p, index);
							types.add(p);
						}
					}
				}
				while (type != null) {
					if (!typesHash.containsKey(type)) {
						int index = typesHash.size();
						typesHash.put(type, index);
						types.add(type);
					}
					type = type.cellType;
				}
			}

			Collections.sort(types, (t1, t2) -> {
				int type1 = t1.getTypeClass();
				int type2 = t2.getTypeClass();
				if (type1 != type2) {
					return type1 - type2;
				}

				if (type1 == Type.ARRAY && t1.getDimension() != t2.getDimension()) {
					return t1.getDimension() - t2.getDimension();
				}

				if (type1 == Type.OBJECT) {
					if (t1.parameters == null && t2.parameters != null) {
						return -1;
					} else if (t1.parameters != null && t2.parameters == null) {
						return -1;
					}
				}

				int index1 = typesHash.get(t1);
				int index2 = typesHash.get(t2);
				return index1 - index2;
			});

			writeShort(types.size());
			for (int i = 0; i < types.size(); i++) {
				Type type = types.get(i);
				writeShort(typesHash.get(type));
				write(type);
			}
		} finally {
			dos = oldDos;
		}
		return bos_type.toByteArray();
	}

	// ============================================================================
	private final Map<Integer, Integer> classes = new HashMap<>();

	private final Map<Integer, HiClass> indexToClasses = new HashMap<>();

	public void writeClass(HiClass clazz) throws IOException {
		if (clazz != null) {
			writeShort(registerClass(clazz));
		} else {
			writeShort(-1);
		}
	}

	public void writeClasses(HiClass... classes) throws IOException {
		writeShort(classes != null ? classes.length : 0);
		if (classes != null) {
			for (HiClass clazz : classes) {
				writeClass(clazz);
			}
		}
	}

	private int registerClass(HiClass clazz) {
		if (!clazz.isVar()) {
			if (clazz.isArray()) {
				registerClass(clazz.getArrayType());
			}
			if (clazz.isPrimitive()) {
				_registerClass(clazz.getAutoboxClass());
			} else if (clazz.getAutoboxedPrimitiveClass() != null) {
				_registerClass(clazz.getAutoboxedPrimitiveClass());
			}
		}
		return _registerClass(clazz);
	}

	private int _registerClass(HiClass clazz) {
		CodeContext ctx = getRoot();
		int classId = System.identityHashCode(clazz); // classes may have identical names
		int index = ctx.classes.computeIfAbsent(classId, k -> ctx.classes.size());
		ctx.indexToClasses.put(index, clazz);
		return index;
	}

	public void statistics() {
		System.out.println("boolean: " + len_boolean);
		System.out.println("byte: " + len_byte);
		System.out.println("char: " + len_char);
		System.out.println("short: " + len_short);
		System.out.println("int: " + len_int);
		System.out.println("long: " + len_long);
		System.out.println("float: " + len_float);
		System.out.println("double: " + len_double);
		System.out.println("utf: " + len_utf);
		System.out.println("object: " + len_object);

		System.out.println("utf count: " + stringsHash.size());
		System.out.println("types count: " + typesHash.size());
		System.out.println("classes count: " + classes.size());
	}

	public ClassCodeContext getClassContext(int index) throws IOException {
		ClassCodeContext classContext = new ClassCodeContext();
		classContext.clazz = indexToClasses.get(index);
		classContext.ctx = new CodeContext(this, classContext.clazz);
		classContext.ctx.writeShort(index);
		classContext.clazz.code(classContext.ctx);
		classContext.code = classContext.ctx.code();
		return classContext;
	}

	public List<ClassCodeContext> getClassesCode() throws IOException {
		List<ClassCodeContext> list = new ArrayList<>();
		getClassesCode(list);
		return list;
	}

	private void getClassesCode(List<ClassCodeContext> list) throws IOException {
		for (int i = 0; i < classes.size(); i++) {
			ClassCodeContext classContext = getClassContext(i);
			list.add(classContext);
		}
	}

	public static class ClassCodeContext {
		public HiClass clazz;

		public CodeContext ctx;

		public byte[] code;
	}

	public byte[] code() throws IOException {
		if (clazz == null) {
			ByteArrayOutputStream bos_all = new ByteArrayOutputStream();
			try (DataOutputStream dos_all = new DataOutputStream(bos_all)) {
				// code classes and collect strings and types
				List<ClassCodeContext> classesCode = getClassesCode();

				// code types and collect strings
				byte[] typesCode = getTypesCode();

				// write strings
				byte[] stringsCode = getStringsCode();
				bos_all.write(stringsCode);

				// write types
				bos_all.write(typesCode);

				// write classes (class with main method too)
				dos_all.writeShort(classesCode.size());
				for (ClassCodeContext classCode : classesCode) {
					dos_all.write(classCode.code);
				}

				// body data
				bos_all.write(bos.toByteArray());
			}
			return bos_all.toByteArray();
		} else {
			return bos.toByteArray();
		}
	}
}
