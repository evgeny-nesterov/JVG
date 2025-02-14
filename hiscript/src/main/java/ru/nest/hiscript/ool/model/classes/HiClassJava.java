package ru.nest.hiscript.ool.model.classes;

import ru.nest.hiscript.ool.model.ClassResolver;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiClassLoader;
import ru.nest.hiscript.ool.model.HiConstructor;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.MethodSignature;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.java.HiConstructorJava;
import ru.nest.hiscript.ool.model.java.HiFieldJava;
import ru.nest.hiscript.ool.model.java.HiMethodJava;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;
import ru.nest.hiscript.ool.runtime.HiRuntimeEnvironment;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class HiClassJava extends HiClass {
	public Class javaClass;

	private final static HiConstructorJava noJavaConstructor = new HiConstructorJava(null, null, null);

	public HiClassJava(HiClassLoader classLoader, String name, Class javaClass) {
		super(classLoader, null, null, name, CLASS_TYPE_TOP, null);
		this.javaClass = javaClass;
	}

	@Override
	protected List<HiConstructor> _searchConstructors(ClassResolver classResolver, MethodSignature signature) {
		HiClass[] argTypes = signature.argClasses;
		Class[] javaArgClasses = new Class[argTypes.length];
		for (int i = 0; i < argTypes.length; i++) {
			HiClass argType = argTypes[i];
			if (argType.isNull()) {
				continue;
			}
			Class argTypeJavaClass = argType.getJavaClass(classResolver.getEnv());
			if (argTypeJavaClass == null) {
				classResolver.processResolverException("inconvertible java class argument: " + argType.getNameDescr());
				return null;
			}
			javaArgClasses[i] = argTypeJavaClass;
		}
		try {
			Integer signatureId = Objects.hash(javaArgClasses); // TODO check correct!
			Map<Integer, HiConstructorJava> javaConstructorsMap = classResolver.getEnv().javaConstructorsMap;
			HiConstructorJava javaConstructor = javaConstructorsMap.get(signatureId);
			if (javaConstructor != null) {
				return javaConstructor != noJavaConstructor ? Arrays.asList(javaConstructor) : null;
			}
			Constructor matchedConstructor = null;
			Constructor nullMatchedConstructor = null;
			for (Constructor constructor : javaClass.getConstructors()) {
				switch (matchParameters(javaArgClasses, constructor.getParameterTypes())) {
					case NOT:
						continue;
					case NULL_MATCHED:
						if (nullMatchedConstructor != null) {
							classResolver.processResolverException("multiple java constructors of class " + javaClass.getName() + " matched to arguments (" + String.join(", ", Arrays.asList(javaArgClasses).stream().map(t -> t != null ? t.toString() : "any").collect(Collectors.toList())) + "): " + matchedConstructor + " and " + constructor);
							return null;
						}
						nullMatchedConstructor = constructor;
					case MATCHED:
						matchedConstructor = constructor;
						break;
				}
			}
			if (matchedConstructor != null) {
				Type type = null;
				if (interfaces != null && interfaces.length == 1) {
					type = Type.getType(interfaces[0]);
				}
				javaConstructor = new HiConstructorJava(this, type, matchedConstructor);
				javaConstructorsMap.put(signatureId, javaConstructor);
				return Arrays.asList(javaConstructor);
			} else {
				javaConstructorsMap.put(signatureId, noJavaConstructor);
			}
		} catch (Exception e) {
			classResolver.processResolverException(e.getMessage());
		}
		return null;
	}

	public enum MatchParametersType {
		NOT, MATCHED, NULL_MATCHED
	}

	public static MatchParametersType matchParameters(Class[] types, Class[] javaTypes) {
		if (types.length != javaTypes.length) {
			return MatchParametersType.NOT;
		}
		if (types.length == 0) {
			return MatchParametersType.MATCHED;
		}
		boolean nullMatched = false;
		for (int i = 0; i < types.length; i++) {
			Class t1 = types[i];
			if (!matchParameter(t1, javaTypes[i])) {
				return MatchParametersType.NOT;
			}
			if (t1 == null) {
				nullMatched = true;
			}
		}
		return nullMatched ? MatchParametersType.NULL_MATCHED : MatchParametersType.MATCHED;
	}

	public static boolean matchParameter(Class t1, Class t2) {
		if (t1 == null) {
			return !t2.isPrimitive();
		} else if (t1 == boolean.class || t1 == Boolean.class) {
			return t2 == boolean.class || t2 == Boolean.class;
		} else if (t1 == int.class || t1 == Integer.class) {
			return t2 == int.class || t2 == Integer.class || //
					t2 == long.class || t2 == Long.class || //
					t2 == float.class || t2 == Float.class || //
					t2 == double.class || t2 == Double.class;
		} else if (t1 == long.class || t1 == Long.class) {
			return t2 == long.class || t2 == Long.class || //
					t2 == double.class || t2 == Double.class;
		} else if (t1 == double.class || t1 == Double.class) {
			return t2 == double.class || t2 == Double.class;
		} else if (t1 == char.class || t1 == Character.class) {
			return t2 == char.class || t2 == Character.class || //
					t2 == int.class || t2 == Integer.class || //
					t2 == long.class || t2 == Long.class || //
					t2 == float.class || t2 == Float.class || //
					t2 == double.class || t2 == Double.class;
		} else if (t1 == byte.class || t1 == Byte.class) {
			return t2 == byte.class || t2 == Byte.class || //
					t2 == short.class || t2 == Short.class || //
					t2 == int.class || t2 == Integer.class || //
					t2 == long.class || t2 == Long.class || //
					t2 == float.class || t2 == Float.class || //
					t2 == double.class || t2 == Double.class;
		} else if (t1 == float.class || t1 == Float.class) {
			return t2 == float.class || t2 == Float.class || //
					t2 == double.class || t2 == Double.class;
		} else if (t1 == short.class || t1 == Short.class) {
			return t2 == short.class || t2 == Short.class || //
					t2 == int.class || t2 == Integer.class || //
					t2 == long.class || t2 == Long.class || //
					t2 == float.class || t2 == Float.class || //
					t2 == double.class || t2 == Double.class;
		} else if (t1.isArray()) {
			int dimension1 = 0;
			while (t1.isArray()) {
				t1 = t1.getComponentType();
				dimension1++;
			}

			int dimension2 = 0;
			while (t2.isArray()) {
				t2 = t2.getComponentType();
				dimension2++;
			}

			if (dimension1 != dimension2) {
				return false;
			} else if (t1.isPrimitive()) {
				return t1 == t2;
			}
			return matchParameter(t1, t2);
		}
		return t2.isAssignableFrom(t1);
	}

	private final Map<MethodSignature, HiMethodJava> javaMethodsMap = new ConcurrentHashMap<>();

	@Override
	protected List<HiMethod> _searchMethods(ClassResolver classResolver, MethodSignature signature) {
		HiClass[] argTypes = signature.argClasses;
		Class[] javaArgClasses = new Class[argTypes.length];
		for (int i = 0; i < argTypes.length; i++) {
			HiClass argType = argTypes[i];
			if (argType.isNull()) {
				continue;
			}
			Class argTypeJavaClass = argType.getJavaClass(classResolver.getEnv());
			if (argTypeJavaClass == null) {
				classResolver.processResolverException("Inconvertible java class argument: " + argType.getNameDescr());
				return null;
			}
			javaArgClasses[i] = argTypeJavaClass;
		}
		try {
			HiMethodJava javaMethod = javaMethodsMap.get(signature);
			if (javaMethod != null) {
				return javaMethod != HiMethodJava.NULL ? Collections.singletonList(javaMethod) : null;
			}
			Method matchedMethod = null;
			Method nullMatchedMethod = null;
			for (Method method : javaClass.getMethods()) {
				if (!method.getName().equals(signature.name)) {
					continue;
				}
				switch (matchParameters(javaArgClasses, method.getParameterTypes())) {
					case NOT:
						continue;
					case NULL_MATCHED:
						if (nullMatchedMethod != null) {
							classResolver.processResolverException("multiple java methods of class " + javaClass.getName() + " matched to signature " + signature.name + "(" + String.join(", ", Arrays.asList(javaArgClasses).stream().map(t -> t != null ? t.toString() : "any").collect(Collectors.toList())) + "): " + matchedMethod + " and " + method);
							return null;
						}
						nullMatchedMethod = method;
					case MATCHED:
						matchedMethod = method;
						break;
				}
			}
			if (matchedMethod != null) {
				javaMethod = new HiMethodJava(classResolver, this, matchedMethod, signature.name);
				javaMethodsMap.put(signature, javaMethod);
				return Collections.singletonList(javaMethod);
			} else {
				javaMethodsMap.put(signature, HiMethodJava.NULL);
			}
		} catch (Exception e) {
			classResolver.processResolverException(e.getMessage());
		}
		return null;
	}

	@Override
	protected HiField<?> _searchField(ClassResolver classResolver, String name, boolean local) {
		try {
			Field field = javaClass.getDeclaredField(name);
			//if (field.isAccessible()) {
			return new HiFieldJava(field, name, classResolver.getEnv());
			//}
		} catch (NoSuchFieldException e) {
		}
		return null;
	}

	@Override
	public boolean isObject() {
		return true;
	}

	@Override
	public boolean isJava() {
		return true;
	}

	@Override
	public void code(CodeContext os) {
	}

	public static HiClass decode(DecodeContext os) {
		return null;
	}

	@Override
	public Class getJavaClass(HiRuntimeEnvironment env) {
		return javaClass;
	}
}
