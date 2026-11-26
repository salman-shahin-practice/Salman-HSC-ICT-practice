package com.example.engine

data class HtmlCheckResult(
    val passed: Boolean,
    val accuracyPercent: Int,
    val summaryBn: String,
    val warningsBn: List<String>
)

object HtmlAnalyzer {

    private val voidTags = setOf(
        "br", "hr", "img", "input", "meta", "link", "source", "wbr", "!doctype"
    )

    fun analyze(html: String, requiredTagsCsv: String): HtmlCheckResult {
        val lower = html.lowercase()
        val warnings = mutableListOf<String>()

        if ("</img>" in lower) {
            warnings += "<img> একটি Empty ট্যাগ—</img> ক্লোজিং ট্যাগ লেখা যাবে না।"
        }
        if ("</br>" in lower) {
            warnings += "<br> একটি Empty ট্যাগ—</br> লিখতে হয় না।"
        }
        if ("<img" in lower && "alt=" !in lower) {
            warnings += "<img> ট্যাগে alt=\"...\" এট্রিবিউট যোগ করা বোর্ড পরীক্ষার জন্য উত্তম।"
        }
        if ("<a" in lower && "href=" !in lower) {
            warnings += "<a> ট্যাগের ভেতরে href=\"...\" এট্রিবিউট দিতে ভুলে গেছো!"
        }
        if ("<table" in lower && "border=" !in lower && "border:" !in lower) {
            warnings += "টেবিলে দাগ দেখানোর জন্য border=\"1\" অথবা CSS border ব্যবহার করো।"
        }

        // Check basic tag balance for common container tags
        val openCounts = mutableMapOf<String, Int>()
        val closeCounts = mutableMapOf<String, Int>()
        Regex("<(/?)([a-zA-Z][a-zA-Z0-9]*)\\b[^>]*>").findAll(lower).forEach { match ->
            val isClosing = match.groupValues[1] == "/"
            val tag = match.groupValues[2]
            if (tag !in voidTags) {
                if (isClosing) {
                    closeCounts[tag] = (closeCounts[tag] ?: 0) + 1
                } else {
                    openCounts[tag] = (openCounts[tag] ?: 0) + 1
                }
            }
        }
        openCounts.forEach { (tag, openCount) ->
            val closeCount = closeCounts[tag] ?: 0
            if (openCount > closeCount) {
                warnings += "<$tag> ট্যাগটি খোলা হয়েছে কিন্তু </$tag> দিয়ে সঠিকভাবে বন্ধ করা হয়নি।"
            }
        }

        val requiredTokens = requiredTagsCsv
            .split(",")
            .map { it.trim().lowercase() }
            .filter { it.isNotBlank() }

        if (requiredTokens.isEmpty()) {
            val score = if (warnings.isEmpty()) 100 else (100 - warnings.size * 15).coerceAtLeast(50)
            return HtmlCheckResult(
                passed = warnings.isEmpty() && html.isNotBlank(),
                accuracyPercent = score,
                summaryBn = if (warnings.isEmpty()) "চমৎকার! কোনো HTML ত্রুটি পাওয়া যায়নি।" else "কোড রেন্ডার হয়েছে, তবে নিচের সতর্কতাগুলো দেখে নাও।",
                warningsBn = warnings
            )
        }

        val missing = requiredTokens.filter { token ->
            val hasTag = Regex("<$token\\b").containsMatchIn(lower)
            val hasAttr = Regex("\\b$token\\s*=").containsMatchIn(lower)
            !hasTag && !hasAttr
        }

        val matchedCount = requiredTokens.size - missing.size
        val accuracy = ((matchedCount.toFloat() / requiredTokens.size) * 100).toInt()
            .let { if (warnings.isNotEmpty()) (it - 10).coerceAtLeast(30) else it }
            .coerceIn(0, 100)

        missing.forEach { token ->
            warnings += "এই সমস্যার জন্য `$token` ট্যাগ বা এট্রিবিউট ব্যবহার করা প্রয়োজন।"
        }

        val passed = missing.isEmpty() && openCounts.all { (tag, count) -> (closeCounts[tag] ?: 0) >= count }
        val summaryBn = if (passed) {
            "সাবাশ! প্রয়োজনীয় সব HTML ট্যাগ সঠিকভাবে ব্যবহৃত হয়েছে ($accuracy%)।"
        } else {
            "আরও একটু চেষ্টা করো! ${missing.size}টি আবশ্যক ট্যাগ/এট্রিবিউট বাকি আছে।"
        }

        return HtmlCheckResult(
            passed = passed,
            accuracyPercent = accuracy,
            summaryBn = summaryBn,
            warningsBn = warnings
        )
    }

    fun autoCloseSuggestion(code: String): Pair<String, String>? {
        val trimmedEnd = code.trimEnd()
        val match = Regex("<([a-zA-Z][a-zA-Z0-9]*)(\\s[^>]*)?>$").find(trimmedEnd) ?: return null
        val tag = match.groupValues[1].lowercase()
        if (tag in voidTags) return null
        val closing = "</$tag>"
        return tag to closing
    }
}
