package script.ool.model;

import java.io.IOException;

import script.ool.model.nodes.CodeContext;
import script.ool.model.nodes.DecodeContext;
import script.ool.model.nodes.EmptyNode;
import script.ool.model.nodes.NodeArgument;
import script.ool.model.nodes.NodeArray;
import script.ool.model.nodes.NodeArrayValue;
import script.ool.model.nodes.NodeBlock;
import script.ool.model.nodes.NodeBoolean;
import script.ool.model.nodes.NodeBreak;
import script.ool.model.nodes.NodeByte;
import script.ool.model.nodes.NodeChar;
import script.ool.model.nodes.NodeClass;
import script.ool.model.nodes.NodeConstructor;
import script.ool.model.nodes.NodeContinue;
import script.ool.model.nodes.NodeDeclaration;
import script.ool.model.nodes.NodeDeclarations;
import script.ool.model.nodes.NodeDoWhile;
import script.ool.model.nodes.NodeDouble;
import script.ool.model.nodes.NodeExpression;
import script.ool.model.nodes.NodeFloat;
import script.ool.model.nodes.NodeFor;
import script.ool.model.nodes.NodeIdentificator;
import script.ool.model.nodes.NodeIf;
import script.ool.model.nodes.NodeInt;
import script.ool.model.nodes.NodeInvocation;
import script.ool.model.nodes.NodeLabel;
import script.ool.model.nodes.NodeLong;
import script.ool.model.nodes.NodeNative;
import script.ool.model.nodes.NodeNull;
import script.ool.model.nodes.NodeReturn;
import script.ool.model.nodes.NodeShort;
import script.ool.model.nodes.NodeString;
import script.ool.model.nodes.NodeSwitch;
import script.ool.model.nodes.NodeSynchronized;
import script.ool.model.nodes.NodeThrow;
import script.ool.model.nodes.NodeTry;
import script.ool.model.nodes.NodeType;
import script.ool.model.nodes.NodeWhile;

public abstract class Node implements Codable {
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

	public final static byte TYPE_IDENTIFICATOR = 19;

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
				return NodeExpression.decode(os);
			case TYPE_FLOAT:
				return NodeFloat.decode(os);
			case TYPE_FOR:
				return NodeFor.decode(os);
			case TYPE_IDENTIFICATOR:
				return NodeIdentificator.decode(os);
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
				return Field.decode(os);
			case TYPE_SYNCHRONIZED:
				return NodeSynchronized.decode(os);
		}
		throw new RuntimeException("Node can't be decoded: undefined type " + type);
	}
}
