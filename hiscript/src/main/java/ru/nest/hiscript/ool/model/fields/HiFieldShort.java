package ru.nest.hiscript.ool.model.fields;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

public class HiFieldShort extends HiFieldNumber<Short> {
    public HiFieldShort(String name) {
        super("short", name);
    }

    private short value;

    @Override
    protected boolean validateType(ValidationInfo validationInfo, CompileClassContext ctx, HiClass fieldClass, NodeValueType valueType) {
        if (valueType.isCompileValue()) {
            if (valueType.clazz == HiClassPrimitive.INT) {
                return valueType.intValue >= Short.MIN_VALUE && valueType.intValue <= Short.MAX_VALUE;
            } else if (valueType.clazz == HiClassPrimitive.BYTE) {
                return true;
            } else if (valueType.clazz == HiClassPrimitive.CHAR) {
                return valueType.charValue <= Short.MAX_VALUE;
            }
            return false;
        } else {
            return valueType.clazz == HiClassPrimitive.BYTE;
        }
    }

    @Override
    public void get(RuntimeContext ctx, Value value, int valueType) {
        switch (valueType) {
            case SHORT:
                value.shortNumber = this.value;
                break;
            case INT:
                value.intNumber = this.value;
                break;
            case LONG:
                value.longNumber = this.value;
                break;
            case FLOAT:
                value.floatNumber = this.value;
                break;
            case DOUBLE:
                value.doubleNumber = this.value;
                break;
            default:
                ctx.throwRuntimeException("incompatible types; found " + value.valueClass.getNameDescr() + ", required " + type.fullName);
                break;
        }
    }

    @Override
    public void set(RuntimeContext ctx, Value value, int valueType) {
        switch (valueType) {
            case BYTE:
                this.value = value.byteNumber;
                break;
            case SHORT:
                this.value = value.shortNumber;
                break;
            default:
                // autocast
                if (value.valueType == Value.VALUE) {
                    switch (valueType) {
                        case CHAR:
                            if (value.character >= Short.MIN_VALUE && value.character <= Short.MAX_VALUE) {
                                this.value = (short) value.character;
                                return;
                            }
                            break;
                        case INT:
                            if (value.intNumber >= Short.MIN_VALUE && value.intNumber <= Short.MAX_VALUE) {
                                this.value = (short) value.intNumber;
                                return;
                            }
                            break;
                    }
                }
                ctx.throwRuntimeException("incompatible types; found " + value.valueClass.getNameDescr() + ", required " + type.fullName);
                break;
        }
    }

    @Override
    public Short get() {
        return value;
    }

    @Override
    public Object getJava(RuntimeContext ctx) {
        return value;
    }
}
