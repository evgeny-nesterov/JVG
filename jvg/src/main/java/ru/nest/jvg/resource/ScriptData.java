package ru.nest.jvg.resource;

public class ScriptData {
	public ScriptData(String data) {
		this.data = data;
	}

	private String data;

	public String getData() {
		return data;
	}

	public boolean isLine() {
		return data.indexOf('\n') == -1;
	}
}
