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
import ru.nest.hiscript.ool.model.nodes.NodeSwitch;
import ru.nest.hiscript.ool.model.nodes.NodeSynchronized;
import ru.nest.hiscript.ool.model.nodes.NodeThis;
import ru.nest.hiscript.ool.model.nodes.NodeThrow;
import ru.nest.hiscript.ool.model.nodes.NodeTry;
import ru.nest.hiscript.ool.model.nodes.NodeType;
import ru.nest.hiscript.ool.model.nodes.NodeWhile;

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

	public final static byte TYPE_ENUM = 43;

	public final static byte TYPE_CATCH = 44;

	public Node(String name, int type) {
		this(name, type, -1);
	}

	public Node(String name, int type, int line) {
		this.name = name;
		this.type = type;
		this.line = line;
	}

	protected int line = -1;

	protected String name;

	protected int type;

	@Override
	public String toString() {
		return name;
	}

	public abstract void execute(RuntimeContext ctx);

	@Override
	public void code(CodeContext os) throws IOException {
		os.writeByte(type);
	}

	public static Node decode(DecodeContext os) throws IOException {
		int type = os.readByte();
		switch (type) {
			case TYPE_EMPTY:
				return EmptyNode.decode(os);
			case TYPE_ARGUMENT:
				return NodeArgument.decode(os);
			case TYPE_ARRAY:
				return NodeArray.decode(os);
			case TYPE_ARRAY_VALUE:
				return NodeArrayValue.decode(os);
			case TYPE_BLOCK:
				return NodeBlock.decode(os);
			case TYPE_BOOLEAN:
				return NodeBoolean.decode(os);
			case TYPE_BREAK:
				return NodeBreak.decode(os);
			case TYPE_BYTE:
				return NodeByte.decode(os);
			case TYPE_CHAR:
				return NodeChar.decode(os);
			case TYPE_CLASS:
				return NodeClass.decode(os);
			case TYPE_CONSTRUCTOR:
				return NodeConstructor.decode(os);
			case TYPE_CONTINUE:
				return NodeContinue.decode(os);
			case TYPE_DECLARATION:
				return NodeDeclaration.decode(os);
			case TYPE_DECLARATIONS:
				return NodeDeclarations.decode(os);
			case TYPE_DOUBLE:
				return NodeDouble.decode(os);
			case TYPE_DO_WHILE:
				return NodeDoWhile.decode(os);
			case TYPE_EXPRESSION:
				return NodeExpressionNoLS.decode(os);
			case TYPE_FLOAT:
				return NodeFloat.decode(os);
			case TYPE_FOR:
				return NodeFor.decode(os);
			case TYPE_FOR_ITERATOR:
				return NodeForIterator.decode(os);
			case TYPE_IDENTIFIER:
				return NodeIdentifier.decode(os);
			case TYPE_IF:
				return NodeIf.decode(os);
			case TYPE_INT:
				return NodeInt.decode(os);
			case TYPE_INVOCATION:
				return NodeInvocation.decode(os);
			case TYPE_LABEL:
				return NodeLabel.decode(os);
			case TYPE_LONG:
				return NodeLong.decode(os);
			case TYPE_NATIVE:
				return NodeNative.decode(os);
			case TYPE_NULL:
				return NodeNull.decode(os);
			case TYPE_RETURN:
				return NodeReturn.decode(os);
			case TYPE_SHORT:
				return NodeShort.decode(os);
			case TYPE_STRING:
				return NodeString.decode(os);
			case TYPE_SWITCH:
				return NodeSwitch.decode(os);
			case TYPE_THROW:
				return NodeThrow.decode(os);
			case TYPE_TRY:
				return NodeTry.decode(os);
			case TYPE_TYPE:
				return NodeType.decode(os);
			case TYPE_WHILE:
				return NodeWhile.decode(os);
			case TYPE_FIELD:
				return HiField.decode(os);
			case TYPE_SYNCHRONIZED:
				return NodeSynchronized.decode(os);
			case TYPE_LOGICAL_SWITCH:
				return NodeLogicalSwitch.decode(os);
			case TYPE_ASSERT:
				return NodeAssert.decode(os);
			case THIS:
				return NodeThis.decode(os);
			case TYPE_CATCH:
				return NodeCatch.decode(os);
		}
		throw new RuntimeException("Node can't be decoded: undefined type " + type);
	}
}
