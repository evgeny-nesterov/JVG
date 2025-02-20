package ru.nest.hiscript.ool.compile;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.compile.parse.RootParseRule;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiClassLoader;
import ru.nest.hiscript.ool.model.HiNodeIF;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.validation.HiScriptValidationException;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.zip.GZIPOutputStream;

public class HiCompiler {
	private final Tokenizer tokenizer;

	private boolean assertsActive = false;

	private boolean verbose = false;

	private boolean printInvalidCode = false;

	private ParseRule<?> rule;

	private ValidationInfo validationInfo;

	private final HiClassLoader classLoader;

	public HiCompiler(HiClassLoader classLoader, Tokenizer tokenizer) {
		this.classLoader = classLoader;
		this.tokenizer = tokenizer;
	}

	public HiNodeIF build() throws TokenizerException, HiScriptParseException, HiScriptValidationException {
		if (rule == null) {
			rule = new RootParseRule(this, true, true);
		}

		validationInfo = new ValidationInfo(this);
		tokenizer.setValidationInfo(verbose ? validationInfo : null);

		HiNodeIF node = rule.visit(tokenizer, null);
		boolean valid = node != null && validationInfo.isValid();
		if (node != null) {
			valid &= node.validate(validationInfo, null);
			valid &= classLoader.validate(validationInfo);
		}

		if (validationInfo.messages.size() > 0) {
			validationInfo.throwExceptionIf();
		} else if (!valid && !verbose) {
			throw new HiScriptValidationException("Validation error", null);
		}
		return node;
	}

	public ValidationInfo getValidationInfo() {
		return validationInfo;
	}

	public void setValidationInfo(ValidationInfo validationInfo) {
		this.validationInfo = validationInfo;
	}

	public void setRule(ParseRule<?> rule) {
		this.rule = rule;
	}

	public static HiCompiler getDefaultCompiler(HiClassLoader classLoader, String s) {
		Tokenizer t = Tokenizer.getDefaultTokenizer(s);
		return new HiCompiler(classLoader, t);
	}

	public static HiCompiler getDefaultCompiler(InputStream is) throws IOException {
		Tokenizer t = Tokenizer.getDefaultTokenizer(is);
		return new HiCompiler(null, t);
	}

	public boolean isAssertsActive() {
		return assertsActive;
	}

	public void setAssertsActive(boolean assertsActive) {
		this.assertsActive = assertsActive;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
		tokenizer.setValidationInfo(verbose ? validationInfo : null);
	}

	public Tokenizer getTokenizer() {
		return tokenizer;
	}

	public HiClassLoader getClassLoader() {
		return classLoader;
	}

	public String getTokenText(Token token) {
		return tokenizer.getText(token);
	}

	public String getTokenLineText(Token token) {
		return tokenizer.getTokenLineText(token);
	}

	public boolean isPrintInvalidCode() {
		return printInvalidCode;
	}

	public void setPrintInvalidCode(boolean printInvalidCode) {
		this.printInvalidCode = printInvalidCode;
	}

	public static boolean compilingSystem = false;

	public static void compileSystem() throws IOException {
		compilingSystem = true;
		CodeContext os = new CodeContext();
		HiClass.getSystemClassLoader().code(os);
		byte[] data = os.code();
		File dir = new File(HiCompiler.class.getResource("/hilibs").getFile());
		dir = new File(dir, "bin");
		if (!dir.exists()) {
			dir.mkdirs();
		}

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		GZIPOutputStream zos = new GZIPOutputStream(bos);
		zos.write(data);
		zos.close();
		byte[] compressedData = bos.toByteArray();

		File file = new File(dir, "system.hilib");
		Files.write(file.toPath(), compressedData);
	}

	public static void main(String[] args) {
		try {
			compileSystem();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
