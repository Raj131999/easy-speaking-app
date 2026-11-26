package com.example.ui

import java.util.Locale

/**
 * Converts numbers, currencies, percentages, times, ordinals, decimals, years,
 * fractions, and common spoken symbols (e.g. "7$" -> "seven dollars", "$5.50" ->
 * "five dollars and fifty cents", "50%" -> "fifty percent", "18th" -> "eighteenth")
 * into natural spoken English words for both display and pronunciation alignment.
 */
object SpokenTextNormalizer {

    private val ONES = arrayOf(
        "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
        "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen",
        "seventeen", "eighteen", "nineteen"
    )

    private val TENS = arrayOf(
        "", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"
    )

    private val ORDINAL_ONES = arrayOf(
        "zeroth", "first", "second", "third", "fourth", "fifth", "sixth", "seventh",
        "eighth", "ninth", "tenth", "eleventh", "twelfth", "thirteenth", "fourteenth",
        "fifteenth", "sixteenth", "seventeenth", "eighteenth", "nineteenth"
    )

    private val ORDINAL_TENS = arrayOf(
        "", "", "twentieth", "thirtieth", "fortieth", "fiftieth",
        "sixtieth", "seventieth", "eightieth", "ninetieth"
    )

    private val DECADE_PLURALS = mapOf(
        0 to "hundreds",
        10 to "tens",
        20 to "twenties",
        30 to "thirties",
        40 to "forties",
        50 to "fifties",
        60 to "sixties",
        70 to "seventies",
        80 to "eighties",
        90 to "nineties"
    )

    private data class CurrencyInfo(
        val singular: String,
        val plural: String,
        val subSingular: String,
        val subPlural: String
    )

    private val CURRENCIES = mapOf(
        "$" to CurrencyInfo("dollar", "dollars", "cent", "cents"),
        "£" to CurrencyInfo("pound", "pounds", "penny", "pence"),
        "€" to CurrencyInfo("euro", "euros", "cent", "cents"),
        "₹" to CurrencyInfo("rupee", "rupees", "paisa", "paise"),
        "¥" to CurrencyInfo("yen", "yen", "", "")
    )

    private val FAST_CHECK_REGEX = Regex("[0-9\$£€₹¥%°&+=@#]")

    private val TWENTY_FOUR_SEVEN_REGEX = Regex("(?<![A-Za-z0-9])24/7(?![A-Za-z0-9])")

    private val CURRENCY_PREFIX_REGEX = Regex(
        """([\$£€₹¥])\s*(\d{1,3}(?:,\d{3})+|\d+)(?:\.(\d+))?(?:(?:\s*(thousand|million|billion|trillion)(?![A-Za-z]))|(?:([KkMmBbTt])(?![A-Za-z-])))?""",
        RegexOption.IGNORE_CASE
    )

    private val CURRENCY_SUFFIX_REGEX = Regex(
        """(?<![A-Za-z0-9])(\d{1,3}(?:,\d{3})+|\d+)(?:\.(\d+))?(?:(?:\s*(thousand|million|billion|trillion)(?![A-Za-z]))|(?:([KkMmBbTt])(?![A-Za-z-])))?\s*([\$£€₹¥])""",
        RegexOption.IGNORE_CASE
    )

    private val PERCENT_REGEX = Regex(
        """(?<![A-Za-z0-9])(-?)(\d{1,3}(?:,\d{3})+|\d+)(?:\.(\d+))?\s*%"""
    )

    private val TEMPERATURE_REGEX = Regex(
        """(?<![A-Za-z0-9])(-?)(\d+(?:\.\d+)?)\s*°\s*([CcFf])?(?![A-Za-z])"""
    )

    private val TIME_REGEX = Regex(
        """(?<![A-Za-z0-9:])(\d{1,2}):(\d{2})(?:\s*(?:([AaPp])\.[Mm]\.|([AaPp])[Mm]))?(?![A-Za-z0-9])"""
    )

    private val FRACTION_REGEX = Regex(
        """(?<![A-Za-z0-9/])(\d+)\s*/\s*(\d+)(?![A-Za-z0-9/])"""
    )

    private val DECADE_REGEX = Regex(
        """(?<![A-Za-z0-9])(1[1-9]|20)(\d0)[sS](?![A-Za-z0-9])"""
    )

    private val ORDINAL_REGEX = Regex(
        """(?<![A-Za-z0-9])(\d+)(st|nd|rd|th)(?![A-Za-z0-9])""",
        RegexOption.IGNORE_CASE
    )

    private val SHORTHAND_SCALE_REGEX = Regex(
        """(?<![A-Za-z0-9])(\d+(?:\.\d+)?)([KkMBT])(?![A-Za-z0-9-])"""
    )

    private val HASH_NUMBER_REGEX = Regex("""#\s*(\d+)""")

    private val DECIMAL_REGEX = Regex(
        """(?<![A-Za-z0-9.])(-?)(\d{1,3}(?:,\d{3})+|\d+)\.(\d+)(?![A-Za-z0-9.])"""
    )

    private val COMMA_INTEGER_REGEX = Regex(
        """(?<![A-Za-z0-9,])(-?)(\d{1,3}(?:,\d{3})+)(?![A-Za-z0-9,])"""
    )

    private val NEGATIVE_INTEGER_REGEX = Regex(
        """(?<=^|\s)-(\d+)(?![A-Za-z0-9])"""
    )

    private val STANDALONE_INTEGER_REGEX = Regex(
        """(?<![A-Za-z0-9])(\d+)(?![A-Za-z0-9])"""
    )

    private val DIGITS_BEFORE_LETTERS_REGEX = Regex(
        """(?<![A-Za-z0-9])(\d+)([A-Za-z]+)"""
    )

    private val LETTERS_BEFORE_DIGITS_REGEX = Regex(
        """([A-Za-z]+)(\d+)(?![A-Za-z0-9])"""
    )

    fun normalizeToSpokenWords(input: String): String {
        if (input.isBlank() || !FAST_CHECK_REGEX.containsMatchIn(input)) {
            return input
        }

        var result = input

        // 1. Common idiom 24/7
        result = TWENTY_FOUR_SEVEN_REGEX.replace(result, "twenty four seven")

        // 2. Currency prefix: $7, $5.50, $1 million, $120,000, £5, €10, ₹500
        result = CURRENCY_PREFIX_REGEX.replace(result) { m ->
            val symbol = m.groupValues[1]
            val wholeRaw = m.groupValues[2]
            val fracRaw = m.groupValues[3]
            val scaleRaw = m.groupValues[4].ifEmpty { m.groupValues[5] }
            formatCurrency(symbol, wholeRaw, fracRaw, scaleRaw)
        }

        // 3. Currency suffix: 7$, 7 $, 5.50$, 500₹, 10€
        result = CURRENCY_SUFFIX_REGEX.replace(result) { m ->
            val wholeRaw = m.groupValues[1]
            val fracRaw = m.groupValues[2]
            val scaleRaw = m.groupValues[3].ifEmpty { m.groupValues[4] }
            val symbol = m.groupValues[5]
            formatCurrency(symbol, wholeRaw, fracRaw, scaleRaw)
        }

        // 4. Percentages: 50%, 99.9%
        result = PERCENT_REGEX.replace(result) { m ->
            val neg = m.groupValues[1] == "-"
            val wholeRaw = m.groupValues[2]
            val fracRaw = m.groupValues[3]
            val numWords = formatNumberWithOptionalDecimal(neg, wholeRaw, fracRaw)
            "$numWords percent"
        }

        // 5. Temperatures / degrees: 72°F, 20°C, 90°
        result = TEMPERATURE_REGEX.replace(result) { m ->
            val neg = m.groupValues[1] == "-"
            val numRaw = m.groupValues[2]
            val unit = m.groupValues[3].uppercase(Locale.US)
            val parts = numRaw.split(".")
            val numWords = formatNumberWithOptionalDecimal(neg, parts[0], parts.getOrNull(1).orEmpty())
            val unitWord = when (unit) {
                "F" -> " fahrenheit"
                "C" -> " celsius"
                else -> ""
            }
            val degWord = if (numRaw == "1" && !neg) "degree" else "degrees"
            "$numWords $degWord$unitWord"
        }

        // 6. Times: 8:00 AM, 9:30 AM, 3:15, 9:05 PM
        result = TIME_REGEX.replace(result) { m ->
            val hour = m.groupValues[1].toIntOrNull()
            val minute = m.groupValues[2].toIntOrNull()
            val amPmChar = m.groupValues[3].ifEmpty { m.groupValues[4] }.uppercase(Locale.US)
            if (hour == null || minute == null || hour !in 0..23 || minute !in 0..59) {
                m.value
            } else {
                val hourWord = numberToWords(hour.toLong())
                val amPmSuffix = when (amPmChar) {
                    "A" -> " AM"
                    "P" -> " PM"
                    else -> ""
                }
                when {
                    minute == 0 && amPmSuffix.isNotEmpty() -> "$hourWord$amPmSuffix"
                    minute == 0 -> "$hourWord o'clock"
                    minute < 10 -> "$hourWord oh ${numberToWords(minute.toLong())}$amPmSuffix"
                    else -> "$hourWord ${numberToWords(minute.toLong())}$amPmSuffix"
                }
            }
        }

        // 7. Fractions: 1/2, 3/4, 2/3
        result = FRACTION_REGEX.replace(result) { m ->
            val num = m.groupValues[1].toLongOrNull()
            val den = m.groupValues[2].toLongOrNull()
            if (num == null || den == null || den == 0L) {
                m.value
            } else {
                formatFraction(num, den)
            }
        }

        // 8. Decades: 1930s, 1950s, 1960s, 2000s, 2020s
        result = DECADE_REGEX.replace(result) { m ->
            val century = m.groupValues[1].toInt()
            val decade = m.groupValues[2].toInt()
            if (century == 20 && decade == 0) {
                "two thousands"
            } else {
                val centuryWord = numberToWords(century.toLong())
                val decadeWord = DECADE_PLURALS[decade] ?: "${numberToWords(decade.toLong())}s"
                "$centuryWord $decadeWord"
            }
        }

        // 9. Ordinals: 1st, 2nd, 3rd, 18th, 21ST
        result = ORDINAL_REGEX.replace(result) { m ->
            val value = m.groupValues[1].toLongOrNull()
            if (value != null) ordinalToWords(value) else m.value
        }

        // 10. Shorthand multipliers: 10K, 2.5M, 3B
        result = SHORTHAND_SCALE_REGEX.replace(result) { m ->
            val numRaw = m.groupValues[1]
            val scale = expandScaleWord(m.groupValues[2])
            val parts = numRaw.split(".")
            val numWords = formatNumberWithOptionalDecimal(false, parts[0], parts.getOrNull(1).orEmpty())
            "$numWords $scale"
        }

        // 11. Hash number: #1 -> number one
        result = HASH_NUMBER_REGEX.replace(result) { m ->
            val value = m.groupValues[1].toLongOrNull()
            if (value != null) "number ${numberToWords(value)}" else m.value
        }

        // 12. Decimals: 1.2, 2.3, 99.9
        result = DECIMAL_REGEX.replace(result) { m ->
            val neg = m.groupValues[1] == "-"
            val wholeRaw = m.groupValues[2]
            val fracRaw = m.groupValues[3]
            formatNumberWithOptionalDecimal(neg, wholeRaw, fracRaw)
        }

        // 13. Comma-separated integers: 1,000, 28,000, 442,000
        result = COMMA_INTEGER_REGEX.replace(result) { m ->
            val neg = m.groupValues[1] == "-"
            val clean = m.groupValues[2].replace(",", "")
            val prefix = if (neg) "minus " else ""
            val value = clean.toLongOrNull()
            if (value != null) "$prefix${numberToWords(value)}" else m.value
        }

        // 14. Negative standalone integers: -5 -> minus five
        result = NEGATIVE_INTEGER_REGEX.replace(result) { m ->
            val value = m.groupValues[1].toLongOrNull()
            if (value != null) "minus ${numberToWords(value)}" else m.value
        }

        // 15. Standalone integers & 4-digit years: 1992, 2050, 2007, 45-minute, 400
        result = STANDALONE_INTEGER_REGEX.replace(result) { m ->
            val raw = m.groupValues[1]
            val value = raw.toLongOrNull()
            when {
                value == null -> spellDigits(raw)
                raw.length == 4 && !raw.startsWith("0") && value in 1100L..2099L -> yearToWords(value.toInt())
                else -> numberToWords(value)
            }
        }

        // 16. Alphanumeric tokens: 3D -> three D, B12 -> B twelve, CO2 -> CO two
        result = DIGITS_BEFORE_LETTERS_REGEX.replace(result) { m ->
            val value = m.groupValues[1].toLongOrNull()
            val letters = m.groupValues[2]
            if (value != null) "${numberToWords(value)} $letters" else m.value
        }
        result = LETTERS_BEFORE_DIGITS_REGEX.replace(result) { m ->
            val letters = m.groupValues[1]
            val value = m.groupValues[2].toLongOrNull()
            if (value != null) "$letters ${numberToWords(value)}" else m.value
        }

        // 17. Remaining standalone symbols
        result = result
            .replace("&", " and ")
            .replace("+", " plus ")
            .replace("=", " equals ")
            .replace("@", " at ")
            .replace("%", " percent ")
            .replace("$", " dollars ")
            .replace("£", " pounds ")
            .replace("€", " euros ")
            .replace("₹", " rupees ")
            .replace("¥", " yen ")
            .replace("#", " number ")

        // 18. Normalize whitespace without disturbing newlines or punctuation attachment
        result = result
            .replace(Regex("[ \\t]+"), " ")
            .replace(Regex(" ([.,!?;:])"), "$1")
            .trim()

        return result
    }

    private fun formatCurrency(
        symbol: String,
        wholeRaw: String,
        fracRaw: String,
        scaleRaw: String
    ): String {
        val info = CURRENCIES[symbol] ?: CurrencyInfo("dollar", "dollars", "cent", "cents")
        val cleanWhole = wholeRaw.replace(",", "")
        val wholeVal = cleanWhole.toLongOrNull() ?: 0L

        if (scaleRaw.isNotBlank()) {
            val scaleWord = expandScaleWord(scaleRaw)
            val amountWords = formatNumberWithOptionalDecimal(false, cleanWhole, fracRaw)
            return "$amountWords $scaleWord ${info.plural}"
        }

        if (fracRaw.isNotBlank()) {
            if (fracRaw.length <= 2 && info.subPlural.isNotEmpty()) {
                val centsVal = if (fracRaw.length == 1) {
                    (fracRaw.toIntOrNull() ?: 0) * 10
                } else {
                    fracRaw.toIntOrNull() ?: 0
                }
                val wholePart = if (wholeVal > 0L || centsVal == 0) {
                    val unit = if (wholeVal == 1L) info.singular else info.plural
                    "${numberToWords(wholeVal)} $unit"
                } else {
                    ""
                }
                val subPart = if (centsVal > 0) {
                    val subUnit = if (centsVal == 1) info.subSingular else info.subPlural
                    "${numberToWords(centsVal.toLong())} $subUnit"
                } else {
                    ""
                }
                return when {
                    wholePart.isNotEmpty() && subPart.isNotEmpty() -> "$wholePart and $subPart"
                    wholePart.isNotEmpty() -> wholePart
                    subPart.isNotEmpty() -> subPart
                    else -> "zero ${info.plural}"
                }
            } else {
                val amountWords = formatNumberWithOptionalDecimal(false, cleanWhole, fracRaw)
                return "$amountWords ${info.plural}"
            }
        }

        val unit = if (wholeVal == 1L) info.singular else info.plural
        return "${numberToWords(wholeVal)} $unit"
    }

    private fun expandScaleWord(raw: String): String = when (raw.lowercase(Locale.US)) {
        "k", "thousand" -> "thousand"
        "m", "million" -> "million"
        "b", "billion" -> "billion"
        "t", "trillion" -> "trillion"
        else -> raw.lowercase(Locale.US)
    }

    private fun formatNumberWithOptionalDecimal(
        negative: Boolean,
        wholeRaw: String,
        fracRaw: String
    ): String {
        val cleanWhole = wholeRaw.replace(",", "")
        val wholeVal = cleanWhole.toLongOrNull()
        val wholeWords = if (wholeVal != null) numberToWords(wholeVal) else spellDigits(cleanWhole)
        val prefix = if (negative) "minus " else ""
        return if (fracRaw.isNotBlank()) {
            "$prefix$wholeWords point ${spellDigits(fracRaw)}"
        } else {
            "$prefix$wholeWords"
        }
    }

    private fun formatFraction(num: Long, den: Long): String {
        val numWord = numberToWords(num)
        val denWord = when (den) {
            2L -> if (num == 1L) "half" else "halves"
            4L -> if (num == 1L) "quarter" else "quarters"
            else -> {
                val ord = ordinalToWords(den)
                if (num == 1L) ord else "${ord}s"
            }
        }
        return "$numWord $denWord"
    }

    private fun yearToWords(year: Int): String {
        if (year == 2000) return "two thousand"
        if (year in 2001..2009) return "two thousand ${numberToWords((year - 2000).toLong())}"
        val hi = year / 100
        val lo = year % 100
        val hiWord = numberToWords(hi.toLong())
        return when {
            lo == 0 -> "$hiWord hundred"
            lo < 10 -> "$hiWord oh ${numberToWords(lo.toLong())}"
            else -> "$hiWord ${numberToWords(lo.toLong())}"
        }
    }

    private fun spellDigits(digits: String): String {
        return digits.mapNotNull { ch ->
            if (ch in '0'..'9') ONES[ch - '0'] else null
        }.joinToString(" ")
    }

    fun numberToWords(n: Long): String {
        if (n < 0L) {
            return if (n == Long.MIN_VALUE) "minus ${spellDigits(n.toString().removePrefix("-"))}"
            else "minus ${numberToWords(-n)}"
        }
        if (n < 20L) return ONES[n.toInt()]
        if (n < 100L) {
            val t = (n / 10).toInt()
            val r = (n % 10).toInt()
            return if (r == 0) TENS[t] else "${TENS[t]} ${ONES[r]}"
        }
        if (n < 1000L) {
            val h = (n / 100).toInt()
            val r = n % 100
            return if (r == 0L) "${ONES[h]} hundred" else "${ONES[h]} hundred ${numberToWords(r)}"
        }

        val scales = listOf(
            1_000_000_000_000L to "trillion",
            1_000_000_000L to "billion",
            1_000_000L to "million",
            1_000L to "thousand"
        )
        var remainder = n
        val parts = mutableListOf<String>()
        for ((unitVal, unitName) in scales) {
            if (remainder >= unitVal) {
                val count = remainder / unitVal
                parts.add("${numberToWords(count)} $unitName")
                remainder %= unitVal
            }
        }
        if (remainder > 0L) {
            parts.add(numberToWords(remainder))
        }
        return parts.joinToString(" ")
    }

    fun ordinalToWords(n: Long): String {
        if (n < 0L) return "minus ${ordinalToWords(-n)}"
        if (n < 20L) return ORDINAL_ONES[n.toInt()]
        if (n < 100L) {
            val t = (n / 10).toInt()
            val r = (n % 10).toInt()
            return if (r == 0) ORDINAL_TENS[t] else "${TENS[t]} ${ORDINAL_ONES[r]}"
        }
        val rem = n % 100
        if (rem == 0L) {
            return "${numberToWords(n)}th"
        }
        val prefix = numberToWords(n - rem)
        return "$prefix ${ordinalToWords(rem)}"
    }
}
