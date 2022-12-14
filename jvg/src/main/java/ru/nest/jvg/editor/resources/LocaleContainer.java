package ru.nest.jvg.editor.resources;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;

public class LocaleContainer {
	public LocaleContainer(String lang) {
		this.lang = lang;
		load();
	}

	private String lang;

	private Map<String, String> items = new HashMap<>();

	public void load() {
		try {
			InputStream is = LocaleContainer.class.getResourceAsStream("lang/" + lang + ".txt");
			LineNumberReader reader = new LineNumberReader(new InputStreamReader(is));
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				int commentIndex = line.indexOf("#");
				if (commentIndex != -1) {
					line = line.substring(0, commentIndex);
				}

				int separatorIndex = line.indexOf("=");
				if (separatorIndex != -1) {
					String key = line.substring(0, separatorIndex).trim();
					String value = line.substring(separatorIndex + 1, line.length()).trim();
					value = value.replaceAll("\\\\n", "\n");
					items.put(key, value);
				}
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public String get(String key, String defaultValue) {
		String value = items.get(key);
		if (value != null) {
			return value;
		} else {
			System.err.println(key + "=" + defaultValue);
			return defaultValue;
		}
	}
}
