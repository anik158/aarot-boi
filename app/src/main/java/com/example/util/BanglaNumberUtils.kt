package com.example.util

object BanglaNumberUtils {

    private val banglaDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    private val englishDigits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')

    /**
     * Converts a Bangla or English digit string to standard English digit string.
     * Useful before parsing to Int or Double.
     */
    fun toEnglishDigits(input: String): String {
        val sb = StringBuilder()
        for (ch in input) {
            val banglaIndex = banglaDigits.indexOf(ch)
            if (banglaIndex != -1) {
                sb.append(englishDigits[banglaIndex])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString().trim()
    }

    /**
     * Converts any standard English digit string or number to Bangla digits.
     */
    fun toBanglaDigits(input: String): String {
        val sb = StringBuilder()
        for (ch in input) {
            val engIndex = englishDigits.indexOf(ch)
            if (engIndex != -1) {
                sb.append(banglaDigits[engIndex])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun toBanglaDigits(number: Long): String {
        return toBanglaDigits(number.toString())
    }

    fun toBanglaDigits(number: Int): String {
        return toBanglaDigits(number.toString())
    }

    /**
     * Formats currency according to Bangladeshi South Asian grouping (e.g. 1,85,500 or 12,50,000)
     * with ৳ currency sign in Bangla.
     */
    fun formatCurrency(amount: Double): String {
        val isNegative = amount < 0
        val longVal = kotlin.math.abs(amount).toLong()
        val str = longVal.toString()
        val formatted = if (str.length > 3) {
            val last3 = str.takeLast(3)
            val remaining = str.dropLast(3)
            val sb = StringBuilder()
            var count = 0
            for (i in remaining.length - 1 downTo 0) {
                sb.append(remaining[i])
                count++
                if (count == 2 && i != 0) {
                    sb.append(',')
                    count = 0
                }
            }
            sb.reverse().append(',').append(last3).toString()
        } else {
            str
        }
        val prefix = if (isNegative) "-৳" else "৳"
        return prefix + toBanglaDigits(formatted)
    }

    fun formatNumber(number: Int): String {
        return formatCurrency(number.toDouble()).removePrefix("৳")
    }

    fun formatNumber(number: Double): String {
        return formatCurrency(number).removePrefix("৳")
    }
}
