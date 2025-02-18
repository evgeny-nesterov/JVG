package ru.nest.hiscript.pol.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Methods {
	private Map<String, Map<String, Map<Integer, List<Method>>>> hash_methods = null;

	public Method get(String namespace, String name, int[] argsTypes, int[] argsDimensions) {
		if (hash_methods == null) {
			return null;
		}
		if (namespace == null) {
			namespace = "";
		}

		Map<String, Map<Integer, List<Method>>> namespace_methods = hash_methods.get(namespace);
		if (namespace_methods == null) {
			return null;
		}

		Map<Integer, List<Method>> argscount_methods = namespace_methods.get(name);
		if (argscount_methods == null) {
			return null;
		}

		int argsCount = argsTypes.length;
		List<Method> methods = argscount_methods.get(argsCount);
		if (methods == null) {
			return null;
		}

		int size = methods.size();
		for (int j = 0; j < size; j++) {
			Method m = methods.get(j);
			boolean equals = true;
			for (int i = 0; i < argsCount; i++) {
				if (m.getArgsDimensions()[i] != argsDimensions[i] || (argsDimensions[i] == 0 && !Types.isAutoCast(argsTypes[i], m.getArgsTypes()[i])) || (argsDimensions[i] > 0 && argsTypes[i] != m.getArgsTypes()[i])) {
					equals = false;
					break;
				}
			}

			if (equals) {
				return m;
			}
		}
		return null;
	}

	public void add(Method method) {
		if (hash_methods == null) {
			hash_methods = new HashMap<>();
		}

		Map<String, Map<Integer, List<Method>>> namespace_methods = hash_methods.computeIfAbsent(method.getNamespace(), k -> new HashMap<>());

		Map<Integer, List<Method>> argscount_methods = namespace_methods.computeIfAbsent(method.getName(), k -> new HashMap<>());

		int argsCount = method.getArgsTypes().length;
		List<Method> methods = argscount_methods.computeIfAbsent(argsCount, k -> new ArrayList<>());
		methods.add(method);
	}
}
