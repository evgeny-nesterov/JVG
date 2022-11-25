package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;

public class MathImpl extends ImplUtil {
	public static void Math_int_abs_int(RuntimeContext ctx, int a) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("int");
		ctx.value.intNumber = Math.abs(a);
	}

	public static void Math_float_abs_float(RuntimeContext ctx, float a) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("float");
		ctx.value.floatNumber = Math.abs(a);
	}

	public static void Math_long_abs_long(RuntimeContext ctx, long a) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("long");
		ctx.value.longNumber = Math.abs(a);
	}

	public static void Math_double_abs_double(RuntimeContext ctx, double a) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("double");
		ctx.value.doubleNumber = Math.abs(a);
	}

	public static void Math_double_acos_double(RuntimeContext ctx, double a) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("double");
		ctx.value.doubleNumber = Math.acos(a);
	}

	public static void Math_double_asin_double(RuntimeContext ctx, double a) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("double");
		ctx.value.doubleNumber = Math.asin(a);
	}

	public static void Math_double_atan_double(RuntimeContext ctx, double a) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("double");
		ctx.value.doubleNumber = Math.atan(a);
	}

	public static void Math_double_atan_double_double(RuntimeContext ctx, double y, double x) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("double");
		ctx.value.doubleNumber = Math.atan2(y, x);
	}

	public static void Math_double_cbrt_double(RuntimeContext ctx, double a) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("double");
		ctx.value.doubleNumber = Math.cbrt(a);
	}

	public static void Math_double_ceil_double(RuntimeContext ctx, double a) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("double");
		ctx.value.doubleNumber = Math.ceil(a);
	}

	public static void Math_double_cos_double(RuntimeContext ctx, double a) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("double");
		ctx.value.doubleNumber = Math.cos(a);
	}

	public static void Math_double_cosh_double(RuntimeContext ctx, double a) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("double");
		ctx.value.doubleNumber = Math.cosh(a);
	}

	public static void Math_double_exp_double(RuntimeContext ctx, double a) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("double");
		ctx.value.doubleNumber = Math.exp(a);
	}

	public static void Math_double_expm1_double(RuntimeContext ctx, double a) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("double");
		ctx.value.doubleNumber = Math.expm1(a);
	}

	public static void Math_double_floor_double(RuntimeContext ctx, double a) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("double");
		ctx.value.doubleNumber = Math.floor(a);
	}

	public static void Math_double_hypot_double_double(RuntimeContext ctx, double x, double y) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("double");
		ctx.value.doubleNumber = Math.hypot(x, y);
	}

	public static void Math_double_IEEEremainder_double_double(RuntimeContext ctx, double f1, double f2) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("double");
		ctx.value.doubleNumber = Math.IEEEremainder(f1, f2);
	}

	public static void Math_double_log_double(RuntimeContext ctx, double a) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("double");
		ctx.value.doubleNumber = Math.log(a);
	}

	public static void Math_double_log10_double(RuntimeContext ctx, double a) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("double");
		ctx.value.doubleNumber = Math.log10(a);
	}

	public static void Math_double_log1p_double(RuntimeContext ctx, double a) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("double");
		ctx.value.doubleNumber = Math.log1p(a);
	}

	public static void Math_int_max_int_int(RuntimeContext ctx, int a, int b) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("int");
		ctx.value.intNumber = Math.max(a, b);
	}

	public static void Math_int_max_float_float(RuntimeContext ctx, float a, float b) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("float");
		ctx.value.floatNumber = Math.max(a, b);
	}

	public static void Math_int_max_long_long(RuntimeContext ctx, long a, long b) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("long");
		ctx.value.longNumber = Math.max(a, b);
	}

	public static void Math_int_max_double_double(RuntimeContext ctx, double a, double b) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("double");
		ctx.value.doubleNumber = Math.max(a, b);
	}

	public static void Math_int_min_int_int(RuntimeContext ctx, int a, int b) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("int");
		ctx.value.intNumber = Math.min(a, b);
	}

	public static void Math_float_min_float_float(RuntimeContext ctx, float a, float b) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("float");
		ctx.value.floatNumber = Math.min(a, b);
	}

	public static void Math_long_min_long_long(RuntimeContext ctx, long a, long b) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("long");
		ctx.value.longNumber = Math.min(a, b);
	}

	public static void Math_double_min_double_double(RuntimeContext ctx, double a, double b) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("double");
		ctx.value.doubleNumber = Math.min(a, b);
	}

	public static void Math_double_pow_double_double(RuntimeContext ctx, double a, double b) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("double");
		ctx.value.doubleNumber = Math.pow(a, b);
	}

	public static void Math_double_random(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("double");
		ctx.value.doubleNumber = Math.random();
	}

	public static void Math_double_rint_double(RuntimeContext ctx, double a) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("double");
		ctx.value.doubleNumber = Math.rint(a);
	}

	public static void Math_long_round_double(RuntimeContext ctx, double a) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("long");
		ctx.value.longNumber = Math.round(a);
	}

	public static void Math_int_round_double(RuntimeContext ctx, float a) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("int");
		ctx.value.intNumber = Math.round(a);
	}

	public static void Math_double_signum_double(RuntimeContext ctx, double a) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("double");
		ctx.value.doubleNumber = Math.signum(a);
	}

	public static void Math_float_signum_float(RuntimeContext ctx, float a) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("float");
		ctx.value.floatNumber = Math.signum(a);
	}

	public static void Math_double_sin_double(RuntimeContext ctx, double a) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("double");
		ctx.value.doubleNumber = Math.sin(a);
	}

	public static void Math_double_sinh_double(RuntimeContext ctx, double a) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("double");
		ctx.value.doubleNumber = Math.sinh(a);
	}

	public static void Math_double_sqrt_double(RuntimeContext ctx, double a) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("double");
		ctx.value.doubleNumber = Math.sqrt(a);
	}

	public static void Math_double_tan_double(RuntimeContext ctx, double a) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("double");
		ctx.value.doubleNumber = Math.tan(a);
	}

	public static void Math_double_tanh_double(RuntimeContext ctx, double a) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("double");
		ctx.value.doubleNumber = Math.tanh(a);
	}

	public static void Math_double_toDegrees_double(RuntimeContext ctx, double a) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("double");
		ctx.value.doubleNumber = Math.toDegrees(a);
	}

	public static void Math_double_toRadians_double(RuntimeContext ctx, double a) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("double");
		ctx.value.doubleNumber = Math.toRadians(a);
	}

	public static void Math_double_ulp_double(RuntimeContext ctx, double a) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("double");
		ctx.value.doubleNumber = Math.ulp(a);
	}

	public static void Math_float_ulp_float(RuntimeContext ctx, float a) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("float");
		ctx.value.floatNumber = Math.ulp(a);
	}
}
