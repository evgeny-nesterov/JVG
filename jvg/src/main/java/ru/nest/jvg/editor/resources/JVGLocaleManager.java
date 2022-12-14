package ru.nest.jvg.editor.resources;

import java.util.HashMap;
import java.util.Map;

import javax.swing.UIDefaults;
import javax.swing.UIManager;

public class JVGLocaleManager {
	public final static String RU = "ru";

	public final static String EN = "en";

	public final static String DEFUALT = RU;

	private final static JVGLocaleManager instance = new JVGLocaleManager();

	public static JVGLocaleManager getInstance() {
		return instance;
	}

	private JVGLocaleManager() {
	}

	private Map<String, LocaleContainer> locales = new HashMap<>();

	public void setLanguage(String lang) {
		current = locales.get(lang);
		if (current == null) {
			current = new LocaleContainer(lang);
			locales.put(lang, current);
		}

		UIDefaults u = UIManager.getLookAndFeelDefaults();
		u.put("OptionPane.yesButtonText", getValue("option.yes", "Yes"));
		u.put("OptionPane.noButtonText", getValue("option.no", "No"));
		u.put("OptionPane.okButtonText", getValue("option.ok", "OK"));
		u.put("OptionPane.cancelButtonText", getValue("option.cancel", "Cancel"));

		u.put("AbstractUndoableEdit.undoText", getValue("UndoableEdit.undoText", ""));
		u.put("AbstractUndoableEdit.redoText", getValue("UndoableEdit.redoText", ""));
	}

	private LocaleContainer current;

	public String getValue(String key) {
		return getValue(key, key);
	}

	public String getValue(String key, String defaultValue) {
		if (current == null) {
			setLanguage(EN);
		}
		return current.get(key, defaultValue != null ? defaultValue : "");
	}

	public static void main(String[] args) {
		JVGLocaleManager.getInstance().setLanguage(JVGLocaleManager.RU);
		System.out.println(JVGLocaleManager.getInstance().getValue("X"));
		System.out.println(JVGLocaleManager.getInstance().getValue("Y"));
		System.out.println(JVGLocaleManager.getInstance().getValue("abc"));
	}
}
