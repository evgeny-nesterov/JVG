package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.RuntimeContext;

public class MathImpl extends ImplUtil {
	public static void Math_int_abs_int(RuntimeContext ctx, int a) {
		returnInt(ctx, Math.abs(a));
	}

	public static void Math_float_abs_float(RuntimeContext ctx, float a) {
		returnFloat(ctx, Math.abs(a));
	}

	public static void Math_long_abs_long(RuntimeContext ctx, long a) {
		returnLong(ctx, Math.abs(a));
	}

	public static void Math_double_abs_double(RuntimeContext ctx, double a) {
		returnDouble(ctx, Math.abs(a));
	}

	public static void Math_double_acos_double(RuntimeContext ctx, double a) {
		returnDouble(ctx, Math.acos(a));
	}

	public static void Math_double_asin_double(RuntimeContext ctx, double a) {
		returnDouble(ctx, Math.asin(a));
	}

	public static void Math_double_atan_double(RuntimeContext ctx, double a) {
		returnDouble(ctx, Math.atan(a));
	}

	public static void Math_double_atan_double_double(RuntimeContext ctx, double y, double x) {
		returnDouble(ctx, Math.atan2(y, x));
	}

	public static void Math_double_cbrt_double(RuntimeContext ctx, double a) {
		returnDouble(ctx, Math.cbrt(a));
	}

	public static void Math_double_ceil_double(RuntimeContext ctx, double a) {
		returnDouble(ctx, Math.ceil(a));
	}

	public static void Math_double_cos_double(RuntimeContext ctx, double a) {
		returnDouble(ctx, Math.cos(a));
	}

	public static void Math_double_cosh_double(RuntimeContext ctx, double a) {
		returnDouble(ctx, Math.cosh(a));
	}

	public static void Math_double_exp_double(RuntimeContext ctx, double a) {
		returnDouble(ctx, Math.exp(a));
	}

	public static void Math_double_expm1_double(RuntimeContext ctx, double a) {
		returnDouble(ctx, Math.expm1(a));
	}

	public static void Math_double_floor_double(RuntimeContext ctx, double a) {
		returnDouble(ctx, Math.floor(a));
	}

	public static void Math_double_hypot_double_double(RuntimeContext ctx, double x, double y) {
		returnDouble(ctx, Math.hypot(x, y));
	}

	public static void Math_double_IEEEremainder_double_double(RuntimeContext ctx, double f1, double f2) {
		returnDouble(ctx, Math.IEEEremainder(f1, f2));
	}

	public static void Math_double_log_double(RuntimeContext ctx, double a) {
		returnDouble(ctx, Math.log(a));
	}

	public static void Math_double_log10_double(RuntimeContext ctx, double a) {
		returnDouble(ctx, Math.log10(a));
	}

	public static void Math_double_log1p_double(RuntimeContext ctx, double a) {
		returnDouble(ctx, Math.log1p(a));
	}

	public static void Math_int_max_int_int(RuntimeContext ctx, int a, int b) {
		returnInt(ctx, Math.max(a, b));
	}

	public static void Math_int_max_float_float(RuntimeContext ctx, float a, float b) {
		returnFloat(ctx, Math.max(a, b));
	}

	public static void Math_int_max_long_long(RuntimeContext ctx, long a, long b) {
		returnLong(ctx, Math.max(a, b));
	}

	public static void Math_int_max_double_double(RuntimeContext ctx, double a, double b) {
		returnDouble(ctx, Math.max(a, b));
	}

	public static void Math_int_min_int_int(RuntimeContext ctx, int a, int b) {
		returnInt(ctx, Math.min(a, b));
	}

	public static void Math_float_min_float_float(RuntimeContext ctx, float a, float b) {
		returnFloat(ctx, Math.min(a, b));
	}

	public static void Math_long_min_long_long(RuntimeContext ctx, long a, long b) {
		returnLong(ctx, Math.min(a, b));
	}

	public static void Math_double_min_double_double(RuntimeContext ctx, double a, double b) {
		returnDouble(ctx, Math.min(a, b));
	}

	public static void Math_double_pow_double_double(RuntimeContext ctx, double a, double b) {
		returnDouble(ctx, Math.pow(a, b));
	}

	public static void Math_double_random(RuntimeContext ctx) {
		returnDouble(ctx, Math.random());
	}

	public static void Math_double_rint_double(RuntimeContext ctx, double a) {
		returnDouble(ctx, Math.rint(a));
	}

	public static void Math_long_round_double(RuntimeContext ctx, double a) {
		returnLong(ctx, Math.round(a));
	}

	public static void Math_int_round_double(RuntimeContext ctx, float a) {
		returnInt(ctx, Math.round(a));
	}

	public static void Math_double_signum_double(RuntimeContext ctx, double a) {
		returnDouble(ctx, Math.signum(a));
	}

	public static void Math_float_signum_float(RuntimeContext ctx, float a) {
		returnFloat(ctx, Math.signum(a));
	}

	public static void Math_double_sin_double(RuntimeContext ctx, double a) {
		returnDouble(ctx, Math.sin(a));
	}

	public static void Math_double_sinh_double(RuntimeContext ctx, double a) {
		returnDouble(ctx, Math.sinh(a));
	}

	public static void Math_double_sqrt_double(RuntimeContext ctx, double a) {
		returnDouble(ctx, Math.sqrt(a));
	}

	public static void Math_double_tan_double(RuntimeContext ctx, double a) {
		returnDouble(ctx, Math.tan(a));
	}

	public static void Math_double_tanh_double(RuntimeContext ctx, double a) {
		returnDouble(ctx, Math.tanh(a));
	}

	public static void Math_double_toDegrees_double(RuntimeContext ctx, double a) {
		returnDouble(ctx, Math.toDegrees(a));
	}

	public static void Math_double_toRadians_double(RuntimeContext ctx, double a) {
		returnDouble(ctx, Math.toRadians(a));
	}

	public static void Math_double_ulp_double(RuntimeContext ctx, double a) {
		returnDouble(ctx, Math.ulp(a));
	}

	public static void Math_float_ulp_float(RuntimeContext ctx, float a) {
		returnFloat(ctx, Math.ulp(a));
	}
}
