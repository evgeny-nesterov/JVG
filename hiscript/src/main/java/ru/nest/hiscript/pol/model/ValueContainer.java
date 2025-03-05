package ru.nest.hiscript.pol.model;

import ru.nest.hiscript.tokenizer.SymbolToken;
import ru.nest.hiscript.tokenizer.SymbolType;
import ru.nest.hiscript.tokenizer.WordToken;
import ru.nest.hiscript.tokenizer.Words;

import java.lang.reflect.Array;

public class ValueContainer implements Words {
	public int type;

	public int dimension;

	public boolean isArray() {
		return dimension > 0;
	}

	public char character;

	public String string;

	public boolean bool;

	public byte byteNumber;

	public short shortNumber;

	public int intNumber;

	public float floatNumber;

	public long longNumber;

	public double doubleNumber;

	public Object array;

	public void copy(ValueContainer value) throws ExecuteException {
		if (!value.isArray()) {
			switch (value.type) {
				case VOID:
					break;

				case CHAR:
					value.character = getCharacter();
					break;

				case STRING:
					value.string = getString();
					break;

				case BOOLEAN:
					value.bool = getBoolean();
					break;

				case BYTE:
					value.byteNumber = getByte();
					break;

				case SHORT:
					value.shortNumber = getShort();
					break;

				case INT:
					value.intNumber = getInt();
					break;

				case FLOAT:
					value.floatNumber = getFloat();
					break;

				case LONG:
					value.longNumber = getLong();
					break;

				case DOUBLE:
					value.doubleNumber = getDouble();
					break;
			}
		} else {
			value.array = getArray(value.type, value.dimension);
		}
	}

	public Object getValue() {
		if (!isArray()) {
			switch (type) {
				case VOID:
					return null;

				case CHAR:
					return character;

				case STRING:
					return string;

				case BOOLEAN:
					return bool;

				case BYTE:
					return byteNumber;

				case SHORT:
					return shortNumber;

				case INT:
					return intNumber;

				case FLOAT:
					return floatNumber;

				case LONG:
					return longNumber;

				case DOUBLE:
					return doubleNumber;

				default:
					return null;
			}
		} else {
			return array;
		}
	}

	public void setValue(Object o, int type) throws ExecuteException {
		if (o == null) {
			return;
		}

		dimension = Types.getDimension(o.getClass());
		if (dimension == 0) {
			switch (Types.getType(o.getClass())) {
				case CHAR:
					character = (Character) o;
					this.type = CHAR;
					break;

				case STRING:
					string = (String) o;
					this.type = STRING;
					break;

				case BOOLEAN:
					bool = (Boolean) o;
					this.type = BOOLEAN;
					break;

				case BYTE:
					byteNumber = (Byte) o;
					this.type = BYTE;
					break;

				case SHORT:
					shortNumber = (Short) o;
					this.type = SHORT;
					break;

				case INT:
					intNumber = (Integer) o;
					this.type = INT;
					break;

				case FLOAT:
					floatNumber = (Float) o;
					this.type = FLOAT;
					break;

				case LONG:
					longNumber = (Long) o;
					this.type = LONG;
					break;

				case DOUBLE:
					doubleNumber = (Double) o;
					this.type = DOUBLE;
					break;
			}

			cast(type, 0);
		} else {
			this.type = type;
			array = o;
		}
	}

	char c(char cc) {
		return ++cc;
	}

	public String convertToString() {
		if (!isArray()) {
			switch (type) {
				case CHAR:
					return Character.toString(character);

				case STRING:
					return string;

				case BOOLEAN:
					return Boolean.toString(bool);

				case BYTE:
					return Byte.toString(byteNumber);

				case SHORT:
					return Short.toString(shortNumber);

				case INT:
					return Integer.toString(intNumber);

				case FLOAT:
					return Float.toString(floatNumber);

				case LONG:
					return Long.toString(longNumber);

				case DOUBLE:
					return Double.toString(doubleNumber);

				default:
					return "";
			}
		} else {
			return array != null ? array.toString() : "null";
		}
	}

	public char getCharacter() throws ExecuteException {
		switch (type) {
			case VOID:
			case STRING:
			case BOOLEAN:
				throw new ExecuteException("incompatible types; found " + getTypeDescr() + ", required char");

			case BYTE:
			case SHORT:
			case INT:
			case FLOAT:
			case LONG:
			case DOUBLE:
				throw new ExecuteException("possible loss of precision; found " + getTypeDescr() + ", required char");
		}
		return character;
	}

	public String getString() throws ExecuteException {
		if (type != STRING) {
			throw new ExecuteException("incompatible types; found " + getTypeDescr() + ", required string");
		}
		return string;
	}

	public boolean getBoolean() throws ExecuteException {
		if (type != BOOLEAN) {
			throw new ExecuteException("incompatible types; found " + getTypeDescr() + ", required boolean");
		}

		return bool;
	}

	public byte getByte() throws ExecuteException {
		switch (type) {
			case VOID:
			case STRING:
			case BOOLEAN:
				throw new ExecuteException("incompatible types; found " + getTypeDescr() + ", required byte");

			case CHAR:
			case SHORT:
			case INT:
			case FLOAT:
			case LONG:
			case DOUBLE:
				throw new ExecuteException("possible loss of precision; found " + getTypeDescr() + ", required byte");
		}

		return byteNumber;
	}

	public short getShort() throws ExecuteException {
		switch (type) {
			case VOID:
			case STRING:
			case BOOLEAN:
				throw new ExecuteException("incompatible types; found " + getTypeDescr() + ", required short");

			case BYTE:
				return byteNumber;

			case CHAR:
			case INT:
			case FLOAT:
			case LONG:
			case DOUBLE:
				throw new ExecuteException("possible loss of precision; found " + getTypeDescr() + ", required short");
		}

		return shortNumber;
	}

	public int getInt() throws ExecuteException {
		switch (type) {
			case VOID:
			case STRING:
			case BOOLEAN:
				throw new ExecuteException("incompatible types; found " + getTypeDescr() + ", required int");

			case CHAR:
				return character;

			case BYTE:
				return byteNumber;

			case SHORT:
				return shortNumber;

			case FLOAT:
			case LONG:
			case DOUBLE:
				throw new ExecuteException("possible loss of precision; found " + getTypeDescr() + ", required int");
		}

		return intNumber;
	}

	public float getFloat() throws ExecuteException {
		switch (type) {
			case VOID:
			case STRING:
			case BOOLEAN:
				throw new ExecuteException("incompatible types; found " + getTypeDescr() + ", required float");

			case CHAR:
				return character;

			case BYTE:
				return byteNumber;

			case SHORT:
				return shortNumber;

			case INT:
				return intNumber;

			case LONG:
			case DOUBLE:
				throw new ExecuteException("possible loss of precision; found " + getTypeDescr() + ", required float");
		}

		return floatNumber;
	}

	public long getLong() throws ExecuteException {
		switch (type) {
			case VOID:
			case STRING:
			case BOOLEAN:
				throw new ExecuteException("incompatible types; found " + getTypeDescr() + ", required long");

			case CHAR:
				return character;

			case BYTE:
				return byteNumber;

			case SHORT:
				return shortNumber;

			case INT:
				return intNumber;

			case FLOAT:
			case DOUBLE:
				throw new ExecuteException("possible loss of precision; found " + getTypeDescr() + ", required long");
		}

		return longNumber;
	}

	public double getDouble() throws ExecuteException {
		switch (type) {
			case VOID:
			case STRING:
			case BOOLEAN:
				throw new ExecuteException("incompatible types; found " + getTypeDescr() + ", required double");

			case CHAR:
				return character;

			case BYTE:
				return byteNumber;

			case SHORT:
				return shortNumber;

			case INT:
				return intNumber;

			case FLOAT:
				return floatNumber;

			case LONG:
				return longNumber;
		}

		return doubleNumber;
	}

	public Object getArray() throws ExecuteException {
		return getArray(type, dimension);
	}

	public Object getArray(int type, int dimension) throws ExecuteException {
		if (type != this.type || dimension != this.dimension) {
			throw new ExecuteException("incompatible types; found " + getTypeDescr() + ", required " + Types.getTypeDescr(type, dimension));
		}

		if (isArray()) {
			return array;
		} else {
			throw new ExecuteException("incompatible types; found " + getTypeDescr() + ", required " + Types.getTypeDescr(type, dimension));
		}
	}

	public char getCharacter(int index) throws ExecuteException {
		switch (type) {
			case VOID:
			case STRING:
			case BOOLEAN:
				throw new ExecuteException("incompatible types; found " + getTypeDescr() + ", required char[]");

			case BYTE:
			case SHORT:
			case INT:
			case FLOAT:
			case LONG:
			case DOUBLE:
				throw new ExecuteException("possible loss of precision; found " + getTypeDescr() + ", required char[]");
		}

		return ((char[]) array)[index];
	}

	public String getString(int index) throws ExecuteException {
		if (type != STRING) {
			throw new ExecuteException("incompatible types; found " + getTypeDescr() + ", required string[]");
		}

		return ((String[]) array)[index];
	}

	public boolean getBoolean(int index) throws ExecuteException {
		if (type != BOOLEAN) {
			throw new ExecuteException("incompatible types; found " + getTypeDescr() + ", required boolean[]");
		}

		return ((boolean[]) array)[index];
	}

	public byte getByte(int index) throws ExecuteException {
		switch (type) {
			case VOID:
			case STRING:
			case BOOLEAN:
				throw new ExecuteException("incompatible types; found " + getTypeDescr() + ", required byte[]");

			case CHAR:
			case SHORT:
			case INT:
			case FLOAT:
			case LONG:
			case DOUBLE:
				throw new ExecuteException("possible loss of precision; found " + getTypeDescr() + ", required byte[]");
		}

		return ((byte[]) array)[index];
	}

	public short getShort(int index) throws ExecuteException {
		switch (type) {
			case VOID:
			case STRING:
			case BOOLEAN:
				throw new ExecuteException("incompatible types; found " + getTypeDescr() + ", required short[]");

			case BYTE:
				return ((byte[]) array)[index];

			case CHAR:
			case INT:
			case FLOAT:
			case LONG:
			case DOUBLE:
				throw new ExecuteException("possible loss of precision; found " + getTypeDescr() + ", required short[]");
		}

		return ((short[]) array)[index];
	}

	public int getInt(int index) throws ExecuteException {
		switch (type) {
			case VOID:
			case STRING:
			case BOOLEAN:
				throw new ExecuteException("incompatible types; found " + getTypeDescr() + ", required int[]");

			case CHAR:
				return ((char[]) array)[index];

			case BYTE:
				return ((byte[]) array)[index];

			case SHORT:
				return ((short[]) array)[index];

			case FLOAT:
			case LONG:
			case DOUBLE:
				throw new ExecuteException("possible loss of precision; found " + getTypeDescr() + ", required int[]");
		}

		return ((int[]) array)[index];
	}

	public float getFloat(int index) throws ExecuteException {
		switch (type) {
			case VOID:
			case STRING:
			case BOOLEAN:
				throw new ExecuteException("incompatible types; found " + getTypeDescr() + ", required float[]");

			case CHAR:
				return ((char[]) array)[index];

			case BYTE:
				return ((byte[]) array)[index];

			case SHORT:
				return ((short[]) array)[index];

			case INT:
				return ((int[]) array)[index];

			case LONG:
			case DOUBLE:
				throw new ExecuteException("possible loss of precision; found " + getTypeDescr() + ", required float[]");
		}

		return ((float[]) array)[index];
	}

	public long getLong(int index) throws ExecuteException {
		switch (type) {
			case VOID:
			case STRING:
			case BOOLEAN:
				throw new ExecuteException("incompatible types; found " + getTypeDescr() + ", required long[]");

			case CHAR:
				if (array instanceof char[]) {
					return ((char[]) array)[index];
				} else {
					return ((Character[]) array)[index];
				}

			case BYTE:
				return ((byte[]) array)[index];

			case SHORT:
				return ((short[]) array)[index];

			case INT:
				return ((int[]) array)[index];

			case FLOAT:
			case DOUBLE:
				throw new ExecuteException("possible loss of precision; found " + getTypeDescr() + ", required long[]");
		}

		return ((long[]) array)[index];
	}

	public double getDouble(int index) throws ExecuteException {
		switch (type) {
			case VOID:
			case STRING:
			case BOOLEAN:
				throw new ExecuteException("incompatible types; found " + getTypeDescr() + ", required double[]");

			case CHAR:
				return ((char[]) array)[index];

			case BYTE:
				return ((byte[]) array)[index];

			case SHORT:
				return ((short[]) array)[index];

			case INT:
				return ((int[]) array)[index];

			case FLOAT:
				return ((float[]) array)[index];

			case LONG:
				return ((long[]) array)[index];
		}

		return ((double[]) array)[index];
	}

	public void castCharacter() throws ExecuteException {
		switch (type) {
			case VOID:
			case STRING:
			case BOOLEAN:
				throw new ExecuteException("inconvertible types; found " + getTypeDescr() + ", required char");

			case BYTE:
				character = (char) byteNumber;
				break;

			case SHORT:
				character = (char) shortNumber;
				break;

			case INT:
				character = (char) intNumber;
				break;

			case FLOAT:
				character = (char) floatNumber;
				break;

			case LONG:
				character = (char) longNumber;
				break;

			case DOUBLE:
				character = (char) doubleNumber;
				break;
		}

		type = CHAR;
	}

	public void castString() throws ExecuteException {
		if (type != STRING) {
			throw new ExecuteException("inconvertible types; found " + getTypeDescr() + ", required string");
		}
	}

	public void castBoolean() throws ExecuteException {
		if (type != BOOLEAN) {
			throw new ExecuteException("inconvertible types; found " + getTypeDescr() + ", required boolean");
		}
	}

	public void castByte() throws ExecuteException {
		switch (type) {
			case VOID:
			case STRING:
			case BOOLEAN:
				throw new ExecuteException("inconvertible types; found " + getTypeDescr() + ", required byte");

			case CHAR:
				byteNumber = (byte) character;
				break;

			case SHORT:
				byteNumber = (byte) shortNumber;
				break;

			case INT:
				byteNumber = (byte) intNumber;
				break;

			case FLOAT:
				byteNumber = (byte) floatNumber;
				break;

			case LONG:
				byteNumber = (byte) longNumber;
				break;

			case DOUBLE:
				byteNumber = (byte) doubleNumber;
				break;
		}

		type = BYTE;
	}

	public void castShort() throws ExecuteException {
		switch (type) {
			case VOID:
			case STRING:
			case BOOLEAN:
				throw new ExecuteException("inconvertible types; found " + getTypeDescr() + ", required short");

			case CHAR:
				shortNumber = (short) character;
				break;

			case BYTE:
				shortNumber = byteNumber;
				break;

			case INT:
				shortNumber = (short) intNumber;
				break;

			case FLOAT:
				shortNumber = (short) floatNumber;
				break;

			case LONG:
				shortNumber = (short) longNumber;
				break;

			case DOUBLE:
				shortNumber = (short) doubleNumber;
				break;
		}

		type = SHORT;
	}

	public void castInt() throws ExecuteException {
		switch (type) {
			case VOID:
			case STRING:
			case BOOLEAN:
				throw new ExecuteException("inconvertible types; found " + getTypeDescr() + ", required int");

			case CHAR:
				intNumber = character;
				break;

			case BYTE:
				intNumber = byteNumber;
				break;

			case SHORT:
				intNumber = shortNumber;
				break;

			case FLOAT:
				intNumber = (int) floatNumber;
				break;

			case LONG:
				intNumber = (int) longNumber;
				break;

			case DOUBLE:
				intNumber = (int) doubleNumber;
				break;
		}

		type = INT;
	}

	public void castFloat() throws ExecuteException {
		switch (type) {
			case VOID:
			case STRING:
			case BOOLEAN:
				throw new ExecuteException("inconvertible types; found " + getTypeDescr() + ", required float");

			case CHAR:
				floatNumber = character;
				break;

			case BYTE:
				floatNumber = byteNumber;
				break;

			case SHORT:
				floatNumber = shortNumber;
				break;

			case INT:
				floatNumber = intNumber;
				break;

			case LONG:
				floatNumber = longNumber;
				break;

			case DOUBLE:
				floatNumber = (float) doubleNumber;
				break;
		}

		type = FLOAT;
	}

	public void castLong() throws ExecuteException {
		switch (type) {
			case VOID:
			case STRING:
			case BOOLEAN:
				throw new ExecuteException("inconvertible types; found " + getTypeDescr() + ", required long");

			case CHAR:
				longNumber = character;
				break;

			case BYTE:
				longNumber = byteNumber;
				break;

			case SHORT:
				longNumber = shortNumber;
				break;

			case INT:
				longNumber = intNumber;
				break;

			case FLOAT:
				longNumber = (long) floatNumber;
				break;

			case DOUBLE:
				longNumber = (long) doubleNumber;
				break;
		}

		type = LONG;
	}

	public void castDouble() throws ExecuteException {
		switch (type) {
			case VOID:
			case STRING:
			case BOOLEAN:
				throw new ExecuteException("inconvertible types; found " + getTypeDescr() + ", required double");

			case CHAR:
				doubleNumber = character;
				break;

			case BYTE:
				doubleNumber = byteNumber;
				break;

			case SHORT:
				doubleNumber = shortNumber;
				break;

			case INT:
				doubleNumber = intNumber;
				break;

			case FLOAT:
				doubleNumber = floatNumber;
				break;

			case LONG:
				doubleNumber = longNumber;
				break;
		}

		type = DOUBLE;
	}

	public void castArray(int type, int dimension) throws ExecuteException {
		if (this.type != type || this.dimension != dimension) {
			throw new ExecuteException("inconvertible types; found " + getTypeDescr() + ", required " + Types.getTypeDescr(type, dimension));
		}
	}

	public void cast(int type, int dimension) throws ExecuteException {
		if (!isArray()) {
			if (dimension > 0) {
				throw new ExecuteException("incompatible types; found " + getTypeDescr() + ", required " + Types.getTypeDescr(type, dimension));
			}

			switch (type) {
				case CHAR:
					castCharacter();
					break;

				case STRING:
					castString();
					break;

				case BOOLEAN:
					castBoolean();
					break;

				case BYTE:
					castByte();
					break;

				case SHORT:
					castShort();
					break;

				case INT:
					castInt();
					break;

				case FLOAT:
					castFloat();
					break;

				case LONG:
					castLong();
					break;

				case DOUBLE:
					castDouble();
					break;
			}
		} else {
			castArray(type, dimension);
		}
	}

	public void setArrayValue(ValueContainer value, int index) throws ExecuteException {
		if (dimension != value.dimension - 1) {
			throw new ExecuteException("incompatible types; found " + getTypeDescr() + ", required " + value.getTypeDescr());
		}

		if (value.dimension > 1) {
			if (type != value.type) // for arrays there are no autocastings
			{
				throw new ExecuteException("incompatible types; found " + getTypeDescr() + ", required " + value.getTypeDescr());
			}

			array = ((Object[]) value.array)[index];
		} else if (value.dimension == 1) {
			if (isArray()) {
				throw new ExecuteException("incompatible types; found " + getTypeDescr() + ", required " + value.getTypeDescr());
			}

			if (value.array == null) {
				throw new ExecuteException("incompatible types; found null, required " + value.getTypeDescr());
			}

			switch (type) {
				case CHAR:
					character = value.getCharacter(index);
					break;

				case STRING:
					string = value.getString(index);
					break;

				case BOOLEAN:
					bool = value.getBoolean(index);
					break;

				case BYTE:
					byteNumber = value.getByte(index);
					break;

				case SHORT:
					shortNumber = value.getShort(index);
					break;

				case INT:
					intNumber = value.getInt(index);
					break;

				case FLOAT:
					floatNumber = value.getFloat(index);
					break;

				case LONG:
					longNumber = value.getLong(index);
					break;

				case DOUBLE:
					doubleNumber = value.getDouble(index);
					break;
			}
		}
	}

	public void setArrayValue(int[] indexes, ValueContainer value, SymbolType equateType) throws ExecuteException {
		int deltaDimension = dimension - indexes.length;
		if (deltaDimension != value.dimension) {
			throw new ExecuteException("incompatible types; found " + getTypeDescr() + ", required " + value.getTypeDescr());
		}

		Object array = getArray();
		for (int i = 0; i < indexes.length - 1; i++) {
			array = Array.get(array, indexes[i]);
		}

		if (deltaDimension > 0) {
			if (type != value.type) {
				throw new ExecuteException("incompatible types; found " + getTypeDescr() + ", required " + value.getTypeDescr());
			}

			if (equateType != SymbolType.EQUATE) {
				throw new ExecuteException("operator " + SymbolToken.getSymbol(equateType) + " can not be applied to " + getTypeDescr() + ", " + value.getTypeDescr());
			}

			Array.set(array, indexes[indexes.length - 1], value.getArray(type, deltaDimension));
		} else {
			switch (type) {
				case CHAR:
					Array.set(array, indexes[indexes.length - 1], value.getCharacter());
					break;
				case STRING:
					Array.set(array, indexes[indexes.length - 1], value.getString());
					break;
				case BOOLEAN:
					Array.set(array, indexes[indexes.length - 1], value.getBoolean());
					break;
				case BYTE:
					Array.set(array, indexes[indexes.length - 1], value.getByte());
					break;
				case SHORT:
					Array.set(array, indexes[indexes.length - 1], value.getShort());
					break;
				case INT:
					Array.set(array, indexes[indexes.length - 1], value.getInt());
					break;
				case FLOAT:
					Array.set(array, indexes[indexes.length - 1], value.getFloat());
					break;
				case LONG:
					Array.set(array, indexes[indexes.length - 1], value.getLong());
					break;
				case DOUBLE:
					Array.set(array, indexes[indexes.length - 1], value.getDouble());
					break;
			}
		}
	}

	public String getTypeDescr() {
		return WordToken.getWord(type) + Types.getArrayPostfix(dimension);
	}

	public static void main(String[] args) {
		int[][][][] a = new int[2][3][2][3];
		Class<?> c = a.getClass();
		Class<?> clazz;
		while ((clazz = c.getComponentType()) != null) {
			c = clazz;
		}
	}
}
