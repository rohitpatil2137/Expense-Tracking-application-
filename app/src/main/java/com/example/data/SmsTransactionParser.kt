package com.example.data

import java.util.regex.Pattern

data class ParsedTransaction(
    val amount: Double,
    val isIncome: Boolean,
    val category: String,
    val note: String,
    val rawBody: String,
    val timestamp: Long
)

object SmsTransactionParser {
    // Regex matches decimal amount with currency prefixes like $, Rs., INR, etc.
    private val amountRegex = Pattern.compile("(?i)(?:rs\\.?|inr|\\$)\\s*([0-9,]+(?:\\.[0-9]{2})?)")
    
    fun parseMessage(body: String, timestamp: Long): ParsedTransaction? {
        val normalized = body.lowercase()
        
        // Ensure it is a financial transaction or notification
        val isDebit = normalized.contains("debit") || normalized.contains("spent") || normalized.contains("withdrawn") || normalized.contains("paid") || normalized.contains("sent")
        val isCredit = normalized.contains("credit") || normalized.contains("deposit") || normalized.contains("received") || normalized.contains("salary")
        
        if (!isDebit && !isCredit) return null
        
        val matcher = amountRegex.matcher(body)
        if (!matcher.find()) return null
        
        val amountStr = matcher.group(1)?.replace(",", "") ?: return null
        val amount = amountStr.toDoubleOrNull() ?: return null
        if (amount <= 0.0) return null
        
        // Classify Category
        var category = "Miscellaneous"
        var isIncome = false
        
        if (isDebit) {
            isIncome = false
            category = when {
                normalized.contains("uber") || normalized.contains("lyft") || normalized.contains("metro") || normalized.contains("cab") || normalized.contains("transport") -> "Transport & Auto"
                normalized.contains("swiggy") || normalized.contains("zomato") || normalized.contains("food") || normalized.contains("restaurant") || normalized.contains("dining") -> "Food & Dining"
                normalized.contains("netflix") || normalized.contains("spotify") || normalized.contains("theatre") || normalized.contains("movie") || normalized.contains("entertainment") -> "Entertainment"
                normalized.contains("amazon") || normalized.contains("walmart") || normalized.contains("target") || normalized.contains("shop") || normalized.contains("mall") -> "Shopping"
                normalized.contains("hospital") || normalized.contains("pharmacy") || normalized.contains("medical") || normalized.contains("doctor") -> "Health & Wellness"
                normalized.contains("bill") || normalized.contains("electric") || normalized.contains("recharge") || normalized.contains("utilities") -> "Utilities & Bills"
                else -> "Miscellaneous"
            }
        } else if (isCredit) {
            isIncome = true
            category = when {
                normalized.contains("salary") -> "Salary"
                normalized.contains("credit card") || normalized.contains("creditcard") || normalized.contains("hdfc") || normalized.contains("icici") || normalized.contains("sbi") -> "Credit Card"
                normalized.contains("slice") -> "Slice"
                else -> "Other Income"
            }
        }
        
        // Extract a clean short source or description
        var note = "SMS Auto Import"
        val parts = body.split(Pattern.compile("(?i)(?:at|to|from|by|at)\\s+"), 2)
        if (parts.size > 1) {
            val potentialSrc = parts[1].split(Pattern.compile("[. \\n]"), 3)
            if (potentialSrc.isNotEmpty()) {
                val cleanWord = potentialSrc[0].trim().replace("[^a-zA-Z]".toRegex(), "")
                if (cleanWord.length > 2) {
                    note = "SMS: ${cleanWord.uppercase()}"
                }
            }
        } else {
            // Pick some keywords
            if (normalized.contains("salary")) {
                note = "Salary Received"
            } else if (normalized.contains("slice")) {
                note = "Slice Cashback/Refund"
            } else if (normalized.contains("credit")) {
                note = "Account Credit"
            }
        }
        
        return ParsedTransaction(
            amount = amount,
            isIncome = isIncome,
            category = category,
            note = note.take(30),
            rawBody = body,
            timestamp = timestamp
        )
    }
}
