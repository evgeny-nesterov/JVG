package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.HiScriptRuntimeException;
import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;
import ru.nest.hiscript.ool.model.nodes.EmptyNode;
import ru.nest.hiscript.ool.model.nodes.NodeAnnotation;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;
import ru.nest.hiscript.ool.model.nodes.NodeArray;
import ru.nest.hiscript.ool.model.nodes.NodeArrayValue;
import ru.nest.hiscript.ool.model.nodes.NodeAssert;
import ru.nest.hiscript.ool.model.nodes.NodeBlock;
import ru.nest.hiscript.ool.model.nodes.NodeBoolean;
import ru.nest.hiscript.ool.model.nodes.NodeBreak;
import ru.nest.hiscript.ool.model.nodes.NodeByte;
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
import ru.nest.hiscript.ool.model.nodes.NodeIdentifier;
import ru.nest.hiscript.ool.model.nodes.NodeIf;
import ru.nest.hiscript.ool.model.nodes.NodeInt;
import ru.nest.hiscript.ool.model.nodes.NodeInvocation;
import ru.nest.hiscript.ool.model.nodes.NodeLabel;
import ru.nest.hiscript.ool.model.nodes.NodeLogicalSwitch;
import ru.nest.hiscript.ool.model.nodes.NodeLong;
import ru.nest.hiscript.ool.model.nodes.NodeNative;
import ru.nest.hiscript.ool.model.nodes.NodeNull;
import ru.nest.hiscript.ool.model.nodes.NodeReturn;
import ru.nest.hiscript.ool.model.nodes.NodeShort;
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
import java.util.List;

public abstract class HiNode implements Codeable, TokenAccessible {
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

	public final static byte THIS = 37;

	public final static byte SUPER = 38;

	public final static byte MAIN_WRAPPER = 39;

	public final static byte TYPE_FOR_ITERATOR = 40;

	public final static byte TYPE_LOGICAL_SWITCH = 41;

	public final static byte TYPE_ASSERT = 42;

	public final static byte TYPE_CATCH = 43;

	public final static byte TYPE_CASTED_IDENTIFIER = 44;

	public final static byte TYPE_ANNOTATION = 45;

	public final static byte TYPE_ANNOTATION_ARGUMENT = 46;

	public final static byte TYPE_EXPRESSION_SWITCH = 47;

	public HiNode(String name, int type) {
		this(name, type, null);
	}

	public HiNode(String name, int type, Token token) {
		this.name = name;
		this.type = type;
		this.token = token != null ? token.bounds() : null;
	}

	protected Token token;

	protected String name;

	protected int type;

	private HiClass valueClass = null;

	private boolean valid;

	private boolean isValue;

	private boolean isConstant;

	private HiNode resolvedValueVariable;

	@Override
	public String toString() {
		return name;
	}

	@Override
	public Token getToken() {
		return token;
	}

	public HiClass getValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		getValueType(validationInfo, ctx);
		return valueClass;
	}

	public NodeValueType getValueType(ValidationInfo validationInfo, CompileClassContext ctx) {
		if (valueClass == null) {
			ctx.nodeValueType.resolvedValueVariable = null;

			computeValueType(validationInfo, ctx);

			valueClass = ctx.nodeValueType.type;
			valid = ctx.nodeValueType.valid;
			isValue = ctx.nodeValueType.isValue;
			isConstant = ctx.nodeValueType.isConstant;
			resolvedValueVariable = ctx.nodeValueType.resolvedValueVariable;

			if (valueClass != null) {
				valueClass.init(ctx);
			} else {
				valueClass = HiClassPrimitive.VOID;
			}
		} else {
			ctx.nodeValueType.get(this, valueClass, valid, isValue, isConstant, resolvedValueVariable);
		}
		return ctx.nodeValueType;
	}

	public HiClass getValueType() {
		return valueClass;
	}

	public void setToken(Token token) {
		this.token = token;
	}

	public boolean isValue() {
		return false;
	}

	public boolean isConstant(CompileClassContext ctx) {
		return false;
	}

	public Object getConstantValue() {
		return null;
	}

	protected void computeValueType(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.nodeValueType.resolvedValueVariable = null;
		HiClass clazz = computeValueClass(validationInfo, ctx);
		boolean valid = clazz != null;
		boolean isValue = isValue() && (valid ? clazz != HiClassPrimitive.VOID : false);
		boolean isConstant = (valid ? clazz != HiClassPrimitive.VOID : false) && isConstant(ctx);
		ctx.nodeValueType.get(this, clazz, valid, isValue, isConstant, ctx.nodeValueType.resolvedValueVariable);
	}

	protected HiClass computeValueClass(ValidationInfo validationInfo, CompileClassContext ctx) {
		return HiClassPrimitive.VOID;
	}

	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
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

	public abstract void execute(RuntimeContext ctx);

	@Override
	public void code(CodeContext os) throws IOException {
		os.writeByte(type);
		os.writeToken(token);
	}

	public static HiNode decode(DecodeContext os) throws IOException {
		int type = os.readByte();
		Token token = os.readToken();
		HiNode node = null;
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
			case TYPE_BYTE:
				node = NodeByte.decode(os, token);
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
			case TYPE_SHORT:
				node = NodeShort.decode(os);
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
			case TYPE_LOGICAL_SWITCH:
				node = NodeLogicalSwitch.decode(os);
				break;
			case TYPE_ASSERT:
				node = NodeAssert.decode(os);
				break;
			case THIS:
				node = NodeThis.decode(os);
				break;
			case TYPE_CATCH:
				node = NodeCatch.decode(os);
				break;
			case SUPER:
				node = NodeSuper.decode(os);
				break;
		}
		if (node != null) {
			node.token = token;
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
		if (valueClass == null || valueClass != HiClassPrimitive.BOOLEAN) {
			validationInfo.error("boolean is expected", getToken());
			return false;
		}
		return true;
	}

	public boolean expectValue(ValidationInfo validationInfo, CompileClassContext ctx) {
		HiClass valueClass = getValueClass(validationInfo, ctx);
		if (valueClass == null || valueClass == HiClassPrimitive.VOID) {
			validationInfo.error("value is expected", getToken());
			return false;
		}
		return true;
	}

	public boolean expectConstant(ValidationInfo validationInfo, CompileClassContext ctx) {
		NodeValueType valueType = getValueType(validationInfo, ctx);
		if (valueType == null || !valueType.isConstant) {
			validationInfo.error("constant expected", getToken());
			return false;
		}
		return true;
	}

	public boolean expectValueClass(ValidationInfo validationInfo, CompileClassContext ctx, HiClass valueClass) {
		NodeValueType castedConditionValueType = getValueType(validationInfo, ctx);
		if (!HiClass.autoCast(castedConditionValueType.type, valueClass, castedConditionValueType.isValue)) {
			validationInfo.error(valueClass.fullName + " is expected", getToken());
			return false;
		}
		return true;
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

	public boolean isTerminal() {
		return false;
	}

	private RuntimeContext computeValue(CompileClassContext ctx) {
		RuntimeContext rctx = new RuntimeContext(ctx.getCompiler(), true);
		rctx.enterStart(null);
		rctx.level.clazz = ctx.clazz;
		rctx.validating = true;

		execute(rctx);

		HiObject exception = rctx.exception;
		rctx.exit();
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
			return null;
		} else {
			validationInfo.error(rctx.getExceptionMessage(), token);
			return null;
		}
	}

	public byte byteValue(CompileClassContext ctx) {
		if (type == PrimitiveTypes.BYTE) {
			return ((NodeByte) this).getValue();
		}
		return computeValue(ctx).value.getByte();
	}

	public short shortValue(CompileClassContext ctx) {
		if (type == PrimitiveTypes.SHORT) {
			return ((NodeShort) this).getValue();
		}
		return computeValue(ctx).value.getShort();
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
