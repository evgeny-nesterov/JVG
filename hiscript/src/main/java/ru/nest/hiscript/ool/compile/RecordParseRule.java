package ru.nest.hiscript.ool.compile;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.model.AnnotatedModifiers;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiConstructor;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.HiNodeIF;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.ModifiersIF;
import ru.nest.hiscript.ool.model.Operations;
import ru.nest.hiscript.ool.model.OperationsGroup;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.classes.HiClassRecord;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;
import ru.nest.hiscript.ool.model.nodes.NodeBlock;
import ru.nest.hiscript.ool.model.nodes.NodeExpressionNoLS;
import ru.nest.hiscript.ool.model.nodes.NodeGeneric;
import ru.nest.hiscript.ool.model.nodes.NodeGenerics;
import ru.nest.hiscript.ool.model.nodes.NodeIdentifier;
import ru.nest.hiscript.ool.model.nodes.NodeReturn;
import ru.nest.hiscript.ool.model.nodes.NodeThis;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecordParseRule extends ParserUtil {
	private final static RecordParseRule instance = new RecordParseRule();

	public static RecordParseRule getInstance() {
		return instance;
	}

	private RecordParseRule() {
	}

	public HiClass visit(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		tokenizer.start();
		Token startToken = startToken(tokenizer);

		AnnotatedModifiers annotatedModifiers = visitAnnotatedModifiers(tokenizer, ctx, false);
		if (visitWord(Words.RECORD, tokenizer) != null) {
			tokenizer.commit();
			checkModifiers(tokenizer, annotatedModifiers.getModifiers(), annotatedModifiers.getToken(), PUBLIC, PROTECTED, PRIVATE, STATIC);

			String recordName = visitWord(Words.NOT_SERVICE, tokenizer);
			if (recordName == null) {
				recordName = "Null" + new Object().hashCode();
				tokenizer.error("record name is expected");
			}

			// @generics
			NodeGenerics generics = GenericsParseRule.getInstance().visit(tokenizer, ctx);
			if (generics != null) {
				generics.setSourceType(NodeGeneric.GenericSourceType.classSource);
			}

			expectSymbol(tokenizer, Symbols.PARENTHESES_LEFT);

			List<NodeArgument> arguments = new ArrayList<>();
			Token token = tokenizer.currentToken();
			visitArgumentsDefinitions(tokenizer, arguments, ctx);
			if (arguments.size() == 0) {
				tokenizer.error("record argument expected", token);
			}
			expectSymbol(tokenizer, Symbols.PARENTHESES_RIGHT);

			boolean hasContent = false;
			if (checkSymbol(tokenizer, Symbols.SEMICOLON) != -1) {
				tokenizer.nextToken();
			} else {
				expectSymbol(tokenizer, Symbols.BRACES_LEFT);
				hasContent = true;
			}

			HiClassRecord record = new HiClassRecord(ctx.getClassLoader(), recordName, generics, ctx.classType, ctx);
			record.annotations = annotatedModifiers.getAnnotations();
			record.defaultConstructor = new HiConstructor(record, null, null, Modifiers.PUBLIC(), null, arguments, null, null, null, HiConstructor.BodyConstructorType.NONE);
			NodeBlock defaultConstructorBody = new NodeBlock();
			record.defaultConstructor.body = defaultConstructorBody;

			for (NodeArgument argument : arguments) {
				HiField field = HiField.getField(argument.getType(), argument.getVariableName(), argument.getToken());
				field.getModifiers().setAccess(ModifiersIF.ACCESS_PRIVATE);
				ctx.addField(field);

				String getMethodName = "get" + Character.toUpperCase(argument.getVariableName().charAt(0));
				if (argument.getVariableName().length() > 1) {
					getMethodName += argument.getVariableName().substring(1);
				}
				HiNode getMethodBody = new NodeBlock(new NodeReturn(new NodeIdentifier(argument.getVariableName(), 0)));
				HiMethod getMethod = new HiMethod(record, null, new Modifiers(ModifiersIF.ACCESS_PUBLIC | ModifiersIF.FINAL), null, argument.getType(), getMethodName, (NodeArgument[]) null, null, getMethodBody);
				getMethod.setToken(argument.getToken());
				ctx.addMethod(getMethod);

				String setMethodName = "set" + Character.toUpperCase(argument.getVariableName().charAt(0));
				if (argument.getVariableName().length() > 1) {
					setMethodName += argument.getVariableName().substring(1);
				}
				HiNodeIF[] operands = new HiNode[] {new NodeThis(), new NodeIdentifier(argument.getVariableName(), 0), new NodeIdentifier(argument.getVariableName(), 0)};
				HiOperation[] operations = NodeExpressionNoLS.compile(operands, Arrays.asList(new OperationsGroup(Operations.INVOCATION), new OperationsGroup(Operations.EQUATE)));
				NodeExpressionNoLS setExpression = new NodeExpressionNoLS(operands, operations);
				setExpression.setToken(argument.getToken());
				// TODO support set methods?
				HiNode setMethodBody = new NodeBlock(setExpression);
				HiMethod setMethod = new HiMethod(record, null, new Modifiers(ModifiersIF.ACCESS_PUBLIC | ModifiersIF.FINAL), null, Type.voidType, setMethodName, new NodeArgument[] {argument}, null, setMethodBody);
				setMethod.setToken(argument.getToken());
				ctx.addMethod(setMethod);

				defaultConstructorBody.addStatement(setExpression);
			}

			ctx.clazz = record;
			ctx.clazz.modifiers = annotatedModifiers.getModifiers();

			if (hasContent) {
				ClassParseRule.getInstance().visitContent(tokenizer, ctx, (tokenizer1, ctx1) -> {
					// TODO parse specific content
					return false;
				});
				expectSymbol(tokenizer, Symbols.BRACES_RIGHT);
			} else {
				ctx.initClass();
			}

			ctx.clazz.setToken(tokenizer.getBlockToken(startToken));
			return ctx.clazz;
		}

		tokenizer.rollback();
		return null;
	}
}
