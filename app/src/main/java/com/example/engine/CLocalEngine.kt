package com.example.engine

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class DryRunStep(
    val stepNumber: Int,
    val description: String,
    val variablesSnapshot: Map<String, String>
)

data class FriendlyCompilerError(
    val titleBn: String,
    val explanationBn: String,
    val fixTipBn: String,
    val lineNumber: Int? = null
)

data class CExecutionResult(
    val success: Boolean,
    val output: String,
    val executionMode: String, // "Offline Engine" or "Cloud GCC"
    val error: FriendlyCompilerError? = null,
    val warningsBn: List<String> = emptyList(),
    val dryRunSteps: List<DryRunStep> = emptyList(),
    val passedTestCase: Boolean? = null
)

object CLocalEngine {

    private sealed interface Token {
        data class Num(val value: Double, val isFloat: Boolean) : Token
        data class Str(val value: String) : Token
        data class Chr(val value: Char) : Token
        data class Id(val name: String) : Token
        data class Sym(val text: String) : Token
    }

    private enum class VarType { INT, FLOAT, CHAR }

    private data class VarSlot(
        val type: VarType,
        var value: Double = 0.0,
        var array: MutableList<Double>? = null
    )

    private data class FuncDef(
        val returnType: VarType?,
        val params: List<Pair<VarType, String>>,
        val bodyTokens: List<Token>
    )

    private class ReturnSignal(val value: Double) : RuntimeException()
    private class BreakSignal : RuntimeException()
    private class ContinueSignal : RuntimeException()

    fun execute(
        code: String,
        stdin: String = "",
        expectedOutput: String? = null
    ): CExecutionResult {
        val staticError = quickStaticDiagnose(code)
        if (staticError != null) {
            return CExecutionResult(
                success = false,
                output = "",
                executionMode = "Offline Engine",
                error = staticError
            )
        }

        val warnings = collectWarnings(code)
        return try {
            val cleaned = preprocess(code)
            val tokens = tokenize(cleaned)
            val interp = Interpreter(tokens, stdin)
            interp.run()
            val out = interp.output.toString().trimEnd('\n')
            val testPassed = expectedOutput?.takeIf { it.isNotBlank() }?.let { exp ->
                normalizeOut(out) == normalizeOut(exp)
            }
            CExecutionResult(
                success = true,
                output = out.ifBlank { "(কোনো আউটপুট নেই — printf ব্যবহার করো)" },
                executionMode = "Offline Engine",
                warningsBn = warnings,
                dryRunSteps = interp.dryRunSteps.take(40),
                passedTestCase = testPassed
            )
        } catch (e: Exception) {
            val friendly = translateRuntimeError(e.message ?: "Syntax error")
            CExecutionResult(
                success = false,
                output = "",
                executionMode = "Offline Engine",
                error = friendly,
                warningsBn = warnings
            )
        }
    }

    fun translateCompilerError(rawCompilerMsg: String): FriendlyCompilerError {
        return translateRuntimeError(rawCompilerMsg)
    }

    private fun normalizeOut(s: String): String =
        s.lines().joinToString("\n") { it.trim() }.trim()

    private fun quickStaticDiagnose(code: String): FriendlyCompilerError? {
        if (code.isBlank()) {
            return FriendlyCompilerError(
                titleBn = "কোড ফাঁকা রয়েছে",
                explanationBn = "রান করার আগে main() ফাংশনসহ একটি C প্রোগ্রাম লেখো।",
                fixTipBn = "#include <stdio.h> এবং int main() { ... return 0; } দিয়ে শুরু করো।"
            )
        }

        var braceBalance = 0
        var parenBalance = 0
        var inString = false
        var inChar = false
        var inLineComment = false
        var inBlockComment = false

        val lines = code.lines()
        for ((idx, line) in lines.withIndex()) {
            val lineNo = idx + 1
            inLineComment = false
            var i = 0
            while (i < line.length) {
                val c = line[i]
                val next = line.getOrNull(i + 1)
                if (inBlockComment) {
                    if (c == '*' && next == '/') {
                        inBlockComment = false
                        i += 2
                        continue
                    }
                    i++
                    continue
                }
                if (inLineComment) break
                if (!inString && !inChar && c == '/' && next == '/') {
                    inLineComment = true
                    break
                }
                if (!inString && !inChar && c == '/' && next == '*') {
                    inBlockComment = true
                    i += 2
                    continue
                }
                if (!inChar && c == '"' && line.getOrNull(i - 1) != '\\') {
                    inString = !inString
                } else if (!inString && c == '\'' && line.getOrNull(i - 1) != '\\') {
                    inChar = !inChar
                } else if (!inString && !inChar) {
                    when (c) {
                        '{' -> braceBalance++
                        '}' -> {
                            braceBalance--
                            if (braceBalance < 0) {
                                return FriendlyCompilerError(
                                    titleBn = "অতিরিক্ত '}' বন্ধনী পাওয়া গেছে",
                                    explanationBn = "লাইন $lineNo-এ খোলার চেয়ে বেশি '}' ব্যবহার করা হয়েছে।",
                                    fixTipBn = "প্রতিটি '{' ও '}' জোড়া মিলিয়ে দেখো।",
                                    lineNumber = lineNo
                                )
                            }
                        }
                        '(' -> parenBalance++
                        ')' -> {
                            parenBalance--
                            if (parenBalance < 0) {
                                return FriendlyCompilerError(
                                    titleBn = "অতিরিক্ত ')' বন্ধনী পাওয়া গেছে",
                                    explanationBn = "লাইন $lineNo-এ অতিরিক্ত ')' রয়েছে।",
                                    fixTipBn = "ফাংশন বা শর্তের প্রথম বন্ধনী মিলিয়ে ঠিক করো।",
                                    lineNumber = lineNo
                                )
                            }
                        }
                    }
                }
                i++
            }
            if (inString) {
                return FriendlyCompilerError(
                    titleBn = "স্ট্রিং কোটেশন (\") বন্ধ করা হয়নি",
                    explanationBn = "লাইন $lineNo-এ ডাবল কোটেশন শুরু হয়েছে কিন্তু শেষ হয়নি।",
                    fixTipBn = "printf বা স্ট্রিংয়ের শেষে \" দিতে ভুলবে না।",
                    lineNumber = lineNo
                )
            }
        }

        if (braceBalance != 0) {
            return FriendlyCompilerError(
                titleBn = "দ্বিতীয় বন্ধনী '{ }' সম্পূর্ণ বন্ধ হয়নি",
                explanationBn = "কোডে $braceBalance টি '{' খোলা আছে যেগুলোর '}' দেওয়া হয়নি।",
                fixTipBn = "সব ফাংশন, লুপ বা if ব্লকের শেষে '}' যোগ করো।"
            )
        }
        if (parenBalance != 0) {
            return FriendlyCompilerError(
                titleBn = "প্রথম বন্ধনী '( )' সম্পূর্ণ বন্ধ হয়নি",
                explanationBn = "একটি '(' বন্ধ করা বাকি আছে।",
                fixTipBn = "printf(...), scanf(...), if(...) বা for(...) এর বন্ধনী চেক করো।"
            )
        }
        if (!Regex("\\bmain\\s*\\(").containsMatchIn(code)) {
            return FriendlyCompilerError(
                titleBn = "main() ফাংশন পাওয়া যায়নি",
                explanationBn = "প্রতিটি C প্রোগ্রাম অবশ্যই int main() থেকে শুরু হয়।",
                fixTipBn = "int main() { ... return 0; } যুক্ত করো।"
            )
        }

        // Check obvious missing semicolon on printf/scanf/return lines
        for ((idx, rawLine) in lines.withIndex()) {
            val trimmed = rawLine.substringBefore("//").trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("/*")) continue
            val isStatement = trimmed.startsWith("printf(") || trimmed.startsWith("scanf(") ||
                trimmed.startsWith("return ")
            if (isStatement && !trimmed.endsWith(";") && !trimmed.endsWith("{")) {
                return FriendlyCompilerError(
                    titleBn = "সেমিকোলন (;) অনুপস্থিত",
                    explanationBn = "লাইন ${idx + 1}-এ স্টেটমেন্টের শেষে সেমিকোলন (;) দেওয়া হয়নি।",
                    fixTipBn = "C ভাষায় printf, scanf, return এবং ভেরিয়েবল স্টেটমেন্টের শেষে অবশ্যই ';' দিতে হয়।",
                    lineNumber = idx + 1
                )
            }
        }
        return null
    }

    private fun collectWarnings(code: String): List<String> {
        val warnings = mutableListOf<String>()
        if (Regex("scanf\\s*\\([^;]*,\\s*[a-zA-Z_][a-zA-Z0-9_]*\\s*\\)").containsMatchIn(code)) {
            warnings += "সতর্কতা: scanf()-এ ভেরিয়েবলের আগে `&` না দিলে রানটাইম ত্রুটি হতে পারে (যেমন: `&n`)।"
        }
        if (Regex("\\bif\\s*\\([^=!<>]*=[^=][^)]*\\)").containsMatchIn(code)) {
            warnings += "সতর্কতা: `if(...)` এর ভেতরে `=` ব্যবহার করেছো! তুলনা করতে `==` ব্যবহার করতে হয়।"
        }
        if (Regex("\\bfor\\s*\\([^)]*\\)\\s*;").containsMatchIn(code)) {
            warnings += "সতর্কতা: `for(...)` এর ঠিক পরে `;` দিয়েছো, ফলে লুপের বডি খালি চলছে!"
        }
        return warnings
    }

    private fun translateRuntimeError(raw: String): FriendlyCompilerError {
        val low = raw.lowercase()
        return when {
            "expected ';'" in low || "semicolon" in low -> FriendlyCompilerError(
                titleBn = "সেমিকোলন (;) পাওয়া যায়নি",
                explanationBn = "কোনো একটি স্টেটমেন্টের শেষে ';' দিতে ভুলে গেছো। ($raw)",
                fixTipBn = "প্রতিটি লাইনের শেষে ';' আছে কি না যাচাই করো।"
            )
            "division by zero" in low || "divide by zero" in low -> FriendlyCompilerError(
                titleBn = "শূন্য (0) দিয়ে ভাগ করা সম্ভব নয়",
                explanationBn = "প্রোগ্রামে কোনো সংখ্যাকে 0 দিয়ে ভাগ বা % করার চেষ্টা হয়েছে।",
                fixTipBn = "ভাগের আগে নিশ্চিত করো যে ভাজক (divisor) 0 নয়।"
            )
            "infinite loop" in low || "step limit" in low -> FriendlyCompilerError(
                titleBn = "অসীম লুপ (Infinite Loop) শনাক্ত হয়েছে",
                explanationBn = "লুপটি থামার শর্ত পূরণ না করে ৫,০০০ ধাপের বেশি চলেছে।",
                fixTipBn = "while বা for লুপে কাউন্টার বাড়ছে কি না (যেমন: i++) এবং শর্ত ঠিক আছে কি না দেখো।"
            )
            "undeclared" in low || "undefined variable" in low -> FriendlyCompilerError(
                titleBn = "অঘোষিত ভেরিয়েবল (Undeclared Variable)",
                explanationBn = "ব্যবহারের আগে ভেরিয়েবলটি declare করা হয়নি। ($raw)",
                fixTipBn = "ব্যবহারের আগে int, float বা char দিয়ে ভেরিয়েবল ঘোষণা করো।"
            )
            "scanf" in low || "input" in low -> FriendlyCompilerError(
                titleBn = "ইনপুট (scanf) ত্রুটি",
                explanationBn = "প্রোগ্রাম ইনপুট চেয়েছে কিন্তু Input বক্সে পর্যাপ্ত সংখ্যা দেওয়া হয়নি।",
                fixTipBn = "নিচের Stdin Input বক্সে স্পেস দিয়ে প্রয়োজনীয় সংখ্যা লেখো (যেমন: `10 5`)।"
            )
            else -> FriendlyCompilerError(
                titleBn = "সিনট্যাক্স বা কম্পাইল ত্রুটি",
                explanationBn = "কোডে ব্যাকরণগত ত্রুটি রয়েছে: $raw",
                fixTipBn = "ব্র্যাকেট { }, সেমিকোলন ; এবং ভেরিয়েবলের বানান আরেকবার মিলিয়ে নাও।"
            )
        }
    }

    private fun preprocess(code: String): String {
        val noBlock = Regex("/\\*[\\s\\S]*?\\*/").replace(code, " ")
        val out = StringBuilder()
        val defines = mutableMapOf<String, String>()
        noBlock.lines().forEach { line ->
            val noLine = line.substringBefore("//")
            val trimmed = noLine.trim()
            if (trimmed.startsWith("#define ")) {
                val parts = trimmed.removePrefix("#define ").trim().split(Regex("\\s+"), limit = 2)
                if (parts.size == 2) defines[parts[0]] = parts[1]
            } else if (!trimmed.startsWith("#")) {
                var replaced = noLine
                defines.forEach { (k, v) ->
                    replaced = Regex("\\b${Regex.escape(k)}\\b").replace(replaced, v)
                }
                out.append(replaced).append('\n')
            }
        }
        return out.toString()
    }

    private fun tokenize(src: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        val twoChar = setOf("++", "--", "+=", "-=", "*=", "/=", "%=", "==", "!=", "<=", ">=", "&&", "||")
        while (i < src.length) {
            val c = src[i]
            if (c.isWhitespace()) {
                i++
                continue
            }
            if (c == '"') {
                val sb = StringBuilder()
                i++
                while (i < src.length && src[i] != '"') {
                    if (src[i] == '\\' && i + 1 < src.length) {
                        i++
                        sb.append(
                            when (src[i]) {
                                'n' -> '\n'
                                't' -> '\t'
                                '\\' -> '\\'
                                '"' -> '"'
                                else -> src[i]
                            }
                        )
                    } else {
                        sb.append(src[i])
                    }
                    i++
                }
                if (i >= src.length) error("Unclosed string literal")
                i++
                tokens += Token.Str(sb.toString())
                continue
            }
            if (c == '\'') {
                i++
                val ch = if (i < src.length && src[i] == '\\') {
                    i++
                    when (src.getOrNull(i)) {
                        'n' -> '\n'
                        't' -> '\t'
                        '0' -> '\u0000'
                        else -> src.getOrNull(i) ?: '\u0000'
                    }
                } else {
                    src.getOrNull(i) ?: '\u0000'
                }
                i++
                if (src.getOrNull(i) == '\'') i++
                tokens += Token.Chr(ch)
                continue
            }
            if (c.isDigit() || (c == '.' && src.getOrNull(i + 1)?.isDigit() == true)) {
                val start = i
                var hasDot = false
                while (i < src.length && (src[i].isDigit() || (src[i] == '.' && !hasDot))) {
                    if (src[i] == '.') hasDot = true
                    i++
                }
                val numStr = src.substring(start, i)
                tokens += Token.Num(numStr.toDouble(), hasDot)
                continue
            }
            if (c.isLetter() || c == '_') {
                val start = i
                while (i < src.length && (src[i].isLetterOrDigit() || src[i] == '_')) i++
                tokens += Token.Id(src.substring(start, i))
                continue
            }
            if (i + 1 < src.length && src.substring(i, i + 2) in twoChar) {
                tokens += Token.Sym(src.substring(i, i + 2))
                i += 2
                continue
            }
            tokens += Token.Sym(c.toString())
            i++
        }
        return tokens
    }

    private class Interpreter(
        private val allTokens: List<Token>,
        stdin: String
    ) {
        val output = StringBuilder()
        val dryRunSteps = mutableListOf<DryRunStep>()
        private val inputTokens = Regex("\\S+").findAll(stdin).map { it.value }.toList()
        private var inputIndex = 0
        private val functions = mutableMapOf<String, FuncDef>()
        private val scopes = mutableListOf<MutableMap<String, VarSlot>>(mutableMapOf())
        private var stepCounter = 0
        private val maxSteps = 5000

        fun run() {
            parseTopLevel()
            val mainFn = functions["main"] ?: error("main() function not found")
            try {
                executeBlockTokens(mainFn.bodyTokens)
            } catch (_: ReturnSignal) {
                // Normal return from main()
            }
        }

        private fun parseTopLevel() {
            var idx = 0
            while (idx < allTokens.size) {
                val retType = parseTypeOrVoid(allTokens[idx])
                if (retType == null && !(allTokens[idx] is Token.Id && (allTokens[idx] as Token.Id).name == "void")) {
                    error("Expected declaration near ${(allTokens[idx] as? Token.Id)?.name ?: allTokens[idx]}")
                }
                idx++
                val name = (allTokens.getOrNull(idx) as? Token.Id)?.name ?: error("Expected identifier")
                idx++
                if (allTokens.getOrNull(idx) == Token.Sym("(")) {
                    idx++
                    val params = mutableListOf<Pair<VarType, String>>()
                    while (idx < allTokens.size && allTokens[idx] != Token.Sym(")")) {
                        if (allTokens[idx] == Token.Sym(",")) {
                            idx++
                            continue
                        }
                        if ((allTokens[idx] as? Token.Id)?.name == "void") {
                            idx++
                            continue
                        }
                        val pType = parseTypeOrVoid(allTokens[idx]) ?: error("Expected parameter type")
                        idx++
                        val pName = (allTokens.getOrNull(idx) as? Token.Id)?.name ?: error("Expected parameter name")
                        idx++
                        params += pType to pName
                    }
                    if (allTokens.getOrNull(idx) != Token.Sym(")")) error("Expected ')' in function $name")
                    idx++
                    if (allTokens.getOrNull(idx) == Token.Sym(";")) {
                        idx++ // forward prototype
                        continue
                    }
                    val (body, nextIdx) = extractBracedBlock(allTokens, idx)
                    functions[name] = FuncDef(retType, params, body)
                    idx = nextIdx
                } else {
                    // Global variable declaration skipped or initialized
                    while (idx < allTokens.size && allTokens[idx] != Token.Sym(";")) idx++
                    idx++
                }
            }
        }

        private fun parseTypeOrVoid(t: Token): VarType? = when ((t as? Token.Id)?.name) {
            "int", "long", "short" -> VarType.INT
            "float", "double" -> VarType.FLOAT
            "char" -> VarType.CHAR
            else -> null
        }

        private fun extractBracedBlock(tokens: List<Token>, startIdx: Int): Pair<List<Token>, Int> {
            if (tokens.getOrNull(startIdx) != Token.Sym("{")) error("Expected '{'")
            var depth = 0
            var i = startIdx
            val inner = mutableListOf<Token>()
            while (i < tokens.size) {
                val tk = tokens[i]
                if (tk == Token.Sym("{")) {
                    if (depth > 0) inner += tk
                    depth++
                } else if (tk == Token.Sym("}")) {
                    depth--
                    if (depth == 0) return inner to (i + 1)
                    inner += tk
                } else {
                    inner += tk
                }
                i++
            }
            error("Unclosed '{'")
        }

        private fun executeBlockTokens(tokens: List<Token>) {
            scopes.add(mutableMapOf())
            try {
                var i = 0
                while (i < tokens.size) {
                    i = executeStatement(tokens, i)
                }
            } finally {
                scopes.removeAt(scopes.lastIndex)
            }
        }

        private fun executeStatement(tokens: List<Token>, start: Int): Int {
            tickStep("Step")
            var i = start
            val tk = tokens.getOrNull(i) ?: return i
            if (tk == Token.Sym(";")) return i + 1
            if (tk == Token.Sym("{")) {
                val (block, next) = extractBracedBlock(tokens, i)
                executeBlockTokens(block)
                return next
            }
            if (tk is Token.Id) {
                val type = parseTypeOrVoid(tk)
                if (type != null) {
                    return executeVarDecl(tokens, i + 1, type)
                }
                when (tk.name) {
                    "return" -> {
                        i++
                        val semi = findStatementEnd(tokens, i)
                        val valRes = if (semi > i) evalExpr(tokens.subList(i, semi)) else 0.0
                        throw ReturnSignal(valRes)
                    }
                    "break" -> {
                        if (tokens.getOrNull(i + 1) != Token.Sym(";")) error("Expected ';' after break")
                        throw BreakSignal()
                    }
                    "continue" -> {
                        if (tokens.getOrNull(i + 1) != Token.Sym(";")) error("Expected ';' after continue")
                        throw ContinueSignal()
                    }
                    "if" -> return executeIf(tokens, i + 1)
                    "while" -> return executeWhile(tokens, i + 1)
                    "do" -> return executeDoWhile(tokens, i + 1)
                    "for" -> return executeFor(tokens, i + 1)
                    "switch" -> return executeSwitch(tokens, i + 1)
                }
            }

            val end = findStatementEnd(tokens, i)
            if (end > i) {
                evalExpr(tokens.subList(i, end))
            }
            return end + 1
        }

        private fun executeVarDecl(tokens: List<Token>, start: Int, type: VarType): Int {
            var i = start
            while (i < tokens.size) {
                val name = (tokens.getOrNull(i) as? Token.Id)?.name ?: error("Expected variable name")
                i++
                if (tokens.getOrNull(i) == Token.Sym("[")) {
                    i++
                    val sizeTokens = mutableListOf<Token>()
                    while (i < tokens.size && tokens[i] != Token.Sym("]")) {
                        sizeTokens += tokens[i]
                        i++
                    }
                    if (tokens.getOrNull(i) != Token.Sym("]")) error("Expected ']'")
                    i++
                    val declaredSize = if (sizeTokens.isNotEmpty()) evalExpr(sizeTokens).toInt().coerceAtLeast(1) else 16
                    val arr = MutableList(declaredSize) { 0.0 }
                    if (tokens.getOrNull(i) == Token.Sym("=")) {
                        i++
                        if (tokens.getOrNull(i) == Token.Sym("{")) {
                            val (initTokens, nextIdx) = extractBracedBlock(tokens, i)
                            i = nextIdx
                            val elems = splitTopLevelCommas(initTokens)
                            elems.forEachIndexed { idx, elemToks ->
                                val v = evalExpr(elemToks)
                                if (idx < arr.size) arr[idx] = v else arr.add(v)
                            }
                        }
                    }
                    scopes.last()[name] = VarSlot(type, 0.0, arr)
                } else {
                    var initVal = 0.0
                    if (tokens.getOrNull(i) == Token.Sym("=")) {
                        i++
                        val exprTokens = mutableListOf<Token>()
                        var paren = 0
                        while (i < tokens.size) {
                            val cur = tokens[i]
                            if (cur == Token.Sym("(")) paren++
                            else if (cur == Token.Sym(")")) paren--
                            else if (paren == 0 && (cur == Token.Sym(",") || cur == Token.Sym(";"))) break
                            exprTokens += cur
                            i++
                        }
                        initVal = evalExpr(exprTokens)
                        if (type == VarType.INT) initVal = initVal.toInt().toDouble()
                    }
                    scopes.last()[name] = VarSlot(type, initVal)
                }
                if (tokens.getOrNull(i) == Token.Sym(",")) {
                    i++
                    continue
                }
                if (tokens.getOrNull(i) == Token.Sym(";")) {
                    return i + 1
                }
                error("Expected ';' after variable declaration $name")
            }
            error("Expected ';'")
        }

        private fun executeIf(tokens: List<Token>, start: Int): Int {
            val (condTokens, afterParen) = extractParenExpr(tokens, start)
            val cond = evalExpr(condTokens) != 0.0
            val (thenEnd, thenExecuted) = if (cond) {
                executeStatement(tokens, afterParen) to true
            } else {
                skipStatement(tokens, afterParen) to false
            }
            var next = thenEnd
            if ((tokens.getOrNull(next) as? Token.Id)?.name == "else") {
                next = if (!thenExecuted) {
                    executeStatement(tokens, next + 1)
                } else {
                    skipStatement(tokens, next + 1)
                }
            }
            return next
        }

        private fun executeWhile(tokens: List<Token>, start: Int): Int {
            val (condTokens, bodyStart) = extractParenExpr(tokens, start)
            var bodyEnd = skipStatement(tokens, bodyStart)
            var iter = 0
            while (evalExpr(condTokens) != 0.0) {
                iter++
                tickStep("while iteration #$iter")
                try {
                    bodyEnd = executeStatement(tokens, bodyStart)
                } catch (_: ContinueSignal) {
                    continue
                } catch (_: BreakSignal) {
                    break
                }
            }
            return bodyEnd
        }

        private fun executeDoWhile(tokens: List<Token>, start: Int): Int {
            var iter = 0
            var afterBody: Int
            do {
                iter++
                tickStep("do-while iteration #$iter")
                try {
                    afterBody = executeStatement(tokens, start)
                } catch (_: ContinueSignal) {
                    afterBody = skipStatement(tokens, start)
                } catch (_: BreakSignal) {
                    afterBody = skipStatement(tokens, start)
                    val whileIdx = afterBody
                    val (_, afterCond) = extractParenExpr(tokens, whileIdx + 1)
                    return if (tokens.getOrNull(afterCond) == Token.Sym(";")) afterCond + 1 else afterCond
                }
                val whileKw = tokens.getOrNull(afterBody) as? Token.Id
                if (whileKw?.name != "while") error("Expected 'while' after do block")
                val (condTokens, afterCond) = extractParenExpr(tokens, afterBody + 1)
                val next = if (tokens.getOrNull(afterCond) == Token.Sym(";")) afterCond + 1 else afterCond
                if (evalExpr(condTokens) == 0.0) return next
            } while (true)
        }

        private fun executeFor(tokens: List<Token>, start: Int): Int {
            val (header, bodyStart) = extractParenExpr(tokens, start)
            val parts = splitTopLevelSemicolons(header)
            if (parts.size != 3) error("Invalid for(...) header")
            scopes.add(mutableMapOf())
            var bodyEnd = skipStatement(tokens, bodyStart)
            try {
                if (parts[0].isNotEmpty()) {
                    val first = parts[0].first()
                    val declType = parseTypeOrVoid(first)
                    if (declType != null) {
                        executeVarDecl(parts[0] + Token.Sym(";"), 1, declType)
                    } else {
                        evalExpr(parts[0])
                    }
                }
                var iter = 0
                while (parts[1].isEmpty() || evalExpr(parts[1]) != 0.0) {
                    iter++
                    tickStep("for iteration #$iter")
                    try {
                        bodyEnd = executeStatement(tokens, bodyStart)
                    } catch (_: ContinueSignal) {
                        // run update
                    } catch (_: BreakSignal) {
                        break
                    }
                    if (parts[2].isNotEmpty()) evalExpr(parts[2])
                }
            } finally {
                scopes.removeAt(scopes.lastIndex)
            }
            return bodyEnd
        }

        private fun executeSwitch(tokens: List<Token>, start: Int): Int {
            val (exprToks, bodyStart) = extractParenExpr(tokens, start)
            val switchVal = evalExpr(exprToks).toInt()
            val (bodyTokens, afterSwitch) = extractBracedBlock(tokens, bodyStart)

            var matched = false
            var defaultIdx = -1
            var i = 0
            // First scan for matching case
            while (i < bodyTokens.size) {
                val t = bodyTokens[i]
                if (t is Token.Id && t.name == "case") {
                    val caseNum = evalExpr(listOf(bodyTokens[i + 1])).toInt()
                    val colon = bodyTokens.indexOfFirstFrom(i, Token.Sym(":"))
                    if (caseNum == switchVal) {
                        matched = true
                        i = colon + 1
                        break
                    }
                    i = colon + 1
                } else if (t is Token.Id && t.name == "default") {
                    val colon = bodyTokens.indexOfFirstFrom(i, Token.Sym(":"))
                    defaultIdx = colon + 1
                    i = colon + 1
                } else {
                    i++
                }
            }
            val startExec = if (matched) i else if (defaultIdx >= 0) defaultIdx else return afterSwitch
            var idx = startExec
            try {
                while (idx < bodyTokens.size) {
                    val t = bodyTokens[idx]
                    if (t is Token.Id && (t.name == "case" || t.name == "default")) {
                        val colon = bodyTokens.indexOfFirstFrom(idx, Token.Sym(":"))
                        idx = colon + 1
                        continue
                    }
                    idx = executeStatement(bodyTokens, idx)
                }
            } catch (_: BreakSignal) {
                // exit switch cleanly
            }
            return afterSwitch
        }

        private fun List<Token>.indexOfFirstFrom(from: Int, target: Token): Int {
            for (k in from until size) if (this[k] == target) return k
            error("Expected ':' in switch case")
        }

        private fun skipStatement(tokens: List<Token>, start: Int): Int {
            val tk = tokens.getOrNull(start) ?: return start
            if (tk == Token.Sym(";")) return start + 1
            if (tk == Token.Sym("{")) return extractBracedBlock(tokens, start).second
            if (tk is Token.Id && tk.name == "if") {
                val (_, afterParen) = extractParenExpr(tokens, start + 1)
                var next = skipStatement(tokens, afterParen)
                if ((tokens.getOrNull(next) as? Token.Id)?.name == "else") {
                    next = skipStatement(tokens, next + 1)
                }
                return next
            }
            if (tk is Token.Id && (tk.name == "while" || tk.name == "for" || tk.name == "switch")) {
                val (_, afterParen) = extractParenExpr(tokens, start + 1)
                return skipStatement(tokens, afterParen)
            }
            if (tk is Token.Id && tk.name == "do") {
                val afterBody = skipStatement(tokens, start + 1)
                val (_, afterCond) = extractParenExpr(tokens, afterBody + 1)
                return if (tokens.getOrNull(afterCond) == Token.Sym(";")) afterCond + 1 else afterCond
            }
            return findStatementEnd(tokens, start) + 1
        }

        private fun findStatementEnd(tokens: List<Token>, start: Int): Int {
            var i = start
            while (i < tokens.size) {
                if (tokens[i] == Token.Sym(";")) return i
                i++
            }
            error("Expected ';'")
        }

        private fun extractParenExpr(tokens: List<Token>, start: Int): Pair<List<Token>, Int> {
            if (tokens.getOrNull(start) != Token.Sym("(")) error("Expected '('")
            var depth = 0
            var i = start
            val inner = mutableListOf<Token>()
            while (i < tokens.size) {
                val tk = tokens[i]
                if (tk == Token.Sym("(")) {
                    if (depth > 0) inner += tk
                    depth++
                } else if (tk == Token.Sym(")")) {
                    depth--
                    if (depth == 0) return inner to (i + 1)
                    inner += tk
                } else {
                    inner += tk
                }
                i++
            }
            error("Unclosed '('")
        }

        private fun splitTopLevelCommas(tokens: List<Token>): List<List<Token>> {
            val res = mutableListOf<List<Token>>()
            var cur = mutableListOf<Token>()
            var depth = 0
            for (tk in tokens) {
                when (tk) {
                    Token.Sym("("), Token.Sym("[") -> {
                        depth++
                        cur += tk
                    }
                    Token.Sym(")"), Token.Sym("]") -> {
                        depth--
                        cur += tk
                    }
                    Token.Sym(",") if depth == 0 -> {
                        res += cur
                        cur = mutableListOf()
                    }
                    else -> cur += tk
                }
            }
            if (cur.isNotEmpty()) res += cur
            return res
        }

        private fun splitTopLevelSemicolons(tokens: List<Token>): List<List<Token>> {
            val res = mutableListOf<List<Token>>()
            var cur = mutableListOf<Token>()
            var depth = 0
            for (tk in tokens) {
                when (tk) {
                    Token.Sym("(") -> { depth++; cur += tk }
                    Token.Sym(")") -> { depth--; cur += tk }
                    Token.Sym(";") if depth == 0 -> {
                        res += cur
                        cur = mutableListOf()
                    }
                    else -> cur += tk
                }
            }
            res += cur
            return res
        }

        private fun findVar(name: String): VarSlot {
            for (s in scopes.asReversed()) {
                s[name]?.let { return it }
            }
            error("Undeclared variable '$name'")
        }

        private fun tickStep(desc: String) {
            stepCounter++
            if (stepCounter > maxSteps) {
                error("Step limit exceeded (possible infinite loop)")
            }
            if (dryRunSteps.size < 40 && (desc != "Step" || stepCounter <= 15)) {
                val snap = linkedMapOf<String, String>()
                scopes.forEach { scope ->
                    scope.forEach { (k, slot) ->
                        if (slot.array == null) {
                            snap[k] = if (slot.type == VarType.INT) slot.value.toInt().toString()
                            else String.format("%.2f", slot.value)
                        }
                    }
                }
                if (snap.isNotEmpty()) {
                    dryRunSteps += DryRunStep(dryRunSteps.size + 1, desc, snap)
                }
            }
        }

        private fun evalExpr(tokens: List<Token>): Double {
            if (tokens.isEmpty()) return 0.0
            return ExprParser(tokens).parseComma()
        }

        private inner class ExprParser(private val toks: List<Token>) {
            private var p = 0

            private fun peek(): Token? = toks.getOrNull(p)
            private fun matchSym(s: String): Boolean {
                if (peek() == Token.Sym(s)) {
                    p++
                    return true
                }
                return false
            }

            fun parseComma(): Double {
                var v = parseAssign()
                while (matchSym(",")) v = parseAssign()
                return v
            }

            private fun parseAssign(): Double {
                val saved = p
                val lhs = peek()
                if (lhs is Token.Id) {
                    val next = toks.getOrNull(p + 1)
                    if (next == Token.Sym("[")) {
                        p += 2
                        val idx = parseTernary().toInt()
                        if (!matchSym("]")) error("Expected ']'")
                        val op = (peek() as? Token.Sym)?.text
                        if (op in setOf("=", "+=", "-=", "*=", "/=", "%=")) {
                            p++
                            val rhs = parseAssign()
                            val slot = findVar(lhs.name)
                            val arr = slot.array ?: error("${lhs.name} is not an array")
                            if (idx !in arr.indices) error("Array index $idx out of bounds")
                            val res = applyCompound(arr[idx], rhs, op!!, slot.type)
                            arr[idx] = res
                            return res
                        }
                        p = saved
                    } else if (next is Token.Sym && next.text in setOf("=", "+=", "-=", "*=", "/=", "%=")) {
                        p += 2
                        val rhs = parseAssign()
                        val slot = findVar(lhs.name)
                        val res = applyCompound(slot.value, rhs, next.text, slot.type)
                        slot.value = res
                        return res
                    }
                }
                return parseTernary()
            }

            private fun applyCompound(cur: Double, rhs: Double, op: String, type: VarType): Double {
                val raw = when (op) {
                    "=" -> rhs
                    "+=" -> cur + rhs
                    "-=" -> cur - rhs
                    "*=" -> cur * rhs
                    "/=" -> {
                        if (rhs == 0.0) error("Division by zero")
                        cur / rhs
                    }
                    "%=" -> {
                        if (rhs.toInt() == 0) error("Division by zero")
                        (cur.toInt() % rhs.toInt()).toDouble()
                    }
                    else -> rhs
                }
                return if (type == VarType.INT) raw.toInt().toDouble() else raw
            }

            private fun parseTernary(): Double {
                val cond = parseOr()
                if (matchSym("?")) {
                    val tVal = parseTernary()
                    if (!matchSym(":")) error("Expected ':' in ternary")
                    val fVal = parseTernary()
                    return if (cond != 0.0) tVal else fVal
                }
                return cond
            }

            private fun parseOr(): Double {
                var v = parseAnd()
                while (matchSym("||")) {
                    val r = parseAnd()
                    v = if (v != 0.0 || r != 0.0) 1.0 else 0.0
                }
                return v
            }

            private fun parseAnd(): Double {
                var v = parseEq()
                while (matchSym("&&")) {
                    val r = parseEq()
                    v = if (v != 0.0 && r != 0.0) 1.0 else 0.0
                }
                return v
            }

            private fun parseEq(): Double {
                var v = parseRel()
                while (true) {
                    v = when {
                        matchSym("==") -> if (v == parseRel()) 1.0 else 0.0
                        matchSym("!=") -> if (v != parseRel()) 1.0 else 0.0
                        else -> return v
                    }
                }
            }

            private fun parseRel(): Double {
                var v = parseAdd()
                while (true) {
                    v = when {
                        matchSym("<") -> if (v < parseAdd()) 1.0 else 0.0
                        matchSym(">") -> if (v > parseAdd()) 1.0 else 0.0
                        matchSym("<=") -> if (v <= parseAdd()) 1.0 else 0.0
                        matchSym(">=") -> if (v >= parseAdd()) 1.0 else 0.0
                        else -> return v
                    }
                }
            }

            private fun parseAdd(): Double {
                var v = parseMul()
                while (true) {
                    v = when {
                        matchSym("+") -> v + parseMul()
                        matchSym("-") -> v - parseMul()
                        else -> return v
                    }
                }
            }

            private fun parseMul(): Double {
                var v = parseUnary()
                while (true) {
                    v = when {
                        matchSym("*") -> v * parseUnary()
                        matchSym("/") -> {
                            val r = parseUnary()
                            if (r == 0.0) error("Division by zero")
                            if (isIntValue(v) && isIntValue(r)) (v.toInt() / r.toInt()).toDouble() else v / r
                        }
                        matchSym("%") -> {
                            val r = parseUnary().toInt()
                            if (r == 0) error("Division by zero")
                            (v.toInt() % r).toDouble()
                        }
                        else -> return v
                    }
                }
            }

            private fun isIntValue(d: Double): Boolean = d == d.toInt().toDouble() && d !in listOf(Double.NaN)

            private fun parseUnary(): Double {
                if (matchSym("!")) return if (parseUnary() == 0.0) 1.0 else 0.0
                if (matchSym("-")) return -parseUnary()
                if (matchSym("+")) return parseUnary()
                if (matchSym("++")) {
                    val id = (peek() as? Token.Id)?.name ?: error("Expected variable after ++")
                    p++
                    val slot = findVar(id)
                    slot.value += 1.0
                    return slot.value
                }
                if (matchSym("--")) {
                    val id = (peek() as? Token.Id)?.name ?: error("Expected variable after --")
                    p++
                    val slot = findVar(id)
                    slot.value -= 1.0
                    return slot.value
                }
                return parsePrimary()
            }

            private fun parsePrimary(): Double {
                val tk = peek() ?: error("Unexpected end of expression")
                return when (tk) {
                    is Token.Num -> {
                        p++
                        tk.value
                    }
                    is Token.Chr -> {
                        p++
                        tk.value.code.toDouble()
                    }
                    is Token.Sym if tk.text == "(" -> {
                        p++
                        // Handle simple cast like (float)x
                        val castType = (peek() as? Token.Id)?.let { parseTypeOrVoid(it) }
                        if (castType != null && toks.getOrNull(p + 1) == Token.Sym(")")) {
                            p += 2
                            val valCast = parseUnary()
                            return if (castType == VarType.INT) valCast.toInt().toDouble() else valCast
                        }
                        val v = parseComma()
                        if (!matchSym(")")) error("Expected ')'")
                        v
                    }
                    is Token.Id -> {
                        p++
                        if (matchSym("(")) {
                            val argsTokens = mutableListOf<List<Token>>()
                            if (!matchSym(")")) {
                                val rawArgs = mutableListOf<Token>()
                                var depth = 0
                                while (p < toks.size) {
                                    val c = toks[p]
                                    if (c == Token.Sym("(")) depth++
                                    else if (c == Token.Sym(")")) {
                                        if (depth == 0) { p++; break }
                                        depth--
                                    }
                                    rawArgs += c
                                    p++
                                }
                                argsTokens += splitTopLevelCommas(rawArgs)
                            }
                            return invokeFunction(tk.name, argsTokens)
                        }
                        if (matchSym("[")) {
                            val idx = parseComma().toInt()
                            if (!matchSym("]")) error("Expected ']'")
                            val arr = findVar(tk.name).array ?: error("${tk.name} is not an array")
                            if (idx !in arr.indices) error("Array index $idx out of bounds")
                            var res = arr[idx]
                            if (matchSym("++")) arr[idx] = res + 1.0
                            else if (matchSym("--")) arr[idx] = res - 1.0
                            return res
                        }
                        val slot = findVar(tk.name)
                        val cur = slot.value
                        if (matchSym("++")) slot.value = cur + 1.0
                        else if (matchSym("--")) slot.value = cur - 1.0
                        cur
                    }
                    else -> error("Unexpected token $tk")
                }
            }
        }

        private fun invokeFunction(name: String, argTokens: List<List<Token>>): Double {
            return when (name) {
                "printf" -> {
                    if (argTokens.isEmpty()) return 0.0
                    val fmt = (argTokens[0].firstOrNull() as? Token.Str)?.value
                        ?: error("printf first argument must be format string")
                    val args = argTokens.drop(1).map { toks ->
                        if (toks.size == 1 && toks[0] is Token.Str) (toks[0] as Token.Str).value
                        else evalExpr(toks)
                    }
                    output.append(formatPrintf(fmt, args))
                    0.0
                }
                "scanf" -> {
                    if (argTokens.size < 2) error("scanf requires format string and &variable")
                    for (arg in argTokens.drop(1)) {
                        val cleaned = if (arg.firstOrNull() == Token.Sym("&")) arg.drop(1) else arg
                        val rawInput = inputTokens.getOrNull(inputIndex++)
                            ?: error("scanf expected input in Stdin Input box")
                        val num = rawInput.toDoubleOrNull() ?: rawInput.firstOrNull()?.code?.toDouble()
                            ?: error("Invalid scanf input: $rawInput")
                        val id = (cleaned.firstOrNull() as? Token.Id)?.name ?: error("Invalid scanf target")
                        val slot = findVar(id)
                        if (cleaned.size > 2 && cleaned[1] == Token.Sym("[")) {
                            val idx = evalExpr(cleaned.subList(2, cleaned.size - 1)).toInt()
                            val arr = slot.array ?: error("$id is not an array")
                            arr[idx] = if (slot.type == VarType.INT) num.toInt().toDouble() else num
                        } else {
                            slot.value = if (slot.type == VarType.INT) num.toInt().toDouble() else num
                        }
                    }
                    argTokens.size - 1.0
                }
                "sqrt" -> sqrt(evalExpr(argTokens.firstOrNull() ?: emptyList()))
                "pow" -> {
                    val a = evalExpr(argTokens.getOrElse(0) { emptyList() })
                    val b = evalExpr(argTokens.getOrElse(1) { emptyList() })
                    a.pow(b)
                }
                "abs", "fabs" -> abs(evalExpr(argTokens.firstOrNull() ?: emptyList()))
                else -> {
                    val fn = functions[name] ?: error("Undefined function '$name'")
                    val evalArgs = argTokens.map { evalExpr(it) }
                    scopes.add(mutableMapOf())
                    try {
                        fn.params.forEachIndexed { idx, (pType, pName) ->
                            val v = evalArgs.getOrElse(idx) { 0.0 }
                            scopes.last()[pName] = VarSlot(pType, if (pType == VarType.INT) v.toInt().toDouble() else v)
                        }
                        executeBlockTokens(fn.bodyTokens)
                        0.0
                    } catch (ret: ReturnSignal) {
                        ret.value
                    } finally {
                        scopes.removeAt(scopes.lastIndex)
                    }
                }
            }
        }

        private fun formatPrintf(fmt: String, args: List<Any>): String {
            val sb = StringBuilder()
            var argIdx = 0
            var i = 0
            while (i < fmt.length) {
                if (fmt[i] == '%' && i + 1 < fmt.length) {
                    if (fmt[i + 1] == '%') {
                        sb.append('%')
                        i += 2
                        continue
                    }
                    var j = i + 1
                    while (j < fmt.length && fmt[j] !in setOf('d', 'i', 'f', 'c', 's', 'l')) j++
                    if (j < fmt.length && fmt[j] == 'l' && fmt.getOrNull(j + 1) == 'f') j++
                    val spec = fmt.substring(i, j + 1)
                    val arg = args.getOrNull(argIdx++)
                    when {
                        spec.endsWith("d") || spec.endsWith("i") -> {
                            sb.append((arg as? Number)?.toInt() ?: 0)
                        }
                        spec.endsWith("f") -> {
                            val precisionMatch = Regex("\\.(\\d+)").find(spec)
                            val precision = precisionMatch?.groupValues?.get(1)?.toIntOrNull() ?: 6
                            val d = (arg as? Number)?.toDouble() ?: 0.0
                            sb.append(String.format(java.util.Locale.US, "%.${precision}f", d))
                        }
                        spec.endsWith("c") -> {
                            val ch = ((arg as? Number)?.toInt() ?: 32).toChar()
                            sb.append(ch)
                        }
                        spec.endsWith("s") -> {
                            sb.append(arg?.toString() ?: "")
                        }
                        else -> sb.append(spec)
                    }
                    i = j + 1
                } else {
                    sb.append(fmt[i])
                    i++
                }
            }
            return sb.toString()
        }
    }
}
