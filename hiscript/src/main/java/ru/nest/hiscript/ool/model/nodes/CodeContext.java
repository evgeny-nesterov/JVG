package ru.nest.hiscript.ool.model.nodes;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.nest.hiscript.ool.model.Clazz;
import ru.nest.hiscript.ool.model.Codable;
import ru.nest.hiscript.ool.model.Type;

public class CodeContext {
	public CodeContext() {
		this(null, null);
	}

	public CodeContext(CodeContext parent, Clazz clazz) {
		this.parent = parent;
		this.clazz = clazz;
	}

	private Clazz clazz;

	private CodeContext parent;

	public CodeContext getRoot() {
		CodeContext ctx = this;
		while (ctx.parent != null) {
			ctx = ctx.parent;
		}
		return ctx;
	}

	private ByteArrayOutputStream bos = new ByteArrayOutputStream();

	private DataOutputStream dos = new DataOutputStream(bos);

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
	public void writeNullable(Codable object) throws IOException {
		writeBoolean(object != null);
		if (object != null) {
			object.code(this);
		}
	}

	public <N extends Codable> void write(N[] objects) throws IOException {
		if (objects != null) {
			for (int i = 0; i < objects.length; i++)
				objects[i].code(this);
		}
	}

	public <N extends Codable> void write(List<N> objects) throws IOException {
		if (objects != null) {
			for (int i = 0; i < objects.size(); i++)
				objects.get(i).code(this);
		}
	}

	public <N extends Codable> void write(N object) throws IOException {
		object.code(this);
	}

	public <N extends Codable> void writeNullable(List<N> objects) throws IOException {
		if (objects != null) {
			for (int i = 0; i < objects.size(); i++)
				writeNullable(objects.get(i));
		}
	}

	public <N extends Codable> void writeNullable(N[] objects) throws IOException {
		if (objects != null) {
			for (int i = 0; i < objects.length; i++)
				writeNullable(objects[i]);
		}
	}

	// ============================================================================
	private int len_utf = 0;

	private Map<String, Integer> stringsHash = new HashMap<String, Integer>();

	private List<String> strings = new ArrayList<String>();

	private int getUTFIndex(String value) throws IOException {
		CodeContext ctx = getRoot();

		int index;
		if (ctx.stringsHash.containsKey(value)) {
			index = ctx.stringsHash.get(value);
		} else {
			index = ctx.stringsHash.size();
			ctx.stringsHash.put(value, index);
			ctx.strings.add(value);

			// DEBUG
			// System.out.println("string: '" + value + "', index = " + index);
		}

		return index;
	}

	public byte[] getStringsCode() throws IOException {
		ByteArrayOutputStream bos_string = new ByteArrayOutputStream();
		DataOutputStream dos_string = new DataOutputStream(bos_string);

		dos_string.writeShort(stringsHash.size());
		for (String s : strings) {
			dos_string.writeUTF(s);
			len_utf += s.length();
		}

		return bos_string.toByteArray();
	}

	public void writeUTF(String value) throws IOException {
		int index = getUTFIndex(value);
		writeShort(index);
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

	// ============================================================================
	private Map<Type, Integer> typesHash = new HashMap<Type, Integer>();

	private List<Type> types = new ArrayList<Type>();

	public void writeType(Type type) throws IOException {
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

	public byte[] getTypesCode() throws IOException {
		ByteArrayOutputStream bos_type = new ByteArrayOutputStream();
		DataOutputStream dos_type = new DataOutputStream(bos_type);

		DataOutputStream oldDos = dos;
		dos = dos_type;

		Collections.sort(types, new Comparator<Type>() {
			@Override
			public int compare(Type t1, Type t2) {
				int type1 = t1.getType();
				int type2 = t2.getType();
				if (type1 != type2) {
					return type1 - type2;
				}

				if (type1 == Type.ARRAY && t1.getDimension() != t2.getDimension()) {
					return t1.getDimension() - t2.getDimension();
				}

				int index1 = typesHash.get(t1);
				int index2 = typesHash.get(t2);
				return index1 - index2;
			}
		});

		writeShort(types.size());
		for (Type type : types) {
			writeShort(typesHash.get(type));
			write(type);

			// DEBUG
			// System.out.println("write type: " + type + ", index=" +
			// typesHash.get(type));
		}

		dos = oldDos;
		return bos_type.toByteArray();
	}

	// ============================================================================
	private HashMap<Clazz, Integer> classes = new HashMap<Clazz, Integer>();

	private HashMap<Integer, Clazz> index_to_classes = new HashMap<Integer, Clazz>();

	public void writeClass(Clazz clazz) throws IOException {
		CodeContext ctx = getRoot();

		int index = -1;
		if (ctx.classes.containsKey(clazz)) {
			index = ctx.classes.get(clazz);
		} else {
			index = ctx.classes.size();
			ctx.classes.put(clazz, index);
			ctx.index_to_classes.put(index, clazz);
		}

		boolean isHasIndex = index != -1;
		writeBoolean(isHasIndex);
		if (isHasIndex) {
			writeShort(index);
		} else {
			writeUTF(clazz.fullName);
		}
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

		System.out.println("utf count: " + stringsHash.size());
		System.out.println("types count: " + typesHash.size());
		System.out.println("classes count: " + classes.size());
	}

	public int getClassesCount() {
		return classes.size();
	}

	public ClassCodeContext getClassContext(int index) throws IOException {
		ClassCodeContext classContext = new ClassCodeContext();
		classContext.clazz = index_to_classes.get(index);
		classContext.ctx = new CodeContext(this, classContext.clazz);
		classContext.clazz.code(classContext.ctx);
		classContext.code = classContext.ctx.code();
		return classContext;
	}

	public List<ClassCodeContext> getClassesCode() throws IOException {
		List<ClassCodeContext> list = new ArrayList<ClassCodeContext>();
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
		public Clazz clazz;

		public CodeContext ctx;

		public byte[] code;
	}

	public byte[] code() throws IOException {
		// code classes and collect strings and types
		List<ClassCodeContext> classesCode = getClassesCode();

		ByteArrayOutputStream bos_all = new ByteArrayOutputStream();
		DataOutputStream dos_all = new DataOutputStream(bos_all);

		if (clazz == null) {
			// code types and collect strings
			byte[] typesCode = getTypesCode();

			// write strings
			byte[] stringsCode = getStringsCode();
			dos_all.write(stringsCode);

			// write types
			dos_all.write(typesCode);

			// write classes
			dos_all.writeShort(classesCode.size());
			for (ClassCodeContext classCode : classesCode) {
				dos_all.write(classCode.code);
			}

			// DEBUG
			// System.out.println("types count: " + typesHash.size());
		}

		// body data
		dos_all.write(bos.toByteArray());

		// DEBUG
		// System.out.println((clazz != null ? (clazz.fullName + ": ") : "") +
		// "size=" + bos_all.size());
		return bos_all.toByteArray();
	}
}
