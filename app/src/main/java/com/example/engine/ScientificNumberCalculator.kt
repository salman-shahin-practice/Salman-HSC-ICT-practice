package com.example.engine

import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cbrt
import kotlin.math.cos
import kotlin.math.cosh
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.sinh
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.math.tanh

enum class AngleUnit(val label: String) {
    DEG("DEG"),
    RAD("RAD"),
    GRA("GRA")
}

enum class NumberBase(val radix: Int, val shortLabel: String, val titleBn: String) {
    BIN(2, "BIN", "বাইনারি (২)"),
    OCT(8, "OCT", "অক্টাল (৮)"),
    DEC(10, "DEC", "ডেসিমাল (১০)"),
    HEX(16, "HEX", "হেক্সাডেসিমাল (১৬)")
}

enum class BaseOperation(val symbol: String, val labelBn: String, val bitwiseOnly: Boolean = false) {
    ADD("+", "যোগ (+)"),
    SUB("−", "বিয়োগ (−)"),
    MUL("×", "গুণ (×)"),
    DIV("÷", "ভাগ (÷)"),
    AND("AND", "Bitwise AND (&)", true),
    OR("OR", "Bitwise OR (|)", true),
    XOR("XOR", "Bitwise XOR (^)", true)
}

data class ScientificEvalResult(
    val success: Boolean,
    val formattedResult: String = "",
    val numericValue: Double? = null,
    val integerBaseSummary: BaseConversionResult? = null,
    val errorMessageBn: String? = null
)

data class BaseConversionResult(
    val valid: Boolean,
    val binary: String = "",
    val octal: String = "",
    val decimal: String = "",
    val hex: String = "",
    val stepsBn: List<String> = emptyList(),
    val errorMessageBn: String? = null
)

data class BaseArithmeticResult(
    val valid: Boolean,
    val conversion: BaseConversionResult? = null,
    val stepsBn: List<String> = emptyList(),
    val errorMessageBn: String? = null
)

data class TwosComplementResult(
    val valid: Boolean,
    val bitWidth: Int,
    val binaryResult: String = "",
    val decimalResult: Int = 0,
    val signMagnitude: String = "",
    val onesComplement: String = "",
    val twosComplement: String = "",
    val stepsBn: List<String> = emptyList(),
    val errorMessageBn: String? = null
)

object ScientificNumberCalculator {

    private sealed interface Token {
        data class Num(val value: Double) : Token
        data class Func(val name: String) : Token
        data class Op(val ch: Char) : Token
        data object LParen : Token
        data object RParen : Token
        data object Factorial : Token
        data object Percent : Token
    }

    fun evaluateScientificExpression(
        rawExpression: String,
        angleUnit: AngleUnit = AngleUnit.DEG,
        lastAnswer: Double = 0.0,
        variables: Map<String, Double> = emptyMap()
    ): ScientificEvalResult {
        val cleaned = rawExpression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("√", "sqrt")
            .replace("∛", "cbrt")
            .trim()

        if (cleaned.isEmpty()) {
            return ScientificEvalResult(success = false, errorMessageBn = "হিসাব করার জন্য সমীকরণ লিখুন।")
        }

        return try {
            val tokens = tokenizeScientific(cleaned, lastAnswer, variables)
            val parser = RecursiveParser(tokens, angleUnit)
            val value = parser.parse()
            if (value.isNaN() || value.isInfinite()) {
                ScientificEvalResult(success = false, errorMessageBn = "অসংজ্ঞায়িত ফলাফল (Undefined / Infinity)।")
            } else {
                val formatted = formatDoubleClean(value)
                val roundedLong = value.toLong()
                val baseSummary = if (abs(value - roundedLong.toDouble()) < 1e-9 &&
                    abs(value) <= 1_000_000_000_000.0
                ) {
                    convertNumberBase(roundedLong.toString(), NumberBase.DEC, NumberBase.BIN)
                } else {
                    null
                }
                ScientificEvalResult(
                    success = true,
                    formattedResult = formatted,
                    numericValue = value,
                    integerBaseSummary = baseSummary
                )
            }
        } catch (e: IllegalArgumentException) {
            ScientificEvalResult(success = false, errorMessageBn = e.message ?: "অবৈধ গাণিতিক সমীকরণ।")
        } catch (_: Exception) {
            ScientificEvalResult(success = false, errorMessageBn = "সমীকরণটি সঠিকভাবে লেখা হয়নি।")
        }
    }

    private fun tokenizeScientific(
        expr: String,
        lastAnswer: Double,
        variables: Map<String, Double> = emptyMap()
    ): List<Token> {
        val raw = mutableListOf<Token>()
        var i = 0
        while (i < expr.length) {
            val ch = expr[i]
            when {
                ch.isWhitespace() -> i++
                ch.isDigit() || ch == '.' -> {
                    val start = i
                    var dotCount = 0
                    while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) {
                        if (expr[i] == '.') dotCount++
                        if (dotCount > 1) throw IllegalArgumentException("সংখ্যায় একাধিক দশমিক বিন্দু (.) দেওয়া যাবে না।")
                        i++
                    }
                    // Optional scientific E notation (e.g. 1.5E-3)
                    if (i < expr.length && (expr[i] == 'E' || expr[i] == 'e') &&
                        (i + 1 < expr.length && (expr[i + 1].isDigit() || expr[i + 1] == '+' || expr[i + 1] == '-'))
                    ) {
                        i++
                        if (i < expr.length && (expr[i] == '+' || expr[i] == '-')) i++
                        val expDigitsStart = i
                        while (i < expr.length && expr[i].isDigit()) i++
                        if (i == expDigitsStart) throw IllegalArgumentException("অবৈধ বৈজ্ঞানিক সূচক (E)।")
                    }
                    val numStr = expr.substring(start, i)
                    val num = numStr.toDoubleOrNull()
                        ?: throw IllegalArgumentException("অবৈধ সংখ্যা: $numStr")
                    raw.add(Token.Num(num))
                }
                ch == 'π' -> {
                    raw.add(Token.Num(PI))
                    i++
                }
                ch.isLetter() -> {
                    val start = i
                    while (i < expr.length && expr[i].isLetter()) i++
                    val rawWord = expr.substring(start, i)
                    val word = rawWord.lowercase()
                    when (word) {
                        "pi" -> raw.add(Token.Num(PI))
                        "e" -> raw.add(Token.Num(kotlin.math.E))
                        "ans" -> raw.add(Token.Num(lastAnswer))
                        "sin", "cos", "tan", "asin", "acos", "atan",
                        "sinh", "cosh", "tanh", "asinh", "acosh", "atanh",
                        "log", "ln", "sqrt", "cbrt", "abs", "exp" -> raw.add(Token.Func(word))
                        "mod" -> raw.add(Token.Op('m'))
                        "p" -> raw.add(Token.Op('P'))
                        "c" -> {
                            val upper = rawWord
                            if (upper == "C" && variables.containsKey("C") &&
                                (raw.isEmpty() || raw.last() is Token.Op || raw.last() is Token.LParen)
                            ) {
                                raw.add(Token.Num(variables["C"] ?: 0.0))
                            } else {
                                raw.add(Token.Op('C'))
                            }
                        }
                        else -> {
                            val varVal = variables[rawWord.uppercase()] ?: variables[rawWord]
                            if (varVal != null) {
                                raw.add(Token.Num(varVal))
                            } else {
                                throw IllegalArgumentException("অজানা ফাংশন বা চিহ্ন: $rawWord")
                            }
                        }
                    }
                }
                ch == '(' -> {
                    raw.add(Token.LParen)
                    i++
                }
                ch == ')' -> {
                    raw.add(Token.RParen)
                    i++
                }
                ch == '!' -> {
                    raw.add(Token.Factorial)
                    i++
                }
                ch == '%' -> {
                    raw.add(Token.Percent)
                    i++
                }
                ch in charArrayOf('+', '-', '*', '/', '^') -> {
                    raw.add(Token.Op(ch))
                    i++
                }
                else -> throw IllegalArgumentException("অবৈধ অক্ষর: $ch")
            }
        }

        // Insert implicit multiplication where appropriate:
        // e.g., 2π, 2(3), (2)(3), 3sin(30), 5!2, 50%(2)
        val result = mutableListOf<Token>()
        for (idx in raw.indices) {
            val curr = raw[idx]
            if (result.isNotEmpty()) {
                val prev = result.last()
                val prevCanMultiply = prev is Token.Num || prev is Token.RParen ||
                    prev is Token.Factorial || prev is Token.Percent
                val currCanMultiply = curr is Token.Num || curr is Token.Func || curr is Token.LParen
                if (prevCanMultiply && currCanMultiply) {
                    result.add(Token.Op('*'))
                }
            }
            result.add(curr)
        }
        return result
    }

    private class RecursiveParser(
        private val tokens: List<Token>,
        private val angleUnit: AngleUnit
    ) {
        private var pos = 0

        fun parse(): Double {
            if (tokens.isEmpty()) throw IllegalArgumentException("সমীকরণ খালি।")
            val value = parseAddSub()
            if (pos < tokens.size) {
                throw IllegalArgumentException("সমীকরণে অতিরিক্ত বা অসমাপ্ত বন্ধনী রয়েছে।")
            }
            return value
        }

        private fun peek(): Token? = tokens.getOrNull(pos)

        private fun parseAddSub(): Double {
            var left = parseMulDiv()
            while (true) {
                val op = (peek() as? Token.Op)?.ch
                if (op == '+' || op == '-') {
                    pos++
                    val right = parseMulDiv()
                    left = if (op == '+') left + right else left - right
                } else {
                    break
                }
            }
            return left
        }

        private fun parseMulDiv(): Double {
            var left = parseUnary()
            while (true) {
                val op = (peek() as? Token.Op)?.ch
                when (op) {
                    '*' -> {
                        pos++
                        left *= parseUnary()
                    }
                    '/' -> {
                        pos++
                        val right = parseUnary()
                        if (abs(right) < 1e-15) {
                            throw IllegalArgumentException("শূন্য দিয়ে ভাগ করা যায় না।")
                        }
                        left /= right
                    }
                    'm' -> {
                        pos++
                        val right = parseUnary()
                        if (abs(right) < 1e-15) {
                            throw IllegalArgumentException("শূন্য দিয়ে মডুলাস (mod) করা যায় না।")
                        }
                        left %= right
                    }
                    else -> break
                }
            }
            return left
        }

        private fun parseUnary(): Double {
            val op = (peek() as? Token.Op)?.ch
            return if (op == '+' || op == '-') {
                pos++
                val inner = parseUnary()
                if (op == '-') -inner else inner
            } else {
                parsePower()
            }
        }

        private fun parsePower(): Double {
            var base = parsePostfix()
            while (true) {
                val op = (peek() as? Token.Op)?.ch
                when (op) {
                    '^' -> {
                        pos++
                        val exponent = parseUnary()
                        base = base.pow(exponent)
                    }
                    'P' -> {
                        pos++
                        val r = parseUnary()
                        base = computePermutation(base, r)
                    }
                    'C' -> {
                        pos++
                        val r = parseUnary()
                        base = computeCombination(base, r)
                    }
                    else -> break
                }
            }
            return base
        }

        private fun parsePostfix(): Double {
            var value = parsePrimary()
            while (true) {
                when (peek()) {
                    is Token.Factorial -> {
                        pos++
                        value = computeFactorial(value)
                    }
                    is Token.Percent -> {
                        pos++
                        value /= 100.0
                    }
                    else -> break
                }
            }
            return value
        }

        private fun parsePrimary(): Double {
            return when (val tok = peek()) {
                is Token.Num -> {
                    pos++
                    tok.value
                }
                is Token.LParen -> {
                    pos++
                    val inner = parseAddSub()
                    if (peek() !is Token.RParen) {
                        throw IllegalArgumentException("বন্ধনী ')' শেষ করা হয়নি।")
                    }
                    pos++
                    inner
                }
                is Token.Func -> {
                    pos++
                    val arg = if (peek() is Token.LParen) {
                        pos++
                        val inner = parseAddSub()
                        if (peek() !is Token.RParen) {
                            throw IllegalArgumentException("${tok.name}(...) এর বন্ধনী ')' শেষ করা হয়নি।")
                        }
                        pos++
                        inner
                    } else {
                        parseUnary()
                    }
                    applyFunction(tok.name, arg)
                }
                else -> throw IllegalArgumentException("অসম্পূর্ণ বা অপ্রত্যাশিত চিহ্ন।")
            }
        }

        private fun applyFunction(name: String, arg: Double): Double {
            fun toRad(value: Double): Double = when (angleUnit) {
                AngleUnit.DEG -> Math.toRadians(value)
                AngleUnit.RAD -> value
                AngleUnit.GRA -> value * PI / 200.0
            }

            fun fromRad(rad: Double): Double = when (angleUnit) {
                AngleUnit.DEG -> Math.toDegrees(rad)
                AngleUnit.RAD -> rad
                AngleUnit.GRA -> rad * 200.0 / PI
            }

            return when (name) {
                "sin" -> snapTrig(sin(toRad(arg)))
                "cos" -> snapTrig(cos(toRad(arg)))
                "tan" -> {
                    val rad = toRad(arg)
                    val c = cos(rad)
                    if (abs(c) < 1e-12) {
                        throw IllegalArgumentException("tan(${formatDoubleClean(arg)}) অসংজ্ঞায়িত।")
                    }
                    snapTrig(tan(rad))
                }
                "asin" -> {
                    if (arg < -1.0 || arg > 1.0) throw IllegalArgumentException("asin ইনপুট অবশ্যই -1 থেকে 1 এর মধ্যে হতে হবে।")
                    fromRad(asin(arg))
                }
                "acos" -> {
                    if (arg < -1.0 || arg > 1.0) throw IllegalArgumentException("acos ইনপুট অবশ্যই -1 থেকে 1 এর মধ্যে হতে হবে।")
                    fromRad(acos(arg))
                }
                "atan" -> fromRad(atan(arg))
                "sinh" -> sinh(arg)
                "cosh" -> cosh(arg)
                "tanh" -> tanh(arg)
                "asinh" -> ln(arg + sqrt(arg * arg + 1.0))
                "acosh" -> {
                    if (arg < 1.0) throw IllegalArgumentException("acosh ইনপুট অবশ্যই >= 1 হতে হবে।")
                    ln(arg + sqrt(arg * arg - 1.0))
                }
                "atanh" -> {
                    if (arg <= -1.0 || arg >= 1.0) throw IllegalArgumentException("atanh ইনপুট (-1, 1) এর মধ্যে হতে হবে।")
                    0.5 * ln((1.0 + arg) / (1.0 - arg))
                }
                "log" -> {
                    if (arg <= 0.0) throw IllegalArgumentException("log এর ইনপুট অবশ্যই ধনাত্মক (> 0) হতে হবে।")
                    log10(arg)
                }
                "ln" -> {
                    if (arg <= 0.0) throw IllegalArgumentException("ln এর ইনপুট অবশ্যই ধনাত্মক (> 0) হতে হবে।")
                    ln(arg)
                }
                "sqrt" -> {
                    if (arg < 0.0) throw IllegalArgumentException("ঋণাত্মক সংখ্যার বাস্তব বর্গমূল হয় না।")
                    sqrt(arg)
                }
                "cbrt" -> cbrt(arg)
                "abs" -> abs(arg)
                "exp" -> exp(arg)
                else -> throw IllegalArgumentException("অজানা ফাংশন: $name")
            }
        }

        private fun snapTrig(v: Double): Double {
            return when {
                abs(v) < 1e-12 -> 0.0
                abs(v - 0.5) < 1e-12 -> 0.5
                abs(v + 0.5) < 1e-12 -> -0.5
                abs(v - 1.0) < 1e-12 -> 1.0
                abs(v + 1.0) < 1e-12 -> -1.0
                else -> v
            }
        }

        private fun computeFactorial(v: Double): Double {
            if (v < 0 || abs(v - v.toLong().toDouble()) > 1e-9) {
                throw IllegalArgumentException("ফ্যাক্টোরিয়াল (!) শুধু অঋণাত্মক পূর্ণসংখ্যার জন্য প্রযোজ্য।")
            }
            val n = v.toLong()
            if (n > 170) throw IllegalArgumentException("মান অনেক বড় (সর্বোচ্চ ১৭০!)।")
            var acc = 1.0
            for (k in 2L..n) acc *= k.toDouble()
            return acc
        }

        private fun computePermutation(nVal: Double, rVal: Double): Double {
            val n = nVal.roundToLong()
            val r = rVal.roundToLong()
            if (abs(nVal - n) > 1e-9 || abs(rVal - r) > 1e-9 || n < 0 || r < 0 || r > n) {
                throw IllegalArgumentException("nPr এর জন্য অঋণাত্মক পূর্ণসংখ্যা n >= r >= 0 হতে হবে।")
            }
            var acc = 1.0
            for (k in (n - r + 1)..n) acc *= k.toDouble()
            return acc
        }

        private fun computeCombination(nVal: Double, rVal: Double): Double {
            val n = nVal.roundToLong()
            val r = rVal.roundToLong()
            if (abs(nVal - n) > 1e-9 || abs(rVal - r) > 1e-9 || n < 0 || r < 0 || r > n) {
                throw IllegalArgumentException("nCr এর জন্য অঋণাত্মক পূর্ণসংখ্যা n >= r >= 0 হতে হবে।")
            }
            val kMax = minOf(r, n - r)
            var res = 1.0
            for (i in 1..kMax) {
                res = res * (n - kMax + i).toDouble() / i.toDouble()
            }
            return res
        }
    }

    fun formatDoubleClean(value: Double): String {
        if (abs(value) < 1e-12) return "0"
        val bd = BigDecimal.valueOf(value)
            .round(MathContext(12, RoundingMode.HALF_UP))
            .stripTrailingZeros()
        return bd.toPlainString()
    }

    // -------------------------------------------------------------------------
    // Number System Converter (BIN, OCT, DEC, HEX) with Fractional Support
    // -------------------------------------------------------------------------

    fun convertNumberBase(
        rawInput: String,
        fromBase: NumberBase,
        explainTargetBase: NumberBase = if (fromBase == NumberBase.DEC) NumberBase.BIN else NumberBase.DEC
    ): BaseConversionResult {
        val cleaned = rawInput.trim().uppercase()
        if (cleaned.isEmpty()) {
            return BaseConversionResult(valid = false, errorMessageBn = "রূপান্তরের জন্য একটি সংখ্যা লিখুন।")
        }

        val negative = cleaned.startsWith("-")
        val unsigned = when {
            cleaned.startsWith("-") || cleaned.startsWith("+") -> cleaned.substring(1)
            else -> cleaned
        }

        if (unsigned.isEmpty() || unsigned == ".") {
            return BaseConversionResult(valid = false, errorMessageBn = "সঠিক সংখ্যা লিখুন।")
        }

        val parts = unsigned.split(".")
        if (parts.size > 2) {
            return BaseConversionResult(valid = false, errorMessageBn = "একাধিক রেডিক্স পয়েন্ট (.) ব্যবহার করা যাবে না।")
        }

        val intStr = parts[0].ifEmpty { "0" }
        val fracStr = if (parts.size == 2) parts[1] else ""

        if (!isValidDigitsForBase(intStr, fromBase) || (fracStr.isNotEmpty() && !isValidDigitsForBase(fracStr, fromBase))) {
            return BaseConversionResult(
                valid = false,
                errorMessageBn = "${fromBase.titleBn} পদ্ধতিতে অবৈধ অঙ্ক রয়েছে।"
            )
        }

        val intBig = BigInteger(intStr, fromBase.radix)
        val (fracNum, fracDen) = parseFractionRational(fracStr, fromBase.radix)

        val binStr = formatRationalInBase(negative, intBig, fracNum, fracDen, NumberBase.BIN)
        val octStr = formatRationalInBase(negative, intBig, fracNum, fracDen, NumberBase.OCT)
        val decStr = formatRationalInBase(negative, intBig, fracNum, fracDen, NumberBase.DEC)
        val hexStr = formatRationalInBase(negative, intBig, fracNum, fracDen, NumberBase.HEX)

        val steps = buildConversionSteps(
            unsigned = "${intStr}${if (fracStr.isNotEmpty()) ".$fracStr" else ""}",
            negative = negative,
            intStr = intStr,
            fracStr = fracStr,
            intBig = intBig,
            fracNum = fracNum,
            fracDen = fracDen,
            fromBase = fromBase,
            toBase = explainTargetBase,
            convertedTarget = when (explainTargetBase) {
                NumberBase.BIN -> binStr
                NumberBase.OCT -> octStr
                NumberBase.DEC -> decStr
                NumberBase.HEX -> hexStr
            }
        )

        return BaseConversionResult(
            valid = true,
            binary = binStr,
            octal = octStr,
            decimal = decStr,
            hex = hexStr,
            stepsBn = steps
        )
    }

    private fun isValidDigitsForBase(text: String, base: NumberBase): Boolean {
        if (text.isEmpty()) return false
        return text.all { ch ->
            val digit = when (ch) {
                in '0'..'9' -> ch - '0'
                in 'A'..'F' -> 10 + (ch - 'A')
                else -> 99
            }
            digit < base.radix
        }
    }

    private fun parseFractionRational(fracStr: String, radix: Int): Pair<BigInteger, BigInteger> {
        if (fracStr.isEmpty()) return BigInteger.ZERO to BigInteger.ONE
        val baseBig = BigInteger.valueOf(radix.toLong())
        var num = BigInteger.ZERO
        var den = BigInteger.ONE
        for (ch in fracStr) {
            val digit = Character.digit(ch, radix)
            num = num.multiply(baseBig).add(BigInteger.valueOf(digit.toLong()))
            den = den.multiply(baseBig)
        }
        return num to den
    }

    private fun formatRationalInBase(
        negative: Boolean,
        intBig: BigInteger,
        fracNum: BigInteger,
        fracDen: BigInteger,
        targetBase: NumberBase
    ): String {
        val sign = if (negative && (intBig > BigInteger.ZERO || fracNum > BigInteger.ZERO)) "-" else ""
        val intOut = intBig.toString(targetBase.radix).uppercase()
        if (fracNum == BigInteger.ZERO) return "$sign$intOut"

        val baseBig = BigInteger.valueOf(targetBase.radix.toLong())
        var rem = fracNum
        val sb = StringBuilder()
        val maxDigits = if (targetBase == NumberBase.DEC) 8 else 10
        var count = 0
        while (rem > BigInteger.ZERO && count < maxDigits) {
            val prod = rem.multiply(baseBig)
            val digit = prod.divide(fracDen).toInt()
            rem = prod.remainder(fracDen)
            sb.append(Character.forDigit(digit, targetBase.radix).uppercaseChar())
            count++
        }
        val trimmedFrac = sb.toString().trimEnd('0')
        return if (trimmedFrac.isEmpty()) "$sign$intOut" else "$sign$intOut.$trimmedFrac"
    }

    private fun buildConversionSteps(
        unsigned: String,
        negative: Boolean,
        intStr: String,
        fracStr: String,
        intBig: BigInteger,
        fracNum: BigInteger,
        fracDen: BigInteger,
        fromBase: NumberBase,
        toBase: NumberBase,
        convertedTarget: String
    ): List<String> {
        val steps = mutableListOf<String>()
        val signPrefix = if (negative) "−" else ""
        steps.add("প্রদত্ত সংখ্যা: $signPrefix($unsigned)${subscript(fromBase.radix)} → রূপান্তর: ${toBase.titleBn}")

        if (fromBase == toBase) {
            steps.add("একই সংখ্যা পদ্ধতি হওয়ায় মান অপরিবর্তিত: $convertedTarget")
            return steps
        }

        // Case 1: Decimal -> Other Base (Division & Multiplication)
        if (fromBase == NumberBase.DEC) {
            val baseBig = BigInteger.valueOf(toBase.radix.toLong())
            if (intBig == BigInteger.ZERO) {
                steps.add("পূর্ণাংশ: 0 ÷ ${toBase.radix} = 0")
            } else {
                var cur = intBig
                val remainders = mutableListOf<String>()
                var limit = 0
                while (cur > BigInteger.ZERO && limit < 16) {
                    val q = cur.divide(baseBig)
                    val r = cur.remainder(baseBig).toInt()
                    val rLabel = if (toBase == NumberBase.HEX && r >= 10) {
                        "$r (${('A' + (r - 10))})"
                    } else {
                        "$r"
                    }
                    steps.add("$cur ÷ ${toBase.radix} → ভাগফল = $q, ভাগশেষ = $rLabel")
                    remainders.add(Character.forDigit(r, toBase.radix).uppercaseChar().toString())
                    cur = q
                    limit++
                }
                steps.add("নিচ থেকে উপরে (MSB → LSB) সাজিয়ে পূর্ণাংশ = ${remainders.reversed().joinToString("")}")
            }

            if (fracNum > BigInteger.ZERO) {
                var rem = fracNum
                val carried = mutableListOf<String>()
                var stepCount = 0
                while (rem > BigInteger.ZERO && stepCount < 6) {
                    val decBefore = BigDecimal(rem).divide(BigDecimal(fracDen), 4, RoundingMode.HALF_UP)
                        .stripTrailingZeros().toPlainString()
                    val prod = rem.multiply(baseBig)
                    val digit = prod.divide(fracDen).toInt()
                    rem = prod.remainder(fracDen)
                    val decAfter = BigDecimal(rem).divide(BigDecimal(fracDen), 4, RoundingMode.HALF_UP)
                        .stripTrailingZeros().toPlainString()
                    val dChar = Character.forDigit(digit, toBase.radix).uppercaseChar().toString()
                    steps.add("ভগ্নাংশ: $decBefore × ${toBase.radix} = ${digit}${decAfter.removePrefix("0")} → পূর্ণাংশ = $dChar")
                    carried.add(dChar)
                    stepCount++
                }
                steps.add("উপর থেকে নিচে ভগ্নাংশ = .${carried.joinToString("")}")
            }
            steps.add("চূড়ান্ত উত্তর: ($convertedTarget)${subscript(toBase.radix)}")
            return steps
        }

        // Case 2: Any Base -> Decimal (Positional Weight Expansion)
        if (toBase == NumberBase.DEC) {
            val intTerms = intStr.mapIndexed { idx, ch ->
                val power = intStr.length - 1 - idx
                val d = Character.digit(ch, fromBase.radix)
                "$d×${fromBase.radix}^$power"
            }
            val fracTerms = fracStr.mapIndexed { idx, ch ->
                val power = -(idx + 1)
                val d = Character.digit(ch, fromBase.radix)
                "$d×${fromBase.radix}^($power)"
            }
            val allTerms = (intTerms + fracTerms).take(8)
            val ellipsis = if (intTerms.size + fracTerms.size > 8) " + ..." else ""
            steps.add("স্থানীয় মান বিস্তার: ${allTerms.joinToString(" + ")}$ellipsis")
            steps.add("যোগফল = $convertedTarget (ডেসিমাল)")
            return steps
        }

        // Case 3: Binary <-> Octal (3-bit) or Binary <-> Hex (4-bit)
        if ((fromBase == NumberBase.BIN && (toBase == NumberBase.OCT || toBase == NumberBase.HEX)) ||
            ((fromBase == NumberBase.OCT || fromBase == NumberBase.HEX) && toBase == NumberBase.BIN)
        ) {
            val groupSize = if (fromBase == NumberBase.OCT || toBase == NumberBase.OCT) 3 else 4
            if (fromBase == NumberBase.BIN) {
                val padIntLen = ((intStr.length + groupSize - 1) / groupSize) * groupSize
                val paddedInt = intStr.padStart(padIntLen, '0')
                val intGroups = paddedInt.chunked(groupSize)
                val mappedInt = intGroups.map { grp ->
                    val v = grp.toInt(2)
                    "$grp→${Character.forDigit(v, toBase.radix).uppercaseChar()}"
                }
                steps.add("$groupSize-বিট গ্রুপিং (পূর্ণাংশ): ${mappedInt.joinToString(" | ")}")
                if (fracStr.isNotEmpty()) {
                    val padFracLen = ((fracStr.length + groupSize - 1) / groupSize) * groupSize
                    val paddedFrac = fracStr.padEnd(padFracLen, '0')
                    val fracGroups = paddedFrac.chunked(groupSize)
                    val mappedFrac = fracGroups.map { grp ->
                        val v = grp.toInt(2)
                        "$grp→${Character.forDigit(v, toBase.radix).uppercaseChar()}"
                    }
                    steps.add("$groupSize-বিট গ্রুপিং (ভগ্নাংশ): ${mappedFrac.joinToString(" | ")}")
                }
            } else {
                val mappedInt = intStr.map { ch ->
                    val v = Character.digit(ch, fromBase.radix)
                    "$ch→${v.toString(2).padStart(groupSize, '0')}"
                }
                steps.add("প্রতিটি অঙ্কের $groupSize-বিট বাইনারি: ${mappedInt.joinToString(" | ")}")
                if (fracStr.isNotEmpty()) {
                    val mappedFrac = fracStr.map { ch ->
                        val v = Character.digit(ch, fromBase.radix)
                        "$ch→${v.toString(2).padStart(groupSize, '0')}"
                    }
                    steps.add("ভগ্নাংশের $groupSize-বিট বাইনারি: ${mappedFrac.joinToString(" | ")}")
                }
            }
            steps.add("চূড়ান্ত উত্তর: ($convertedTarget)${subscript(toBase.radix)}")
            return steps
        }

        // Case 4: Octal <-> Hex via Binary bridge
        val binBridge = formatRationalInBase(negative, intBig, fracNum, fracDen, NumberBase.BIN)
        steps.add("ধাপ ১: ${fromBase.titleBn} → ৩/৪-বিট বাইনারি = ($binBridge)₂")
        steps.add("ধাপ ২: বাইনারি সংখ্যাকে ${if (toBase == NumberBase.HEX) "৪-বিট" else "৩-বিট"} গ্রুপে সাজিয়ে = ($convertedTarget)${subscript(toBase.radix)}")
        return steps
    }

    // -------------------------------------------------------------------------
    // Number System Arithmetic (+, -, *, /, AND, OR, XOR)
    // -------------------------------------------------------------------------

    fun evaluateBaseArithmetic(
        leftRaw: String,
        rightRaw: String,
        base: NumberBase,
        op: BaseOperation
    ): BaseArithmeticResult {
        val leftConv = convertNumberBase(leftRaw, base, NumberBase.DEC)
        if (!leftConv.valid) {
            return BaseArithmeticResult(valid = false, errorMessageBn = "প্রথম সংখ্যা: ${leftConv.errorMessageBn}")
        }
        val rightConv = convertNumberBase(rightRaw, base, NumberBase.DEC)
        if (!rightConv.valid) {
            return BaseArithmeticResult(valid = false, errorMessageBn = "দ্বিতীয় সংখ্যা: ${rightConv.errorMessageBn}")
        }

        val leftDec = leftConv.decimal.toBigDecimalOrNull()
            ?: return BaseArithmeticResult(valid = false, errorMessageBn = "প্রথম সংখ্যা পড়া যায়নি।")
        val rightDec = rightConv.decimal.toBigDecimalOrNull()
            ?: return BaseArithmeticResult(valid = false, errorMessageBn = "দ্বিতীয় সংখ্যা পড়া যায়নি।")

        if (op.bitwiseOnly) {
            if (leftRaw.contains('.') || rightRaw.contains('.')) {
                return BaseArithmeticResult(
                    valid = false,
                    errorMessageBn = "Bitwise অপারেশন (${op.symbol}) শুধু পূর্ণসংখ্যার জন্য প্রযোজ্য।"
                )
            }
            val a = leftDec.toBigInteger()
            val b = rightDec.toBigInteger()
            val resBig = when (op) {
                BaseOperation.AND -> a.and(b)
                BaseOperation.OR -> a.or(b)
                BaseOperation.XOR -> a.xor(b)
                else -> BigInteger.ZERO
            }
            val resConv = convertNumberBase(resBig.toString(), NumberBase.DEC, base)
            val width = maxOf(a.abs().bitLength(), b.abs().bitLength(), 4).coerceAtMost(32)
            val aBin = a.toString(2).padStart(width, '0')
            val bBin = b.toString(2).padStart(width, '0')
            val rBin = resBig.toString(2).padStart(width, '0')
            val steps = listOf(
                "A (${base.shortLabel}) = ${leftRaw.trim().uppercase()} → Binary: $aBin",
                "B (${base.shortLabel}) = ${rightRaw.trim().uppercase()} → Binary: $bBin",
                "$aBin ${op.symbol} $bBin = $rBin",
                "ফলাফল (${base.shortLabel}) = ${resConv.inBase(base)} | (DEC: ${resConv.decimal})"
            )
            return BaseArithmeticResult(valid = true, conversion = resConv, stepsBn = steps)
        }

        val resDec = try {
            when (op) {
                BaseOperation.ADD -> leftDec.add(rightDec)
                BaseOperation.SUB -> leftDec.subtract(rightDec)
                BaseOperation.MUL -> leftDec.multiply(rightDec)
                BaseOperation.DIV -> {
                    if (rightDec.compareTo(BigDecimal.ZERO) == 0) {
                        return BaseArithmeticResult(valid = false, errorMessageBn = "শূন্য দিয়ে ভাগ করা যায় না।")
                    }
                    leftDec.divide(rightDec, 8, RoundingMode.HALF_UP)
                }
                else -> BigDecimal.ZERO
            }.stripTrailingZeros()
        } catch (_: Exception) {
            return BaseArithmeticResult(valid = false, errorMessageBn = "হিসাব করতে সমস্যা হয়েছে।")
        }

        val resConv = convertNumberBase(resDec.toPlainString(), NumberBase.DEC, base)
        val steps = listOf(
            "প্রথম সংখ্যা: ${leftRaw.trim().uppercase()}${subscript(base.radix)} = ${leftConv.decimal}₁₀",
            "দ্বিতীয় সংখ্যা: ${rightRaw.trim().uppercase()}${subscript(base.radix)} = ${rightConv.decimal}₁₀",
            "ডেসিমালে হিসাব: ${leftConv.decimal} ${op.symbol} ${rightConv.decimal} = ${resDec.toPlainString()}",
            "ফলাফল (${base.shortLabel}) = ${resConv.inBase(base)}${subscript(base.radix)} | Binary = ${resConv.binary}₂"
        )
        return BaseArithmeticResult(valid = true, conversion = resConv, stepsBn = steps)
    }

    // -------------------------------------------------------------------------
    // 2's Complement (২-এর পরিপূরক) Signed Binary Calculator (8-bit / 16-bit)
    // -------------------------------------------------------------------------

    fun computeTwosComplement(
        valueA: Int,
        valueB: Int? = null,
        isSubtract: Boolean = true,
        bitWidth: Int = 8
    ): TwosComplementResult {
        val bits = if (bitWidth == 16) 16 else 8
        val minVal = -(1 shl (bits - 1))
        val maxVal = (1 shl (bits - 1)) - 1

        if (valueA !in minVal..maxVal) {
            return TwosComplementResult(
                valid = false,
                bitWidth = bits,
                errorMessageBn = "$bits-বিট রেজিস্টারে মান $minVal থেকে +$maxVal এর মধ্যে হতে হবে।"
            )
        }

        if (valueB == null) {
            val absBin = abs(valueA).toString(2).padStart(bits, '0')
            val signMag = if (valueA >= 0) {
                "0" + abs(valueA).toString(2).padStart(bits - 1, '0')
            } else {
                "1" + abs(valueA).toString(2).padStart(bits - 1, '0')
            }
            val ones = invertBits(absBin)
            val twos = toTwosComplementBits(valueA, bits)
            val steps = mutableListOf(
                "সংখ্যা = $valueA ($bits-বিট রেজিস্টার)",
                "|${valueA}| এর $bits-বিট বাইনারি (প্রকৃত মান): $absBin"
            )
            if (valueA < 0) {
                steps.add("১-এর পরিপূরক (বিট উল্টে): $ones")
                steps.add("২-এর পরিপূরক (+1 যোগ করে): $twos")
            } else {
                steps.add("ধনাত্মক সংখ্যার ক্ষেত্রে ২-এর পরিপূরক = প্রকৃত মান = $twos")
                steps.add("(বিপরীত চিহ্ন -$valueA পেতে: ১-এর পরিপূরক $ones + 1 = ${toTwosComplementBits(-valueA, bits)})")
            }
            return TwosComplementResult(
                valid = true,
                bitWidth = bits,
                binaryResult = twos,
                decimalResult = valueA,
                signMagnitude = signMag,
                onesComplement = ones,
                twosComplement = twos,
                stepsBn = steps
            )
        }

        if (valueB !in minVal..maxVal) {
            return TwosComplementResult(
                valid = false,
                bitWidth = bits,
                errorMessageBn = "দ্বিতীয় সংখ্যা $minVal থেকে +$maxVal এর মধ্যে হতে হবে।"
            )
        }

        val effectiveB = if (isSubtract) -valueB else valueB
        val total = valueA + effectiveB
        if (total !in minVal..maxVal) {
            return TwosComplementResult(
                valid = false,
                bitWidth = bits,
                errorMessageBn = "ফলাফল ($total) $bits-বিট রেঞ্জ ($minVal..+$maxVal) অতিক্রম করেছে (Overflow)। 16-bit নির্বাচন করুন।"
            )
        }

        val aBits = toTwosComplementBits(valueA, bits)
        val bMagBits = abs(effectiveB).toString(2).padStart(bits, '0')
        val bOnes = invertBits(bMagBits)
        val bTwos = toTwosComplementBits(effectiveB, bits)

        val mask = (1 shl bits) - 1
        val rawSum = (valueA and mask) + (effectiveB and mask)
        val hasCarry = rawSum > mask
        val resultBits = (rawSum and mask).toString(2).padStart(bits, '0')
        val signBit = resultBits.first()

        val steps = mutableListOf<String>()
        val opLabel = if (isSubtract) "$valueA − ($valueB) = $valueA + ($effectiveB)" else "$valueA + ($valueB)"
        steps.add("সমস্যা: $opLabel ($bits-বিট ২-এর পরিপূরক পদ্ধতি)")
        steps.add("A = $valueA → $bits-বিট: $aBits")
        if (effectiveB < 0) {
            steps.add("|${effectiveB}| এর প্রকৃত মান: $bMagBits")
            steps.add("১-এর পরিপূরক: $bOnes → (+1) → ২-এর পরিপূরক: $bTwos")
        } else {
            steps.add("B = +$effectiveB → $bits-বিট: $bTwos")
        }
        steps.add("বাইনারি যোগ: $aBits + $bTwos = ${if (hasCarry) "1(Carry) " else ""}$resultBits")
        if (hasCarry) {
            steps.add("অতিরিক্ত ক্যারি (Carry Bit = 1) বাদ দেওয়া হলো → $resultBits")
        }
        if (signBit == '0') {
            steps.add("সাইন বিট (MSB) = 0 → ফলাফল ধনাত্মক: +$total")
        } else {
            val resOnes = invertBits(resultBits)
            val resMag = toTwosComplementBits(-total, bits)
            steps.add("সাইন বিট (MSB) = 1 → ফলাফল ঋণাত্মক। এর ২-এর পরিপূরক ($resOnes + 1 = $resMag) = $total")
        }

        return TwosComplementResult(
            valid = true,
            bitWidth = bits,
            binaryResult = resultBits,
            decimalResult = total,
            signMagnitude = if (total >= 0) resultBits else "1" + abs(total).toString(2).padStart(bits - 1, '0'),
            onesComplement = invertBits(resultBits),
            twosComplement = resultBits,
            stepsBn = steps
        )
    }

    private fun invertBits(bits: String): String =
        bits.map { if (it == '0') '1' else '0' }.joinToString("")

    private fun toTwosComplementBits(value: Int, bits: Int): String {
        val mask = (1 shl bits) - 1
        return (value and mask).toString(2).padStart(bits, '0')
    }

    private fun subscript(radix: Int): String = when (radix) {
        2 -> "₂"
        8 -> "₈"
        10 -> "₁₀"
        16 -> "₁₆"
        else -> "($radix)"
    }

    // -------------------------------------------------------------------------
    // Casio fx-100MS & fx-991MS Advanced Features (Fraction, SD, EQN, CMPLX, Calculus)
    // -------------------------------------------------------------------------

    fun toFractionString(value: Double, maxDen: Int = 1000): String {
        if (value.isNaN() || value.isInfinite()) return formatDoubleClean(value)
        val sign = if (value < 0) "-" else ""
        val absVal = abs(value)
        val whole = absVal.toLong()
        val frac = absVal - whole
        if (frac < 1e-9) return "$sign$whole"

        var bestNum = 0L
        var bestDen = 1L
        var bestErr = Double.MAX_VALUE
        for (den in 1L..maxDen.toLong()) {
            val num = (frac * den).roundToLong()
            val err = abs(frac - num.toDouble() / den.toDouble())
            if (err < bestErr) {
                bestErr = err
                bestNum = num
                bestDen = den
                if (err < 1e-9) break
            }
        }
        if (bestErr > 1e-5 || bestNum == 0L) return formatDoubleClean(value)
        val gcdVal = gcd(bestNum, bestDen)
        val n = bestNum / gcdVal
        val d = bestDen / gcdVal
        return if (whole > 0L) "$sign$whole⌟$n⌟$d" else "$sign$n⌟$d"
    }

    private fun gcd(a: Long, b: Long): Long {
        var x = a
        var y = b
        while (y != 0L) {
            val t = x % y
            x = y
            y = t
        }
        return if (x == 0L) 1L else x
    }

    data class SdStatsResult(
        val n: Int,
        val sumX: Double,
        val sumX2: Double,
        val mean: Double,
        val popStdDev: Double,
        val sampleStdDev: Double
    )

    fun computeSdStats(samples: List<Double>): SdStatsResult? {
        if (samples.isEmpty()) return null
        val n = samples.size
        val sumX = samples.sum()
        val sumX2 = samples.sumOf { it * it }
        val mean = sumX / n
        val popVar = (sumX2 / n) - (mean * mean)
        val popStd = sqrt(popVar.coerceAtLeast(0.0))
        val sampleStd = if (n > 1) {
            val s2 = samples.sumOf { (it - mean) * (it - mean) } / (n - 1)
            sqrt(s2.coerceAtLeast(0.0))
        } else 0.0
        return SdStatsResult(
            n = n,
            sumX = sumX,
            sumX2 = sumX2,
            mean = mean,
            popStdDev = popStd,
            sampleStdDev = sampleStd
        )
    }

    data class ComplexEvalResult(
        val real: Double,
        val imag: Double,
        val magnitude: Double,
        val angleInUnit: Double,
        val rectangularText: String,
        val polarText: String
    )

    fun evaluateComplex(
        a: Double,
        b: Double,
        c: Double,
        d: Double,
        op: String,
        angleUnit: AngleUnit = AngleUnit.DEG
    ): ComplexEvalResult? {
        val (re, im) = when (op) {
            "+" -> (a + c) to (b + d)
            "−", "-" -> (a - c) to (b - d)
            "×", "*" -> (a * c - b * d) to (a * d + b * c)
            "÷", "/" -> {
                val denom = c * c + d * d
                if (abs(denom) < 1e-15) return null
                ((a * c + b * d) / denom) to ((b * c - a * d) / denom)
            }
            else -> return null
        }
        val mag = hypot(re, im)
        val rad = atan2(im, re)
        val angle = when (angleUnit) {
            AngleUnit.DEG -> Math.toDegrees(rad)
            AngleUnit.RAD -> rad
            AngleUnit.GRA -> rad * 200.0 / PI
        }
        val sign = if (im >= 0) "+" else "−"
        val rect = "${formatDoubleClean(re)} $sign ${formatDoubleClean(abs(im))}i"
        val polar = "${formatDoubleClean(mag)} ∠ ${formatDoubleClean(angle)}°"
        return ComplexEvalResult(re, im, mag, angle, rect, polar)
    }

    fun solveQuadratic(a: Double, b: Double, c: Double): List<String> {
        if (abs(a) < 1e-12) return listOf("a অবশ্যই শূন্য নয় (a ≠ 0)।")
        val disc = b * b - 4 * a * c
        return if (disc >= 0) {
            val x1 = (-b + sqrt(disc)) / (2 * a)
            val x2 = (-b - sqrt(disc)) / (2 * a)
            listOf(
                "D = b² - 4ac = ${formatDoubleClean(disc)}",
                "x₁ = ${formatDoubleClean(x1)}",
                "x₂ = ${formatDoubleClean(x2)}"
            )
        } else {
            val re = -b / (2 * a)
            val im = sqrt(-disc) / (2 * a)
            listOf(
                "D = ${formatDoubleClean(disc)} (< 0, জটিল মূল)",
                "x₁ = ${formatDoubleClean(re)} + ${formatDoubleClean(im)}i",
                "x₂ = ${formatDoubleClean(re)} − ${formatDoubleClean(im)}i"
            )
        }
    }

    fun solveLinear2x2(
        a1: Double,
        b1: Double,
        c1: Double,
        a2: Double,
        b2: Double,
        c2: Double
    ): List<String> {
        val det = a1 * b2 - a2 * b1
        if (abs(det) < 1e-12) {
            return listOf("নির্ণায়ক D = 0, সমীকরণ জোটের একক সমাধান নেই।")
        }
        val x = (c1 * b2 - c2 * b1) / det
        val y = (a1 * c2 - a2 * c1) / det
        return listOf(
            "D = ${formatDoubleClean(det)}",
            "x = ${formatDoubleClean(x)}",
            "y = ${formatDoubleClean(y)}"
        )
    }

    fun evaluateDerivative(
        rawExprWithX: String,
        xVal: Double,
        angleUnit: AngleUnit = AngleUnit.DEG
    ): ScientificEvalResult {
        val h = 1e-5
        val fPlus = evaluateScientificExpression(rawExprWithX, angleUnit, variables = mapOf("X" to (xVal + h), "x" to (xVal + h)))
        if (!fPlus.success || fPlus.numericValue == null) return fPlus
        val fMinus = evaluateScientificExpression(rawExprWithX, angleUnit, variables = mapOf("X" to (xVal - h), "x" to (xVal - h)))
        if (!fMinus.success || fMinus.numericValue == null) return fMinus
        val deriv = (fPlus.numericValue - fMinus.numericValue) / (2.0 * h)
        return ScientificEvalResult(
            success = true,
            formattedResult = formatDoubleClean(deriv),
            numericValue = deriv
        )
    }

    fun evaluateIntegral(
        rawExprWithX: String,
        lower: Double,
        upper: Double,
        angleUnit: AngleUnit = AngleUnit.DEG
    ): ScientificEvalResult {
        val n = 100 // even subdivisions for Simpson's rule
        val h = (upper - lower) / n
        var sum = 0.0
        for (i in 0..n) {
            val xi = lower + i * h
            val res = evaluateScientificExpression(rawExprWithX, angleUnit, variables = mapOf("X" to xi, "x" to xi))
            if (!res.success || res.numericValue == null) return res
            val weight = when {
                i == 0 || i == n -> 1.0
                i % 2 == 1 -> 4.0
                else -> 2.0
            }
            sum += weight * res.numericValue
        }
        val integral = sum * h / 3.0
        return ScientificEvalResult(
            success = true,
            formattedResult = formatDoubleClean(integral),
            numericValue = integral
        )
    }
}

fun BaseConversionResult.inBase(base: NumberBase): String = when (base) {
    NumberBase.BIN -> binary
    NumberBase.OCT -> octal
    NumberBase.DEC -> decimal
    NumberBase.HEX -> hex
}
