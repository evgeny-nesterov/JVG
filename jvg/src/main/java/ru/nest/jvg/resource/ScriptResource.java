package ru.nest.jvg.resource;

public class ScriptResource extends Resource<ScriptData> {
	public ScriptResource(String data) {
		setResource(new ScriptData(data));
	}

	public ScriptResource(ScriptData script) {
		setResource(script);
	}

	private ScriptData script;

	@Override
	public ScriptData getResource() {
		return script;
	}

	@Override
	public void setResource(ScriptData script) {
		this.script = script;
	}

	private ScriptResource postScript;

	public ScriptResource getPostScript() {
		return postScript;
	}

	public void setPostScript(ScriptResource postScript) {
		this.postScript = postScript;
	}
}
