// these are written by the test runner
//@file:JvmName("SpecTestMain")
//package wabt.spec_test

import kotlin.text.StringBuilder

var g_tests_run: Int = 0
var g_tests_passed: Int = 0

fun error(message: String, exception: Throwable) {
    System.err.println(message)
    exception.printStackTrace()
}

inline fun <T> ASSERT_TRAP(f: () -> T, name: String) {
    g_tests_run++
    try {
        f()
        error(StringBuilder("expected ").append(name).append(" to trap.").toString(), Exception())
    } catch (e: wasm_rt_impl.WasmException) {
        g_tests_passed++
    }
}

inline fun <T> ASSERT_EXHAUSTION(f: () -> T, name: String) {
    g_tests_run++
    try {
        f()
        error(StringBuilder("expected ").append(name).append(" to trap.").toString(), Exception())
    } catch (e: wasm_rt_impl.ExhaustionException) {
        g_tests_passed++
    } catch (e: wasm_rt_impl.WasmException) {
        error(StringBuilder("expected ").append(name).append(" to trap due to exhaustion.").toString(), e)
    }
}

inline fun <T> ASSERT_RETURN(f: () -> T, name: String) {
    g_tests_run++
    try {
        f()
        g_tests_passed++
    } catch (e: wasm_rt_impl.WasmException) {
        error(StringBuilder().append(name).append(" trapped.").toString(), e)
    }
}

inline fun <T> ASSERT_RETURN_T(f: () -> T, expected: T, is_equal: (T, T) -> Boolean, name: String) {
    g_tests_run++
    try {
        val value: T = f()
        if (is_equal(value, expected)) {
            g_tests_passed++
        } else {
            error(StringBuilder("in ").append(name).append(": expected ").append(expected).append(", got ").append(value).append(".").toString(), Exception())
        }
    } catch (e: wasm_rt_impl.WasmException) {
        error(StringBuilder().append(name).append(" trapped.").toString(), e)
    }
}

inline fun <T> ASSERT_RETURN_NAN_T(f: () -> T, is_nan_kind: (T) -> Boolean, name: String, nan: String) {
    g_tests_run++
    try {
        val value: T = f()
        if (is_nan_kind(value)) {
            g_tests_passed++
        } else {
            // FIXME(Soni): print the bits
            error(StringBuilder("in ").append(name).append(": expected ").append(nan).append(", got ").append(value).append(".").toString(), Exception())
        }
    } catch (e: wasm_rt_impl.WasmException) {
        error(StringBuilder().append(name).append(" trapped.").toString(), e)
    }
}


inline fun ASSERT_RETURN_I32(f: () -> Int, expected: Int, name: String) = ASSERT_RETURN_T(f, expected, ::is_equal_u32, name)
inline fun ASSERT_RETURN_I64(f: () -> Long, expected: Long, name: String) = ASSERT_RETURN_T(f, expected, ::is_equal_u64, name)
inline fun ASSERT_RETURN_F32(f: () -> Float, expected: Float, name: String) = ASSERT_RETURN_T(f, expected, ::is_equal_f32, name)
inline fun ASSERT_RETURN_F64(f: () -> Double, expected: Double, name: String) = ASSERT_RETURN_T(f, expected, ::is_equal_f64, name)

inline fun ASSERT_RETURN_CANONICAL_NAN_F32(f: () -> Float, name: String) = ASSERT_RETURN_NAN_T(f, ::is_canonical_nan_f32, name, "canonical")
inline fun ASSERT_RETURN_CANONICAL_NAN_F64(f: () -> Double, name: String) = ASSERT_RETURN_NAN_T(f, ::is_canonical_nan_f64, name, "canonical")
inline fun ASSERT_RETURN_ARITHMETIC_NAN_F32(f: () -> Float, name: String) = ASSERT_RETURN_NAN_T(f, ::is_arithmetic_nan_f32, name, "arithmetic")
inline fun ASSERT_RETURN_ARITHMETIC_NAN_F64(f: () -> Double, name: String) = ASSERT_RETURN_NAN_T(f, ::is_arithmetic_nan_f64, name, "arithmetic")

fun is_equal_u32(x: Int, y: Int): Boolean = x == y
fun is_equal_u64(x: Long, y: Long): Boolean = x == y
fun is_equal_f32(x: Float, y: Float): Boolean = x.toRawBits() == y.toRawBits()
fun is_equal_f64(x: Double, y: Double): Boolean = x.toRawBits() == y.toRawBits()

fun make_nan_f32(x: Int): Float = Float.fromBits(x or 0x7f800000)
fun make_nan_f64(x: Long): Double = Double.fromBits(x or 0x7ff0000000000000L)

fun is_canonical_nan_f32(x: Float):   Boolean = (x.toRawBits() and 0x7fffffff) == 0x7fc00000
fun is_arithmetic_nan_f32(x: Float):  Boolean = (x.toRawBits() and 0x7fc00000) == 0x7fc00000
fun is_canonical_nan_f64(x: Double):  Boolean = (x.toRawBits() and 0x7fffffffffffffffL) == 0x7ff8000000000000L
fun is_arithmetic_nan_f64(x: Double): Boolean = (x.toRawBits() and 0x7ff8000000000000L) == 0x7ff8000000000000L


class Z_spectest() {
  
  /*
   * spectest implementations
   */
  fun spectest_print() {
    print("spectest.print()\n");
  }
  
  fun spectest_print_i32(i: Int) {
    print("spectest.print_i32(");
    print(i);
    print(")\n");
  }
  
  fun spectest_print_f32(f: Float) {
    //printf("spectest.print_f32(%g)\n", f);
    print("spectest.print_f32(");
    print(f);
    print(")\n");
  }
  
  fun spectest_print_i32_f32(i: Int, f: Float) {
    //printf("spectest.print_i32_f32(%d %g)\n", i, f);
    print("spectest.print_i32_f32(");
    print(i);
    print(" ");
    print(f);
    print(")\n");
  }
  
  fun spectest_print_f64(d: Double) {
    //printf("spectest.print_f64(%g)\n", d);
    print("spectest.print_f64(");
    print(d);
    print(")\n");
  }
  
  fun spectest_print_f64_f64(d1: Double, d2: Double) {
    //printf("spectest.print_f64_f64(%g %g)\n", d1, d2);
    print("spectest.print_f64)f64(");
    print(d1);
    print(" ");
    print(d2);
    print(")\n");
  }
  
  var spectest_table: wasm_rt_impl.Table = wasm_rt_impl.Table(0, 0)
  var spectest_memory: wasm_rt_impl.Memory = wasm_rt_impl.Memory(0, 0)
  var spectest_global_i32: Int = 666
  var spectest_global_i64: Long = 666
  var spectest_global_f32: Float = 666f
  var spectest_global_f64: Double = 666.0
  
  var Z_printZ_vv: () -> Unit = this::spectest_print;
  var Z_print_i32Z_vi: (Int) -> Unit = this::spectest_print_i32;
  var Z_print_f32Z_vf: (Float) -> Unit = this::spectest_print_f32;
  var Z_print_i32_f32Z_vif: (Int, Float) -> Unit = this::spectest_print_i32_f32;
  var Z_print_f64Z_vd: (Double) -> Unit = this::spectest_print_f64;
  var Z_print_f64_f64Z_vdd: (Double, Double) -> Unit = this::spectest_print_f64_f64;

  var Z_table: wasm_rt_impl.Table by this@Z_spectest::spectest_table;
  var Z_memory: wasm_rt_impl.Memory by this@Z_spectest::spectest_memory;
  var Z_global_i32Z_i: Int by this@Z_spectest::spectest_global_i32;
  var Z_global_i64Z_i: Long by this@Z_spectest::spectest_global_i64;
  var Z_global_f32Z_i: Float by this@Z_spectest::spectest_global_f32;
  var Z_global_f64Z_i: Double by this@Z_spectest::spectest_global_f64;

  init {
    wasm_rt_impl.allocate_memory(this@Z_spectest::spectest_memory, 1, 2)
    wasm_rt_impl.allocate_table(this@Z_spectest::spectest_table, 10, 20)
  }
  
}

// workaround for run_spec_tests being MethodTooLarge
fun <R> runNoInline(f: () -> R): R = run { f() }

fun <T> run_test(fn: () -> T, mod: Z_spectest): T = fn()

fun <T> run_test(fn: (Z_spectest) -> T, mod: Z_spectest): T = fn(mod)

fun main(args: Array<String>) {
    val spectest = Z_spectest();
    run_spec_tests(spectest);

    print(g_tests_passed);
    print("/");
    print(g_tests_run);
    print(" tests passed.\n");

    if (g_tests_passed != g_tests_run) {
        System.exit(1);
    } else {
        System.exit(0);
    }
}
