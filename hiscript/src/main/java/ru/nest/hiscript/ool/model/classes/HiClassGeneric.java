package ru.nest.hiscript.ool.model.classes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.ClassResolver;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;
import ru.nest.hiscript.ool.model.nodes.NodeGeneric;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class HiClassGeneric extends HiClass {
	public HiClassGeneric(String genericName, Type genericType, HiClass clazz, HiClass[] parametersClasses, boolean isSuper, NodeGeneric.GenericSourceType sourceType, int index, HiClass sourceClass, ClassResolver classResolver) {
		super(classResolver.getClassLoader(), Type.objectType, clazz.enclosingClass, clazz.interfaceTypes, genericName, null, CLASS_GENERIC, classResolver);
		this.genericType = genericType;
		this.clazz = clazz;
		this.parametersClasses = parametersClasses;
		this.isSuper = isSuper;
		this.sourceType = sourceType;
		this.index = index;
		this.sourceClass = sourceClass;
	}

	// for decode
	protected HiClassGeneric(String genericName, Type genericType, boolean isSuper, NodeGeneric.GenericSourceType sourceType, int index) {
		super(Type.objectType, genericName, null, CLASS_GENERIC);
	}

	public Type genericType;

	public HiClass clazz;

	public HiClass[] parametersClasses;

	public boolean isSuper;

	public NodeGeneric.GenericSourceType sourceType;

	public int index;

	public HiClass sourceClass;

	@Override
	public boolean isGeneric() {
		return true;
	}

	@Override
	public boolean isStatic() {
		return false;
	}

	@Override
	public void init(ClassResolver classResolver) {
		clazz.init(classResolver);
	}

	@Override
	public HiField<?> getField(ClassResolver classResolver, String name) {
		return clazz.getField(classResolver, name);
	}

	@Override
	public HiMethod searchMethod(ClassResolver classResolver, String name, HiClass... argTypes) {
		return clazz.searchMethod(classResolver, name, argTypes);
	}

	@Override
	public boolean isInstanceof(HiClass clazz) {
		return this.clazz.isInstanceof(clazz) || (sourceType != NodeGeneric.GenericSourceType.classSource && clazz.isInstanceof(this.clazz));
	}

	@Override
	protected boolean _validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		return true;
	}

	@Override
	public String toString() {
		return super.toString() + (isSuper ? " super " : " extends ") + genericType;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		os.writeUTF(name);
		os.writeType(genericType);
		os.writeBoolean(isSuper);
		os.writeByte(sourceType.ordinal());
		os.writeShort(index);
		os.writeClass(clazz);
		os.writeClasses(parametersClasses);
		os.writeClass(sourceClass);
	}

	public static HiClassGeneric decode(DecodeContext os) throws IOException {
		HiClassGeneric clazz = new HiClassGeneric(os.readUTF(), os.readType(), os.readBoolean(), NodeGeneric.GenericSourceType.values()[os.readByte()], os.readShort());
		os.readClass(c -> clazz.clazz = c);
		clazz.parametersClasses = os.readClasses();
		os.readClass(c -> clazz.sourceClass = c);
		return clazz;
	}
}
