package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.HiScriptRuntimeException;
import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;
import ru.nest.hiscript.ool.model.nodes.EmptyNode;
import ru.nest.hiscript.ool.model.nodes.NodeAnnotation;
import ru.nest.hiscript.ool.model.nodes.NodeAnnotationArgument;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;
import ru.nest.hiscript.ool.model.nodes.NodeArray;
import ru.nest.hiscript.ool.model.nodes.NodeArrayValue;
import ru.nest.hiscript.ool.model.nodes.NodeAssert;
import ru.nest.hiscript.ool.model.nodes.NodeBlock;
import ru.nest.hiscript.ool.model.nodes.NodeBoolean;
import ru.nest.hiscript.ool.model.nodes.NodeBreak;
import ru.nest.hiscript.ool.model.nodes.NodeCastedIdentifier;
import ru.nest.hiscript.ool.model.nodes.NodeCatch;
import ru.nest.hiscript.ool.model.nodes.NodeChar;
import ru.nest.hiscript.ool.model.nodes.NodeClass;
import ru.nest.hiscript.ool.model.nodes.NodeConstructor;
import ru.nest.hiscript.ool.model.nodes.NodeContinue;
import ru.nest.hiscript.ool.model.nodes.NodeDeclaration;
import ru.nest.hiscript.ool.model.nodes.NodeDeclarations;
import ru.nest.hiscript.ool.model.nodes.NodeDoWhile;
import ru.nest.hiscript.ool.model.nodes.NodeDouble;
import ru.nest.hiscript.ool.model.nodes.NodeExpressionNoLS;
import ru.nest.hiscript.ool.model.nodes.NodeExpressionSwitch;
import ru.nest.hiscript.ool.model.nodes.NodeFloat;
import ru.nest.hiscript.ool.model.nodes.NodeFor;
import ru.nest.hiscript.ool.model.nodes.NodeForIterator;
import ru.nest.hiscript.ool.model.nodes.NodeGeneric;
import ru.nest.hiscript.ool.model.nodes.NodeGenerics;
import ru.nest.hiscript.ool.model.nodes.NodeGetClass;
import ru.nest.hiscript.ool.model.nodes.NodeIdentifier;
import ru.nest.hiscript.ool.model.nodes.NodeIf;
import ru.nest.hiscript.ool.model.nodes.NodeInt;
import ru.nest.hiscript.ool.model.nodes.NodeInvocation;
import ru.nest.hiscript.ool.model.nodes.NodeLabel;
import ru.nest.hiscript.ool.model.nodes.NodeLogicalSwitch;
import ru.nest.hiscript.ool.model.nodes.NodeLong;
import ru.nest.hiscript.ool.model.nodes.NodeMainWrapper;
import ru.nest.hiscript.ool.model.nodes.NodeMethodReference;
import ru.nest.hiscript.ool.model.nodes.NodeNative;
import ru.nest.hiscript.ool.model.nodes.NodeNull;
import ru.nest.hiscript.ool.model.nodes.NodeReturn;
import ru.nest.hiscript.ool.model.nodes.NodeString;
import ru.nest.hiscript.ool.model.nodes.NodeSuper;
import ru.nest.hiscript.ool.model.nodes.NodeSwitch;
import ru.nest.hiscript.ool.model.nodes.NodeSynchronized;
import ru.nest.hiscript.ool.model.nodes.NodeThis;
import ru.nest.hiscript.ool.model.nodes.NodeThrow;
import ru.nest.hiscript.ool.model.nodes.NodeTry;
import ru.nest.hiscript.ool.model.nodes.NodeType;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.nodes.NodeWhile;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.tokenizer.Token;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract class HiNode implements HiNodeIF {
	public final static byte TYPE_EMPTY = -1;

	public final static byte TYPE_ARGUMENT = 1;

	public final static byte TYPE_ARRAY = 2;

	public final static byte TYPE_ARRAY_VALUE = 3;

	public final static byte TYPE_BLOCK = 4;

	public final static byte TYPE_BOOLEAN = 5;

	public final static byte TYPE_BREAK = 6;

	public final static byte TYPE_BYTE = 7;

	public final static byte TYPE_CHAR = 8;

	public final static byte TYPE_CLASS = 9;

	public final static byte TYPE_CONSTRUCTOR = 10;

	public final static byte TYPE_CONTINUE = 11;

	public final static byte TYPE_DECLARATION = 12;

	public final static byte TYPE_DECLARATIONS = 13;

	public final static byte TYPE_DOUBLE = 14;

	public final static byte TYPE_DO_WHILE = 15;

	public final static byte TYPE_EXPRESSION = 16;

	public final static byte TYPE_FLOAT = 17;

	public final static byte TYPE_FOR = 18;

	public final static byte TYPE_IDENTIFIER = 19;

	public final static byte TYPE_IF = 20;

	public final static byte TYPE_INT = 21;

	public final static byte TYPE_INVOCATION = 22;

	public final static byte TYPE_LABEL = 23;

	public final static byte TYPE_LONG = 24;

	public final static byte TYPE_NATIVE = 25;

	public final static byte TYPE_NULL = 26;

	public final static byte TYPE_RETURN = 27;

	public final static byte TYPE_SHORT = 28;

	public final static byte TYPE_STRING = 29;

	public final static byte TYPE_SWITCH = 30;

	public final static byte TYPE_THROW = 31;

	public final static byte TYPE_TRY = 32;

	public final static byte TYPE_TYPE = 33;

	public final static byte TYPE_WHILE = 34;

	public final static byte TYPE_FIELD = 35;

	public final static byte TYPE_SYNCHRONIZED = 36;

	public final static byte TYPE_THIS = 37;

	public final static byte TYPE_SUPER = 38;

	public final static byte TYPE_MAIN_WRAPPER = 39;

	public final static byte TYPE_FOR_ITERATOR = 40;

	public final static byte TYPE_LOGICAL_SWITCH = 41;

	public final static byte TYPE_ASSERT = 42;

	public final static byte TYPE_CATCH = 43;

	public final static byte TYPE_CASTED_IDENTIFIER = 44;

	public final static byte TYPE_ANNOTATION = 45;

	public final static byte TYPE_ANNOTATION_ARGUMENT = 46;

	public final static byte TYPE_EXPRESSION_SWITCH = 47;

	public final static byte TYPE_METHOD_REFERENCE = 48;

	public final static byte TYPE_GET_CLASS = 49;

	public final static byte TYPE_GENERICS = 50;

	public final static byte TYPE_GENERIC = 51;

	public final static byte TYPE_METHOD = 52;

	public HiNode(String name, int type, boolean isStatement) {
		this(name, type, null, isStatement);
	}

	public HiNode(String name, int type, Token token, boolean isStatement) {
		this.name = name;
		this.type = type;
		this.token = token != null ? token.bounds() : null;
		this.isStatement = isStatement;
	}

	protected String name;

	public int type;

	private boolean isStatement;

	@Override
	public boolean isStatement() {
		return isStatement;
	}

	public void setStatement(boolean isStatement) {
		this.isStatement = isStatement;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Node value type
	 * Used only for validation and compilation, not from runtime
	 */
	private NodeValueType nodeValueType;

	public Type getReturnValueType() {
		return nodeValueType != null ? nodeValueType.type : null;
	}

	@Override
	public NodeValueType.NodeValueReturnType getValueReturnType() {
		return nodeValueType != null ? nodeValueType.returnType : null;
	}

	public HiClass getValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		getNodeValueType(validationInfo, ctx);
		return nodeValueType.clazz;
	}

	public NodeValueType getNodeValueType(ValidationInfo validationInfo, CompileClassContext ctx) {
		if (nodeValueType == null) {
			nodeValueType = new NodeValueType();
			nodeValueType.init(this);

			computeValueType(validationInfo, ctx);
			ctx.nodeValueType.copyTo(nodeValueType);

			nodeValueType.node = this;
			if (nodeValueType.clazz == null) {
				nodeValueType.clazz = HiClassPrimitive.VOID;
				nodeValueType.returnType = NodeValueType.NodeValueReturnType.noValue;
			}

			if (nodeValueType.valueClass != null) {
				nodeValueType.valueClass.init(ctx);
			} else {
				nodeValueType.valueClass = HiClassPrimitive.VOID;
			}

			if (nodeValueType.valueClass == null) {
				nodeValueType.getCompileValueFromNode();
			}
		} else {
			nodeValueType.copyTo(ctx.nodeValueType);
		}
		return ctx.nodeValueType;
	}

	public boolean isConstant(CompileClassContext ctx) {
		return false;
	}

	public Object getConstantValue() {
		return null;
	}

	protected void computeValueType(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.nodeValueType.resolvedValueVariable = null;
		ctx.nodeValueType.enclosingClass = null;
		ctx.nodeValueType.enclosingType = null;
		ctx.nodeValueType.returnType = null;
		ctx.nodeValueType.valueClass = null;

		HiClass clazz = computeValueClass(validationInfo, ctx);
		Type type = ctx.nodeValueType.type != null ? ctx.nodeValueType.type : Type.getType(clazz);
		boolean valid = clazz != null;
		NodeValueType.NodeValueReturnType returnType = null;
		if (valid) {
			if (clazz != HiClassPrimitive.VOID) {
				returnType = getValueReturnType();
				if (returnType == null) {
					returnType = ctx.nodeValueType.returnType;
				}
			} else {
				returnType = NodeValueType.NodeValueReturnType.noValue;
			}
		}
		HiClass valueClass = returnType == NodeValueType.NodeValueReturnType.compileValue ? ctx.nodeValueType.valueClass : null;
		boolean isConstant = valid && clazz != HiClassPrimitive.VOID && isConstant(ctx);
		ctx.nodeValueType.get(this, clazz, type, valid, returnType, valueClass, isConstant, ctx.nodeValueType.resolvedValueVariable, ctx.nodeValueType.enclosingClass, ctx.nodeValueType.enclosingType);
	}

	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.nodeValueType.returnType = NodeValueType.NodeValueReturnType.noValue;
		ctx.nodeValueType.type = Type.voidType;
		return HiClassPrimitive.VOID;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.currentNode = this;
		return true;
	}

	public boolean validateBlock(ValidationInfo validationInfo, CompileClassContext ctx) {
		if (this instanceof NodeBlock) {
			validate(validationInfo, ctx);
		} else {
			ctx.enter(RuntimeContext.BLOCK, this);
			validate(validationInfo, ctx);
			ctx.exit();
		}
		return true;
	}

	public static boolean validateAnnotations(ValidationInfo validationInfo, CompileClassContext ctx, NodeAnnotation[] annotations) {
		boolean valid = true;
		if (annotations != null) {
			List<String> names = new ArrayList<>(annotations.length);
			for (NodeAnnotation annotation : annotations) {
				valid &= annotation.validate(validationInfo, ctx);
				if (names.contains(annotation.name)) {
					validationInfo.error("duplicate annotation", annotation.getToken());
					valid = false;
				} else {
					names.add(annotation.name);
				}
			}
		}
		return valid;
	}

	/**
	 * Token
	 */
	protected Token token;

	@Override
	public Token getToken() {
		return token;
	}

	@Override
	public void setToken(Token token) {
		this.token = token;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		os.writeByte(type);
		os.writeToken(token);
	}

	public static HiNodeIF decode(DecodeContext os) throws IOException {
		int type = os.readByte();
		Token token = os.readToken();
		HiNodeIF node = null;
		switch (type) {
			case TYPE_EMPTY:
				node = EmptyNode.decode(os);
				break;
			case TYPE_ARGUMENT:
				node = NodeArgument.decode(os);
				break;
			case TYPE_ARRAY:
				node = NodeArray.decode(os);
				break;
			case TYPE_ARRAY_VALUE:
				node = NodeArrayValue.decode(os);
				break;
			case TYPE_BLOCK:
				node = NodeBlock.decode(os);
				break;
			case TYPE_BOOLEAN:
				node = NodeBoolean.decode(os, token);
				break;
			case TYPE_BREAK:
				node = NodeBreak.decode(os);
				break;
			case TYPE_BYTE: // not used
				break;
			case TYPE_CHAR:
				node = NodeChar.decode(os, token);
				break;
			case TYPE_CLASS:
				node = NodeClass.decode(os);
				break;
			case TYPE_CONSTRUCTOR:
				node = NodeConstructor.decode(os);
				break;
			case TYPE_CONTINUE:
				node = NodeContinue.decode(os);
				break;
			case TYPE_DECLARATION:
				node = NodeDeclaration.decode(os);
				break;
			case TYPE_DECLARATIONS:
				node = NodeDeclarations.decode(os);
				break;
			case TYPE_DOUBLE:
				node = NodeDouble.decode(os);
				break;
			case TYPE_DO_WHILE:
				node = NodeDoWhile.decode(os);
				break;
			case TYPE_EXPRESSION:
				node = NodeExpressionNoLS.decode(os);
				break;
			case TYPE_FLOAT:
				node = NodeFloat.decode(os);
				break;
			case TYPE_FOR:
				node = NodeFor.decode(os);
				break;
			case TYPE_FOR_ITERATOR:
				node = NodeForIterator.decode(os);
				break;
			case TYPE_IDENTIFIER:
				node = NodeIdentifier.decode(os);
				break;
			case TYPE_CASTED_IDENTIFIER:
				node = NodeCastedIdentifier.decode(os);
				break;
			case TYPE_IF:
				node = NodeIf.decode(os);
				break;
			case TYPE_INT:
				node = NodeInt.decode(os);
				break;
			case TYPE_INVOCATION:
				node = NodeInvocation.decode(os);
				break;
			case TYPE_LABEL:
				node = NodeLabel.decode(os);
				break;
			case TYPE_LONG:
				node = NodeLong.decode(os);
				break;
			case TYPE_NATIVE:
				node = NodeNative.decode(os);
				break;
			case TYPE_NULL:
				node = NodeNull.decode(os);
				break;
			case TYPE_RETURN:
				node = NodeReturn.decode(os);
				break;
			case TYPE_STRING:
				node = NodeString.decode(os);
				break;
			case TYPE_SWITCH:
				node = NodeSwitch.decode(os);
				break;
			case TYPE_EXPRESSION_SWITCH:
				node = NodeExpressionSwitch.decode(os);
				break;
			case TYPE_THROW:
				node = NodeThrow.decode(os);
				break;
			case TYPE_TRY:
				node = NodeTry.decode(os);
				break;
			case TYPE_TYPE:
				node = NodeType.decode(os);
				break;
			case TYPE_WHILE:
				node = NodeWhile.decode(os);
				break;
			case TYPE_FIELD:
				node = HiField.decode(os);
				break;
			case TYPE_SYNCHRONIZED:
				node = NodeSynchronized.decode(os);
				break;
			case TYPE_MAIN_WRAPPER:
				node = NodeMainWrapper.decode(os);
				break;
			case TYPE_LOGICAL_SWITCH:
				node = NodeLogicalSwitch.decode(os);
				break;
			case TYPE_ASSERT:
				node = NodeAssert.decode(os);
				break;
			case TYPE_THIS:
				node = NodeThis.decode(os);
				break;
			case TYPE_CATCH:
				node = NodeCatch.decode(os);
				break;
			case TYPE_SUPER:
				node = NodeSuper.decode(os);
				break;
			case TYPE_ANNOTATION:
				node = NodeAnnotation.decode(os);
				break;
			case TYPE_ANNOTATION_ARGUMENT:
				node = NodeAnnotationArgument.decode(os);
				break;
			case TYPE_METHOD_REFERENCE:
				node = NodeMethodReference.decode(os);
				break;
			case TYPE_GET_CLASS:
				node = NodeGetClass.decode(os);
				break;
			case TYPE_GENERICS:
				node = NodeGenerics.decode(os);
				break;
			case TYPE_GENERIC:
				node = NodeGeneric.decode(os);
				break;
			case TYPE_METHOD:
				node = HiMethod.decode(os, false);
				break;
		}
		if (node != null) {
			node.setToken(token);
			return node;
		} else {
			throw new HiScriptRuntimeException("node cannot be decoded: undefined type " + type);
		}
	}

	public boolean expectIntValue(ValidationInfo validationInfo, CompileClassContext ctx) {
		HiClass valueClass = getValueClass(validationInfo, ctx);
		if (valueClass == null || !valueClass.isIntNumber()) {
			validationInfo.error("int is expected", getToken());
			return false;
		}
		return true;
	}

	public boolean expectBooleanValue(ValidationInfo validationInfo, CompileClassContext ctx) {
		HiClass valueClass = getValueClass(validationInfo, ctx);
		if (valueClass == null || (valueClass != HiClassPrimitive.BOOLEAN && valueClass.getAutoboxedPrimitiveClass() != HiClassPrimitive.BOOLEAN)) {
			validationInfo.error("boolean is expected", getToken());
			return false;
		}
		return true;
	}

	public boolean expectValue(ValidationInfo validationInfo, CompileClassContext ctx) {
		HiClass valueClass = getValueClass(validationInfo, ctx);
		if (valueClass == null || valueClass == HiClassPrimitive.VOID || getValueReturnType() == NodeValueType.NodeValueReturnType.classValue) {
			validationInfo.error("value is expected", getToken());
			return false;
		}
		return true;
	}

	public boolean expectConstant(ValidationInfo validationInfo, CompileClassContext ctx) {
		NodeValueType valueType = getNodeValueType(validationInfo, ctx);
		if (valueType == null || !valueType.isConstant) {
			validationInfo.error("constant expected", getToken());
			return false;
		}
		return true;
	}

	public boolean expectValueClass(ValidationInfo validationInfo, CompileClassContext ctx, HiClass valueClass) {
		NodeValueType castedConditionValueType = getNodeValueType(validationInfo, ctx);
		if (castedConditionValueType.isCompileValue()) {
			if (!castedConditionValueType.autoCastValue(valueClass)) {
				validationInfo.error(valueClass.getNameDescr() + " is expected", getToken());
				return false;
			}
			return true;
		} else if (castedConditionValueType.returnType != NodeValueType.NodeValueReturnType.classValue) {
			if (!HiClass.autoCast(ctx, castedConditionValueType.clazz, valueClass, false, true)) {
				validationInfo.error(valueClass.getNameDescr() + " is expected", getToken());
				return false;
			}
			return true;
		}
		validationInfo.error(valueClass.getNameDescr() + " is expected", getToken());
		return false;
	}

	public boolean expectObjectValue(ValidationInfo validationInfo, CompileClassContext ctx) {
		HiClass valueClass = getValueClass(validationInfo, ctx);
		if (valueClass == null || !valueClass.isObject()) {
			validationInfo.error("object is expected", getToken());
			return false;
		}
		return true;
	}

	public boolean expectIterableValue(ValidationInfo validationInfo, CompileClassContext ctx) {
		HiClass valueClass = getValueClass(validationInfo, ctx);
		// TODO +Iterable
		if (valueClass == null || !(valueClass.isArray() || valueClass.isInstanceof(HiClass.ARRAYLIST_CLASS_NAME))) {
			validationInfo.error("iterable is expected", getToken());
			return false;
		}
		return true;
	}

	public void checkStatementTermination(CompileClassContext ctx) {
		ctx.breaksLabels.clear();
		boolean isReturnStatement = isReturnStatement(null, ctx.breaksLabels);
		for (Iterator<String> it = ctx.breaksLabels.iterator(); it.hasNext(); ) {
			if (!isReturnStatement(it.next(), null)) {
				it.remove();
			}
		}
		if (isReturnStatement || ctx.breaksLabels.size() > 0) {
			ctx.level.terminate(isReturnStatement);
		}
	}

	public boolean isReturnStatement(String label, Set<String> labels) {
		return false;
	}

	public NodeReturn getReturnNode() {
		return null;
	}

	private RuntimeContext computeValue(CompileClassContext ctx) {
		RuntimeContext rctx = new RuntimeContext(ctx.getCompiler(), true);
		rctx.enterStart(null);
		rctx.level.clazz = ctx.clazz;
		rctx.validating = true;

		HiObject exception;
		try {
			execute(rctx);
			exception = rctx.exception;
		} finally {
			rctx.exit();
		}

		rctx.exception = exception;
		return rctx;
	}

	public Object getObjectValue(ValidationInfo validationInfo, CompileClassContext ctx, Token token) {
		if (isConstant(ctx)) {
			return getConstantValue();
		}
		RuntimeContext rctx = computeValue(ctx);
		if (rctx.exception == null) {
			if (rctx.value.valueType == Value.VALUE) {
				return rctx.value.get();
			} else if (rctx.value.valueType == Value.VARIABLE) {
				return rctx.value.variable;
			}
		} else {
			validationInfo.error(rctx.getExceptionMessage(), token);
		}
		return null;
	}

	public int intValue(CompileClassContext ctx) {
		if (type == PrimitiveTypes.INT) {
			return ((NodeInt) this).getValue();
		}
		return computeValue(ctx).value.getInt();
	}

	public long longValue(CompileClassContext ctx) {
		if (type == PrimitiveTypes.LONG) {
			return ((NodeLong) this).getValue();
		}
		return computeValue(ctx).value.getLong();
	}

	public float floatValue(CompileClassContext ctx) {
		if (type == PrimitiveTypes.FLOAT) {
			return ((NodeFloat) this).getValue();
		}
		return computeValue(ctx).value.getFloat();
	}

	public double doubleValue(CompileClassContext ctx) {
		if (type == PrimitiveTypes.DOUBLE) {
			return ((NodeDouble) this).getValue();
		}
		return computeValue(ctx).value.getDouble();
	}

	public char charValue(CompileClassContext ctx) {
		if (type == PrimitiveTypes.CHAR) {
			return ((NodeChar) this).getValue();
		}
		return computeValue(ctx).value.getChar();
	}

	public boolean computeBooleanValue(CompileClassContext ctx) {
		return computeValue(ctx).value.getBoolean();
	}
}
