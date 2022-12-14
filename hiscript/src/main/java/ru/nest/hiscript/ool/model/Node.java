package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;
import ru.nest.hiscript.ool.model.nodes.EmptyNode;
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
import ru.nest.hiscript.ool.model.nodes.NodeWhile;
import ru.nest.hiscript.tokenizer.Token;

import java.io.IOException;

public abstract class Node implements Codeable {
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

	public Node(String name, int type) {
		this(name, type, null);
	}

	public Node(String name, int type, Token token) {
		this.name = name;
		this.type = type;
		this.token = token;
	}

	protected Token token;

	protected String name;

	protected int type;

	@Override
	public String toString() {
		return name;
	}

	public Token getToken() {
		return token;
	}

	public void setToken(Token token) {
		this.token = token;
	}

	public HiClass getValueType(ValidationContext ctx) {
		return null;
	}

	public void validate(ValidationContext ctx) {
	}

	public abstract void execute(RuntimeContext ctx);

	@Override
	public void code(CodeContext os) throws IOException {
		os.writeByte(type);
		if (token != null) {
			token.code(os);
		} else {
			os.writeInt(-1);
		}
	}

	public static Node decode(DecodeContext os) throws IOException {
		int type = os.readByte();
		Token token = Token.decode(os);
		Node node = null;
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
				node = NodeBoolean.decode(os);
				break;
			case TYPE_BREAK:
				node = NodeBreak.decode(os);
				break;
			case TYPE_BYTE:
				node = NodeByte.decode(os);
				break;
			case TYPE_CHAR:
				node = NodeChar.decode(os);
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
			throw new RuntimeException("Node can't be decoded: undefined type " + type);
		}
	}
}
